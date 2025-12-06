package org.komapper.extension.validator

import arrow.core.raise.context.raise
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe

class StringValidatorTest :
    FunSpec({

        context("plus") {
            val validator = Kova.string().max(2) + Kova.string().max(3)

            test("success") {
                validator.tryValidate("1").shouldBeRight()
            }

            test("failure") {
                val details = validator.tryValidate("1234").shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe "\"1234\" must be at most 2 characters"
                details[1].message.content shouldBe "\"1234\" must be at most 3 characters"
            }
        }

        context("and") {
            val validator = Kova.string().max(2) and Kova.string().max(3)

            test("success") {
                validator.tryValidate("1").shouldBeRight()
            }

            test("failure") {
                val details = validator.tryValidate("1234").shouldBeLeft()
                details.size shouldBe 2
                details[0].message.content shouldBe "\"1234\" must be at most 2 characters"
                details[1].message.content shouldBe "\"1234\" must be at most 3 characters"
            }
        }

        context("or") {
            val validator = (Kova.string().isInt() or Kova.literal("zero")).toUpperCase()

            test("success - int") {
                validator.tryValidate("1").shouldBeRight() shouldBe "1"
            }

            test("success - literal") {
                validator.tryValidate("zero").shouldBeRight() shouldBe "ZERO"
            }

            test("failure") {
                validator.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.id shouldBe "kova.or"
                }
            }
        }

        context("chain") {
            val length = Kova.string().length(3)
            val validator = Kova.string().trim().then {
                length(it)
                it.uppercase()
            }
            test("success") {
                validator.tryValidate(" abc ").shouldBeRight() shouldBe "ABC"
            }

            test("failure") {
                validator.tryValidate(" a ").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"a\" must be exactly 3 characters"
                }
            }
        }

        context("constrain") {
            val validator =
                Kova.string().constrain("test") {
                    satisfies(it == "OK") { "Constraint failed" }
                }

            test("success") {
                validator.tryValidate("OK").shouldBeRight()
            }

            test("failure") {
                validator.tryValidate("NG").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Constraint failed"
                }
            }
        }

        context("min") {
            val min = Kova.string().min(3)

            test("success") {
                min.tryValidate("abc").shouldBeRight()
            }

            test("failure") {
                min.tryValidate("ab").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"ab\" must be at least 3 characters"
                }
            }
        }

        context("max") {
            val max = Kova.string().max(1)

            test("success") {
                max.tryValidate("a").shouldBeRight()
            }

            test("failure") {
                max.tryValidate("ab").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"ab\" must be at most 1 characters"
                }
            }
        }

        context("length") {
            val length = Kova.string().length(1)

            test("success") {
                length.tryValidate("a").shouldBeRight()
            }

            test("failure") {
                length.tryValidate("ab").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"ab\" must be exactly 1 characters"
                }
            }
        }

        context("notBlank") {
            val notBlank = Kova.string().notBlank()

            test("success") {
                notBlank.tryValidate("ab").shouldBeRight()
            }
            test("failure") {
                notBlank.tryValidate("").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"\" must not be blank"
                }
            }
        }

        context("notBlank with message") {
            val notBlank = Kova.string().notBlank(Message.text0 { "Must not be blank" })

            test("success") {
                notBlank.tryValidate("ab").shouldBeRight()
            }
            test("failure") {
                notBlank.tryValidate("").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "Must not be blank"
                }
            }
        }

        context("notEmpty") {
            val notEmpty = Kova.string().notEmpty()

            test("success") {
                notEmpty.tryValidate("ab").shouldBeRight()
            }

            test("failure") {
                notEmpty.tryValidate("").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"\" must not be empty"
                }
            }
        }

        context("startsWith") {
            val startsWith = Kova.string().startsWith("ab")

            test("success") {
                startsWith.tryValidate("abcde").shouldBeRight()
            }
            test("failure") {
                startsWith.tryValidate("cde").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"cde\" must start with \"ab\""
                }
            }
        }

        context("endsWith") {
            val endsWith = Kova.string().endsWith("de")

            test("success") {
                endsWith.tryValidate("abcde").shouldBeRight()
            }
            test("failure") {
                endsWith.tryValidate("ab").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"ab\" must end with \"de\""
                }
            }
        }

        context("contains") {
            val contains = Kova.string().contains("cd")

            test("success") {
                contains.tryValidate("abcde").shouldBeRight()
            }
            test("failure") {
                contains.tryValidate("fg").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"fg\" must contain \"cd\""
                }
            }
        }

        context("matches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")
            val matches = Kova.string().matches(emailPattern)

            test("success") {
                matches.tryValidate("user@example.com").shouldBeRight()
            }
            test("failure") {
                matches.tryValidate("invalid-email").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"invalid-email\" must match pattern ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
                }
            }
        }

        context("email") {
            val email = Kova.string().email()

            test("success - simple email") {
                email.tryValidate("user@example.com").shouldBeRight()
            }
            test("success - with dots") {
                email.tryValidate("first.last@example.com").shouldBeRight()
            }
            test("success - with plus") {
                email.tryValidate("user+tag@example.com").shouldBeRight()
            }
            test("success - with hyphen in domain") {
                email.tryValidate("user@my-domain.com").shouldBeRight()
            }
            test("success - with subdomain") {
                email.tryValidate("user@mail.example.com").shouldBeRight()
            }
            test("success - with numbers") {
                email.tryValidate("user123@example.com").shouldBeRight()
            }
            test("success - with underscore") {
                email.tryValidate("user_name@example.com").shouldBeRight()
            }
            test("success - case insensitive") {
                email.tryValidate("User@Example.COM").shouldBeRight()
            }
            test("failure - starts with dot") {
                email.tryValidate(".user@example.com").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\".user@example.com\" must be a valid email address"
                }
            }
            test("failure - consecutive dots") {
                email.tryValidate("user..name@example.com").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"user..name@example.com\" must be a valid email address"
                }
            }
            test("failure - ends with dot before @") {
                email.tryValidate("user.@example.com").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"user.@example.com\" must be a valid email address"
                }
            }
            test("failure - no @") {
                email.tryValidate("userexample.com").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"userexample.com\" must be a valid email address"
                }
            }
            test("failure - no domain") {
                email.tryValidate("user@").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"user@\" must be a valid email address"
                }
            }
            test("failure - no local part") {
                email.tryValidate("@example.com").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"@example.com\" must be a valid email address"
                }
            }
            test("failure - no TLD") {
                email.tryValidate("user@example").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"user@example\" must be a valid email address"
                }
            }
            test("failure - spaces") {
                email.tryValidate("user name@example.com").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"user name@example.com\" must be a valid email address"
                }
            }
        }

        context("isInt") {
            val isInt = Kova.string().isInt()

            test("success") {
                isInt.tryValidate("123").shouldBeRight()
            }
            test("failure") {
                isInt.tryValidate("123a").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"123a\" must be an int"
                }
            }
        }

        context("isLong") {
            val isLong = Kova.string().isLong()

            test("success") {
                isLong.tryValidate("9223372036854775807").shouldBeRight()
            }
            test("failure") {
                isLong.tryValidate("123.45").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"123.45\" must be a long"
                }
            }
        }

        context("isShort") {
            val isShort = Kova.string().isShort()

            test("success") {
                isShort.tryValidate("32767").shouldBeRight()
            }
            test("failure") {
                isShort.tryValidate("99999").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"99999\" must be a short"
                }
            }
        }

        context("isByte") {
            val isByte = Kova.string().isByte()

            test("success") {
                isByte.tryValidate("127").shouldBeRight()
            }
            test("failure") {
                isByte.tryValidate("256").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"256\" must be a byte"
                }
            }
        }

        context("isDouble") {
            val isDouble = Kova.string().isDouble()

            test("success") {
                isDouble.tryValidate("123.45").shouldBeRight()
            }
            test("failure") {
                isDouble.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a double"
                }
            }
        }

        context("isFloat") {
            val isFloat = Kova.string().isFloat()

            test("success") {
                isFloat.tryValidate("123.45").shouldBeRight()
            }
            test("failure") {
                isFloat.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a float"
                }
            }
        }

        context("isBigDecimal") {
            val isBigDecimal = Kova.string().isBigDecimal()

            test("success") {
                isBigDecimal.tryValidate("123.456789012345678901234567890").shouldBeRight()
            }
            test("failure") {
                isBigDecimal.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a big decimal"
                }
            }
        }

        context("isBigInteger") {
            val isBigInteger = Kova.string().isBigInteger()

            test("success") {
                isBigInteger.tryValidate("12345678901234567890").shouldBeRight()
            }
            test("failure") {
                isBigInteger.tryValidate("123.45").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"123.45\" must be a big integer"
                }
            }
        }

        context("isBoolean") {
            val isBoolean = Kova.string().isBoolean()

            test("success - true") {
                isBoolean.tryValidate("true").shouldBeRight()
            }
            test("success - false") {
                isBoolean.tryValidate("false").shouldBeRight()
            }
            test("false - case sensitive") {
                isBoolean.tryValidate("TRUE").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"TRUE\" must be a boolean"
                }
            }
            test("failure") {
                isBoolean.tryValidate("yes").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"yes\" must be a boolean"
                }
            }
        }

        context("toBoolean") {
            val toBoolean = Kova.string().toBoolean()

            test("success - true") {
                toBoolean.tryValidate("true").shouldBeRight() shouldBe true
            }
            test("success - false") {
                toBoolean.tryValidate("false").shouldBeRight() shouldBe false
            }
            test("failure") {
                toBoolean.tryValidate("yes").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"yes\" must be a boolean"
                }
            }
        }

        context("toLong") {
            val toLong = Kova.string().toLong()

            test("success") {
                toLong.tryValidate("9223372036854775807").shouldBeRight() shouldBe 9223372036854775807L
            }
            test("failure") {
                toLong.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a long"
                }
            }
        }

        context("toShort") {
            val toShort = Kova.string().toShort()

            test("success") {
                toShort.tryValidate("32767").shouldBeRight() shouldBe 32767.toShort()
            }
            test("failure") {
                toShort.tryValidate("99999").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"99999\" must be a short"
                }
            }
        }

        context("toByte") {
            val toByte = Kova.string().toByte()

            test("success") {
                toByte.tryValidate("127").shouldBeRight() shouldBe 127.toByte()
            }
            test("failure") {
                toByte.tryValidate("256").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"256\" must be a byte"
                }
            }
        }

        context("toDouble") {
            val toDouble = Kova.string().toDouble()

            test("success") {
                toDouble.tryValidate("123.45").shouldBeRight() shouldBe 123.45
            }
            test("failure") {
                toDouble.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a double"
                }
            }
        }

        context("toFloat") {
            val toFloat = Kova.string().toFloat()

            test("success") {
                toFloat.tryValidate("123.45").shouldBeRight() shouldBe 123.45f
            }
            test("failure") {
                toFloat.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a float"
                }
            }
        }

        context("toBigDecimal") {
            val toBigDecimal = Kova.string().toBigDecimal()

            test("success") {
                toBigDecimal.tryValidate("123.456789012345678901234567890")
                    .shouldBeRight() shouldBe "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                toBigDecimal.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" must be a big decimal"
                }
            }
        }

        context("toBigInteger") {
            val toBigInteger = Kova.string().toBigInteger()

            test("success") {
                toBigInteger.tryValidate("12345678901234567890")
                    .shouldBeRight() shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                toBigInteger.tryValidate("123.45").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"123.45\" must be a big integer"
                }
            }
        }

        context("uppercase") {
            val uppercase = Kova.string().uppercase()

            test("success") {
                uppercase.tryValidate("HELLO").shouldBeRight()
            }
            test("success - empty string") {
                uppercase.tryValidate("").shouldBeRight()
            }
            test("failure") {
                uppercase.tryValidate("Hello").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"Hello\" must be uppercase"
                }
            }
        }

        context("lowercase") {
            val lowercase = Kova.string().lowercase()

            test("success") {
                lowercase.tryValidate("hello").shouldBeRight()
            }
            test("success - empty string") {
                lowercase.tryValidate("").shouldBeRight()
            }
            test("failure") {
                lowercase.tryValidate("Hello").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"Hello\" must be lowercase"
                }
            }
        }

        context("toInt") {
            val toInt = Kova.string().toInt()

            test("success") {
                toInt.tryValidate("123").shouldBeRight() shouldBe 123
            }
            test("failure") {
                toInt.tryValidate("123a").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"123a\" must be an int"
                }
            }
        }

        context("nullableString") {
            val max1 = Kova.nullable<String>().notNull().constrain(Kova.string().max(1).asNullable())

            test("success") {
                max1.tryValidate("1").shouldBeRight()
            }
            test("failure - null") {
                max1.tryValidate(null).shouldBeLeft()
            }
            test("failure") {
                max1.tryValidate("12").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"12\" must be at most 1 characters"
                }
            }
        }

        context("map - string bools") {
            val stringBools = Kova.string().and {
                when (it) {
                    "true" -> true
                    "1" -> true
                    "false" -> false
                    "0" -> false
                    else -> raise("\"$it\" is not a boolean value".failure)
                }
            }

            test("success - true") {
                stringBools.tryValidate("true").shouldBeRight() shouldBe true
            }
            test("success - 1") {
                stringBools.tryValidate("1").shouldBeRight() shouldBe true
            }
            test("success - false") {
                stringBools.tryValidate("false").shouldBeRight() shouldBe false
            }
            test("success - 0") {
                stringBools.tryValidate("0").shouldBeRight() shouldBe false
            }
            test("failure") {
                stringBools.tryValidate("abc").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"abc\" is not a boolean value"
                }
            }
        }

        context("trim") {
            val trim = Kova.string().trim()

            test("success - trimming leading whitespace") {
                trim.tryValidate("  hello").shouldBeRight() shouldBe "hello"
            }

            test("success - trimming trailing whitespace") {
                trim.tryValidate("hello  ").shouldBeRight() shouldBe "hello"
            }

            test("success - trimming both sides") {
                trim.tryValidate("  hello  ").shouldBeRight() shouldBe "hello"
            }

            test("success - no whitespace to trim") {
                trim.tryValidate("hello").shouldBeRight() shouldBe "hello"
            }

            test("success - empty string") {
                trim.tryValidate("").shouldBeRight() shouldBe ""
            }

            test("success - only whitespace") {
                trim.tryValidate("   ").shouldBeRight() shouldBe ""
            }
        }

        context("trim with constraints") {
            val trimMin3 = Kova.string().trim() then {
                Kova.string().min(3)(it)
                it
            }

            test("success - trimmed value meets constraint") {
                trimMin3.tryValidate("  hello  ").shouldBeRight() shouldBe "hello"
            }

            test("failure - trimmed value violates constraint") {
                trimMin3.tryValidate("  hi  ").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"hi\" must be at least 3 characters"
                }
            }

            test("failure - whitespace only becomes empty after trim") {
                trimMin3.tryValidate("   ").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"\" must be at least 3 characters"
                }
            }
        }

        context("toUpperCase") {
            val toUpperCase = Kova.string().toUpperCase()

            test("success - lowercase to uppercase") {
                toUpperCase.tryValidate("hello").shouldBeRight() shouldBe "HELLO"
            }

            test("success - mixed case to uppercase") {
                toUpperCase.tryValidate("HeLLo").shouldBeRight() shouldBe "HELLO"
            }

            test("success - already uppercase") {
                toUpperCase.tryValidate("HELLO").shouldBeRight() shouldBe "HELLO"
            }

            test("success - empty string") {
                toUpperCase.tryValidate("").shouldBeRight() shouldBe ""
            }

            test("success - with numbers and symbols") {
                toUpperCase.tryValidate("hello123!@#").shouldBeRight() shouldBe "HELLO123!@#"
            }
        }

        context("toUpperCase with constraints") {
            val toUpperCaseMin3 = Kova.string().toUpperCase() then {
                Kova.string().min(3)(it)
                it
            }

            test("success - transformed value meets constraint") {
                toUpperCaseMin3.tryValidate("hello").shouldBeRight() shouldBe "HELLO"
            }

            test("failure - transformed value violates constraint") {
                toUpperCaseMin3.tryValidate("hi").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"HI\" must be at least 3 characters"
                }
            }

            test("success - combining toUpperCase with startsWith") {
                val toUpperCaseStartsWithH = Kova.string().toUpperCase() then {
                    Kova.string().startsWith("H")(it)
                    it
                }
                toUpperCaseStartsWithH.tryValidate("hello").shouldBeRight() shouldBe "HELLO"
            }
        }

        context("toLowerCase") {
            val toLowerCase = Kova.string().toLowerCase()

            test("success - uppercase to lowercase") {
                toLowerCase.tryValidate("HELLO").shouldBeRight() shouldBe "hello"
            }

            test("success - mixed case to lowercase") {
                toLowerCase.tryValidate("HeLLo").shouldBeRight() shouldBe "hello"
            }

            test("success - already lowercase") {
                toLowerCase.tryValidate("hello").shouldBeRight() shouldBe "hello"
            }

            test("success - empty string") {
                toLowerCase.tryValidate("").shouldBeRight() shouldBe ""
            }

            test("success - with numbers and symbols") {
                toLowerCase.tryValidate("HELLO123!@#").shouldBeRight() shouldBe "hello123!@#"
            }
        }

        context("toLowerCase with constraints") {
            val toLowerCaseMin3 = Kova.string().toLowerCase().then {
                Kova.string().min(3)(it)
                it
            }

            test("success - transformed value meets constraint") {
                toLowerCaseMin3.tryValidate("HELLO").shouldBeRight() shouldBe "hello"
            }

            test("failure - transformed value violates constraint") {
                toLowerCaseMin3.tryValidate("HI").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"hi\" must be at least 3 characters"
                }
            }

            test("success - combining toLowerCase with startsWith") {
                val toLowerCaseStartsWithH = Kova.string().toLowerCase().then {
                    Kova.string().startsWith("h")(it)
                    it
                }
                toLowerCaseStartsWithH.tryValidate("HELLO").shouldBeRight() shouldBe "hello"
            }
        }

        context("isEnum with Type") {
            val isEnum = Kova.string().isEnum<Status, _>()

            test("success - ACTIVE") {
                isEnum.tryValidate("ACTIVE").shouldBeRight()
            }
            test("success - INACTIVE") {
                isEnum.tryValidate("INACTIVE").shouldBeRight()
            }
            test("success - PENDING") {
                isEnum.tryValidate("PENDING").shouldBeRight()
            }
            test("failure - invalid value") {
                isEnum.tryValidate("INVALID").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
                }
            }
            test("failure - lowercase") {
                isEnum.tryValidate("active").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
                }
            }
        }

        context("isEnum with KClass") {
            val isEnum = Kova.string().isEnum(Status::class)

            test("success - ACTIVE") {
                isEnum.tryValidate("ACTIVE").shouldBeRight()
            }
            test("success - INACTIVE") {
                isEnum.tryValidate("INACTIVE").shouldBeRight()
            }
            test("success - PENDING") {
                isEnum.tryValidate("PENDING").shouldBeRight()
            }
            test("failure - invalid value") {
                isEnum.tryValidate("INVALID").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
                }
            }
            test("failure - lowercase") {
                isEnum.tryValidate("active").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
                }
            }
        }

        context("toEnum") {
            val toEnum = Kova.string().toEnum<Status>()

            test("success - ACTIVE") {
                toEnum.tryValidate("ACTIVE").shouldBeRight() shouldBe Status.ACTIVE
            }
            test("success - INACTIVE") {
                toEnum.tryValidate("INACTIVE").shouldBeRight() shouldBe Status.INACTIVE
            }
            test("success - PENDING") {
                toEnum.tryValidate("PENDING").shouldBeRight() shouldBe Status.PENDING
            }
            test("failure - invalid value") {
                toEnum.tryValidate("INVALID").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
                }
            }
            test("failure - lowercase") {
                toEnum.tryValidate("active").shouldBeLeft().shouldBeSingleton {
                    it.message.content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
                }
            }
        }
    }) {
    enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING,
    }
}
