package org.komapper.extension.validator

import java.text.MessageFormat
import java.util.ResourceBundle

/**
 * Represents an error message for validation failures.
 *
 * Messages can be simple text, resource bundle entries (i18n), or contain nested validation failures.
 * Use the companion object factory methods to create message providers for custom validators.
 *
 * Example usage:
 * ```kotlin
 * // Simple text message
 * val textMessage = Message.Text("Value must be positive")
 *
 * // Resource bundle message (i18n)
 * val resourceMessage = Message.Resource("kova.string.min", input, minLength)
 *
 * // Custom message provider
 * val customProvider = Message.text0<String> { context ->
 *     "Invalid value: ${context.input}"
 * }
 * ```
 */
sealed interface Message {
    /**
     * A simple text message without i18n support.
     *
     * Use this for hardcoded error messages or when i18n is not needed.
     *
     * Example:
     * ```kotlin
     * Message.Text("Value must be positive")
     * Message.Text(content = "Value must be positive")
     * ```
     */
    class Text(private val content: String) : Message {
        override fun toString() = content
    }

    /**
     * A message loaded from a resource bundle for i18n support.
     *
     * Messages are loaded from `kova.properties` files using [MessageFormat] for parameter substitution.
     *
     * Example resource file (kova.properties):
     * ```properties
     * custom.positive=Number {0} must be positive
     * custom.range=Number {0} must be between {1} and {2}
     * ```
     *
     * Example usage:
     * ```kotlin
     * Message.Resource("custom.positive", value)
     * Message.Resource("custom.range", value, min, max)
     * ```
     *
     * @param id The resource bundle key
     * @param args Arguments to substitute into the message pattern
     */
    class Resource(internal val id: String, vararg args: Any?) : Message {
        override fun toString() = content
        private val content: String by lazy { MessageFormat.format(getPattern(id), *args) }
    }

    companion object {
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
}

private const val RESOURCE_BUNDLE_BASE_NAME = "kova"
private val bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_BASE_NAME)

internal fun getPattern(key: String) = bundle.getString(key)
