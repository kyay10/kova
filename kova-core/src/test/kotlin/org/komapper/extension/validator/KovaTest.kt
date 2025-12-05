package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class KovaTest :
    FunSpec({

        context("failFast") {
            val validator = Kova.string().min(3).length(4)

            test("failFast = false") {
                validator.tryValidate("ab").shouldBeLeft().size shouldBe 2
            }

            test("failFast = true") {
                validator.tryValidate("ab", ValidationConfig(failFast = true)).shouldBeLeft().shouldBeSingleton()
            }
        }

        context("failFast - plus") {
            val validator = Kova.string().min(3).asNullable() + Kova.string().length(4).asNullable()

            test("failFast = false") {
                validator.tryValidate("ab").shouldBeLeft().size shouldBe 2
            }

            test("failFast = true") {
                validator.tryValidate("ab", ValidationConfig(failFast = true)).shouldBeLeft().shouldBeSingleton()
            }
        }

        context("boolean") {
            val validator = Kova.boolean()

            test("success - true") {
                validator.tryValidate(true).shouldBeRight()
            }

            test("success - false") {
                validator.tryValidate(false).shouldBeRight()
            }
        }

        context("nullable") {
            data class User(
                val name: String?,
                val age: Int?,
            )

            val userSchema =
                object : ObjectSchema<User>() {
                    val nameV = User::name { Kova.nullable<String>().isNull().or(Kova.literal("")) }
                    val ageV = User::age { Kova.nullable<Int>().isNull().or(Kova.literal(0)) }

                    fun bind(
                        name: String?,
                        age: Int?,
                    ) = factory {
                        val name = nameV.bind(name)
                        val age = ageV.bind(age)
                        create(::User, name, age)
                    }
                }

            test("success - null") {
                val userFactory = userSchema.bind(null, null)
                userFactory.tryCreate().shouldBeRight().first shouldBe User(null, null)
            }

            test("success - non-null") {
                val userFactory = userSchema.bind("", 0)
                userFactory.tryCreate().shouldBeRight().first shouldBe User("", 0)
            }

            test("failure") {
                val userFactory = userSchema.bind("abc", 10)
                val details = userFactory.tryCreate().shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe
                    "at least one constraint must be satisfied: [[Value abc must be null], [Value abc must be ]]"
                details[1].message.content shouldBe
                    "at least one constraint must be satisfied: [[Value 10 must be null], [Value 10 must be 0]]"
            }
        }

        context("generic") {

            data class Request(
                private val map: Map<String, String>,
            ) {
                operator fun get(key: String): String? = map[key]
            }

            val notNull = Kova.nullable<String>().notNull()
            val notNullAndMin3 = notNull.and(Kova.string().min(3)).toNonNullable()
            val requestKey = Kova.generic<Request>().name("Request[key]").map { it["key"] }
            val requestKeyIsNotNull = requestKey.then(notNull)
            val requestKeyIsNotNullAndMin3 = requestKey.then(notNullAndMin3)

            test("success - requestKeyIsNotNull") {
                requestKeyIsNotNull.tryValidate(Request(mapOf("key" to "abc"))).shouldBeRight().first shouldBe "abc"
            }

            test("failure - requestKeyIsNotNull") {
                requestKeyIsNotNull.tryValidate(Request(mapOf())).shouldBeLeft().shouldBeSingleton {
                    it.path.fullName shouldBe "Request[key]"
                    it.message.content shouldBe "Value must not be null"
                }
            }

            test("success - requestKeyIsNotNullAndMin3") {
                requestKeyIsNotNullAndMin3.tryValidate(Request(mapOf("key" to "abc")))
                    .shouldBeRight().first shouldBe "abc"
            }

            test("failure - requestKeyIsNotNullAndMin3") {
                requestKeyIsNotNullAndMin3.tryValidate(Request(mapOf("key" to "ab"))).shouldBeLeft()
                    .shouldBeSingleton {
                        it.path.fullName shouldBe "Request[key]"
                        it.message.content shouldBe "\"ab\" must be at least 3 characters"
                    }
            }
        }
    })
