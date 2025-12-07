package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConditionalValidatorTest :
    FunSpec({
        context("onlyIf") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int.validate() {
                if (this % 2 == 0) min(3)
            }

            test("success") {
                shouldBeValid { 1.validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { 2.validate() }.message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("onlyIf and plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int.validate() {
                accumulatingUnit { if (this % 2 == 0) min(3) }
                min(1)
            }

            test("success - plus") {
                shouldBeValid { 1.validate() }
            }

            test("failure - plus") {
                val details = shouldBeInvalid { 0.validate() }
                details shouldHaveSize 2
                details[0].message.content shouldBe "Number 0 must be greater than or equal to 3"
                details[1].message.content shouldBe "Number 0 must be greater than or equal to 1"
            }
        }
    })
