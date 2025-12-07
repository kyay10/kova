package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComparableValidatorTest :
    FunSpec({

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun UInt.validate() {
                accumulatingUnit { max(2u) }
                max(3u)
            }

            test("success") {
                shouldBeValid { 1u.validate() }
            }

            test("failure") {
                val details = shouldBeInvalid { 5u.validate() }
                details shouldHaveSize 2
                details[0].message.content shouldBe "Number 5 must be less than or equal to 2"
                details[1].message.content shouldBe "Number 5 must be less than or equal to 3"
            }
        }

        context("constrain") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun UInt.validate() = constrain("test") { satisfies(this == 10u) { "Constraint failed" } }

            test("success") {
                shouldBeValid { 10u.validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { 20u.validate() }.message.content shouldBe "Constraint failed"
            }
        }
    })
