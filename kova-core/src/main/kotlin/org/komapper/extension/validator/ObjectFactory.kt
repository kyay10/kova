package org.komapper.extension.validator

import kotlin.reflect.KFunction

context(_: ValidationContext)
private fun <T> shouldReturnEarly(validationResult: ValidationResult<T>): Boolean =
    failFast && validationResult.isFailure()

context(_: ValidationContext)
private fun <T> tryConstruct(validator: IdentityValidator<T>, block: () -> T) = validator.execute(block())

private fun <T : Any> createFailure(
    arg: ValidationResult<*>,
    vararg args: ValidationResult<*>,
): ValidationResult<T> {
    val result =
        (listOf(arg) + args)
            .filterIsInstance<ValidationResult.Failure>()
            .map { it as ValidationResult<Any?> }
            .reduce { a, b -> a + b }
    return when (result) {
        is ValidationResult.Success -> error("This should never happen.")
        is ValidationResult.Failure -> result
    }
}

private fun <T> unwrapValidationResult(result: ValidationResult<T>): T =
    when (result) {
        is ValidationResult.Success -> result.value
        is ValidationResult.Failure -> throw ValidationException(result.details)
    }

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
fun interface ObjectFactory<T> {
    /**
     * Executes validation and object construction.
     *
     * @param context The validation context
     * @return A validation result containing either the constructed object or failure details
     */
    context(_: ValidationContext)
    fun execute(): ValidationResult<T>
}

/**
 * Validates inputs and attempts to create an object, returning a [ValidationResult].
 *
 * This is the recommended way to use ObjectFactory when you want to handle
 * both success and failure cases programmatically.
 *
 * Example:
 * ```kotlin
 * val factory = PersonSchema.build("Alice", 30)
 * val result = factory.tryCreate()
 * when (result) {
 *     is ValidationResult.Success -> println("Created: ${result.value}")
 *     is ValidationResult.Failure -> result.details.forEach { println(it.message.content) }
 * }
 * ```
 *
 * @param config Configuration options for validation (failFast, logging)
 * @return A validation result containing either the created object or failure details
 */
fun <T> ObjectFactory<T>.tryCreate(config: ValidationConfig = ValidationConfig()): ValidationResult<T> =
    context(ValidationContext(config = config)) { execute() }

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
 * @throws ValidationException if validation fails
 */
fun <T> ObjectFactory<T>.create(config: ValidationConfig = ValidationConfig()): T =
    unwrapValidationResult(context(ValidationContext(config = config)) { execute() })

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
    validator: IdentityValidator<R>,
    ctor: (T0) -> R,
    arg0: ObjectFactory<T0>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess()) {
            tryConstruct(validator) {
                ctor(result0.value)
            }
        } else {
            createFailure(result0)
        }
    }
}

internal fun <T0, T1, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() && result1.isSuccess()) {
            tryConstruct(validator) {
                ctor(result0.value, result1.value)
            }
        } else {
            createFailure(result0, result1)
        }
    }
}

internal fun <T0, T1, T2, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1, T2) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() && result1.isSuccess() && result2.isSuccess()) {
            tryConstruct(validator) {
                ctor(result0.value, result1.value, result2.value)
            }
        } else {
            createFailure(result0, result1, result2)
        }
    }
}

internal fun <T0, T1, T2, T3, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1, T2, T3) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() && result1.isSuccess() && result2.isSuccess() && result3.isSuccess()) {
            tryConstruct(validator) {
                ctor(result0.value, result1.value, result2.value, result3.value)
            }
        } else {
            createFailure(result0, result1, result2, result3)
        }
    }
}

