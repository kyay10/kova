package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LiteralValidatorTest :
    FunSpec({

        context("literal - boolean") {

            test("success") {
                shouldBeValid { true literal true }
            }

            test("failure") {
                shouldBeInvalidSingle { false literal true }.message.content shouldBe "Value false must be true"
            }
        }

        context("literal - int") {
            test("success") {
                shouldBeValid { 123 literal 123 }
            }

            test("failure") {
                shouldBeInvalidSingle { 456 literal 123 }.message.content shouldBe "Value 456 must be 123"
            }
        }

        context("literal - string") {
            test("success") {
                shouldBeValid { "abc" literal "abc" }
            }

            test("failure") {
                shouldBeInvalidSingle { "de" literal "abc" }.message.content shouldBe "Value de must be abc"
            }
        }

        context("literals - string list") {
            test("success") {
                shouldBeValid { "bbb" literal listOf("aaa", "bbb", "ccc") }
            }

            test("failure") {
                shouldBeInvalidSingle { "ddd" literal listOf("aaa", "bbb", "ccc") }
                    .message.content shouldBe "Value ddd must be one of [aaa, bbb, ccc]"
            }
        }
    })
