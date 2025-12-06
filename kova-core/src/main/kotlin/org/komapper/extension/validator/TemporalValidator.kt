package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import java.time.Clock
import java.time.temporal.Temporal

/**
 * Creates a new temporal validator.
 *
 * @param T The temporal type being validated
 * @param name Debug name for the validator
 * @param validator Validator to wrap
 * @param clock Clock used for temporal comparisons (past, future, etc.)
 * @param temporalNow Strategy for obtaining the current temporal value
 */
fun <T, S> TemporalValidator(
    name: String = "empty",
    clock: Clock = Clock.systemDefaultZone(),
    temporalNow: TemporalNow<T>,
    validator: Validator<T, S>,
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> = object : TemporalValidator<T, S> {
    override val clock: Clock = clock
    override val temporalNow: TemporalNow<T> = temporalNow

    override fun invoke(c: ValidationContext, r: RaiseAccumulate<FailureDetail>, input: T) = validator(c, r, input)
}

/**
 * Validator for temporal values (LocalDate, LocalTime, LocalDateTime) with comparison constraints.
 *
 * Supports validation for all temporal types that implement [Temporal] and [Comparable].
 *
 * Example:
 * ```kotlin
 * val dateValidator = Kova.temporal<LocalDate>().past().min(LocalDate.of(2020, 1, 1))
 * val timeValidator = Kova.temporal<LocalTime>().future()
 * val dateTimeValidator = Kova.temporal<LocalDateTime>().futureOrPresent()
 * ```
 *
 * @param T The temporal type being validated
 */
interface TemporalValidator<T, S> : (ValidationContext, RaiseAccumulate<FailureDetail>, T) -> S
    where T : Temporal, T : Comparable<T> {
    val clock: Clock
    val temporalNow: TemporalNow<T>
}

fun <T, S> TemporalValidator<T, S>.constrain(
    id: String,
    check: Constraint<T>,
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    TemporalValidator(
        name = id,
        validator = (this as Validator<T, S>).constrain(id, check),
        clock = clock,
        temporalNow = temporalNow
    )

/**
 * Validates that the temporal value is greater than or equal to [value] (inclusive).
 *
 * @param value Minimum allowed value
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.min(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.temporal.min"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id, Constraints.min(value, message))

/**
 * Validates that the temporal value is less than or equal to [value] (inclusive).
 *
 * @param value Maximum allowed value
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.max(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.temporal.max"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id, Constraints.max(value, message))

/**
 * Validates that the temporal value is strictly greater than [value] (exclusive).
 *
 * @param value The value that the input must be greater than
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.gt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.temporal.gt"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> = constrain(message.id, Constraints.gt(value, message))

/**
 * Validates that the temporal value is greater than or equal to [value] (inclusive).
 *
 * Alias for [min].
 *
 * @param value The minimum value (inclusive)
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.gte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.temporal.gte"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id, Constraints.gte(value, message))

/**
 * Validates that the temporal value is strictly less than [value] (exclusive).
 *
 * @param value The value that the input must be less than
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.lt(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.temporal.lt"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> = constrain(message.id, Constraints.lt(value, message))

/**
 * Validates that the temporal value is less than or equal to [value] (inclusive).
 *
 * Alias for [max].
 *
 * @param value The maximum value (inclusive)
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.lte(
    value: T,
    message: MessageProvider1<T, T> = Message.resource1("kova.temporal.lte"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id, Constraints.lte(value, message))

/**
 * Validates that the temporal value is in the future (strictly greater than now).
 *
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.future(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.future"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it > temporalNow.now(clock)) { message(it) }
    }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.futureOrPresent(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.futureOrPresent"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it >= temporalNow.now(clock)) { message(it) }
    }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.past(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.past"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it < temporalNow.now(clock)) { message(it) }
    }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * @param message Custom error message provider
 */
fun <T, S> TemporalValidator<T, S>.pastOrPresent(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.pastOrPresent"),
): TemporalValidator<T, S> where T : Temporal, T : Comparable<T> =
    constrain(message.id) {
        satisfies(it <= temporalNow.now(clock)) { message(it) }
    }

/**
 * Combines this validator with another validator using logical AND.
 *
 * Alias for [and].
 *
 * @param other The validator to combine with
 */
operator fun <T, S> TemporalValidator<T, Unit>.plus(other: Validator<T, S>): TemporalValidator<T, S>
    where T : Temporal, T : Comparable<T> = and(other)

/**
 * Combines this validator with another validator using logical AND.
 *
 * Both validators must pass for the combined validator to pass.
 *
 * @param other The validator to combine with
 */
infix fun <T, S> TemporalValidator<T, Unit>.and(other: Validator<T, S>): TemporalValidator<T, S>
    where T : Temporal, T : Comparable<T> = TemporalValidator(
    name = "and",
    validator = (this as Constraint<T>).and(other),
    clock = clock,
    temporalNow = temporalNow,
)

/**
 * Combines this validator with another validator using logical OR.
 *
 * Either validator can pass for the combined validator to pass.
 *
 * @param other The validator to combine with
 */
infix fun <T, S> TemporalValidator<T, S>.or(other: Validator<T, S>): TemporalValidator<T, S>
    where T : Temporal, T : Comparable<T> = TemporalValidator(
    name = "or",
    validator = (this as Validator<T, S>).or(other),
    clock = clock,
    temporalNow = temporalNow,
)