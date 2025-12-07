package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KovaTest :
    FunSpec({

        context("failFast") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.validate() {
                accumulatingUnit { min(3) }
                length(4)
            }

            test("failFast = false") {
                shouldBeInvalid { "ab".validate() } shouldHaveSize 2
            }

            test("failFast = true") {
                shouldBeInvalidSingle(failFast = true) { "ab".validate() }
            }
        }

        context("failFast - plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String?.validate() {
                accumulatingUnit { this?.min(3) }
                this?.length(4)
            }

            test("failFast = false") {
                shouldBeInvalid { "ab".validate() } shouldHaveSize 2
            }

            test("failFast = true") {
                shouldBeInvalidSingle(failFast = true) { "ab".validate() }
            }
        }

        context("empty") {
            test("success") {
                shouldBeValid { }
            }
        }

        context("nullable") {
            data class User(val name: String?, val age: Int?)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun makeUser(name: String?, age: Int?): User = constructing {
                name.property("name") { or { it.isNull() } or { it literal "" } or Fail }
                age.property("age") { or { age.isNull() } or { age literal 0 } or Fail }
                User(name, age)
            }

            test("success - null") {
                shouldBeValid { makeUser(null, null) } shouldBe User(null, null)
            }

            test("success - non-null") {
                shouldBeValid { makeUser("", 0) } shouldBe User("", 0)
            }

            test("failure") {
                val details = shouldBeInvalid { makeUser("abc", 10) }
                details shouldHaveSize 2
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

            context(_: ValidationContext)
            fun Request.validateKey(block: context(ValidationContext) (String?) -> Unit) {
                val key = this["key"]
                this["key"].addPath("Request[key]") { block(key) }
            }

            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Request.validateKeyIsNotNull() = validateKey { it.notNull() }

            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Request.validateKeyIsNotNullAndMin3() = validateKey {
                it.notNull()
                it min 3
            }

            test("success - requestKeyIsNotNull") {
                shouldBeValid { Request(mapOf("key" to "abc")).validateKeyIsNotNull() }
            }

            test("failure - requestKeyIsNotNull") {
                val detail = shouldBeInvalidSingle { Request(mapOf()).validateKeyIsNotNull() }
                detail.path.fullName shouldBe "Request[key]"
                detail.message.content shouldBe "Value must not be null"
            }

            test("success - requestKeyIsNotNullAndMin3") {
                shouldBeValid { Request(mapOf("key" to "abc")).validateKeyIsNotNullAndMin3() }
            }

            test("failure - requestKeyIsNotNullAndMin3") {
                val detail = shouldBeInvalidSingle { Request(mapOf("key" to "ab")).validateKeyIsNotNullAndMin3() }
                detail.path.fullName shouldBe "Request[key]"
                detail.message.content shouldBe "\"ab\" must be at least 3 characters"
            }
        }
    })
