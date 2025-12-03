# Examples - Advanced

Advanced usage patterns and complex scenarios.

---

## Real-Time Data Updates

```kotlin
@Composable
fun RealTimeChart() {
    var data by remember {
        mutableStateOf(
            listOf(BarData(0f, "Start"))
        )
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val newValue = Random.nextFloat() * 100
            data = (data + BarData(newValue, "")).takeLast(10)
        }
    }
    
    BarChart(dataCollection = data)
}
```

---

## Multiple Charts with Shared Data

```kotlin
@Composable
fun MultiChartDashboard(data: List<DataPoint>) {
    Column {
        BarChart(
            dataCollection = data.map { BarData(it.value, it.label) }
        )
        
        LineChart(
            dataCollection = data.map { LineData(it.value, it.label) }
        )
        
        PieChart(
            dataCollection = data.map { PieData(it.value, it.label) }
        )
    }
}
```

---

## Interactive Charts

```kotlin
@Composable
fun InteractiveChart() {
    var selectedData by remember { mutableStateOf<BarData?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    
    BarChart(
        dataCollection = data,
        onBarClick = { barData ->
            selectedData = barData
            showDialog = true
        }
    )
    
    if (showDialog && selectedData != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(selectedData!!.xValue) },
            text = { Text("Value: ${selectedData!!.yValue}") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
```

---

For more information, see the [API Reference](../api-reference.md).

