# Theming

Every Charty chart reads its styling from an ambient `ChartyTheme`. Set one once, near the top of your app, and every chart below it picks up your design system — series palette, typography, tooltip and crosshair styling, corner shapes, and overlay metrics — without you restating anything on individual charts.

Nothing is required. With no theme in place charts use built-in defaults that follow the system light/dark setting, and adopting a theme never changes rendering on its own: the defaults reproduce exactly what Charty drew before.

---

## The shape of a theme

```kotlin
ChartyTheme(
    palette: List<ChartyColor>,          // multi-series colors, cycled per series
    primaryColor: ChartyColor,           // single-series color
    axisColor: ChartyColor,
    gridColor: ChartyColor,
    typography: ChartyTypography,        // 9 text roles
    componentColors: ChartyComponentColors, // 26 color roles
    labelTextStyle: TextStyle,           // resolved axis-label style
    shapes: ChartyShapes,                // corner treatments
    dimensions: ChartyDimensions,        // 27 sizes and spacings
)
```

Each holder has sensible defaults, so you override one area without restating the others.

**Every color in the theme is a `ChartyColor`**, so a gradient is accepted anywhere a solid color is — including text. See [Colors and Animations](colors-and-animations.md).

---

## Providing a theme

Wrap your charts in `ChartyThemeProvider`:

```kotlin
ChartyThemeProvider(theme = ChartyTheme.dark()) {
    LineChart(data = { points })
    BarChart(data = { bars })
}
```

`ChartyTheme.light()` and `ChartyTheme.dark()` are the two presets. Without a provider, charts resolve one of these from `isSystemInDarkTheme()`, so axes and labels stay legible on a dark background out of the box.

To read the theme yourself — for a legend or a heading you draw next to a chart:

```kotlin
val theme = currentChartyTheme
Text(text = "Revenue", style = theme.typography.axisLabel)
```

---

## Mapping a design system

Charty makes no assumptions about where your styling comes from. Mapping Material 3 takes a few obvious lines:

```kotlin
ChartyThemeProvider(
    theme =
        ChartyTheme(
            palette = brand.series.map { ChartyColor.Solid(it) },
            primaryColor = ChartyColor.Solid(MaterialTheme.colorScheme.primary),
            axisColor = ChartyColor.Solid(MaterialTheme.colorScheme.outline),
            gridColor = ChartyColor.Solid(MaterialTheme.colorScheme.outlineVariant),
            typography =
                ChartyTypography(
                    axisLabel = MaterialTheme.typography.labelSmall,
                    tooltipLabel = MaterialTheme.typography.bodySmall,
                ),
            componentColors =
                ChartyComponentColors(
                    tooltipBackground = ChartyColor.Solid(MaterialTheme.colorScheme.inverseSurface),
                    tooltipText = ChartyColor.Solid(MaterialTheme.colorScheme.inverseOnSurface),
                ),
            shapes = ChartyShapes(tooltip = MaterialTheme.shapes.small),
        ),
) {
    BarChart(data = { bars })
}
```

The same mapping works from any source — your own tokens, a JSON design-token file, whatever you already have. Charty only asks for `ChartyColor`, `TextStyle`, `Shape`, `Dp`, and `Float`.

---

## Typography roles

`ChartyTypography` names every piece of text Charty draws, so you can point each at the right style in your type scale.

| Role | Where it is drawn |
| --- | --- |
| `axisLabel` | Tick labels on both axes |
| `dataLabel` | Values printed on bars, points, and slices |
| `tooltipLabel` | Text inside the tooltip bubble |
| `legendLabel` | Series names in `ChartLegend` |
| `markerLabel` | The callout pill of a `PersistentMarker` |
| `crosshairLabel` | The crosshair's value pill |
| `annotationLabel` | Annotation callouts |
| `referenceLineLabel` | The label on a reference line |
| `referenceBandLabel` | The label on a reference band |

---

## Component colors

`ChartyComponentColors` covers 26 roles across tooltips, the crosshair, markers, annotations, brush selection, reference lines and bands, the scroll-edge scrim, and label text. Roles typed `ChartyColor?` are opt-in: leaving them `null` keeps an inherited value rather than forcing you to pick one.

```kotlin
ChartyComponentColors(
    tooltipBackground = ChartyColor.Gradient(listOf(Color(0xFF1E88E5), Color(0xFF6A1B9A))),
    tooltipText = ChartyColor.Solid(Color.White),
    tooltipBorder = null,                    // no border
    crosshairLabelBackground = null,         // inherits primaryColor
)
```

Two roles resolve through the theme rather than standing alone:

- `crosshairLabelBackground` falls back to `primaryColor`.
- `crosshairLabelText` falls back to `axisLabelText`.

`ChartyComponentColors.light()` and `.dark()` give you the presets to start from.

---

## Shapes and dimensions

`ChartyShapes` carries the corner treatments of Charty's floating surfaces (`tooltip`, `tooltipCornerRadius`, `crosshairLabel`, `markerLabelCornerRadius`, `annotationLabelCornerRadius`).

`ChartyDimensions` carries 27 sizes and spacings — tooltip padding, elevation, arrow size, crosshair line width and dot radius, marker geometry, and so on. Override a handful; the rest keep their defaults.

```kotlin
ChartyTheme(
    shapes = ChartyShapes(tooltip = RoundedCornerShape(12.dp), tooltipCornerRadius = 12.dp),
    dimensions = ChartyDimensions(crosshairLineWidth = 2f, crosshairDotRadius = 5f),
)
```

---

## How a chart resolves its styling

Config properties that the theme can supply are declared nullable, and `null` means *take the theme's*:

```kotlin
BarChart(
    data = { bars },
    barConfig = BarChartConfig(),                      // tooltip styled by the theme
)

BarChart(
    data = { bars },
    barConfig = BarChartConfig(tooltipConfig = myTooltip),  // your styling, theme ignored
)
```

This applies to `tooltipConfig` on every chart that has one, and to `ChartCrosshair.config`:

```kotlin
crosshair = ChartCrosshair()                       // guide line, dot, and pill from the theme
crosshair = ChartCrosshair(config = myCrosshair)   // your styling
```

An explicit value always wins over the theme. A chart's own `color` parameter defaults to `ChartyThemeDefaults.primaryColor()`, and multi-series charts cycle `ChartyThemeDefaults.seriesColor(index)`, so passing nothing gets you the themed palette.

`ChartyThemeDefaults` exposes each of these as a composable factory if you want to build a config from theme values and then tweak one field:

```kotlin
val tooltip = ChartyThemeDefaults.tooltipConfig().copy(showArrow = false)
```

Available factories: `scaffoldConfig()`, `primaryColor()`, `seriesColor(index)`, `dataLabelStyle()`, `legendLabelStyle()`, `tooltipConfig(showArrow)`, `crosshairConfig()`, `markerStyle(dataIndex, label)`, `annotationStyle()`, `brushSelectionStyle()`, and `referenceLineConfig()`.

---

## Light and dark together

Because the theme is read through composition, switching it re-styles every chart at once:

```kotlin
val theme = if (isSystemInDarkTheme()) ChartyTheme.dark() else ChartyTheme.light()

ChartyThemeProvider(theme = theme) {
    Dashboard()
}
```

If you never provide a theme, this is exactly what Charty does for you.
