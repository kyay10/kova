package org.komapper.extension.validator

import org.komapper.extension.validator.Message.Resource
import org.komapper.extension.validator.Message.Text

/**
 * Factory for creating Message providers
 *
 * Available through `Message.text0()` and `Message.resource0()`.
 */
interface MessageProvider0Factory {
    /**
     * Creates a text-based message provider with no arguments.
     *
     * Use this for custom hardcoded error messages.
     *
     * Example:
     * ```kotlin
     * fun positive(message: MessageProvider0<Int> = Message.text0 { context ->
     *     "Number ${context.input} must be positive"
     * }): NumberValidator<Int>
     * ```
     *
     * @param get Function that generates the message text
     * @return A message provider
     */
    fun <T> text0(get: (T) -> String): (T) -> Message = { Text(get(it)) }

    /**
     * Creates a resource bundle-based message provider with no arguments.
     *
     * Use this for i18n support. The message is loaded from `kova.properties`.
     *
     * Example:
     * ```kotlin
     * fun notBlank(message: MessageProvider0<String> = Message.resource0("kova.string.notBlank")): StringValidator
     * ```
     *
     * @param id The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T> resource0(id: String): (T) -> Message = { Resource(id, it) }
}

/**
 * Factory for creating Message providers
 *
 * Available through `Message.text1()` and `Message.resource1()`.
 */
interface MessageProvider1Factory {
    /**
     * Creates a text-based message provider with one argument.
     *
     * Use this for custom error messages that include one dynamic value.
     *
     * Example:
     * ```kotlin
     * fun min(
     *     minValue: Int,
     *     message: MessageProvider1<String, Int> = Message.text1 { context, min ->
     *         "String must be at least $min characters, but was ${context.input.length}"
     *     }
     * ): StringValidator
     * ```
     *
     * @param get Function that generates the message text from context and argument
     * @return A message provider
     */
    fun <T, A1> text1(get: (T, A1) -> String): (T, A1) -> Message = { input, arg -> Text(get(input, arg)) }

    /**
     * Creates a resource bundle-based message provider with one argument.
     *
     * Use this for i18n support with one dynamic value.
     * The message pattern uses `{0}` for the input and `{1}` for the argument.
     *
     * Example resource (kova.properties):
     * ```properties
     * kova.string.min={0} must be at least {1} characters
     * ```
     *
     * Example usage:
     * ```kotlin
     * fun min(
     *     minLength: Int,
     *     message: MessageProvider1<String, Int> = Message.resource1("kova.string.min")
     * ): StringValidator
     * ```
     *
     * @param id The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T, A1> resource1(id: String): (T, A1) -> Message = { input, arg1 -> Resource(id, input, arg1) }
}

/**
 * Factory for creating Message providers
 *
 * Available through `Message.text2()` and `Message.resource2()`.
 */
interface MessageProvider2Factory {
    /**
     * Creates a text-based message provider with two arguments.
     *
     * Use this for custom error messages that include two dynamic values.
     *
     * Example:
     * ```kotlin
     * fun range(
     *     min: Int,
     *     max: Int,
     *     message: MessageProvider2<Int, Int, Int> = Message.text2 { context, minVal, maxVal ->
     *         "Value must be between $minVal and $maxVal, but was ${context.input}"
     *     }
     * ): NumberValidator<Int>
     * ```
     *
     * @param get Function that generates the message text from context and arguments
     * @return A message provider
     */
    fun <T, A1, A2> text2(get: (T, A1, A2) -> String): (T, A1, A2) -> Message = { input, arg1, arg2 ->
        Text(get(input, arg1, arg2))
    }

    /**
     * Creates a resource bundle-based message provider with two arguments.
     *
     * Use this for i18n support with two dynamic values.
     * The message pattern uses `{0}` for the input, `{1}` for the first argument,
     * and `{2}` for the second argument.
     *
     * Example resource (kova.properties):
     * ```properties
     * kova.collection.min={0} must have at least {1} elements, but has {2}
     * ```
     *
     * Example usage:
     * ```kotlin
     * fun min(
     *     size: Int,
     *     message: MessageProvider2<List<*>, Int, Int> = Message.resource2("kova.collection.min")
     * ): CollectionValidator<E, C>
     * ```
     *
     * @param id The resource bundle key
     * @return A message provider that loads messages from resources
     */
    fun <T, A1, A2> resource2(id: String): (T, A1, A2) -> Message = { input, arg1, arg2 ->
        Resource(id, input, arg1, arg2)
    }
}
