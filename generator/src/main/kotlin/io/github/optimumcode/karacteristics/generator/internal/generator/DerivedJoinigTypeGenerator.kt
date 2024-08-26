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
import io.github.optimumcode.karacteristics.generator.internal.model.JoiningType
import java.nio.file.Path

private const val CODE_POINT_PARAMETER = "codePoint"

fun generateDerivedJoiningTypesTests(
  packageName: String,
  testPackageName: String,
  outputDir: Path,
  joiningTypes: Map<String, List<JoiningType>>,
) {
  val enumClass = ClassName(packageName, "CodepointJoiningType")

  for ((type, definitions) in joiningTypes) {
    generateTests(
      ClassName(testPackageName, "CodepointJoiningType${type}Test"),
      definitions
        .asSequence()
        .map {
          TestDescription(
            name = type,
            property = "joiningType",
            expectedEnum = enumClass,
            expectedName = type.uppercase(),
            ranges = listOf(it.range),
          )
        },
    ).writeTo(outputDir)
  }
}

fun generateDerivedJoiningTypes(
  packageName: String,
  outputDir: Path,
  joiningTypes: Map<String, List<JoiningType>>,
) {
  fun containsFunction(): FunSpec.Builder =
    FunSpec
      .builder("contains")
      .returns(Boolean::class)
      .addParameter(ParameterSpec.builder(CODE_POINT_PARAMETER, INT).build())

  val enumClass = ClassName(packageName, "CodepointJoiningType")

  FileSpec
    .builder(enumClass)
    .addType(
      TypeSpec
        .enumBuilder(enumClass)
        .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "detekt:all").build())
        .addFunction(containsFunction().addModifiers(ABSTRACT, INTERNAL).build())
        .apply {
          for ((type, groupedTypes) in joiningTypes) {
            addEnumConstant(
              type.uppercase(),
              TypeSpec
                .anonymousClassBuilder()
                .addFunction(
                  containsFunction()
                    .addModifiers(OVERRIDE)
                    .checkCodePoint(groupedTypes)
                    .build(),
                ).build(),
            )
          }
        }.build(),
    ).build()
    .writeTo(outputDir)
}

private fun FunSpec.Builder.checkCodePoint(properties: List<JoiningType>): FunSpec.Builder =
  apply {
    val ranges = properties.map { it.range }
    checkCodepointInRanges(ranges, CODE_POINT_PARAMETER)
  }