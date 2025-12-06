package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith

class CollectionValidatorTest :
    FunSpec({
        context("notEmpty") {
            val validator = Kova.list<String>().notEmpty()

            test("success") {
                validator.tryValidate(listOf("1")).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(emptyList()).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Collection [] must not be empty"
                }
            }
        }

        context("length") {
            val validator = Kova.list<String>().length(2)

            test("success") {
                validator.tryValidate(listOf("1", "2")).shouldBeRight()
            }

            test("failure - too few elements") {
                validator.tryValidate(listOf("1")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Collection [1] must have exactly 2 elements"
                }
            }

            test("failure - too many elements") {
                validator.tryValidate(listOf("1", "2", "3")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Collection [1, 2, 3] must have exactly 2 elements"
                }
            }
        }

        context("plus") {
            val validator = Kova.list<String>().min(2).min(3)

            test("success") {
                validator.tryValidate(listOf("1", "2", "3")).shouldBeRight()
            }

            test("failure") {
                val details = validator.tryValidate(listOf("1")).shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe "Collection(size=1) must have at least 2 elements"
                details[1].message.content shouldBe "Collection(size=1) must have at least 3 elements"
            }
        }

        context("constrain") {
            val validator = Kova.list<String>().constrain("test") {
                satisfies(it.size == 1) { "Constraint failed" }
            }

            test("success") {
                validator.tryValidate(listOf("1")).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(listOf("1", "2")).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Constraint failed"
                }
            }
        }

        context("onEach") {
            val validator = Kova.list<String>().onEach(Kova.string().length(3))

            test("success") {
                validator.tryValidate(listOf("123", "456")).shouldBeRight()
            }

            test("failure") {
                val details = validator.tryValidate(listOf("123", "4567", "8910")).shouldBeLeft()
                details.size shouldBe 2
                details[0].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "[1]<collection element>"
                    it.message.id shouldBe "kova.string.length"
                    it.message.content shouldBe "\"4567\" must be exactly 3 characters"
                }
                details[1].let {
                    it.root shouldBe ""
                    it.path.fullName shouldBe "[2]<collection element>"
                    it.message.id shouldBe "kova.string.length"
                    it.message.content shouldBe "\"8910\" must be exactly 3 characters"
                }
            }

            test("failure - failFast is true") {
                validator.tryValidate(listOf("123", "4567", "8910"), ValidationConfig(failFast = true)).shouldBeLeft()
                    .shouldBeSingleton {
                        it.root shouldBe ""
                        it.path.fullName shouldBe "[1]<collection element>"
                        it.message.content shouldBe "\"4567\" must be exactly 3 characters"
                    }
            }
        }

        context("property") {
            data class ListHolder(
                val list: List<String>,
            )

            val schema = object : ObjectSchema<ListHolder>() {
                val list = ListHolder::list { Kova.list<String>().onEach(Kova.string().length(3)) }
            }

            test("success") {
                schema.tryValidate(ListHolder(listOf("123", "456"))).shouldBeRight()
            }

            test("failure") {
                schema.tryValidate(ListHolder(listOf("123", "4567"))).shouldBeLeft().shouldBeSingleton {
                    it.root shouldEndWith "ListHolder"
                    it.path.fullName shouldBe "list[1]<collection element>"
                    it.message.content shouldBe "\"4567\" must be exactly 3 characters"
                }
            }
        }
    })
