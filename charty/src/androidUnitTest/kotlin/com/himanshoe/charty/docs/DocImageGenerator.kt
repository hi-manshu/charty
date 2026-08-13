package com.himanshoe.charty.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.captureRoboImage
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
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.block.BlockBarChart
import com.himanshoe.charty.block.config.BlockBarChartConfig
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.calendar.CalendarHeatmapChart
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.CellShape
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.candlestick.CandlestickChart
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.circular.CircularProgressIndicator
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.combo.ComboChart
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.gauge.AngularGaugeChart
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
import com.himanshoe.charty.heatmap.MatrixHeatmapChart
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.Sparkline
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.config.SparklineConfig
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
import com.himanshoe.charty.radar.config.MultipleRadarChartConfig
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.data.RadarAxisData
import com.himanshoe.charty.radar.data.RadarDataSet
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders one marketing-quality still of every public chart into `docs/charty/img/`.
 *
 * This is a documentation-asset generator, not a regression test: it never asserts and it always
 * overwrites its output. Golden-image regressions live in
 * [com.himanshoe.charty.snapshot.ChartSnapshotTest] instead, so a rendering change fails there
 * rather than silently rewriting the published docs.
 *
 * Regenerate every image with:
 * `./gradlew :charty:testDebugUnitTest --tests "com.himanshoe.charty.docs.DocImageGenerator"`
 *
 * Every chart is rendered with [Animation.Disabled] where the API allows it, so the capture shows
 * the settled end state rather than a frame of the entrance animation.
 *
 * Method order is pinned because [wavyChart] is the one chart whose motion is an infinite
 * transition: it leaves Robolectric's main looper permanently non-idle, so any capture scheduled
 * after it would spin forever. Sorting by name places it last, after every other capture.
 */
