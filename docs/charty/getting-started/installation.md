# Installation

Charty is published to Maven Central. Add the single dependency to your shared
`commonMain` source set — no platform-specific additions are required.

## Requirements

| Tool | Minimum version |
|------|----------------|
| Kotlin | 1.9+ |
| Compose Multiplatform | 1.6+ |
| Android `minSdk` | 24 |

## Gradle dependency (Kotlin DSL)

Replace `<version>` with the latest release. You can always look up the current
version from the Maven Central badge:

[![Maven Central](https://img.shields.io/maven-central/v/com.himanshoe/charty)](https://central.sonatype.com/artifact/com.himanshoe/charty)

Badge image URL for your own README:
```
https://img.shields.io/maven-central/v/com.himanshoe/charty
```

### Multiplatform project (`build.gradle.kts`)

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.himanshoe:charty:<version>")
        }
    }
}
```

### Android-only project (`build.gradle.kts`)

```kotlin
dependencies {
    implementation("com.himanshoe:charty:<version>")
}
```

## Supported platforms

The same artifact supports all five targets out of the box — nothing extra to
configure per platform:

| Platform | Target |
|----------|--------|
| Android | `minSdk 24` |
| iOS (device) | `iosArm64` |
| iOS (simulator) | `iosSimulatorArm64` |
| JVM Desktop | `jvm` |
| JS browser | `js` |
| WasmJS browser | `wasmJs` |

## Permissions and manifest entries

No Android permissions, manifest entries, or `ProGuard`/`R8` rules are required.
The library is pure Kotlin + Compose — it performs no I/O, camera, network, or
storage access.

## Verifying the installation

After syncing, import any chart composable from `commonMain` to confirm the
dependency resolved correctly:

```kotlin
import com.himanshoe.charty.bar.BarChart
```

If the import resolves, you are ready to render your first chart.
