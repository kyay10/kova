package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NumberValidatorTest :
    FunSpec({

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int.validate() {
                accumulatingUnit { max(2) }
                accumulatingUnit { max(3) }
                negative()
            }
            test("success") {
                shouldBeValid { (-1).validate() }
            }

            test("failure") {
                val details = shouldBeInvalid { 5.validate() }
                details shouldHaveSize 3
                details[0].message.content shouldBe "Number 5 must be less than or equal to 2"
                details[1].message.content shouldBe "Number 5 must be less than or equal to 3"
                details[2].message.content shouldBe "Number 5 must be negative"
            }
        }

        context("or") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int.validate() {
                or { max(2) } or { max(3) } or Accum
                min(1)
            }
            test("success : 2") {
                shouldBeValid { 2.validate() }
            }
            test("success : 3") {
                shouldBeValid { 3.validate() }
            }

            test("failure : 4") {
                shouldBeInvalidSingle { 4.validate() }.message.id shouldBe "kova.or"
            }
        }

        context("constrain") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Int.validate() = satisfies(this == 10) { Message.Text("Constraint failed") }

            test("success") {
                shouldBeValid { 10.validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { 20.validate() }.message.content shouldBe "Constraint failed"
            }
        }

        context("positive") {
            test("success with positive number") {
                shouldBeValid { 1.positive() }
            }

            test("success with large positive number") {
                shouldBeValid { 100.positive() }
            }

            test("failure with zero") {
                shouldBeInvalidSingle { 0.positive() }.message.content shouldBe "Number 0 must be positive"
            }

            test("failure with negative number") {
                shouldBeInvalidSingle { (-1).positive() }.message.content shouldBe "Number -1 must be positive"
            }
        }

        context("positive with double") {
            test("success") {
                shouldBeValid { 0.1.positive() }
            }

            test("failure") {
                shouldBeInvalidSingle { (-0.1).positive() }.message.content shouldBe "Number -0.1 must be positive"
            }
        }

        context("negative") {
            test("success with negative number") {
                shouldBeValid { (-1).negative() }
            }

            test("success with large negative number") {
                shouldBeValid { (-100).negative() }
            }

            test("failure with zero") {
                shouldBeInvalidSingle { 0.negative() }.message.content shouldBe "Number 0 must be negative"
            }

            test("failure with positive number") {
                shouldBeInvalidSingle { 1.negative() }.message.content shouldBe "Number 1 must be negative"
            }
        }

        context("negative with double") {
            test("success") {
                shouldBeValid { (-0.1).negative() }
            }

            test("failure") {
                shouldBeInvalidSingle { (0.1).negative() }.message.content shouldBe "Number 0.1 must be negative"
            }
        }

        context("notPositive") {
            test("success with zero") {
                shouldBeValid { 0.notPositive() }
            }

            test("success with negative number") {
                shouldBeValid { (-1).notPositive() }
            }

            test("success with large negative number") {
                shouldBeValid { (-100).notPositive() }
            }

            test("failure with positive number") {
                shouldBeInvalidSingle { (1).notPositive() }.message.content shouldBe "Number 1 must not be positive"
            }
        }

        context("notPositive with double") {
            test("success with zero") {
                shouldBeValid { 0.0.notPositive() }
            }

            test("success with negative") {
                shouldBeValid { (-0.1).notPositive() }
            }

            test("failure with positive") {
                shouldBeInvalidSingle { 0.1.notPositive() }.message.content shouldBe "Number 0.1 must not be positive"
            }
        }

        context("gt (greater than)") {
            test("success with value greater than threshold") {
                shouldBeValid { 6 gt 5 }
            }

            test("success with large value") {
                shouldBeValid { 100 gt 5 }
            }

            test("failure with equal value") {
                shouldBeInvalidSingle { 5 gt 5 }.message.content shouldBe "Number 5 must be greater than 5"
            }

            test("failure with value less than threshold") {
                shouldBeInvalidSingle { 4 gt 5 }.message.content shouldBe "Number 4 must be greater than 5"
            }
        }

        context("gt with double") {
            test("success") {
                shouldBeValid { 5.6 gt 5.5 }
            }

            test("failure with equal value") {
                shouldBeInvalidSingle { 5.5 gt 5.5 }.message.content shouldBe "Number 5.5 must be greater than 5.5"
            }

            test("failure with smaller value") {
                shouldBeInvalidSingle { 5.4 gt 5.5 }.message.content shouldBe "Number 5.4 must be greater than 5.5"
            }
        }

        context("gte (greater than or equal)") {
            test("success with value greater than threshold") {
                shouldBeValid { 6 gte 5 }
            }

            test("success with equal value") {
                shouldBeValid { 5 gte 5 }
            }

            test("failure with value less than threshold") {
                shouldBeInvalidSingle { 4 gte 5 }.message.content shouldBe "Number 4 must be greater than or equal to 5"
            }
        }

        context("gte with double") {
            test("success with greater value") {
                shouldBeValid { 5.6 gte 5.5 }
            }

            test("success with equal value") {
                shouldBeValid { 5.5 gte 5.5 }
            }

            test("failure") {
                shouldBeInvalidSingle { 5.4 gte 5.5 }.message.content shouldBe "Number 5.4 must be greater than or equal to 5.5"
            }
        }

        context("lt (less than)") {
            test("success with value less than threshold") {
                shouldBeValid { 4 lt 5 }
            }

            test("success with large negative value") {
                shouldBeValid { (-100) lt 5 }
            }

            test("failure with equal value") {
                shouldBeInvalidSingle { 5 lt 5 }.message.content shouldBe "Number 5 must be less than 5"
            }

            test("failure with value greater than threshold") {
                shouldBeInvalidSingle { 6 lt 5 }.message.content shouldBe "Number 6 must be less than 5"
            }
        }

        context("lt with double") {
            test("success") {
                shouldBeValid { 5.4 lt 5.5 }
            }

            test("failure with equal value") {
                shouldBeInvalidSingle { 5.5 lt 5.5 }.message.content shouldBe "Number 5.5 must be less than 5.5"
            }

            test("failure with greater value") {
                shouldBeInvalidSingle { 5.6 lt 5.5 }.message.content shouldBe "Number 5.6 must be less than 5.5"
            }
        }

        context("lte (less than or equal)") {
            test("success with value less than threshold") {
                shouldBeValid { 4 lte 5 }
            }

            test("success with equal value") {
                shouldBeValid { 5 lte 5 }
            }

            test("failure with value greater than threshold") {
                shouldBeInvalidSingle { 6 lte 5 }.message.content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("lte with double") {
            test("success with smaller value") {
                shouldBeValid { 5.4 lte 5.5 }
            }

            test("success with equal value") {
                shouldBeValid { 5.5 lte 5.5 }
            }

            test("failure") {
                shouldBeInvalidSingle { 5.6 lte 5.5 }.message.content shouldBe "Number 5.6 must be less than or equal to 5.5"
            }
        }
    })
