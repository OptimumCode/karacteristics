# karacteristics

[![Licence](https://img.shields.io/github/license/OptimumCode/json-schema-validator)](https://opensource.org/license/mit/)
![Maven Central Version](https://img.shields.io/maven-central/v/io.github.optimumcode/karacteristics)

The **karacteristics** library provides convenient extension properties to get characteristics for Unicode codepoints.
The following characteristics are available:

+ [category](https://www.unicode.org/reports/tr44/#General_Category_Values)
+ [bidirectional class](https://www.unicode.org/reports/tr44/#Bidi_Class_Values)
+ [derived property](https://www.unicode.org/Public/UNIDATA/DerivedCoreProperties.txt)
+ [joining type](https://unicode.org/Public/UNIDATA/extracted/DerivedJoiningType.txt)

The first two properties are generated using information from https://www.compart.com/en/unicode/ resource.
The latter two are generated from the corresponding files.

# Problem to solve

There is no way to get codepoint characteristics in Kotlin Multiplatform.
The available API provides only `category` property for `Char` type.
But this is no enough - some of the Unicode codepoints take more than 2 bytes (size of `Char`).
Because of that, the existing API returns incorrect information for such codepoints.

# Usage

### Supported targets

| Target            |
|-------------------|
| jvm               |
| js                |
| wasmJs            |
| wasmWasi          |
| macosX64          |
| macosArm64        |
| iosArm64          |
| iosSimulatorArm64 |
| linuxX64          |
| linuxArm64        |
| mingwX64          |

The library is published to Maven Central.
To use it, just add a corresponding dependency to the common source set.

```kotlin
kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation("io.github.optimumcode:karacteristics:0.0.5")
      }
    }
  }
}
```