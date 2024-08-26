package io.github.optimumcode.karacteristics.generator.internal.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import io.github.optimumcode.karacteristics.generator.internal.model.Range

fun generateTests(
  testClass: ClassName,
  testsToGenerate: Sequence<TestDescription>,
): FileSpec =
  FileSpec
    .builder(testClass)
    .addImport("io.kotest.matchers", "shouldBe")
    .addImport(
      "io.github.optimumcode.karacteristics",
      "category",
      "bidirectionalClass",
      "joiningType",
      "derivedProperty",
    ).addType(
      TypeSpec
        .classBuilder(testClass)
        .superclass(ClassName("io.kotest.core.spec.style", "FunSpec"))
        .addInitializerBlock(buildTestsCodeBlock(testsToGenerate))
        .build(),
    ).build()

private fun buildTestsCodeBlock(testsToGenerate: Sequence<TestDescription>): CodeBlock =
  buildCodeBlock {
    class SingleTest(
      val name: String,
      val property: String,
      val expectedEnum: ClassName,
      val expectedName: String,
      val testValue: Int,
    )

    testsToGenerate
      .flatMap { description ->
        description.ranges
          .asSequence()
          .flatMap(Range::toTestParameters)
          .map { (testSuffix, testValue) ->
            SingleTest(
              name = "test ${description.name} $testSuffix",
              property = description.property,
              expectedEnum = description.expectedEnum,
              expectedName = description.expectedName,
              testValue = testValue,
            )
          }
      }.forEach { test ->
        beginControlFlow("test(%S)", test.name)
        addStatement("val result = %L.%N", test.testValue, test.property)
        addStatement("result shouldBe %T.%N", test.expectedEnum, test.expectedName)
        endControlFlow()
      }
  }

private fun Range.toTestParameters(): Sequence<Pair<String, Int>> {
  if (start == end) {
    return sequenceOf(shortString() to start)
  }
  if (end - start == 1) {
    return sequenceOf(
      "${shortString()} min" to start,
      "${shortString()} max" to end,
    )
  }
  return sequenceOf(
    "${shortString()} min" to start,
    "${shortString()} mid" to (end + start) / 2,
    "${shortString()} max" to end,
  )
}

private fun Range.shortString(): String = "[$start, $end]"

class TestDescription(
  val name: String,
  val property: String,
  val expectedEnum: ClassName,
  val expectedName: String,
  val ranges: List<Range>,
)