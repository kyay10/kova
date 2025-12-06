package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun constrain(id: String, check: context(ValidationContext) () -> Unit) = addLog(id) { check() }