# CandlestickChart

Best for financial time-series data (OHLC) such as stock prices or commodity values.

```kotlin
CandlestickChart(
    data = {
        listOf(
            CandleData(label = "Mon", open = 100f, high = 115f, low = 95f,  close = 110f),
            CandleData(label = "Tue", open = 110f, high = 120f, low = 105f, close = 108f),
            CandleData(label = "Wed", open = 108f, high = 125f, low = 100f, close = 120f),
            CandleData(label = "Thu", open = 120f, high = 130f, low = 112f, close = 115f),
            CandleData(label = "Fri", open = 115f, high = 118f, low = 108f, close = 112f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(320.dp),
    candlestickConfig = CandlestickChartConfig(
        animation = Animation.Default,
    ),
)
```

Each `CandleData` entry defines a candle:
- `open` / `close` — the candle body (bullish when close > open, bearish otherwise)
- `high` / `low` — the upper and lower wicks extending beyond the body

**Key config options:**
- `candlestickConfig` — controls wick width, candle body width fraction, and bullish/bearish colors
- `scaffoldConfig` — configure the y-axis label formatter for price display (e.g., `"$%.2f"`)
- `interactionConfig` — enables horizontal scroll and zoom for long time series
