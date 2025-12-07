package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import org.komapper.extension.validator.Message.Resource

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
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <K, V> Map<K, V>.min(size: Int, message: (Map<K, V>, Int, Int) -> Message) =
    satisfies(this.size >= size) { message(this, this.size, size) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <K, V> Map<K, V>.min(size: Int) = min(size, Message.resource2("kova.map.min"))

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
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <K, V> Map<K, V>.max(size: Int, message: (Map<K, V>, Int, Int) -> Message) =
    satisfies(this.size <= size) { message(this, this.size, size) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <K, V> Map<K, V>.max(size: Int) = max(size, Message.resource2("kova.map.max"))

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
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <K, V> Map<K, V>.notEmpty(message: (Map<K, V>) -> Message = { Resource("kova.map.notEmpty", it) }) =
    satisfies(isNotEmpty()) { message(this) }

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
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <K, V> Map<K, V>.length(size: Int, message: (Map<K, V>, Int) -> Message) =
    satisfies(this.size == size) { message(this, size) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <K, V> Map<K, V>.length(size: Int) = length(size, Message.resource1("kova.map.length"))

/**
 * Validates each entry (key-value pair) of the map using the specified validator.
 *
 * If any entry fails validation, the entire map validation fails.
 * Error paths include entry information for better error reporting.
 *
 * Example:
 * ```kotlin
 * val entryValidator = Validation<Map.Entry<String, Int>, Map.Entry<String, Int>> { entry, ctx ->
 *     if (entry.key.length >= 2 && entry.value >= 0) {
 *         ValidationResult.Success(entry, ctx)
 *     } else {
 *         ValidationResult.Failure(/* ... */)
 *     }
 * }
 * val validator = Kova.map<String, Int>().onEach(entryValidator)
 * ```
 *
 * @param constraint The validator to apply to each entry
 * @return A new validator with per-entry validation
 */
context(_: ValidationContext, _: Accumulate<FailureDetail>)
infix fun <K, V> Map<K, V>.onEach(constraint: Constraint<Map.Entry<K, V>>) =
    appendPath("<map entry>") { validateOnEach { constraint(it) } }

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
 * @param constraint The validator to apply to each key
 * @return A new validator with per-key validation
 */
context(_: ValidationContext, _: Accumulate<FailureDetail>)
infix fun <K, V> Map<K, V>.onEachKey(constraint: Constraint<K>) =
    appendPath("<map key>") { validateOnEach { constraint(it.key) } }

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
 * @param constraint The validator to apply to each value
 * @return A new validator with per-value validation
 */
context(_: ValidationContext, _: Accumulate<FailureDetail>)
infix fun <K, V> Map<K, V>.onEachValue(constraint: Constraint<V>) =
    validateOnEach { appendPath("[${it.key}]<map value>") { constraint(it.value) } }

context(_: Accumulate<FailureDetail>)
private fun <K, V> Map<K, V>.validateOnEach(validate: context(RaiseAccumulate<FailureDetail>) (Map.Entry<K, V>) -> Unit) =
    forEach { accumulatingUnit { validate(it) } }