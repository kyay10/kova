package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.RaiseAccumulate.Value
import arrow.core.raise.context.RaiseAccumulate
import kotlin.contracts.InvocationKind
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.reflect.KProperty0

context(_: ValidationContext)
inline fun <reified T> T.checking(block: context(ValidationContext) () -> Unit) {
    contract { callsInPlace(block, EXACTLY_ONCE) }
    val rootName = T::class.qualifiedName ?: T::class.simpleName ?: T::class.toString()
    this.addRoot(rootName, block)
}

context(_: ValidationContext)
inline fun <reified R> constructing(block: context(ValidationContext) () -> R): R {
    contract { callsInPlace(block, EXACTLY_ONCE) }
    val rootName = R::class.qualifiedName ?: R::class.simpleName ?: R::class.toString()
    return null.addRoot(rootName, block)
}

context(_: ValidationContext, _: Accumulate<FailureDetail>)
inline fun <T, R> T.parameter(name: String, block: context(ValidationContext, RaiseAccumulate<FailureDetail>) (T) -> R): Value<R> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return accumulating { this.addPath(name) { block(this) } }
}

@IgnorableReturnValue
context(_: ValidationContext, _: Accumulate<FailureDetail>)
inline fun <T> T.property(name: String, block: Constraint<T>): Value<Boolean> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return accumulating { this.addPathChecked(name) { block(this) } != null }
}

@IgnorableReturnValue
context(_: ValidationContext, _: Accumulate<FailureDetail>)
inline operator fun <T> KProperty0<T>.invoke(block: Constraint<T>): Value<Boolean> {
    return get().property(name, block)
}