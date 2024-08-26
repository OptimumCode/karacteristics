package io.github.optimumcode.karacteristics.generator.internal.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.optimumcode.karacteristics.generator.internal.model.DerivedProperty
import java.nio.file.Path

private const val CODE_POINT_PARAMETER = "codePoint"

fun generateDerivedPropertiesTest(
  packageName: String,
  testPackageName: String,
  outputDir: Path,
  derivedProperties: Map<String, List<DerivedProperty>>,
) {
  val enumClass = ClassName(packageName, "CodepointDerivedProperty")

  for ((type, definitions) in derivedProperties) {
    generateTests(
      ClassName(testPackageName, "CodepointDerivedProperty${type}Test"),
      definitions
        .asSequence()
        .map {
          TestDescription(
            name = type,
            property = "derivedProperty",
            expectedEnum = enumClass,
            expectedName = type.uppercase(),
            ranges = listOf(it.range),
          )
        },
    ).writeTo(outputDir)
  }
}

fun generateDerivedProperties(
  packageName: String,
  outputDir: Path,
  derivedProperties: Map<String, List<DerivedProperty>>,
) {
  fun containsFunction(): FunSpec.Builder =
    FunSpec
      .builder("contains")
      .returns(Boolean::class)
      .addParameter(ParameterSpec.builder(CODE_POINT_PARAMETER, INT).build())

  val enumClass = ClassName(packageName, "CodepointDerivedProperty")

  FileSpec
    .builder(enumClass)
    .addGeneratedNotice()
    .addType(
      TypeSpec
        .enumBuilder(enumClass)
        .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "detekt:all").build())
        .addFunction(containsFunction().addModifiers(ABSTRACT, INTERNAL).build())
        .apply {
          for ((type, properties) in derivedProperties) {
            addEnumConstant(
              type.uppercase(),
              TypeSpec
                .anonymousClassBuilder()
                .addFunction(
                  containsFunction()
                    .addModifiers(OVERRIDE)
                    .checkCodePoint(properties)
                    .build(),
                ).build(),
            )
          }
        }.build(),
    ).build()
    .writeTo(outputDir)
}

private fun FunSpec.Builder.checkCodePoint(properties: List<DerivedProperty>): FunSpec.Builder =
  apply {
    val ranges = properties.map { it.range }
    checkCodepointInRanges(ranges, CODE_POINT_PARAMETER)
  }