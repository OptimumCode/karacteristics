package io.github.optimumcode.karacteristics

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class NegativeTest :
  FunSpec(
    {
      mapOf<String, CodePoint.() -> Any>(
        "category" to CodePoint::category,
        "bidirectional class" to CodePoint::bidirectionalClass,
        "joining type" to CodePoint::joiningType,
        "derived property" to CodePoint::derivedProperty,
      ).forEach { (name, property) ->
        test("codepoint $name our of range") {
          shouldThrow<IllegalArgumentException> {
            0xFFFFFF.property()
          }.message shouldBe "code point must be in [0, 0x10FFFF]"
        }
        test("negative codepoint $name our of range") {
          shouldThrow<IllegalArgumentException> {
            (-1).property()
          }.message shouldBe "code point must be in [0, 0x10FFFF]"
        }
      }
    },
  )