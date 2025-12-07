package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun constrain(id: String, check: context(ValidationContext) () -> Unit) {
    contract { callsInPlace(check, InvocationKind.EXACTLY_ONCE) }
    addLog(id) { check() }
}