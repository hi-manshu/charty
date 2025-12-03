#!/usr/bin/env python3
"""Generate stub documentation files for all charts"""

import os

# Define all chart pages
charts = {
    "bar": [
        ("stacked-bar-chart.md", "Stacked Bar Chart", "Display multiple data series stacked vertically"),
        ("comparison-bar-chart.md", "Comparison Bar Chart", "Compare multiple series side-by-side"),
        ("lollipop-bar-chart.md", "Lollipop Bar Chart", "Minimalist bar chart with lollipop-style markers"),
        ("waterfall-chart.md", "Waterfall Chart", "Show cumulative effect of sequential values"),
        ("wavy-chart.md", "Wavy Chart", "Bars with decorative wavy tops"),
        ("bubble-bar-chart.md", "Bubble Bar Chart", "Bars with bubble indicators"),
        ("mosaic-bar-chart.md", "Mosaic Bar Chart", "Bars with mosaic pattern styling"),
        ("span-chart.md", "Span Chart", "Visualize ranges or time spans"),
    ],
    "line": [
        ("line-chart.md", "Line Chart", "Classic line chart connecting data points"),
        ("area-chart.md", "Area Chart", "Line chart with filled area below"),
        ("multiline-chart.md", "Multiline Chart", "Display multiple data series"),
        ("stacked-area-chart.md", "Stacked Area Chart", "Multiple series stacked"),
    ],
    "point": [
        ("point-chart.md", "Point Chart", "Scatter plot visualization"),
        ("bubble-chart.md", "Bubble Chart", "Size-based data points"),
    ],
    "pie": [
        ("pie-chart.md", "Pie Chart", "Circular sector representation"),
    ],
    "radar": [
        ("radar-chart.md", "Radar Chart", "Multi-axis spider/web chart"),
        ("multiple-radar-chart.md", "Multiple Radar Chart", "Compare multiple datasets"),
    ],
    "candlestick": [
        ("candlestick-chart.md", "Candlestick Chart", "Financial OHLC data visualization"),
    ],
    "combo": [
        ("combo-chart.md", "Combo Chart", "Combine multiple chart types"),
    ],
    "block": [
        ("block-bar.md", "Block Bar", "Block-style bar visualization"),
    ],
    "circular": [
        ("circular-progress.md", "Circular Progress", "Animated circular progress indicator"),
    ],
}

base_dir = "docs/charts"

for category, pages in charts.items():
    category_dir = os.path.join(base_dir, category)
    os.makedirs(category_dir, exist_ok=True)

    for filename, title, description in pages:
        filepath = os.path.join(category_dir, filename)

        # Skip if file already exists
        if os.path.exists(filepath):
            print(f"Skipping {filepath} (already exists)")
            continue

        content = f"""# {title}

{description}

---

## Overview

{title} provides a unique way to visualize your data with Charty.

---

## Basic Usage

```kotlin
import com.himanshoe.charty.{category}.*

// TODO: Add basic usage example
```

---

## Configuration

```kotlin
// TODO: Add configuration options
```

---

## Examples

### Simple Example

```kotlin
// TODO: Add simple example
```

### Customized Example

```kotlin
// TODO: Add customized example
```

---

## Best Practices

- TODO: Add best practices

---

## Related Charts

- See [Chart Overview](../overview.md) for all available charts

---

## API Reference

For complete API documentation, see the [API Reference](../../api-reference.md).
"""

        with open(filepath, 'w') as f:
            f.write(content)

        print(f"Created {filepath}")

print("Done!")

