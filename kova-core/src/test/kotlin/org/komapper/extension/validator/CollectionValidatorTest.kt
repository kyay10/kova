package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

class CollectionValidatorTest :
    FunSpec({
        context("notEmpty") {
            test("success") {
                shouldBeValid { listOf("1").notEmpty() }
            }

            test("failure") {
                shouldBeInvalidSingle { emptyList<Nothing>().notEmpty() }
                    .message.content shouldBe "Collection [] must not be empty"
            }
        }

        context("length") {
            test("success") {
                shouldBeValid { listOf("1", "2").length(2) }
            }

            test("failure - too few elements") {
                shouldBeInvalidSingle { listOf("1").length(2) }
                    .message.content shouldBe "Collection [1] must have exactly 2 elements"
            }

            test("failure - too many elements") {
                shouldBeInvalidSingle { listOf("1", "2", "3").length(2) }
                    .message.content shouldBe "Collection [1, 2, 3] must have exactly 2 elements"

            }
        }

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun List<String>.validate() {
                accumulatingUnit { min(2) }
                min(3)
            }
            test("success") {
                shouldBeValid { listOf("1", "2", "3").validate() }
            }

            test("failure") {
                val details = shouldBeInvalid { listOf("1").validate() }
                details shouldHaveSize 2
                details[0].message.content shouldBe "Collection(size=1) must have at least 2 elements"
                details[1].message.content shouldBe "Collection(size=1) must have at least 3 elements"
            }
        }

        context("constrain") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun List<String>.validate() = constrain("test") { satisfies(size == 1) { "Constraint failed" } }

            test("success") {
                shouldBeValid { listOf("1").validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { listOf("1", "2").validate() }.message.content shouldBe "Constraint failed"
            }
        }

        context("onEach") {
            test("success") {
                shouldBeValid { listOf("123", "456") onEach { it length 3 } }
            }

            test("failure") {
                val details = shouldBeInvalid { listOf("123", "4567", "8910") onEach { it length 3 } }
                details shouldHaveSize 2
                val detail0 = details[0]
                detail0.root shouldBe ""
                detail0.path.fullName shouldBe "[1]<collection element>"
                detail0.message.id shouldBe "kova.string.length"
                detail0.message.content shouldBe "\"4567\" must be exactly 3 characters"
                val detail1 = details[1]
                detail1.root shouldBe ""
                detail1.path.fullName shouldBe "[2]<collection element>"
                detail1.message.id shouldBe "kova.string.length"
                detail1.message.content shouldBe "\"8910\" must be exactly 3 characters"
            }

            test("failure - failFast is true") {
                val detail = shouldBeInvalidSingle(ValidationConfig(failFast = true)) {
                    listOf("123", "4567", "8910") onEach { it length 3 }
                }
                detail.root shouldBe ""
                detail.path.fullName shouldBe "[1]<collection element>"
                detail.message.content shouldBe "\"4567\" must be exactly 3 characters"
            }
        }

        context("property") {
            data class ListHolder(val list: List<String>)

            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun ListHolder.validate() = checking { ::list { l -> l onEach { it length 3 } } }

            test("success") {
                shouldBeValid { ListHolder(listOf("123", "456")).validate() }
            }

            test("failure") {
                val detail = shouldBeInvalidSingle { ListHolder(listOf("123", "4567")).validate() }
                detail.root shouldEndWith "ListHolder"
                detail.path.fullName shouldBe "list[1]<collection element>"
                detail.message.content shouldBe "\"4567\" must be exactly 3 characters"
            }
        }
    })
