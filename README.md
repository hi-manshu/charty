# Charty

![Banner](img/banner.png)

> A sleek, lightweight charting library for Jetpack Compose — and, uniquely, for **Kotlin Multiplatform**: one codebase renders your charts on **Android, iOS, Desktop, and the Web**.
> *Built with ❤️ by [@hi-manshu](https://github.com/hi-manshu).*

[![Charty](https://img.shields.io/maven-central/v/com.himanshoe/charty?color=f4c430&label=Maven%20Central)](https://central.sonatype.com/artifact/com.himanshoe/charty)
![Charty Static Check](https://github.com/hi-manshu/charty/actions/workflows/static-check.yml/badge.svg)
![GitHub Repo stars](https://img.shields.io/github/stars/hi-manshu/charty)
[![AndroidWeekly](https://img.shields.io/badge/Featured%20in%20androidweekly.net-%23532-orange)](https://androidweekly.net/issues/issue-532)
[![Twitter Follow](https://img.shields.io/twitter/follow/hi_man_shoe?label=Follow&style=social)](https://twitter.com/hi_man_shoe)

### 📖 **[Documentation](https://hi-manshu.github.io/charty/)**  ·  ▶️ **[Playground](https://hi-manshu.github.io/charty/playground/)**  ·  📝 **[Changelog](CHANGELOG.md)**

Every chart has a page with a picture, the code that produced it, and its full configuration. The
playground runs all 35 of them in your browser, every option exposed as a control you can turn.

---

## Install

```kotlin
commonMain.dependencies {
    implementation("com.himanshoe:charty:3.0.0")
}
```

Projected 3D charts are a separate artifact on its own version, and every declaration in it is
`@ChartyExperimental` — see [charty-3d](https://hi-manshu.github.io/charty/charts/3d/) for the opt-in,
and for an honest account of what tilting a chart costs you.

```kotlin
implementation("com.himanshoe:charty-3d:1.0.0")
```

## Your first chart

```kotlin
LineChart(
    data = { listOf(LineData("Mon", 20f), LineData("Tue", 45f), LineData("Wed", 30f)) },
    color = ChartyColor.Solid(ChartyColors.Blue),
)
```

Then add interactions and styling declaratively:

```kotlin
LineChart(
    data = { priceData },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(
        interpolation = LineInterpolation.SMOOTH,
        downsampleThreshold = 800,          // stays smooth over huge series
    ),
    crosshair = ChartCrosshair(),            // draggable guide line and label
)
```

👉 **[Quick start](https://hi-manshu.github.io/charty/getting-started/quick-start.html)**

## What you get

- **35 chart types** — bar and its dozen variants, line, area, scatter, bubble, combo, pie, radar,
  gauges, candlestick, calendar and matrix heatmaps, Gantt, funnel, and projected 3D.
- **Five targets from one API** — Android (minSdk 24), iOS, JVM desktop, JavaScript and WebAssembly.
- **[Live data](https://hi-manshu.github.io/charty/guides/streaming.html)** — a rolling window that
  slides as points arrive, scrollback through history, and a jump-to-latest pill.
- **[Interactions](https://hi-manshu.github.io/charty/configurations/interactions.html)** — a
  draggable crosshair that can [sync across charts](https://hi-manshu.github.io/charty/guides/synced-crosshair.html),
  tooltips drawn as your own Composable, persistent markers, zoom, pan, and brush selection.
- **[Theming](https://hi-manshu.github.io/charty/customization/theming.html)** — one `ChartyTheme`
  carrying typography, colours, shapes and dimensions that every chart resolves its config against.
- **[PNG export](https://hi-manshu.github.io/charty/guides/exporting-charts.html)**, LTTB
  downsampling for large series, per-point screen-reader traversal, and an Android baseline profile
  shipped in the AAR.

---

## Apps using Charty

| App | Link |
| --- | --- |
| 🐱 **NEKO** | [github.com/nekomangaorg/Neko](https://github.com/nekomangaorg/Neko) |
| ⏱️ **TimePlanner** | [github.com/v1tzor/TimePlanner](https://github.com/v1tzor/TimePlanner) |

Using Charty? Open a PR to add your app.

## Contributing

The documentation site is generated from the markdown in [`docs/charty`](docs/charty) — edit the page,
not the HTML. `./gradlew :docsite:assembleSite` builds the whole site locally and fails on a dead link.

## Support

If Charty helps you, please ⭐ the repo — it genuinely helps.

[![Sponsor](https://img.shields.io/badge/Sponsor-%E2%9D%A4-db61a2?logo=github)](https://github.com/sponsors/hi-manshu)
[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-FFDD00?style=flat&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/himanshoe)

## License

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
