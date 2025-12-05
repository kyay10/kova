package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class LiteralValidatorTest :
    FunSpec({

        context("literal - boolean") {
            val validator = Kova.literal(true)

            test("success") {
                validator.tryValidate(true).shouldBeRight().first.shouldBeTrue()
            }

            test("failure") {
                validator.tryValidate(false).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value false must be true"
                }
            }
        }

        context("literal - int") {
            val validator = Kova.literal(123)

            test("success") {
                validator.tryValidate(123).shouldBeRight().first shouldBe 123
            }

            test("failure") {
                validator.tryValidate(456).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value 456 must be 123"
                }
            }
        }

        context("literal - string") {
            val validator = Kova.literal("abc")

            test("success") {
                validator.tryValidate("abc").shouldBeRight().first shouldBe "abc"
            }

            test("failure") {
                validator.tryValidate("de").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value de must be abc"
                }
            }
        }

        context("literals - string vararg") {
            val validator = Kova.literal("aaa", "bbb", "ccc")

            test("success") {
                validator.tryValidate("bbb").shouldBeRight().first shouldBe "bbb"
            }

            test("failure") {
                validator.tryValidate("ddd").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value ddd must be one of [aaa, bbb, ccc]"
                }
            }
        }

        context("literals - string list") {
            val validator = Kova.literal(listOf("aaa", "bbb", "ccc"))

            test("success") {
                validator.tryValidate("bbb").shouldBeRight().first shouldBe "bbb"
            }

            test("failure") {
                validator.tryValidate("ddd").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value ddd must be one of [aaa, bbb, ccc]"
                }
            }
        }
    })
