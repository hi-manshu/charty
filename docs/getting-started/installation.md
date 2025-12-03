# Installation

Get started with Charty in your project by adding the dependency.

## Prerequisites

- Kotlin 2.2.21 or higher
- Compose Multiplatform 1.9.3 or higher
- Android minSdk 24 (for Android projects)

---

## Gradle Setup

### Kotlin Multiplatform

Add Charty to your common source set in your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.himanshoe:charty:<latest-version>")
        }
    }
}
```

### Android Project

Add Charty to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.himanshoe:charty:<latest-version>")
}
```

!!! tip "Latest Version"
    Check the latest version on [Maven Central](https://search.maven.org/artifact/com.himanshoe/charty)
    
    ![Maven Central](https://img.shields.io/maven-central/v/com.himanshoe/charty?color=f4c430&label=Maven%20Central)

---

## Version Catalog (recommended)

If you're using Gradle version catalogs, add this to your `libs.versions.toml`:

```toml
[versions]
charty = "<latest-version>"

[libraries]
charty = { module = "com.himanshoe:charty", version.ref = "charty" }
```

Then in your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.charty)
        }
    }
}
```

---

## Platform-Specific Configuration

### Android

No additional configuration needed! Charty works out of the box with Jetpack Compose.

Make sure your `build.gradle.kts` has Compose enabled:

```kotlin
android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "<compose-compiler-version>"
    }
}
```

### iOS

Charty works with Compose Multiplatform for iOS. Ensure you have the iOS framework configured:

```kotlin
kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "YourApp"
            isStatic = true
        }
    }
}
```

### Desktop (JVM)

Charty works with Compose for Desktop:

```kotlin
kotlin {
    jvm {
        withJava()
    }
}
```

### Web (JS/Wasm)

For web targets:

```kotlin
kotlin {
    js {
        browser()
    }
    
    // or for WebAssembly
    wasmJs {
        browser()
    }
}
```

---

## Verify Installation

Create a simple test to verify Charty is installed correctly:

```kotlin
import androidx.compose.runtime.Composable
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.data.BarData

@Composable
fun TestChart() {
    BarChart(
        dataCollection = listOf(
            BarData(10f, "A"),
            BarData(20f, "B"),
            BarData(15f, "C")
        )
    )
}
```

If this compiles and runs without errors, you're all set! 🎉

---

## Next Steps

- [Quick Start Guide](quick-start.md) - Create your first chart
- [Configuration](configuration.md) - Learn about chart configuration options
- [Chart Overview](../charts/overview.md) - Explore all available charts

---

## Troubleshooting

### Common Issues

#### Compose not found

Make sure you have Compose Multiplatform plugin in your project:

```kotlin
plugins {
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
```

#### Version conflicts

If you encounter version conflicts, check that your Kotlin, Compose, and Charty versions are compatible.

#### Build errors on iOS

Ensure you have Xcode and command-line tools properly installed:

```bash
xcode-select --install
```

---

Need help? [Open an issue on GitHub](https://github.com/hi-manshu/charty/issues)

