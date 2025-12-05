package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class ComparableValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.uInt().max(2u) + Kova.uInt().max(3u)

            test("success") {
                validator.tryValidate(1u).shouldBeRight().first shouldBe 1u
            }

            test("failure") {
                val details = validator.tryValidate(5u).shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe "Number 5 must be less than or equal to 2"
                details[1].message.content shouldBe "Number 5 must be less than or equal to 3"
            }
        }

        context("constrain") {
            val validator =
                Kova.uInt().constrain("test") {
                    satisfies(it == 10u) { "Constraint failed" }
                }

            test("success") {
                validator.tryValidate(10u).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(20u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Constraint failed"
                }
            }
        }
    })
