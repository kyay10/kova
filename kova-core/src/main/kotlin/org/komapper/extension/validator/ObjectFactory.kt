package org.komapper.extension.validator

import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.NonEmptyList
import arrow.core.nel
import arrow.core.raise.Accumulate
import arrow.core.raise.RaiseDSL
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.accumulate
import arrow.core.raise.context.either
import arrow.core.raise.context.raise
import kotlin.reflect.KFunction

context(raise: Raise<NonEmptyList<Error>>)
@RaiseDSL
inline fun <Error, A> accumulateUnless(failFast: Boolean, block: context(RaiseAccumulate<Error>) () -> A): A =
    if (failFast) block(RaiseAccumulate(object : Accumulate<Error> {
        override val latestError get() = null
        override fun accumulateAll(errors: NonEmptyList<Error>) = raise(errors)
    }) { raise(it.nel()) })
    else accumulate { block() }

/**
 * Factory for validating inputs and constructing objects.
 *
 * ObjectFactory combines validation with object construction, allowing you to
 * validate multiple inputs and then construct an object only if all validations succeed.
 * This is commonly used in ObjectSchema to create validated instances.
 *
 * Example:
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 *
 * object PersonSchema : ObjectSchema<Person>() {
 *     private val name = Person::name { Kova.string().min(1).max(50) }
 *     private val age = Person::age { Kova.int().min(0).max(120) }
 *
 *     fun build(nameInput: String, ageInput: Int) =
 *         arguments(
 *             arg(nameInput, name),
 *             arg(ageInput, age)
 *         ).build(::Person)
 * }
 *
 * // Usage
 * val result = PersonSchema.build("Alice", 30).tryCreate()
 * when (result) {
 *     is ValidationResult.Success -> println("Created: ${result.value}")
 *     is ValidationResult.Failure -> println("Errors: ${result.details}")
 * }
 * ```
 *
 * @param T The type of object this factory creates
 */
typealias ObjectFactory<T> = context(ValidationContext, RaiseAccumulate<FailureDetail>) () -> T

/**
 * Validates inputs and attempts to create an object, returning an [arrow.core.Either].
 *
 * This is the recommended way to use ObjectFactory when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val factory = PersonSchema.build("Alice", 30)
 * val result = factory.tryCreate()
 * when (result) {
 *     is Either.Right -> println("Created: ${result.value}")
 *     is Either.Left -> result.value.forEach { println(it.message.content) }
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return A validation result containing either the created object or failure details
 */
fun <T> ObjectFactory<T>.tryCreate(
    config: ValidationConfig = ValidationConfig()
): EitherNel<FailureDetail, T> = either { create(config) }

/**
 * Validates inputs and creates an object, or throws an exception on failure.
 *
 * Use this when you want validation failures to throw exceptions rather than
 * handling them programmatically.
 *
 * Example:
 * ```kotlin
 * try {
 *     val person = PersonSchema.build("Alice", 30).create()
 *     println("Created: $person")
 * } catch (e: ValidationException) {
 *     println("Validation failed: ${e.messages}")
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return The created object of type [T]
 */
context(_: Raise<Nel<FailureDetail>>)
fun <T> ObjectFactory<T>.create(config: ValidationConfig = ValidationConfig()): T =
    context(ValidationContext(config = config)) { accumulateUnless(failFast) { this() } }

internal data class FunctionDesc(
    val name: String,
    private val parameters: Map<Int, String?>,
) {
    operator fun get(index: Int): String {
        if (index < 0 || parameters.size <= index) return "param$index"
        return parameters[index] ?: "param$index"
    }
}

private val isKotlinReflectAvailable: Boolean =
    try {
        Class.forName("kotlin.reflect.full.KClasses")
        true
    } catch (ignored: ClassNotFoundException) {
        false
    }

