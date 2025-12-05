package org.komapper.extension.validator

import arrow.core.raise.Raise
import arrow.core.raise.either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight


/**
 * Verifies if a block of code will raise.
 *
 * ```kotlin
 * val raised: String = shouldRaise {
 *   raise("failed")
 * }
 * ```
 */
inline fun <T> shouldRaise(block: Raise<T>.() -> Unit): T = either(block).shouldBeLeft()

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