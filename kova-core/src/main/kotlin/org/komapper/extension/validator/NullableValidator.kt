package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Validates that the input is not null.
 *
 * This constraint fails if the input is null.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().asNullable().notNull()
 * validator.validate("hello") // Success: "hello"
 * validator.validate(null)    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator that rejects null
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun <T> T.notNull(message: () -> Message = { Message.Resource("kova.nullable.notNull") }): T & Any {
    contract {
        returns() implies (this@notNull != null)
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        (this@notNull == null) holdsIn message
    }
    satisfies(this != null) { message() }
    return this
}

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun <T : Any> T?.isNull(message: MessageProvider0<T> = Message.resource0("kova.nullable.isNull")) {
    contract { returns() implies (this@isNull == null) }
    constrain(message.id) { satisfies(this == null) { message(this!!) } }
}