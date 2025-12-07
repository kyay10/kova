package org.komapper.extension.validator

import arrow.core.raise.Accumulate
import arrow.core.raise.context.Raise
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

@Suppress("UNNECESSARY_SAFE_CALL")
class ObjectSchemaTest :
    FunSpec({

        data class User(
            val id: Int,
            val name: String,
        )

        data class Street(
            val id: Int,
            val name: String,
        )

        data class Address(
            val id: Int,
            val street: Street,
            val country: String = "US",
            val postalCode: String = "",
        )

        data class Employee(
            val id: Int,
            val name: String,
            val address: Address,
        )

        data class Person(
            val id: Int,
            val firstName: String?,
            val lastName: String?,
            val address: Address?,
        )

        context("plus") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun User.validate() = checking {
                ::name {
                    it.min(1)
                    it.max(10)
                }
                ::id { it.min(1) }
            }

            test("success") {
                val user = User(1, "abc")
                shouldBeValid { user.validate() }
            }

            test("failure - 1 rule violated") {
                val user = User(2, "too-long-name")
                val detail = shouldBeInvalidSingle { user.validate() }
                detail.root shouldBe "User"
                detail.path.fullName shouldBe "name"
                detail.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
            }

            test("failure - 2 rules violated") {
                val user = User(0, "too-long-name")
                val details = shouldBeInvalid { user.validate() }
                details shouldHaveSize 2

                val detail0 = details[0]
                detail0.root shouldBe "User"
                detail0.path.fullName shouldBe "name"
                detail0.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
                val detail1 = details[1]
                detail1.root shouldBe "User"
                detail1.path.fullName shouldBe "id"
                detail1.message.content shouldBe "Number 0 must be greater than or equal to 1"
            }
        }
        context("constrain") {
            data class Period(val startDate: LocalDate, val endDate: LocalDate)

            context(_: ValidationContext, _: Raise<FailureDetail>)
            fun Period.validate() = checking {
                satisfies(this.startDate <= this.endDate) { "startDate must be less than or equal to endDate" }
            }

            test("success") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1))
                shouldBeValid { period.validate() }
            }

            test("failure") {
                val period = Period(LocalDate.of(2020, 1, 1), LocalDate.of(2019, 1, 1))
                val detail = shouldBeInvalidSingle { period.validate() }
                detail.root shouldBe "Period"
                detail.path.fullName shouldBe ""
                detail.message.content shouldBe "startDate must be less than or equal to endDate"
            }
        }

        context("nullable") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun User.validate() = checking {
                ::id { it.min(1) }
                ::name {
                    it.min(1)
                    it.max(10)
                }
            }

            test("success - non null") {
                val user = User(1, "abc")
                shouldBeValid { user?.validate() }
            }

            test("success - null") {
                shouldBeValid { null?.validate() }
            }
        }

        context("prop - simple") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun User.validate() = checking {
                ::id { it.min(1) }
                ::name {
                    it.min(1)
                    it.max(10)
                }
            }

            test("success") {
                shouldBeValid { User(1, "abc").validate() }
            }

            test("failure - 1 constraint violated") {
                val detail = shouldBeInvalidSingle { User(2, "too-long-name").validate() }
                detail.root shouldBe "User"
                detail.path.fullName shouldBe "name"
                detail.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
            }

            test("failure - 2 constraints violated") {
                val details = shouldBeInvalid { User(0, "too-long-name").validate() }
                details shouldHaveSize 2

                val detail0 = details[0]
                detail0.root shouldBe "User"
                detail0.path.fullName shouldBe "id"
                detail0.message.content shouldBe "Number 0 must be greater than or equal to 1"
                val detail1 = details[1]
                detail1.root shouldBe "User"
                detail1.path.fullName shouldBe "name"
                detail1.message.content shouldBe "\"too-long-name\" must be at most 10 characters"
            }
        }

        context("prop - nest") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Street.validate() = checking {
                ::id { it.min(1) }
                ::name {
                    it.min(3)
                    it.max(5)
                }
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Address.validate() = checking { ::street { it.validate() } }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Employee.validate() = checking { ::address { it.validate() } }

            test("success") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def")))
                shouldBeValid { employee.validate() }
            }

            test("failure") {
                val employee = Employee(1, "abc", Address(1, Street(1, "too-long-name")))
                val detail = shouldBeInvalidSingle { employee.validate() }
                detail.root shouldBe "Employee"
                detail.path.fullName shouldBe "address.street.name"
                detail.message.content shouldBe "\"too-long-name\" must be at most 5 characters"
            }
        }

        context("prop - nest - dynamic") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Street.validate() = checking {
                ::id { it.min(1) }
                ::name {
                    it.min(3)
                    it.max(5)
                }
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Address.validate() = checking {
                ::street { it.validate() }
                ::postalCode {
                    when (country) {
                        "US" -> it.length(8)
                        else -> it.length(5)
                    }
                }
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Employee.validate() = checking { ::address { it.validate() } }

            test("success - country is US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "12345678"))
                shouldBeValid { employee.validate() }
            }

            test("success - country is not US") {
                val employee = Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "12345"))
                shouldBeValid { employee.validate() }
            }

            test("failure - country is US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "US", postalCode = "123456789"))
                val detail = shouldBeInvalidSingle { employee.validate() }
                detail.root shouldBe "Employee"
                detail.path.fullName shouldBe "address.postalCode"
                detail.message.content shouldBe "\"123456789\" must be exactly 8 characters"
            }

            test("failure - country is not US") {
                val employee =
                    Employee(1, "abc", Address(1, Street(1, "def"), country = "JP", postalCode = "123456789"))
                val detail = shouldBeInvalidSingle { employee.validate() }
                detail.root shouldBe "Employee"
                detail.path.fullName shouldBe "address.postalCode"
                detail.message.content shouldBe "\"123456789\" must be exactly 5 characters"
            }
        }

        context("prop - nullable") {
            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Street.validate() = checking {
                ::id { it.min(1) }
                ::name {
                    it.min(3)
                    it.max(5)
                }
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Address.validate() = checking { ::street { it.validate() } }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Person.validate() = checking {
                ::firstName { }
                ::lastName { }
                ::address { it?.validate() }
            }

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Person.validate2() = checking {
                ::firstName { it.notNull() }
                ::lastName { it.notNull() }
                ::address { it?.validate() }
            }

            test("success") {
                val person = Person(1, "abc", "def", Address(1, Street(1, "hij")))
                shouldBeValid { person.validate() }
            }

            test("success - nullable") {
                val person = Person(1, null, null, null)
                shouldBeValid { person.validate() }
            }

            test("failure - isNotNull") {
                val person = Person(1, null, null, null)
                val details = shouldBeInvalid { person.validate2() }
                details shouldHaveSize 2
                val detail0 = details[0]
                detail0.root shouldBe "Person"
                detail0.path.fullName shouldBe "firstName"
                detail0.message.content shouldBe "Value must not be null"
                val detail1 = details[1]
                detail1.root shouldBe "Person"
                detail1.path.fullName shouldBe "lastName"
                detail1.message.content shouldBe "Value must not be null"
            }
        }

        context("recursive") {
            data class Node(val children: List<Node> = emptyList())

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            fun Node.validate(): Unit = checking {
                ::children {
                    accumulatingUnit { it.max(3) }
                    it onEach { child -> child.validate() }
                }
            }

            test("success") {
                val node = Node(listOf(Node(), Node(), Node()))
                shouldBeValid { node.validate() }
            }

            test("failure - children size > 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(), Node(), Node(), Node()))))
                val detail = shouldBeInvalidSingle { node.validate() }
                detail.path.fullName shouldBe "children[2]<collection element>.children"
                detail.message.content shouldBe "Collection(size=4) must have at most 3 elements"
            }

            test("failure - grand children size > 3") {
                val node = Node(listOf(Node(), Node(), Node(listOf(Node(listOf(Node(), Node(), Node(), Node()))))))
                val detail = shouldBeInvalidSingle { node.validate() }
                detail.path.fullName shouldBe "children[2]<collection element>.children[0]<collection element>.children"
                detail.message.content shouldBe "Collection(size=4) must have at most 3 elements"
            }
        }

        context("circular reference detection") {
            data class NodeWithValue(val value: Int, var next: NodeWithValue?)

            context(_: ValidationContext, _: Accumulate<FailureDetail>)
            tailrec fun NodeWithValue.validate(): Unit = checking {
                ::value {
                    it.min(0)
                    it.max(100)
                }
                ::next { if (it != null) return it.validate() }
            }

            test("circular reference detected - validation succeeds without error") {
                val node1 = NodeWithValue(10, null)
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference: node1 -> node2 -> node1

                shouldBeValid { node1.validate() }
            }

            test("non-circular nested objects - all valid") {
                val node4 = NodeWithValue(40, null)
                val node3 = NodeWithValue(30, node4)
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                shouldBeValid { node1.validate() }
            }

            test("constraint violation in nested object") {
                val node3 = NodeWithValue(150, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node3)
                val node1 = NodeWithValue(10, node2)

                val detail = shouldBeInvalidSingle { node1.validate() }
                detail.path.fullName shouldBe "next.next.value"
                detail.message.content shouldBe "Number 150 must be less than or equal to 100"
            }

            test("constraint violation in root object") {
                val node2 = NodeWithValue(20, null)
                val node1 = NodeWithValue(-5, node2) // Invalid: < 0

                val detail = shouldBeInvalidSingle { node1.validate() }
                detail.path.fullName shouldBe "value"
                detail.message.content shouldBe "Number -5 must be greater than or equal to 0"
            }

            test("circular reference with constraint violation - stops before revisiting") {
                val node1 = NodeWithValue(200, null) // Invalid: > 100
                val node2 = NodeWithValue(20, node1)
                node1.next = node2 // Create circular reference

                val detail = shouldBeInvalidSingle { node1.validate() }
                detail.path.fullName shouldBe "value"
                detail.message.content shouldBe "Number 200 must be less than or equal to 100"
            }
        }
    })
