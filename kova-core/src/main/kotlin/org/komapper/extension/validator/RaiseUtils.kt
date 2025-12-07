package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.RaiseAccumulate
import arrow.core.raise.RaiseDSL
import arrow.core.raise.accumulating
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

@RaiseDSL
context(acc: Accumulate<Error>)
inline fun <Error, A> accumulating(block: context(RaiseAccumulate<Error>) () -> A): RaiseAccumulate.Value<A> {
    contract { callsInPlace(block, AT_MOST_ONCE) }
    return acc.accumulating(block)
}

@IgnorableReturnValue
@RaiseDSL
context(acc: Accumulate<Error>)
inline fun <Error> accumulatingUnit(block: context(RaiseAccumulate<Error>) () -> Unit): RaiseAccumulate.Value<Unit> {
    contract { callsInPlace(block, AT_MOST_ONCE) }
    return acc.accumulating(block)
}