internal fun <T0, T1, T2, T3, T4, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1, T2, T3, T4) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result4 =
            addPath(funInfo[4], null) { arg4.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() && result1.isSuccess() && result2.isSuccess() && result3.isSuccess() && result4.isSuccess()) {
            tryConstruct(validator) {
                ctor(result0.value, result1.value, result2.value, result3.value, result4.value)
            }
        } else {
            createFailure(result0, result1, result2, result3, result4)
        }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1, T2, T3, T4, T5) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result4 =
            addPath(funInfo[4], null) { arg4.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result5 =
            addPath(funInfo[5], null) { arg5.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() &&
            result1.isSuccess() &&
            result2.isSuccess() &&
            result3.isSuccess() &&
            result4.isSuccess() &&
            result5.isSuccess()
        ) {
            tryConstruct(validator) {
                ctor(result0.value, result1.value, result2.value, result3.value, result4.value, result5.value)
            }
        } else {
            createFailure(result0, result1, result2, result3, result4, result5)
        }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1, T2, T3, T4, T5, T6) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
    arg6: ObjectFactory<T6>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result4 =
            addPath(funInfo[4], null) { arg4.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result5 =
            addPath(funInfo[5], null) { arg5.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result6 =
            addPath(funInfo[6], null) { arg6.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() &&
            result1.isSuccess() &&
            result2.isSuccess() &&
            result3.isSuccess() &&
            result4.isSuccess() &&
            result5.isSuccess() &&
            result6.isSuccess()
        ) {
            tryConstruct(validator) {
                ctor(
                    result0.value,
                    result1.value,
                    result2.value,
                    result3.value,
                    result4.value,
                    result5.value,
                    result6.value,
                )
            }
        } else {
            createFailure(result0, result1, result2, result3, result4, result5, result6)
        }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, T7, R> createObjectFactory(
    validator: IdentityValidator<R>,
    ctor: (T0, T1, T2, T3, T4, T5, T6, T7) -> R,
    arg0: ObjectFactory<T0>,
    arg1: ObjectFactory<T1>,
    arg2: ObjectFactory<T2>,
    arg3: ObjectFactory<T3>,
    arg4: ObjectFactory<T4>,
    arg5: ObjectFactory<T5>,
    arg6: ObjectFactory<T6>,
    arg7: ObjectFactory<T7>,
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result4 =
            addPath(funInfo[4], null) { arg4.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result5 =
            addPath(funInfo[5], null) { arg5.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result6 =
            addPath(funInfo[6], null) { arg6.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result7 =
            addPath(funInfo[7], null) { arg7.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() &&
            result1.isSuccess() &&
            result2.isSuccess() &&
            result3.isSuccess() &&
            result4.isSuccess() &&
            result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess()
        ) {
            tryConstruct(validator) {
                ctor(
                    result0.value,
                    result1.value,
                    result2.value,
                    result3.value,
                    result4.value,
                    result5.value,
                    result6.value,
                    result7.value,
                )
            }
        } else {
            createFailure(result0, result1, result2, result3, result4, result5, result6, result7)
        }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, R> createObjectFactory(
    validator: IdentityValidator<R>,
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
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result4 =
            addPath(funInfo[4], null) { arg4.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result5 =
            addPath(funInfo[5], null) { arg5.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result6 =
            addPath(funInfo[6], null) { arg6.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result7 =
            addPath(funInfo[7], null) { arg7.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result8 =
            addPath(funInfo[8], null) { arg8.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() &&
            result1.isSuccess() &&
            result2.isSuccess() &&
            result3.isSuccess() &&
            result4.isSuccess() &&
            result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess() &&
            result8.isSuccess()
        ) {
            tryConstruct(validator) {
                ctor(
                    result0.value,
                    result1.value,
                    result2.value,
                    result3.value,
                    result4.value,
                    result5.value,
                    result6.value,
                    result7.value,
                    result8.value,
                )
            }
        } else {
            createFailure(result0, result1, result2, result3, result4, result5, result6, result7, result8)
        }
    }
}

internal fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, R> createObjectFactory(
    validator: IdentityValidator<R>,
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
): ObjectFactory<R> = ObjectFactory {
        val funInfo = introspectFunction(ctor)
        addRoot(funInfo.name, ctor) {
        val result0 =
            addPath(funInfo[0], null) { arg0.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result1 =
            addPath(funInfo[1], null) { arg1.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result2 =
            addPath(funInfo[2], null) { arg2.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result3 =
            addPath(funInfo[3], null) { arg3.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result4 =
            addPath(funInfo[4], null) { arg4.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result5 =
            addPath(funInfo[5], null) { arg5.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result6 =
            addPath(funInfo[6], null) { arg6.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result7 =
            addPath(funInfo[7], null) { arg7.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result8 =
            addPath(funInfo[8], null) { arg8.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        val result9 =
            addPath(funInfo[9], null) { arg9.execute() }.let {
                if (shouldReturnEarly(it)) return@ObjectFactory createFailure(it) else it
            }
        if (result0.isSuccess() &&
            result1.isSuccess() &&
            result2.isSuccess() &&
            result3.isSuccess() &&
            result4.isSuccess() &&
            result5.isSuccess() &&
            result6.isSuccess() &&
            result7.isSuccess() &&
            result8.isSuccess() &&
            result9.isSuccess()
        ) {
            tryConstruct(validator) {
                ctor(
                    result0.value,
                    result1.value,
                    result2.value,
                    result3.value,
                    result4.value,
                    result5.value,
                    result6.value,
                    result7.value,
                    result8.value,
                    result9.value,
                )
            }
        } else {
            createFailure(result0, result1, result2, result3, result4, result5, result6, result7, result8, result9)
        }
    }
}
