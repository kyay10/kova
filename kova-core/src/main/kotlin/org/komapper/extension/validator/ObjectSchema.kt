package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.impure
import kotlin.reflect.KProperty1

/**
 * Base class for defining validation schemas for objects.
 *
 * ObjectSchema allows you to define validation rules for each property of an object,
 * as well as object-level constraints that validate relationships between properties.
 *
 * Example usage:
 * ```kotlin
 * data class User(val name: String, val age: Int)
 *
 * object UserSchema : ObjectSchema<User>() {
 *     val name = User::name { Kova.string().min(1).max(50) }
 *     val age = User::age { Kova.int().min(0).max(120) }
 * }
 *
 * // Validate an object
 * val result = UserSchema.tryValidate(User("Alice", 30))
 * ```
 *
 * Object-level constraints:
 * ```kotlin
 * data class Period(val startDate: LocalDate, val endDate: LocalDate)
 *
 * object PeriodSchema : ObjectSchema<Period>({
 *     constrain("dateRange") {
 *         satisfies(it.startDate <= it.endDate, "Start date must be before end date")
 *     }
 * }) {
 *     val startDate = Period::startDate { Kova.localDate() }
 *     val endDate = Period::endDate { Kova.localDate() }
 * }
 * ```
 *
 * Object construction with validation:
 * ```kotlin
 * object PersonSchema : ObjectSchema<Person>() {
 *     private val nameV = Person::name { Kova.string().min(1) }
 *     private val ageV = Person::age { Kova.int().min(0) }
 *
 *     fun bind(name: String, age: Int) = factory {
 *         create(::Person, nameV.bind(name), ageV.bind(age))
 *     }
 * }
 *
 * // Validate and construct an object
 * val result = PersonSchema.bind("Alice", 30).tryCreate()
 * ```
 *
 * @param T The type of object to validate
 */
