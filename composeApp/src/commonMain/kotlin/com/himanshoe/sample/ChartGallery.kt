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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.util.fastForEach
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
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
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
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.theme.ChartyTheme
import com.himanshoe.charty.common.theme.ChartyThemeProvider
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.PillTooltip
import com.himanshoe.charty.gauge.AngularGaugeChart
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
import com.himanshoe.charty.gauge.data.GaugeBand
import com.himanshoe.charty.heatmap.MatrixHeatmapChart
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.Sparkline
import com.himanshoe.charty.line.SparklineConfig
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.config.PieChartStyle
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty.point.BubbleChart
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.point.data.PointData
import com.himanshoe.charty.radar.MultipleRadarChart
import com.himanshoe.charty.radar.RadarChart
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.config.RadarLabelConfig
import com.himanshoe.charty.radar.data.RadarAxisData
import com.himanshoe.charty.radar.data.RadarDataSet

/** A single configuration of a chart — one "iteration" shown on the chart's detail screen. */
private data class ChartVariant(
    val label: String,
    val content: @Composable () -> Unit,
)

/** One entry in the chart gallery: a titled, categorised chart type with one or more [variants]. */
private data class ChartDemo(
    val title: String,
    val description: String,
    val category: String,
    val accent: Color,
    val variants: List<ChartVariant>,
)

private val galleryDemos: List<ChartDemo> = buildGalleryDemos()

/**
 * The sample app entry point: a polished gallery of buttons, one per chart type, each opening that
 * chart on its own screen. The full continuous-scroll showcase is reachable from the top of the list.
 */
@Composable
fun App(modifier: Modifier = Modifier) {
    var darkMode by remember { mutableStateOf(false) }
    val colorScheme =
        if (darkMode) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    val chartyTheme =
        if (darkMode) {
            ChartyTheme.dark()
        } else {
            ChartyTheme.light()
        }
    MaterialTheme(colorScheme = colorScheme) {
        ChartyThemeProvider(theme = chartyTheme) {
            var selected by remember { mutableStateOf<ChartDemo?>(null) }
            var showAll by remember { mutableStateOf(false) }
            Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when {
                    showAll -> AllChartsScreen(onBack = { showAll = false })
                    selected != null -> ChartDetailScreen(demo = selected!!, onBack = { selected = null })
                    else ->
                        GalleryHomeScreen(
                            darkMode = darkMode,
                            onToggleDark = { darkMode = !darkMode },
                            onOpenAll = { showAll = true },
                            onOpen = { selected = it },
                        )
                }
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
    darkMode: Boolean,
    onToggleDark: () -> Unit,
    onOpenAll: () -> Unit,
    onOpen: (ChartDemo) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { GalleryHeader(darkMode = darkMode, onToggleDark = onToggleDark) }
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
private fun GalleryHeader(
    darkMode: Boolean,
    onToggleDark: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
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
        Text(
            text =
                if (darkMode) {
                    "☀︎ Light"
                } else {
                    "☾ Dark"
                },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onToggleDark)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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
            val variantCount = demo.variants.size
            Text(
                text =
                    if (variantCount == 1) {
                        "1 style"
                    } else {
                        "$variantCount styles"
                    },
                style = MaterialTheme.typography.labelSmall,
                color = demo.accent,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(8.dp))
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
            demo.variants.fastForEach { variant ->
                ChartVariantCard(accent = demo.accent, variant = variant)
            }
        }
    }
}

@Composable
private fun ChartVariantCard(
    accent: Color,
    variant: ChartVariant,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = variant.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(320.dp).padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                variant.content()
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
                ).statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp),
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

/**
 * Wraps a clickable chart with a live selection label above it, so tap variants in the gallery show
 * the callback firing. [chart] receives the modifier to apply to the chart.
 */
@Composable
private fun SelectionColumn(
    hint: String,
    selected: String?,
    chart: @Composable (Modifier) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = selected ?: hint,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            chart(Modifier.fillMaxSize())
        }
    }
}

