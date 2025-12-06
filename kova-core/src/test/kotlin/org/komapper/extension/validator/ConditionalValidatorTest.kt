package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class ConditionalValidatorTest :
    FunSpec({
        context("onlyIf") {
            val validator = Kova.int().min(3).onlyIf { it % 2 == 0 }

            test("success") {
                validator.tryValidate(1).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }
        }

        context("onlyIf and plus") {
            val validator = Kova.int().min(3).onlyIf { it % 2 == 0 } + Kova.int().min(1)

            test("success - plus") {
                validator.tryValidate(1).shouldBeRight()
            }

            test("failure - plus") {
                val details = validator.tryValidate(0).shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe "Number 0 must be greater than or equal to 3"
                details[1].message.content shouldBe "Number 0 must be greater than or equal to 1"
            }
        }
    })
