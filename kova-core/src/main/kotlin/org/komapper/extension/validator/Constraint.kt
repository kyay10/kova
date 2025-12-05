package org.komapper.extension.validator

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.right
import arrow.core.left
import arrow.core.nel

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
 * @property check The validation logic that returns a [ConstraintResult]
 */
data class Constraint<T>(
    val id: String,
    val check: context(ValidationContext) (T) -> ConstraintResult,
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
 * Result of applying a constraint to a value.
 *
 * Either [Unit] if the constraint passes, or [Message] if it fails.
 */
typealias ConstraintResult = EitherNel<FailureDetail, Unit>

/**
 * Scope available within constraint validation logic.
 *
 * Provides helper methods for evaluating conditions and producing constraint results.
 *
 * Example usage:
 * ```kotlin
 * constrain("range") { context ->
 *     satisfies(
 *         context.input in 1..100,
 *         "Value must be between 1 and 100"
 *     )
 * }
 * ```
 */

/**
 * Evaluates a condition and returns the appropriate constraint result.
 *
 * Returns [Unit] if the condition is true,
 * or [Message] with the given message if false.
 *
 * Example with Message object:
 * ```kotlin
 * satisfies(
 *     value > 0,
 *     Message.Resource("custom.positive", value)
 * )
 * ```
 *
 * @param condition The condition to evaluate
 * @param message The error message to use if the condition is false
 * @return The constraint result
 */
context(_: ValidationContext)
fun satisfies(
    condition: Boolean,
    message: Message,
): ConstraintResult = if (condition) Unit.right() else message.details.left()

context(c: ValidationContext)
private val Message.details
    get() = when (this) {
        is Message.Text, is Message.Resource -> SimpleFailureDetail(contextOf<ValidationContext>(), this).nel()
        is Message.ValidationFailure -> details
    }

/**
 * Evaluates a condition and returns the appropriate constraint result.
 *
 * Returns [Unit] if the condition is true,
 * or [Message] with the given text message if false.
 *
 * Example with simple string:
 * ```kotlin
 * satisfies(
 *     value > 0,
 *     "Value must be positive"
 * )
 * ```
 *
 * @param condition The condition to evaluate
 * @param message The error message text to use if the condition is false
 * @return The constraint result
 */
context(_: ValidationContext)
fun satisfies(
    condition: Boolean,
    message: String,
): ConstraintResult = satisfies(condition, Message.Text(content = message))