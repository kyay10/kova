package org.komapper.extension.validator

import arrow.core.raise.context.Raise
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapEntryValidatorTest :
    FunSpec({
        context("constrain") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Map.Entry<String, String>.validate() = satisfies(key != value) { "Constraint failed: $key" }

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
