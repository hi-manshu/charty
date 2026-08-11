@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
)

package com.himanshoe.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.BubbleBarChart
import com.himanshoe.charty.bar.ComparisonBarChart
import com.himanshoe.charty.bar.GroupedHorizontalBarChart
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.LollipopBarChart
import com.himanshoe.charty.bar.MosaicBarChart
import com.himanshoe.charty.bar.NormalizedHorizontalBarChart
import com.himanshoe.charty.bar.SpanChart
import com.himanshoe.charty.bar.StackedBarChart
import com.himanshoe.charty.bar.StackedHorizontalBarChart
import com.himanshoe.charty.bar.WaterfallChart
import com.himanshoe.charty.bar.WavyChart
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.block.BlockBarChart
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.calendar.CalendarHeatmapChart
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.candlestick.CandlestickChart
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.circular.CircularProgressIndicator
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.combo.ComboChart
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty.point.BubbleChart
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.point.data.PointData
import com.himanshoe.charty.radar.MultipleRadarChart
import com.himanshoe.charty.radar.RadarChart
import com.himanshoe.charty.radar.data.RadarAxisData
import com.himanshoe.charty.radar.data.RadarDataSet

/** One entry in the chart gallery: a titled, categorised demo that renders a chart. */
private data class ChartDemo(
    val title: String,
    val description: String,
    val category: String,
    val accent: Color,
    val content: @Composable () -> Unit,
)

private val galleryDemos: List<ChartDemo> = buildGalleryDemos()

