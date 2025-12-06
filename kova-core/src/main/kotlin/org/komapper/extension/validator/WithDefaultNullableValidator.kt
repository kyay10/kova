package org.komapper.extension.validator

/**
 * Converts a non-nullable validator to a nullable validator with a default value.
 *
 * When the input is null, the validator returns the provided default value.
 * This ensures the output is always non-null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3).asNullable("default")
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param defaultValue The value to use when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(defaultValue: S): Validator<T?, S> = asNullable { defaultValue }

@JvmName("asNullableConstraint")
fun <T : Any> Constraint<T>.asNullable(defaultValue: T): Validator<T?, T> = asNullable { defaultValue }

/**
 * Converts a non-nullable validator to a nullable validator with a lazily-evaluated default value.
 *
 * When the input is null, the provider function is called to generate the default value.
 * This is useful when the default value is expensive to compute or needs to be fresh each time.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable { UUID.randomUUID().toString() }
 * validator.validate(null)    // Success: newly generated UUID
 * validator.validate("hello") // Success: "hello"
 * ```
 *
 * @param withDefault Function that generates the default value when input is null
 * @return A new validator that accepts null input but produces non-nullable output
 */
fun <T : Any, S : Any> Validator<T, S>.asNullable(withDefault: () -> S): Validator<T?, S> = { input ->
    val defaultValue = withDefault()
    addLog("Validator.asNullable(defaultValue=$defaultValue)") {
        if (input == null) defaultValue else this(input)
    }
}

@JvmName("asNullableConstraint")
fun <T : Any> Constraint<T>.asNullable(withDefault: () -> T): Validator<T?, T> = { input ->
    val defaultValue = withDefault()
    addLog("Constraint.asNullable(defaultValue=$defaultValue)") {
        input?.also { this(input) } ?: defaultValue
    }
}

/**
 * Validates that the input is null.
 *
 * This constraint fails if the input is non-null.
 * Note that even though this validator has a default, this constraint checks
 * the input before the default is applied.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable("default").isNull()
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that only accepts null input
 */
fun <T : Any, S : Any> Validator<T?, S>.isNull(
    message: MessageProvider0<T?> = Message.resource0("kova.nullable.isNull"),
): Validator<T?, S> = constrain(message.id, Constraints.isNull(message))