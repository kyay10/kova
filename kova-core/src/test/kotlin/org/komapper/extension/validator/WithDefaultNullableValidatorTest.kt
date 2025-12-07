package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WithDefaultNullableValidatorTest :
    FunSpec({

        context("elvis") {
            test("success - null") {
                shouldBeValid { null ?: 0 } shouldBe 0
            }

            test("success - non null") {
                shouldBeValid { 123 ?: 0 } shouldBe 123
            }
        }

        context("isNull") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.myIsNull(): Int {
                isNull()
                return this ?: 0
            }
            test("success") {
                shouldBeValid { null.myIsNull() } shouldBe 0
            }

            test("failure") {
                shouldBeInvalidSingle { 4.myIsNull() }.message.content shouldBe "Value 4 must be null"
            }

            test("failure - min constraint violated") {
                shouldBeInvalidSingle { 2.myIsNull() }.message.content shouldBe "Value 2 must be null"
            }
        }

        context("isNull or nullable") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.isNullOrMin3Max3(): Int {
                isNullOr {
                    accumulatingUnit { min(3) }
                    max(3)
                }
                return this ?: 0
            }

            test("success - null") {
                shouldBeValid { null.isNullOrMin3Max3() } shouldBe 0
            }

            test("success - 3") {
                shouldBeValid { 3.isNullOrMin3Max3() } shouldBe 3
            }

            test("failure") {
                val message = shouldBeInvalidSingle { 5.isNullOrMin3Max3() }.message
                message.id shouldBe "kova.or"
                message.content shouldBe
                    "at least one constraint must be satisfied: [[Value 5 must be null], [Number 5 must be less than or equal to 3]]"
            }
        }

        context("isNull or nonNullable") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.isNullOrMin3Max3(): Int {
                isNullOr {
                    accumulatingUnit { min(3) }
                    max(3)
                }
                return this ?: 0
            }

            test("success - null") {
                shouldBeValid { null.isNullOrMin3Max3() } shouldBe 0
            }

            test("success - non-null") {
                shouldBeValid { 3.isNullOrMin3Max3() } shouldBe 3
            }

            test("failure - isNull and max3 constraints violated") {
                shouldBeInvalidSingle { 5.isNullOrMin3Max3() }.message.id shouldBe "kova.or"
            }
        }

        context("isNull or then") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.isNullOrMin3OrMin5AndThenMax4(): Int {
                isNullOr { or { min(3) } or { min(5) } or Fail }
                return (this ?: 0).also { it max 4 }
            }

            test("success - isNull constraint satisfied") {
                shouldBeValid { null.isNullOrMin3OrMin5AndThenMax4() } shouldBe 0
            }

            test("success - min3 constraint satisfied") {
                shouldBeValid { 3.isNullOrMin3OrMin5AndThenMax4() } shouldBe 3
            }

            test("success - max4 constraint failed") {
                shouldBeInvalidSingle { 5.isNullOrMin3OrMin5AndThenMax4() }
                    .message.content shouldBe "Number 5 must be less than or equal to 4"
            }

            test("failure - all constraints violated") {
                shouldBeInvalidSingle { 2.isNullOrMin3OrMin5AndThenMax4() }
                    .message.id shouldBe "kova.or"
            }
        }

        context("and") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.whenNotNullMin3() = this?.also { min(3) } ?: 0

            test("success - non-null") {
                shouldBeValid { 4.whenNotNullMin3() } shouldBe 4
            }

            test("success - null") {
                shouldBeValid { null.whenNotNullMin3() } shouldBe 0
            }

            test("failure - min 3constraint violated") {
                shouldBeInvalidSingle { 2.whenNotNullMin3() }
                    .message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("and - each List element") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun List<Int?>.onEachNullableMin3() = onEach { it?.min(3) }

            test("success - non-null") {
                shouldBeValid { listOf(4, 5).onEachNullableMin3() }
            }

            test("success - null") {
                shouldBeValid { listOf(null, null).onEachNullableMin3() }
            }

            test("failure - min3　constraint violated") {
                shouldBeInvalidSingle {
                    listOf(2, null).onEachNullableMin3()
                }.message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("toNonNullable") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.nullableMin3() = this?.also { min(3) } ?: 0

            test("success - non-null") {
                val value: Int = shouldBeValid { 4.nullableMin3() } // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("success - null") {
                shouldBeValid { null.nullableMin3() } shouldBe 0
            }

            test("failure - min3 constraint is violated") {
                shouldBeInvalidSingle { 2.nullableMin3() }
                    .message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("toNonNullable - then") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.notNullAndMin3AndMax3() = (this ?: 4).also {
                it min 3
                it max 5
            }

            test("success") {
                shouldBeValid { 4.notNullAndMin3AndMax3() } shouldBe 4
            }

            test("success - null") {
                shouldBeValid { null.notNullAndMin3AndMax3() } shouldBe 4
            }

            test("failure - min3 constraint is violated") {
                shouldBeInvalidSingle { 2.notNullAndMin3AndMax3() }.message.content shouldBe "Number 2 must be greater than or equal to 3"
            }

            test("failure - max5 constraint violated") {
                shouldBeInvalidSingle { 6.notNullAndMin3AndMax3() }.message.content shouldBe "Number 6 must be less than or equal to 5"
            }
        }

        context("logs") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.isNullOrMin3Max3(): Int {
                isNullOr { min(3) }
                return this ?: 0
            }

            test("success: 3") {
                shouldBeValid { 3.isNullOrMin3Max3() } shouldBe 3
            }

            test("success: null") {
                shouldBeValid { null.isNullOrMin3Max3() } shouldBe 0
            }
        }
    })
