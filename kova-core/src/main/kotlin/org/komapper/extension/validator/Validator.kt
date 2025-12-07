package org.komapper.extension.validator

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.handleErrorWith
import arrow.core.nel
import arrow.core.raise.RaiseAccumulate.Value
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.bind
import arrow.core.raise.context.bindOrAccumulate
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
typealias Validator<R> = context(ValidationContext, RaiseAccumulate<FailureDetail>) () -> R

/**
 * Validates the input
 *
 * This is the recommended way to perform validation when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * when (val result = validator.tryValidate("hello")) {
 *     is Either.Right -> println("Valid: ${result.value.first}")
 *     is Either.Left -> println("Errors: ${result.value}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return the validated value
 */
inline fun <R> tryValidate(
    config: ValidationConfig = ValidationConfig(),
    validator: Validator<R>
): EitherNel<FailureDetail, R> {
    contract { callsInPlace(validator, InvocationKind.AT_MOST_ONCE) }
    return either { validate(config, validator) }
}

/**
 * Validates the input and returns the validated value, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * try {
 *     val validated = validator.validate("hello")
 *     println("Valid: $validated")
 * } catch (e: ValidationException) {
 *     println("Errors: ${e.details}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return The validated value of type [R]
 */
context(_: Raise<Nel<FailureDetail>>)
inline fun <R> validate(config: ValidationConfig = ValidationConfig(), validator: Validator<R>): R {
    contract { callsInPlace(validator, InvocationKind.EXACTLY_ONCE) }
    context(ValidationContext(config = config)) {
        or {
            val result = validator()
            contextOf<RaiseAccumulate<FailureDetail>>().latestError?.value // raise if any accumulated errors
            return result
        }.bind()
    }
}

context(_: ValidationContext)
inline fun <R> or(validator: Validator<R>): EitherNel<FailureDetail, R> {
    contract { callsInPlace(validator, InvocationKind.AT_MOST_ONCE) }
    return either { accumulateUnless(failFast) { validator() } }
}

context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <T : Any> T?.isNullOr(validator: context(ValidationContext, RaiseAccumulate<FailureDetail>) (T) -> Unit) {
    contract {
        callsInPlace(validator, InvocationKind.AT_MOST_ONCE)
        (this@isNullOr != null) holdsIn validator
    }
    return or { isNull() } or { validator(this!!) } or Fail
}

@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun <T : Any> T?.isNullOrAccumulate(validator: context(ValidationContext, RaiseAccumulate<FailureDetail>) (T) -> Unit): Value<Unit> {
    contract {
        callsInPlace(validator, InvocationKind.AT_MOST_ONCE)
        (this@isNullOrAccumulate != null) holdsIn validator
    }
    return or { isNull() } or { validator(this!!) } or Accumulate
}

@JvmName("orNel")
context(_: ValidationContext)
inline infix fun <R> EitherNel<FailureDetail, R>.or(validator: Validator<R>): Either<FailureDetail, R> {
    contract { callsInPlace(validator, InvocationKind.AT_MOST_ONCE) }
    return handleErrorWith { details -> org.komapper.extension.validator.or(validator).mapLeft { details or it } }
}

context(_: ValidationContext)
inline infix fun <R> Either<FailureDetail, R>.or(validator: Validator<R>): Either<FailureDetail, R> {
    contract { callsInPlace(validator, InvocationKind.AT_MOST_ONCE) }
    return mapLeft { it.nel() } or validator
}

object Fail
object Accumulate

context(_: Raise<FailureDetail>)
infix fun <R> Either<FailureDetail, R>.or(fail: Fail): R = bind()

context(_: RaiseAccumulate<FailureDetail>)
infix fun <R> Either<FailureDetail, R>.or(accumulate: Accumulate): Value<R> = bindOrAccumulate()

@IgnorableReturnValue
@JvmName("orUnit")
context(_: RaiseAccumulate<FailureDetail>)
infix fun Either<FailureDetail, Unit>.or(accumulate: Accumulate): Value<Unit> = bindOrAccumulate()

/**
 * Adds a name to the validation path for better error reporting.
 *name
 * This is useful for identifying which field failed validation in complex objects.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).name("username")
 * // Failures will show path like "User.username"
 * ```
 *
 * @param name The name to add to the validation path
 * @return A new validator that tracks the path
 */
context(_: ValidationContext)
inline fun <T, R> T.name(name: String, block: context(ValidationContext) (T) -> R): R =
    this.addPath(name) { addLog("Validator.name(name=$name)") { block(this) } }