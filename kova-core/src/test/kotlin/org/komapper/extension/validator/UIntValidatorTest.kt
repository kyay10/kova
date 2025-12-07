package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UIntValidatorTest :
    FunSpec({

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun UInt.validate() {
                accumulatingUnit { max(10u) }
                accumulatingUnit { max(20u) }
                min(5u)
            }

            test("success") {
                shouldBeValid { 8u.validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { 15u.validate() }
                    .message.content shouldBe "Number 15 must be less than or equal to 10"
            }
        }

        context("or") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun UInt.validate() {
                or { max(10u) } or { max(20u) } or Accumulate
                min(5u)
            }

            test("success : 10") {
                shouldBeValid { 10u.validate() }
            }

            test("success : 20") {
                shouldBeValid { 20u.validate() }
            }

            test("failure : 25") {
                shouldBeInvalidSingle { 25u.validate() }.message.id shouldBe "kova.or"
            }
        }

        context("constrain") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun UInt.validate() = constrain("test") {
                satisfies(this == 10u) { Message.Text("Constraint failed") }
            }

            test("success") {
                shouldBeValid { 10u.validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { 20u.validate() }.message.content shouldBe "Constraint failed"
            }
        }

        context("min") {
            test("success with value greater than threshold") {
                shouldBeValid { 6u min 5u }
            }

            test("success with equal value") {
                shouldBeValid { 5u min 5u }
            }

            test("failure with value less than threshold") {
                shouldBeInvalidSingle { 4u min 5u }.message.content shouldBe "Number 4 must be greater than or equal to 5"
            }
        }

        context("max") {
            test("success with value less than threshold") {
                shouldBeValid { 9u max 10u }
            }

            test("success with equal value") {
                shouldBeValid { 10u max 10u }
            }

            test("failure with value greater than threshold") {
                shouldBeInvalidSingle { 11u max 10u }.message.content shouldBe "Number 11 must be less than or equal to 10"
            }
        }

        context("gt (greater than)") {
            test("success with value greater than threshold") {
                shouldBeValid { 6u gt 5u }
            }

            test("success with large value") {
                shouldBeValid { 100u gt 5u }
            }

            test("failure with equal value") {
                shouldBeInvalidSingle { 5u gt 5u }.message.content shouldBe "Number 5 must be greater than 5"
            }

            test("failure with value less than threshold") {
                shouldBeInvalidSingle { 4u gt 5u }.message.content shouldBe "Number 4 must be greater than 5"
            }
        }

        context("gte (greater than or equal)") {
            test("success with value greater than threshold") {
                shouldBeValid { 6u gte 5u }
            }

            test("success with equal value") {
                shouldBeValid { 5u gte 5u }
            }

            test("failure with value less than threshold") {
                shouldBeInvalidSingle { 4u gte 5u }.message.content shouldBe "Number 4 must be greater than or equal to 5"
            }
        }

        context("lt (less than)") {
            test("success with value less than threshold") {
                shouldBeValid { 4u lt 5u }
            }

            test("success with zero") {
                shouldBeValid { 0u lt 5u }
            }

            test("failure with equal value") {
                shouldBeInvalidSingle { 5u lt 5u }.message.content shouldBe "Number 5 must be less than 5"
            }

            test("failure with value greater than threshold") {
                shouldBeInvalidSingle { 6u lt 5u }.message.content shouldBe "Number 6 must be less than 5"
            }
        }

        context("lte (less than or equal)") {
            test("success with value less than threshold") {
                shouldBeValid { 4u lte 5u }
            }

            test("success with equal value") {
                shouldBeValid { 5u lte 5u }
            }

            test("failure with value greater than threshold") {
                shouldBeInvalidSingle { 6u lte 5u }.message.content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("uLong") {
            context("min") {
                test("success") {
                    shouldBeValid { 6uL min 5uL }
                }

                test("failure") {
                    shouldBeInvalidSingle { 4uL min 5uL }
                        .message.content shouldBe "Number 4 must be greater than or equal to 5"
                }
            }

            context("max") {
                test("success") {
                    shouldBeValid { 9uL max 10uL }
                }

                test("failure") {
                    shouldBeInvalidSingle { 11uL max 10uL }
                        .message.content shouldBe "Number 11 must be less than or equal to 10"
                }
            }
        }

        context("uByte") {
            context("min") {
                test("success") {
                    shouldBeValid { 6u min 5u }
                }

                test("failure") {
                    shouldBeInvalidSingle { 4u min 5u }.message.content shouldBe "Number 4 must be greater than or equal to 5"
                }
            }

            context("max") {
                test("success") {
                    shouldBeValid { 9u max 10u }
                }

                test("failure") {
                    shouldBeInvalidSingle { 11u max 10u }.message.content shouldBe "Number 11 must be less than or equal to 10"
                }
            }
        }

        context("uShort") {
            context("min") {
                test("success") {
                    shouldBeValid { 6u min 5u }
                }

                test("failure") {
                    shouldBeInvalidSingle { 4u min 5u }.message.content shouldBe "Number 4 must be greater than or equal to 5"
                }
            }

            context("max") {
                test("success") {
                    shouldBeValid { 9u max 10u }
                }

                test("failure") {
                    shouldBeInvalidSingle { 11u max 10u }
                        .message.content shouldBe "Number 11 must be less than or equal to 10"
                }
            }
        }

        context("chaining multiple validators") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun UInt.validate() {
                accumulatingUnit { min(5u) }
                accumulatingUnit { max(10u) }
                accumulatingUnit { gt(6u) }
                lte(9u)
            }

            test("success with value 7") {
                shouldBeValid { 7u.validate() }
            }

            test("success with value 9") {
                shouldBeValid { 9u.validate() }
            }

            test("failure with value 5") {
                shouldBeInvalidSingle { 5u.validate() }.message.content shouldBe "Number 5 must be greater than 6"
            }

            test("failure with value 10") {
                shouldBeInvalidSingle { 10u.validate() }.message.content shouldBe "Number 10 must be less than or equal to 9"
            }
        }
    })
