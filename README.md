# 🎯 Charty — the charting library for Compose Multiplatform

![Banner](img/banner.png)

> A sleek, lightweight charting library for Jetpack Compose — and, uniquely, for **Kotlin Multiplatform**: one codebase renders your charts on **Android, iOS, Desktop, and the Web**.
> *Built with ❤️ by [@hi-manshu](https://github.com/hi-manshu).*

[![Github Followers](https://img.shields.io/github/followers/hi-manshu?label=Follow&style=social)](https://github.com/hi-manshu)
[![Twitter Follow](https://img.shields.io/twitter/follow/hi_man_shoe?label=Follow&style=social)](https://twitter.com/hi_man_shoe)
[![AndroidWeekly](https://img.shields.io/badge/Featured%20in%20androidweekly.net-%23532-orange)](https://androidweekly.net/issues/issue-532)
![GitHub Repo stars](https://img.shields.io/github/stars/hi-manshu/charty)
![Charty](https://img.shields.io/maven-central/v/com.himanshoe/charty?color=f4c430&label=Maven%20Central)
![Charty Static Check](https://github.com/hi-manshu/charty/actions/workflows/static-check.yml/badge.svg)

---

## ▶️ Try it live in your browser

**[Open the interactive playground →](https://hi-manshu.github.io/charty/)** — tweak the data and every config option and watch ~15 chart families re-render instantly. No install, no emulator. It's the same Compose code running in your browser via WebAssembly.

> _Deploying? Enable **Settings → Pages → Source: GitHub Actions** and the `Deploy playground` workflow publishes it on every push._

<!-- Tip: drop short GIFs of the interactions into img/ and embed them here. -->

---

## ✨ Why Charty

- 🌍 **Truly multiplatform.** Android (minSdk 24), iOS, JVM Desktop, and JS/Wasm in the browser — from a single `commonMain` API. Most Compose chart libraries are Android-only.
- 📊 **~25 chart types** out of the box, from bar/line/area to radar, candlestick, and calendar heatmaps.
- 👆 **Rich interactions** — a unified draggable **crosshair**, tap tooltips (canvas or your own Composable), persistent markers, zoom/pan, and brush selection.
- ⚡ **Scales to big data** — built-in **LTTB downsampling** keeps tens of thousands of points at interactive frame rates.
- ♿ **Accessible** — whole-chart descriptions **and** per-data-point screen-reader traversal, so TalkBack/VoiceOver users can inspect each value.
- 🎨 **Themeable** — light/dark-aware `ChartyTheme`, `ChartyColor` solids & gradients, graceful empty/loading states.
- 📦 **Ships an Android baseline profile** in the AAR, so consuming apps get Charty's hot paths AOT-compiled at install (no first-render jank).

---

## 🚀 Quick start

Charty is on **Maven Central** (`com.himanshoe:charty`). Use the latest version from the badge above.

**Kotlin Multiplatform** (`build.gradle.kts`):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.himanshoe:charty:<latest-version>")
        }
    }
}
```

**Android-only:**

```kotlin
dependencies {
    implementation("com.himanshoe:charty:<latest-version>")
}
```

Your first chart is three lines:

```kotlin
LineChart(
    data = { listOf(LineData("Mon", 20f), LineData("Tue", 45f), LineData("Wed", 30f)) },
    color = ChartyColor.Solid(ChartyColors.Blue),
)
```

Add interactions and styling declaratively:

```kotlin
LineChart(
    data = { priceData },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(
        interpolation = LineInterpolation.SMOOTH,
        downsampleThreshold = 800,          // stay smooth over huge series
    ),
    crosshair = ChartCrosshair(),            // draggable guide line + label
)
```

---

## 📈 Chart catalog

| Family | Charts |
| --- | --- |
| **Bar** | Bar, Horizontal, Stacked, Stacked-Horizontal, Grouped-Horizontal, Normalized-Horizontal, Mosaic, Span, Comparison, Waterfall, Lollipop, Bubble-bar, Wavy |
| **Line / Area** | Line, Area, Multiline, Stacked-Area |
| **Point** | Scatter (Point), Bubble |
| **Combo** | Bars + line, optional secondary axis |
| **Circular** | Pie, Donut, Radar, Multiple-Radar, Circular progress |
| **Specialised** | Candlestick (OHLC), Calendar heatmap, Block bar |

Explore every one in the **[live playground](https://hi-manshu.github.io/charty/)**.

---

## ⚡ Performance

Charty reduces large series to a shape-preserving subset with the **Largest-Triangle-Three-Buckets (LTTB)** algorithm before drawing, so a 50k-point line stays interactive. The pure compute paths are micro-benchmarked (`./gradlew :benchmark:mainBenchmark`):

| Operation | Cost |
| --- | --- |
| LTTB downsample, 50,000 → 800 points | ~0.16 ms |
| Axis min/max over 50,000 points | ~0.09 ms |
| 10,000 value→pixel transforms | ~0.07 ms |

_Measured on a dev machine (JMH average time); treat as orders of magnitude, not guarantees._ Enable it per chart with `downsampleThreshold`.

---

## ♿ Accessibility

Every Cartesian chart exposes a generated whole-chart `contentDescription`, and — because charts are canvas-drawn — an overlay of invisible, focusable nodes so screen readers can **traverse the data point by point** ("Point 2 of 5: Feb, 45"). The nodes carry no pointer input, so they never change the chart's look or block touch. Opt in via `ChartScaffold`'s `dataPointDescriptions` (already wired into Line, Area, Bar, and Point).

---

## 🔍 How Charty compares

| | **Charty** | Vico | MPAndroidChart |
| --- | --- | --- | --- |
| Android | ✅ | ✅ | ✅ |
| iOS / Desktop / **Web** | ✅ | ❌ | ❌ |
| Jetpack Compose–native | ✅ | ✅ | ❌ (Views) |
| Live in-browser demo | ✅ | ❌ | ❌ |
| Large-data downsampling | ✅ (LTTB) | partial | ❌ |
| Per-point screen-reader traversal | ✅ | ❌ | limited |
| Ships a baseline profile | ✅ | ❌ | ❌ |

_Vico and MPAndroidChart are excellent, mature libraries on Android — Charty's edge is being the Compose-native option that runs everywhere Kotlin does._

---

## 📚 Documentation

Full API reference and guides: 👉 **[himanshoe.com/docs/charty](https://himanshoe.com/docs/charty)**

---

## 🚀 Apps using Charty

| App | Link |
| --- | --- |
| 🐱 **NEKO** | [github.com/nekomangaorg/Neko](https://github.com/nekomangaorg/Neko) |
| ⏱️ **TimePlanner** | [github.com/v1tzor/TimePlanner](https://github.com/v1tzor/TimePlanner) |

Using Charty? Open a PR to add your app.

---

## ⚖️ License

```text
Copyright 2025 The Charty Authors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 🌟 Support

If Charty helps you, please ⭐ the repo — it genuinely helps.

## ☕ Sponsor

If Charty saves you time, consider buying me a coffee!

[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-FFDD00?style=flat&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/himanshoe)
