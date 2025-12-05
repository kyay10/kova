package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class ValidatorTest :
    FunSpec({

        context("validate") {
            val validator = Kova.int().min(1).max(10)

            test("success") {
                val result = validator.validate(5)
                result shouldBe 5
            }

            test("failure") {
                val ex =
                    shouldThrow<ValidationException> {
                        validator.validate(0)
                    }
                ex.messages.size shouldBe 1
                ex.messages[0].content shouldBe "Number 0 must be greater than or equal to 1"
            }
        }

        context("plus") {
            val a = Kova.int().max(2)
            val b = Kova.int().max(3)
            val c = a + b

            test("success") {
                c.tryValidate(1).shouldBeRight()
            }
            test("failure") {
                c.tryValidate(4).shouldBeLeft()
            }
        }

        context("or: 2") {
            val length2 = Kova.string().length(2)
            val length5 = Kova.string().length(5)
            val length2or5 = length2 or length5

            test("success - length(2)") {
                length2or5.tryValidate("ab").shouldBeRight()
            }
            test("success - length(5)") {
                length2or5.tryValidate("abcde").shouldBeRight()
            }
            test("failure - length(3)") {
                length2or5.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                    it.message.content shouldBe
                        "at least one constraint must be satisfied: [[\"abc\" must be exactly 2 characters], [\"abc\" must be exactly 5 characters]]"

                }
            }
        }

        context("or: 3") {
            val length2 = Kova.string().length(2)
            val length5 = Kova.string().length(5)
            val length7 = Kova.string().length(7)
            val length2or5or7 = length2 or length5 or length7

            test("failure - length(3)") {
                length2or5or7.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                    it.message.content shouldBe "at least one constraint must be satisfied: [[[\"abc\" must be exactly 2 characters], " +
                        "[\"abc\" must be exactly 5 characters]], [\"abc\" must be exactly 7 characters]]"
                }
            }
        }

        context("map") {
            val validator = Kova.int().min(1).map { it * 2 }
            test("success") {
                validator.tryValidate(2).shouldBeRight().first shouldBe 4
            }
            test("failure") {
                validator.tryValidate(-1).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number -1 must be greater than or equal to 1"
                }
            }
        }

        context("compose") {
            val validator = Kova.string().max(1).compose(Kova.int().min(3).map { it.toString() })
            test("success") {
                validator.tryValidate(3).shouldBeRight().first shouldBe "3"
            }
            test("failure - first constraint violated") {
                validator.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }
            test("failure - second constraint violated") {
                validator.tryValidate(10).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"10\" must be at most 1 characters"
                }
            }
        }

        context("andThen") {
            val validator =
                Kova
                    .int()
                    .min(3)
                    .map { it.toString() }
                    .then(Kova.string().max(1))
            test("success") {
                validator.tryValidate(3).shouldBeRight().first shouldBe "3"
            }
            test("failure - first constraint violated") {
                validator.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }
            test("failure - second constraint violated") {
                validator.tryValidate(10).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"10\" must be at most 1 characters"
                }
            }
        }

        // TODO
        xcontext("logs") {
            val validator =
                Kova
                    .string()
                    .trim()
                    .min(3)
                    .max(5)

            test("success") {
                val (value, context) = validator.tryValidate(" abcde ", ValidationConfig(logging = true))
                    .shouldBeRight()
                value shouldBe "abcde"
                context.logs shouldBe
                    listOf(
                        "StringValidator(name=kova.string.max)",
                        "Validator.chain",
                        "Validator.map",
                        "StringValidator(name=kova.string.min)",
                        "Validator.chain",
                        "Validator.map",
                        "StringValidator(name=trim)",
                        "Validator.chain",
                        "Validator.map",
                        "StringValidator(name=empty)",
                        "Validator.chain",
                        "Validator.map",
                        "EmptyValidator",
                        "ConstraintValidator(name=kova.satisfied)",
                        "ConstraintValidator(name=kova.satisfied)",
                        "ConstraintValidator(name=kova.string.min)",
                        "ConstraintValidator(name=kova.string.max)",
                    )
            }
        }
    })
