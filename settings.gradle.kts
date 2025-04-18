enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

rootProject.name = "karacteristics-root"

include(":generator")
include(":library")

project(":library").name = "karacteristics"