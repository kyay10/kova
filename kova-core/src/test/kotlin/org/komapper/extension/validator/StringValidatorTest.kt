package org.komapper.extension.validator

import arrow.core.raise.context.RaiseAccumulate
import arrow.core.raise.context.raise
import io.kotest.assertions.arrow.core.shouldHaveSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("KotlinUnreachableCode")
class StringValidatorTest :
    FunSpec({

        context("plus") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.validate() {
                accumulatingUnit { max(2) }
                max(3)
            }

            test("success") {
                shouldBeValid { "1".validate() }
            }

            test("failure") {
                val details = shouldBeInvalid { "1234".validate() }
                details shouldHaveSize 2
                details[0].message.content shouldBe "\"1234\" must be at most 2 characters"
                details[1].message.content shouldBe "\"1234\" must be at most 3 characters"
            }
        }

        context("or") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.validate(): String {
                or<Unit> { isInt() } or { literal("zero") } or Fail
                return uppercase()
            }

            test("success - int") {
                shouldBeValid { "1".validate() } shouldBe "1"
            }

            test("success - literal") {
                shouldBeValid { "zero".validate() } shouldBe "ZERO"
            }

            test("failure") {
                shouldBeInvalidSingle { "abc".validate() }.message.id shouldBe "kova.or"
            }
        }

        context("chain") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.validate(): String = trim().also { it length 3 }.uppercase()

            test("success") {
                shouldBeValid { " abc ".validate() } shouldBe "ABC"
            }

            test("failure") {
                shouldBeInvalidSingle { " a ".validate() }.message.content shouldBe "\"a\" must be exactly 3 characters"
            }
        }

        context("constrain") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.validate() = constrain("test") {
                satisfies(this == "OK") { "Constraint failed" }
            }

            test("success") {
                shouldBeValid { "OK".validate() }
            }

            test("failure") {
                shouldBeInvalidSingle { "NG".validate() }.message.content shouldBe "Constraint failed"
            }
        }

        context("min") {
            test("success") {
                shouldBeValid { "abc" min 3 }
            }

            test("failure") {
                shouldBeInvalidSingle { "ab" min 3 }.message.content shouldBe "\"ab\" must be at least 3 characters"
            }
        }

        context("max") {
            test("success") {
                shouldBeValid { "a" max 1 }
            }

            test("failure") {
                shouldBeInvalidSingle { "ab" max 1 }.message.content shouldBe "\"ab\" must be at most 1 characters"
            }
        }

        context("length") {
            test("success") {
                shouldBeValid { "a" length 1 }
            }

            test("failure") {
                shouldBeInvalidSingle { "ab" length 1 }.message.content shouldBe "\"ab\" must be exactly 1 characters"
            }
        }

        context("notBlank") {
            test("success") {
                shouldBeValid { "ab".notBlank() }
            }
            test("failure") {
                shouldBeInvalidSingle { "".notBlank() }.message.content shouldBe "\"\" must not be blank"
            }
        }

        context("notBlank with message") {
            test("success") {
                shouldBeValid { "ab".notBlank(Message.text0 { "Must not be blank" }) }
            }
            test("failure") {
                shouldBeInvalidSingle { "".notBlank(Message.text0 { "Must not be blank" }) }
                    .message.content shouldBe "Must not be blank"
            }
        }

        context("notEmpty") {
            test("success") {
                shouldBeValid { "ab".notEmpty() }
            }

            test("failure") {
                shouldBeInvalidSingle { "".notEmpty() }.message.content shouldBe "\"\" must not be empty"
            }
        }

        context("startsWith") {
            test("success") {
                shouldBeValid { "abcde" startsWith "ab" }
            }
            test("failure") {
                shouldBeInvalidSingle { "cde" startsWith "ab" }.message.content shouldBe "\"cde\" must start with \"ab\""
            }
        }

        context("endsWith") {
            test("success") {
                shouldBeValid { "abcde" endsWith "de" }
            }
            test("failure") {
                shouldBeInvalidSingle { "ab" endsWith "de" }.message.content shouldBe "\"ab\" must end with \"de\""
            }
        }

        context("contains") {
            test("success") {
                shouldBeValid { "abcde" contains "cd" }
            }
            test("failure") {
                shouldBeInvalidSingle { "fg" contains "cd" }.message.content shouldBe "\"fg\" must contain \"cd\""
            }
        }

        context("matches") {
            val emailPattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

            test("success") {
                shouldBeValid { "user@example.com" matches emailPattern }
            }
            test("failure") {
                shouldBeInvalidSingle { "invalid-email" matches emailPattern }.message.content shouldBe "\"invalid-email\" must match pattern ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"
            }
        }

        context("email") {
            test("success - simple email") {
                shouldBeValid { "user@example.com".email() }
            }
            test("success - with dots") {
                shouldBeValid { "first.last@example.com".email() }
            }
            test("success - with plus") {
                shouldBeValid { "user+tag@example.com".email() }
            }
            test("success - with hyphen in domain") {
                shouldBeValid { "user@my-domain.com".email() }
            }
            test("success - with subdomain") {
                shouldBeValid { "user@mail.example.com".email() }
            }
            test("success - with numbers") {
                shouldBeValid { "user123@example.com".email() }
            }
            test("success - with underscore") {
                shouldBeValid { "user_name@example.com".email() }
            }
            test("success - case insensitive") {
                shouldBeValid { "User@Example.COM".email() }
            }
            test("failure - starts with dot") {
                shouldBeInvalidSingle { ".user@example.com".email() }.message.content shouldBe "\".user@example.com\" must be a valid email address"
            }
            test("failure - consecutive dots") {
                shouldBeInvalidSingle { "user..name@example.com".email() }.message.content shouldBe "\"user..name@example.com\" must be a valid email address"
            }
            test("failure - ends with dot before @") {
                shouldBeInvalidSingle { "user.@example.com".email() }.message.content shouldBe "\"user.@example.com\" must be a valid email address"
            }
            test("failure - no @") {
                shouldBeInvalidSingle { "userexample.com".email() }.message.content shouldBe "\"userexample.com\" must be a valid email address"
            }
            test("failure - no domain") {
                shouldBeInvalidSingle { "user@".email() }.message.content shouldBe "\"user@\" must be a valid email address"
            }
            test("failure - no local part") {
                shouldBeInvalidSingle { "@example.com".email() }.message.content shouldBe "\"@example.com\" must be a valid email address"
            }
            test("failure - no TLD") {
                shouldBeInvalidSingle { "user@example".email() }.message.content shouldBe "\"user@example\" must be a valid email address"
            }
            test("failure - spaces") {
                shouldBeInvalidSingle { "user name@example.com".email() }.message.content shouldBe "\"user name@example.com\" must be a valid email address"
            }
        }

        context("isInt") {
            test("success") {
                shouldBeValid { "123".isInt() }
            }
            test("failure") {
                shouldBeInvalidSingle { "123a".isInt() }.message.content shouldBe "\"123a\" must be an int"
            }
        }

        context("isLong") {
            test("success") {
                shouldBeValid { "9223372036854775807".isLong() }
            }
            test("failure") {
                shouldBeInvalidSingle { "123.45".isLong() }.message.content shouldBe "\"123.45\" must be a long"
            }
        }

        context("isShort") {
            test("success") {
                shouldBeValid { "32767".isShort() }
            }
            test("failure") {
                shouldBeInvalidSingle { "99999".isShort() }.message.content shouldBe "\"99999\" must be a short"
            }
        }

        context("isByte") {
            test("success") {
                shouldBeValid { "127".isByte() }
            }
            test("failure") {
                shouldBeInvalidSingle { "256".isByte() }.message.content shouldBe "\"256\" must be a byte"
            }
        }

        context("isDouble") {
            test("success") {
                shouldBeValid { "123.45".isDouble() }
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isDouble() }.message.content shouldBe "\"abc\" must be a double"
            }
        }

        context("isFloat") {
            test("success") {
                shouldBeValid { "123.45".isFloat() }
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isFloat() }.message.content shouldBe "\"abc\" must be a float"
            }
        }

        context("isBigDecimal") {
            test("success") {
                shouldBeValid { "123.456789012345678901234567890".isBigDecimal() }
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isBigDecimal() }.message.content shouldBe "\"abc\" must be a big decimal"
            }
        }

        context("isBigInteger") {
            test("success") {
                shouldBeValid { "12345678901234567890".isBigInteger() }
            }
            test("failure") {
                shouldBeInvalidSingle { "123.45".isBigInteger() }.message.content shouldBe "\"123.45\" must be a big integer"
            }
        }

        context("isBoolean") {
            test("success - true") {
                shouldBeValid { "true".isBoolean() }
            }
            test("success - false") {
                shouldBeValid { "false".isBoolean() }
            }
            test("false - case sensitive") {
                shouldBeInvalidSingle { "TRUE".isBoolean() }.message.content shouldBe "\"TRUE\" must be a boolean"
            }
            test("failure") {
                shouldBeInvalidSingle { "yes".isBoolean() }.message.content shouldBe "\"yes\" must be a boolean"
            }
        }

        context("toBoolean") {
            test("success - true") {
                shouldBeValid { "true".isBoolean() } shouldBe true
            }
            test("success - false") {
                shouldBeValid { "false".isBoolean() } shouldBe false
            }
            test("failure") {
                shouldBeInvalidSingle { "yes".isBoolean() }.message.content shouldBe "\"yes\" must be a boolean"
            }
        }

        context("toLong") {
            test("success") {
                shouldBeValid { "9223372036854775807".isLong() } shouldBe 9223372036854775807L
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isLong() }.message.content shouldBe "\"abc\" must be a long"
            }
        }

        context("toShort") {
            test("success") {
                shouldBeValid { "32767".isShort() } shouldBe 32767.toShort()
            }
            test("failure") {
                shouldBeInvalidSingle { "99999".isShort() }.message.content shouldBe "\"99999\" must be a short"
            }
        }

        context("toByte") {
            test("success") {
                shouldBeValid { "127".isByte() } shouldBe 127.toByte()
            }
            test("failure") {
                shouldBeInvalidSingle { "256".isByte() }.message.content shouldBe "\"256\" must be a byte"
            }
        }

        context("toDouble") {
            test("success") {
                shouldBeValid { "123.45".isDouble() } shouldBe 123.45
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isDouble() }.message.content shouldBe "\"abc\" must be a double"
            }
        }

        context("toFloat") {
            test("success") {
                shouldBeValid { "123.45".isFloat() } shouldBe 123.45f
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isFloat() }.message.content shouldBe "\"abc\" must be a float"
            }
        }

        context("toBigDecimal") {
            test("success") {
                shouldBeValid { "123.456789012345678901234567890".isBigDecimal() } shouldBe
                    "123.456789012345678901234567890".toBigDecimal()
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".isBigDecimal() }.message.content shouldBe "\"abc\" must be a big decimal"
            }
        }

        context("toBigInteger") {
            test("success") {
                shouldBeValid { "12345678901234567890".isBigInteger() } shouldBe "12345678901234567890".toBigInteger()
            }
            test("failure") {
                shouldBeInvalidSingle { "123.45".isBigInteger() }.message.content shouldBe "\"123.45\" must be a big integer"
            }
        }

        context("uppercase") {
            test("success") {
                shouldBeValid { "HELLO".isUppercase() }
            }
            test("success - empty string") {
                shouldBeValid { "".isUppercase() }
            }
            test("failure") {
                shouldBeInvalidSingle { "Hello".isUppercase() }.message.content shouldBe "\"Hello\" must be uppercase"
            }
        }

        context("lowercase") {
            test("success") {
                shouldBeValid { "hello".isLowercase() }
            }
            test("success - empty string") {
                shouldBeValid { "".isLowercase() }
            }
            test("failure") {
                shouldBeInvalidSingle { "Hello".isLowercase() }.message.content shouldBe "\"Hello\" must be lowercase"
            }
        }

        context("toInt") {
            test("success") {
                shouldBeValid { "123".isInt() } shouldBe 123
            }
            test("failure") {
                shouldBeInvalidSingle { "123a".isInt() }.message.content shouldBe "\"123a\" must be an int"
            }
        }

        context("nullableString") {
            test("success") {
                shouldBeValid { "1".notNull() max 1 }
            }
            test("failure - null") {
                shouldBeInvalid { null.notNull() max 1 }
            }
            test("failure") {
                shouldBeInvalidSingle { "12".notNull() max 1 }.message.content shouldBe "\"12\" must be at most 1 characters"
            }
        }

        context("map - string bools") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.stringBools(): Boolean = when (this) {
                "true" -> true
                "1" -> true
                "false" -> false
                "0" -> false
                else -> raise("\"$this\" is not a boolean value".failure)
            }

            test("success - true") {
                shouldBeValid { "true".stringBools() } shouldBe true
            }
            test("success - 1") {
                shouldBeValid { "1".stringBools() } shouldBe true
            }
            test("success - false") {
                shouldBeValid { "false".stringBools() } shouldBe false
            }
            test("success - 0") {
                shouldBeValid { "0".stringBools() } shouldBe false
            }
            test("failure") {
                shouldBeInvalidSingle { "abc".stringBools() }.message.content shouldBe "\"abc\" is not a boolean value"
            }
        }

        context("trim") {
            test("success - trimming leading whitespace") {
                shouldBeValid { "  hello".trim() } shouldBe "hello"
            }

            test("success - trimming trailing whitespace") {
                shouldBeValid { "hello  ".trim() } shouldBe "hello"
            }

            test("success - trimming both sides") {
                shouldBeValid { "  hello  ".trim() } shouldBe "hello"
            }

            test("success - no whitespace to trim") {
                shouldBeValid { "hello".trim() } shouldBe "hello"
            }

            test("success - empty string") {
                shouldBeValid { "".trim() } shouldBe ""
            }

            test("success - only whitespace") {
                shouldBeValid { "   ".trim() } shouldBe ""
            }
        }

        context("trim with constraints") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.trimMin3() = trim().also { it min 3 }

            test("success - trimmed value meets constraint") {
                shouldBeValid { "  hello  ".trimMin3() } shouldBe "hello"
            }

            test("failure - trimmed value violates constraint") {
                shouldBeInvalidSingle { "  hi  ".trimMin3() }.message.content shouldBe "\"hi\" must be at least 3 characters"
            }

            test("failure - whitespace only becomes empty after trim") {
                shouldBeInvalidSingle { "   ".trimMin3() }.message.content shouldBe "\"\" must be at least 3 characters"
            }
        }

        context("toUpperCase") {
            test("success - lowercase to uppercase") {
                shouldBeValid { "hello".uppercase() } shouldBe "HELLO"
            }

            test("success - mixed case to uppercase") {
                shouldBeValid { "HeLLo".uppercase() } shouldBe "HELLO"
            }

            test("success - already uppercase") {
                shouldBeValid { "HELLO".uppercase() } shouldBe "HELLO"
            }

            test("success - empty string") {
                shouldBeValid { "".uppercase() } shouldBe ""
            }

            test("success - with numbers and symbols") {
                shouldBeValid { "hello123!@#".uppercase() } shouldBe "HELLO123!@#"
            }
        }

        context("toUpperCase with constraints") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.toUpperCaseMin3() = uppercase().also { it min 3 }

            test("success - transformed value meets constraint") {
                shouldBeValid { "hello".toUpperCaseMin3() } shouldBe "HELLO"
            }

            test("failure - transformed value violates constraint") {
                shouldBeInvalidSingle { "hi".toUpperCaseMin3() }
                    .message.content shouldBe "\"HI\" must be at least 3 characters"
            }

            test("success - combining toUpperCase with startsWith") {
                shouldBeValid { "hello".uppercase().also { it startsWith "H" } } shouldBe "HELLO"
            }
        }

        context("toLowerCase") {
            test("success - uppercase to lowercase") {
                shouldBeValid { "HELLO".lowercase() } shouldBe "hello"
            }

            test("success - mixed case to lowercase") {
                shouldBeValid { "HeLLo".lowercase() } shouldBe "hello"
            }

            test("success - already lowercase") {
                shouldBeValid { "hello".lowercase() } shouldBe "hello"
            }

            test("success - empty string") {
                shouldBeValid { "".lowercase() } shouldBe ""
            }

            test("success - with numbers and symbols") {
                shouldBeValid { "HELLO123!@#".lowercase() } shouldBe "hello123!@#"
            }
        }

        context("toLowerCase with constraints") {
            context(_: ValidationContext, _: RaiseAccumulate<FailureDetail>)
            fun String.toLowerCaseMin3() = lowercase().also { it min 3 }

            test("success - transformed value meets constraint") {
                shouldBeValid { "HELLO".toLowerCaseMin3() } shouldBe "hello"
            }

            test("failure - transformed value violates constraint") {
                shouldBeInvalidSingle { "HI".toLowerCaseMin3() }.message.content shouldBe "\"hi\" must be at least 3 characters"
            }

            test("success - combining toLowerCase with startsWith") {
                shouldBeValid { "HELLO".lowercase().also { it startsWith "h" } } shouldBe "hello"
            }
        }

        context("isEnum with Type") {
            test("success - ACTIVE") {
                shouldBeValid { "ACTIVE".isEnum<Status>() } shouldBe Status.ACTIVE
            }
            test("success - INACTIVE") {
                shouldBeValid { "INACTIVE".isEnum<Status>() } shouldBe Status.INACTIVE
            }
            test("success - PENDING") {
                shouldBeValid { "PENDING".isEnum<Status>() } shouldBe Status.PENDING
            }
            test("failure - invalid value") {
                shouldBeInvalidSingle { "INVALID".isEnum<Status>() }
                    .message.content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
            test("failure - lowercase") {
                shouldBeInvalidSingle { "active".isEnum<Status>() }
                    .message.content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
        }

        context("isEnum with KClass") {

            test("success - ACTIVE") {
                shouldBeValid { "ACTIVE".isEnum(Status::class) } shouldBe Status.ACTIVE
            }
            test("success - INACTIVE") {
                shouldBeValid { "INACTIVE".isEnum(Status::class) } shouldBe Status.INACTIVE
            }
            test("success - PENDING") {
                shouldBeValid { "PENDING".isEnum(Status::class) } shouldBe Status.PENDING
            }
            test("failure - invalid value") {
                shouldBeInvalidSingle { "INVALID".isEnum(Status::class) }
                    .message.content shouldBe "\"INVALID\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
            test("failure - lowercase") {
                shouldBeInvalidSingle { "active".isEnum(Status::class) }
                    .message.content shouldBe "\"active\" must be one of [ACTIVE, INACTIVE, PENDING]"
            }
        }
    }) {
    enum class Status {
        ACTIVE,
        INACTIVE,
        PENDING,
    }
}
