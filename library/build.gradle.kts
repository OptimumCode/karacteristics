@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
  alias(libs.plugins.kotlin.mutliplatform)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.publication)
}

group = "io.github.optimumcode"
description = "Codepoint characteristics for Kotlin"

val generatedSourceDirectory: Provider<Directory> = layout.buildDirectory.dir("generated/source/unicode")
val generatedTestSourceDirectory: Provider<Directory> = layout.buildDirectory.dir("generated/source/unicode-test")

kotlin {
  explicitApi()

  applyDefaultHierarchyTemplate()
  jvm {
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      freeCompilerArgs.add("-Xjdk-release=11")
    }
  }
  js(IR) {
    browser()
    nodejs()
    generateTypeScriptDefinitions()
  }

  macosX64()
  macosArm64()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  linuxX64()
  linuxArm64()

  mingwX64()

  wasmJs {
    browser()
    nodejs()
  }
  wasmWasi()

  sourceSets {
    commonMain {
      kotlin.srcDir(generatedSourceDirectory)
    }
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))

        implementation(
          libs.kotlin.coroutines.core
            .get()
            .toString(),
        ) {
          // see "https://kotlinlang.slack.com/archives/CDFP59223/p1736191408326039?thread_ts=1734964013.996149&cid=CDFP59223"
          because(
            "there is a problem with linkage related to changes in kotlin 2.1.0: " +
              "wasmJs tests in browser does not work without updating coroutines to the version compiled with 2.1.0",
          )
        }
      }
    }
    val nonWasmWasiTest by creating {
      kotlin.srcDir(generatedTestSourceDirectory)
      dependsOn(commonTest.get())
      dependencies {
        implementation(libs.kotest.assertions.core)
        implementation(libs.kotest.framework.engine)
      }
    }
    jvmTest {
      dependsOn(nonWasmWasiTest)
      dependencies {
        implementation(libs.kotest.runner.junit5)
      }
    }
    nativeTest {
      dependsOn(nonWasmWasiTest)
    }
    jsTest {
      dependsOn(nonWasmWasiTest)
    }
    wasmJsTest {
      dependsOn(nonWasmWasiTest)
    }
  }
}

//region Generation tasks block
val generatorConfiguration: Configuration by configurations.creating

dependencies {
  generatorConfiguration(projects.generator)
}

val generatorMainClass = provider { "io.github.optimumcode.karacteristics.generator.Main" }

val dumpDir: File =
  rootProject.layout.projectDirectory
    .dir("unicode_dump")
    .asFile

val dumpCharacterData by tasks.register<JavaExec>("dumpCharacterData") {
  onlyIf {
    dumpDir.run { !exists() || listFiles().isNullOrEmpty() }
  }
  outputs.dir(dumpDir)
  classpath(generatorConfiguration)
  mainClass.set(generatorMainClass)
  args(
    "dump",
    "-o",
    dumpDir,
  )
}

val generateCharacterDirectionData by tasks.register<JavaExec>("generateCharacterDirectionData") {
  inputs.dir(dumpDir)
  outputs.dirs(generatedSourceDirectory, generatedTestSourceDirectory)

  dependsOn(dumpCharacterData)

  classpath(generatorConfiguration)
  mainClass.set(generatorMainClass)
  args(
    "character-direction",
    "-p",
    "io.github.optimumcode.karacteristics",
    "-o",
    generatedSourceDirectory.get(),
    "-t",
    generatedTestSourceDirectory.get(),
    "-d",
    dumpDir,
  )
}

val generateCharacterCategoryData by tasks.register<JavaExec>("generateCharacterCategoryData") {
  inputs.dir(dumpDir)
  outputs.dirs(generatedSourceDirectory, generatedTestSourceDirectory)

  dependsOn(dumpCharacterData)

  classpath(generatorConfiguration)
  mainClass.set(generatorMainClass)
  args(
    "character-category",
    "-p",
    "io.github.optimumcode.karacteristics",
    "-o",
    generatedSourceDirectory.get(),
    "-t",
    generatedTestSourceDirectory.get(),
    "-d",
    dumpDir,
  )
}

val generateDerivedProperties by tasks.register<JavaExec>("generateDerivedProperties") {
  val dataFile =
    rootProject.layout.projectDirectory
      .dir("generator")
      .dir("data")
      .file("rfc5895_appendix_b_1.txt")
  inputs.file(dataFile)
  outputs.dirs(generatedSourceDirectory, generatedTestSourceDirectory)

  classpath(generatorConfiguration)
  mainClass.set(generatorMainClass)
  args(
    "derived-properties",
    "-p",
    "io.github.optimumcode.karacteristics",
    "-o",
    generatedSourceDirectory.get(),
    "-t",
    generatedTestSourceDirectory.get(),
    "-d",
    dataFile,
  )
}

val generateJoiningTypes by tasks.register<JavaExec>("generateJoiningTypes") {
  val dataFile =
    rootProject.layout.projectDirectory
      .dir("generator")
      .dir("data")
      .file("DerivedJoiningType.txt")
  inputs.file(dataFile)
  outputs.dirs(generatedSourceDirectory, generatedTestSourceDirectory)

  classpath(generatorConfiguration)
  mainClass.set(generatorMainClass)
  args(
    "joining-types",
    "-p",
    "io.github.optimumcode.karacteristics",
    "-o",
    generatedSourceDirectory.get(),
    "-t",
    generatedTestSourceDirectory.get(),
    "-d",
    dataFile,
  )
}
//endregion

afterEvaluate {
  val taskFilters: List<(String) -> Boolean> =
    listOf(
      { it.startsWith("compile") },
      { it.endsWith("sourcesJar", ignoreCase = true) },
    )
  tasks.configureEach {
    if (taskFilters.any { it(name) }) {
      dependsOn(
        generateCharacterDirectionData,
        generateCharacterCategoryData,
        generateDerivedProperties,
        generateJoiningTypes,
      )
    }
  }
}