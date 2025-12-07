package org.komapper.extension.validator.pbt

import arrow.core.raise.Accumulate
import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.FailureDetail
import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.onEachKey
import org.komapper.extension.validator.onEachValue
import org.komapper.extension.validator.shouldBeInvalid
import org.komapper.extension.validator.shouldBeValid

class KovaMapTest :
    FunSpec({

        test("min - boundary cases") {
            checkAll(Arb.Companion.int(0..100)) { minSize ->
                // Test exactly at boundary
                val atBoundary = (0 until minSize).associate { "key$it" to "value$it" }
                shouldBeValid { atBoundary min minSize }

                // Test one below boundary (if possible)
                if (minSize > 0) {
                    val belowBoundary = (0 until minSize - 1).associate { "key$it" to "value$it" }
                    shouldBeInvalid { belowBoundary min minSize }
                }

                // Test above boundary
                val aboveBoundary = (0 until minSize + 1).associate { "key$it" to "value$it" }
                shouldBeValid { aboveBoundary min minSize }
            }
        }

        test("min - empty map") {
            shouldBeInvalid { emptyMap<String, String>() min 1 }
        }

        test("min - zero size should always pass") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(),
                    minSize = 0,
                    maxSize = 10,
                ),
            ) { map -> shouldBeValid { map min 0 } }
        }

        test("onEachKey - all keys valid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(1..10),
                    Arb.Companion.string(),
                    minSize = 0,
                    maxSize = 20,
                ),
            ) { map ->
                shouldBeValid { map onEachKey { it min 1 } }
            }
        }

        test("onEachKey - all keys invalid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(0..0),
                    Arb.Companion.string(),
                    minSize = 1,
                    maxSize = 20,
                ),
            ) { map ->
                // All keys are empty (length 0)
                shouldBeInvalid { map onEachKey { it min 1 } }
            }
        }

        test("onEachKey - mixed valid and invalid keys") {
            val mixedMap = mapOf("valid" to "v1", "" to "v2", "also valid" to "v3", "" to "v4")
            shouldBeInvalid { mixedMap onEachKey { it min 1 } }
        }

        test("onEachKey - empty map should pass") {
            shouldBeValid { emptyMap<String, String>() onEachKey { it min 1 } }
        }

        test("onEachValue - all values valid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(1..10),
                    minSize = 0,
                    maxSize = 20,
                ),
            ) { map -> shouldBeValid { map onEachValue { it min 1 } } }
        }

        test("onEachValue - all values invalid") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(0..0),
                    minSize = 1,
                    maxSize = 20,
                ),
            ) { map ->
                // All values are empty (length 0)
                shouldBeInvalid { map onEachValue { it min 1 } }
            }
        }

        test("onEachValue - mixed valid and invalid values") {
            val mixedMap = mapOf("k1" to "valid", "k2" to "", "k3" to "also valid", "k4" to "")
            shouldBeInvalid { mixedMap onEachValue { it min 1 } }
        }

        test("onEachValue - empty map should pass") {
            shouldBeValid { emptyMap<String, String>() onEachValue { it min 1 } }
        }

        test("onEachValue - with complex validator") {
            checkAll(
                Arb.Companion.map(
                    Arb.Companion.string(),
                    Arb.Companion.string(5..20),
                    minSize = 0,
                    maxSize = 10,
                ),
            ) { map ->
                // All values have length 5-20, so they all satisfy min(3).max(25)
                shouldBeValid {
                    map onEachValue {
                        it min 3
                        it max 25
                    }
                }
            }
        }

        test("onEachKey and onEachValue together - both constraints satisfied") {
            val validMap = mapOf("hello" to "world", "foo" to "bar", "test" to "data")
            shouldBeValid {
                validMap onEachKey { it min 1 }
                validMap onEachValue { it min 1 }
            }
        }

        test("onEachKey and onEachValue together - onEachKey fails") {
            val invalidKeyMap = mapOf("hello" to "world", "" to "bar", "test" to "data")
            shouldBeInvalid {
                invalidKeyMap onEachKey { it min 1 }
                invalidKeyMap onEachValue { it min 1 }
            }
        }

        test("onEachKey and onEachValue together - onEachValue fails") {
            val invalidValueMap = mapOf("hello" to "world", "foo" to "", "test" to "data")
            shouldBeInvalid {
                invalidValueMap onEachKey { it min 1 }
                invalidValueMap onEachValue { it min 1 }
            }
        }

        test("onEachKey and onEachValue together - both fail") {
            val bothInvalidMap = mapOf("hello" to "world", "" to "", "test" to "data")
            shouldBeInvalid {
                bothInvalidMap onEachKey { it min 1 }
                bothInvalidMap onEachValue { it min 1 }
            }
        }

        test("min and onEachValue together - both constraints satisfied") {
            val validMap = mapOf("k1" to "hello", "k2" to "world", "k3" to "test")
            shouldBeValid {
                validMap min 2
                validMap onEachValue { it min 1 }
            }
        }

        test("min and onEachValue together - min fails") {
            val tooSmallMap = mapOf("k1" to "hello")
            shouldBeInvalid {
                tooSmallMap min 2
                tooSmallMap onEachValue { it min 1 }
            }
        }

        test("min and onEachValue together - onEachValue fails") {
            val invalidValuesMap = mapOf("k1" to "hello", "k2" to "", "k3" to "world")
            shouldBeInvalid {
                invalidValuesMap min 2
                invalidValuesMap onEachValue { it min 1 }
            }
        }

        test("min and onEachValue together - both fail") {
            val singleInvalidValue = mapOf("k1" to "")
            shouldBeInvalid {
                singleInvalidValue min 2
                singleInvalidValue onEachValue { it min 1 }
            }
        }

        test("validator composition with plus operator") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Map<String, String>.combined() {
                min(2)
                onEachValue { it min 3 }
            }

            // Should pass both constraints
            val validMap = mapOf("k1" to "hello", "k2" to "world")
            shouldBeValid { validMap.combined() }

            // Should fail min constraint
            val tooSmallMap = mapOf("k1" to "hello")
            shouldBeInvalid { tooSmallMap.combined() }

            // Should fail onEachValue constraint
            val invalidValues = mapOf("k1" to "hello", "k2" to "ab")
            shouldBeInvalid { invalidValues.combined() }
        }

        test("onEachValue with nested map validators") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Map<String, Map<String, Int>>.outerValidator() = onEachValue { it min 1 }

            // All inner maps have at least 1 entry
            val validNestedMap =
                mapOf(
                    "map1" to mapOf("a" to 1, "b" to 2),
                    "map2" to mapOf("c" to 3),
                    "map3" to mapOf("d" to 4, "e" to 5, "f" to 6),
                )
            shouldBeValid { validNestedMap.outerValidator() }

            // One inner map is empty
            val invalidNestedMap =
                mapOf(
                    "map1" to mapOf("a" to 1, "b" to 2),
                    "map2" to emptyMap(),
                    "map3" to mapOf("d" to 4, "e" to 5),
                )
            shouldBeInvalid { invalidNestedMap.outerValidator() }
        }
    })