@Suppress("NO_REFLECTION_IN_CLASS_PATH")
private fun introspectFunction(ctor: Any): FunctionDesc =
    if (ctor is KFunction<*>) {
        val parameters =
            if (isKotlinReflectAvailable) {
                ctor.parameters.withIndex().associate { (i, p) -> i to p.name }
            } else {
                emptyMap()
            }
        FunctionDesc(ctor.name, parameters)
    } else {
        FunctionDesc(ctor.toString(), emptyMap())
    }

internal fun <T0, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0) -> R,
    arg0: ObjectFactory<T0>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        ctor(addPath(funInfo[0], null) { arg0() }).also { validator(it) }
    }
}

internal fun <T0, T1, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        ctor(result0, result1).also { validator(it) }
    }
}

internal fun <T0, T1, T2, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        ctor(result0, result1, result2).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        ctor(result0, result1, result2, result3).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, T4, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3, T4) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        val result4 by accumulating { addPath(funInfo[4], null) { arg4() } }
        ctor(result0, result1, result2, result3, result4).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3, T4, T5) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        val result4 by accumulating { addPath(funInfo[4], null) { arg4() } }
        val result5 by accumulating { addPath(funInfo[5], null) { arg5() } }
        ctor(result0, result1, result2, result3, result4, result5).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3, T4, T5, T6) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
    arg6: ObjectFactory<T6>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        val result4 by accumulating { addPath(funInfo[4], null) { arg4() } }
        val result5 by accumulating { addPath(funInfo[5], null) { arg5() } }
        val result6 by accumulating { addPath(funInfo[6], null) { arg6() } }
        ctor(result0, result1, result2, result3, result4, result5, result6).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, T7, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3, T4, T5, T6, T7) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
    arg6: ObjectFactory<T6>,
    arg7: ObjectFactory<T7>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        val result4 by accumulating { addPath(funInfo[4], null) { arg4() } }
        val result5 by accumulating { addPath(funInfo[5], null) { arg5() } }
        val result6 by accumulating { addPath(funInfo[6], null) { arg6() } }
        val result7 by accumulating { addPath(funInfo[7], null) { arg7() } }
        ctor(result0, result1, result2, result3, result4, result5, result6, result7).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3, T4, T5, T6, T7, T8) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
    arg6: ObjectFactory<T6>,
    arg7: ObjectFactory<T7>,
    arg8: ObjectFactory<T8>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        val result4 by accumulating { addPath(funInfo[4], null) { arg4() } }
        val result5 by accumulating { addPath(funInfo[5], null) { arg5() } }
        val result6 by accumulating { addPath(funInfo[6], null) { arg6() } }
        val result7 by accumulating { addPath(funInfo[7], null) { arg7() } }
        val result8 by accumulating { addPath(funInfo[8], null) { arg8() } }
        ctor(result0, result1, result2, result3, result4, result5, result6, result7, result8).also { validator(it) }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, R> createObjectFactory(
    validator: Constraint<R>,
    ctor: (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
    arg6: ObjectFactory<T6>,
    arg7: ObjectFactory<T7>,
    arg8: ObjectFactory<T8>,
    arg9: ObjectFactory<T9>,
): ObjectFactory<R> = {
    val funInfo = introspectFunction(ctor)
    addRoot(funInfo.name, ctor) {
        val result0 by accumulating { addPath(funInfo[0], null) { arg0() } }
        val result1 by accumulating { addPath(funInfo[1], null) { arg1() } }
        val result2 by accumulating { addPath(funInfo[2], null) { arg2() } }
        val result3 by accumulating { addPath(funInfo[3], null) { arg3() } }
        val result4 by accumulating { addPath(funInfo[4], null) { arg4() } }
        val result5 by accumulating { addPath(funInfo[5], null) { arg5() } }
        val result6 by accumulating { addPath(funInfo[6], null) { arg6() } }
        val result7 by accumulating { addPath(funInfo[7], null) { arg7() } }
        val result8 by accumulating { addPath(funInfo[8], null) { arg8() } }
        val result9 by accumulating { addPath(funInfo[9], null) { arg9() } }
        ctor(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9)
            .also { validator(it) }
    }
}
