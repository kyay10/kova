package org.komapper.extension.validator

import arrow.core.NonEmptyList
import arrow.core.raise.Accumulate
import arrow.core.raise.RaiseAccumulate.Value
import arrow.core.raise.RaiseDSL
import arrow.core.raise.accumulate
import arrow.core.raise.accumulating
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@RaiseDSL
context(acc: Accumulate<Error>)
inline fun <Error, A> accumulating(block: context(RaiseAccumulate<Error>) () -> A): Value<A> {
    contract { callsInPlace(block, AT_MOST_ONCE) }
    return acc.accumulating(block)
}

@IgnorableReturnValue
@RaiseDSL
context(_: Accumulate<Error>)
inline fun <Error> accumulatingUnit(block: context(RaiseAccumulate<Error>) () -> Unit): Value<Unit> {
    contract { callsInPlace(block, AT_MOST_ONCE) }
    return accumulating(block)
}

context(raise: Raise<NonEmptyList<Error>>)
@RaiseDSL
inline fun <Error, A> accumulateUnless(failFast: Boolean, block: context(RaiseAccumulate<Error>) () -> A): A {
    contract { callsInPlace(block, EXACTLY_ONCE) }
    return raise.accumulate { block(if (failFast) RaiseAccumulate(FailFastAccumulate(this), ::raise) else this) }
}

@PublishedApi
internal class FailFastAccumulate<Error>(private val accumulate: Accumulate<Error>) : Accumulate<Error> {
    override val latestError get() = null
    override fun accumulateAll(errors: NonEmptyList<Error>) = accumulate.accumulateAll(errors).value
}