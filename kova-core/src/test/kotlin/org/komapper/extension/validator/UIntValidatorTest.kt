package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class UIntValidatorTest :
    FunSpec({

        context("plus") {
            val validator = (Kova.uInt().max(10u) + Kova.uInt().max(20u)).min(5u)

            test("success") {
                validator.tryValidate(8u).shouldBeRight().first shouldBe 8u
            }

            test("failure") {
                validator.tryValidate(15u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 15 must be less than or equal to 10"
                }
            }
        }

        context("or") {
            val validator = (Kova.uInt().max(10u) or Kova.uInt().max(20u)).min(5u)

            test("success : 10") {
                validator.tryValidate(10u).shouldBeRight().first shouldBe 10u
            }

            test("success : 20") {
                validator.tryValidate(20u).shouldBeRight().first shouldBe 20u
            }

            test("failure : 25") {
                validator.tryValidate(25u).shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                }
            }
        }

        context("constrain") {
            val validator =
                Kova.uInt().constrain("test") {
                    satisfies(it == 10u, Message.Text("Constraint failed"))
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

        context("min") {
            val validator = Kova.uInt().min(5u)

            test("success with value greater than threshold") {
                validator.tryValidate(6u).shouldBeRight().first shouldBe 6u
            }

            test("success with equal value") {
                validator.tryValidate(5u).shouldBeRight().first shouldBe 5u
            }

            test("failure with value less than threshold") {
                validator.tryValidate(4u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 4 must be greater than or equal to 5"
                }
            }
        }

        context("max") {
            val validator = Kova.uInt().max(10u)

            test("success with value less than threshold") {
                validator.tryValidate(9u).shouldBeRight().first shouldBe 9u
            }

            test("success with equal value") {
                validator.tryValidate(10u).shouldBeRight().first shouldBe 10u
            }

            test("failure with value greater than threshold") {
                validator.tryValidate(11u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 11 must be less than or equal to 10"
                }
            }
        }

        context("gt (greater than)") {
            val validator = Kova.uInt().gt(5u)

            test("success with value greater than threshold") {
                validator.tryValidate(6u).shouldBeRight().first shouldBe 6u
            }

            test("success with large value") {
                validator.tryValidate(100u).shouldBeRight().first shouldBe 100u
            }

            test("failure with equal value") {
                validator.tryValidate(5u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5 must be greater than 5"
                }
            }

            test("failure with value less than threshold") {
                validator.tryValidate(4u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 4 must be greater than 5"
                }
            }
        }

        context("gte (greater than or equal)") {
            val validator = Kova.uInt().gte(5u)

            test("success with value greater than threshold") {
                validator.tryValidate(6u).shouldBeRight().first shouldBe 6u
            }

            test("success with equal value") {
                validator.tryValidate(5u).shouldBeRight().first shouldBe 5u
            }

            test("failure with value less than threshold") {
                validator.tryValidate(4u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 4 must be greater than or equal to 5"
                }
            }
        }

        context("lt (less than)") {
            val validator = Kova.uInt().lt(5u)

            test("success with value less than threshold") {
                validator.tryValidate(4u).shouldBeRight().first shouldBe 4u
            }

            test("success with zero") {
                validator.tryValidate(0u).shouldBeRight().first shouldBe 0u
            }

            test("failure with equal value") {
                validator.tryValidate(5u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5 must be less than 5"
                }
            }

            test("failure with value greater than threshold") {
                validator.tryValidate(6u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 6 must be less than 5"
                }
            }
        }

        context("lte (less than or equal)") {
            val validator = Kova.uInt().lte(5u)

            test("success with value less than threshold") {
                validator.tryValidate(4u).shouldBeRight().first shouldBe 4u
            }

            test("success with equal value") {
                validator.tryValidate(5u).shouldBeRight().first shouldBe 5u
            }

            test("failure with value greater than threshold") {
                validator.tryValidate(6u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 6 must be less than or equal to 5"
                }
            }
        }

        context("uLong") {
            context("min") {
                val validator = Kova.uLong().min(5uL)

                test("success") {
                    validator.tryValidate(6uL).shouldBeRight().first shouldBe 6uL
                }

                test("failure") {
                    validator.tryValidate(4uL).shouldBeLeft().shouldBeSingleton {
                        it.message.content shouldBe "Number 4 must be greater than or equal to 5"
                    }
                }
            }

            context("max") {
                val validator = Kova.uLong().max(10uL)

                test("success") {
                    validator.tryValidate(9uL).shouldBeRight().first shouldBe 9uL
                }

                test("failure") {
                    validator.tryValidate(11uL).shouldBeLeft().shouldBeSingleton {
                        it.message.content shouldBe "Number 11 must be less than or equal to 10"
                    }
                }
            }
        }

        context("uByte") {
            context("min") {
                val validator = Kova.uByte().min(5u)

                test("success") {
                    validator.tryValidate(6u).shouldBeRight().first shouldBe 6.toUByte()
                }

                test("failure") {
                    validator.tryValidate(4u).shouldBeLeft().shouldBeSingleton {
                        it.message.content shouldBe "Number 4 must be greater than or equal to 5"
                    }
                }
            }

            context("max") {
                val validator = Kova.uByte().max(10u)

                test("success") {
                    validator.tryValidate(9u).shouldBeRight().first shouldBe 9.toUByte()
                }

                test("failure") {
                    validator.tryValidate(11u).shouldBeLeft().shouldBeSingleton {
                        it.message.content shouldBe "Number 11 must be less than or equal to 10"
                    }
                }
            }
        }

        context("uShort") {
            context("min") {
                val validator = Kova.uShort().min(5u)

                test("success") {
                    validator.tryValidate(6u).shouldBeRight().first shouldBe 6.toUShort()
                }

                test("failure") {
                    validator.tryValidate(4u).shouldBeLeft().shouldBeSingleton {
                        it.message.content shouldBe "Number 4 must be greater than or equal to 5"
                    }
                }
            }

            context("max") {
                val validator = Kova.uShort().max(10u)

                test("success") {
                    validator.tryValidate(9u).shouldBeRight().first shouldBe 9.toUShort()
                }

                test("failure") {
                    validator.tryValidate(11u).shouldBeLeft().shouldBeSingleton {
                        it.message.content shouldBe "Number 11 must be less than or equal to 10"
                    }
                }
            }
        }

        context("chaining multiple validators") {
            val validator =
                Kova
                    .uInt()
                    .min(5u)
                    .max(10u)
                    .gt(6u)
                    .lte(9u)

            test("success with value 7") {
                validator.tryValidate(7u).shouldBeRight().first shouldBe 7u
            }

            test("success with value 9") {
                validator.tryValidate(9u).shouldBeRight().first shouldBe 9u
            }

            test("failure with value 5") {
                validator.tryValidate(5u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5 must be greater than 6"
                }
            }

            test("failure with value 10") {
                validator.tryValidate(10u).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 10 must be less than or equal to 9"
                }
            }
        }
    })
