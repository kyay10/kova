package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import org.komapper.extension.validator.Message.Resource
import java.time.Clock

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