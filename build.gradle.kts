import kotlinx.validation.ExperimentalBCVApi

plugins {
  alias(libs.plugins.kotlin.mutliplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.binaryCompatibility)
}

apiValidation {
  ignoredProjects += setOf("generator")
  @OptIn(ExperimentalBCVApi::class)
  klib {
    enabled = true
  }
}