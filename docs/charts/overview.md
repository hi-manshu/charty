# Chart Overview

Charty provides a comprehensive collection of chart types for all your data visualization needs. This page provides an overview of all available charts.

---

## Bar Charts

Bar charts are perfect for comparing discrete values across categories.

### [Bar Chart](bar/bar-chart.md)
Standard vertical bar chart for comparing values.

```kotlin
BarChart(
    dataCollection = listOf(
        BarData(100f, "Jan"),
        BarData(200f, "Feb")
    )
)
```

### [Horizontal Bar Chart](bar/horizontal-bar-chart.md)
Horizontal orientation for better label readability.

### [Stacked Bar Chart](bar/stacked-bar-chart.md)
Display multiple data series stacked on top of each other.

### [Comparison Bar Chart](bar/comparison-bar-chart.md)
Compare multiple series side-by-side.

### [Lollipop Bar Chart](bar/lollipop-bar-chart.md)
Minimalist bar chart with lollipop-style markers.

### [Waterfall Chart](bar/waterfall-chart.md)
Show cumulative effect of sequential values.

### [Wavy Chart](bar/wavy-chart.md)
Bars with decorative wavy tops.

### [Bubble Bar Chart](bar/bubble-bar-chart.md)
Bars with bubble indicators for additional data dimension.

### [Mosaic Bar Chart](bar/mosaic-bar-chart.md)
Bars with mosaic/tile pattern styling.

### [Span Chart](bar/span-chart.md)
Visualize ranges or time spans.

---

## Line Charts

Line charts show trends and changes over continuous data.

### [Line Chart](line/line-chart.md)
Classic line chart connecting data points.

```kotlin
LineChart(
    dataCollection = listOf(
        LineData(10f, "Jan"),
        LineData(20f, "Feb")
    )
)
```

### [Area Chart](line/area-chart.md)
Line chart with filled area below the line.

### [Multiline Chart](line/multiline-chart.md)
Display multiple data series on the same chart.

### [Stacked Area Chart](line/stacked-area-chart.md)
Multiple data series stacked on top of each other.

---

## Point Charts

Point charts display individual data points, perfect for scatter plots and correlations.

### [Point Chart](point/point-chart.md)
Classic scatter plot for showing relationships.

```kotlin
PointChart(
    dataCollection = listOf(
        PointData(10f, 20f),
        PointData(15f, 35f)
    )
)
```

### [Bubble Chart](point/bubble-chart.md)
Scatter plot with variable-sized bubbles for a third data dimension.

---

## Circular Charts

### [Pie Chart](pie/pie-chart.md)
Display proportions as slices of a circle.

```kotlin
PieChart(
    dataCollection = listOf(
        PieData(30f, "Category A"),
        PieData(45f, "Category B")
    )
)
```

### [Circular Progress](circular/circular-progress.md)
Animated circular progress indicator.

---

## Radar Charts

Radar charts are ideal for comparing multiple variables.

### [Radar Chart](radar/radar-chart.md)
Multi-axis spider/web chart.

```kotlin
RadarChart(
    dataCollection = listOf(
        RadarData(0.8f, "Speed"),
        RadarData(0.6f, "Power")
    )
)
```

### [Multiple Radar Chart](radar/multiple-radar-chart.md)
Compare multiple datasets on the same radar.

---

## Financial Charts

### [Candlestick Chart](candlestick/candlestick-chart.md)
Display financial OHLC (Open, High, Low, Close) data.

```kotlin
CandlestickChart(
    dataCollection = listOf(
        CandleData(
            open = 100f,
            high = 110f,
            low = 95f,
            close = 105f
        )
    )
)
```

---

## Combination Charts

### [Combo Chart](combo/combo-chart.md)
Combine multiple chart types in one visualization (e.g., bar + line).

---

## Specialized Charts

### [Block Bar](block/block-bar.md)
Block-style bar visualization with unique styling.

---

## Chart Selection Guide

Choose the right chart for your data:

| Data Type | Recommended Chart |
|-----------|------------------|
| Comparing categories | Bar Chart, Horizontal Bar Chart |
| Trends over time | Line Chart, Area Chart |
| Proportions of whole | Pie Chart |
| Multiple series comparison | Stacked Bar, Multiline Chart |
| Relationships/correlation | Point Chart, Bubble Chart |
| Multi-dimensional data | Radar Chart, Bubble Chart |
| Financial data | Candlestick Chart |
| Ranges/spans | Span Chart, Waterfall Chart |
| Cumulative values | Waterfall Chart, Stacked Area |

---

## Common Features

All charts in Charty share common features:

- ✅ **Compose-native**: Built with Jetpack Compose
- ✅ **Kotlin Multiplatform**: Works on Android, iOS, Desktop, Web
- ✅ **Customizable**: Colors, sizes, styles, animations
- ✅ **Accessible**: Support for content descriptions
- ✅ **Performant**: Optimized for smooth rendering
- ✅ **Responsive**: Adapts to different screen sizes

---

## Next Steps

- Explore individual chart documentation for detailed usage
- Check out [Examples](../examples/basic-usage.md) for real-world usage
- Review [Configuration Guide](../getting-started/configuration.md) for customization options

---

## Need a Custom Chart?

If you need a chart type not listed here, consider:

1. Opening a [feature request](https://github.com/hi-manshu/charty/issues/new)
2. Combining existing charts with Compose layouts
3. Contributing a new chart type to the library

