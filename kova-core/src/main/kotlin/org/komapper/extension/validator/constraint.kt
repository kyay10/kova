package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.ensure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import org.komapper.extension.validator.Message.Resource

typealias Constraint<T> = context(ValidationContext, RaiseAccumulate<FailureDetail>) (T) -> Unit

/**
 * Evaluates a condition and raises a failure with the provided message if false.
 *
 * Example with Message object:
 * ```kotlin
 * satisfies(value > 0) { Message.Resource("custom.positive", value) }
 * ```
 *
 * @param condition The condition to evaluate
 * @param message The error message to use if the condition is false
 * @return The constraint result
 */
context(_: ValidationContext, r: Raise<FailureDetail>)
inline fun satisfies(condition: Boolean, message: () -> Message) {
    contract {
        returns() implies condition
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        !condition holdsIn message
    }
    r.ensure(condition) { message().failure }
}

/**
 * Evaluates a condition and raises a failure with the provided message if false.
 *
 * Example with simple string:
 * ```kotlin
 * satisfies(value > 0) { "Value must be positive" }
 * ```
 *
 * @param condition The condition to evaluate
 * @param message The error message text to use if the condition is false
 * @return The constraint result
 */
@JvmName("satisfiesString")
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
context(_: ValidationContext, r: Raise<FailureDetail>)
inline fun satisfies(condition: Boolean, message: () -> String) {
    contract {
        returns() implies condition
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        !condition holdsIn message
    }
    r.ensure(condition) { message().failure }
}

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
@IgnorableReturnValue
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <T> T.notNull(message: () -> Message = { Resource("kova.nullable.notNull") }): T & Any {
    contract {
        returns() implies (this@notNull != null)
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        (this@notNull == null) holdsIn message
    }
    satisfies(this != null) { message() }
    return this
}

context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <T : Any> T?.isNull(message: (T) -> Message = { Resource("kova.nullable.isNull", it) }) {
    contract { returns() implies (this@isNull == null) }
    satisfies(this == null) { message(this!!) }
}