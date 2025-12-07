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
    /** The error message describing the failure */
    val message: Message

    /** The root object's qualified class name (e.g., "com.example.User") */
    val root: String

    /** The field path where validation failed, excluding the root (e.g., "name" or "address.city") */
    val path: Path
}

private data class SimpleFailureDetail(
    override val root: String,
    override val path: Path,
    override val message: Message
) : FailureDetail

context(c: ValidationContext)
val Message.failure: FailureDetail get() = SimpleFailureDetail(c.root, c.path, this)

context(_: ValidationContext)
val String.failure: FailureDetail get() = Message.Text(this).failure

private data class CompositeFailureDetail(
    override val root: String,
    override val path: Path,
    val first: Nel<FailureDetail>,
    val second: Nel<FailureDetail>
) : FailureDetail {
    override val message: Message
        get() = Message.Resource("kova.or", first.map { it.nestedMessage }, second.map { it.nestedMessage })
}

context(c: ValidationContext)
infix fun Nel<FailureDetail>.or(other: Nel<FailureDetail>): FailureDetail =
    CompositeFailureDetail(c.root, c.path, this, other)

private val FailureDetail.nestedMessage: Message
    get() = when (this) {
        is SimpleFailureDetail -> message
        is CompositeFailureDetail ->
            Message.Resource("kova.or.nested", first.map { it.nestedMessage }, second.map { it.nestedMessage })
    }