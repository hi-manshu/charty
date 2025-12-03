# Pie Chart

Display proportions as slices of a circle.

## Basic Usage

```kotlin
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.data.PieData

PieChart(
    dataCollection = listOf(
        PieData(30f, "Category A"),
        PieData(45f, "Category B"),
        PieData(25f, "Category C")
    )
)
```

## Configuration

```kotlin
import com.himanshoe.charty.pie.config.PieChartConfig

PieChart(
    dataCollection = data,
    config = PieChartConfig(
        showSliceLabels = true,
        showPercentage = true,
        donut = false
    )
)
```

---

For more details, see [Chart Overview](../overview.md).

