package io.github.optimumcode.karacteristics.generator.internal.dump

import io.github.optimumcode.karacteristics.generator.internal.model.DerivedProperty
import io.github.optimumcode.karacteristics.generator.internal.model.Range
import java.nio.file.Path
import kotlin.io.path.inputStream

object DerivedPropertiesLoader {
  fun loadProperties(path: Path): Map<String, List<DerivedProperty>> =
    path
      .inputStream()
      .bufferedReader(Charsets.UTF_8)
      .useLines { lines ->
        lines
          .filter(String::isNotBlank)
          .map(this::parseDerivedProperties)
          .groupBy(DerivedProperty::type)
      }

  private fun parseDerivedProperties(line: String): DerivedProperty {
    val parts = line.split(';', limit = 2)
    check(parts.size == 2) {
      "Line '$line' does not match required pattern"
    }
    val (codepoints, property) = parts
    val range: Range = parseCodepointsPart(codepoints)
    val type: String = extractType(property)
    return DerivedProperty(type, range)
  }

  private fun extractType(property: String): String {
    // extract and create copy
    return property.substringBefore('#').trim() + ""
  }
}