package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.ensure
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

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
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun satisfies(condition: Boolean, message: () -> Message) {
    contract { returns() implies condition }
    ensure(condition) { message().failure }
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
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun satisfies(condition: Boolean, message: () -> String) {
    contract { returns() implies condition }
    ensure(condition) { message().failure }
}