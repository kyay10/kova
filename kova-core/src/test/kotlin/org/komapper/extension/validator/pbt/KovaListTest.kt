package org.komapper.extension.validator.pbt

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.komapper.extension.validator.FailureDetail
import org.komapper.extension.validator.ValidationContext
import org.komapper.extension.validator.accumulatingUnit
import org.komapper.extension.validator.max
import org.komapper.extension.validator.min
import org.komapper.extension.validator.onEach
import org.komapper.extension.validator.shouldBeInvalid
import org.komapper.extension.validator.shouldBeValid

class KovaListTest :
    FunSpec({

        test("min - boundary cases") {
            checkAll(Arb.Companion.int(0..100)) { minSize ->
                // Test exactly at boundary
                val atBoundary = List(minSize) { "element$it" }
                shouldBeValid { atBoundary min minSize }

                // Test one below boundary (if possible)
                if (minSize > 0) {
                    val belowBoundary = List(minSize - 1) { "element$it" }
                    shouldBeInvalid { belowBoundary min minSize }
                }

                // Test above boundary
                val aboveBoundary = List(minSize + 1) { "element$it" }
                shouldBeValid { aboveBoundary min minSize }
            }
        }

        test("min - empty list") {
            shouldBeInvalid { emptyList<Nothing>() min 1 }
        }

        test("min - zero size should always pass") {
            checkAll(Arb.Companion.list(Arb.Companion.string(), 0..10)) { list ->
                shouldBeValid { list min 0 }
            }
        }

        test("onEach - all elements valid") {
            checkAll(Arb.Companion.list(Arb.Companion.string(1..10), 0..20)) { list ->
                shouldBeValid { list onEach { it min 1 } }
            }
        }

        test("onEach - all elements invalid") {
            checkAll(Arb.Companion.list(Arb.Companion.string(0..0), 1..20)) { list ->
                // All strings are empty (length 0)
                shouldBeInvalid { list onEach { it min 1 } }
            }
        }

        test("onEach - mixed valid and invalid elements") {
            val mixedList = listOf("valid", "", "also valid", "")
            shouldBeInvalid { mixedList onEach { it min 1 } }
        }

        test("onEach - empty list should pass") {
            shouldBeValid { emptyList<String>() onEach { it min 1 } }
        }

        test("onEach - with complex validator") {
            checkAll(Arb.Companion.list(Arb.Companion.string(5..20), 0..10)) { list ->
                // All strings have length 5-20, so they all satisfy min(3).max(25)
                shouldBeValid {
                    list onEach {
                        it min 3
                        it max 25
                    }
                }
            }
        }

        test("min and onEach together - both constraints satisfied") {
            val validList = listOf("hello", "world", "test")
            shouldBeValid {
                validList min 2
                validList onEach { it min 1 }
            }
        }

        test("min and onEach together - min fails") {
            val tooSmallList = listOf("hello")
            shouldBeInvalid {
                tooSmallList min 2
                tooSmallList onEach { it min 1 }
            }
        }

        test("min and onEach together - onEach fails") {
            val invalidElementsList = listOf("hello", "", "world")
            shouldBeInvalid {
                invalidElementsList min 2
                invalidElementsList onEach { it min 1 }
            }
        }

        test("min and onEach together - both fail") {
            val singleInvalidElement = listOf("")
            shouldBeInvalid {
                singleInvalidElement min 2
                singleInvalidElement onEach { it min 1 }
            }
        }

        test("validator composition with plus operator") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun List<String>.combined() {
                accumulatingUnit { min(2) }
                onEach { it min 3 }
            }

            // Should pass both constraints
            val validList = listOf("hello", "world")
            shouldBeValid { validList.combined() }

            // Should fail min constraint
            val tooSmallList = listOf("hello")
            shouldBeInvalid { tooSmallList.combined() }

            // Should fail onEach constraint
            val invalidElements = listOf("hello", "ab")
            shouldBeInvalid { invalidElements.combined() }
        }

        test("onEach with nested list validators") {
            // All inner lists have at least 1 element
            val validNestedList = listOf(listOf(1, 2), listOf(3), listOf(4, 5, 6))
            shouldBeValid { validNestedList onEach { it min 1 } }

            // One inner list is empty
            val invalidNestedList = listOf(listOf(1, 2), emptyList(), listOf(4, 5))
            shouldBeInvalid { invalidNestedList onEach { it min 1 } }
        }
    })
