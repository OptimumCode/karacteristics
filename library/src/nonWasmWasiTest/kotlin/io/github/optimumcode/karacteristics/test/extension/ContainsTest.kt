package io.github.optimumcode.karacteristics.test.extension

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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ContainsTest :
  FunSpec(
    {
      val codepoint = 0x20
      test("category contains") {
        codepoint.category.asClue {
          (codepoint in CodepointCategory.SPACE_SEPARATOR) shouldBe true
        }
      }

      test("bidirectional class contains") {
        codepoint.bidirectionalClass.asClue {
          (codepoint in CodepointBidirectionalClass.WHITE_SPACE) shouldBe true
        }
      }

      test("joining type contains") {
        codepoint.joiningType.asClue {
          (codepoint in CodepointJoiningType.NON_JOINING) shouldBe true
        }
      }

      test("derived property contains") {
        codepoint.derivedProperty.asClue {
          (codepoint in CodepointDerivedProperty.DISALLOWED) shouldBe true
        }
      }
    },
  )