package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.context.Raise
import arrow.core.raise.context.RaiseAccumulate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("UNNECESSARY_SAFE_CALL", "KotlinUnreachableCode")
class NullableValidatorTest :
    FunSpec({
        context("isNull") {
            test("success") {
                shouldBeValid { null.isNull() }
            }

            test("failure") {
                shouldBeInvalidSingle { 4.isNull() }.message.content shouldBe "Value 4 must be null"
            }

            test("failure - min constraint violated") {
                shouldBeInvalidSingle { 2.isNull() }.message.content shouldBe "Value 2 must be null"
            }
        }

        context("isNull or nullable") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Int?.isNullOrMin3Max3() = isNullOr {
                accumulatingUnit { min(3) }
                max(3)
            }

            test("success - null") {
                shouldBeValid { null.isNullOrMin3Max3() }
            }

            test("success - 3") {
                shouldBeValid { 3.isNullOrMin3Max3() }
            }

            test("failure") {
                val message = shouldBeInvalidSingle { 5.isNullOrMin3Max3() }.message
                message.id shouldBe "kova.or"
                message.content shouldBe
                    "at least one constraint must be satisfied: [[Value 5 must be null], [Number 5 must be less than or equal to 3]]"
            }
        }

        context("isNull or then") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun Int?.isNullOrMin3OrMin5AndThenMax4() {
                isNullOrAccumulate { or { min(3) } or { min(5) } or Fail }
                this?.max(4)
            }

            test("success - isNull constraint satisfied") {
                shouldBeValid { null.isNullOrMin3OrMin5AndThenMax4() }
            }

            test("success - min3 constraint satisfied") {
                shouldBeValid { 3.isNullOrMin3OrMin5AndThenMax4() }
            }

            test("success - max4 constraint failed") {
                shouldBeInvalidSingle { 5.isNullOrMin3OrMin5AndThenMax4() }.message.content shouldBe "Number 5 must be less than or equal to 4"
            }

            test("failure - all constraints violated") {
                shouldBeInvalidSingle { 2.isNullOrMin3OrMin5AndThenMax4() }.message.id shouldBe "kova.or"
            }
        }

        context("and") {
            test("success - non-null") {
                shouldBeValid { 4?.min(3) }
            }

            test("success - null") {
                shouldBeValid { null?.min(3) }
            }

            test("failure - min 3constraint violated") {
                shouldBeInvalidSingle { 2?.min(3) }.message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("and - each List element") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
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
            test("success - non-null") {
                shouldBeValid { 4.notNull() min 3 }
            }

            test("failure - null") {
                shouldBeInvalidSingle { null.notNull() min 3 }.message.content shouldBe "Value must not be null"
            }

            test("failure - min3 constraint is violated") {
                shouldBeInvalidSingle { 2.notNull() min 3 }.message.content shouldBe "Number 2 must be greater than or equal to 3"
            }
        }

        context("toNonNullable - then") {
            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Int?.notNullAndMin3AndMax3() {
                notNull()
                min(3)
                max(5)
            }

            test("success") {
                shouldBeValid { 4.notNullAndMin3AndMax3() }
            }

            test("failure - notNull constraint is violated") {
                shouldBeInvalidSingle { null.notNullAndMin3AndMax3() }.message.content shouldBe "Value must not be null"
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
            fun Int?.isNullOrMin3Max3() = isNullOr {
                accumulatingUnit { min(3) }
                max(3)
            }

            test("success: 3") {
                shouldBeValid { 3.isNullOrMin3Max3() }
            }

            test("success: null") {
                shouldBeValid { null.isNullOrMin3Max3() }
            }
        }
    })
