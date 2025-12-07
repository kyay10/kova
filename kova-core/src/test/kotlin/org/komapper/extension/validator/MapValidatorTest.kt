package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.context.RaiseAccumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapValidatorTest :
    FunSpec({

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Map<String, String>.validate() {
                accumulatingUnit { min(2) }
                min(3)
            }
            test("success") {
                shouldBeValid { mapOf("a" to "1", "b" to "2", "c" to "3").validate() }
            }

            test("failure") {
                val details = shouldBeInvalid { mapOf("a" to "1").validate() }
                details shouldHaveSize 2
                details[0].message.content shouldBe "Map(size=1) must have at least 2 entries"
                details[1].message.content shouldBe "Map(size=1) must have at least 3 entries"
            }
        }

        context("max") {
            test("success") {
                shouldBeValid { mapOf("a" to "1", "b" to "2").max(2) }
            }

            test("failure") {
                shouldBeInvalidSingle { mapOf("a" to "1", "b" to "2", "c" to "3").max(2) }
                    .message.content shouldBe "Map(size=3) must have at most 2 entries"
            }
        }

        context("notEmpty") {
            test("success") {
                shouldBeValid { mapOf("a" to "1").notEmpty() }
            }

            test("failure") {
                shouldBeInvalidSingle { emptyMap<String, String>().notEmpty() }
                    .message.content shouldBe "Map {} must not be empty"
            }
        }

        context("length") {
            test("success") {
                shouldBeValid { mapOf("a" to "1", "b" to "2").length(2) }
            }

            test("failure - too few") {
                shouldBeInvalidSingle { mapOf("a" to "1").length(2) }
                    .message.content shouldBe "Map {a=1} must have exactly 2 entries"
            }

            test("failure - too many") {
                shouldBeInvalidSingle { mapOf("a" to "1", "b" to "2", "c" to "3").length(2) }
                    .message.content shouldBe "Map {a=1, b=2, c=3} must have exactly 2 entries"
            }
        }

        context("constrain") {
            test("success") {
                shouldBeValid { satisfies(mapOf("a" to "1").size == 1) { "Constraint failed" } }
            }

            test("failure") {
                shouldBeInvalidSingle {
                    satisfies(mapOf("a" to "1", "b" to "2").size == 1) { "Constraint failed" }
                }.message.content shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Map<String, String>.validate() =
                onEach { satisfies(it.key != it.value) { "Constraint failed: ${it.key}" } }

            test("success") {
                shouldBeValid { mapOf("a" to "1", "b" to "2").validate() }
            }

            test("failure") {
                val details = shouldBeInvalid { mapOf("a" to "a", "b" to "b").validate() }
                details[0].message.content shouldBe "Constraint failed: a"
                details[1].message.content shouldBe "Constraint failed: b"
            }
        }

        context("onEachKey") {
            test("success") {
                shouldBeValid { mapOf("a" to "1", "b" to "2").onEachKey { it length 1 } }
            }

            test("failure") {
                val details = shouldBeInvalid { mapOf("a" to "1", "bb" to "2", "ccc" to "3").onEachKey { it length 1 } }
                details shouldHaveSize 2
                val detail0 = details[0]
                detail0.root shouldBe ""
                detail0.path.fullName shouldBe "<map key>"
                detail0.message.content shouldBe "\"bb\" must be exactly 1 characters"
                val detail1 = details[1]
                detail1.root shouldBe ""
                detail1.path.fullName shouldBe "<map key>"
                detail1.message.content shouldBe "\"ccc\" must be exactly 1 characters"
            }
        }

        context("onEachValue") {
            test("success") {
                shouldBeValid { mapOf("a" to "1", "b" to "2").onEachValue { it length 1 } }
            }

            test("failure") {
                val details =
                    shouldBeInvalid { mapOf("a" to "1", "b" to "22", "c" to "333").onEachValue { it length 1 } }
                details shouldHaveSize 2
                val detail0 = details[0]
                detail0.root shouldBe ""
                detail0.path.fullName shouldBe "[b]<map value>"
                detail0.message.content shouldBe "\"22\" must be exactly 1 characters"
                val detail1 = details[1]
                detail1.root shouldBe ""
                detail1.path.fullName shouldBe "[c]<map value>"
                detail1.message.content shouldBe "\"333\" must be exactly 1 characters"
            }
        }
    })
