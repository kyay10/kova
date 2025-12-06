package org.komapper.extension.validator

/**
 * Converts a non-nullable validator to a nullable validator.
 *
 * The resulting validator accepts null values and passes them through unchanged.
 * Non-null values are validated using the original validator.
 *
 * Example:
 * ```kotlin
 * val nonNullValidator = Kova.string().min(3).max(10)
 * val nullableValidator = nonNullValidator.asNullable()
 *
 * nullableValidator.validate(null)    // Success: null
 * nullableValidator.validate("hello") // Success: "hello"
 * nullableValidator.validate("ab")    // Failure: too short
 * ```
 *
 * @return A new nullable validator that accepts null input
 */
@JvmName("asNullableConstraint")
fun <T : Any> Constraint<T>.asNullable(): Constraint<T?> = { input ->
    addLog("Validator.asNullable") { if (input != null) this(input) }
}

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().notNull()
 * validator.validate("hello") // Success: "hello"
 * validator.validate(null)    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that rejects null
 */
fun <T : Any, S> Validator<T?, S>.notNull(
    message: MessageProvider0<T?> = Message.resource0("kova.nullable.notNull"),
): Validator<T?, S> = constrain(message.id, Constraints.notNull(message))

/**
 * Provides a default value for null inputs.
 *
 * If the input is null, the validator returns the default value instead.
 * This converts the validator to a [WithDefaultNullableValidator] that produces non-nullable output.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3).asNullable().withDefault("default")
 * validator.validate(null)    // Success: "default"
 * validator.validate("hello") // Success: "hello"
 * validator.validate("ab")    // Failure: too short
 * ```
 *
 * @param defaultValue The value to use when input is null
 * @return A new validator with non-nullable output that uses the default for null inputs
 */
fun <T, S : Any> Validator<T, S?>.withDefault(defaultValue: S): Validator<T, S> = withDefault { defaultValue }

/**
 * Provides a lazily-evaluated default value for null inputs.
 *
 * If the input is null, the provider function is called to generate the default value.
 * This is useful when the default value is expensive to compute.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable()
 *     .withDefault { generateDefaultValue() }
 *
 * validator.validate(null)    // Success: result of generateDefaultValue()
 * validator.validate("hello") // Success: "hello"
 * ```
 *
 * @param provide Function that generates the default value
 * @return A new validator with non-nullable output that uses the provided default for null inputs
 */
fun <T, S : Any> Validator<T, S?>.withDefault(provide: () -> S): Validator<T, S> = map { it ?: provide() }