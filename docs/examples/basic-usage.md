# Examples - Basic Usage
- [Configuration Guide](../getting-started/configuration.md) - Detailed configuration options
- [Advanced Examples](advanced.md) - Complex use cases
- [Customization Examples](customization.md) - Learn advanced customization

## Next Steps

---

```
}
    }
        )
                .height(300.dp)
                .fillMaxWidth()
            modifier = Modifier
            ),
                RadarData(0.75f, "Ease of Use")
                RadarData(0.85f, "Features"),
                RadarData(0.8f, "Support"),
                RadarData(0.7f, "Price"),
                RadarData(0.9f, "Quality"),
            dataCollection = listOf(
        RadarChart(
        
        Text("Product Ratings", style = MaterialTheme.typography.headlineSmall)
    Column {
fun ProductComparison() {
@Composable
```kotlin

## Product Comparison

---

```
}
    }
        )
                .height(200.dp)
                .fillMaxWidth()
            modifier = Modifier
            color = Color(0xFFFF9800),
            ),
                LineData(22f, "9pm")
                LineData(26f, "6pm"),
                LineData(28f, "3pm"),
                LineData(25f, "12pm"),
                LineData(20f, "9am"),
                LineData(18f, "6am"),
            dataCollection = listOf(
        AreaChart(
        
        Text("Temperature (°C)", style = MaterialTheme.typography.headlineSmall)
    ) {
        verticalArrangement = Arrangement.spacedBy(16.dp)
        modifier = Modifier.fillMaxSize().padding(16.dp),
    Column(
fun WeatherDashboard() {
@Composable
```kotlin

## Weather Dashboard

---

```
}
    }
        )
                .height(250.dp)
                .fillMaxWidth()
            modifier = Modifier
            ),
                BarData(10f, "Poor")
                BarData(15f, "Fair"),
                BarData(30f, "Good"),
                BarData(45f, "Excellent"),
            dataCollection = listOf(
        HorizontalBarChart(
        
        Text("Customer Satisfaction", style = MaterialTheme.typography.headlineSmall)
    ) {
        verticalArrangement = Arrangement.spacedBy(16.dp)
        modifier = Modifier.fillMaxSize().padding(16.dp),
    Column(
fun SurveyResults() {
@Composable
```kotlin

## Survey Results

---

```
}
    }
        )
                .height(300.dp)
                .fillMaxWidth()
            modifier = Modifier
            ),
                CandleData(158f, 162f, 157f, 161f, "Fri")
                CandleData(152f, 160f, 151f, 158f, "Thu"),
                CandleData(156f, 157f, 151f, 152f, "Wed"),
                CandleData(153f, 158f, 152f, 156f, "Tue"),
                CandleData(150f, 155f, 148f, 153f, "Mon"),
            dataCollection = listOf(
        CandlestickChart(
        
        Text("AAPL - 7 Days", style = MaterialTheme.typography.headlineSmall)
    Column {
fun StockPriceChart() {
@Composable
```kotlin

## Stock Price Tracker

---

```
}
    }
        )
                .height(250.dp)
                .fillMaxWidth()
            modifier = Modifier
            ),
                PieData(10f, "Other")
                PieData(20f, "Cycling"),
                PieData(30f, "Walking"),
                PieData(40f, "Running"),
            dataCollection = listOf(
        PieChart(
        Text("Activity Distribution", style = MaterialTheme.typography.headlineSmall)
        // Activity distribution
        
        )
                .height(200.dp)
                .fillMaxWidth()
            modifier = Modifier
            color = Color(0xFF4CAF50),
            ),
                BarData(4500f, "Sun")
                BarData(6000f, "Sat"),
                BarData(9500f, "Fri"),
                BarData(12000f, "Thu"),
                BarData(7800f, "Wed"),
                BarData(10200f, "Tue"),
                BarData(8500f, "Mon"),
            dataCollection = listOf(
        BarChart(
        Text("Weekly Steps", style = MaterialTheme.typography.headlineSmall)
        // Daily steps
    ) {
        verticalArrangement = Arrangement.spacedBy(16.dp)
        modifier = Modifier.fillMaxSize().padding(16.dp),
    Column(
fun FitnessStats() {
@Composable
```kotlin

## Fitness Tracker

---

```
}
    }
        )
                .height(250.dp)
                .fillMaxWidth()
            modifier = Modifier
            ),
                LineData(85000f, "Q4")
                LineData(72000f, "Q3"),
                LineData(65000f, "Q2"),
                LineData(50000f, "Q1"),
            dataCollection = listOf(
        LineChart(
        Text("Revenue Trend", style = MaterialTheme.typography.headlineSmall)
        // Revenue trend line chart
        
        )
            )
                showGridLines = true
                showAxis = true,
            config = BarChartConfig(
                .height(250.dp),
                .fillMaxWidth()
            modifier = Modifier
            ),
                BarData(25000f, "Apr")
                BarData(22000f, "Mar"),
                BarData(18000f, "Feb"),
                BarData(15000f, "Jan"),
            dataCollection = listOf(
        BarChart(
        Text("Monthly Sales", style = MaterialTheme.typography.headlineSmall)
        // Monthly sales bar chart
    ) {
        verticalArrangement = Arrangement.spacedBy(16.dp)
            .padding(16.dp),
            .fillMaxSize()
        modifier = Modifier
    Column(
fun SalesDashboard() {
@Composable
```kotlin

## Sales Dashboard

---

Real-world examples demonstrating basic usage of Charty charts.


