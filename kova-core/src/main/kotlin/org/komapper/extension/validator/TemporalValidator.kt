package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import java.time.Clock
import java.time.temporal.Temporal

/**
 * Validates that the temporal value is in the future (strictly greater than now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T> T.future(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.future")
): Unit where T : Temporal, T : Comparable<T> = constrain(message.id) { satisfies(this > now()) { message(this) } }

/**
 * Validates that the temporal value is in the future or present (greater than or equal to now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T> T.futureOrPresent(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.futureOrPresent")
): Unit where T : Temporal, T : Comparable<T> = constrain(message.id) { satisfies(this >= now()) { message(this) } }

/**
 * Validates that the temporal value is in the past (strictly less than now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T> T.past(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.past"),
): Unit where T : Temporal, T : Comparable<T> = constrain(message.id) { satisfies(this < now()) { message(this) } }

/**
 * Validates that the temporal value is in the past or present (less than or equal to now).
 *
 * @param message Custom error message provider
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>, _: TemporalNow<T>, _: Clock)
fun <T> T.pastOrPresent(
    message: MessageProvider0<T> = Message.resource0("kova.temporal.pastOrPresent"),
): Unit where T : Temporal, T : Comparable<T> = constrain(message.id) { satisfies(this <= now()) { message(this) } }