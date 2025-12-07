package org.komapper.extension.validator

import arrow.core.raise.context.Raise

/**
 * Validates that the number is greater than or equal to the specified minimum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().min(0)
 * validator.validate(10)  // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value Minimum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Comparable<T>> T.min(value: T, message: MessageProvider1<T, T>) =
    satisfies(this >= value) { message(this, value) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T : Comparable<T>> T.min(value: T) = min(value, Message.resource1("kova.number.min"))

/**
 * Validates that the number is less than or equal to the specified maximum value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().max(100)
 * validator.validate(50)   // Success
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value Maximum value (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Comparable<T>> T.max(value: T, message: MessageProvider1<T, T>) =
    satisfies(this <= value) { message(this, value) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T : Comparable<T>> T.max(value: T) = max(value, Message.resource1("kova.number.max"))

/**
 * Validates that the number is strictly greater than the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().gt(0)
 * validator.validate(1)   // Success
 * validator.validate(0)   // Failure
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Comparable<T>> T.gt(value: T, message: MessageProvider1<T, T>) =
    satisfies(this > value) { message(this, value) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T : Comparable<T>> T.gt(value: T) = gt(value, Message.resource1("kova.number.gt"))

/**
 * Validates that the number is greater than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().gte(0)
 * validator.validate(1)   // Success
 * validator.validate(0)   // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the greater-than-or-equal constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Comparable<T>> T.gte(value: T, message: MessageProvider1<T, T>) = min(value, message)

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T : Comparable<T>> T.gte(value: T) = gte(value, Message.resource1("kova.number.gte"))

/**
 * Validates that the number is strictly less than the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().lt(100)
 * validator.validate(50)   // Success
 * validator.validate(100)  // Failure
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value The value to compare against (exclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Comparable<T>> T.lt(value: T, message: MessageProvider1<T, T>) =
    satisfies(this < value) { message(this, value) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T : Comparable<T>> T.lt(value: T) = lt(value, Message.resource1("kova.number.lt"))

/**
 * Validates that the number is less than or equal to the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().lte(100)
 * validator.validate(50)   // Success
 * validator.validate(100)  // Success
 * validator.validate(150)  // Failure
 * ```
 *
 * @param value The value to compare against (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the less-than-or-equal constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Comparable<T>> T.lte(value: T, message: MessageProvider1<T, T>) = max(value, message)

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <T : Comparable<T>> T.lte(value: T) = lte(value, Message.resource1("kova.number.lte"))

/**
 * Validates that the number is positive (greater than zero).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().positive()
 * validator.validate(1)   // Success
 * validator.validate(0)   // Failure
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the positive constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Number> T.positive(message: MessageProvider0<T> = Message.resource0("kova.number.positive")) =
    satisfies(toDouble() > 0.0) { message(this) }

/**
 * Validates that the number is negative (less than zero).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().negative()
 * validator.validate(-1)  // Success
 * validator.validate(0)   // Failure
 * validator.validate(1)   // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the negative constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Number> T.negative(message: MessageProvider0<T> = Message.resource0("kova.number.negative")) =
    satisfies(toDouble() < 0.0) { message(this) }

/**
 * Validates that the number is not positive (less than or equal to zero).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().notPositive()
 * validator.validate(-1)  // Success
 * validator.validate(0)   // Success
 * validator.validate(1)   // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-positive constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Number> T.notPositive(message: MessageProvider0<T> = Message.resource0("kova.number.notPositive")) =
    satisfies(toDouble() <= 0.0) { message(this) }

/**
 * Validates that the number is not negative (greater than or equal to zero).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.int().notNegative()
 * validator.validate(0)   // Success
 * validator.validate(1)   // Success
 * validator.validate(-1)  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-negative constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
fun <T : Number> T.notNegative(message: MessageProvider0<T> = Message.resource0("kova.number.notNegative")) =
    satisfies(toDouble() >= 0.0) { message(this) }
