package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import org.komapper.extension.validator.Message.Resource
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Strategy interface for obtaining the current temporal value from a [Clock].
 *
 * This abstraction allows validators to work with different temporal types
 * (LocalDate, LocalTime, LocalDateTime) which have different `now()` methods.
 *
 * @param T The temporal type
 */
interface TemporalNow<T> {
    /**
     * Gets the current temporal value using the provided clock.
     *
     * @param clock The clock to use for determining "now"
     * @return The current temporal value
     */
    context(clock: Clock)
    fun now(): T
}

context(temporalNow: TemporalNow<T>, _: Clock)
fun <T> now(): T = temporalNow.now()

/**
 * [TemporalNow] implementation for [LocalDate].
 *
 * Provides the current date using [LocalDate.now].
 */
object LocalDateNow : TemporalNow<LocalDate> {
    context(clock: Clock)
    override fun now(): LocalDate = LocalDate.now(clock)
}

/**
 * [TemporalNow] implementation for [LocalTime].
 *
 * Provides the current time using [LocalTime.now].
 */
object LocalTimeNow : TemporalNow<LocalTime> {
    context(clock: Clock)
    override fun now(): LocalTime = LocalTime.now(clock)
}

/**
 * [TemporalNow] implementation for [LocalDateTime].
 *
 * Provides the current date-time using [LocalDateTime.now].
 */
object LocalDateTimeNow : TemporalNow<LocalDateTime> {
    context(clock: Clock)
    override fun now(): LocalDateTime = LocalDateTime.now(clock)
}

/**
 * Creates a validator for temporal values with temporal constraints.
 *
 * This generic method supports LocalDate, LocalTime, LocalDateTime, and any other
 * type that implements both Temporal and Comparable.
 *
 * @param T The temporal type to validate
 * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
 * @param temporalNow Strategy for obtaining the current temporal value
 * @return A temporal validator for type T
 *
 * Example:
 * ```kotlin
 * val dateValidator = Kova.temporal<LocalDate>(LocalDateNow)
 * val timeValidator = Kova.temporal<LocalTime>(LocalTimeNow)
 * ```
 */
inline fun <T, R> temporal(
    temporalNow: TemporalNow<T>,
    clock: Clock = Clock.systemDefaultZone(),
    block: context(TemporalNow<T>, Clock) () -> R
): R = block(temporalNow, clock)

/**
 * Creates a validator for LocalDate values with temporal constraints.
 *
 * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
 */
inline fun <R> localDate(
    clock: Clock = Clock.systemDefaultZone(),
    block: context(TemporalNow<LocalDate>, Clock) () -> R
): R = temporal(LocalDateNow, clock, block)

/**
 * Creates a validator for LocalTime values with temporal constraints.
 *
 * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
 */
inline fun <R> localTime(
    clock: Clock = Clock.systemDefaultZone(),
    block: context(TemporalNow<LocalTime>, Clock) () -> R
): R = temporal(LocalTimeNow, clock, block)

/**
 * Creates a validator for LocalDateTime values with temporal constraints.
 *
 * @param clock The clock used for temporal comparisons like past() and future(). Defaults to system default zone.
 */
inline fun <R> localDateTime(
    clock: Clock = Clock.systemDefaultZone(),
    block: context(TemporalNow<LocalDateTime>, Clock) () -> R
): R = temporal(LocalDateTimeNow, clock, block)

/**
 * Validates that the temporal value is in the future (strictly greater than now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
inline fun <T : Comparable<T>> T.future(message: (T) -> Message = { Resource("kova.temporal.future", it) }) =
    satisfies(this > now()) { message(this) }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
inline fun <T : Comparable<T>> T.futureOrPresent(
    message: (T) -> Message = { Resource("kova.temporal.futureOrPresent", it) }
) = satisfies(this >= now()) { message(this) }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
inline fun <T : Comparable<T>> T.past(message: (T) -> Message = { Resource("kova.temporal.past", it) }) =
    satisfies(this < now()) { message(this) }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
inline fun <T : Comparable<T>> T.pastOrPresent(
    message: (T) -> Message = { Resource("kova.temporal.pastOrPresent", it) }
) = satisfies(this <= now()) { message(this) }