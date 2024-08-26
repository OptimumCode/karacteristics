package io.github.optimumcode.karacteristics.generator.internal.dump

import io.github.optimumcode.karacteristics.generator.internal.model.JoiningType
import io.github.optimumcode.karacteristics.generator.internal.model.Range
import java.nio.file.Path
import kotlin.io.path.inputStream

internal object JoiningTypesLoader {
  private const val DEFAULT_TYPE = "Non_Joining"
  private const val TYPE_DECLARATION = "Joining_Type="
  private const val COMMENT = '#'
  private const val SEPARATOR = ';'
  private const val MAX_CODE_POINT = 0x10FFFF

  fun loadTypes(path: Path): Map<String, List<JoiningType>> {
    val groupedJoiningTypes: MutableMap<String, MutableList<JoiningType>> =
      path
        .inputStream()
        .bufferedReader(Charsets.UTF_8)
        .useLines { lines ->
          var type: String? = null
          val destination = hashMapOf<String, MutableList<JoiningType>>()
          for (line in lines.filter(String::isNotBlank)) {
            if (line.contains(TYPE_DECLARATION)) {
              type = line.substringAfter(TYPE_DECLARATION).trim() + ""
              continue
            }
            if (SEPARATOR !in line || line.startsWith(COMMENT)) {
              continue
            }
            val codepoints = line.substringBefore(SEPARATOR).trim()
            destination
              .computeIfAbsent(
                requireNotNull(type) { "type" },
              ) { arrayListOf() }
              .add(
                JoiningType(
                  type = type,
                  range = parseCodepointsPart(codepoints),
                ),
              )
          }
          destination
        }
    val specifiedRanges =
      groupedJoiningTypes.values
        .asSequence()
        .flatMap { it.asSequence().map(JoiningType::range) }
        .sortedBy { it.start }
        .toList()
    groupedJoiningTypes[DEFAULT_TYPE] = specifiedRanges.generateMissingRanges(start = 0, end = MAX_CODE_POINT)
    return groupedJoiningTypes
  }

  private operator fun Range.contains(value: Int): Boolean = value in start..end

  private fun List<Range>.generateMissingRanges(
    start: Int,
    end: Int,
  ): MutableList<JoiningType> =
    ArrayList<JoiningType>().apply {
      var rangeStart = start

      for (range in this@generateMissingRanges) {
        if (rangeStart in range) {
          rangeStart = range.end + 1
          continue
        }
        add(JoiningType(DEFAULT_TYPE, Range(rangeStart, range.start - 1)))
        rangeStart = range.end + 1
      }
      if (rangeStart <= end) {
        add(JoiningType(DEFAULT_TYPE, Range(rangeStart, end)))
      }
    }
}