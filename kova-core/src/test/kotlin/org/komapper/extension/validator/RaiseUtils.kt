package org.komapper.extension.validator

import arrow.core.raise.Raise
import arrow.core.raise.context.RaiseAccumulate
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

inline fun <R> shouldBeValid(
    config: ValidationConfig = ValidationConfig(),
    block: context(ValidationContext, RaiseAccumulate<FailureDetail>) () -> R
) = shouldNotRaise { validate(config, block) }

@IgnorableReturnValue
inline fun shouldBeInvalid(
    config: ValidationConfig = ValidationConfig(),
    block: context(ValidationContext, RaiseAccumulate<FailureDetail>) () -> Any?
) = shouldRaise { validate(config, block) }

@IgnorableReturnValue
inline fun shouldBeInvalidSingle(
    config: ValidationConfig = ValidationConfig(),
    block: context(ValidationContext, RaiseAccumulate<FailureDetail>) () -> Any?
) = shouldBeInvalid(config, block).shouldBeSingleton().single()