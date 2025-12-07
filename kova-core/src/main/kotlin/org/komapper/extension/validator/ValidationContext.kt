package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import arrow.core.raise.context.ensure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Context object that tracks the state of validation execution.
 *
 * Contains information about the validation root, current path, logs, and configuration.
 * The context is immutable and threaded through the validation process, with each
 * validator potentially creating a new context with updated state.
 *
 * @property root The root object's qualified name (e.g., "com.example.User")
 * @property path The current validation path, tracking nested objects and circular references
 * @property logs Debug logs of validator operations (only populated when logging is enabled)
 * @property config Validation configuration settings
 */
data class ValidationContext(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val logs: List<String> = emptyList(),
    val config: ValidationConfig = ValidationConfig(),
)

/** Whether validation should stop at the first failure. */
context(c: ValidationContext)
val failFast: Boolean get() = c.config.failFast

/** Whether debug logging of validator operations is enabled. */
context(c: ValidationContext)
val logging: Boolean get() = c.config.logging

/**
 * Configuration settings for validation execution.
 *
 * @property failFast If true, validation stops at the first failure instead of collecting all errors.
 *                    Default is false (collect all errors).
 * @property logging If true, enables debug logging of validator operations. Default is false.
 *
 * Example:
 * ```kotlin
 * val config = ValidationConfig(failFast = true)
 * val result = validator.tryValidate(input, config)
 * ```
 */
data class ValidationConfig(
    val failFast: Boolean = false,
    val logging: Boolean = false,
)

context(c: ValidationContext)
inline fun <R> Any?.addRoot(name: String, block: context(ValidationContext) () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    // initialize root
    return block(if (c.root.isEmpty()) c.copy(root = name, path = Path(name = "", obj = this, parent = null)) else c)
}

context(c: ValidationContext)
inline fun <R> Any?.addPath(name: String, block: context(ValidationContext) () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(c.copy(path = c.path.copy(name = name, obj = this, parent = c.path)))
}

context(c: ValidationContext)
inline fun <R> Any?.bindObject(block: context(ValidationContext) () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(c.copy(path = c.path.copy(obj = this)))
}

context(c: ValidationContext)
inline fun <R> Any?.addPathChecked(name: String, block: context(ValidationContext) () -> R): R? {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (this != null && c.path.containsObject(this)) return null
    return this.addPath(name, block)
}

context(c: ValidationContext)
inline fun <R> addLog(log: String, block: context(ValidationContext) () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(if (logging) c.copy(logs = c.logs + log) else c)
}

context(c: ValidationContext)
inline fun <R> appendPath(text: String, block: context(ValidationContext) () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(c.copy(path = c.path.copy(name = c.path.name + text)))
}

/**
 * Represents a path through the object graph during validation.
 *
 * This class forms a linked list structure that tracks the validation path and
 * object references for circular reference detection.
 *
 * @property name The name of the current path segment (e.g., "address", "city")
 * @property obj The object at this path point (used for circular reference detection)
 * @property parent The parent path segment, or null if this is the root
 */
data class Path(
    val name: String,
    val obj: Any?,
    val parent: Path?,
) {
    /**
     * The full dotted path from root to this point, excluding the root class name.
     *
     * Examples: "address.city", "name" (not "User.address.city" or "Person.name")
     */
    val fullName: String
        get() {
            if (parent == null || parent.name.isEmpty()) return name
            return if (name.isEmpty()) parent.fullName else "${parent.fullName}.$name"
        }

    /**
     * Checks if the given object appears anywhere in the path ancestry.
     *
     * Uses object identity (===) to detect circular references. This prevents
     * infinite loops when validating objects with circular references.
     *
     * @param target The object to search for in the path
     * @return true if the object is found in the path ancestry
     */
    fun containsObject(target: Any): Boolean {
        if (obj === target) return true
        return parent?.containsObject(target) ?: false
    }
}
