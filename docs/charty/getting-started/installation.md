# Installation

Charty is published to Maven Central. Add the single dependency to your shared
`commonMain` source set — no platform-specific additions are required.

## Requirements

| Tool | Version |
|------|---------|
| Kotlin | 2.4.0 (the version Charty is built with) |
| Compose Multiplatform | 1.11.1 |
| Android `minSdk` | 24 |

Charty is compiled against these versions. Kotlin metadata is not forward-compatible,
so a consuming project on an older Kotlin or Compose Multiplatform release may fail
to resolve the artifact — match these versions or newer.

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

No Android permissions, manifest entries, or `ProGuard`/`R8` rules are required to
render charts. Charty performs no network or camera access.

The one exception is **[PNG export](../guides/exporting-charts.md)**, which writes a
file when you call it. It still needs no permission: on Android it writes into your
app's own cache directory, on desktop into the user's `Downloads` folder, and in the
browser it triggers a normal download. If you want to *share* the exported image on
Android, your app must declare its own `FileProvider` — see that guide for why.

## Verifying the installation

After syncing, import any chart composable from `commonMain` to confirm the
dependency resolved correctly:

```kotlin
import com.himanshoe.charty.bar.BarChart
```

If the import resolves, you are ready to render your first chart.
