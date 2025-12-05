package org.komapper.extension.validator

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class NullableValidatorTest :
    FunSpec({

        context("nullable") {
            val nullable = Kova.nullable<Int>()

            test("success - null") {
                nullable.tryValidate(null).shouldBeRight().first shouldBe null
            }

            test("success - non null") {
                nullable.tryValidate(123).shouldBeRight().first shouldBe 123
            }
        }

        context("withDefault - literal") {
            val nullable = Kova.nullable<Int>().withDefault(0)

            test("success - null") {
                nullable.tryValidate(null).shouldBeRight().first shouldBe 0
            }

            test("success - non null") {
                nullable.tryValidate(123).shouldBeRight().first shouldBe 123
            }
        }

        context("withDefault - lambda") {
            val nullable = Kova.nullable<Int>().withDefault { 0 }

            test("success - null") {
                nullable.tryValidate(null).shouldBeRight().first shouldBe 0
            }

            test("success - non null") {
                nullable.tryValidate(123).shouldBeRight().first shouldBe 123
            }
        }

        context("isNull") {
            val isNull = Kova.nullable<Int>().isNull()

            test("success") {
                isNull.tryValidate(null).shouldBeRight()
            }

            test("failure") {
                isNull.tryValidate(4).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value 4 must be null"
                }
            }

            test("failure - min constraint violated") {
                isNull.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value 2 must be null"
                }
            }
        }

        context("isNull or nullable") {
            val isNull = Kova.nullable<Int>().isNull()
            val isNullOrMin3Max3 =
                isNull.or(
                    Kova
                        .int()
                        .min(3)
                        .max(3)
                        .asNullable(),
                )

            test("success - null") {
                isNullOrMin3Max3.tryValidate(null).shouldBeRight()
            }

            test("success - 3") {
                isNullOrMin3Max3.tryValidate(3).shouldBeRight()
            }

            test("failure") {
                isNullOrMin3Max3.tryValidate(5).shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                    it.message.content shouldBe
                        "at least one constraint must be satisfied: [[Value 5 must be null], [Number 5 must be less than or equal to 3]]"
                }
            }
        }

        context("isNull or nonNullable") {
            val min3 = Kova.int().min(3)
            val max3 = Kova.int().max(3)
            val isNullOrMin3Max3 = Kova.nullable<Int>().isNull().or(min3 and max3)

            test("success - null") {
                isNullOrMin3Max3.tryValidate(null).shouldBeRight()
            }

            test("success - non-null") {
                isNullOrMin3Max3.tryValidate(3).shouldBeRight()
            }

            test("failure - isNull and max3 constraints violated") {
                isNullOrMin3Max3.tryValidate(5).shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                }
            }
        }

        context("isNull or then") {
            val min3 = Kova.int().min(3)
            val min5 = Kova.int().min(5)
            val max4 = Kova.int().max(4)
            val isNullOrMin3OrMin5AndThenMax4 =
                Kova
                    .int()
                    .asNullable()
                    .isNull()
                    .or(min3 or min5)
                    .then(max4)

            test("success - isNull constraint satisfied") {
                isNullOrMin3OrMin5AndThenMax4.tryValidate(null).shouldBeRight()
            }

            test("success - min3 constraint satisfied") {
                isNullOrMin3OrMin5AndThenMax4.tryValidate(3).shouldBeRight()
            }

            test("success - max4 constraint failed") {
                isNullOrMin3OrMin5AndThenMax4.tryValidate(5).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 5 must be less than or equal to 4"
                }
            }

            test("failure - all constraints violated") {
                isNullOrMin3OrMin5AndThenMax4.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                }
            }
        }

        context("and") {
            val min3 = Kova.int().min(3)
            val whenNotNullMin3 = Kova.nullable<Int>().and(min3)

            test("success - non-null") {
                whenNotNullMin3.tryValidate(4).shouldBeRight()
            }

            test("success - null") {
                whenNotNullMin3.tryValidate(null).shouldBeRight()
            }

            test("failure - min 3constraint violated") {
                whenNotNullMin3.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }
        }

        context("and - each List element") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = Kova.nullable<Int>().and(min3)
            val onEachNullableMin3 = Kova.list<Int?>().onEach(nullableMin3)

            test("success - non-null") {
                onEachNullableMin3.tryValidate(listOf(4, 5)).shouldBeRight()
            }

            test("success - null") {
                onEachNullableMin3.tryValidate(listOf(null, null)).shouldBeRight()
            }

            test("failure - min3　constraint violated") {
                onEachNullableMin3.tryValidate(listOf(2, null)).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }
        }

        context("toNonNullable") {
            val min3 = Kova.int().min(3)
            val nullableMin3 = min3.asNullable().toNonNullable()

            test("success - non-null") {
                val value: Int =
                    nullableMin3.tryValidate(4).shouldBeRight().first // The type is "Int" instead of "Int?"
                value shouldBe 4
            }

            test("failure - null") {
                nullableMin3.tryValidate(null).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value must not be null"
                }
            }

            test("failure - min3 constraint is violated") {
                nullableMin3.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }
        }

        context("toNonNullable - then") {
            val max5 = Kova.int().max(5)
            val min3 = Kova.int().min(3)
            val notNullAndMin3AndMax3 = Kova.nullable<Int>().toNonNullable().then(min3 and max5)

            test("success") {
                notNullAndMin3AndMax3.tryValidate(4).shouldBeRight()
            }

            test("failure - notNull constraint is violated") {
                notNullAndMin3AndMax3.tryValidate(null).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Value must not be null"
                }
            }

            test("failure - min3 constraint is violated") {
                notNullAndMin3AndMax3.tryValidate(2).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 2 must be greater than or equal to 3"
                }
            }

            test("failure - max5 constraint violated") {
                notNullAndMin3AndMax3.tryValidate(6).shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Number 6 must be less than or equal to 5"
                }
            }
        }

        context("logs") {
            val min3 = Kova.int().min(3)
            val isNullOrMin3Max3 = Kova.nullable<Int>().isNull().or(min3)

            test("success: 3") {
                isNullOrMin3Max3.tryValidate(3).shouldBeRight().second.logs.joinToString("\n").also(::println)
            }

            test("success: null") {
                isNullOrMin3Max3.tryValidate(null).shouldBeRight().second.logs.joinToString("\n").also(::println)
            }
        }
    })
