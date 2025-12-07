package org.komapper.extension.validator.pbt

import arrow.core.raise.context.Raise
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.FailureDetail
import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.endsWith
import org.komapper.extension.validator.length
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.notBlank
import org.komapper.extension.validator.shouldBeInvalid
import org.komapper.extension.validator.shouldBeValid
import org.komapper.extension.validator.startsWith

class KovaStringTest :
    FunSpec({

        test("min - boundary cases") {
            checkAll(Arb.int(0..100)) { minLength ->
                // Test exactly at boundary
                val atBoundary = "a".repeat(minLength)
                shouldBeValid { atBoundary min minLength }

                // Test one below boundary (if possible)
                if (minLength > 0) {
                    val belowBoundary = "a".repeat(minLength - 1)
                    shouldBeInvalid { belowBoundary min minLength }
                }

                // Test above boundary
                val aboveBoundary = "a".repeat(minLength + 1)
                shouldBeValid { aboveBoundary min minLength }
            }
        }

        test("max - boundary cases") {
            checkAll(Arb.int(0..100)) { maxLength ->
                // Test exactly at boundary
                val atBoundary = "a".repeat(maxLength)
                shouldBeValid { atBoundary max maxLength }

                // Test one above boundary
                val aboveBoundary = "a".repeat(maxLength + 1)
                shouldBeInvalid { aboveBoundary max maxLength }

                // Test below boundary
                if (maxLength > 0) {
                    val belowBoundary = "a".repeat(maxLength - 1)
                    shouldBeValid { belowBoundary max maxLength }
                }
            }
        }

        test("min and max together") {
            checkAll(Arb.int(0..50), Arb.int(0..50)) { min, max ->
                if (min <= max) {
                    context(_: ValidationContext, _: Raise<FailureDetail>)
                    fun String.validate() {
                        min(min)
                        max(max)
                    }

                    // Test valid range - middle
                    val validLength = (min + max) / 2
                    shouldBeValid { "a".repeat(validLength).validate() }

                    // Test valid range - at min boundary
                    shouldBeValid { "a".repeat(min).validate() }

                    // Test valid range - at max boundary
                    shouldBeValid { "a".repeat(max).validate() }

                    // Test below range
                    if (min > 0) {
                        shouldBeInvalid { "a".repeat(min - 1).validate() }
                    }

                    // Test above range
                    shouldBeInvalid { "a".repeat(max + 1).validate() }
                }
            }
        }

        test("notBlank - should pass for non-blank strings") {
            checkAll(Arb.string(1..100).filter { it.isNotBlank() }) { input ->
                shouldBeValid { input.notBlank() }
            }
        }

        test("notBlank - should fail for blank strings") {
            val blankStrings =
                Arb.choice(
                    Arb.constant(""),
                    Arb.int(1..10).map { " ".repeat(it) },
                    Arb.int(1..10).map { "\t".repeat(it) },
                    Arb.int(1..10).map { "\n".repeat(it) },
                    Arb.int(1..10).map { " \t\n".repeat(it) },
                )

            checkAll(blankStrings) { input -> shouldBeInvalid { input.notBlank() } }
        }

        test("length - should pass when string has exact length") {
            checkAll(Arb.int(0..100)) { targetLength ->
                val input = "a".repeat(targetLength)
                shouldBeValid { input length targetLength }
            }
        }

        test("length - should fail when string has different length") {
            checkAll(Arb.int(0..100), Arb.int(0..100)) { actualLength, targetLength ->
                if (actualLength != targetLength) {
                    val input = "a".repeat(actualLength)
                    shouldBeInvalid { input length targetLength }
                }
            }
        }

        test("startsWith - should pass when string starts with prefix") {
            checkAll(Arb.string(0..50), Arb.string(0..50)) { prefix, suffix ->
                val input = prefix + suffix
                shouldBeValid { input startsWith prefix }
            }
        }

        test("startsWith - should fail when string doesn't start with prefix") {
            checkAll(Arb.string(1..10), Arb.string(1..10).filter { it.isNotEmpty() }) { prefix, input ->
                if (!input.startsWith(prefix)) shouldBeInvalid { input startsWith prefix }
            }
        }

        test("endsWith - should pass when string ends with suffix") {
            checkAll(Arb.string(0..50), Arb.string(0..50)) { prefix, suffix ->
                val input = prefix + suffix
                shouldBeValid { input endsWith suffix }
            }
        }

        test("endsWith - should fail when string doesn't end with suffix") {
            checkAll(Arb.string(1..10), Arb.string(1..10).filter { it.isNotEmpty() }) { suffix, input ->
                if (!input.endsWith(suffix)) shouldBeInvalid { input endsWith suffix }
            }
        }
    })
