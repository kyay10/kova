package org.komapper.extension.validator

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.Temporal

/**
 * Strategy interface for obtaining the current temporal value from a [Clock].
 *
 * This abstraction allows validators to work with different temporal types
 * (LocalDate, LocalTime, LocalDateTime) which have different `now()` methods.
 *
 * @param T The temporal type
 */
interface TemporalNow<T : Temporal> {
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
fun <T : Temporal> now(): T = temporalNow.now()

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
inline fun <T : Temporal, R> temporal(
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