package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.raise
import kotlin.reflect.KClass

/**
 * Validates that the string length is at least the specified minimum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3)
 * validator.validate("hello") // Success
 * validator.validate("hi")    // Failure
 * ```
 *
 * @param length Minimum string length (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the minimum length constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.min(length: Int, message: MessageProvider1<String, Int>) =
    constrain(message.id) { satisfies(this.length >= length) { message(this, length) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.min(length: Int) = min(length, Message.resource1("kova.string.min"))

/**
 * Validates that the string length does not exceed the specified maximum.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().max(10)
 * validator.validate("hello")      // Success
 * validator.validate("very long string") // Failure
 * ```
 *
 * @param length Maximum string length (inclusive)
 * @param message Custom error message provider
 * @return A new validator with the maximum length constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.max(length: Int, message: MessageProvider1<String, Int>) =
    constrain(message.id) { satisfies(this.length <= length) { message(this, length) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.max(length: Int) = max(length, Message.resource1("kova.string.max"))

/**
 * Validates that the string is not blank (not empty and not only whitespace).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notBlank()
 * validator.validate("hello") // Success
 * validator.validate("   ")   // Failure
 * validator.validate("")      // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-blank constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.notBlank(message: MessageProvider0<String> = Message.resource0("kova.string.notBlank")) =
    constrain(message.id) { satisfies(isNotBlank()) { message(this) } }

/**
 * Validates that the string is not empty.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().notEmpty()
 * validator.validate("hello") // Success
 * validator.validate("   ")   // Success (contains whitespace)
 * validator.validate("")      // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the not-empty constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.notEmpty(message: MessageProvider0<String> = Message.resource0("kova.string.notEmpty")) =
    constrain(message.id) { satisfies(isNotEmpty()) { message(this) } }

/**
 * Validates that the string length equals exactly the specified value.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().length(5)
 * validator.validate("hello") // Success
 * validator.validate("hi")    // Failure
 * ```
 *
 * @param length Exact string length required
 * @param message Custom error message provider
 * @return A new validator with the exact length constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.length(length: Int, message: MessageProvider1<String, Int>) =
    constrain(message.id) { satisfies(this.length == length) { message(this, length) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.length(length: Int) = length(length, Message.resource1("kova.string.length"))

/**
 * Validates that the string starts with the specified prefix.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().startsWith("Hello")
 * validator.validate("Hello World") // Success
 * validator.validate("Goodbye")     // Failure
 * ```
 *
 * @param prefix The required prefix
 * @param message Custom error message provider
 * @return A new validator with the starts-with constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.startsWith(prefix: CharSequence, message: MessageProvider1<String, CharSequence>) =
    constrain(message.id) { satisfies(startsWith(prefix, ignoreCase = false)) { message(this, prefix) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.startsWith(prefix: CharSequence) = startsWith(prefix, Message.resource1("kova.string.startsWith"))

/**
 * Validates that the string ends with the specified suffix.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().endsWith(".txt")
 * validator.validate("document.txt") // Success
 * validator.validate("document.pdf") // Failure
 * ```
 *
 * @param suffix The required suffix
 * @param message Custom error message provider
 * @return A new validator with the ends-with constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.endsWith(suffix: CharSequence, message: MessageProvider1<String, CharSequence>) =
    constrain(message.id) { satisfies(endsWith(suffix, ignoreCase = false)) { message(this, suffix) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.endsWith(suffix: CharSequence) = endsWith(suffix, Message.resource1("kova.string.endsWith"))

/**
 * Validates that the string contains the specified substring.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().contains("world")
 * validator.validate("hello world") // Success
 * validator.validate("hello")       // Failure
 * ```
 *
 * @param infix The required substring
 * @param message Custom error message provider
 * @return A new validator with the contains constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.contains(infix: CharSequence, message: MessageProvider1<String, CharSequence>) =
    constrain(message.id) { satisfies(infix in this) { message(this, infix) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.contains(infix: CharSequence) = contains(infix, Message.resource1("kova.string.contains"))

/**
 * Validates that the string matches the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().matches(Regex("\\d{3}-\\d{4}"))
 * validator.validate("123-4567") // Success
 * validator.validate("12-34")    // Failure
 * ```
 *
 * @param pattern The regex pattern to match
 * @param message Custom error message provider
 * @return A new validator with the regex constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.matches(pattern: Regex, message: MessageProvider1<String, Regex>) =
    constrain(message.id) { satisfies(pattern matches this) { message(this, pattern) } }

context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
infix fun String.matches(pattern: Regex) = matches(pattern, Message.resource1("kova.string.matches"))

private val emailPattern = Regex(
    "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}\$",
    RegexOption.IGNORE_CASE
)

/**
 * Validates that the string is a valid email address.
 *
 * Uses a comprehensive email validation pattern that checks for common email format requirements.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().email()
 * validator.validate("user@example.com") // Success
 * validator.validate("invalid-email")    // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the email constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.email(message: MessageProvider0<String> = Message.resource0("kova.string.email")) = constrain(message.id) {
    satisfies(emailPattern matches this) { message(this) }
}

/**
 * Validates that the string can be parsed as an Int.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isInt()
 * validator.validate("123")  // Success
 * validator.validate("12.5") // Failure
 * validator.validate("abc")  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-int constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isInt(message: MessageProvider0<String> = Message.resource0("kova.string.isInt")) =
    toIntOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a Long.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isLong()
 * validator.validate("123456789") // Success
 * validator.validate("abc")       // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-long constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isLong(message: MessageProvider0<String> = Message.resource0("kova.string.isLong")) =
    toLongOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a Short.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isShort()
 * validator.validate("123") // Success
 * validator.validate("abc") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-short constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isShort(message: MessageProvider0<String> = Message.resource0("kova.string.isShort")) =
    toShortOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a Byte.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isByte()
 * validator.validate("12")  // Success
 * validator.validate("abc") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-byte constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isByte(message: MessageProvider0<String> = Message.resource0("kova.string.isByte")) =
    toByteOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a Double.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isDouble()
 * validator.validate("12.5") // Success
 * validator.validate("abc")  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-double constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isDouble(message: MessageProvider0<String> = Message.resource0("kova.string.isDouble")) =
    toDoubleOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a Float.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isFloat()
 * validator.validate("12.5") // Success
 * validator.validate("abc")  // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-float constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isFloat(message: MessageProvider0<String> = Message.resource0("kova.string.isFloat")) =
    toFloatOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a BigDecimal.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isBigDecimal()
 * validator.validate("123.456789") // Success
 * validator.validate("abc")        // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-big-decimal constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isBigDecimal(message: MessageProvider0<String> = Message.resource0("kova.string.isBigDecimal")) =
    toBigDecimalOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a BigInteger.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isBigInteger()
 * validator.validate("123456789012345") // Success
 * validator.validate("abc")             // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-big-integer constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isBigInteger(message: MessageProvider0<String> = Message.resource0("kova.string.isBigInteger")) =
    toBigIntegerOrNull().notNull { message(this) }

/**
 * Validates that the string can be parsed as a Boolean.
 *
 * Accepts "true" or "false" (case-insensitive).
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().isBoolean()
 * validator.validate("true")  // Success
 * validator.validate("false") // Success
 * validator.validate("yes")   // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the is-boolean constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isBoolean(message: MessageProvider0<String> = Message.resource0("kova.string.isBoolean")) =
    toBooleanStrictOrNull().notNull { message(this) }

/**
 * Validates that the string is a valid name for the specified enum type.
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * val validator = Kova.string().isEnum(Role::class)
 * validator.validate("ADMIN") // Success
 * validator.validate("OTHER") // Failure
 * ```
 *
 * @param klass The enum class to validate against
 * @param message Custom error message provider
 * @return A new validator with the is-enum constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun <E : Enum<E>> String.isEnum(
    klass: KClass<E>, message: MessageProvider1<String, List<String>> = Message.resource1("kova.string.isEnum"),
): E = try {
    java.lang.Enum.valueOf(klass.java, this)
} catch (_: IllegalArgumentException) {
    raise(message(this, klass.java.enumConstants.map { it.name }).failure)
}

/**
 * Validates that the string is a valid name for the specified enum type (reified version).
 *
 * Example:
 * ```kotlin
 * enum class Role { ADMIN, USER, GUEST }
 * val validator = Kova.string().isEnum<Role>()
 * validator.validate("ADMIN") // Success
 * validator.validate("OTHER") // Failure
 * ```
 *
 * @return A new validator with the is-enum constraint
 */
@IgnorableReturnValue
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
inline fun <reified E : Enum<E>> String.isEnum(): E = try {
    enumValueOf<E>(this)
} catch (_: IllegalArgumentException) {
    raise(Message.Resource("kova.string.isEnum", this, enumValues<E>().map { it.name }).failure)
}

/**
 * Validates that the string is in uppercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().uppercase()
 * validator.validate("HELLO") // Success
 * validator.validate("hello") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the uppercase constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isUppercase(message: MessageProvider0<String> = Message.resource0("kova.string.uppercase")) =
    constrain(message.id) { satisfies(this == uppercase()) { message(this) } }

/**
 * Validates that the string is in lowercase.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().lowercase()
 * validator.validate("hello") // Success
 * validator.validate("HELLO") // Failure
 * ```
 *
 * @param message Custom error message provider
 * @return A new validator with the lowercase constraint
 */
context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
fun String.isLowercase(message: MessageProvider0<String> = Message.resource0("kova.string.lowercase")) =
    constrain(message.id) { satisfies(this == lowercase()) { message(this) } }