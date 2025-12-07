package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.context.Raise
import arrow.core.toNonEmptyListOrNull
import org.komapper.extension.validator.Message.Resource

/**
 * Validates that the collection size is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().min(2)
 * validator.validate(listOf("a", "b", "c")) // Success
 * validator.validate(listOf("a"))           // Failure
 * ```
 *
 * @param size Minimum collection size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum size constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <C : Collection<*>> C.min(size: Int, message: (C, Int, Int) -> Message) =
    satisfies(this.size >= size) { message(this, this.size, size) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <C : Collection<*>> C.min(size: Int) = min(size, Message.resource2("kova.collection.min"))

/**
 * Validates that the collection size does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().max(3)
 * validator.validate(listOf("a", "b"))       // Success
 * validator.validate(listOf("a", "b", "c", "d")) // Failure
 * ```
 *
 * @param size Maximum collection size (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum size constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <C : Collection<*>> C.max(size: Int, message: (C, Int, Int) -> Message) =
    satisfies(this.size <= size) { message(this, this.size, size) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <C : Collection<*>> C.max(size: Int) = max(size, Message.resource2("kova.collection.max"))

/**
 * Validates that the collection is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().notEmpty()
 * validator.validate(listOf("a")) // Success
 * validator.validate(listOf())    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <C : Collection<*>> C.notEmpty(message: (C) -> Message = { Resource("kova.collection.notEmpty", it) }) =
    toNonEmptyListOrNull().notNull { message(this) }

/**
 * Validates that the collection size equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>().length(3)
 * validator.validate(listOf("a", "b", "c")) // Success
 * validator.validate(listOf("a", "b"))      // Failure
 * ```
 *
 * @param size Exact collection size required
 * @param message Custom error message provider
 * @return A new validator with the exact size constraint
 */
context(_: ValidationContext, _: Raise<FailureDetail>)
inline fun <C : Collection<*>> C.length(size: Int, message: (C, Int) -> Message) =
    satisfies(this.size == size) { message(this, size) }

context(_: ValidationContext, _: Raise<FailureDetail>)
infix fun <C : Collection<*>> C.length(size: Int) = length(size, Message.resource1("kova.collection.length"))

/**
 * Validates each element of the collection using the specified validator.
 *
 * If any element fails validation, the entire collection validation fails.
 * Error paths include element indices for better error reporting.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.collection<String>()
 *     .notEmpty()
 *     .onEach(Kova.string().min(2).max(10))
 *
 * validator.validate(listOf("abc", "def"))    // Success
 * validator.validate(listOf("a", "b"))        // Failure: elements too short
 * ```
 *
 * @param constraint The validator to apply to each element
 * @return A new validator with per-element validation
 */
context(_: ValidationContext, _: Accumulate<FailureDetail>)
inline infix fun <E, C : Collection<E>> C.onEach(constraint: Constraint<E>) =
    forEachIndexed { i, e -> appendPath("[$i]<collection element>") { accumulatingUnit { constraint(e) } } }
