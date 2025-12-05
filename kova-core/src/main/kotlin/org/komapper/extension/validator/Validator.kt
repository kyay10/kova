package org.komapper.extension.validator

import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.raise.RaiseDSL
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import arrow.core.raise.merge

@RaiseDSL
context(raise: RaiseAccumulate<Error>)
fun <Error, A> EitherNel<Error, A>.bindNel(): A =
    with(raise) { this@bindNel.bindNel() }

/**
 * Core validator interface for type-safe validation.
 *
 * A validator transforms an input of type [IN] into an output of type [OUT],
 * or raises a validation failure with detailed error information.
 *
 * Validators are immutable and composable using operators like [plus], [and], [or],
 * [map], [then], and [chain].
 *
 * @param IN The input type to validate
 * @param OUT The output type after successful validation
 */
fun interface Validator<IN, OUT> {
    /**
     * Executes the validation on the given input.
     *
     * @param input The value to validate
     * @param context The validation context tracking state and configuration
     * @return the validated value
     */
    context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
    fun execute(input: IN): Pair<OUT, ValidationContext>

    companion object {
        fun <T> success() = IdentityValidator<T> { it to contextOf<ValidationContext>() }
    }
}

/**
 * Validates the input
 *
 * This is the recommended way to perform validation when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * when (val result = validator.tryValidate("hello")) {
 *     is Either.Right -> println("Valid: ${result.value.first}")
 *     is Either.Left -> println("Errors: ${result.value}")
 * }
 * ```
 *
 * @param input The value to validate
 * @param config Configuration options for validation (failFast, logging)
 * @return the validated value
 */
fun <IN, OUT> Validator<IN, OUT>.tryValidate(
    input: IN,
    config: ValidationConfig = ValidationConfig()
): EitherNel<FailureDetail, Pair<OUT, ValidationContext>> = either { validateAndGetContext(input, config) }

/**
 * Validates the input and returns the validated value, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).max(10)
 * try {
 *     val validated = validator.validate("hello")
 *     println("Valid: $validated")
 * } catch (e: ValidationException) {
 *     println("Errors: ${e.details}")
 * }
 * ```
 *
 * @param input The value to validate
 * @param config Configuration options for validation (failFast, logging)
 * @return The validated value of type [OUT]
 */
context(_: Raise<Nel<FailureDetail>>)
fun <IN, OUT> Validator<IN, OUT>.validate(
    input: IN,
    config: ValidationConfig = ValidationConfig(),
): OUT = validateAndGetContext(input, config).first

context(_: Raise<Nel<FailureDetail>>)
fun <IN, OUT> Validator<IN, OUT>.validateAndGetContext(
    input: IN,
    config: ValidationConfig = ValidationConfig()
): Pair<OUT, ValidationContext> =
    context(ValidationContext(config = config)) { accumulateUnless(failFast) { execute(input) } }

/**
 * Operator overload for [and]. Combines two validators that both must succeed.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(3) + Kova.string().max(10)
 * // Equivalent to: Kova.string().min(3).max(10)
 * ```
 */
operator fun <IN, OUT> Validator<IN, OUT>.plus(other: Validator<IN, OUT>): Validator<IN, OUT> = this and other

/**
 * Combines two validators where both must succeed for the overall validation to succeed.
 *
 * If either validator fails, the failure is included in the result. With failFast enabled,
 * execution stops at the first failure.
 *
 * Example:
 * ```kotlin
 * val nameValidator = Kova.string().min(1) and Kova.string().max(50)
 * val ageValidator = Kova.int().min(0) and Kova.int().max(120)
 * ```
 *
 * @param other The second validator to apply
 * @return A new validator that succeeds only if both validators succeed
 */
infix fun <IN, OUT> Validator<IN, OUT>.and(other: Validator<IN, OUT>): Validator<IN, OUT> = Validator { input ->
    addLog("Validator.and") {
        accumulating { execute(input) }
        other.execute(input)
    }
}

/**
 * Combines two validators where at least one must succeed for the overall validation to succeed.
 *
 * If the first validator succeeds, the second is not executed. If both fail,
 * a composite failure containing both error branches is returned.
 *
 * Example:
 * ```kotlin
 * // Accept either a short string or a string starting with "LONG:"
 * val validator = Kova.string().max(10) or
 *     (Kova.string().min(6).startsWith("LONG:"))
 * ```
 *
 * @param other The alternative validator to try if this one fails
 * @return A new validator that succeeds if either validator succeeds
 */
infix fun <IN, OUT> Validator<IN, OUT>.or(other: Validator<IN, OUT>): Validator<IN, OUT> = Validator { input ->
    addLog("Validator.or") {
        val selfDetails = merge<Nel<FailureDetail>> { return@Validator accumulateUnless(failFast) { execute(input) } }
        val otherDetails =
            merge<Nel<FailureDetail>> { return@Validator accumulateUnless(failFast) { other.execute(input) } }
        raise(CompositeFailureDetail(contextOf<ValidationContext>(), first = selfDetails, second = otherDetails))
    }
}

/**
 * Transforms the output value on successful validation.
 *
 * The transform function is only called if validation succeeds.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).map { it.trim().uppercase() }
 * validator.validate("  hello  ") // Returns "HELLO"
 * ```
 *
 * @param transform Function to transform the validated value
 * @return A new validator with the transformed output type
 */
fun <IN, OUT, NEW> Validator<IN, OUT>.map(
    transform: context(ValidationContext, RaiseAccumulate<FailureDetail>) (OUT) -> NEW
): Validator<IN, NEW> = then {
    transform(it) to contextOf<ValidationContext>()
}

/**
 * Adds a name to the validation path for better error reporting.
 *
 * This is useful for identifying which field failed validation in complex objects.
 *
 * Example:
 * ```kotlin
 * val validator = Kova.string().min(1).name("username")
 * // Failures will show path like "User.username"
 * ```
 *
 * @param name The name to add to the validation path
 * @return A new validator that tracks the path
 */
fun <IN, OUT> Validator<IN, OUT>.name(name: String): Validator<IN, OUT> = Validator { input ->
    addPath(name, input) { addLog("Validator.name(name=$name)") { execute(input) } }
}

/**
 * Composes two validators by applying the [before] validator first, then this validator.
 *
 * This is the reverse of [then].
 *
 * @param before The validator to apply first
 * @return A new validator that applies both validators in sequence
 */
fun <IN, OUT, NEW> Validator<OUT, NEW>.compose(before: Validator<IN, OUT>): Validator<IN, NEW> = before.then(this)

/**
 * Chains two validators sequentially, passing the output of the first to the second.
 *
 * **Key characteristic**: The input type (IN) and output type (OUT) can be different types.
 * This allows type transformation through the validation chain.
 *
 * If the first validator fails, the second is not executed.
 *
 * Example with type transformation:
 * ```kotlin
 * // String -> Int transformation
 * val parseAndValidate = Kova.string()
 *     .isInt()
 *     .map { it.toInt() }
 *     .then(Kova.int().min(0).max(100))
 * // Input: String, Output: Int
 * ```
 *
 * @param after The validator to apply to the output of this validator
 * @return A new validator that applies both validators in sequence
 */
fun <IN, OUT, NEW> Validator<IN, OUT>.then(after: Validator<OUT, NEW>): Validator<IN, NEW> = Validator { input ->
    addLog("Validator.then") {
        val (value, context) = execute(input)
        context(context) { after.execute(value) }
    }
}