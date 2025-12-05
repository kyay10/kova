package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class MapValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.map<String, String>().min(2).min(3)

            test("success") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3")).shouldBeRight().first shouldBe
                    mapOf(
                        "a" to "1",
                        "b" to "2",
                        "c" to "3",
                    )
            }

            test("failure") {
                val details = validator.tryValidate(mapOf("a" to "1")).shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe "Map(size=1) must have at least 2 entries"
                details[1].message.content shouldBe "Map(size=1) must have at least 3 entries"
            }
        }

        context("max") {
            val validator = Kova.map<String, String>().max(2)

            test("success") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2")).shouldBeRight().first shouldBe mapOf(
                    "a" to "1",
                    "b" to "2"
                )
            }

            test("failure") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Map(size=3) must have at most 2 entries"
                }
            }
        }

        context("notEmpty") {
            val validator = Kova.map<String, String>().notEmpty()

            test("success") {
                validator.tryValidate(mapOf("a" to "1")).shouldBeRight().first shouldBe mapOf("a" to "1")
            }

            test("failure") {
                validator.tryValidate(emptyMap()).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Map {} must not be empty"
                }
            }
        }

        context("length") {
            val validator = Kova.map<String, String>().length(2)

            test("success") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2")).shouldBeRight().first shouldBe mapOf(
                    "a" to "1",
                    "b" to "2"
                )
            }

            test("failure - too few") {
                validator.tryValidate(mapOf("a" to "1")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Map {a=1} must have exactly 2 entries"
                }
            }

            test("failure - too many") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2", "c" to "3")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Map {a=1, b=2, c=3} must have exactly 2 entries"
                }
            }
        }

        context("constrain") {
            val validator =
                Kova.map<String, String>().constrain("test") {
                    satisfies(it.size == 1) { "Constraint failed" }
                }

            test("success") {
                validator.tryValidate(mapOf("a" to "1")).shouldBeRight().first shouldBe mapOf("a" to "1")
            }

            test("failure") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Constraint failed"
                }
            }
        }

        context("onEach") {
            val validator =
                Kova.map<String, String>().onEach(
                    Kova.mapEntry<String, String>().constrain("test") {
                        satisfies(it.key != it.value) { "Constraint failed: ${it.key}" }
                    },
                )

            test("success") {
                validator.tryValidate(mapOf("a" to "1", "b" to "1")).shouldBeRight().first shouldBe mapOf(
                    "a" to "1",
                    "b" to "1"
                )
            }

            test("failure") {
                val details = validator.tryValidate(mapOf("a" to "a", "b" to "b")).shouldBeLeft()
                details[0].message.content shouldBe "Constraint failed: a"
                details[1].message.content shouldBe "Constraint failed: b"
            }
        }

        context("onEachKey") {
            val validator = Kova.map<String, String>().onEachKey(Kova.string().length(1))

            test("success") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2")).shouldBeRight().first shouldBe mapOf(
                    "a" to "1",
                    "b" to "2"
                )
            }

            test("failure") {
                val details = validator.tryValidate(mapOf("a" to "1", "bb" to "2", "ccc" to "3")).shouldBeLeft()
                details.size shouldBe 2
                details[0].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "<map key>"
                    it.message.content shouldBe "\"bb\" must be exactly 1 characters"
                }
                details[1].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "<map key>"
                    it.message.content shouldBe "\"ccc\" must be exactly 1 characters"
                }
            }
        }

        context("onEachValue") {
            val validator = Kova.map<String, String>().onEachValue(Kova.string().length(1))

            test("success") {
                validator.tryValidate(mapOf("a" to "1", "b" to "2")).shouldBeRight().first shouldBe mapOf(
                    "a" to "1",
                    "b" to "2"
                )
            }

            test("failure") {
                val details = validator.tryValidate(mapOf("a" to "1", "b" to "22", "c" to "333")).shouldBeLeft()
                details.size shouldBe 2
                details[0].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "[b]<map value>"
                    it.message.content shouldBe "\"22\" must be exactly 1 characters"
                }
                details[1].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "[c]<map value>"
                    it.message.content shouldBe "\"333\" must be exactly 1 characters"
                }
            }
        }
    })
