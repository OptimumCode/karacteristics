@file:Suppress("ktlint:standard:filename")

package io.github.optimumcode.karacteristics

public typealias CodePoint = Int

private const val MAX_CODE_POINT = 0x10FFFF

public val CodePoint.category: CodepointCategory
  get() {
    requireCodepointInRange()
    return CodepointCategory.entries.first { it.characterData.contains(this) }
  }

public operator fun CodepointCategory.contains(codepoint: CodePoint): Boolean = characterData.contains(codepoint)

public val CodePoint.bidirectionalClass: CodepointBidirectionalClass
  get() {
    requireCodepointInRange()
    return CodepointBidirectionalClass.entries.first { it.characterData.contains(this) }
  }

public operator fun CodepointBidirectionalClass.contains(codepoint: CodePoint): Boolean =
  characterData.contains(codepoint)

public val CodePoint.joiningType: CodepointJoiningType
  get() {
    requireCodepointInRange()
    return CodepointJoiningType.entries.first { it.contains(this) }
  }

public operator fun CodepointJoiningType.contains(codepoint: CodePoint): Boolean = contains(codepoint)

public val CodePoint.derivedProperty: CodepointDerivedProperty
  get() {
    requireCodepointInRange()
    return CodepointDerivedProperty.entries.first { it.contains(this) }
  }

public operator fun CodepointDerivedProperty.contains(codepoint: CodePoint): Boolean = contains(codepoint)

private fun CodePoint.requireCodepointInRange() {
  require(this in 0..MAX_CODE_POINT) {
    "code point must be in [0, 0x${MAX_CODE_POINT.toString(16).uppercase()}]"
  }
}