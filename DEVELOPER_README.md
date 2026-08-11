# Charty — Developer Guide

> Internal reference for contributors and maintainers.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Module Structure](#module-structure)
- [Supported Platforms](#supported-platforms)
- [Chart Inventory](#chart-inventory)
- [Key Abstractions](#key-abstractions)
- [How to Add a New Chart](#how-to-add-a-new-chart)
- [KDoc Guidelines](#kdoc-guidelines)
- [Build & Tooling](#build--tooling)
- [Compose Stability](#compose-stability)
- [Publishing a Release](#publishing-a-release)
- [What Can Be Done Next](#what-can-be-done-next)

---

## Architecture Overview

Charty is a **Kotlin Multiplatform (KMP)** chart library built on **Compose Multiplatform**. All chart rendering happens via Compose Canvas — there are no native drawing dependencies, so the same Kotlin code runs identically on Android, iOS, Desktop (JVM), Web (JS), and WebAssembly.

### Rendering Pipeline

```
Consumer calls ChartComposable(data = { ... })
        │
        ▼
  data lambda is remembered → List<ChartData>
        │
        ▼
  ChartScaffold  ← ChartScaffoldConfig (axes, grid, labels)
        │
        ├── DrawAxisAndLabels (Composable overlay)
        │
        └── Canvas { chartContext →
                  chart-specific draw functions (internal)
            }
```

`ChartScaffold` is the shared layout container for all Cartesian charts. It calculates a `ChartContext` — a value object holding pixel boundaries and the data value range — and passes it to the chart's drawing lambda. Non-Cartesian charts (Pie, Radar, Circular) draw directly on a `Canvas` without `ChartScaffold`.

### Interaction Model

Click / tap interactions are implemented as `Modifier` extensions that use `pointerInput` internally. Each chart builds its "hit bounds" list (e.g. `List<Pair<Rect, BarData>>`) and stores it in a `rememberTooltipManager()` holder. On tap, the matching bound is found and a `TooltipState` is emitted, which the chart's draw loop reads to render the tooltip in the next frame.

---

## Module Structure

```
chartyv3/
├── charty/                          # Library module (published to Maven Central)
│   └── src/commonMain/kotlin/com/himanshoe/charty/
│       ├── bar/                     # BarChart, HorizontalBarChart, StackedBarChart,
│       │   │                        # StackedHorizontalBarChart, GroupedHorizontalBarChart,
│       │   │                        # NormalizedHorizontalBarChart, MosaicBarChart, SpanChart,
│       │   │                        # WavyChart, BubbleBarChart, ComparisonBarChart,
│       │   │                        # LollipopBarChart, WaterfallChart
│       │   ├── config/              # BarChartConfig, StackedBarChartConfig,
│       │   │                        # StackedHorizontalBarChartConfig,
│       │   │                        # GroupedHorizontalBarChartConfig,
│       │   │                        # NormalizedHorizontalBarChartConfig,
│       │   │                        # WavyChartConfig, …
│       │   ├── data/                # BarData, BarGroup, SpanData
│       │   └── internal/            # Private drawing helpers (NOT public API)
│       ├── block/                   # BlockBar
│       ├── calendar/                # CalendarHeatmapChart
│       ├── candlestick/             # CandlestickChart
│       ├── circular/                # CircularProgressIndicator
│       ├── color/                   # ChartyColor (Solid / Gradient), ChartyColors palette
│       ├── combo/                   # ComboChart (bar + line)
│       ├── common/                  # ChartContext, ChartScaffold, ChartOrientation,
│       │   │                        # ChartLegend
│       │   ├── accessibility/       # ChartAccessibility — screen-reader description generators
│       │   ├── animation/           # rememberChartAnimation
│       │   ├── axis/                # AxisConfig, DrawAxisAndLabels, LabelRotation
│       │   ├── config/              # Animation, ChartScaffoldConfig, ReferenceLineConfig,
│       │   │                        # CornerRadius, ChartInteractionConfig
│       │   ├── constants/           # ChartConstants (shared magic numbers)
│       │   ├── data/                # ChartDataPoint, getLabels/getValues helpers
│       │   ├── draw/                # ReferenceLineDrawer, ChartDrawUtils
│       │   ├── ext/                 # DrawScopeExtensions
│       │   ├── gesture/             # GestureUtils, ChartGestureModifiers,
│       │   │                        # ChartCrosshairConfig, CrosshairManager
│       │   ├── tooltip/             # TooltipConfig, TooltipDrawer, TooltipManager
│       │   ├── viewport/            # ViewPortState (zoom/pan)
│       │   └── util/                # ValueCalculations
│       ├── line/                    # LineChart, AreaChart, MultilineChart,
│       │   │                        # StackedAreaChart
│       │   ├── config/              # LineChartConfig
│       │   ├── data/                # LineData, LineGroup, StackedAreaPoint, …
│       │   ├── ext/                 # LineChartExt, AreaChartExtensions
│       │   └── internal/            # Private drawing helpers
│       ├── pie/                     # PieChart (Pie & Donut styles)
│       ├── point/                   # PointChart (scatter), BubbleChart
│       └── radar/                   # RadarChart, MultipleRadarChart
│
├── composeApp/                      # Sample / demo application
│   └── src/commonMain/              # Demo screens exercising every chart type
│
├── config/detekt/detekt.yml         # Static analysis rules
├── gradle/libs.versions.toml        # Version catalog
└── .github/workflows/               # CI: compose stability check, manual release
```

---

## Supported Platforms

| Platform | Target | Notes |
|---|---|---|
| Android | `androidTarget` | Min SDK 24 |
| iOS (device) | `iosArm64` | Static framework |
| iOS (simulator) | `iosSimulatorArm64` | Static framework |
| Desktop (JVM) | `jvm` | Swing backend |
| Web (JS) | `js { browser() }` | |
| Web (Wasm) | `wasmJs { browser() }` | Experimental |

---

## Chart Inventory

### Cartesian Charts (use `ChartScaffold`)

| Composable | File | Description |
|---|---|---|
| `BarChart` | `bar/BarChart.kt` | Vertical bar chart; negative values, data labels, tooltips |
| `HorizontalBarChart` | `bar/HorizontalBarChart.kt` | Horizontal bar chart; negative values, data labels |
| `StackedBarChart` | `bar/StackedBarChart.kt` | Stacked vertical bars (absolute values); data labels |
| `StackedHorizontalBarChart` | `bar/StackedHorizontalBarChart.kt` | Stacked horizontal bars; horizontal counterpart to `StackedBarChart` |
| `GroupedHorizontalBarChart` | `bar/GroupedHorizontalBarChart.kt` | Side-by-side horizontal bars per row; full negative/positive support |
| `NormalizedHorizontalBarChart` | `bar/NormalizedHorizontalBarChart.kt` | 100%-normalised horizontal bars; horizontal counterpart to `MosaicBarChart` |
| `MosaicBarChart` | `bar/MosaicBarChart.kt` | 100%-normalised stacked vertical bar chart |
| `SpanChart` | `bar/SpanChart.kt` | Horizontal range / Gantt-style bars |
| `WavyChart` | `bar/WavyChart.kt` | Animated sine-wave bars; crosshair support |
| `BubbleBarChart` | `bar/BubbleBarChart.kt` | Bar chart with bubble markers at data points |
| `ComparisonBarChart` | `bar/ComparisonBarChart.kt` | Back-to-back bars for direct comparison |
| `LollipopBarChart` | `bar/LollipopBarChart.kt` | Stem + circle (lollipop) chart |
| `WaterfallChart` | `bar/WaterfallChart.kt` | Cumulative running-total with positive/negative segments |
| `LineChart` | `line/LineChart.kt` | Single-series line; smooth/straight, crosshair |
| `AreaChart` | `line/AreaChart.kt` | Filled area below the line |
| `MultilineChart` | `line/MultilineChart.kt` | Multiple line series; per-series color, gradient fill, legend, crosshair |
| `StackedAreaChart` | `line/StackedAreaChart.kt` | Stacked filled area series; legend, crosshair |
| `ComboChart` | `combo/ComboChart.kt` | Bars + line overlay on shared y-axis; crosshair |
| `PointChart` | `point/PointChart.kt` | Scatter / dot plot; crosshair |
| `BubbleChart` | `point/BubbleChart.kt` | Scatter with size-encoded third dimension; crosshair |
| `CandlestickChart` | `candlestick/CandlestickChart.kt` | OHLC candlestick chart for financial data |
| `CalendarHeatmapChart` | `calendar/CalendarHeatmapChart.kt` | GitHub-style contribution heatmap grid |

### Non-Cartesian Charts

| Composable | File | Description |
|---|---|---|
| `PieChart` | `pie/PieChart.kt` | Pie chart with optional donut style, legends, click |
| `RadarChart` | `radar/RadarChart.kt` | Single-dataset spider/web chart |
| `MultipleRadarChart` | `radar/MultipleRadarChart.kt` | Overlapping radar datasets with legend |
| `CircularProgressIndicator` | `circular/CircularProgressIndicator.kt` | Concentric activity rings |
| `BlockBar` | `block/BlockBar.kt` | Horizontal segmented proportion bar |

---

## Key Abstractions

### `ChartContext`

```kotlin
data class ChartContext(
    val left: Float, val top: Float, val right: Float, val bottom: Float,
    val minValue: Float, val maxValue: Float,
) {
    val width: Float get() = right - left   // computed — do not pass explicitly
    val height: Float get() = bottom - top  // computed — do not pass explicitly
}
```

Utility functions:
- `convertValueToYPosition(value)` — maps a data value to a canvas y-coordinate
- `calculateBarLeftPosition(index, totalBars, widthFraction)` — left edge of a bar
- `calculateBarWidth(totalBars, widthFraction)` — bar width in pixels
- `calculateCenteredXPosition(index, totalItems)` — centred x for a point/label
- `ChartyColor.toVerticalGradientBrush()` — extension that creates a `Brush`

### `ChartyColor`

A sealed class with two variants:

```kotlin
ChartyColor.Solid(color: Color)          // single colour
ChartyColor.Gradient(colors: List<Color>) // list of colours
```

All chart composables accept a `ChartyColor`. For charts that draw a single element (bar, line), `Solid` is the default. For charts that distinguish multiple series or items, `Gradient` is preferred.

### `Animation`

```kotlin
sealed interface Animation {
    data object Disabled : Animation
    data class Enabled(val duration: Int = 800) : Animation
    companion object {
        val Default = Enabled()
        val Fast    = Enabled(400)
        val Slow    = Enabled(1200)
    }
}
```

Pass to any chart config's `animation` property. Internally, `rememberChartAnimation(animation)` returns an `Animatable<Float>` state that charts use to scale/fade their draw calls.

### `ChartScaffoldConfig`

Controls the shared visual shell (axes, grid, labels) around Cartesian charts.

```kotlin
data class ChartScaffoldConfig(
    val showAxis: Boolean = true,
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val axisColor: Color = Color.Black,
    val gridColor: Color = Color.LightGray,
    val axisThickness: Float = 2f,
    val gridThickness: Float = 1f,
    val labelTextStyle: TextStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
    val leftLabelRotation: LabelRotation = LabelRotation.Straight,
)
```

### `ReferenceLineConfig`

A chart-agnostic overlay for drawing horizontal or vertical reference/target lines. Supported by `BarChart`, `HorizontalBarChart`, `StackedBarChart`, `StackedHorizontalBarChart`, `GroupedHorizontalBarChart`, `LineChart`, `AreaChart`, `PointChart`, `ComboChart`.

---

## How to Add a New Chart

Follow these steps to add a hypothetical `GaugeChart` as an example.
The code snippets below use `CandlestickChart` naming for illustration — refer to
the real `candlestick/` package for a complete working implementation.

### 1. Create the data class

```kotlin
// charty/src/commonMain/kotlin/com/himanshoe/charty/candlestick/data/CandlestickData.kt
package com.himanshoe.charty.candlestick.data

/**
 * Represents a single OHLC candle.
 *
 * @property label The x-axis label (e.g. date string).
 * @property open Opening price.
 * @property high Highest price.
 * @property low Lowest price.
 * @property close Closing price.
 */
data class CandlestickData(
    val label: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
) : com.himanshoe.charty.common.data.ChartDataPoint {
    override val value: Float get() = close
    override val xLabel: String get() = label
}
```

### 2. Create the config class

```kotlin
// charty/src/commonMain/kotlin/com/himanshoe/charty/candlestick/config/CandlestickChartConfig.kt
package com.himanshoe.charty.candlestick.config

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.common.config.Animation

/**
 * Configuration for [CandlestickChart] appearance.
 *
 * @property bullishColor Color for candles where close > open.
 * @property bearishColor Color for candles where close <= open.
 * @property wickWidth Width of the high/low wick in pixels.
 * @property bodyWidthFraction Fraction of available slot width the candle body occupies.
 * @property animation Entry animation configuration.
 */
data class CandlestickChartConfig(
    val bullishColor: Color = Color(0xFF4CAF50),
    val bearishColor: Color = Color(0xFFF44336),
    val wickWidth: Float = 2f,
    val bodyWidthFraction: Float = 0.5f,
    val animation: Animation = Animation.Default,
)
```

### 3. Create internal draw helpers

```kotlin
// charty/src/commonMain/kotlin/com/himanshoe/charty/candlestick/internal/CandlestickDrawer.kt
package com.himanshoe.charty.candlestick.internal

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandlestickData
import com.himanshoe.charty.common.ChartContext

internal fun DrawScope.drawCandlesticks(
    dataList: List<CandlestickData>,
    chartContext: ChartContext,
    config: CandlestickChartConfig,
    animationProgress: Float,
) {
    // drawing logic here
}
```

### 4. Create the public composable

```kotlin
// charty/src/commonMain/kotlin/com/himanshoe/charty/candlestick/CandlestickChart.kt
package com.himanshoe.charty.candlestick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandlestickData
import com.himanshoe.charty.candlestick.internal.drawCandlesticks
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * A composable function that displays a candlestick chart for financial OHLC data.
 *
 * @param data A lambda returning the list of [CandlestickData] to display.
 * @param modifier The modifier to be applied to the chart.
 * @param config Appearance configuration, defined by [CandlestickChartConfig].
 * @param scaffoldConfig Scaffold configuration (axes, grid, labels).
 *
 * Example usage:
 * ```kotlin
 * CandlestickChart(
 *     data = {
 *         listOf(
 *             CandlestickData("Mon", open = 100f, high = 120f, low = 95f, close = 110f),
 *             CandlestickData("Tue", open = 110f, high = 115f, low = 100f, close = 105f),
 *         )
 *     }
 * )
 * ```
 */
@Composable
fun CandlestickChart(
    data: () -> List<CandlestickData>,
    modifier: Modifier = Modifier,
    config: CandlestickChartConfig = CandlestickChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Candlestick chart data cannot be empty" }

    val animationProgress = rememberChartAnimation(config.animation)
    val allValues = dataList.flatMap { listOf(it.high, it.low) }

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig = AxisConfig(
            minValue = allValues.min(),
            maxValue = allValues.max(),
            steps = 6,
        ),
        config = scaffoldConfig,
    ) { chartContext ->
        drawCandlesticks(dataList, chartContext, config, animationProgress.value)
    }
}
```

### 5. Add to the sample app

Open `composeApp/src/commonMain/` and add a demo screen showcasing the new chart with realistic data.

### 6. Write KDoc and run checks

- Add a `@param` tag for every parameter.
- Wrap the code example in ` ```kotlin ` fences (see [KDoc Guidelines](#kdoc-guidelines)).
- Run `./gradlew ktlintFormat detekt` before opening a PR.

---

## KDoc Guidelines

All public API surfaces — composables, data classes, config classes, sealed classes, and their members — must have KDoc.

### Required Tags

| Tag | When to use |
|---|---|
| `@param name` | Every parameter of a composable or function |
| `@property name` | Every property of a `data class` or `sealed class` |
| `@return` | Non-unit functions where the return value is non-obvious |
| `@throws` | If the function can throw (rare in Charty) |

### Code Examples

Wrap all examples in ` ```kotlin ` fences inside the KDoc block:

```kotlin
/**
 * Short description of what the chart does.
 *
 * Longer description if needed.
 *
 * @param data ...
 * @param modifier ...
 *
 * Example usage:
 * ```kotlin
 * MyChart(
 *     data = { listOf(MyData("A", 10f)) },
 *     modifier = Modifier.fillMaxWidth().height(300.dp)
 * )
 * ```
 */
```

### Internal Helpers

Private and internal functions do not require KDoc but should have a single-line comment if the logic is non-trivial.

---

## Build & Tooling

### Prerequisites

- JDK 17+
- Android Studio Ladybug or newer (for Android targets)
- Xcode 16+ (for iOS targets, macOS only)

### Common Gradle Tasks

```bash
# Format code
./gradlew ktlintFormat

# Run static analysis
./gradlew detekt

# Build everything
./gradlew build

# Build only the library
./gradlew :charty:build

# Run unit tests
./gradlew :charty:allTests

# Generate Dokka HTML docs
./gradlew :charty:dokkaHtml
# Output: charty/build/dokka/html/

# Run the desktop sample app
./gradlew :composeApp:run

# Check Compose compiler metrics / stability
./gradlew :charty:compileKotlinJvm
# Reports: charty/build/compose_reports/
```

### Code Quality

| Tool | Config | Task |
|---|---|---|
| ktlint | `.editorconfig` | `./gradlew ktlintFormat` |
| detekt | `config/detekt/detekt.yml` | `./gradlew detekt` |
| Compose stability | CI workflow | `./gradlew :charty:compileKotlinJvm` |

The CI pipeline (`compose-stability-check.yml`) automatically analyses Compose stability on every push and PR.

---

## Compose Stability

All public chart composables and their parameter types must be **stable** to avoid unnecessary recompositions.

Rules:
- Data classes that have only `val` properties with stable types (primitives, `String`, `Color`) are automatically stable.
- Use `@Immutable` or `@Stable` annotations when the Compose compiler cannot infer stability (e.g. classes with `List<T>` properties).
- The `data` parameter of every chart is a **lambda** (`() -> List<T>`) — this is intentional. The lambda itself is stable; it captures the state from outside.
- Run `./scripts/check_stability_local.sh` for a local stability report before opening a PR.

---

## Publishing a Release

Releases are published to **Maven Central** via the GitHub Actions workflow `.github/workflows/manual-release.yml`.

### Required Environment Secrets

| Secret | Purpose |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Sonatype username |
| `MAVEN_CENTRAL_PASSWORD` | Sonatype password |
| `SIGNING_KEY_ID` | GPG key ID (last 8 chars) |
| `SIGNING_PASSWORD` | GPG key passphrase |
| `SIGNING_SECRET_KEY` | GPG secret key (base64-encoded) |
| `MAVEN_CENTRAL_VERIFICATION_TOKEN` | Maven Central portal token |

### Release Steps

1. Decide the new version number following [SemVer](https://semver.org/).
2. Trigger the manual release workflow from GitHub Actions:
   - Set `VERSION_NAME` to the new version (e.g. `3.1.0`).
3. The workflow will:
   - Generate `verification.properties` from the template.
   - Build and sign all artifacts.
   - Upload and auto-release to Maven Central.
4. After the release appears on Maven Central, tag the commit:
   ```bash
   git tag v3.1.0
   git push origin v3.1.0
   ```

### Local Publish (testing only)

```bash
VERSION_NAME=3.1.0-LOCAL ./gradlew :charty:publishToMavenLocal
```

---

## What Can Be Done Next

The following are concrete improvement areas and feature ideas, ordered roughly by impact.

### New Chart Types

| Chart | Notes |
|---|---|
| **Histogram** | Auto-bin continuous data into ranges |
| **Gauge / Dial** | Single-value arc (0–100%) |
| **Treemap** | Hierarchical area decomposition |
| **Funnel Chart** | Conversion funnel stages |
| **Gantt Chart** | Timeline bars with date/time axis |

### Enhancements to Existing Charts

- **Lambda `@Stable` fix** — config classes like `LineChartConfig` carry lambda properties (`tooltipFormatter`, `dataLabelFormatter`). Kotlin lambda equality is reference-based, so two identical-looking configs are never `==`, undermining `@Stable`. Fix: move default lambdas to top-level `val`s so callers share the same reference.
- **Dual Y-axis** — independent left and right value axes (useful for `ComboChart`).
- **Threshold Bands** — colour-coded horizontal regions (e.g. green/yellow/red zones).
- **Negative Stacked Bars** — `StackedBarChart` and `StackedHorizontalBarChart` currently require positive values; add `BELOW_AXIS` mode.
- **Log Scale Axis** — logarithmic y-axis option for charts with wide value ranges.
- **Date/Time X-Axis** — built-in `kotlinx-datetime` integration for time-series labels.
- **Gradient fill draw order** — `MultilineChart` with `showGradientFill = true` currently interleaves fill-N and line-N. A two-pass approach (all fills, then all lines) would prevent later fills from obscuring earlier lines.

### API & DX Improvements

- **`remember` helper functions** — e.g. `rememberBarData { ... }` wrappers to make state management more ergonomic.
- **Snapshot testing** — golden-image tests for each chart using Paparazzi (Android) or `compose-ui-test` snapshots.
- **Screenshot documentation** — auto-generate chart gallery from the sample app for the documentation site.
- **Compose Previews** — `@Preview` annotations on each chart with sample data for IDE previewing.

### Infrastructure

- **Unit test coverage** — add KotlinTest / Kotest tests for `ChartContext` calculations, `ValueCalculations`, and animation helpers.
- **Multiplatform screenshot tests** — integrate Roborazzi or a Compose Multiplatform screenshot tool.
- **Dokka multi-module site** — publish full API docs automatically on release via GitHub Pages.
- **Version catalog alignment** — align `libs.versions.toml` with the Kotlin and AGP BOM cadence.
- **Benchmark module** — a `macrobenchmark` module to track rendering performance regressions.

---

*For questions, open a [GitHub Discussion](https://github.com/hi-manshu/charty/discussions) or file an [issue](https://github.com/hi-manshu/charty/issues).*