open class ObjectSchema<T : Any> private constructor(
    private val ruleMap: MutableMap<String, Rule>,
    private val constraint: Constraint<T> = {},
) : (ValidationContext, RaiseAccumulate<FailureDetail>, T) -> Unit {
    /**
     * Creates an ObjectSchema with optional object-level constraints.
     *
     * @param block Lambda for defining constraints that validate relationships between properties
     */
    constructor(block: Constraint<T> = {}) : this(
        mutableMapOf(),
        block
    )

    override fun invoke(c: ValidationContext, r: RaiseAccumulate<FailureDetail>, input: T) = context(c, r) {
        val klass = input::class
        val rootName = klass.qualifiedName ?: klass.simpleName ?: klass.toString()
        addRoot(rootName, input) {
            for ((key, rule) in ruleMap) accumulating { applyRule(input, key, rule) }
            constraint(input)
        }
    }

    context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
    private fun applyRule(input: T, key: String, rule: Rule) {
        val value = rule.transform(input)
        // If circular reference detected, terminate validation early with success
        impure { addPathChecked(key, value) { rule.choose(input)(value) } }
    }

    /**
     * Replaces the validator for a specific property.
     *
     * This allows you to override the validation rule for a property after the schema has been defined.
     *
     * @param key The property to replace the validator for
     * @param validator The new validator to use
     * @return A new ObjectSchema with the replaced validator
     */
    fun <V> replace(
        key: KProperty1<T, V>,
        validator: Constraint<V>,
    ): ObjectSchema<T> {
        val rule =
            Rule(
                transform = { receiver: T -> key.get(receiver) } as (Any?) -> Any?,
                choose = { _: T -> validator } as (Any?) -> Constraint<Any?>,
            )
        val newRuleMap = ruleMap.toMutableMap()
        newRuleMap.replace(key.name, rule)
        return ObjectSchema(newRuleMap)
    }

    /**
     * Defines a validation rule for a property.
     *
     * This operator function allows you to use the invoke syntax to define validators
     * for object properties. The property must be defined as a member of the ObjectSchema
     * subclass (not in the constructor lambda).
     *
     * Example:
     * ```kotlin
     * object UserSchema : ObjectSchema<User>() {
     *     val name = User::name { Kova.string().min(1).max(50) }
     *     // The property 'name' gets the returned StringValidator
     * }
     * ```
     *
     * @param block Lambda that creates the validator for this property
     * @return The validator created by the block
     */
    operator fun <V, C : (ValidationContext, RaiseAccumulate<FailureDetail>, V) -> Unit> KProperty1<T, V>.invoke(block: () -> C): C {
        val validator = block()
        addRule(this) { _ -> validator }
        return validator
    }

    /**
     * Defines a conditional validation rule for a property based on the object's state.
     *
     * This allows you to choose different validators based on the value of other properties.
     *
     * Example:
     * ```kotlin
     * object UserSchema : ObjectSchema<User>() {
     *     val type = User::type { Kova.string() }
     *     val identifier = User::identifier choose { user ->
     *         when (user.type) {
     *             "email" -> Kova.string().email()
     *             "phone" -> Kova.string().matches(phoneRegex)
     *             else -> Kova.string().min(1)
     *         }
     *     }
     * }
     * ```
     *
     * @param block Lambda that chooses a validator based on the object
     * @return The block function for further use
     */
    infix fun <V, C: (ValidationContext, RaiseAccumulate<FailureDetail>, V) -> Unit> KProperty1<T, V>.choose(block: (T) -> C): (T) -> C {
        addRule(this, block)
        return block
    }

    private fun <T, V> addRule(
        key: KProperty1<T, V>,
        choose: (T) -> Constraint<V>,
    ) {
        val transform = { receiver: T -> key.get(receiver) }
        transform as (Any?) -> Any?
        choose as (Any?) -> Constraint<Any?>
        ruleMap[key.name] = Rule(transform, choose)
    }

    /**
     * Creates an object factory scope for composing validators with object construction.
     *
     * This method provides access to the `bind` and `create` methods for building
     * ObjectFactories that validate inputs and construct objects.
     *
     * Example:
     * ```kotlin
     * object PersonSchema : ObjectSchema<Person>() {
     *     private val nameV = Person::name { Kova.string().min(1) }
     *     private val ageV = Person::age { Kova.int().min(0) }
     *
     *     fun bind(name: String, age: Int) = factory {
     *         create(::Person, nameV.bind(name), ageV.bind(age))
     *     }
     * }
     *
     * val result = PersonSchema.bind("Alice", 30).tryCreate()
     * ```
     *
     * @param block Lambda with ObjectSchemaFactoryScope receiver that returns an ObjectFactory
     * @return An ObjectFactory that validates inputs and constructs the object
     */
    fun factory(block: ObjectSchemaFactoryScope<T>.() -> ObjectFactory<T>): ObjectFactory<T> =
        ObjectSchemaFactoryScope(this).block()
}

internal data class Rule(
    val transform: (Any?) -> Any?,
    val choose: (Any?) -> Constraint<Any?>,
)

