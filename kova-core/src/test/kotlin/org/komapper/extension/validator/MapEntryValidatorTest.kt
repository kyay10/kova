package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapEntryValidatorTest :
    FunSpec({
        context("constrain") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Map.Entry<String, String>.validate() = constrain("test") {
                satisfies(key != value) { "Constraint failed: $key" }
            }

            test("success") {
                shouldBeValid { mapOf("a" to "1").entries.first().validate() }
            }

            test("failure") {
                shouldBeInvalidSingle {
                    mapOf("a" to "a").entries.first().validate()
                }.message.content shouldBe "Constraint failed: a"
            }
        }
    })
