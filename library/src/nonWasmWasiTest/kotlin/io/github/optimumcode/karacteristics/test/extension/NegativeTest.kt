package io.github.optimumcode.karacteristics.test.extension

import io.github.optimumcode.karacteristics.CodePoint
import io.github.optimumcode.karacteristics.CodepointBidirectionalClass
import io.github.optimumcode.karacteristics.CodepointCategory
import io.github.optimumcode.karacteristics.CodepointDerivedProperty
import io.github.optimumcode.karacteristics.CodepointJoiningType
import io.github.optimumcode.karacteristics.bidirectionalClass
import io.github.optimumcode.karacteristics.category
import io.github.optimumcode.karacteristics.contains
import io.github.optimumcode.karacteristics.derivedProperty
import io.github.optimumcode.karacteristics.joiningType
import io.kotest.assertions.asClue
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

      val codepoint = 0x20
      test("category not contains") {
        codepoint.category.asClue {
          (codepoint in CodepointCategory.SPACING_MARK) shouldBe false
        }
      }

      test("bidirectional class not contains") {
        codepoint.bidirectionalClass.asClue {
          (codepoint in CodepointBidirectionalClass.ARABIC_LETTER) shouldBe false
        }
      }

      test("joining type not contains") {
        codepoint.joiningType.asClue {
          (codepoint in CodepointJoiningType.DUAL_JOINING) shouldBe false
        }
      }

      test("derived property not contains") {
        codepoint.derivedProperty.asClue {
          (codepoint in CodepointDerivedProperty.UNASSIGNED) shouldBe false
        }
      }
    },
  )