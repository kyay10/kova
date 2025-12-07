package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import java.time.Clock

/**
 * Validates that the temporal value is in the future (strictly greater than now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T : Comparable<T>> T.future(message: MessageProvider0<T> = Message.resource0("kova.temporal.future")) =
    satisfies(this > now()) { message(this) }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T : Comparable<T>> T.futureOrPresent(message: MessageProvider0<T> = Message.resource0("kova.temporal.futureOrPresent")) =
    satisfies(this >= now()) { message(this) }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T : Comparable<T>> T.past(message: MessageProvider0<T> = Message.resource0("kova.temporal.past")) =
    satisfies(this < now()) { message(this) }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: Raise<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T : Comparable<T>> T.pastOrPresent(message: MessageProvider0<T> = Message.resource0("kova.temporal.pastOrPresent")) =
    satisfies(this <= now()) { message(this) }