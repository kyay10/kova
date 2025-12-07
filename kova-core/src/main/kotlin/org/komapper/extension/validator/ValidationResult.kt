package org.komapper.extension.validator

import arrow.core.Nel

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

context(c: ValidationContext)
val Message.failure: FailureDetail get() = SimpleFailureDetail(c, this)

context(c: ValidationContext)
val String.failure: FailureDetail get() = Message.Text(this).failure

internal data class CompositeFailureDetail(
    override val context: ValidationContext,
    val first: Nel<FailureDetail>,
    val second: Nel<FailureDetail>,
) : FailureDetail {
    override val message: Message get() = Message.Resource("kova.or", composeMessages(first), composeMessages(second))
}

context(c: ValidationContext)
infix fun Nel<FailureDetail>.or(other: Nel<FailureDetail>): FailureDetail = CompositeFailureDetail(c, this, other)

private fun composeMessages(details: List<FailureDetail>): List<Message> = details.map {
    when (it) {
        is SimpleFailureDetail -> it.message
        is CompositeFailureDetail -> {
            val first = composeMessages(it.first)
            val second = composeMessages(it.second)
            Message.Resource("kova.or.nested", first, second)
        }
    }
}