class ObjectSchemaFactoryScope<T : Any>(
    val validator: Constraint<T>,
) {
    /**
     * Binds a validator to a specific value, creating an ObjectFactory.
     *
     * This extension function converts a validator and its input value into an ObjectFactory
     * that can be used with the `create` method for object validation and construction.
     *
     * Example:
     * ```kotlin
     * object PersonSchema : ObjectSchema<Person>() {
     *     private val nameV = Person::name { Kova.string().min(1) }
     *     private val ageV = Person::age { Kova.int().min(0) }
     *
     *     fun bind(name: String, age: Int) = factory {
     *         create(::Person, nameV.bind(name), ageV.bind(age))
     *     }
     * }
     *
     * val result = PersonSchema.bind("Alice", 30).tryCreate()
     * ```
     *
     * @param value The input value to validate
     * @return An ObjectFactory that executes the validator with the bound value
     */
    fun <IN, OUT> Validator<IN, OUT>.bind(value: IN): ObjectFactory<OUT> = {
        bindObject(value) { this(value) }
    }

    @JvmName("bindObject")
    fun <IN> Constraint<IN>.bind(value: IN): ObjectFactory<IN> = {
        bindObject(value) { value.also { this(value) } }
    }

    /**
     * Creates an object factory with 1 ObjectFactory argument.
     *
     * This method validates and constructs objects from ObjectFactories.
     * Each ObjectFactory argument is created by binding a validator to a value.
     *
     * Example:
     * ```kotlin
     * object PersonSchema : ObjectSchema<Person>() {
     *     private val nameV = Person::name { Kova.string().min(1) }
     *
     *     fun bind(name: String) = factory {
     *         create(::Person, nameV.bind(name))
     *     }
     * }
     *
     * val result = PersonSchema.bind("Alice").tryCreate()
     * ```
     *
     * @param ctor The constructor function to create the object
     * @param arg0 The ObjectFactory for the first argument
     * @return An ObjectFactory that validates inputs and constructs the object
     */
    fun <T0> create(
        ctor: (T0) -> T,
        arg0: ObjectFactory<T0>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0)

    /**
     * Creates an object factory with 2 ObjectFactory arguments.
     *
     * @param ctor The constructor function to create the object
     * @param arg0 The ObjectFactory for the first argument
     * @param arg1 The ObjectFactory for the second argument
     * @return An ObjectFactory that validates inputs and constructs the object
     * @see create for usage examples
     */
    fun <T0, T1> create(
        ctor: (T0, T1) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1)

    /** Creates an object factory with 3 ObjectFactory arguments. @see create */
    fun <T0, T1, T2> create(
        ctor: (T0, T1, T2) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2)

    /** Creates an object factory with 4 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3> create(
        ctor: (T0, T1, T2, T3) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
        arg3: ObjectFactory<T3>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3)

    /** Creates an object factory with 5 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3, T4> create(
        ctor: (T0, T1, T2, T3, T4) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
        arg3: ObjectFactory<T3>,
        arg4: ObjectFactory<T4>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3, arg4)

    /** Creates an object factory with 6 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3, T4, T5> create(
        ctor: (T0, T1, T2, T3, T4, T5) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
        arg3: ObjectFactory<T3>,
        arg4: ObjectFactory<T4>,
        arg5: ObjectFactory<T5>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3, arg4, arg5)

    /** Creates an object factory with 7 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3, T4, T5, T6> create(
        ctor: (T0, T1, T2, T3, T4, T5, T6) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
        arg3: ObjectFactory<T3>,
        arg4: ObjectFactory<T4>,
        arg5: ObjectFactory<T5>,
        arg6: ObjectFactory<T6>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3, arg4, arg5, arg6)

    /** Creates an object factory with 8 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3, T4, T5, T6, T7> create(
        ctor: (T0, T1, T2, T3, T4, T5, T6, T7) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
        arg3: ObjectFactory<T3>,
        arg4: ObjectFactory<T4>,
        arg5: ObjectFactory<T5>,
        arg6: ObjectFactory<T6>,
        arg7: ObjectFactory<T7>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7)

    /** Creates an object factory with 9 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3, T4, T5, T6, T7, T8> create(
        ctor: (T0, T1, T2, T3, T4, T5, T6, T7, T8) -> T,
        arg0: ObjectFactory<T0>,
        arg1: ObjectFactory<T1>,
        arg2: ObjectFactory<T2>,
        arg3: ObjectFactory<T3>,
        arg4: ObjectFactory<T4>,
        arg5: ObjectFactory<T5>,
        arg6: ObjectFactory<T6>,
        arg7: ObjectFactory<T7>,
        arg8: ObjectFactory<T8>,
    ): ObjectFactory<T> = createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8)

    /** Creates an object factory with 10 ObjectFactory arguments. @see create */
    fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9> create(
        ctor: (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> T,
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
    ): ObjectFactory<T> =
        createObjectFactory(validator, ctor, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
}
