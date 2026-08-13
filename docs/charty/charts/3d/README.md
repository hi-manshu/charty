# charty-3d

Projected three-dimensional charts, published as a **separate artifact** so a project that does not
want them does not carry them.

```kotlin
implementation("com.himanshoe:charty:<version>")
implementation("com.himanshoe:charty-3d:<version>")
```

Both artifacts are released together at the same version, so `charty-3d` can never resolve against a
`charty` it was not built for.

## Opting in

Every public declaration here carries `@ChartyExperimental`. The API is still finding its shape and
may change in a minor release without the deprecation cycle the rest of Charty follows, so the
compiler makes you say you accept that:

```kotlin
@OptIn(ChartyExperimental::class)
@Composable
fun Dashboard() {
    Bar3DChart(data = { bars })
}
```

Or once for a whole module, in its `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets.all {
        languageSettings.optIn("com.himanshoe.charty.common.annotation.ChartyExperimental")
    }
}
```

## What "3D" means here

These are **projected**, not rendered. There is no GPU scene, no depth buffer and no lighting model.
Shapes are built as solids, their visible faces are worked out from the viewing angle, and the nearer
ones are painted over the further ones. That is enough to read as three-dimensional on every platform
Compose runs on, and it costs nothing beyond the `Canvas` a flat chart already uses.

## Read this before choosing one

Depth in both of these charts is **decoration — it carries no data**. That has real costs:

- A tall bar in front can hide a shorter one behind it, and no viewing angle removes that for every
  dataset.
- A tilted pie shows the near slices' walls, so they present more ink than slices of the same value
  at the back. The chart cannot correct for it; it is what the projection does.
- With `perspective` above `0f`, two values that are equal draw at different lengths depending on
  where they sit.

Use these when the figure is the point — a dashboard, a slide, a headline number — and the exact
values are read from labels or a legend. Use [`BarChart`](../bar/BarChart.md) or
[`PieChart`](../radial/PieChart.md) when the comparison is the point. Both defaults here are
deliberately shallow and parallel, so the comparison stays as honest as a projection allows unless
you ask for more.

## The charts

| Chart | What it is |
| --- | --- |
| [Bar3DChart](Bar3DChart.md) | Bars extruded into solids on a floor |
| [Pie3DChart](Pie3DChart.md) | A pie or ring given thickness |

## Projection3D

Both charts take the same viewing angle type.

```kotlin
Projection3D(
    pitch: Float = 18f,        // degrees; positive tips the far edge up — looking down at it
    yaw: Float = 14f,          // degrees; positive swings the right-hand side toward you
    depth: Float = 0.45f,      // reference distance for perspective, as a fraction of the plot
    perspective: Float = 0f,   // 0f parallel, 1f strongly convergent
)
```

Presets:

| Preset | Angle | Perspective |
| --- | --- | --- |
| `Default` | pitch 18°, yaw 14° | none — values stay comparable |
| `Isometric` | pitch 30°, yaw 45° | none |
| `Subtle` | pitch 8°, yaw 8° | none — depth as a hint |
| `Dramatic` | pitch 34°, yaw 28° | 0.6 — photographic, hardest to compare |

`perspective` is the one setting that changes what the reader can conclude. At `0f` a parallel
projection keeps equal lengths equal wherever they sit; above it, they converge. Three of the four
presets are parallel for that reason.

## Interaction

Both charts take a click listener, and both resolve it **nearest face first** — a tap always selects
the shape you can see, never one occluded behind it.

## Limitations

- No axes, grid, tooltip slot, crosshair, streaming window, or viewport pan/zoom. These charts draw
  on a plain `Canvas` rather than through `ChartScaffold`, so the shared Cartesian machinery does not
  apply to them.
- No accessibility traversal per data point yet.
