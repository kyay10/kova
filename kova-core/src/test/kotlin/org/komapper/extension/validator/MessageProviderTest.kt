package org.komapper.extension.validator

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessageProviderTest :
    FunSpec({
        context("MessageProvider0") {
            test("text0") {
                val provider = Message.text0<String> { input -> "input=$input" }
                val message = provider("abc")
                message.content shouldBe "input=abc"
            }

            test("resource0") {
                val provider = Message.resource0<String>("kova.string.isInt")
                val message = provider("abc")
                message.content shouldBe "\"abc\" must be an int"
            }
        }

        context("MessageProvider1") {
            test("text1") {
                val provider = Message.text1<String, Int> { input, a1 -> "input=$input, a1=$a1" }
                val message = provider("abc", 10)
                message.content shouldBe "input=abc, a1=10"
            }

            test("resource1") {
                val provider = Message.resource1<String, Int>("kova.string.length")
                val message = provider("abc", 1)
                message.content shouldBe "\"abc\" must be exactly 1 characters"
            }
        }

        context("MessageProvider2") {
            test("text2") {
                val provider = Message.text2<String, Int, Boolean> { input, a1, a2 -> "input=$input, a1=$a1, a2=$a2" }
                val message = provider("abc", 10, true)
                message.content shouldBe "input=abc, a1=10, a2=true"
            }

            test("resource2") {
                val provider = Message.resource2<List<String>, Int, Int>("kova.collection.max")
                val message = provider(listOf("abc"), 1, 2)
                message.content shouldBe "Collection(size=1) must have at most 2 elements"
            }
        }
    })