private fun buildGalleryDemos(): List<ChartDemo> {
    val blue = ChartyColors.Blue
    val green = ChartyColors.Green
    val orange = ChartyColors.Orange
    val purple = ChartyColors.Purple
    val red = ChartyColors.Red

    val blueGradient = ChartyColor.Gradient(listOf(blue, purple))
    val warmGradient = ChartyColor.Gradient(listOf(orange, red))
    val coolGradient = ChartyColor.Gradient(listOf(green, blue))
    val palette = ChartyColor.Gradient(listOf(blue, green, orange, purple, red))

    val bars =
        listOf(
            BarData(label = "Jan", value = 120f),
            BarData(label = "Feb", value = 180f),
            BarData(label = "Mar", value = 90f),
            BarData(label = "Apr", value = 210f),
            BarData(label = "May", value = 150f),
        )
    val barsSigned =
        listOf(
            BarData(label = "Jan", value = 120f),
            BarData(label = "Feb", value = -60f),
            BarData(label = "Mar", value = 90f),
            BarData(label = "Apr", value = -30f),
            BarData(label = "May", value = 150f),
        )
    val line =
        listOf(
            LineData(label = "Mon", value = 20f),
            LineData(label = "Tue", value = 45f),
            LineData(label = "Wed", value = 30f),
            LineData(label = "Thu", value = 70f),
            LineData(label = "Fri", value = 55f),
        )
    val lineSigned =
        listOf(
            LineData(label = "Mon", value = 25f),
            LineData(label = "Tue", value = -18f),
            LineData(label = "Wed", value = 12f),
            LineData(label = "Thu", value = -30f),
            LineData(label = "Fri", value = 40f),
        )
    val pointsSigned =
        listOf(
            PointData(label = "A", value = 30f),
            PointData(label = "B", value = -20f),
            PointData(label = "C", value = 15f),
            PointData(label = "D", value = -12f),
            PointData(label = "E", value = 45f),
        )
    val points =
        listOf(
            PointData(label = "A", value = 30f),
            PointData(label = "B", value = 65f),
            PointData(label = "C", value = 45f),
            PointData(label = "D", value = 80f),
            PointData(label = "E", value = 50f),
        )
    val groups =
        listOf(
            BarGroup(label = "Q1", values = listOf(40f, 30f, 20f)),
            BarGroup(label = "Q2", values = listOf(55f, 25f, 35f)),
            BarGroup(label = "Q3", values = listOf(30f, 45f, 25f)),
            BarGroup(label = "Q4", values = listOf(60f, 40f, 30f)),
        )
    val lineGroups =
        listOf(
            LineGroup(label = "Mon", values = listOf(20f, 35f, 15f)),
            LineGroup(label = "Tue", values = listOf(45f, 28f, 38f)),
            LineGroup(label = "Wed", values = listOf(30f, 52f, 25f)),
            LineGroup(label = "Thu", values = listOf(70f, 40f, 55f)),
            LineGroup(label = "Fri", values = listOf(55f, 65f, 45f)),
        )

    val heatmapCells =
        listOf("Mon", "Tue", "Wed", "Thu", "Fri").flatMapIndexed { rowIndex: Int, day: String ->
            listOf("8h", "10h", "12h", "14h", "16h").mapIndexed { columnIndex, hour ->
                HeatmapCell(
                    rowLabel = day,
                    columnLabel = hour,
                    value = ((rowIndex * 7 + columnIndex * 11) % 13).toFloat() + columnIndex,
                )
            }
        }

    val sparkValues = listOf(12f, 18f, 9f, 22f, 17f, 26f, 21f, 30f, 24f, 33f)

    return listOf(
        ChartDemo(
            title = "Angular Gauge",
            description = "Needle dial with plot bands",
            category = "Gauge",
            accent = red,
            variants =
                listOf(
                    ChartVariant("Default") { AngularGaugeChart(value = { 68f }, modifier = chartFill) },
                    ChartVariant("Plot bands") {
                        AngularGaugeChart(
                            value = { 68f },
                            color = warmGradient,
                            config =
                                AngularGaugeConfig(
                                    plotBands =
                                        listOf(
                                            GaugeBand(fromValue = 0f, toValue = 40f, color = ChartyColor.Solid(green)),
                                            GaugeBand(
                                                fromValue = 40f,
                                                toValue = 75f,
                                                color = ChartyColor.Solid(orange),
                                            ),
                                            GaugeBand(fromValue = 75f, toValue = 100f, color = ChartyColor.Solid(red)),
                                        ),
                                ),
                            modifier = chartFill,
                        )
                    },
                ),
        ),
        ChartDemo(
            title = "Matrix Heatmap",
            description = "Value grid with a colour scale",
            category = "Specialised",
            accent = purple,
            variants =
                listOf(
                    ChartVariant("Default") { MatrixHeatmapChart(data = { heatmapCells }, modifier = chartFill) },
                    ChartVariant("With values") {
                        MatrixHeatmapChart(
                            data = { heatmapCells },
                            config = MatrixHeatmapConfig(showValues = true),
                            modifier = chartFill,
                        )
                    },
                ),
        ),
        ChartDemo(
            title = "Sparkline",
            description = "Inline mini line for cards and rows",
            category = "Line",
            accent = blue,
            variants =
                listOf(
                    ChartVariant("Default") { Sparkline(data = { sparkValues }, modifier = chartFill) },
                    ChartVariant("No fill, smooth") {
                        Sparkline(
                            data = { sparkValues },
                            color = coolGradient,
                            config = SparklineConfig(showFill = false, smoothCurve = true),
                            modifier = chartFill,
                        )
                    },
                ),
        ),
        ChartDemo(
            "Bar Chart",
            "Compare values across categories",
            "Bar",
            blue,
            listOf(
                ChartVariant("Default") { BarChart(data = { bars }, modifier = chartFill) },
                ChartVariant("Empty data (built-in placeholder)") {
                    BarChart(data = { emptyList() }, modifier = chartFill)
                },
                ChartVariant("Empty data (custom placeholder)") {
                    BarChart(
                        data = { emptyList() },
                        emptyContent = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "📭", fontSize = 40.sp)
                                Text(text = "Nothing to show yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Gradient bars") { BarChart(data = { bars }, color = blueGradient, modifier = chartFill) },
                ChartVariant("Extra-rounded corners") {
                    BarChart(
                        data = { bars },
                        barConfig = BarChartConfig(cornerRadius = CornerRadius.ExtraLarge),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Square corners, thin bars") {
                    BarChart(
                        data = {
                            bars
                        },
                        barConfig =
                            BarChartConfig(
                                cornerRadius = CornerRadius.None,
                                barWidthFraction = 0.4f,
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Value labels") {
                    BarChart(data = { bars }, barConfig = BarChartConfig(showDataLabels = true), modifier = chartFill)
                },
                ChartVariant("Reference line (target)") {
                    BarChart(
                        data = {
                            bars
                        },
                        barConfig =
                            BarChartConfig(
                                referenceLine = ReferenceLineConfig(value = 160f, label = "Target"),
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Reference band (range)") {
                    BarChart(
                        data = {
                            bars
                        },
                        barConfig =
                            BarChartConfig(
                                referenceBand =
                                    ReferenceBandConfig(
                                        lowValue = 200f,
                                        highValue = 180f,
                                        fill = ChartyColor.Solid(green),
                                        label = "Healthy",
                                    ),
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Negative values (below axis)") {
                    BarChart(
                        data = {
                            barsSigned
                        },
                        barConfig =
                            BarChartConfig(
                                negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Negative values (from min)") {
                    BarChart(
                        data = {
                            barsSigned
                        },
                        barConfig =
                            BarChartConfig(
                                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Tap to select (onBarClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a bar", selected = selected) { chartModifier ->
                        BarChart(
                            data = { bars },
                            onBarClick = { bar -> selected = "${bar.label} = ${bar.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant("Tooltip: pill (compose)") {
                    BarChart(
                        data = { bars },
                        onBarClick = {},
                        tooltip = ChartTooltip.compose { PillTooltip() },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Horizontal Bar",
            "Bars laid out left to right",
            "Bar",
            green,
            listOf(
                ChartVariant("Default") { HorizontalBarChart(data = { bars }, modifier = chartFill) },
                ChartVariant("Tap to select (onBarClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a bar", selected = selected) { chartModifier ->
                        HorizontalBarChart(
                            data = { bars },
                            onBarClick = { bar -> selected = "${bar.label} = ${bar.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant(
                    "Gradient",
                ) { HorizontalBarChart(data = { bars }, color = coolGradient, modifier = chartFill) },
                ChartVariant("Value labels") {
                    HorizontalBarChart(
                        data = { bars },
                        barConfig = BarChartConfig(showDataLabels = true),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Negative values") {
                    HorizontalBarChart(data = { barsSigned }, modifier = chartFill)
                },
            ),
        ),
        ChartDemo(
            "Stacked Bar",
            "Segments stacked per category",
            "Bar",
            orange,
            listOf(
                ChartVariant("Default") { StackedBarChart(data = { groups }, modifier = chartFill) },
                ChartVariant("Tap a segment (onSegmentClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a segment", selected = selected) { chartModifier ->
                        StackedBarChart(
                            data = { groups },
                            onSegmentClick = { seg ->
                                selected =
                                    "${seg.barGroup.label} · #${seg.segmentIndex} = ${seg.segmentValue}"
                            },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant(
                    "Custom palette",
                ) { StackedBarChart(data = { groups }, colors = palette, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Grouped Horizontal Bar",
            "Grouped bars per row",
            "Bar",
            purple,
            listOf(
                ChartVariant("Default") { GroupedHorizontalBarChart(data = { groups }, modifier = chartFill) },
                ChartVariant("Tap a bar (onBarClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a bar", selected = selected) { chartModifier ->
                        GroupedHorizontalBarChart(
                            data = { groups },
                            onBarClick = { entry ->
                                selected =
                                    "${entry.barGroup.label} · #${entry.barIndex} = ${entry.barValue}"
                            },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant(
                    "Custom palette",
                ) { GroupedHorizontalBarChart(data = { groups }, colors = palette, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Stacked Horizontal Bar",
            "Horizontal stacked segments",
            "Bar",
            blue,
            listOf(
                ChartVariant("Default") { StackedHorizontalBarChart(data = { groups }, modifier = chartFill) },
                ChartVariant(
                    "Custom palette",
                ) { StackedHorizontalBarChart(data = { groups }, colors = palette, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Normalized Horizontal Bar",
            "Each bar fills 100%",
            "Bar",
            green,
            listOf(
                ChartVariant("Default") { NormalizedHorizontalBarChart(data = { groups }, modifier = chartFill) },
                ChartVariant("Custom palette") {
                    NormalizedHorizontalBarChart(data = { groups }, colors = palette, modifier = chartFill)
                },
            ),
        ),
        ChartDemo(
            "Mosaic Bar",
            "Proportional stacked columns",
            "Bar",
            red,
            listOf(
                ChartVariant("Default") { MosaicBarChart(data = { groups }, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Comparison Bar",
            "Two series compared",
            "Bar",
            purple,
            listOf(
                ChartVariant("Default") {
                    ComparisonBarChart(data = {
                        groups.map { BarGroup(label = it.label, values = it.values.take(2)) }
                    }, modifier = chartFill)
                },
            ),
        ),
        ChartDemo(
            "Lollipop Bar",
            "Stems with value dots",
            "Bar",
            orange,
            listOf(
                ChartVariant("Default") { LollipopBarChart(data = { bars }, modifier = chartFill) },
                ChartVariant(
                    "Gradient",
                ) { LollipopBarChart(data = { bars }, colors = warmGradient, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Bubble Bar",
            "Bars rendered as bubbles",
            "Bar",
            blue,
            listOf(
                ChartVariant("Default") { BubbleBarChart(data = { bars }, modifier = chartFill) },
                ChartVariant("Tap to select (onBarClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a bubble bar", selected = selected) { chartModifier ->
                        BubbleBarChart(
                            data = { bars },
                            onBarClick = { bar -> selected = "${bar.label} = ${bar.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant(
                    "Gradient",
                ) { BubbleBarChart(data = { bars }, color = blueGradient, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Waterfall",
            "Running cumulative total",
            "Bar",
            green,
            listOf(
                ChartVariant("Default") { WaterfallChart(data = { bars }, modifier = chartFill) },
                ChartVariant("Tap to select (onBarClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a step", selected = selected) { chartModifier ->
                        WaterfallChart(
                            data = { bars },
                            onBarClick = { bar -> selected = "${bar.label} = ${bar.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant("With ups and downs") { WaterfallChart(data = { barsSigned }, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Wavy Bar",
            "Bars with a wavy top",
            "Bar",
            purple,
            listOf(
                ChartVariant("Default") { WavyChart(data = { bars }, modifier = chartFill) },
                ChartVariant("Gradient") { WavyChart(data = { bars }, color = blueGradient, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Span (Range) Chart",
            "Start-to-end ranges",
            "Bar",
            red,
            listOf(
                ChartVariant("Default") {
                    SpanChart(
                        data = {
                            listOf(
                                SpanData(label = "Mon", startValue = 10f, endValue = 40f),
                                SpanData(label = "Tue", startValue = 20f, endValue = 60f),
                                SpanData(label = "Wed", startValue = 15f, endValue = 35f),
                                SpanData(label = "Thu", startValue = 30f, endValue = 70f),
                            )
                        },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Custom palette") {
                    SpanChart(
                        data = {
                            listOf(
                                SpanData(label = "Mon", startValue = 10f, endValue = 40f),
                                SpanData(label = "Tue", startValue = 20f, endValue = 60f),
                                SpanData(label = "Wed", startValue = 15f, endValue = 35f),
                                SpanData(label = "Thu", startValue = 30f, endValue = 70f),
                            )
                        },
                        colors = palette,
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Line Chart",
            "Trend over an axis",
            "Line & Area",
            blue,
            listOf(
                ChartVariant("Straight") { LineChart(data = { line }, modifier = chartFill) },
                ChartVariant("Negative values (below axis)") {
                    LineChart(data = { lineSigned }, modifier = chartFill)
                },
                ChartVariant("Negative values (from min)") {
                    LineChart(
                        data = {
                            lineSigned
                        },
                        lineConfig =
                            LineChartConfig(
                                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Tap a point (onPointClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a point", selected = selected) { chartModifier ->
                        LineChart(
                            data = { line },
                            lineConfig = LineChartConfig(highlightSelectedColumn = true),
                            onPointClick = { point -> selected = "${point.label} = ${point.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant("Tooltip: canvas (default)") {
                    LineChart(data = { line }, onPointClick = {}, tooltip = ChartTooltip.canvas(), modifier = chartFill)
                },
                ChartVariant("Tooltip: pill (compose preset)") {
                    LineChart(
                        data = { line },
                        onPointClick = {},
                        tooltip = ChartTooltip.compose { PillTooltip() },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Tooltip: fully custom") {
                    LineChart(
                        data = { line },
                        onPointClick = {},
                        tooltip =
                            ChartTooltip.compose {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF212121),
                                    shadowElevation = 6.dp,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(
                                                        8.dp,
                                                    ).clip(CircleShape)
                                                    .background(Color(0xFF43A047)),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = text, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Crosshair: default label (drag me)") {
                    LineChart(data = { line }, crosshair = ChartCrosshair(), modifier = chartFill)
                },
                ChartVariant("Crosshair: custom label (drag me)") {
                    LineChart(
                        data = { line },
                        crosshair =
                            ChartCrosshair(
                                label = {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF1E88E5),
                                        shadowElevation = 4.dp,
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text(
                                                text = data.label,
                                                color = Color.White.copy(alpha = 0.8f),
                                                style = MaterialTheme.typography.labelSmall,
                                            )
                                            Text(
                                                text = "${data.value}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                },
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Smooth curve") {
                    LineChart(
                        data = { line },
                        lineConfig = LineChartConfig(interpolation = LineInterpolation.SMOOTH),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Step line") {
                    LineChart(
                        data = { line },
                        lineConfig = LineChartConfig(interpolation = LineInterpolation.STEP),
                        modifier = chartFill,
                    )
                },
                ChartVariant(
                    "Gradient line",
                ) { LineChart(data = { line }, color = blueGradient, modifier = chartFill) },
                ChartVariant("Line only (no points)") {
                    LineChart(data = { line }, lineConfig = LineChartConfig(showPoints = false), modifier = chartFill)
                },
                ChartVariant("Pinned markers") {
                    LineChart(
                        data = {
                            line
                        },
                        lineConfig =
                            LineChartConfig(
                                markers = listOf(PersistentMarker(dataIndex = 3, label = "Peak")),
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Reference band") {
                    LineChart(
                        data = {
                            line
                        },
                        lineConfig =
                            LineChartConfig(
                                referenceBand =
                                    ReferenceBandConfig(
                                        lowValue = 30f,
                                        highValue = 55f,
                                        fill = ChartyColor.Solid(green),
                                        label = "Target",
                                    ),
                            ),
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Area Chart",
            "Line with a filled area",
            "Line & Area",
            green,
            listOf(
                ChartVariant("Default") { AreaChart(data = { line }, modifier = chartFill) },
                ChartVariant("Income 2022 (marker + guide line)") {
                    AreaChart(
                        data = {
                            listOf(
                                LineData(label = "Jan", value = 4200f),
                                LineData(label = "Feb", value = 5600f),
                                LineData(label = "Mar", value = 5100f),
                                LineData(label = "Apr", value = 8900f),
                                LineData(label = "May", value = 9700f),
                            )
                        },
                        color = ChartyColor.Gradient(listOf(Color(0xFF43A047), Color(0x1043A047))),
                        lineConfig =
                            LineChartConfig(
                                interpolation = LineInterpolation.SMOOTH,
                                showPoints = false,
                                markers =
                                    listOf(
                                        PersistentMarker(
                                            dataIndex = 3,
                                            label = "$8,900",
                                            showGuideLine = true,
                                            dotColor = ChartyColor.Solid(Color(0xFFFF9800)),
                                            labelBackgroundColor = ChartyColor.Solid(Color(0xFF43A047)),
                                        ),
                                    ),
                            ),
                        scaffoldConfig = ChartScaffoldConfig(showAxis = false, showGrid = false),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Smooth curve") {
                    AreaChart(data = { line }, lineConfig = LineChartConfig(smoothCurve = true), modifier = chartFill)
                },
                ChartVariant(
                    "Warm gradient",
                ) { AreaChart(data = { line }, color = warmGradient, modifier = chartFill) },
                ChartVariant("Tap a point (onPointClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a point", selected = selected) { chartModifier ->
                        AreaChart(
                            data = { line },
                            onPointClick = { point -> selected = "${point.label} = ${point.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant("Tooltip: pill (compose)") {
                    AreaChart(
                        data = { line },
                        onPointClick = {},
                        tooltip = ChartTooltip.compose { PillTooltip() },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Multiline Chart",
            "Several series at once",
            "Line & Area",
            orange,
            listOf(
                ChartVariant("Default") { MultilineChart(data = { lineGroups }, modifier = chartFill) },
                ChartVariant("Gradient fill") {
                    MultilineChart(
                        data = { lineGroups },
                        lineConfig = LineChartConfig(showGradientFill = true),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Smooth curves") {
                    MultilineChart(
                        data = { lineGroups },
                        lineConfig = LineChartConfig(smoothCurve = true),
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Stacked Area",
            "Cumulative stacked areas",
            "Line & Area",
            purple,
            listOf(
                ChartVariant("Default") { StackedAreaChart(data = { lineGroups }, modifier = chartFill) },
                ChartVariant(
                    "Custom palette",
                ) { StackedAreaChart(data = { lineGroups }, colors = palette, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Point (Scatter)",
            "Individual data points",
            "Point",
            blue,
            listOf(
                ChartVariant("Default") { PointChart(data = { points }, modifier = chartFill) },
                ChartVariant("Negative values (below axis)") {
                    PointChart(data = { pointsSigned }, modifier = chartFill)
                },
                ChartVariant("Tap a point (onPointClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a point", selected = selected) { chartModifier ->
                        PointChart(
                            data = { points },
                            pointConfig = PointChartConfig(highlightSelectedColumn = true),
                            onPointClick = { point -> selected = "${point.label} = ${point.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant("Tooltip: pill (compose)") {
                    PointChart(
                        data = { points },
                        onPointClick = {},
                        tooltip = ChartTooltip.compose { PillTooltip() },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Value labels") {
                    PointChart(
                        data = { points },
                        pointConfig = PointChartConfig(showLabels = true),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Pinned markers") {
                    PointChart(
                        data = {
                            points
                        },
                        pointConfig =
                            PointChartConfig(
                                markers = listOf(PersistentMarker(dataIndex = 3, label = "Max")),
                            ),
                        modifier = chartFill,
                    )
                },
                ChartVariant(
                    "Gradient points",
                ) { PointChart(data = { points }, color = blueGradient, modifier = chartFill) },
            ),
        ),
        ChartDemo(
            "Bubble Chart",
            "Points sized by a value",
            "Point",
            red,
            listOf(
                ChartVariant("Default") {
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
                ChartVariant("Gradient") {
                    BubbleChart(
                        data = {
                            listOf(
                                BubbleData("A", yValue = 30f, size = 120f),
                                BubbleData("B", yValue = 60f, size = 220f),
                                BubbleData("C", yValue = 45f, size = 160f),
                                BubbleData("D", yValue = 80f, size = 90f),
                            )
                        },
                        color = warmGradient,
                        modifier = chartFill,
                    )
                },
                ChartVariant("Tap a bubble (onBubbleClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a bubble", selected = selected) { chartModifier ->
                        BubbleChart(
                            data = {
                                listOf(
                                    BubbleData("A", yValue = 30f, size = 120f),
                                    BubbleData("B", yValue = 60f, size = 220f),
                                    BubbleData("C", yValue = 45f, size = 160f),
                                    BubbleData("D", yValue = 80f, size = 90f),
                                )
                            },
                            onBubbleClick = { bubble ->
                                selected =
                                    "${bubble.label} = ${bubble.yValue} (size ${bubble.size})"
                            },
                            modifier = chartModifier,
                        )
                    }
                },
            ),
        ),
        ChartDemo(
            "Combo Chart",
            "Bars plus a line",
            "Point",
            orange,
            listOf(
                ChartVariant("Shared axis") {
                    ComboChart(
                        data = {
                            listOf(
                                ComboChartData(label = "Jan", barValue = 120f, lineValue = 40f),
                                ComboChartData(label = "Feb", barValue = 180f, lineValue = 65f),
                                ComboChartData(label = "Mar", barValue = 90f, lineValue = 55f),
                                ComboChartData(label = "Apr", barValue = 210f, lineValue = 80f),
                            )
                        },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Dual Y axis") {
                    ComboChart(
                        data = {
                            listOf(
                                ComboChartData(label = "Jan", barValue = 1200f, lineValue = 4f),
                                ComboChartData(label = "Feb", barValue = 1800f, lineValue = 6.5f),
                                ComboChartData(label = "Mar", barValue = 900f, lineValue = 5.5f),
                                ComboChartData(label = "Apr", barValue = 2100f, lineValue = 8f),
                            )
                        },
                        comboConfig = ComboChartConfig(secondaryAxisForLine = true),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Tap a column (onDataClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap a column", selected = selected) { chartModifier ->
                        ComboChart(
                            data = {
                                listOf(
                                    ComboChartData(label = "Jan", barValue = 120f, lineValue = 40f),
                                    ComboChartData(label = "Feb", barValue = 180f, lineValue = 65f),
                                    ComboChartData(label = "Mar", barValue = 90f, lineValue = 55f),
                                    ComboChartData(label = "Apr", barValue = 210f, lineValue = 80f),
                                )
                            },
                            onDataClick = { d -> selected = "${d.label}: bar ${d.barValue}, line ${d.lineValue}" },
                            modifier = chartModifier,
                        )
                    }
                },
            ),
        ),
        ChartDemo(
            "Candlestick",
            "Financial OHLC prices",
            "Point",
            green,
            listOf(
                ChartVariant("Default") {
                    CandlestickChart(
                        data = {
                            listOf(
                                CandleData(label = "Mon", open = 100f, high = 130f, low = 90f, close = 120f),
                                CandleData(label = "Tue", open = 120f, high = 140f, low = 110f, close = 115f),
                                CandleData(label = "Wed", open = 115f, high = 125f, low = 95f, close = 105f),
                                CandleData(label = "Thu", open = 105f, high = 150f, low = 100f, close = 145f),
                            )
                        },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Pie Chart",
            "Parts of a whole",
            "Circular",
            purple,
            listOf(
                ChartVariant("Pie") {
                    PieChart(
                        data = {
                            listOf(
                                PieData(label = "A", value = 40f, color = ChartyColor.Solid(blue)),
                                PieData(label = "B", value = 25f, color = ChartyColor.Solid(green)),
                                PieData(label = "C", value = 20f, color = ChartyColor.Solid(orange)),
                                PieData(label = "D", value = 15f, color = ChartyColor.Solid(red)),
                            )
                        },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Donut") {
                    PieChart(
                        data = {
                            listOf(
                                PieData(label = "A", value = 40f, color = ChartyColor.Solid(blue)),
                                PieData(label = "B", value = 25f, color = ChartyColor.Solid(green)),
                                PieData(label = "C", value = 20f, color = ChartyColor.Solid(orange)),
                                PieData(label = "D", value = 15f, color = ChartyColor.Solid(red)),
                            )
                        },
                        config = PieChartConfig(style = PieChartStyle.DONUT),
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Circular Progress",
            "Concentric progress rings",
            "Circular",
            blue,
            listOf(
                ChartVariant("Three rings") {
                    CircularProgressIndicator(
                        rings = {
                            listOf(
                                CircularRingData(
                                    label = "Steps",
                                    progress = 72f,
                                    maxValue = 100f,
                                    color = ChartyColor.Solid(blue),
                                ),
                                CircularRingData(
                                    label = "Move",
                                    progress = 55f,
                                    maxValue = 100f,
                                    color = ChartyColor.Solid(green),
                                ),
                                CircularRingData(
                                    label = "Stand",
                                    progress = 88f,
                                    maxValue = 100f,
                                    color = ChartyColor.Solid(orange),
                                ),
                            )
                        },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Single ring") {
                    CircularProgressIndicator(
                        rings = {
                            listOf(
                                CircularRingData(
                                    label = "Goal",
                                    progress = 64f,
                                    maxValue = 100f,
                                    color = ChartyColor.Solid(purple),
                                ),
                            )
                        },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Radar Chart",
            "Multi-axis profile",
            "Circular",
            red,
            listOf(
                ChartVariant("Blue profile") {
                    RadarChart(
                        data = {
                            listOf(
                                RadarDataSet(
                                    label = "Player",
                                    axes =
                                        listOf(
                                            RadarAxisData(label = "Speed", value = 80f),
                                            RadarAxisData(label = "Power", value = 65f),
                                            RadarAxisData(label = "Skill", value = 90f),
                                            RadarAxisData(label = "Stamina", value = 70f),
                                            RadarAxisData(label = "Defense", value = 55f),
                                        ),
                                    color = ChartyColor.Solid(blue),
                                ),
                            )
                        },
                        modifier = chartFill,
                    )
                },
                ChartVariant("Axis labels + values") {
                    RadarChart(
                        data = {
                            listOf(
                                RadarDataSet(
                                    label = "Player",
                                    axes =
                                        listOf(
                                            RadarAxisData(label = "Speed", value = 80f),
                                            RadarAxisData(label = "Power", value = 65f),
                                            RadarAxisData(label = "Skill", value = 90f),
                                            RadarAxisData(label = "Stamina", value = 70f),
                                            RadarAxisData(label = "Defense", value = 55f),
                                        ),
                                    color = ChartyColor.Solid(purple),
                                ),
                            )
                        },
                        config = RadarChartConfig(labelConfig = RadarLabelConfig(showLabels = true, showValues = true)),
                        modifier = chartFill,
                    )
                },
                ChartVariant("Tap an axis (onAxisClick)") {
                    var selected by remember { mutableStateOf<String?>(null) }
                    SelectionColumn(hint = "Tap an axis", selected = selected) { chartModifier ->
                        RadarChart(
                            data = {
                                listOf(
                                    RadarDataSet(
                                        label = "Player",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Speed", value = 80f),
                                                RadarAxisData(label = "Power", value = 65f),
                                                RadarAxisData(label = "Skill", value = 90f),
                                                RadarAxisData(label = "Stamina", value = 70f),
                                                RadarAxisData(label = "Defense", value = 55f),
                                            ),
                                        color = ChartyColor.Solid(blue),
                                    ),
                                )
                            },
                            onAxisClick = { axis, _ -> selected = "${axis.label} = ${axis.value}" },
                            modifier = chartModifier,
                        )
                    }
                },
                ChartVariant("Warm profile") {
                    RadarChart(
                        data = {
                            listOf(
                                RadarDataSet(
                                    label = "Rival",
                                    axes =
                                        listOf(
                                            RadarAxisData(label = "Speed", value = 60f),
                                            RadarAxisData(label = "Power", value = 85f),
                                            RadarAxisData(label = "Skill", value = 60f),
                                            RadarAxisData(label = "Stamina", value = 90f),
                                            RadarAxisData(label = "Defense", value = 75f),
                                        ),
                                    color = ChartyColor.Solid(orange),
                                ),
                            )
                        },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Multiple Radar",
            "Several profiles overlaid",
            "Circular",
            purple,
            listOf(
                ChartVariant("Two profiles") {
                    MultipleRadarChart(
                        dataSets = {
                            listOf(
                                RadarDataSet(
                                    label = "A",
                                    axes =
                                        listOf(
                                            RadarAxisData("Speed", 80f),
                                            RadarAxisData("Power", 60f),
                                            RadarAxisData("Skill", 90f),
                                            RadarAxisData("Stamina", 70f),
                                        ),
                                    color = ChartyColor.Solid(blue),
                                ),
                                RadarDataSet(
                                    label = "B",
                                    axes =
                                        listOf(
                                            RadarAxisData("Speed", 55f),
                                            RadarAxisData("Power", 85f),
                                            RadarAxisData("Skill", 60f),
                                            RadarAxisData("Stamina", 90f),
                                        ),
                                    color = ChartyColor.Solid(orange),
                                ),
                            )
                        },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Block Bar",
            "Segmented value blocks",
            "Specialized",
            green,
            listOf(
                ChartVariant("Default") {
                    BlockBarChart(
                        data = {
                            listOf(
                                BlockData(value = 40f, color = ChartyColor.Solid(blue)),
                                BlockData(value = 30f, color = ChartyColor.Solid(green)),
                                BlockData(value = 20f, color = ChartyColor.Solid(orange)),
                                BlockData(value = 10f, color = ChartyColor.Solid(red)),
                            )
                        },
                        modifier = chartFill,
                    )
                },
            ),
        ),
        ChartDemo(
            "Calendar Heatmap",
            "GitHub-style activity grid",
            "Specialized",
            green,
            listOf(
                ChartVariant("90 days") {
                    CalendarHeatmapChart(
                        data = {
                            (1..90).map { day ->
                                CalendarData(
                                    year = 2024,
                                    month = ((day - 1) / 30) + 1,
                                    day = ((day - 1) % 30) + 1,
                                    value =
                                        (
                                            day *
                                                7 %
                                                10
                                        ).toFloat(),
                                )
                            }
                        },
                        modifier = chartFill,
                    )
                },
            ),
        ),
    )
}
