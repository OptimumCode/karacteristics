import com.expediagroup.graphql.plugin.gradle.config.GraphQLSerializer
import com.expediagroup.graphql.plugin.gradle.graphql

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.expediagroup.graphql)
}

kotlin {
  jvmToolchain(11)
}

dependencies {
  implementation(libs.kotlinpoet)
  implementation(libs.graphql.ktor)
  implementation(libs.clikt) {
    because("cli for executing generation")
  }
}

graphql {
  client {
    endpoint = "https://www.compart.com/en/unicode/graphql"
    packageName = "io.github.optimumcode.karacteristics.generator.internal.graphql"
    serializer = GraphQLSerializer.KOTLINX
  }
}