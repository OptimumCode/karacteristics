package io.github.optimumcode.karacteristics.generator.internal.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.FunSpec.Builder
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.optimumcode.karacteristics.generator.internal.model.BiDirectionalClass
import io.github.optimumcode.karacteristics.generator.internal.model.Range
import java.nio.file.Path

private const val MIN_CODEPOINT_PROPERTY = "minCodepoint"

private const val MAX_CODEPOINT_PROPERTY = "maxCodepoint"

private const val CONTAINS_METHOD = "contains"

private const val CODEPOINT_PARAMETER = "codepoint"

fun generateDirectionClassesTests(
  packageName: String,
  testPackageName: String,
  outputDir: Path,
  classes: List<BiDirectionalClass>,
  rangeProvider: (BiDirectionalClass) -> List<Range>,
) {
  val enumClass = ClassName(packageName, "CodepointBidirectionalClass")

  classes.forEach { directionalClass ->
    val description =
      TestDescription(
        name = directionalClass.name,
        property = "bidirectionalClass",
        expectedEnum = enumClass,
        expectedName = directionalClass.enumName(),
        ranges = rangeProvider(directionalClass),
      )
    generateTests(
      ClassName(testPackageName, "CodepointBidirectionalClass${directionalClass.name.replace(" ", "")}Test"),
      sequenceOf(description),
    ).writeTo(outputDir)
  }
}

fun generateDirectionClasses(
  packageName: String,
  outputDir: Path,
  classes: List<BiDirectionalClass>,
  rangeProvider: (BiDirectionalClass) -> List<Range>,
) {
  val internalPackageName = "$packageName.internal.bidiclasses"
  val unicodeObjects =
    classes.associateBy {
      it.name.replace(" ", "")
    }

  val characterData = ClassName(internalPackageName, "CodepointDirectionData")
  FileSpec
    .builder(characterData)
    .addGeneratedNotice()
    .addType(
      TypeSpec
        .interfaceBuilder(characterData)
        .addModifiers(INTERNAL)
        .addProperty(
          PropertySpec
            .builder(MIN_CODEPOINT_PROPERTY, INT)
            .addModifiers(ABSTRACT)
            .build(),
        ).addProperty(
          PropertySpec
            .builder(MAX_CODEPOINT_PROPERTY, INT)
            .addModifiers(ABSTRACT)
            .build(),
        ).addFunction(
          FunSpec
            .builder(CONTAINS_METHOD)
            .addModifiers(ABSTRACT)
            .addParameter(
              ParameterSpec
                .builder(CODEPOINT_PARAMETER, INT)
                .build(),
            ).returns(BOOLEAN)
            .build(),
        ).build(),
    ).build()
    .writeTo(outputDir)

  unicodeObjects.forEach { (directionClassName, unicodeObject) ->
    println("Processing '${unicodeObject.name}' group")
    generateObjectWithCheckLogic(
      unicodeObject,
      directionClassName,
      internalPackageName,
      characterData,
      rangeProvider,
    ).build()
      .writeTo(outputDir)
  }
  generateEnum(packageName, characterData, unicodeObjects, internalPackageName, outputDir)
}

private fun BiDirectionalClass.enumName(): String = name.replace(" ", "_").uppercase()

private fun generateEnum(
  packageName: String,
  characterData: ClassName,
  unicodeObjects: Map<String, BiDirectionalClass>,
  internalPackageName: String,
  outputDir: Path,
) {
  val characterDataProperty = "characterData"

  val enumClass = ClassName(packageName, "CodepointBidirectionalClass")

  FileSpec
    .builder(enumClass)
    .addGeneratedNotice()
    .addType(
      TypeSpec
        .enumBuilder(enumClass)
        .primaryConstructor(
          FunSpec
            .constructorBuilder()
            .addParameter(
              ParameterSpec
                .builder(characterDataProperty, characterData)
                .build(),
            ).build(),
        ).addProperty(
          PropertySpec
            .builder(characterDataProperty, characterData)
            .addModifiers(INTERNAL)
            .initializer(characterDataProperty)
            .build(),
        ).apply {
          unicodeObjects.forEach { (className, unicodeObject) ->
            addEnumConstant(
              unicodeObject.enumName(),
              TypeSpec
                .anonymousClassBuilder()
                .apply {
                  kdoc.addStatement("%L type \"%L\" in unicode", unicodeObject.name, unicodeObject.id)
                }.addSuperclassConstructorParameter("%T", ClassName(internalPackageName, className))
                .build(),
            )
          }
        }.build(),
    ).build()
    .writeTo(outputDir)
}

private fun generateObjectWithCheckLogic(
  biDirectionalClass: BiDirectionalClass,
  directionClassName: String,
  packageName: String,
  interfaceImpl: ClassName,
  rangeProvider: (BiDirectionalClass) -> List<Range>,
): FileSpec.Builder {
  val codepointRanges: List<Range> = rangeProvider(biDirectionalClass)
  val minCodepoint: Int = codepointRanges.minOf { it.start }
  val maxCodepoint: Int = codepointRanges.maxOf { it.end }
  val minCodepointProp =
    PropertySpec
      .builder(MIN_CODEPOINT_PROPERTY, INT)
      .addModifiers(OVERRIDE)
      .getter(FunSpec.getterBuilder().addStatement("return %L", minCodepoint.toHexString()).build())
      .build()
  val maxCodepointProp =
    PropertySpec
      .builder(MAX_CODEPOINT_PROPERTY, INT)
      .addModifiers(OVERRIDE)
      .getter(FunSpec.getterBuilder().addStatement("return %L", maxCodepoint.toHexString()).build())
      .build()
  return FileSpec
    .builder(packageName, directionClassName)
    .addGeneratedNotice()
    .addType(
      TypeSpec
        .objectBuilder(directionClassName)
        .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "detekt:all").build())
        .addModifiers(INTERNAL)
        .addSuperinterface(interfaceImpl)
        .addProperties(
          listOf(
            minCodepointProp,
            maxCodepointProp,
          ),
        ).addFunction(
          FunSpec
            .builder(CONTAINS_METHOD)
            .addModifiers(OVERRIDE)
            .returns(BOOLEAN)
            .addParameter(
              ParameterSpec
                .builder(CODEPOINT_PARAMETER, INT)
                .build(),
            ).apply {
              if (codepointRanges.size > 1) {
                beginControlFlow(
                  "if (%2N > %1L || %3N < %1L)",
                  CODEPOINT_PARAMETER,
                  minCodepointProp,
                  maxCodepointProp,
                )
                addStatement("return false")
                endControlFlow()
              }
              checkCodepointInRanges(codepointRanges, CODEPOINT_PARAMETER)
            }.build(),
        ).build(),
    )
}