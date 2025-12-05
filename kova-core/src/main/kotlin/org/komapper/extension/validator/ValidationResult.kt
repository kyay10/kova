package org.komapper.extension.validator

import arrow.core.EitherNel

typealias ValidationResult<T> = EitherNel<FailureDetail, Pair<T, ValidationContext>>

/**
 * Detailed information about a validation failure.
 *
 * Contains the validation context, error message, root object name, and field path
 * where the validation failed.
 *
 * Implementations include simple failures and composite failures (from OR operations).
 */
sealed interface FailureDetail {
    /** The validation context at the point of failure */
    val context: ValidationContext

    /** The error message describing the failure */
    val message: Message

    /** The root object's qualified class name (e.g., "com.example.User") */
    val root get() = context.root

    /** The field path where validation failed, excluding the root (e.g., "name" or "address.city") */
    val path get() = context.path
}

internal data class SimpleFailureDetail(
    override val context: ValidationContext,
    override val message: Message,
) : FailureDetail

internal data class CompositeFailureDetail(
    override val context: ValidationContext,
    val first: List<FailureDetail>,
    val second: List<FailureDetail>,
) : FailureDetail {
    override val message: Message get() {
        val firstMessages = composeMessages(first)
        val secondMessages = composeMessages(second)
        return Message.Resource("kova.or", firstMessages, secondMessages)
    }
}

private fun composeMessages(details: List<FailureDetail>): List<Message> =
    details.map {
        when (it) {
            is SimpleFailureDetail -> it.message
            is CompositeFailureDetail -> {
                val first = composeMessages(it.first)
                val second = composeMessages(it.second)
                Message.Resource("kova.or.nested", first, second)
            }
        }
    }