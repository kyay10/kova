package org.komapper.extension.validator

/**
 * Type alias for validators that check inputs of type T
 *
 * This simplifies type signatures for validators that validate but don't transform the type,
 * such as string validators, number validators, and most primitive type validators.
 *
 * Example:
 * ```kotlin
 * // Instead of: Validator<String, Unit>
 * val validator: Constraint<String> = Kova.string().min(1).max(10)
 * ```
 */
typealias Constraint<T> = Validator<T, Unit>

/**
 * Validates that the input equals the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().literal("admin")
 * validator.validate("admin") // Success
 * validator.validate("user")  // Failure
 * ```
 *
 * @param value The expected value
 * @param message Custom error message provider
 * @return A new validator that accepts only the specified value
 */
fun <T, S> Validator<T, S>.literal(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.literal.single"),
) = constrain(message.id) {
    satisfies(it == value) { message(it, value) }
}

/**
 * Validates that the input is one of the specified values.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().literal(listOf("admin", "user", "guest"))
 * validator.validate("admin") // Success
 * validator.validate("other") // Failure
 * ```
 *
 * @param values The list of acceptable values
 * @param message Custom error message provider
 * @return A new validator that accepts only values from the list
 */
fun <T, S> Validator<T, S>.literal(
    values: List<T>,
    message: MessageProvider1<T, List<T>> = Message.resource1("kova.literal.list"),
) = constrain(message.id) {
    satisfies(it in values) { message(it, values) }
}

/**
 * Adds a custom constraint to this validator.
 *
 * This is a fundamental building block for creating custom validation rules.
 * The constraint is chained to the existing validator, executing after it succeeds.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().constrain("alphanumeric") {
 *     satisfies(it.all { c -> c.isLetterOrDigit() }, "Must be alphanumeric")
 * }
 * ```
 *
 * @param id Unique identifier for the constraint (used for error tracking)
 * @param check Constraint logic
 * @return A new validator with the constraint applied
 */
fun <T, S> Validator<T, S>.constrain(
    id: String,
    check: Constraint<T>,
): Validator<T, S> = constrain { org.komapper.extension.validator.constrain(id) { check(it) } }

infix fun <T, S> Validator<T, S>.constrain(
    check: Constraint<T>,
): Validator<T, S> = {
    val result by accumulating { this(it) }
    check(it)
    result
}

/**
 * Conditionally applies this validator based on a predicate.
 *
 * If the condition returns false, validation passes automatically without executing
 * this validator. This is useful for conditional validation logic.
 *
 * Example:
 * ```kotlin
 * // Only validate email format if the string looks like an email
 * val validator = Kova.string()
 *     .email()
 *     .onlyIf { it.contains("@") }
 *
 * // More practical: validate discount code only if provided
 * val discountValidator = Kova.string()
 *     .min(5)
 *     .onlyIf { it.isNotBlank() }
 * ```
 *
 * @param condition Predicate that determines whether to apply this validator
 * @return A new validator that conditionally validates
 */
fun <T> Constraint<T>.onlyIf(condition: (T) -> Boolean): Constraint<T> = { input ->
    if (condition(input)) this(input)
}