package org.komapper.extension.validator

/**
 * Type alias for map validators.
 *
 * Provides a convenient type for validators that work with Map types.
 *
 * @param K The key type of the map
 * @param V The value type of the map
 */
typealias MapValidator<K, V> = IdentityValidator<Map<K, V>>

/**
 * Validates that the map size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().min(2)
 * validator.validate(mapOf("a" to 1, "b" to 2, "c" to 3)) // Success
 * validator.validate(mapOf("a" to 1))                     // Failure
 * ```
 *
 * @param size Minimum map size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum size constraint
 */
fun <K, V> MapValidator<K, V>.min(
    size: Int,
    message: MessageProvider2<Map<K, V>, Int, Int> = Message.resource2("kova.map.min"),
) = constrain(message.id) {
    satisfies(it.size >= size, message(it, it.size, size))
}

/**
 * Validates that the map size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().max(3)
 * validator.validate(mapOf("a" to 1, "b" to 2))                // Success
 * validator.validate(mapOf("a" to 1, "b" to 2, "c" to 3, "d" to 4)) // Failure
 * ```
 *
 * @param size Maximum map size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum size constraint
 */
fun <K, V> MapValidator<K, V>.max(
    size: Int,
    message: MessageProvider2<Map<K, V>, Int, Int> = Message.resource2("kova.map.max"),
) = constrain(message.id) {
    satisfies(it.size <= size, message(it, it.size, size))
}

/**
 * Validates that the map is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().notEmpty()
 * validator.validate(mapOf("a" to 1)) // Success
 * validator.validate(mapOf())         // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
fun <K, V> MapValidator<K, V>.notEmpty(message: MessageProvider0<Map<K, V>> = Message.resource0("kova.map.notEmpty")) =
    constrain(message.id) {
        satisfies(it.isNotEmpty(), message(it))
    }

/**
 * Validates that the map size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>().length(3)
 * validator.validate(mapOf("a" to 1, "b" to 2, "c" to 3)) // Success
 * validator.validate(mapOf("a" to 1, "b" to 2))           // Failure
 * ```
 *
 * @param size Exact map size required
 * @param message Custom error message provider
 * @return A new validator with the exact size constraint
 */
fun <K, V> MapValidator<K, V>.length(
    size: Int,
    message: MessageProvider1<Map<K, V>, Int> = Message.resource1("kova.map.length"),
) = constrain(message.id) {
    satisfies(it.size == size, message(it, size))
}

/**
 * Validates each entry (key-value pair) of the map using the specified validator.
 *
 * If any entry fails validation, the entire map validation fails.
 * Error paths include entry information for better error reporting.
 *
 * Example:
 * ```kotlin
 * val entryValidator = Validator<Map.Entry<String, Int>, Map.Entry<String, Int>> { entry, ctx ->
 *     if (entry.key.length >= 2 && entry.value >= 0) {
 *         ValidationResult.Success(entry, ctx)
 *     } else {
 *         ValidationResult.Failure(/* ... */)
 *     }
 * }
 * val validator = Kova.map<String, Int>().onEach(entryValidator)
 * ```
 *
 * @param validator The validator to apply to each entry
 * @return A new validator with per-entry validation
 */
fun <K, V> MapValidator<K, V>.onEach(validator: Validator<Map.Entry<K, V>, *>) =
    constrain("kova.map.onEach") {
        validateOnEach(it) { entry ->
            appendPath("<map entry>") { validator.execute(entry) }
        }
    }

/**
 * Validates each key of the map using the specified validator.
 *
 * If any key fails validation, the entire map validation fails.
 * Error paths include key information for better error reporting.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>()
 *     .notEmpty()
 *     .onEachKey(Kova.string().min(2).max(10))
 *
 * validator.validate(mapOf("abc" to 1, "def" to 2)) // Success
 * validator.validate(mapOf("a" to 1, "b" to 2))     // Failure: keys too short
 * ```
 *
 * @param validator The validator to apply to each key
 * @return A new validator with per-key validation
 */
fun <K, V> MapValidator<K, V>.onEachKey(validator: Validator<K, *>) =
    constrain("kova.map.onEachKey") {
        validateOnEach(it) { entry ->
            appendPath("<map key>") { validator.execute(entry.key) }
        }
    }

/**
 * Validates each value of the map using the specified validator.
 *
 * If any value fails validation, the entire map validation fails.
 * Error paths include the associated key for better error reporting.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.map<String, Int>()
 *     .notEmpty()
 *     .onEachValue(Kova.int().min(0).max(100))
 *
 * validator.validate(mapOf("a" to 10, "b" to 20))  // Success
 * validator.validate(mapOf("a" to -1, "b" to 150)) // Failure: values out of range
 * ```
 *
 * @param validator The validator to apply to each value
 * @return A new validator with per-value validation
 */
fun <K, V> MapValidator<K, V>.onEachValue(validator: Validator<V, *>) = constrain("kova.map.onEachValue") {
    validateOnEach(it) { entry ->
        appendPath("[${entry.key}]<map value>") { validator.execute(entry.value) }
    }
}

context(_: ValidationContext)
private fun <K, V, T> validateOnEach(
    input: Map<K, V>,
    validate: (Map.Entry<K, V>) -> ValidationResult<T>,
): ConstraintResult {
    val failures = mutableListOf<ValidationResult.Failure>()
    for (entry in input.entries) {
        val result = validate(entry)
        if (result.isFailure()) {
            failures.add(result)
            if (failFast) break
        }
    }
    val failureDetails = failures.flatMap { it.details }
    return satisfies(failureDetails.isEmpty(), Message.ValidationFailure(details = failureDetails))
}
