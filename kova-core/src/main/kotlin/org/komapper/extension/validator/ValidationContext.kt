package org.komapper.extension.validator

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
 * @property failFast Whether validation should stop at the first failure
 */
data class ValidationContext(
    val root: String = "",
    val path: Path = Path(name = "", obj = null, parent = null),
    val failFast: Boolean = false,
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
inline fun <R> Any?.addPathChecked(name: String, block: context(ValidationContext) () -> R): R? {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    if (this != null && c.path.containsObject(this)) return null
    return this.addPath(name, block)
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
data class Path(val name: String, val obj: Any?, val parent: Path?) {
    /**
     * The full dotted path from root to this point, excluding the root class name.
     *
     * Examples: "address.city", "name" (not "User.address.city" or "Person.name")
     */
    val fullName: String get() = buildString { appendFullNameReversed() }.reversed()

    /**
     * Checks if the given object appears anywhere in the path ancestry.
     *
     * Uses object identity (===) to detect circular references. This prevents
     * infinite loops when validating objects with circular references.
     *
     * @param target The object to search for in the path
     * @return true if the object is found in the path ancestry
     */
    fun containsObject(target: Any): Boolean = containsObjectImpl(target)
}

context(a: Appendable)
private tailrec fun Path.appendFullNameReversed() {
    a.append(name.reversed())
    if (parent != null && parent.name.isNotEmpty()) {
        if (name != "") a.append('.')
        parent.appendFullNameReversed()
    }
}

private tailrec fun Path.containsObjectImpl(target: Any): Boolean {
    if (obj === target) return true
    return (parent ?: return false).containsObjectImpl(target)
}
