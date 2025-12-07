package org.komapper.extension.validator

import arrow.core.raise.Raise
import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.collections.shouldBeSingleton


/**
 * Verifies if a block of code will raise.
 *
 * ```kotlin
 * val raised: String = shouldRaise {
 *   raise("failed")
 * }
 * ```
 */
inline fun <T> shouldRaise(block: Raise<T>.() -> Any?): T = either(block).shouldBeLeft()

/**
 * Verifies that a block of code will not raise anything.
 *
 * ```kotlin
 * val raised: String = shouldNotRaise {
 *   raise("failed") // fails
 * }
 * ```
 */
inline fun <T, R> shouldNotRaise(block: Raise<T>.() -> R) = either(block).shouldBeRight()

inline fun <R> shouldBeValid(failFast: Boolean = false, validation: Validation<R>) =
    shouldNotRaise { validate(failFast, validation) }

@IgnorableReturnValue
inline fun shouldBeInvalid(failFast: Boolean = false, block: Validation<Any?>) =
    shouldRaise { validate(failFast, block) }

@IgnorableReturnValue
inline fun shouldBeInvalidSingle(failFast: Boolean = false, block: Validation<Any?>) =
    shouldBeInvalid(failFast, block).shouldBeSingleton().single()

internal val Message.id get() = (this as? Message.Resource)?.id
internal val Message.content get() = toString()