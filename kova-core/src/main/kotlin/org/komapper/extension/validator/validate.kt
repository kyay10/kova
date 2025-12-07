package org.komapper.extension.validator

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.handleErrorWith
import arrow.core.nel
import arrow.core.raise.Accumulate
import arrow.core.raise.RaiseAccumulate.Value
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.bind
import arrow.core.raise.either
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Core validator interface for type-safe validation.
 *
 * A validator does some validation, producing an output of type [R],
 * or raises a validation failure with detailed error information.
 *
 * Validators are immutable and composable using normal function composition
 *
 * @param R The output type after successful validation
 */
typealias Validation<R> = context(ValidationContext, RaiseAccumulate<FailureDetail>) () -> R

/**
 * Validates the input and returns the validated value, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * val validation = Kova.string().min(1).max(10)
 * try {
 *     val validated = validation.validate("hello")
 *     println("Valid: $validated")
 * } catch (e: ValidationException) {
 *     println("Errors: ${e.details}")
 * }
 * ```
 *
 * @param failFast Whether to stop at the first failure or accumulate all failures
 * @return The validated value of type [R]
 */
context(_: Raise<Nel<FailureDetail>>)
inline fun <R> validate(failFast: Boolean = false, validation: Validation<R>): R {
    contract { callsInPlace(validation, InvocationKind.EXACTLY_ONCE) }
    return accumulateUnless(failFast) { context(ValidationContext(failFast = failFast)) { validation() } }
}

context(c: ValidationContext)
inline fun <R> or(validation: context(RaiseAccumulate<FailureDetail>) () -> R): EitherNel<FailureDetail, R> {
    contract { callsInPlace(validation, InvocationKind.AT_MOST_ONCE) }
    return either { accumulateUnless(c.failFast) { validation() } }
}

context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <T : Any> T?.isNullOr(constraint: context(RaiseAccumulate<FailureDetail>) (T) -> Unit) {
    contract {
        callsInPlace(constraint, InvocationKind.AT_MOST_ONCE)
        (this@isNullOr != null) holdsIn constraint
    }
    return or { isNull() } or { constraint(this!!) } or Fail
}

@IgnorableReturnValue
context(_: ValidationContext, _: Accumulate<FailureDetail>)
inline fun <T : Any> T?.isNullOrAccumulate(constraint: context(RaiseAccumulate<FailureDetail>) (T) -> Unit): Value<Unit> {
    contract {
        callsInPlace(constraint, InvocationKind.AT_MOST_ONCE)
        (this@isNullOrAccumulate != null) holdsIn constraint
    }
    return or { isNull() } or { constraint(this!!) } or Accum
}

@JvmName("orNel")
context(_: ValidationContext)
inline infix fun <R> EitherNel<FailureDetail, R>.or(
    validation: context(RaiseAccumulate<FailureDetail>) () -> R
): Either<FailureDetail, R> {
    contract { callsInPlace(validation, InvocationKind.AT_MOST_ONCE) }
    return handleErrorWith { details -> org.komapper.extension.validator.or(validation).mapLeft { details or it } }
}

context(_: ValidationContext)
inline infix fun <R> Either<FailureDetail, R>.or(
    validation: context(RaiseAccumulate<FailureDetail>) () -> R
): Either<FailureDetail, R> {
    contract { callsInPlace(validation, InvocationKind.AT_MOST_ONCE) }
    return mapLeft { it.nel() } or validation
}

object Fail
object Accum

context(_: Raise<FailureDetail>)
infix fun <R> Either<FailureDetail, R>.or(fail: Fail): R = bind()

context(_: Accumulate<FailureDetail>)
infix fun <R> Either<FailureDetail, R>.or(accumulate: Accum): Value<R> = accumulating { bind() }

@IgnorableReturnValue
@JvmName("orUnit")
context(_: Accumulate<FailureDetail>)
infix fun Either<FailureDetail, Unit>.or(accumulate: Accum): Value<Unit> = accumulating { bind() }