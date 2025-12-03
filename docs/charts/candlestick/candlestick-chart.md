# Candlestick Chart

Display financial OHLC (Open, High, Low, Close) data.

## Basic Usage

```kotlin
import com.himanshoe.charty.candlestick.CandlestickChart
import com.himanshoe.charty.candlestick.data.CandleData

CandlestickChart(
    dataCollection = listOf(
        CandleData(
            open = 100f,
            high = 110f,
            low = 95f,
            close = 105f,
            label = "Day 1"
        )
    )
)
```

---

For more details, see [Chart Overview](../overview.md).

