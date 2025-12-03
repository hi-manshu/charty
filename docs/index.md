# 🎯 Charty

## Elementary Chart library for Compose with KMP support!

![Charty Banner](img/banner.png)

> A sleek & lightweight charting library for Jetpack Compose, now with **Kotlin Multiplatform (KMP)** support!  
> *Built with ❤️ by [@hi-manshu](https://github.com/hi-manshu) for developers who love charts.*

[![Maven Central](https://img.shields.io/maven-central/v/com.himanshoe/charty?color=f4c430&label=Maven%20Central%20%3A%20Charty)](https://search.maven.org/artifact/com.himanshoe/charty)
[![GitHub issues](https://img.shields.io/github/issues/hi-manshu/charty)](https://github.com/hi-manshu/charty/issues)
[![GitHub stars](https://img.shields.io/github/stars/hi-manshu/charty)](https://github.com/hi-manshu/charty/stargazers)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

## ✨ Features

- 🎨 **Rich Chart Collection**: 20+ chart types including Bar, Line, Pie, Radar, Candlestick, and more
- 📱 **Kotlin Multiplatform**: Works on Android, iOS, Desktop, and Web
- 🎯 **Compose First**: Built natively with Jetpack Compose
- ⚡ **Lightweight**: Minimal dependencies, maximum performance
- 🎨 **Highly Customizable**: Full control over colors, styles, and animations
- 📊 **Modern Design**: Beautiful, Material Design-inspired charts
- 🔧 **Easy to Use**: Simple, intuitive API

---

## 🚀 Quick Start

Get started with Charty in just a few minutes:

```kotlin
@Composable
fun MyChart() {
    BarChart(
        dataCollection = listOf(
            BarData(10f, "Jan"),
            BarData(20f, "Feb"),
            BarData(15f, "Mar")
        ),
        config = BarChartConfig(
            showAxis = true,
            showGridLines = true
        )
    )
}
```

[Get Started →](getting-started/installation.md){ .md-button .md-button--primary }

---

## 📊 Chart Types

Charty supports a wide variety of chart types:

### Bar Charts
- [Bar Chart](charts/bar/bar-chart.md) - Standard vertical bars
- [Horizontal Bar Chart](charts/bar/horizontal-bar-chart.md) - Horizontal orientation
- [Stacked Bar Chart](charts/bar/stacked-bar-chart.md) - Multiple series stacked
- [Comparison Bar Chart](charts/bar/comparison-bar-chart.md) - Side-by-side comparison
- [Lollipop Bar Chart](charts/bar/lollipop-bar-chart.md) - Lollipop style visualization
- [Waterfall Chart](charts/bar/waterfall-chart.md) - Cumulative effect display
- [Wavy Chart](charts/bar/wavy-chart.md) - Bars with wavy tops
- [Bubble Bar Chart](charts/bar/bubble-bar-chart.md) - Bars with bubble indicators
- [Mosaic Bar Chart](charts/bar/mosaic-bar-chart.md) - Mosaic pattern bars
- [Span Chart](charts/bar/span-chart.md) - Range-based visualization

### Line Charts
- [Line Chart](charts/line/line-chart.md) - Connect data points with lines
- [Area Chart](charts/line/area-chart.md) - Filled area under the line
- [Multiline Chart](charts/line/multiline-chart.md) - Multiple data series
- [Stacked Area Chart](charts/line/stacked-area-chart.md) - Stacked filled areas

### Point Charts
- [Point Chart](charts/point/point-chart.md) - Scatter plot visualization
- [Bubble Chart](charts/point/bubble-chart.md) - Size-based data points

### Other Charts
- [Pie Chart](charts/pie/pie-chart.md) - Circular sector representation
- [Radar Chart](charts/radar/radar-chart.md) - Multi-axis visualization
- [Multiple Radar Chart](charts/radar/multiple-radar-chart.md) - Compare multiple datasets
- [Candlestick Chart](charts/candlestick/candlestick-chart.md) - Financial data visualization
- [Combo Chart](charts/combo/combo-chart.md) - Combine multiple chart types
- [Block Bar](charts/block/block-bar.md) - Block-style bars
- [Circular Progress](charts/circular/circular-progress.md) - Circular progress indicator

---

## 🌍 Kotlin Multiplatform Support

Charty works seamlessly across multiple platforms:

| Platform | Status |
|----------|--------|
| Android | ✅ Fully Supported |
| iOS | ✅ Fully Supported |
| Desktop (JVM) | ✅ Fully Supported |
| Web (JS) | ✅ Fully Supported |
| Web (Wasm) | ✅ Fully Supported |

---

## 🎨 Customization

Every chart in Charty is highly customizable:

- **Colors**: Define your own color schemes
- **Styles**: Customize line thickness, bar width, corner radius, etc.
- **Animations**: Control animation duration and style
- **Axes**: Configure axis labels, grid lines, and formatting
- **Labels**: Full control over data labels and legends

[Learn More About Customization →](examples/customization.md)

---

## 📱 Apps Using Charty

| App | Link |
| --- | --- |
| 🐱 **NEKO** | [github.com/nekomangaorg/Neko](https://github.com/nekomangaorg/Neko) |
| ⏱️ **TimePlanner** | [github.com/v1tzor/TimePlanner](https://github.com/v1tzor/TimePlanner) |

---

## 🤝 Contributing

Contributions are welcome! Please check out our [Contributing Guide](contributing.md) to get started.

---

## 📄 License

Charty is licensed under the Apache License 2.0. See [LICENSE](license.md) for details.

---

## 🌟 Support

If you like this library, please consider:

- ⭐ Starring the [GitHub repository](https://github.com/hi-manshu/charty)
- 🐛 Reporting bugs and issues
- 💡 Suggesting new features
- 🔧 Contributing code improvements

---

## 📞 Contact

- **GitHub**: [@hi-manshu](https://github.com/hi-manshu)
- **Twitter**: [@hi_man_shoe](https://twitter.com/hi_man_shoe)

---

<div align="center">
Made with ❤️ by Himanshu Singh
</div>