@RunWith(RobolectricTestRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [DOC_SDK], qualifiers = DOC_QUALIFIERS)
class DocImageGenerator {
    @Test
    fun barChart() =
        capture(name = "bar_chart") {
            BarChart(
                data = { revenueBars },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Indigo),
                barConfig =
                    BarChartConfig(
                        animation = Animation.Disabled,
                        showDataLabels = true,
                    ),
            )
        }

    @Test
    fun horizontalBarChart() =
        capture(name = "horizontal_bar_chart") {
            HorizontalBarChart(
                data = { channelBars },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Teal),
                barConfig = BarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun stackedBarChart() =
        capture(name = "stacked_bar_chart") {
            StackedBarChart(
                data = { platformGroups },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                stackedConfig = StackedBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun stackedHorizontalBarChart() =
        capture(name = "stacked_horizontal_bar_chart") {
            StackedHorizontalBarChart(
                data = { platformGroups },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                config = StackedHorizontalBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun groupedHorizontalBarChart() =
        capture(name = "grouped_horizontal_bar_chart") {
            GroupedHorizontalBarChart(
                data = { regionGroups },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                config = GroupedHorizontalBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun normalizedHorizontalBarChart() =
        capture(name = "normalized_horizontal_bar_chart") {
            NormalizedHorizontalBarChart(
                data = { platformGroups },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                config = NormalizedHorizontalBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun mosaicBarChart() =
        capture(name = "mosaic_bar_chart") {
            MosaicBarChart(
                data = { mosaicGroups },
                modifier = Modifier.fillMaxSize(),
                config = MosaicBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun spanChart() =
        capture(name = "span_chart") {
            SpanChart(
                data = { temperatureSpans },
                modifier = Modifier.fillMaxSize(),
                colors = ChartyColor.Gradient(colors = listOf(Cyan, Indigo)),
                barConfig = BarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun comparisonBarChart() =
        capture(name = "comparison_bar_chart") {
            ComparisonBarChart(
                data = { planVsActualGroups },
                modifier = Modifier.fillMaxSize(),
                comparisonConfig = ComparisonBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun waterfallChart() =
        capture(name = "waterfall_chart") {
            WaterfallChart(
                data = { cashFlowBars },
                modifier = Modifier.fillMaxSize(),
                config =
                    WaterfallChartConfig(
                        animation = Animation.Disabled,
                        positiveColor = ChartyColor.Solid(color = Teal),
                        negativeColor = ChartyColor.Solid(color = Pink),
                    ),
            )
        }

    @Test
    fun lollipopBarChart() =
        capture(name = "lollipop_bar_chart") {
            LollipopBarChart(
                data = { channelBars },
                modifier = Modifier.fillMaxSize(),
                colors = ChartyColor.Solid(color = Indigo),
                config = LollipopBarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun bubbleBarChart() =
        capture(name = "bubble_bar_chart") {
            BubbleBarChart(
                data = { revenueBars },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Cyan),
                bubbleConfig =
                    BubbleBarChartConfig(
                        animation = Animation.Disabled,
                        bubbleRadius = 14f,
                        bubbleSpacing = 10f,
                    ),
            )
        }

    @Test
    fun wavyChart() =
        capture(name = "wavy_chart") {
            WavyChart(
                data = { revenueBars },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Indigo),
                wavyConfig = WavyChartConfig(),
            )
        }

    @Test
    fun lineChart() =
        capture(name = "line_chart") {
            LineChart(
                data = { sessionsLine },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Indigo),
                lineConfig =
                    LineChartConfig(
                        animation = Animation.Disabled,
                        smoothCurve = true,
                    ),
            )
        }

    @Test
    fun areaChart() =
        capture(name = "area_chart") {
            AreaChart(
                data = { sessionsLine },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Gradient(colors = listOf(Indigo, Indigo.copy(alpha = 0.15f))),
                lineConfig =
                    LineChartConfig(
                        animation = Animation.Disabled,
                        smoothCurve = true,
                    ),
            )
        }

    @Test
    fun multilineChart() =
        capture(name = "multiline_chart") {
            MultilineChart(
                data = { trafficSeries },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                lineConfig = LineChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun stackedAreaChart() =
        capture(name = "stacked_area_chart") {
            StackedAreaChart(
                data = { trafficSeries },
                modifier = Modifier.fillMaxSize(),
                colors = palette,
                lineConfig = LineChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun sparkline() =
        capture(name = "sparkline", width = 420.dp, height = 140.dp) {
            Sparkline(
                data = { sparklineValues },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Indigo),
                config = SparklineConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun pointChart() =
        capture(name = "point_chart") {
            PointChart(
                data = { latencyPoints },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Amber),
                pointConfig = PointChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun bubbleChart() =
        capture(name = "bubble_chart") {
            BubbleChart(
                data = { marketBubbles },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Cyan),
                config =
                    PointChartConfig(
                        animation = Animation.Disabled,
                        pointRadius = 34f,
                    ),
                minBubbleRadius = 8f,
            )
        }

    @Test
    fun comboChart() =
        capture(name = "combo_chart") {
            ComboChart(
                data = { comboSeries },
                modifier = Modifier.fillMaxSize(),
                barColor = ChartyColor.Solid(color = Indigo),
                lineColor = ChartyColor.Solid(color = Amber),
                comboConfig = ComboChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun candlestickChart() =
        capture(name = "candlestick_chart") {
            CandlestickChart(
                data = { candles },
                modifier = Modifier.fillMaxSize(),
                bullishColor = ChartyColor.Solid(color = Teal),
                bearishColor = ChartyColor.Solid(color = Pink),
                candlestickConfig = CandlestickChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun pieChart() =
        capture(name = "pie_chart", width = SQUARE, height = SQUARE) {
            PieChart(
                data = { deviceSlices },
                modifier = Modifier.fillMaxSize(),
                config = PieChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun radarChart() =
        capture(name = "radar_chart", width = SQUARE, height = SQUARE) {
            RadarChart(
                data = { listOf(radarThisQuarter) },
                modifier = Modifier.fillMaxSize(),
                config = RadarChartConfig(animation = Animation.Disabled),
            )
        }

    @Test
    fun multipleRadarChart() =
        capture(name = "multiple_radar_chart", width = SQUARE, height = SQUARE) {
            MultipleRadarChart(
                dataSets = { listOf(radarThisQuarter, radarLastQuarter, radarTarget) },
                modifier = Modifier.fillMaxSize(),
                config =
                    MultipleRadarChartConfig(
                        radarConfig = RadarChartConfig(animation = Animation.Disabled),
                        staggerAnimation = false,
                    ),
            )
        }

    @Test
    fun circularProgressIndicator() =
        capture(name = "circular_progress_indicator", width = SQUARE, height = SQUARE) {
            CircularProgressIndicator(
                rings = { activityRings },
                modifier = Modifier.fillMaxSize(),
                config =
                    CircularProgressConfig(
                        animation = Animation.Disabled,
                        centerHoleRatio = 0.25f,
                    ),
            )
        }

    @Test
    fun blockBarChart() =
        capture(name = "block_bar_chart", width = 420.dp, height = 110.dp) {
            BlockBarChart(
                data = { storageBlocks },
                modifier = Modifier.fillMaxSize(),
                blockBarConfig = BlockBarChartConfig(barHeight = 22.dp),
            )
        }

    @Test
    fun calendarHeatmapChart() =
        capture(name = "calendar_heatmap_chart", width = 900.dp, height = 200.dp) {
            CalendarHeatmapChart(
                data = { contributionYear },
                modifier = Modifier.fillMaxSize(),
                config =
                    CalendarHeatmapConfig(
                        animation = Animation.Disabled,
                        intensityColors = heatIntensities,
                        cellShape = CellShape.RoundedSquare(cornerRadius = 3f),
                    ),
                scrollEnabled = false,
            )
        }

    @Test
    fun matrixHeatmapChart() =
        capture(name = "matrix_heatmap_chart", width = 700.dp, height = 380.dp) {
            MatrixHeatmapChart(
                data = { trafficMatrix },
                modifier = Modifier.fillMaxSize(),
                config =
                    MatrixHeatmapConfig(
                        animation = Animation.Disabled,
                        colorScale = ChartyColor.Gradient(colors = listOf(Indigo.copy(alpha = 0.12f), Indigo)),
                        showValues = true,
                    ),
            )
        }

    @Test
    fun angularGaugeChart() =
        capture(name = "angular_gauge_chart", width = SQUARE, height = SQUARE) {
            AngularGaugeChart(
                value = { 72f },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Indigo),
                config =
                    AngularGaugeConfig(
                        animation = Animation.Disabled,
                        trackColor = ChartyColor.Solid(color = Indigo.copy(alpha = 0.12f)),
                    ),
            )
        }

    @Test
    fun barChartMarkers() =
        capture(name = "bar_chart_markers") {
            BarChart(
                data = { revenueBars },
                modifier = Modifier.fillMaxSize(),
                color = ChartyColor.Solid(color = Indigo),
                barConfig =
                    BarChartConfig(
                        animation = Animation.Disabled,
                        showDataLabels = true,
                        referenceLine =
                            ReferenceLineConfig(
                                value = 60f,
                                color = ChartyColor.Solid(Pink),
                                label = "Target",
                            ),
                        markers =
                            listOf(
                                PersistentMarker(
                                    dataIndex = 5,
                                    label = "Best month",
                                    dotColor = ChartyColor.Solid(color = Pink),
                                    labelBackgroundColor = ChartyColor.Solid(color = Pink),
                                    showGuideLine = true,
                                ),
                            ),
                    ),
            )
        }

    @Test
    fun lineChartInterpolation() =
        capture(name = "line_chart_interpolation", width = 820.dp, height = 300.dp) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            ) {
                interpolationModes.forEach { mode ->
                    Column(
                        modifier = Modifier.weight(weight = 1f),
                        verticalArrangement = Arrangement.spacedBy(space = 6.dp),
                    ) {
                        BasicText(
                            text = mode.second,
                            style =
                                TextStyle(
                                    color = Indigo,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                        )
                        LineChart(
                            data = { interpolationLine },
                            modifier = Modifier.fillMaxWidth().weight(weight = 1f),
                            color = ChartyColor.Solid(color = Indigo),
                            lineConfig =
                                LineChartConfig(
                                    animation = Animation.Disabled,
                                    interpolation = mode.first,
                                    pointRadius = 4f,
                                ),
                            scaffoldConfig = ChartScaffoldConfig(showLabels = false),
                        )
                    }
                }
            }
        }

    @Test
    fun pieChartDonut() =
        capture(name = "pie_chart_donut", width = SQUARE, height = SQUARE) {
            PieChart(
                data = { deviceSlices },
                modifier = Modifier.fillMaxSize(),
                config =
                    PieChartConfig(
                        animation = Animation.Disabled,
                        style = PieChartStyle.DONUT,
                        donutHoleRatio = 0.58f,
                        sliceSpacingDegrees = 1.5f,
                    ),
                centerContent = {
                    BasicText(
                        text = "100%",
                        style =
                            TextStyle(
                                color = Indigo,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                },
            )
        }

    @Test
    fun comboChartSecondaryAxis() =
        capture(name = "combo_chart_secondary_axis") {
            ComboChart(
                data = { comboSecondarySeries },
                modifier = Modifier.fillMaxSize(),
                barColor = ChartyColor.Solid(color = Indigo),
                lineColor = ChartyColor.Solid(color = Amber),
                comboConfig =
                    ComboChartConfig(
                        animation = Animation.Disabled,
                        secondaryAxisForLine = true,
                        smoothCurve = true,
                    ),
            )
        }

    private fun capture(
        name: String,
        width: Dp = WIDE,
        height: Dp = TALL,
        content: @Composable () -> Unit,
    ) {
        captureRoboImage(filePath = "$DOC_IMAGE_DIR/$name.png") {
            Box(
                modifier =
                    Modifier
                        .size(width = width, height = height)
                        .background(color = Color.White)
                        .padding(all = 16.dp),
            ) {
                content()
            }
        }
    }
}

private const val DOC_SDK = 34
private const val DOC_QUALIFIERS = "w1200dp-h1200dp-mdpi"
private const val DOC_IMAGE_DIR = "../docs/charty/img"

private val WIDE = 800.dp
private val TALL = 500.dp
private val SQUARE = 480.dp

private val Indigo = ChartyColors.Indigo
private val Teal = ChartyColors.Teal
private val Amber = ChartyColors.Amber
private val Pink = ChartyColors.Pink
private val Cyan = ChartyColors.Cyan

private val palette = ChartyColor.Gradient(colors = listOf(Indigo, Teal, Amber, Pink, Cyan))

private val heatIntensities =
    listOf(
        Indigo.copy(alpha = 0.25f),
        Indigo.copy(alpha = 0.5f),
        Indigo.copy(alpha = 0.75f),
        Indigo,
    )

private fun bars(vararg entries: Pair<String, Float>): List<BarData> =
    entries.map { entry -> BarData(label = entry.first, value = entry.second) }

private fun lines(vararg entries: Pair<String, Float>): List<LineData> =
    entries.map { entry -> LineData(label = entry.first, value = entry.second) }

private fun points(vararg entries: Pair<String, Float>): List<PointData> =
    entries.map { entry -> PointData(label = entry.first, value = entry.second) }

private val revenueBars =
    bars(
        "Jan" to 42f,
        "Feb" to 58f,
        "Mar" to 51f,
        "Apr" to 73f,
        "May" to 66f,
        "Jun" to 88f,
    )

private val channelBars =
    bars(
        "Organic" to 64f,
        "Referral" to 47f,
        "Email" to 38f,
        "Social" to 29f,
        "Paid" to 21f,
    )

private val cashFlowBars =
    bars(
        "Opening" to 120f,
        "Sales" to 68f,
        "Services" to 34f,
        "Payroll" to -52f,
        "Cloud" to -23f,
        "Closing" to 147f,
    )

private val sessionsLine =
    lines(
        "Mon" to 1240f,
        "Tue" to 1690f,
        "Wed" to 1480f,
        "Thu" to 2130f,
        "Fri" to 2460f,
        "Sat" to 1890f,
        "Sun" to 1620f,
    )

private val latencyPoints =
    points(
        "p10" to 82f,
        "p25" to 104f,
        "p50" to 131f,
        "p75" to 188f,
        "p90" to 264f,
        "p99" to 412f,
    )

private val sparklineValues =
    listOf(
        18f,
        22f,
        19f,
        27f,
        31f,
        26f,
        34f,
        39f,
        35f,
        44f,
        41f,
        49f,
        55f,
        52f,
        61f,
        58f,
        67f,
        74f,
        70f,
        82f,
    )

private val platformGroups =
    listOf(
        BarGroup(label = "Q1", values = listOf(38f, 24f, 16f)),
        BarGroup(label = "Q2", values = listOf(45f, 29f, 18f)),
        BarGroup(label = "Q3", values = listOf(52f, 31f, 22f)),
        BarGroup(label = "Q4", values = listOf(61f, 40f, 27f)),
    )

private val segmentColors =
    listOf(
        ChartyColor.Solid(color = Indigo),
        ChartyColor.Solid(color = Teal),
        ChartyColor.Solid(color = Amber),
    )

private val mosaicGroups =
    platformGroups.map { group -> group.copy(colors = segmentColors) }

private val regionGroups =
    listOf(
        BarGroup(label = "EMEA", values = listOf(72f, 58f)),
        BarGroup(label = "AMER", values = listOf(94f, 81f)),
        BarGroup(label = "APAC", values = listOf(63f, 47f)),
    )

private val comparisonColors =
    listOf(
        ChartyColor.Solid(color = Indigo),
        ChartyColor.Solid(color = Amber),
    )

private val planVsActualGroups =
    listOf(
        BarGroup(label = "Jan", values = listOf(48f, 42f), colors = comparisonColors),
        BarGroup(label = "Feb", values = listOf(55f, 61f), colors = comparisonColors),
        BarGroup(label = "Mar", values = listOf(60f, 54f), colors = comparisonColors),
        BarGroup(label = "Apr", values = listOf(66f, 78f), colors = comparisonColors),
    )

private val temperatureSpans =
    listOf(
        SpanData(label = "Mon", startValue = 11f, endValue = 21f),
        SpanData(label = "Tue", startValue = 13f, endValue = 24f),
        SpanData(label = "Wed", startValue = 12f, endValue = 26f),
        SpanData(label = "Thu", startValue = 15f, endValue = 29f),
        SpanData(label = "Fri", startValue = 17f, endValue = 31f),
    )

private val trafficSeries =
    listOf(
        LineGroup(label = "Jan", values = listOf(120f, 96f, 42f)),
        LineGroup(label = "Feb", values = listOf(168f, 108f, 51f)),
        LineGroup(label = "Mar", values = listOf(152f, 132f, 47f)),
        LineGroup(label = "Apr", values = listOf(210f, 121f, 63f)),
        LineGroup(label = "May", values = listOf(244f, 154f, 58f)),
        LineGroup(label = "Jun", values = listOf(198f, 176f, 71f)),
    )

private val marketBubbles =
    listOf(
        BubbleData(label = "Alpha", xValue = 12f, yValue = 34f, size = 18f),
        BubbleData(label = "Bravo", xValue = 26f, yValue = 58f, size = 34f),
        BubbleData(label = "Charlie", xValue = 41f, yValue = 46f, size = 26f),
        BubbleData(label = "Delta", xValue = 58f, yValue = 77f, size = 44f),
        BubbleData(label = "Echo", xValue = 73f, yValue = 62f, size = 22f),
        BubbleData(label = "Foxtrot", xValue = 88f, yValue = 91f, size = 30f),
    )

private val interpolationModes =
    listOf(
        LineInterpolation.LINEAR to "LINEAR",
        LineInterpolation.SMOOTH to "SMOOTH",
        LineInterpolation.STEP to "STEP",
    )

private val interpolationLine =
    lines(
        "Mon" to 32f,
        "Tue" to 58f,
        "Wed" to 41f,
        "Thu" to 74f,
        "Fri" to 63f,
        "Sat" to 88f,
    )

private val comboSecondarySeries =
    listOf(
        ComboChartData(label = "Jan", barValue = 1240f, lineValue = 3.2f),
        ComboChartData(label = "Feb", barValue = 1680f, lineValue = 4.1f),
        ComboChartData(label = "Mar", barValue = 1510f, lineValue = 3.8f),
        ComboChartData(label = "Apr", barValue = 2130f, lineValue = 5.6f),
        ComboChartData(label = "May", barValue = 2460f, lineValue = 6.4f),
        ComboChartData(label = "Jun", barValue = 2890f, lineValue = 7.1f),
    )

private val comboSeries =
    listOf(
        ComboChartData(label = "Jan", barValue = 42f, lineValue = 18f),
        ComboChartData(label = "Feb", barValue = 58f, lineValue = 26f),
        ComboChartData(label = "Mar", barValue = 51f, lineValue = 31f),
        ComboChartData(label = "Apr", barValue = 73f, lineValue = 44f),
        ComboChartData(label = "May", barValue = 66f, lineValue = 52f),
        ComboChartData(label = "Jun", barValue = 88f, lineValue = 61f),
    )

private val candles =
    listOf(
        CandleData(label = "Mon", open = 128f, high = 137f, low = 125f, close = 134f),
        CandleData(label = "Tue", open = 134f, high = 141f, low = 130f, close = 131f),
        CandleData(label = "Wed", open = 131f, high = 133f, low = 121f, close = 123f),
        CandleData(label = "Thu", open = 123f, high = 136f, low = 122f, close = 135f),
        CandleData(label = "Fri", open = 135f, high = 148f, low = 134f, close = 146f),
        CandleData(label = "Mon", open = 146f, high = 151f, low = 140f, close = 142f),
        CandleData(label = "Tue", open = 142f, high = 156f, low = 141f, close = 154f),
        CandleData(label = "Wed", open = 154f, high = 162f, low = 150f, close = 159f),
    )

private val deviceSlices =
    listOf(
        PieData(label = "Mobile", value = 46f, color = ChartyColor.Solid(color = Indigo)),
        PieData(label = "Desktop", value = 28f, color = ChartyColor.Solid(color = Teal)),
        PieData(label = "Tablet", value = 14f, color = ChartyColor.Solid(color = Amber)),
        PieData(label = "TV", value = 7f, color = ChartyColor.Solid(color = Pink)),
        PieData(label = "Other", value = 5f, color = ChartyColor.Solid(color = Cyan)),
    )

private fun radarAxes(vararg entries: Pair<String, Float>): List<RadarAxisData> =
    entries.map { entry -> RadarAxisData(label = entry.first, value = entry.second, maxValue = 100f) }

private val radarThisQuarter =
    RadarDataSet(
        label = "This quarter",
        axes =
            radarAxes(
                "Speed" to 82f,
                "Reliability" to 74f,
                "Design" to 91f,
                "Support" to 66f,
                "Value" to 78f,
                "Docs" to 58f,
            ),
        color = ChartyColor.Solid(color = Indigo),
        fillAlpha = 0.22f,
    )

private val radarLastQuarter =
    RadarDataSet(
        label = "Last quarter",
        axes =
            radarAxes(
                "Speed" to 64f,
                "Reliability" to 68f,
                "Design" to 72f,
                "Support" to 59f,
                "Value" to 71f,
                "Docs" to 44f,
            ),
        color = ChartyColor.Solid(color = Teal),
        fillAlpha = 0.22f,
    )

private val radarTarget =
    RadarDataSet(
        label = "Target",
        axes =
            radarAxes(
                "Speed" to 90f,
                "Reliability" to 88f,
                "Design" to 85f,
                "Support" to 80f,
                "Value" to 84f,
                "Docs" to 80f,
            ),
        color = ChartyColor.Solid(color = Amber),
        fillAlpha = 0.22f,
    )

private val activityRings =
    listOf(
        CircularRingData(label = "Move", progress = 78f, color = ChartyColor.Solid(color = Indigo)),
        CircularRingData(label = "Exercise", progress = 62f, color = ChartyColor.Solid(color = Teal)),
        CircularRingData(label = "Stand", progress = 91f, color = ChartyColor.Solid(color = Amber)),
    )

private val storageBlocks =
    listOf(
        BlockData(value = 46f, color = ChartyColor.Solid(color = Indigo)),
        BlockData(value = 24f, color = ChartyColor.Solid(color = Teal)),
        BlockData(value = 18f, color = ChartyColor.Solid(color = Amber)),
        BlockData(value = 12f, color = ChartyColor.Solid(color = Pink)),
    )

private val contributionYear: List<CalendarData> =
    buildList {
        val daysPerMonth = listOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var seed = 20250101L
        daysPerMonth.forEachIndexed { monthIndex, days ->
            for (day in 1..days) {
                seed = seed * 6364136223846793005L + 1442695040888963407L
                val intensity = ((seed ushr 33) % 11L).toFloat()
                add(
                    CalendarData(
                        year = 2025,
                        month = monthIndex + 1,
                        day = day,
                        value = intensity,
                    ),
                )
            }
        }
    }

private val trafficMatrix: List<HeatmapCell> =
    buildList {
        val rows = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val columns = listOf("00h", "04h", "08h", "12h", "16h", "20h")
        val weights = listOf(0.15f, 0.08f, 0.62f, 0.94f, 0.81f, 0.47f)
        val dayFactors = listOf(0.92f, 1.0f, 0.97f, 1.04f, 0.88f, 0.42f, 0.36f)
        rows.forEachIndexed { rowIndex, row ->
            columns.forEachIndexed { columnIndex, column ->
                add(
                    HeatmapCell(
                        rowLabel = row,
                        columnLabel = column,
                        value = (weights[columnIndex] * dayFactors[rowIndex] * 100f).toInt().toFloat(),
                    ),
                )
            }
        }
    }
