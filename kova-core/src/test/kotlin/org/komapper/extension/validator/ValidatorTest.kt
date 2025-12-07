package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ValidatorTest :
    FunSpec({

        context("validate") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Int.validate() {
                min(1)
                max(10)
            }

            test("success") {
                shouldBeValid { 5.validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { 0.validate() }.message.content shouldBe "Number 0 must be greater than or equal to 1"
            }
        }

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int.validate() {
                accumulatingUnit { max(2) }
                max(3)
            }

            test("success") {
                shouldBeValid { 1.validate() }
            }
            test("failure") {
                shouldBeInvalid { 4.validate() }
            }
        }

        context("or: 2") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun String.length2or5() = or { length(2) } or { length(5) } or Fail

            test("success - length(2)") {
                shouldBeValid { "ab".length2or5() }
            }
            test("success - length(5)") {
                shouldBeValid { "abcde".length2or5() }
            }
            test("failure - length(3)") {
                val message = shouldBeInvalidSingle { "abc".length2or5() }.message
                message.id shouldBe "kova.or"
                message.content shouldBe
                    "at least one constraint must be satisfied: [[\"abc\" must be exactly 2 characters], [\"abc\" must be exactly 5 characters]]"
            }
        }

        context("or: 3") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun String.length2or5or7() = or { length(2) } or { length(5) } or { length(7) } or Fail

            test("failure - length(3)") {
                val message = shouldBeInvalidSingle { "abc".length2or5or7() }.message
                message.id shouldBe "kova.or"
                message.content shouldBe "at least one constraint must be satisfied: [[[\"abc\" must be exactly 2 characters], " +
                    "[\"abc\" must be exactly 5 characters]], [\"abc\" must be exactly 7 characters]]"
            }
        }

        context("map") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Int.validate() = also { min(1) } * 2

            test("success") {
                shouldBeValid { 2.validate() } shouldBe 4
            }
            test("failure") {
                shouldBeInvalidSingle { (-1).validate() }.message.content shouldBe "Number -1 must be greater than or equal to 1"
            }
        }

        context("compose") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Int.validate(): String {
                min(3)
                return toString().also { it max 1 }
            }

            test("success") {
                shouldBeValid { 3.validate() } shouldBe "3"
            }
            test("failure - first constraint violated") {
                shouldBeInvalidSingle { 2.validate() }.message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
            test("failure - second constraint violated") {
                shouldBeInvalidSingle { 10.validate() }.message.content shouldBe "\"10\" must be at most 1 characters"
            }
        }
    })
