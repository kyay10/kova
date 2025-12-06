package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class NumberValidatorTest :
    FunSpec({

        context("plus") {
            val validator = (Kova.int().max(2) + Kova.int().max(3)).negative()

            test("success") {
                validator.tryValidate(-1).shouldBeRight()
            }

            test("failure") {
                val details = validator.tryValidate(5).shouldBeLeft()
                details.size shouldBe 3
                details[0].message.content shouldBe "Number 5 must be less than or equal to 2"
                details[1].message.content shouldBe "Number 5 must be less than or equal to 3"
                details[2].message.content shouldBe "Number 5 must be negative"
            }
        }

        context("or") {
            val validator = (Kova.int().max(2) or Kova.int().max(3)).min(1)

            test("success : 2") {
                validator.tryValidate(2).shouldBeRight()
            }
            test("success : 3") {
                validator.tryValidate(3).shouldBeRight()
            }

            test("failure : 4") {
                validator.tryValidate(4).shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                }
            }
        }

        context("constrain") {
            val validator =
                Kova.int().constrain("test") {
                    satisfies(it == 10) { Message.Text("Constraint failed") }
                }

            test("success") {
                validator.tryValidate(10).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(20).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Constraint failed"
                }
            }
        }

        context("positive") {
            val validator = Kova.int().positive()

            test("success with positive number") {
                validator.tryValidate(1).shouldBeRight()
            }

            test("success with large positive number") {
                validator.tryValidate(100).shouldBeRight()
            }

            test("failure with zero") {
                validator.tryValidate(0).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 0 must be positive"
                }
            }

            test("failure with negative number") {
                validator.tryValidate(-1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number -1 must be positive"
                }
            }
        }

        context("positive with double") {
            val validator = Kova.double().positive()

            test("success") {
                validator.tryValidate(0.1).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(-0.1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number -0.1 must be positive"
                }
            }
        }

        context("negative") {
            val validator = Kova.int().negative()

            test("success with negative number") {
                validator.tryValidate(-1).shouldBeRight()
            }

            test("success with large negative number") {
                validator.tryValidate(-100).shouldBeRight()
            }

            test("failure with zero") {
                validator.tryValidate(0).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 0 must be negative"
                }
            }

            test("failure with positive number") {
                validator.tryValidate(1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 1 must be negative"
                }
            }
        }

        context("negative with double") {
            val validator = Kova.double().negative()

            test("success") {
                validator.tryValidate(-0.1).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(0.1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 0.1 must be negative"
                }
            }
        }

        context("notPositive") {
            val validator = Kova.int().notPositive()

            test("success with zero") {
                validator.tryValidate(0).shouldBeRight()
            }

            test("success with negative number") {
                validator.tryValidate(-1).shouldBeRight()
            }

            test("success with large negative number") {
                validator.tryValidate(-100).shouldBeRight()
            }

            test("failure with positive number") {
                validator.tryValidate(1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 1 must not be positive"
                }
            }
        }

        context("notPositive with double") {
            val validator = Kova.double().notPositive()

            test("success with zero") {
                validator.tryValidate(0.0).shouldBeRight()
            }

            test("success with negative") {
                validator.tryValidate(-0.1).shouldBeRight()
            }

            test("failure with positive") {
                validator.tryValidate(0.1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 0.1 must not be positive"
                }
            }
        }

        context("gt (greater than)") {
            val validator = Kova.int().gt(5)

            test("success with value greater than threshold") {
                validator.tryValidate(6).shouldBeRight()
            }

            test("success with large value") {
                validator.tryValidate(100).shouldBeRight()
            }

            test("failure with equal value") {
                validator.tryValidate(5).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5 must be greater than 5"
                }
            }

            test("failure with value less than threshold") {
                validator.tryValidate(4).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 4 must be greater than 5"
                }
            }
        }

        context("gt with double") {
            val validator = Kova.double().gt(5.5)

            test("success") {
                validator.tryValidate(5.6).shouldBeRight()
            }

            test("failure with equal value") {
                validator.tryValidate(5.5).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5.5 must be greater than 5.5"
                }
            }

            test("failure with smaller value") {
                validator.tryValidate(5.4).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5.4 must be greater than 5.5"
                }
            }
        }

        context("gte (greater than or equal)") {
            val validator = Kova.int().gte(5)

            test("success with value greater than threshold") {
                validator.tryValidate(6).shouldBeRight()
            }

            test("success with equal value") {
                validator.tryValidate(5).shouldBeRight()
            }

            test("failure with value less than threshold") {
                validator.tryValidate(4).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 4 must be greater than or equal to 5"
                }
            }
        }

        context("gte with double") {
            val validator = Kova.double().gte(5.5)

            test("success with greater value") {
                validator.tryValidate(5.6).shouldBeRight()
            }

            test("success with equal value") {
                validator.tryValidate(5.5).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(5.4).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5.4 must be greater than or equal to 5.5"
                }
            }
        }

        context("lt (less than)") {
            val validator = Kova.int().lt(5)

            test("success with value less than threshold") {
                validator.tryValidate(4).shouldBeRight()
            }

            test("success with large negative value") {
                validator.tryValidate(-100).shouldBeRight()
            }

            test("failure with equal value") {
                validator.tryValidate(5).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5 must be less than 5"
                }
            }

            test("failure with value greater than threshold") {
                validator.tryValidate(6).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 6 must be less than 5"
                }
            }
        }

        context("lt with double") {
            val validator = Kova.double().lt(5.5)

            test("success") {
                validator.tryValidate(5.4).shouldBeRight()
            }

            test("failure with equal value") {
                validator.tryValidate(5.5).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5.5 must be less than 5.5"
                }
            }

            test("failure with greater value") {
                validator.tryValidate(5.6).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5.6 must be less than 5.5"
                }
            }
        }

        context("lte (less than or equal)") {
            val validator = Kova.int().lte(5)

            test("success with value less than threshold") {
                validator.tryValidate(4).shouldBeRight()
            }

            test("success with equal value") {
                validator.tryValidate(5).shouldBeRight()
            }

            test("failure with value greater than threshold") {
                validator.tryValidate(6).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 6 must be less than or equal to 5"
                }
            }
        }

        context("lte with double") {
            val validator = Kova.double().lte(5.5)

            test("success with smaller value") {
                validator.tryValidate(5.4).shouldBeRight()
            }

            test("success with equal value") {
                validator.tryValidate(5.5).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(5.6).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5.6 must be less than or equal to 5.5"
                }
            }
        }
    })