/**
 * The sample app entry point: a polished gallery of buttons, one per chart type, each opening that
 * chart on its own screen. The full continuous-scroll showcase is reachable from the top of the list.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    MaterialTheme {
        var selected by remember { mutableStateOf<ChartDemo?>(null) }
        var showAll by remember { mutableStateOf(false) }
        Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                showAll -> AllChartsScreen(onBack = { showAll = false })
                selected != null -> ChartDetailScreen(demo = selected!!, onBack = { selected = null })
                else -> GalleryHomeScreen(onOpenAll = { showAll = true }, onOpen = { selected = it })
            }
        }
    }
}

@Composable
private fun AllChartsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        GalleryTopBar(title = "All charts", onBack = onBack)
        AllChartsShowcase(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun GalleryHomeScreen(
    onOpenAll: () -> Unit,
    onOpen: (ChartDemo) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { GalleryHeader() }
        item { AllChartsBanner(onClick = onOpenAll) }
        val grouped = galleryDemos.groupBy { it.category }
        grouped.forEach { (category, demos) ->
            item {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp, bottom = 2.dp),
                )
            }
            items(demos) { demo ->
                ChartRow(demo = demo, onClick = { onOpen(demo) })
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun GalleryHeader() {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Charty",
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "A gallery of every chart. Tap one to open it full-screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AllChartsBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Continuous showcase",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Scroll through every demo in one screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(text = "›", fontSize = 28.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun ChartRow(
    demo: ChartDemo,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(demo.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(demo.accent))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = demo.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = demo.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(text = "›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChartDetailScreen(
    demo: ChartDemo,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GalleryTopBar(title = demo.title, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = demo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(340.dp).padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    demo.content()
                }
            }
        }
    }
}

@Composable
private fun GalleryTopBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                    ),
                ).padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "‹",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.clip(CircleShape).clickable(onClick = onBack).padding(horizontal = 10.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

private val chartFill: Modifier = Modifier.fillMaxSize()

private fun buildGalleryDemos(): List<ChartDemo> {
    val blue = ChartyColors.Blue
    val green = ChartyColors.Green
    val orange = ChartyColors.Orange
    val purple = ChartyColors.Purple
    val red = ChartyColors.Red

    val bars = listOf(BarData("Jan", 120f), BarData("Feb", 180f), BarData("Mar", 90f), BarData("Apr", 210f), BarData("May", 150f))
    val line = listOf(LineData("Mon", 20f), LineData("Tue", 45f), LineData("Wed", 30f), LineData("Thu", 70f), LineData("Fri", 55f))
    val points = listOf(PointData("A", 30f), PointData("B", 65f), PointData("C", 45f), PointData("D", 80f), PointData("E", 50f))
    val groups =
        listOf(
            BarGroup("Q1", listOf(40f, 30f, 20f)),
            BarGroup("Q2", listOf(55f, 25f, 35f)),
            BarGroup("Q3", listOf(30f, 45f, 25f)),
            BarGroup("Q4", listOf(60f, 40f, 30f)),
        )
    val lineGroups =
        listOf(
            LineGroup("Mon", listOf(20f, 35f, 15f)),
            LineGroup("Tue", listOf(45f, 28f, 38f)),
            LineGroup("Wed", listOf(30f, 52f, 25f)),
            LineGroup("Thu", listOf(70f, 40f, 55f)),
            LineGroup("Fri", listOf(55f, 65f, 45f)),
        )

    return listOf(
        ChartDemo("Bar Chart", "Compare values across categories", "Bar", blue) {
            BarChart(data = { bars }, modifier = chartFill)
        },
        ChartDemo("Horizontal Bar", "Bars laid out left to right", "Bar", green) {
            HorizontalBarChart(data = { bars }, modifier = chartFill)
        },
        ChartDemo("Stacked Bar", "Segments stacked per category", "Bar", orange) {
            StackedBarChart(data = { groups }, modifier = chartFill)
        },
        ChartDemo("Grouped Horizontal Bar", "Grouped bars per row", "Bar", purple) {
            GroupedHorizontalBarChart(data = { groups }, modifier = chartFill)
        },
        ChartDemo("Stacked Horizontal Bar", "Horizontal stacked segments", "Bar", blue) {
            StackedHorizontalBarChart(data = { groups }, modifier = chartFill)
        },
        ChartDemo("Normalized Horizontal Bar", "Each bar fills 100%", "Bar", green) {
            NormalizedHorizontalBarChart(data = { groups }, modifier = chartFill)
        },
        ChartDemo("Mosaic Bar", "Proportional stacked columns", "Bar", red) {
            MosaicBarChart(data = { groups }, modifier = chartFill)
        },
        ChartDemo("Comparison Bar", "Two series compared", "Bar", purple) {
            ComparisonBarChart(data = { groups.map { BarGroup(it.label, it.values.take(2)) } }, modifier = chartFill)
        },
        ChartDemo("Lollipop Bar", "Stems with value dots", "Bar", orange) {
            LollipopBarChart(data = { bars }, modifier = chartFill)
        },
        ChartDemo("Bubble Bar", "Bars rendered as bubbles", "Bar", blue) {
            BubbleBarChart(data = { bars }, modifier = chartFill)
        },
        ChartDemo("Waterfall", "Running cumulative total", "Bar", green) {
            WaterfallChart(data = { bars }, modifier = chartFill)
        },
        ChartDemo("Wavy Bar", "Bars with a wavy top", "Bar", purple) {
            WavyChart(data = { bars }, modifier = chartFill)
        },
        ChartDemo("Span (Range) Chart", "Start-to-end ranges", "Bar", red) {
            SpanChart(
                data = {
                    listOf(
                        SpanData("Mon", 10f, 40f),
                        SpanData("Tue", 20f, 60f),
                        SpanData("Wed", 15f, 35f),
                        SpanData("Thu", 30f, 70f),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Line Chart", "Trend over an axis", "Line & Area", blue) {
            LineChart(data = { line }, modifier = chartFill)
        },
        ChartDemo("Area Chart", "Line with a filled area", "Line & Area", green) {
            AreaChart(data = { line }, modifier = chartFill)
        },
        ChartDemo("Multiline Chart", "Several series at once", "Line & Area", orange) {
            MultilineChart(data = { lineGroups }, modifier = chartFill)
        },
        ChartDemo("Stacked Area", "Cumulative stacked areas", "Line & Area", purple) {
            StackedAreaChart(data = { lineGroups }, modifier = chartFill)
        },
        ChartDemo("Point (Scatter)", "Individual data points", "Point", blue) {
            PointChart(data = { points }, modifier = chartFill)
        },
        ChartDemo("Bubble Chart", "Points sized by a value", "Point", red) {
            BubbleChart(
                data = {
                    listOf(
                        BubbleData("A", yValue = 30f, size = 120f),
                        BubbleData("B", yValue = 60f, size = 220f),
                        BubbleData("C", yValue = 45f, size = 160f),
                        BubbleData("D", yValue = 80f, size = 90f),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Combo Chart", "Bars plus a line", "Point", orange) {
            ComboChart(
                data = {
                    listOf(
                        ComboChartData("Jan", 120f, 40f),
                        ComboChartData("Feb", 180f, 65f),
                        ComboChartData("Mar", 90f, 55f),
                        ComboChartData("Apr", 210f, 80f),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Candlestick", "Financial OHLC prices", "Point", green) {
            CandlestickChart(
                data = {
                    listOf(
                        CandleData("Mon", 100f, 130f, 90f, 120f),
                        CandleData("Tue", 120f, 140f, 110f, 115f),
                        CandleData("Wed", 115f, 125f, 95f, 105f),
                        CandleData("Thu", 105f, 150f, 100f, 145f),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Pie Chart", "Parts of a whole", "Circular", purple) {
            PieChart(
                data = {
                    listOf(
                        PieData("A", 40f, ChartyColor.Solid(blue)),
                        PieData("B", 25f, ChartyColor.Solid(green)),
                        PieData("C", 20f, ChartyColor.Solid(orange)),
                        PieData("D", 15f, ChartyColor.Solid(red)),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Circular Progress", "Concentric progress rings", "Circular", blue) {
            CircularProgressIndicator(
                rings = {
                    listOf(
                        CircularRingData("Steps", 72f, 100f, ChartyColor.Solid(blue)),
                        CircularRingData("Move", 55f, 100f, ChartyColor.Solid(green)),
                        CircularRingData("Stand", 88f, 100f, ChartyColor.Solid(orange)),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Radar Chart", "Multi-axis profile", "Circular", red) {
            RadarChart(
                data = {
                    listOf(
                        RadarDataSet(
                            label = "Player",
                            axes =
                                listOf(
                                    RadarAxisData("Speed", 80f),
                                    RadarAxisData("Power", 65f),
                                    RadarAxisData("Skill", 90f),
                                    RadarAxisData("Stamina", 70f),
                                    RadarAxisData("Defense", 55f),
                                ),
                            color = ChartyColor.Solid(blue),
                        ),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Multiple Radar", "Several profiles overlaid", "Circular", purple) {
            MultipleRadarChart(
                dataSets = {
                    listOf(
                        RadarDataSet(
                            "A",
                            listOf(RadarAxisData("Speed", 80f), RadarAxisData("Power", 60f), RadarAxisData("Skill", 90f), RadarAxisData("Stamina", 70f)),
                            ChartyColor.Solid(blue),
                        ),
                        RadarDataSet(
                            "B",
                            listOf(RadarAxisData("Speed", 55f), RadarAxisData("Power", 85f), RadarAxisData("Skill", 60f), RadarAxisData("Stamina", 90f)),
                            ChartyColor.Solid(orange),
                        ),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Block Bar", "Segmented value blocks", "Specialized", green) {
            BlockBarChart(
                data = {
                    listOf(
                        BlockData(40f, ChartyColor.Solid(blue)),
                        BlockData(30f, ChartyColor.Solid(green)),
                        BlockData(20f, ChartyColor.Solid(orange)),
                        BlockData(10f, ChartyColor.Solid(red)),
                    )
                },
                modifier = chartFill,
            )
        },
        ChartDemo("Calendar Heatmap", "GitHub-style activity grid", "Specialized", green) {
            CalendarHeatmapChart(
                data = {
                    (1..90).map { day ->
                        CalendarData(2024, ((day - 1) / 30) + 1, ((day - 1) % 30) + 1, (day * 7 % 10).toFloat())
                    }
                },
                modifier = chartFill,
            )
        },
    )
}
