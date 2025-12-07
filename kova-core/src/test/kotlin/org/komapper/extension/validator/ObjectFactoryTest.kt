package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ObjectFactoryTest :
    FunSpec({
        context("withDefault") {
            data class User(val name: String?, val age: Int?)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeUser(name: String?, age: Int?): User = constructing {
                val name by name.parameter("name") { it ?: "" }
                val age by age.parameter("age") { it ?: 0 }
                User(name, age)
            }

            test("success - null") {
                shouldBeValid { makeUser(null, null) } shouldBe User("", 0)
            }

            test("success - non-null") {
                shouldBeValid { makeUser("abc", 10) } shouldBe User("abc", 10)
            }
        }

        context("1 arg") {
            data class User(val id: Int)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeUser(id: Int): User = constructing {
                id.property("id") { it.min(1) }
                User(id)
            }

            test("success - create") {
                shouldBeValid { makeUser(1) } shouldBe User(1)
            }

            test("failure - create") {
                val detail = shouldBeInvalidSingle { makeUser(-1) }
                detail.root shouldContain "User"
                detail.path.fullName shouldBe "id"
                detail.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }
        }

        context("2 args") {
            data class User(val id: Int, val name: String)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeUser(id: Int, name: String): User = constructing {
                id.property("id") { it.min(1) }
                name.property("name") {
                    it.min(1)
                    it.max(10)
                }
                User(id, name)
            }

            test("success") {
                shouldBeValid { makeUser(1, "abc") } shouldBe User(1, "abc")
            }

            test("failure") {
                shouldBeInvalid { makeUser(0, "") } shouldHaveSize 2
            }

            test("failure - failFast is true") {
                shouldBeInvalidSingle(failFast = true) { makeUser(0, "") }
            }
        }

        context("2 args - generic validator") {
            data class User(val id: Int, val name: String)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeUser(id: Int, name: String): User = constructing {
                id.property("id") { }
                name.property("name") { }
                User(id, name)
            }

            test("success") {
                shouldBeValid { makeUser(1, "abc") } shouldBe User(1, "abc")
            }
        }

        context("2 args - nested factory") {
            data class Age(val value: Int)
            data class Name(val value: String)
            data class Person(val name: Name, val age: Age)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeAge(value: Int): Age = constructing {
                value.property("value") { it.min(0) }
                Age(value)
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeName(name: String): Name = constructing {
                name.property("value") { it.notBlank() }
                Name(name)
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makePerson(name: String, age: Int): Person = constructing {
                val name by name.parameter("name") { makeName(it) }
                val age by age.parameter("age") { makeAge(it) }
                Person(name, age)
            }

            test("success") {
                shouldBeValid { makePerson("abc", 10) } shouldBe Person(Name("abc"), Age(10))
            }
        }
    })
