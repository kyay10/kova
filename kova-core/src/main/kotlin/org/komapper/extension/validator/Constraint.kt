package org.komapper.extension.validator

import arrow.core.right
import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.ensure
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

/**
 * Represents a validation constraint that can be applied to a value.
 *
 * Constraints are used to define custom validation rules that go beyond simple type checks.
 * They are commonly used in ObjectSchema for object-level validation that involves multiple fields.
 *
 * Example of a custom constraint:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * object PeriodSchema : ObjectSchema<Period>({
 *     constrain("dateRange") { context ->
 *         satisfies(
 *             context.input.startDate <= context.input.endDate,
 *             "Start date must be before or equal to end date"
 *         )
 *     }
 * }) {
 *     val startDate = Period::startDate { Kova.localDate() }
 *     val endDate = Period::endDate { Kova.localDate() }
 * }
 * ```
 *
 * @param T The type of value this constraint validates
 * @property id A unique identifier for this constraint
 * @property check The validation logic
 */
data class Constraint<T>(
    val id: String,
    val check: context(ValidationContext, RaiseAccumulate<FailureDetail>) (T) -> Unit,
) {
    companion object {
        /**
         * Creates a constraint that is always satisfied.
         *
         * Used internally as a default constraint.
         */
        fun <T> satisfied(): Constraint<T> = Constraint("kova.satisfied") { Unit.right() }
    }
}

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