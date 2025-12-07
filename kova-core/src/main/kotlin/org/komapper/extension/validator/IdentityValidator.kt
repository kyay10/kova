package org.komapper.extension.validator

import arrow.core.raise.context.Raise

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
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <T> T.literal(value: T, message: (T, T) -> Message) =
    satisfies(this == value) { message(this, value) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T> T.literal(value: T) = literal(value, Message.resource1("kova.literal.single"))

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
 * @param this The list of acceptable values
 * @param message Custom error message provider
 * @return A new validator that accepts only values from the list
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <T> T.literal(values: List<T>, message: (T, List<T>) -> Message) =
    satisfies(this in values) { message(this, values) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T> T.literal(values: List<T>) = literal<T>(values, Message.resource1("kova.literal.list"))