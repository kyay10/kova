package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class MapEntryValidatorTest :
    FunSpec({
        context("constrain") {
            val validator =
                Kova.mapEntry<String, String>().constrain("test") {
                    satisfies(it.key != it.value, "Constraint failed: ${it.key}")
                }

            test("success") {
                validator.tryValidate(mapOf("a" to "1").entries.first()).shouldBeRight()
            }

            test("failure") {
                validator.tryValidate(mapOf("a" to "a").entries.first()).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Constraint failed: a"
                }
            }
        }
    })
