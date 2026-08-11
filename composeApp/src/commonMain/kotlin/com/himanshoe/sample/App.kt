@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "WildcardImport",
    "MaxLineLength",
)

package com.himanshoe.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.block.BlockBarChart
import com.himanshoe.charty.block.config.BlockBarChartConfig
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.calendar.CalendarHeatmapChart
import com.himanshoe.charty.calendar.config.CalendarHeatmapConfig
import com.himanshoe.charty.calendar.config.CellShape
import com.himanshoe.charty.calendar.config.WeekStartDay
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
import com.himanshoe.charty.common.annotation.AnnotationStyle
import com.himanshoe.charty.common.annotation.ChartAnnotation
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.brush.rememberBrushSelectionState
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.ReferenceLineLabelPosition
import com.himanshoe.charty.common.config.ReferenceLineStrokeStyle
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPadding
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.viewport.rememberViewPortState
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.data.StackedAreaPoint
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.config.InteractionConfig
import com.himanshoe.charty.pie.config.LabelConfig
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
import com.himanshoe.charty.radar.config.LegendPosition
import com.himanshoe.charty.radar.config.MultipleRadarChartConfig
import com.himanshoe.charty.radar.config.RadarChartConfig
import com.himanshoe.charty.radar.config.RadarGridConfig
import com.himanshoe.charty.radar.config.RadarGridStyle
import com.himanshoe.charty.radar.config.RadarLabelConfig
import com.himanshoe.charty.radar.data.RadarAxisData
import com.himanshoe.charty.radar.data.RadarDataSet

@Composable
@Suppress("CyclomaticComplexMethod")
fun AllChartsShowcase(modifier: Modifier = Modifier) {
    MaterialTheme {
        Column(
            modifier = modifier.fillMaxSize(),
        ) {
            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // RadarChart demo at position 0
                item {
                    ChartCard(
                        title = "Radar Chart",
                        description = "Multi-dimensional data visualization showing attributes on radiating axes.",
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Example 1: Player Stats - Polygon Grid
                            Text(
                                text = "Player Performance",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                RadarChart(
                                    data = {
                                        listOf(
                                            RadarDataSet(
                                                label = "Player A",
                                                axes =
                                                    listOf(
                                                        RadarAxisData(label = "Speed", value = 85f),
                                                        RadarAxisData(label = "Strength", value = 70f),
                                                        RadarAxisData(label = "Defense", value = 90f),
                                                        RadarAxisData(label = "Agility", value = 75f),
                                                        RadarAxisData(label = "Stamina", value = 80f),
                                                    ),
                                                color = ChartyColor.Solid(Color(0xFF2196F3)),
                                                fillAlpha = 0.4f,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.size(280.dp),
                                    config =
                                        RadarChartConfig(
                                            dataLineWidth = 3f,
                                            showDataPoints = true,
                                            dataPointRadius = 6f,
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = true,
                                                    labelDistanceMultiplier = 1.25f,
                                                ),
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.POLYGON,
                                                    numberOfGridLevels = 5,
                                                    showGridLines = true,
                                                    showAxisLines = true,
                                                ),
                                        ),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Example 2: Skills Assessment - Circular Grid
                            Text(
                                text = "Skills Assessment",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                RadarChart(
                                    data = {
                                        listOf(
                                            RadarDataSet(
                                                label = "Technical Skills",
                                                axes =
                                                    listOf(
                                                        RadarAxisData(label = "Programming", value = 95f),
                                                        RadarAxisData(label = "Design", value = 75f),
                                                        RadarAxisData(label = "Communication", value = 80f),
                                                        RadarAxisData(label = "Leadership", value = 70f),
                                                        RadarAxisData(label = "Testing", value = 85f),
                                                        RadarAxisData(label = "Documentation", value = 78f),
                                                    ),
                                                color = ChartyColor.Solid(Color(0xFF4CAF50)),
                                                fillAlpha = 0.35f,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.size(280.dp),
                                    config =
                                        RadarChartConfig(
                                            dataLineWidth = 2.5f,
                                            showDataPoints = true,
                                            dataPointRadius = 5f,
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = true,
                                                    labelDistanceMultiplier = 1.2f,
                                                ),
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.CIRCULAR,
                                                    numberOfGridLevels = 4,
                                                    showGridLines = true,
                                                    showAxisLines = true,
                                                ),
                                        ),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Example 3: Product Features - Polygon Grid with More Axes
                            Text(
                                text = "Product Features",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                RadarChart(
                                    data = {
                                        listOf(
                                            RadarDataSet(
                                                label = "Product X",
                                                axes =
                                                    listOf(
                                                        RadarAxisData(label = "Price", value = 60f),
                                                        RadarAxisData(label = "Quality", value = 95f),
                                                        RadarAxisData(label = "Features", value = 80f),
                                                        RadarAxisData(label = "Support", value = 85f),
                                                        RadarAxisData(label = "Reliability", value = 90f),
                                                        RadarAxisData(label = "Design", value = 75f),
                                                        RadarAxisData(label = "Performance", value = 88f),
                                                        RadarAxisData(label = "Usability", value = 82f),
                                                    ),
                                                color = ChartyColor.Solid(Color(0xFFE91E63)),
                                                fillAlpha = 0.3f,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.size(270.dp),
                                    config =
                                        RadarChartConfig(
                                            dataLineWidth = 2f,
                                            showDataPoints = true,
                                            dataPointRadius = 4f,
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = true,
                                                    labelDistanceMultiplier = 1.18f,
                                                ),
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.POLYGON,
                                                    numberOfGridLevels = 5,
                                                    showGridLines = true,
                                                    showAxisLines = true,
                                                ),
                                        ),
                                )
                            }
                        }
                    }
                }

                // BlockBarChart demo at position 1
                item {
                    ChartCard(
                        title = "Block Bar Chart",
                        description = "Horizontal segmented pill-shaped bar showing proportional composition.",
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Example 1: Basic 3-block bar
                            Text(
                                text = "Product Categories",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            BlockBarChart(
                                data = {
                                    listOf(
                                        BlockData(value = 1f, color = ChartyColor.Solid(Color(0xFFFF6B81))), // Pink
                                        BlockData(value = 2f, color = ChartyColor.Solid(Color(0xFFFFE066))), // Yellow
                                        BlockData(value = 5f, color = ChartyColor.Solid(Color(0xFF5BE37D))), // Green
                                    )
                                },
                                blockBarConfig =
                                    BlockBarChartConfig(
                                        gapBetweenBlocks = 4.dp,
                                        barHeight = 16.dp,
                                    ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Example 2: Larger bar with 4 segments
                            Text(
                                text = "Team Productivity",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            BlockBarChart(
                                data = {
                                    listOf(
                                        BlockData(value = 25f, color = ChartyColor.Solid(Color(0xFF2196F3))),
                                        BlockData(value = 40f, color = ChartyColor.Solid(Color(0xFF4CAF50))),
                                        BlockData(value = 15f, color = ChartyColor.Solid(Color(0xFFFF9800))),
                                        BlockData(value = 20f, color = ChartyColor.Solid(Color(0xFFE91E63))),
                                    )
                                },
                                blockBarConfig =
                                    BlockBarChartConfig(
                                        gapBetweenBlocks = 6.dp,
                                        barHeight = 24.dp,
                                    ),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Example 3: Progress bar style
                            Text(
                                text = "Project Completion",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            BlockBarChart(
                                data = {
                                    listOf(
                                        BlockData(value = 70f, color = ChartyColor.Solid(Color(0xFF00C853))),
                                        BlockData(value = 30f, color = ChartyColor.Solid(Color(0xFFBDBDBD))),
                                    )
                                },
                                blockBarConfig =
                                    BlockBarChartConfig(
                                        gapBetweenBlocks = 2.dp,
                                        barHeight = 12.dp,
                                    ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // Wavy Chart demo at position 1
                item {
                    ChartCard(
                        title = "Wavy Chart",
                        description = "Animated sine-wave bars supporting positive and negative values.",
                    ) {
                        WavyChart(
                            modifier = Modifier.fillMaxWidth().height(240.dp),
                            data = {
                                listOf(
                                    BarData(label = "Mon", value = 10f),
                                    BarData(label = "Tue", value = -6f),
                                    BarData(label = "Wed", value = 14f),
                                    BarData(label = "Thu", value = -12f),
                                    BarData(label = "Fri", value = 8f),
                                )
                            },
                            scaffoldConfig = ChartScaffoldConfig(),
                        )
                    }
                }

                // Bubble Bar Chart - NEW EXAMPLE AT POSITION 0
                item {
                    var selectedBar by remember { mutableStateOf<BarData?>(null) }

                    Column {
                        // Show selected bar info in a card above the chart
                        selectedBar?.let { bar ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${bar.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "Sales: $${bar.value.toInt()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Bubble Bar Chart",
                            description = "Data visualized as stacked bubbles. Click on any column to see details.",
                        ) {
                            BubbleBarChart(
                                modifier = Modifier.fillMaxWidth().height(320.dp),
                                data = {
                                    listOf(
                                        BarData(label = "100", value = 50f),
                                        BarData(label = "200", value = 200f),
                                        BarData(label = "300", value = 300f),
                                        BarData(label = "400", value = 400f),
                                        BarData(label = "500", value = 500f),
                                        BarData(label = "600", value = 600f),
                                        BarData(label = "700", value = 700f),
                                        BarData(label = "800", value = 800f),
                                        BarData(label = "900", value = 900f),
                                    )
                                },
                                color =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFFFFA64D),
                                            Color(0xFFEF7B45),
                                            Color(0xFFD64C66),
                                        ),
                                    ),
                                bubbleConfig =
                                    BubbleBarChartConfig(
                                        barWidthFraction = 0.6f,
                                        bubbleRadius = 8f,
                                        bubbleSpacing = 4f,
                                        animation = Animation.Enabled(duration = 800),
                                        // Tooltip styling
                                        tooltipConfig =
                                            TooltipConfig(
                                                backgroundColor = Color.Black,
                                                textStyle =
                                                    TextStyle(
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                    ),
                                                shape = RoundedCornerShape(8.dp),
                                                padding =
                                                    TooltipPadding(
                                                        horizontal = 16.dp,
                                                        vertical = 10.dp,
                                                    ),
                                                elevation = 8.dp,
                                                showArrow = true,
                                                arrowSize = 10.dp,
                                            ),
                                        tooltipPosition = TooltipPosition.AUTO,
                                    ),
                                scaffoldConfig =
                                    ChartScaffoldConfig(
                                        leftLabelRotation = LabelRotation.Angle45Negative,
                                    ),
                                onBarClick = { barData ->
                                    selectedBar = barData
                                    println("Bubble bar clicked: ${barData.label} = ${barData.value}")
                                },
                            )
                        }
                    }
                }

                // Interactive Bar Chart with Tooltips - NEW EXAMPLE AT POSITION 0
                item {
                    var selectedBar by remember { mutableStateOf<BarData?>(null) }

                    Column {
                        // Show selected bar info in a card above the chart
                        selectedBar?.let { bar ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${bar.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Sales: $${bar.value.toInt()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Interactive Bar Chart with Tooltips",
                            description = "Click on any bar to see tooltip with details. Tooltip auto-positions above/below based on available space.",
                        ) {
                            BarChart(
                                modifier = Modifier.fillMaxWidth().height(320.dp),
                                data = {
                                    listOf(
                                        BarData(label = "Jan", value = 12500f),
                                        BarData(label = "Feb", value = 15800f),
                                        BarData(label = "Mar", value = 14200f),
                                        BarData(label = "Apr", value = 18900f),
                                        BarData(label = "May", value = 17500f),
                                        BarData(label = "Jun", value = 21300f),
                                    )
                                },
                                color =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF1976D2),
                                        ),
                                    ),
                                barConfig =
                                    BarChartConfig(
                                        barWidthFraction = 0.7f,
                                        cornerRadius = CornerRadius.Large,
                                        animation = Animation.Enabled(duration = 800),
                                        // Tooltip styling
                                        tooltipConfig =
                                            TooltipConfig(
                                                backgroundColor = Color.Black,
                                                textStyle =
                                                    TextStyle(
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                    ),
                                                shape = RoundedCornerShape(8.dp),
                                                padding =
                                                    TooltipPadding(
                                                        horizontal = 16.dp,
                                                        vertical = 10.dp,
                                                    ),
                                                elevation = 8.dp,
                                                showArrow = true,
                                                arrowSize = 10.dp,
                                            ),
                                        tooltipPosition = TooltipPosition.AUTO,
                                    ),
                                scaffoldConfig =
                                    ChartScaffoldConfig(
                                        leftLabelRotation = LabelRotation.Angle45Negative,
                                    ),
                                onBarClick = { barData ->
                                    selectedBar = barData
                                    println("Bar clicked: ${barData.label} = ${barData.value}")
                                },
                            )
                        }
                    }
                }

                item {
                    var selectedBar by remember { mutableStateOf<BarData?>(null) }

                    Column {
                        // Show selected bar info
                        selectedBar?.let { bar ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${bar.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                    Text(
                                        text = "Cumulative Value: ${bar.value}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Waterfall Chart (Interactive)",
                            description = "Click on bars to see cumulative gains/losses with tooltip",
                        ) {
                            WaterfallChart(
                                modifier = Modifier.fillMaxWidth().height(280.dp),
                                data = {
                                    listOf(
                                        BarData(label = "A", value = 10f, color = ChartyColor.Solid(Color(0xFFD64C66))),
                                        BarData(label = "B", value = 7f, color = ChartyColor.Solid(Color(0xFF6A1B9A))),
                                        BarData(label = "C", value = 15f, color = ChartyColor.Solid(Color(0xFF0B1D3B))),
                                        BarData(label = "D", value = 32f, color = ChartyColor.Solid(Color(0xFFD64C66))),
                                    )
                                },
                                config =
                                    WaterfallChartConfig(
                                        barWidthFraction = 0.6f,
                                        cornerRadius = CornerRadius.Medium,
                                    ),
                                onBarClick = { barData ->
                                    selectedBar = barData
                                    println("Waterfall bar clicked: ${barData.label} = ${barData.value}")
                                },
                            )
                        }
                    }
                }
                item {
                    ChartCard(
                        title = "Mosaic Bar Chart",
                        description = "100% stacked bar chart where each bar shows proportional composition",
                    ) {
                        MosaicBarChart(
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            data = {
                                listOf(
                                    BarGroup(
                                        label = "A",
                                        values = listOf(55f, 30f, 15f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF0B1D3B)),
                                                ChartyColor.Solid(Color(0xFFD64C66)),
                                                ChartyColor.Solid(Color(0xFFFFA64D)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "B",
                                        values = listOf(45f, 22f, 33f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF0B1D3B)),
                                                ChartyColor.Solid(Color(0xFFD64C66)),
                                                ChartyColor.Solid(Color(0xFFFFA64D)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "C",
                                        values = listOf(25f, 30f, 45f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF0B1D3B)),
                                                ChartyColor.Solid(Color(0xFFD64C66)),
                                                ChartyColor.Solid(Color(0xFFFFA64D)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "D",
                                        values = listOf(10f, 38f, 52f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF0B1D3B)),
                                                ChartyColor.Solid(Color(0xFFD64C66)),
                                                ChartyColor.Solid(Color(0xFFFFA64D)),
                                            ),
                                    ),
                                )
                            },
                            config =
                                MosaicBarChartConfig(
                                    barWidthFraction = 0.9f,
                                ),
                        )
                    }
                }
                item {
                    var selectedLollipop by remember { mutableStateOf<BarData?>(null) }

                    Column {
                        // Show selected lollipop info
                        selectedLollipop?.let { bar ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${bar.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "Value: ${bar.value.toInt()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Lollipop Bar Chart (Interactive)",
                            description = "Click on lollipops to see values with tooltip",
                        ) {
                            LollipopBarChart(
                                modifier = Modifier.fillMaxWidth().height(280.dp),
                                data = {
                                    listOf(
                                        BarData(label = "A", value = 210f),
                                        BarData(label = "B", value = 380f),
                                        BarData(label = "C", value = 310f),
                                        BarData(label = "D", value = 170f),
                                        BarData(label = "E", value = 450f),
                                    )
                                },
                                colors = ChartyColor.Solid(Color(0xFFE91E63)),
                                config =
                                    LollipopBarChartConfig(
                                        barWidthFraction = 0.25f,
                                        stemThickness = 8f,
                                        circleRadius = 16f,
                                        circleColor = ChartyColor.Solid(Color.Yellow),
                                    ),
                                onBarClick = { barData ->
                                    selectedLollipop = barData
                                    println("Lollipop clicked: ${barData.label} = ${barData.value}")
                                },
                            )
                        }
                    }
                }
                // Candlestick Chart
                item {
                    ChartCard(
                        title = "Candlestick Chart",
                        description = "Financial OHLC chart - shows open, high, low, close prices (auto-filters to 5 labels when >10 data points)",
                    ) {
                        CandlestickChart(
                            modifier = Modifier.fillMaxWidth().height(350.dp),
                            data = {
                                listOf(
                                    CandleData(
                                        "17:00",
                                        open = 95f,
                                        high = 96.5f,
                                        low = 94.5f,
                                        close = 96f,
                                    ),
                                    CandleData(
                                        "17:15",
                                        open = 96f,
                                        high = 96.3f,
                                        low = 95.2f,
                                        close = 95.5f,
                                    ),
                                    CandleData(
                                        "17:30",
                                        open = 95.5f,
                                        high = 96.8f,
                                        low = 95.5f,
                                        close = 96.4f,
                                    ),
                                    CandleData(
                                        "17:45",
                                        open = 96.4f,
                                        high = 96.7f,
                                        low = 95.8f,
                                        close = 96f,
                                    ),
                                    CandleData(
                                        "18:00",
                                        open = 96f,
                                        high = 96.2f,
                                        low = 94.8f,
                                        close = 95f,
                                    ),
                                    CandleData(
                                        "18:15",
                                        open = 95f,
                                        high = 95.6f,
                                        low = 94.6f,
                                        close = 95.3f,
                                    ),
                                    CandleData(
                                        "18:30",
                                        open = 95.3f,
                                        high = 95.5f,
                                        low = 94.5f,
                                        close = 94.8f,
                                    ),
                                    CandleData(
                                        "18:45",
                                        open = 94.8f,
                                        high = 95.2f,
                                        low = 94.3f,
                                        close = 94.6f,
                                    ),
                                    CandleData(
                                        "19:00",
                                        open = 94.6f,
                                        high = 95f,
                                        low = 94.4f,
                                        close = 94.5f,
                                    ),
                                    CandleData(
                                        "19:15",
                                        open = 94.5f,
                                        high = 94.9f,
                                        low = 94.2f,
                                        close = 94.4f,
                                    ),
                                    CandleData(
                                        "19:30",
                                        open = 94.4f,
                                        high = 94.8f,
                                        low = 94f,
                                        close = 94.3f,
                                    ),
                                    CandleData(
                                        "19:45",
                                        open = 94.3f,
                                        high = 94.7f,
                                        low = 94.2f,
                                        close = 94.5f,
                                    ),
                                    CandleData(
                                        "20:00",
                                        open = 94.5f,
                                        high = 95.5f,
                                        low = 94.3f,
                                        close = 95.2f,
                                    ),
                                    CandleData(
                                        "20:15",
                                        open = 95.2f,
                                        high = 95.7f,
                                        low = 95f,
                                        close = 95.5f,
                                    ),
                                    CandleData(
                                        "20:30",
                                        open = 95.5f,
                                        high = 96f,
                                        low = 95.3f,
                                        close = 95.8f,
                                    ),
                                    CandleData(
                                        "20:45",
                                        open = 95.8f,
                                        high = 96.2f,
                                        low = 95.6f,
                                        close = 96f,
                                    ),
                                    CandleData(
                                        "21:00",
                                        open = 96f,
                                        high = 96.5f,
                                        low = 95.5f,
                                        close = 96.2f,
                                    ),
                                    CandleData(
                                        "21:15",
                                        open = 96.2f,
                                        high = 97f,
                                        low = 96f,
                                        close = 96.8f,
                                    ),
                                    CandleData(
                                        "21:30",
                                        open = 96.8f,
                                        high = 97.8f,
                                        low = 96.5f,
                                        close = 97.4f,
                                    ),
                                    CandleData(
                                        "21:45",
                                        open = 97.4f,
                                        high = 97.9f,
                                        low = 97.2f,
                                        close = 97.6f,
                                    ),
                                    CandleData(
                                        "22:00",
                                        open = 97.6f,
                                        high = 97.8f,
                                        low = 97f,
                                        close = 97.2f,
                                    ),
                                )
                            },
                            bullishColor = ChartyColor.Solid(Color(0xFFFFC107)), // Yellow/Gold for bullish
                            bearishColor = ChartyColor.Solid(Color(0xFFE91E63)), // Pink for bearish
                            candlestickConfig =
                                CandlestickChartConfig(
                                    candleWidthFraction = 0.7f,
                                    wickWidthFraction = 0.15f,
                                    showWicks = true,
                                    minCandleBodyHeight = 2f,
                                    cornerRadius = CornerRadius.ExtraLarge,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }
                item {
                    var selectedComboData by remember { mutableStateOf<ComboChartData?>(null) }

                    Column {
                        // Show selected data info
                        selectedComboData?.let { data ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${data.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Bar Value: ${data.barValue.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Line Value: ${data.lineValue.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Combo Chart (Bar + Line) - Interactive",
                            description = "Click on bars or line points to see detailed values with tooltip",
                        ) {
                            ComboChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        ComboChartData("Jan", barValue = 100f, lineValue = 80f),
                                        ComboChartData("Feb", barValue = 150f, lineValue = 120f),
                                        ComboChartData("Mar", barValue = 120f, lineValue = 140f),
                                        ComboChartData("Apr", barValue = 180f, lineValue = 160f),
                                        ComboChartData("May", barValue = 160f, lineValue = 145f),
                                        ComboChartData("Jun", barValue = 200f, lineValue = 180f),
                                    )
                                },
                                barColor = ChartyColor.Solid(Color(0xFF2196F3)),
                                lineColor = ChartyColor.Solid(Color(0xFFFF5722)),
                                comboConfig =
                                    ComboChartConfig(
                                        barWidthFraction = 0.6f,
                                        lineWidth = 3f,
                                        showPoints = true,
                                        pointRadius = 6f,
                                        smoothCurve = false,
                                        animation = Animation.Enabled(duration = 1200),
                                        referenceLine =
                                            ReferenceLineConfig(
                                                value = 150f,
                                                color = Color(0xFF4CAF50),
                                                strokeWidth = 2f,
                                                strokeStyle = ReferenceLineStrokeStyle.DASHED,
                                                label = "Target 150",
                                                labelPosition = ReferenceLineLabelPosition.END,
                                            ),
                                    ),
                                onDataClick = { comboData ->
                                    selectedComboData = comboData
                                    println(
                                        "Combo chart clicked: ${comboData.label} - Bar: ${comboData.barValue}, Line: ${comboData.lineValue}",
                                    )
                                },
                            )
                        }
                    }
                }

                // Bar Chart with Per-Bar Colors
                item {
                    ChartCard(
                        title = "Bar Chart with Per-Bar Colors",
                        description = "Each bar can have its own color - no need to match data size with color array!",
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarData(label = "Jan", value = 100f, color = ChartyColor.Solid(Color(0xFF2196F3))),
                                    BarData(label = "Feb", value = 150f, color = ChartyColor.Solid(Color(0xFF4CAF50))),
                                    BarData(label = "Mar", value = 120f, color = ChartyColor.Solid(Color(0xFFFF9800))),
                                    BarData(label = "Apr", value = 180f, color = ChartyColor.Solid(Color(0xFFE91E63))),
                                    BarData(label = "May", value = 160f, color = ChartyColor.Solid(Color(0xFF9C27B0))),
                                    BarData(label = "Jun", value = 200f, color = ChartyColor.Solid(Color(0xFF00BCD4))),
                                )
                            },
                            color = ChartyColor.Solid(Color.Gray), // Fallback color (not used since each bar has its own)
                            barConfig =
                                BarChartConfig(
                                    barWidthFraction = 0.7f,
                                    cornerRadius = CornerRadius.Medium,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }

                // Circular Progress Indicator (Interactive)
                item {
                    var selectedRing by remember { mutableStateOf<Pair<CircularRingData, Int>?>(null) }

                    Column {
                        // Show selected ring info
                        selectedRing?.let { (ring, index) ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${ring.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "Progress: ${ring.progress.toInt()} / ${ring.maxValue.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "Percentage: ${((ring.progress / ring.maxValue) * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.size(300.dp).background(Color.Black),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                rings = {
                                    listOf(
                                        CircularRingData(
                                            label = "Move",
                                            progress = 450f,
                                            maxValue = 600f,
                                            color = ChartyColor.Solid(Color(0xFFFF3B58)),
                                            backgroundColor = ChartyColor.Solid(Color(0x33FF3B58)),
                                        ),
                                        CircularRingData(
                                            label = "Exercise",
                                            progress = 25f,
                                            maxValue = 30f,
                                            color = ChartyColor.Solid(Color(0xFFACFF3D)),
                                            backgroundColor = ChartyColor.Solid(Color(0x33ACFF3D)),
                                        ),
                                        CircularRingData(
                                            label = "Stand",
                                            progress = 10f,
                                            maxValue = 12f,
                                            color = ChartyColor.Solid(Color(0xFF34D5FF)),
                                            backgroundColor = ChartyColor.Solid(Color(0x3334D5FF)),
                                        ),
                                    )
                                },
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                config =
                                    CircularProgressConfig(
                                        centerHoleRatio = 0.4f,
                                        gapBetweenRings = 12f,
                                        startAngleDegrees = -90f,
                                        strokeCap = StrokeCap.Round,
                                        showCenterText = false,
                                        animation = Animation.Enabled(duration = 1500),
                                        interactionEnabled = true,
                                    ),
                                onRingClick = { ring, index ->
                                    selectedRing = ring to index
                                    println(
                                        "Ring clicked: ${ring.label} (Ring $index) - ${((ring.progress / ring.maxValue) * 100).toInt()}%",
                                    )
                                },
                                centerContent = {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val holeRadius = (size.minDimension / 2f) * 0.34f
                                        drawCircle(
                                            color = Color.Black,
                                            radius = holeRadius,
                                            center = Offset(size.width / 2f, size.height / 2f),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
                item {
                    var selectedBar by remember { mutableStateOf<BarData?>(null) }

                    Column {
                        // Show selected bar info
                        selectedBar?.let { bar ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Department: ${bar.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Score: ${bar.value.toInt()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Horizontal Bar Chart (Interactive)",
                            description = "Click on bars to see department scores with tooltip",
                        ) {
                            HorizontalBarChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        BarData(label = "Marketing", value = 50f),
                                        BarData(label = "Development", value = 60f),
                                        BarData(label = "Sales", value = 60f),
                                        BarData(label = "Operations", value = 60f),
                                        BarData(label = "Support", value = 61f),
                                    )
                                },
                                color = ChartyColor.Solid(Color(0xFF9C27B0)),
                                barConfig =
                                    BarChartConfig(
                                        barWidthFraction = 0.7f,
                                        cornerRadius = CornerRadius.Large,
                                        animation = Animation.Enabled(duration = 1000),
                                    ),
                                onBarClick = { barData ->
                                    selectedBar = barData
                                    println("Horizontal bar clicked: ${barData.label} = ${barData.value}")
                                },
                            )
                        }
                    }
                }

                // Span Chart
                item {
                    ChartCard(
                        title = "Span Chart (Range Chart)",
                        description = "Shows ranges or time spans horizontally - ideal for schedules and timelines",
                    ) {
                        SpanChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    SpanData("Category 1", startValue = 1f, endValue = 15f),
                                    SpanData("Category 2", startValue = 12f, endValue = 28f),
                                    SpanData("Category 3", startValue = 3f, endValue = 18f),
                                    SpanData("Category 4", startValue = 18f, endValue = 32f),
                                    SpanData("Category 5", startValue = 8f, endValue = 22f),
                                )
                            },
                            colors =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                        Color(0xFFE91E63),
                                        Color(0xFF9C27B0),
                                    ),
                                ),
                            barConfig =
                                BarChartConfig(
                                    barWidthFraction = 0.7f,
                                    cornerRadius = CornerRadius.Medium,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }

                // Simple Bar Chart
                item {
                    ChartCard(
                        title = "Simple Bar Chart with Rounded Corners",
                        description = "Animated bar chart with rounded top corners",
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData(label = "Jan", value = 45f),
                                    BarData(label = "Feb", value = 78f),
                                    BarData(label = "Mar", value = 62f),
                                    BarData(label = "Apr", value = 89f),
                                    BarData(label = "May", value = 55f),
                                )
                            },
                            barConfig =
                                BarChartConfig(
                                    barWidthFraction = 0.7f,
                                    cornerRadius = CornerRadius.Large,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                            color = ChartyColor.Solid(Color(0xFF2196F3)),
                        )
                    }
                }

                // Bar Chart with Negative Values - Below Axis Mode
                item {
                    ChartCard(
                        title = "Bar Chart with Negative Values - Below Axis",
                        description = "Chart showing both profit and loss - axis centered at zero",
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData(label = "Jan", value = -45f),
                                    BarData(label = "Feb", value = 78f),
                                    BarData(label = "Mar", value = -62f),
                                    BarData(label = "Apr", value = 89f),
                                    BarData(label = "May", value = -55f),
                                    BarData(label = "Jun", value = 35f),
                                )
                            },
                            barConfig =
                                BarChartConfig(
                                    barWidthFraction = 0.7f,
                                    cornerRadius = CornerRadius.Large,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                            color = ChartyColor.Solid(Color(0xFF2196F3)),
                        )
                    }
                }

                // Bar Chart with Negative Values - From Min Value Mode
                item {
                    ChartCard(
                        title = "Bar Chart with Negative Values - From Min Value",
                        description = "All bars drawn from minimum value upward for relative comparison",
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData(label = "Jan", value = -45f),
                                    BarData(label = "Feb", value = 78f),
                                    BarData(label = "Mar", value = -62f),
                                    BarData(label = "Apr", value = 89f),
                                    BarData(label = "May", value = -55f),
                                    BarData(label = "Jun", value = 35f),
                                )
                            },
                            barConfig =
                                BarChartConfig(
                                    barWidthFraction = 0.7f,
                                    cornerRadius = CornerRadius.Large,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                            color = ChartyColor.Solid(Color(0xFF4CAF50)),
                        )
                    }
                }

                // Comparison Bar Chart (formerly Grouped)
                item {
                    var selectedSegment by remember {
                        mutableStateOf<com.himanshoe.charty.bar.config.ComparisonBarSegment?>(
                            null,
                        )
                    }

                    Column {
                        // Show selected segment info
                        selectedSegment?.let { segment ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Category: ${segment.barGroup.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                    Text(
                                        text = "Bar ${segment.barIndex + 1}: ${segment.barValue.toInt()}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Comparison Bar Chart (Interactive)",
                            description = "Click on any bar to see individual segment values with tooltip",
                        ) {
                            ComparisonBarChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        BarGroup(
                                            label = "Q1",
                                            values = listOf(20f, 30f, 15f),
                                            colors =
                                                listOf(
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF1976D2),
                                                            Color(0xFF64B5F6),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF388E3C),
                                                            Color(0xFF81C784),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFFE64A19),
                                                            Color(0xFFFF8A65),
                                                        ),
                                                    ),
                                                ),
                                        ),
                                        BarGroup(
                                            label = "Q2",
                                            values = listOf(25f, 35f, 20f),
                                            colors =
                                                listOf(
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF1976D2),
                                                            Color(0xFF64B5F6),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF388E3C),
                                                            Color(0xFF81C784),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFFE64A19),
                                                            Color(0xFFFF8A65),
                                                        ),
                                                    ),
                                                ),
                                        ),
                                        BarGroup(
                                            label = "Q3",
                                            values = listOf(30f, 25f, 25f),
                                            colors =
                                                listOf(
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF1976D2),
                                                            Color(0xFF64B5F6),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF388E3C),
                                                            Color(0xFF81C784),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFFE64A19),
                                                            Color(0xFFFF8A65),
                                                        ),
                                                    ),
                                                ),
                                        ),
                                        BarGroup(
                                            label = "Q4",
                                            values = listOf(28f, 40f, 18f),
                                            colors =
                                                listOf(
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF1976D2),
                                                            Color(0xFF64B5F6),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFF388E3C),
                                                            Color(0xFF81C784),
                                                        ),
                                                    ),
                                                    ChartyColor.Gradient(
                                                        listOf(
                                                            Color(0xFFE64A19),
                                                            Color(0xFFFF8A65),
                                                        ),
                                                    ),
                                                ),
                                        ),
                                    )
                                },
                                onBarClick = { segment ->
                                    selectedSegment = segment
                                    println(
                                        "Comparison bar clicked: ${segment.barGroup.label} [${segment.barIndex}] = ${segment.barValue}",
                                    )
                                },
                            )
                        }
                    }
                }

                // Comparison Bar Chart with Negative Values - Below Axis
                item {
                    ChartCard(
                        title = "Comparison Bar Chart - Below Axis Mode",
                        description = "Multiple bars with profit/loss comparison - axis centered at zero",
                    ) {
                        ComparisonBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup(
                                        label = "Q1",
                                        values = listOf(20f, 30f, 15f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q2",
                                        values = listOf(25f, 35f, 20f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q3",
                                        values = listOf(30f, 25f, 25f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q4",
                                        values = listOf(28f, 40f, 18f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                )
                            },
                            comparisonConfig =
                                ComparisonBarChartConfig(
                                    negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                                ),
                        )
                    }
                }

                // Comparison Bar Chart with Negative Values - From Min Value
                item {
                    ChartCard(
                        title = "Comparison Bar Chart - From Min Value Mode",
                        description = "All bars drawn from minimum value for relative comparison",
                    ) {
                        ComparisonBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup(
                                        label = "Q1",
                                        values = listOf(20f, 30f, 15f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q2",
                                        values = listOf(25f, 35f, 20f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q3",
                                        values = listOf(30f, 25f, 25f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q4",
                                        values = listOf(28f, 40f, 18f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                )
                            },
                            comparisonConfig =
                                ComparisonBarChartConfig(
                                    negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                                ),
                        )
                    }
                }

                // Point Chart
                item {
                    ChartCard(
                        title = "Point Chart",
                        description = "Scatter plot with individual points",
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData(label = "Mon", value = 23f),
                                    PointData(label = "Tue", value = 45f),
                                    PointData(label = "Wed", value = 31f),
                                    PointData(label = "Thu", value = 67f),
                                    PointData(label = "Fri", value = 52f),
                                    PointData(label = "Sat", value = 39f),
                                    PointData(label = "Sun", value = 28f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF4CAF50)),
                            pointConfig =
                                PointChartConfig(
                                    pointRadius = 8f,
                                ),
                        )
                    }
                }

                // Line Chart
                item {
                    ChartCard(
                        title = "Line Chart",
                        description = "Connected line chart with points",
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData(label = "Mon", value = 20f),
                                    LineData(label = "Tue", value = 45f),
                                    LineData(label = "Wed", value = 30f),
                                    LineData(label = "Thu", value = 70f),
                                    LineData(label = "Fri", value = 55f),
                                    LineData(label = "Sat", value = 40f),
                                    LineData(label = "Sun", value = 65f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFFFF9800)),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 3f,
                                    strokeCap = StrokeCap.Round,
                                    showPoints = true,
                                    pointRadius = 6f,
                                ),
                        )
                    }
                }

                // Point Chart with Negative Values - Below Axis
                item {
                    ChartCard(
                        title = "Point Chart with Negative Values - Below Axis",
                        description = "Points showing both positive and negative values - axis centered at zero",
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData(label = "Mon", value = -23f),
                                    PointData(label = "Tue", value = 45f),
                                    PointData(label = "Wed", value = -31f),
                                    PointData(label = "Thu", value = 67f),
                                    PointData(label = "Fri", value = -52f),
                                    PointData(label = "Sat", value = 39f),
                                    PointData(label = "Sun", value = -28f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF4CAF50)),
                            pointConfig =
                                PointChartConfig(
                                    pointRadius = 8f,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                                ),
                        )
                    }
                }

                // Point Chart with Negative Values - From Min Value
                item {
                    ChartCard(
                        title = "Point Chart with Negative Values - From Min Value",
                        description = "All points drawn relative to minimum value - axis at bottom",
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData(label = "Mon", value = -23f),
                                    PointData(label = "Tue", value = 45f),
                                    PointData(label = "Wed", value = -31f),
                                    PointData(label = "Thu", value = 67f),
                                    PointData(label = "Fri", value = -52f),
                                    PointData(label = "Sat", value = 39f),
                                    PointData(label = "Sun", value = -28f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFFE91E63)),
                            pointConfig =
                                PointChartConfig(
                                    pointRadius = 8f,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                                ),
                        )
                    }
                }

                // Line Chart with Negative Values - Below Axis
                item {
                    ChartCard(
                        title = "Line Chart with Negative Values - Below Axis",
                        description = "Line chart showing trends with positive and negative values - axis centered",
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData(label = "Mon", value = -20f),
                                    LineData(label = "Tue", value = 45f),
                                    LineData(label = "Wed", value = -30f),
                                    LineData(label = "Thu", value = 70f),
                                    LineData(label = "Fri", value = -55f),
                                    LineData(label = "Sat", value = 40f),
                                    LineData(label = "Sun", value = 65f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFFFF9800)),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 3f,
                                    strokeCap = StrokeCap.Round,
                                    showPoints = true,
                                    pointRadius = 6f,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                                ),
                        )
                    }
                }

                // Line Chart with Negative Values - From Min Value
                item {
                    ChartCard(
                        title = "Line Chart with Negative Values - From Min Value",
                        description = "Line chart with all values drawn from minimum - axis at bottom",
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData(label = "Mon", value = -20f),
                                    LineData(label = "Tue", value = 45f),
                                    LineData(label = "Wed", value = -30f),
                                    LineData(label = "Thu", value = 70f),
                                    LineData(label = "Fri", value = -55f),
                                    LineData(label = "Sat", value = 40f),
                                    LineData(label = "Sun", value = 65f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF00BCD4)),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 3f,
                                    strokeCap = StrokeCap.Round,
                                    showPoints = true,
                                    pointRadius = 6f,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                                ),
                        )
                    }
                }

                // Bar Chart with Gradient
                item {
                    ChartCard(
                        title = "Bar Chart with Gradient",
                        description = "Bars with vertical gradient effect",
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData(label = "Q1", value = 35f),
                                    BarData(label = "Q2", value = 58f),
                                    BarData(label = "Q3", value = 72f),
                                    BarData(label = "Q4", value = 48f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(Color(0xFF9C27B0), Color(0xFFE91E63)),
                                ),
                        )
                    }
                }

                // Mixed Point Chart with Gradient
                item {
                    ChartCard(
                        title = "Point Chart with Multi-Colors",
                        description = "Each point gets a different color from gradient",
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData(label = "A", value = 18f),
                                    PointData(label = "B", value = 42f),
                                    PointData(label = "C", value = 28f),
                                    PointData(label = "D", value = 56f),
                                    PointData(label = "E", value = 35f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                        Color(0xFF9C27B0),
                                    ),
                                ),
                            pointConfig =
                                PointChartConfig(
                                    pointRadius = 10f,
                                ),
                        )
                    }
                }

                // Line Chart without points
                item {
                    ChartCard(
                        title = "Line Chart (Lines Only)",
                        description = "Clean line chart without point markers",
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData(label = "00:00", value = 12f),
                                    LineData(label = "04:00", value = 8f),
                                    LineData(label = "08:00", value = 25f),
                                    LineData(label = "12:00", value = 45f),
                                    LineData(label = "16:00", value = 38f),
                                    LineData(label = "20:00", value = 22f),
                                    LineData(label = "23:59", value = 15f),
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF00BCD4)),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 4f,
                                    smoothCurve = true,
                                    showPoints = false,
                                ),
                        )
                    }
                }

                // Stacked Bar Chart
                item {
                    ChartCard(
                        title = "Stacked Bar Chart",
                        description = "Shows composition and total - segments stacked vertically",
                    ) {
                        StackedBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup(label = "Q1", values = listOf(20f, 30f, 15f)),
                                    BarGroup(label = "Q2", values = listOf(25f, 35f, 20f)),
                                    BarGroup(label = "Q3", values = listOf(30f, 25f, 25f)),
                                    BarGroup(label = "Q4", values = listOf(28f, 40f, 18f)),
                                )
                            },
                            colors =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                    ),
                                ),
                            stackedConfig =
                                StackedBarChartConfig(
                                    barWidthFraction = 0.7f,
                                    topCornerRadius = CornerRadius.Medium,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }

                // Stacked Bar Chart with Per-Segment Gradients
                item {
                    ChartCard(
                        title = "Stacked Bar Chart with Gradient Segments",
                        description = "Each stack segment can have its own gradient - beautiful multi-color effects!",
                    ) {
                        StackedBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup(
                                        label = "Q1",
                                        values = listOf(20f, 30f, 15f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q2",
                                        values = listOf(25f, 35f, 20f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q3",
                                        values = listOf(30f, 25f, 25f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q4",
                                        values = listOf(28f, 40f, 18f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF1976D2),
                                                        Color(0xFF64B5F6),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFF388E3C),
                                                        Color(0xFF81C784),
                                                    ),
                                                ),
                                                ChartyColor.Gradient(
                                                    listOf(
                                                        Color(0xFFE64A19),
                                                        Color(0xFFFF8A65),
                                                    ),
                                                ),
                                            ),
                                    ),
                                )
                            },
                            colors = ChartyColor.Solid(Color.Gray), // Fallback (not used)
                            stackedConfig =
                                StackedBarChartConfig(
                                    barWidthFraction = 0.7f,
                                    topCornerRadius = CornerRadius.Medium,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }

                // Stacked Horizontal Bar Chart
                item {
                    ChartCard(
                        title = "Stacked Horizontal Bar Chart",
                        description = "Part-to-whole composition displayed as horizontal stacked bars — great for category labels that need more space.",
                    ) {
                        StackedHorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            data = {
                                listOf(
                                    BarGroup(label = "Engineering", values = listOf(40f, 25f, 15f)),
                                    BarGroup(label = "Marketing", values = listOf(20f, 35f, 30f)),
                                    BarGroup(label = "Sales", values = listOf(35f, 20f, 25f)),
                                    BarGroup(label = "Support", values = listOf(15f, 30f, 20f)),
                                )
                            },
                            colors = ChartyColors.DefaultGradient,
                            config =
                                StackedHorizontalBarChartConfig(
                                    barWidthFraction = 0.6f,
                                    rightCornerRadius = CornerRadius.Medium,
                                    animation = Animation.Default,
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Stacked Horizontal Bar Chart — Custom Segment Colors",
                        description = "Each bar group can supply per-segment colors for precise color control.",
                    ) {
                        StackedHorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            data = {
                                listOf(
                                    BarGroup(
                                        label = "2021",
                                        values = listOf(30f, 45f, 25f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF2196F3)),
                                                ChartyColor.Solid(Color(0xFF4CAF50)),
                                                ChartyColor.Solid(Color(0xFFFF9800)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "2022",
                                        values = listOf(35f, 40f, 30f),
                                        colors =
                                            listOf(
                                                ChartyColor.Gradient(listOf(Color(0xFF1976D2), Color(0xFF42A5F5))),
                                                ChartyColor.Gradient(listOf(Color(0xFF388E3C), Color(0xFF81C784))),
                                                ChartyColor.Gradient(listOf(Color(0xFFF57C00), Color(0xFFFFB74D))),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "2023",
                                        values = listOf(50f, 30f, 20f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF9C27B0)),
                                                ChartyColor.Solid(Color(0xFFE91E63)),
                                                ChartyColor.Solid(Color(0xFF00BCD4)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "2024",
                                        values = listOf(45f, 35f, 35f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF3F51B5)),
                                                ChartyColor.Solid(Color(0xFF009688)),
                                                ChartyColor.Solid(Color(0xFFFFC107)),
                                            ),
                                    ),
                                )
                            },
                            config =
                                StackedHorizontalBarChartConfig(
                                    barWidthFraction = 0.65f,
                                    rightCornerRadius = CornerRadius.Large,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }

                // Grouped Horizontal Bar Chart
                item {
                    ChartCard(
                        title = "Grouped Horizontal Bar Chart",
                        description = "Multiple bars per row, side-by-side — compare series across categories.",
                    ) {
                        GroupedHorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(320.dp),
                            data = {
                                listOf(
                                    BarGroup(label = "North", values = listOf(120f, 85f, 60f)),
                                    BarGroup(label = "South", values = listOf(95f, 110f, 75f)),
                                    BarGroup(label = "East", values = listOf(80f, 90f, 100f)),
                                    BarGroup(label = "West", values = listOf(105f, 70f, 80f)),
                                )
                            },
                            colors = ChartyColors.ModernPalette,
                            config =
                                GroupedHorizontalBarChartConfig(
                                    barWidthFraction = 0.8f,
                                    barSpacing = 4f,
                                    cornerRadius = CornerRadius.Medium,
                                    animation = Animation.Default,
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Grouped Horizontal Bar Chart — Negative Values",
                        description = "Bars extend in both directions from the zero axis for profit/loss and temperature anomaly data.",
                    ) {
                        GroupedHorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(320.dp),
                            data = {
                                listOf(
                                    BarGroup(label = "Jan", values = listOf(40f, -15f, 25f)),
                                    BarGroup(label = "Feb", values = listOf(-20f, 30f, -10f)),
                                    BarGroup(label = "Mar", values = listOf(55f, -5f, 35f)),
                                    BarGroup(label = "Apr", values = listOf(-30f, 45f, 20f)),
                                )
                            },
                            colors = ChartyColors.WarmPalette,
                            config =
                                GroupedHorizontalBarChartConfig(
                                    barWidthFraction = 0.75f,
                                    barSpacing = 3f,
                                    cornerRadius = CornerRadius.Small,
                                    negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                                    animation = Animation.Enabled(duration = 900),
                                ),
                        )
                    }
                }

                // Normalized Horizontal Bar Chart
                item {
                    ChartCard(
                        title = "Normalized Horizontal Bar Chart",
                        description = "100% stacked horizontal bars — every bar extends to full width showing proportional share.",
                    ) {
                        NormalizedHorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            data = {
                                listOf(
                                    BarGroup(label = "Product A", values = listOf(50f, 30f, 20f)),
                                    BarGroup(label = "Product B", values = listOf(25f, 45f, 30f)),
                                    BarGroup(label = "Product C", values = listOf(35f, 25f, 40f)),
                                    BarGroup(label = "Product D", values = listOf(60f, 20f, 20f)),
                                )
                            },
                            colors = ChartyColors.DefaultGradient,
                            config =
                                NormalizedHorizontalBarChartConfig(
                                    barWidthFraction = 0.6f,
                                    rightCornerRadius = CornerRadius.Medium,
                                    animation = Animation.Default,
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Normalized Horizontal Bar Chart — Survey Results",
                        description = "Likert-scale survey responses with per-segment colors and percentage tooltips.",
                    ) {
                        NormalizedHorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(280.dp),
                            data = {
                                listOf(
                                    BarGroup(
                                        label = "Q1",
                                        values = listOf(40f, 35f, 15f, 10f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF4CAF50)),
                                                ChartyColor.Solid(Color(0xFF8BC34A)),
                                                ChartyColor.Solid(Color(0xFFFF9800)),
                                                ChartyColor.Solid(Color(0xFFF44336)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q2",
                                        values = listOf(30f, 40f, 20f, 10f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF4CAF50)),
                                                ChartyColor.Solid(Color(0xFF8BC34A)),
                                                ChartyColor.Solid(Color(0xFFFF9800)),
                                                ChartyColor.Solid(Color(0xFFF44336)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q3",
                                        values = listOf(20f, 25f, 35f, 20f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF4CAF50)),
                                                ChartyColor.Solid(Color(0xFF8BC34A)),
                                                ChartyColor.Solid(Color(0xFFFF9800)),
                                                ChartyColor.Solid(Color(0xFFF44336)),
                                            ),
                                    ),
                                    BarGroup(
                                        label = "Q4",
                                        values = listOf(10f, 15f, 30f, 45f),
                                        colors =
                                            listOf(
                                                ChartyColor.Solid(Color(0xFF4CAF50)),
                                                ChartyColor.Solid(Color(0xFF8BC34A)),
                                                ChartyColor.Solid(Color(0xFFFF9800)),
                                                ChartyColor.Solid(Color(0xFFF44336)),
                                            ),
                                    ),
                                )
                            },
                            config =
                                NormalizedHorizontalBarChartConfig(
                                    barWidthFraction = 0.65f,
                                    rightCornerRadius = CornerRadius.Large,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                        )
                    }
                }

                // Area Chart
                item {
                    ChartCard(
                        title = "Area Chart",
                        description = "Line chart with filled area - emphasizes magnitude of change",
                    ) {
                        AreaChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData(label = "Jan", value = 15f),
                                    LineData(label = "Feb", value = 28f),
                                    LineData(label = "Mar", value = 42f),
                                    LineData(label = "Apr", value = 35f),
                                    LineData(label = "May", value = 58f),
                                    LineData(label = "Jun", value = 48f),
                                    LineData(label = "Jul", value = 65f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF2196F3).copy(alpha = 0.2f),
                                    ),
                                ),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 3f,
                                    showPoints = true,
                                    pointRadius = 6f,
                                    smoothCurve = true,
                                    animation = Animation.Enabled(duration = 1200),
                                    fillAlpha = 0.4f,
                                ),
                        )
                    }
                }

                // Area Chart with Smooth Curve
                item {
                    ChartCard(
                        title = "Area Chart (Smooth)",
                        description = "Smooth curved area chart for elegant data visualization",
                    ) {
                        AreaChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData(label = "Mon", value = 12f),
                                    LineData(label = "Tue", value = 25f),
                                    LineData(label = "Wed", value = 18f),
                                    LineData(label = "Thu", value = 38f),
                                    LineData(label = "Fri", value = 30f),
                                    LineData(label = "Sat", value = 45f),
                                    LineData(label = "Sun", value = 35f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                                    ),
                                ),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 3f,
                                    showPoints = false,
                                    smoothCurve = true,
                                    animation = Animation.Enabled(duration = 1200),
                                    fillAlpha = 0.5f,
                                ),
                        )
                    }
                }

                // Multiline Chart - Smooth Curves (Interactive)
                item {
                    var selectedPoint by remember { mutableStateOf<MultilinePoint?>(null) }

                    Column {
                        // Show selected point info
                        selectedPoint?.let { point ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${point.lineGroup.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                    Text(
                                        text = "Line Series ${point.seriesIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                    Text(
                                        text = "Value: ${point.value}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Multiline Chart (Smooth) - Interactive",
                            description = "Click on any point to see details with tooltip",
                        ) {
                            MultilineChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        LineGroup(label = "Mon", values = listOf(20f, 35f, 15f)),
                                        LineGroup(label = "Tue", values = listOf(45f, 28f, 38f)),
                                        LineGroup(label = "Wed", values = listOf(30f, 52f, 25f)),
                                        LineGroup(label = "Thu", values = listOf(70f, 40f, 55f)),
                                        LineGroup(label = "Fri", values = listOf(55f, 65f, 45f)),
                                        LineGroup(label = "Sat", values = listOf(40f, 50f, 35f)),
                                    )
                                },
                                colors =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFFE91E63),
                                            Color(0xFF2196F3),
                                            Color(0xFF4CAF50),
                                        ),
                                    ),
                                lineConfig =
                                    LineChartConfig(
                                        lineWidth = 3f,
                                        smoothCurve = true,
                                        showPoints = true,
                                        pointRadius = 6f,
                                        animation = Animation.Enabled(duration = 1200),
                                    ),
                                onPointClick = { point ->
                                    selectedPoint = point
                                    println(
                                        "Multiline point clicked: ${point.lineGroup.label} Line ${point.seriesIndex + 1} = ${point.value}",
                                    )
                                },
                            )
                        }
                    }
                }

                // Multiline Chart - Straight Lines (Interactive)
                item {
                    var selectedPoint by remember { mutableStateOf<MultilinePoint?>(null) }

                    Column {
                        // Show selected point info
                        selectedPoint?.let { point ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${point.lineGroup.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "Line Series ${point.seriesIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                    Text(
                                        text = "Value: ${point.value}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Multiline Chart (Straight) - Interactive",
                            description = "Click on any point to see line details with tooltip",
                        ) {
                            MultilineChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        LineGroup(label = "Jan", values = listOf(25f, 40f)),
                                        LineGroup(label = "Feb", values = listOf(35f, 30f)),
                                        LineGroup(label = "Mar", values = listOf(50f, 45f)),
                                        LineGroup(label = "Apr", values = listOf(45f, 60f)),
                                        LineGroup(label = "May", values = listOf(60f, 50f)),
                                        LineGroup(label = "Jun", values = listOf(55f, 70f)),
                                    )
                                },
                                colors =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFFFF9800),
                                            Color(0xFF9C27B0),
                                        ),
                                    ),
                                lineConfig =
                                    LineChartConfig(
                                        lineWidth = 3f,
                                        smoothCurve = false,
                                        showPoints = true,
                                        pointRadius = 7f,
                                        animation = Animation.Enabled(duration = 1200),
                                    ),
                                onPointClick = { point ->
                                    selectedPoint = point
                                    println(
                                        "Multiline point clicked: ${point.lineGroup.label} Line ${point.seriesIndex + 1} = ${point.value}",
                                    )
                                },
                            )
                        }
                    }
                }

                // Stacked Area Chart - Smooth (Interactive)
                item {
                    var selectedAreaPoint by remember { mutableStateOf<StackedAreaPoint?>(null) }

                    Column {
                        // Show selected area point info
                        selectedAreaPoint?.let { point ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${point.lineGroup.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Area Series ${point.seriesIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Value: ${point.value}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = "Cumulative Total: ${point.cumulativeValue}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Stacked Area Chart (Smooth) - Interactive",
                            description = "Click on any area to see segment and cumulative values",
                        ) {
                            StackedAreaChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        LineGroup(label = "Mon", values = listOf(20f, 15f, 10f)),
                                        LineGroup(label = "Tue", values = listOf(45f, 28f, 12f)),
                                        LineGroup(label = "Wed", values = listOf(30f, 22f, 18f)),
                                        LineGroup(label = "Thu", values = listOf(70f, 30f, 15f)),
                                        LineGroup(label = "Fri", values = listOf(55f, 35f, 20f)),
                                        LineGroup(label = "Sat", values = listOf(40f, 25f, 15f)),
                                    )
                                },
                                colors =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF4CAF50),
                                            Color(0xFFFF9800),
                                        ),
                                    ),
                                lineConfig =
                                    LineChartConfig(
                                        lineWidth = 2f,
                                        smoothCurve = true,
                                        animation = Animation.Enabled(duration = 1200),
                                    ),
                                fillAlpha = 0.7f,
                                onAreaClick = { point ->
                                    selectedAreaPoint = point
                                    println(
                                        "Stacked area clicked: ${point.lineGroup.label} Area ${point.seriesIndex + 1} = ${point.value} (Total: ${point.cumulativeValue})",
                                    )
                                },
                            )
                        }
                    }
                }

                // Stacked Area Chart - Straight (Interactive)
                item {
                    var selectedAreaPoint by remember { mutableStateOf<StackedAreaPoint?>(null) }

                    Column {
                        // Show selected area point info
                        selectedAreaPoint?.let { point ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                    ),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Selected: ${point.lineGroup.label}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Text(
                                        text = "Area Series ${point.seriesIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Text(
                                        text = "Segment Value: ${point.value}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Text(
                                        text = "Total: ${point.cumulativeValue}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }

                        ChartCard(
                            title = "Stacked Area Chart (Straight) - Interactive",
                            description = "Click on any area to see breakdown with tooltip",
                        ) {
                            StackedAreaChart(
                                modifier = Modifier.fillMaxWidth().height(300.dp),
                                data = {
                                    listOf(
                                        LineGroup(label = "Q1", values = listOf(30f, 25f, 20f)),
                                        LineGroup(label = "Q2", values = listOf(40f, 35f, 25f)),
                                        LineGroup(label = "Q3", values = listOf(50f, 30f, 30f)),
                                        LineGroup(label = "Q4", values = listOf(45f, 40f, 28f)),
                                    )
                                },
                                colors =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFFE91E63),
                                            Color(0xFF9C27B0),
                                            Color(0xFF00BCD4),
                                        ),
                                    ),
                                lineConfig =
                                    LineChartConfig(
                                        lineWidth = 2f,
                                        smoothCurve = false,
                                        animation = Animation.Enabled(duration = 1200),
                                    ),
                                fillAlpha = 0.8f,
                                onAreaClick = { point ->
                                    selectedAreaPoint = point
                                    println(
                                        "Stacked area clicked: ${point.lineGroup.label} Area ${point.seriesIndex + 1} = ${point.value} (Total: ${point.cumulativeValue})",
                                    )
                                },
                            )
                        }
                    }
                }

                // Bubble Chart
                item {
                    ChartCard(
                        title = "Bubble Chart",
                        description = "Visualizes 3 dimensions: position, value, and size",
                    ) {
                        BubbleChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BubbleData("Product A", yValue = 45f, size = 150f),
                                    BubbleData("Product B", yValue = 72f, size = 280f),
                                    BubbleData("Product C", yValue = 38f, size = 100f),
                                    BubbleData("Product D", yValue = 85f, size = 220f),
                                    BubbleData("Product E", yValue = 55f, size = 180f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                        Color(0xFF9C27B0),
                                    ),
                                ),
                            config =
                                PointChartConfig(
                                    pointRadius = 40f,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                            minBubbleRadius = 15f,
                        )
                    }
                }

                // Pie Chart
                item {
                    ChartCard(
                        title = "Interactive Pie Chart",
                        description = "Classic pie chart with click interactions, animations, and legend",
                    ) {
                        var clickedSlice by remember { mutableStateOf<String?>(null) }

                        Column {
                            if (clickedSlice != null) {
                                Text(
                                    text = "Selected: $clickedSlice",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }

                            PieChart(
                                modifier = Modifier.fillMaxWidth().height(400.dp),
                                data = {
                                    listOf(
                                        PieData(label = "Product A", value = 45f),
                                        PieData(label = "Product B", value = 30f),
                                        PieData(label = "Product C", value = 15f),
                                        PieData(label = "Product D", value = 10f),
                                    )
                                },
                                color =
                                    ChartyColor.Gradient(
                                        listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF4CAF50),
                                            Color(0xFFFF9800),
                                            Color(0xFFE91E63),
                                        ),
                                    ),
                                config =
                                    PieChartConfig(
                                        style = PieChartStyle.PIE,
                                        labelConfig =
                                            LabelConfig(
                                                shouldShowLabels = true,
                                                shouldShowPercentage = true,
                                                minimumPercentageToShowLabel = 5f,
                                            ),
                                        interactionConfig =
                                            InteractionConfig(
                                                selectedScaleMultiplier = 1.15f,
                                                selectedSlicePullOutDistance = 12f,
                                            ),
                                        animation = Animation.Enabled(duration = 1000),
                                    ),
                                onSliceClick = { slice, _ ->
                                    clickedSlice = "${slice.label}: ${slice.value}"
                                },
                            )
                        }
                    }
                }

                // Donut Chart
                item {
                    ChartCard(
                        title = "Donut Chart with Center Content",
                        description = "Modern donut chart with center hole, custom colors, and right-side legend",
                    ) {
                        var selectedCategory by remember { mutableStateOf<String?>(null) }

                        PieChart(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            data = {
                                listOf(
                                    PieData(label = "Sales", value = 120f),
                                    PieData(label = "Marketing", value = 85f),
                                    PieData(label = "Development", value = 95f),
                                    PieData(label = "Support", value = 60f),
                                    PieData(label = "Operations", value = 40f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF00BCD4),
                                        Color(0xFF9C27B0),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                        Color(0xFFE91E63),
                                    ),
                                ),
                            config =
                                PieChartConfig(
                                    style = PieChartStyle.DONUT,
                                    donutHoleRatio = 0.65f,
                                    startAngleDegrees = -90f,
                                    labelConfig =
                                        LabelConfig(
                                            shouldShowLabels = false,
                                        ),
                                    interactionConfig =
                                        InteractionConfig(
                                            selectedScaleMultiplier = 1.1f,
                                            selectedSlicePullOutDistance = 10f,
                                            unselectedSliceOpacity = 0.5f,
                                        ),
                                    animation = Animation.Enabled(duration = 1200),
                                    sliceSpacingDegrees = 2f,
                                ),
                            onSliceClick = { slice, _ ->
                                selectedCategory = slice.label
                            },
                            centerContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = selectedCategory ?: "Total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text =
                                            if (selectedCategory != null) {
                                                "Selected"
                                            } else {
                                                "400"
                                            },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray,
                                    )
                                }
                            },
                        )
                    }
                }

                // Donut Chart with Top Legend
                item {
                    ChartCard(
                        title = "Compact Donut Chart",
                        description = "Donut chart with top legend and slice spacing",
                    ) {
                        PieChart(
                            modifier = Modifier.fillMaxWidth().height(450.dp),
                            data = {
                                listOf(
                                    PieData(label = "Electronics", value = 180f),
                                    PieData(label = "Clothing", value = 150f),
                                    PieData(label = "Food", value = 120f),
                                    PieData(label = "Books", value = 80f),
                                    PieData(label = "Sports", value = 70f),
                                    PieData(label = "Others", value = 50f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF3F51B5),
                                        Color(0xFF2196F3),
                                        Color(0xFF00BCD4),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFFEB3B),
                                        Color(0xFFFF5722),
                                    ),
                                ),
                            config =
                                PieChartConfig(
                                    style = PieChartStyle.DONUT,
                                    donutHoleRatio = 0.5f,
                                    labelConfig =
                                        LabelConfig(
                                            shouldShowLabels = true,
                                            shouldShowPercentage = true,
                                            minimumPercentageToShowLabel = 8f,
                                        ),
                                    interactionConfig =
                                        InteractionConfig(
                                            selectedScaleMultiplier = 1.08f,
                                            selectedSlicePullOutDistance = 8f,
                                        ),
                                    animation = Animation.Enabled(duration = 800),
                                    sliceSpacingDegrees = 3f,
                                    shouldShowCenterText = true,
                                ),
                            onSliceClick = { slice, index ->
                                println("Clicked ${slice.label} at index $index")
                            },
                        )
                    }
                }

                // Multiple Radar Chart - Basic Example
                item {
                    ChartCard(
                        title = "Multiple Radar Chart - Player Comparison",
                        description = "Compare multiple entities with overlapping radar charts and legend",
                    ) {
                        MultipleRadarChart(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            dataSets = {
                                listOf(
                                    RadarDataSet(
                                        label = "Player 1",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Speed", value = 85f),
                                                RadarAxisData(label = "Power", value = 75f),
                                                RadarAxisData(label = "Defense", value = 90f),
                                                RadarAxisData(label = "Skill", value = 80f),
                                                RadarAxisData(label = "Stamina", value = 70f),
                                                RadarAxisData(label = "Accuracy", value = 88f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF00BCD4)),
                                        fillAlpha = 0.3f,
                                    ),
                                    RadarDataSet(
                                        label = "Player 2",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Speed", value = 70f),
                                                RadarAxisData(label = "Power", value = 90f),
                                                RadarAxisData(label = "Defense", value = 75f),
                                                RadarAxisData(label = "Skill", value = 85f),
                                                RadarAxisData(label = "Stamina", value = 88f),
                                                RadarAxisData(label = "Accuracy", value = 72f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFFE91E63)),
                                        fillAlpha = 0.3f,
                                    ),
                                    RadarDataSet(
                                        label = "Player 3",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Speed", value = 92f),
                                                RadarAxisData(label = "Power", value = 65f),
                                                RadarAxisData(label = "Defense", value = 70f),
                                                RadarAxisData(label = "Skill", value = 95f),
                                                RadarAxisData(label = "Stamina", value = 80f),
                                                RadarAxisData(label = "Accuracy", value = 90f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF4CAF50)),
                                        fillAlpha = 0.3f,
                                    ),
                                )
                            },
                            config =
                                MultipleRadarChartConfig(
                                    showLegend = true,
                                    legendPosition = LegendPosition.TOP_RIGHT,
                                    radarConfig =
                                        RadarChartConfig(
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.POLYGON,
                                                    numberOfGridLevels = 5,
                                                    gridLineColor = ChartyColor.Solid(Color(0xFF455A64)),
                                                    axisLineColor = ChartyColor.Solid(Color(0xFF607D8B)),
                                                ),
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = false,
                                                ),
                                            animation = Animation.Enabled(duration = 1000),
                                            showDataPoints = true,
                                            dataPointRadius = 5f,
                                        ),
                                    staggerAnimation = true,
                                    staggerDelay = 0.2f,
                                    showPointInnerCircle = true,
                                ),
                            onDataSetClick = { dataSet, index ->
                                println("Clicked dataset: ${dataSet.label} at index $index")
                            },
                        )
                    }
                }

                // Multiple Radar Chart - Product Features
                item {
                    ChartCard(
                        title = "Multiple Radar Chart - Product Features",
                        description = "Circular grid with 8 attributes comparison",
                    ) {
                        MultipleRadarChart(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            dataSets = {
                                listOf(
                                    RadarDataSet(
                                        label = "Product A",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Price", value = 70f),
                                                RadarAxisData(label = "Quality", value = 90f),
                                                RadarAxisData(label = "Durability", value = 85f),
                                                RadarAxisData(label = "Design", value = 95f),
                                                RadarAxisData(label = "Features", value = 80f),
                                                RadarAxisData(label = "Support", value = 75f),
                                                RadarAxisData(label = "Warranty", value = 88f),
                                                RadarAxisData(label = "Value", value = 82f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF9C27B0)),
                                        fillAlpha = 0.25f,
                                    ),
                                    RadarDataSet(
                                        label = "Product B",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Price", value = 95f),
                                                RadarAxisData(label = "Quality", value = 75f),
                                                RadarAxisData(label = "Durability", value = 70f),
                                                RadarAxisData(label = "Design", value = 80f),
                                                RadarAxisData(label = "Features", value = 90f),
                                                RadarAxisData(label = "Support", value = 85f),
                                                RadarAxisData(label = "Warranty", value = 65f),
                                                RadarAxisData(label = "Value", value = 88f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFFFF9800)),
                                        fillAlpha = 0.25f,
                                    ),
                                )
                            },
                            config =
                                MultipleRadarChartConfig(
                                    radarConfig =
                                        RadarChartConfig(
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.CIRCULAR,
                                                    numberOfGridLevels = 4,
                                                    gridLineColor = ChartyColor.Solid(Color(0xFF455A64)),
                                                    axisLineColor = ChartyColor.Solid(Color(0xFF607D8B)),
                                                ),
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = false,
                                                ),
                                            animation = Animation.Enabled(duration = 1200),
                                            showDataPoints = true,
                                            dataPointRadius = 6f,
                                        ),
                                    staggerAnimation = true,
                                    staggerDelay = 0.15f,
                                    showPointInnerCircle = true,
                                ),
                        )
                    }
                }

                // Multiple Radar Chart - Skills Assessment
                item {
                    ChartCard(
                        title = "Multiple Radar Chart - Team Skills",
                        description = "Pentagon chart with minimal points and custom styling",
                    ) {
                        MultipleRadarChart(
                            modifier = Modifier.fillMaxWidth().height(380.dp),
                            dataSets = {
                                listOf(
                                    RadarDataSet(
                                        label = "Frontend",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "React", value = 95f),
                                                RadarAxisData(label = "Vue", value = 80f),
                                                RadarAxisData(label = "Angular", value = 70f),
                                                RadarAxisData(label = "Svelte", value = 85f),
                                                RadarAxisData(label = "Mobile", value = 75f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF2196F3)),
                                        fillAlpha = 0.35f,
                                    ),
                                    RadarDataSet(
                                        label = "Backend",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "React", value = 60f),
                                                RadarAxisData(label = "Vue", value = 55f),
                                                RadarAxisData(label = "Angular", value = 50f),
                                                RadarAxisData(label = "Svelte", value = 45f),
                                                RadarAxisData(label = "Mobile", value = 40f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFFFF5722)),
                                        fillAlpha = 0.35f,
                                    ),
                                    RadarDataSet(
                                        label = "Full Stack",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "React", value = 88f),
                                                RadarAxisData(label = "Vue", value = 75f),
                                                RadarAxisData(label = "Angular", value = 70f),
                                                RadarAxisData(label = "Svelte", value = 65f),
                                                RadarAxisData(label = "Mobile", value = 82f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF00C853)),
                                        fillAlpha = 0.35f,
                                    ),
                                )
                            },
                            config =
                                MultipleRadarChartConfig(
                                    radarConfig =
                                        RadarChartConfig(
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.POLYGON,
                                                    numberOfGridLevels = 5,
                                                    gridLineWidth = 1.5f,
                                                    gridLineColor = ChartyColor.Solid(Color(0xFF455A64)),
                                                    axisLineColor = ChartyColor.Solid(Color(0xFF607D8B)),
                                                ),
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = false,
                                                ),
                                            animation = Animation.Enabled(duration = 1500),
                                            showDataPoints = true,
                                            dataPointRadius = 7f,
                                            dataLineWidth = 2.5f,
                                        ),
                                    staggerAnimation = true,
                                    staggerDelay = 0.25f,
                                    showPointInnerCircle = false,
                                    datasetLineWidth = 3f,
                                    datasetPointRadius = 8f,
                                ),
                        )
                    }
                }

                // Multiple Radar Chart - Performance Metrics
                item {
                    ChartCard(
                        title = "Multiple Radar Chart - Performance Metrics",
                        description = "4 datasets with circular grid and gradient colors",
                    ) {
                        MultipleRadarChart(
                            modifier = Modifier.fillMaxWidth().height(420.dp),
                            dataSets = {
                                listOf(
                                    RadarDataSet(
                                        label = "Q1 2024",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Sales", value = 75f),
                                                RadarAxisData(label = "Growth", value = 68f),
                                                RadarAxisData(label = "Profit", value = 82f),
                                                RadarAxisData(label = "Customer Sat", value = 90f),
                                                RadarAxisData(label = "Market Share", value = 70f),
                                                RadarAxisData(label = "Innovation", value = 85f),
                                                RadarAxisData(label = "Efficiency", value = 78f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFFFF6B6B)),
                                        fillAlpha = 0.28f,
                                    ),
                                    RadarDataSet(
                                        label = "Q2 2024",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Sales", value = 82f),
                                                RadarAxisData(label = "Growth", value = 75f),
                                                RadarAxisData(label = "Profit", value = 88f),
                                                RadarAxisData(label = "Customer Sat", value = 92f),
                                                RadarAxisData(label = "Market Share", value = 78f),
                                                RadarAxisData(label = "Innovation", value = 80f),
                                                RadarAxisData(label = "Efficiency", value = 85f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF4ECDC4)),
                                        fillAlpha = 0.28f,
                                    ),
                                    RadarDataSet(
                                        label = "Q3 2024",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Sales", value = 88f),
                                                RadarAxisData(label = "Growth", value = 85f),
                                                RadarAxisData(label = "Profit", value = 90f),
                                                RadarAxisData(label = "Customer Sat", value = 95f),
                                                RadarAxisData(label = "Market Share", value = 85f),
                                                RadarAxisData(label = "Innovation", value = 88f),
                                                RadarAxisData(label = "Efficiency", value = 92f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFF95E1D3)),
                                        fillAlpha = 0.28f,
                                    ),
                                    RadarDataSet(
                                        label = "Q4 2024 (Projected)",
                                        axes =
                                            listOf(
                                                RadarAxisData(label = "Sales", value = 92f),
                                                RadarAxisData(label = "Growth", value = 90f),
                                                RadarAxisData(label = "Profit", value = 93f),
                                                RadarAxisData(label = "Customer Sat", value = 97f),
                                                RadarAxisData(label = "Market Share", value = 88f),
                                                RadarAxisData(label = "Innovation", value = 92f),
                                                RadarAxisData(label = "Efficiency", value = 95f),
                                            ),
                                        color = ChartyColor.Solid(Color(0xFFF38181)),
                                        fillAlpha = 0.28f,
                                    ),
                                )
                            },
                            config =
                                MultipleRadarChartConfig(
                                    showLegend = true,
                                    radarConfig =
                                        RadarChartConfig(
                                            gridConfig =
                                                RadarGridConfig(
                                                    gridStyle = RadarGridStyle.CIRCULAR,
                                                    numberOfGridLevels = 5,
                                                    gridLineColor =
                                                        ChartyColor.Solid(
                                                            Color(0xFF546E7A).copy(alpha = 0.35f),
                                                        ),
                                                    axisLineColor =
                                                        ChartyColor.Solid(
                                                            Color(0xFF78909C).copy(alpha = 0.45f),
                                                        ),
                                                ),
                                            labelConfig =
                                                RadarLabelConfig(
                                                    showLabels = true,
                                                ),
                                            animation = Animation.Enabled(duration = 1800),
                                            showDataPoints = true,
                                            dataPointRadius = 4f,
                                        ),
                                    staggerAnimation = true,
                                    staggerDelay = 0.1f,
                                    showPointInnerCircle = true,
                                ),
                        )
                    }
                }

                // Footer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "ChartContext API",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text =
                                    "All charts built using the same ChartContext helpers:\n" +
                                        "• valueToY() - Value to pixel conversion\n" +
                                        "• getBarX() - Bar positioning\n" +
                                        "• getGroupCenterX() - Centered positioning",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                item {
                    ChartCard(
                        title = "Calendar Heatmap Chart",
                        description = "GitHub-style contribution graph. Tap a cell to see the value.",
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            val calendarData =
                                remember {
                                    buildList {
                                        val contributions =
                                            listOf(
                                                0,
                                                1,
                                                2,
                                                4,
                                                6,
                                                3,
                                                0,
                                                5,
                                                7,
                                                2,
                                                1,
                                                0,
                                                4,
                                                8,
                                                3,
                                                5,
                                                0,
                                                2,
                                                6,
                                                1,
                                                9,
                                                4,
                                                3,
                                                0,
                                                7,
                                                2,
                                                5,
                                                1,
                                                8,
                                                0,
                                                4,
                                            )
                                        var idx = 0
                                        for (month in 1..12) {
                                            val days =
                                                when (month) {
                                                    2 -> 29
                                                    4, 6, 9, 11 -> 30
                                                    else -> 31
                                                }
                                            for (day in 1..days) {
                                                val value = contributions[idx % contributions.size].toFloat()
                                                add(CalendarData(year = 2024, month = month, day = day, value = value))
                                                idx++
                                            }
                                        }
                                    }
                                }

                            Text(
                                text = "Default (Rounded Squares, Sunday start)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            CalendarHeatmapChart(
                                data = { calendarData },
                                modifier = Modifier.fillMaxWidth(),
                                config =
                                    CalendarHeatmapConfig(
                                        intensityColors =
                                            listOf(
                                                Color(0xFF9BE9A8),
                                                Color(0xFF40C463),
                                                Color(0xFF30A14E),
                                                Color(0xFF216E39),
                                            ),
                                        cellShape = CellShape.RoundedSquare(cornerRadius = 2f),
                                        weekStartDay = WeekStartDay.SUNDAY,
                                    ),
                            )

                            Text(
                                text = "Circle Cells, Monday start",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            CalendarHeatmapChart(
                                data = { calendarData },
                                modifier = Modifier.fillMaxWidth(),
                                config =
                                    CalendarHeatmapConfig(
                                        intensityColors =
                                            listOf(
                                                Color(0xFFBBDEFB),
                                                Color(0xFF64B5F6),
                                                Color(0xFF1976D2),
                                                Color(0xFF0D47A1),
                                            ),
                                        emptyColor = Color(0xFFE3F2FD),
                                        cellShape = CellShape.Circle,
                                        weekStartDay = WeekStartDay.MONDAY,
                                    ),
                            )

                            Text(
                                text = "Diamond Cells, last 26 weeks, non-scrollable",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            CalendarHeatmapChart(
                                data = { calendarData },
                                modifier = Modifier.fillMaxWidth(),
                                visibleWeeks = 26,
                                scrollEnabled = false,
                                config =
                                    CalendarHeatmapConfig(
                                        intensityColors =
                                            listOf(
                                                Color(0xFFF8BBD9),
                                                Color(0xFFF48FB1),
                                                Color(0xFFE91E63),
                                                Color(0xFF880E4F),
                                            ),
                                        emptyColor = Color(0xFFFCE4EC),
                                        cellShape = CellShape.Diamond,
                                        weekStartDay = WeekStartDay.SUNDAY,
                                    ),
                            )
                        }
                    }
                }

                item {
                    ChartCard(
                        title = "Zoom & Pan",
                        description = "Pinch to zoom and drag to pan across a large dataset. The viewport shows 40 weekly data points.",
                    ) {
                        val viewPortState = rememberViewPortState()
                        LineChart(
                            data = {
                                listOf(
                                    12f,
                                    25f,
                                    8f,
                                    40f,
                                    33f,
                                    18f,
                                    50f,
                                    44f,
                                    29f,
                                    60f,
                                    55f,
                                    38f,
                                    70f,
                                    62f,
                                    45f,
                                    80f,
                                    74f,
                                    58f,
                                    90f,
                                    84f,
                                    67f,
                                    95f,
                                    89f,
                                    72f,
                                    100f,
                                    93f,
                                    78f,
                                    85f,
                                    70f,
                                    60f,
                                    50f,
                                    65f,
                                    75f,
                                    55f,
                                    45f,
                                    35f,
                                    48f,
                                    30f,
                                    20f,
                                    10f,
                                ).mapIndexed { i, v -> LineData(value = v, label = "W${i + 1}") }
                            },
                            modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp),
                            color = ChartyColor.Solid(Color(0xFF6200EE)),
                            lineConfig = LineChartConfig(smoothCurve = true),
                            interactionConfig =
                                ChartInteractionConfig(
                                    viewPortState = viewPortState,
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Brush Range Selection",
                        description = "Drag across the chart to highlight a data range. The selected indices are reported below.",
                    ) {
                        val brushState = rememberBrushSelectionState()
                        var selectedRange by remember { mutableStateOf("None selected") }
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Selected: $selectedRange",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            BarChart(
                                data = {
                                    listOf(
                                        BarData(value = 30f, label = "Jan"),
                                        BarData(value = 45f, label = "Feb"),
                                        BarData(value = 25f, label = "Mar"),
                                        BarData(value = 60f, label = "Apr"),
                                        BarData(value = 50f, label = "May"),
                                        BarData(value = 40f, label = "Jun"),
                                        BarData(value = 70f, label = "Jul"),
                                        BarData(value = 55f, label = "Aug"),
                                        BarData(value = 35f, label = "Sep"),
                                        BarData(value = 65f, label = "Oct"),
                                        BarData(value = 48f, label = "Nov"),
                                        BarData(value = 80f, label = "Dec"),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                color = ChartyColor.Solid(Color(0xFF6200EE)),
                                interactionConfig =
                                    ChartInteractionConfig(
                                        brushSelectionState = brushState,
                                        onRangeSelect = { start, end ->
                                            selectedRange = "Index $start – $end"
                                        },
                                    ),
                            )
                        }
                    }
                }

                item {
                    ChartCard(
                        title = "Chart Annotations",
                        description = "Vertical marker lines with labels pinned to specific data-point indices.",
                    ) {
                        LineChart(
                            data = {
                                listOf(
                                    LineData(value = 20f, label = "Mon"),
                                    LineData(value = 35f, label = "Tue"),
                                    LineData(value = 28f, label = "Wed"),
                                    LineData(value = 55f, label = "Thu"),
                                    LineData(value = 42f, label = "Fri"),
                                    LineData(value = 60f, label = "Sat"),
                                    LineData(value = 48f, label = "Sun"),
                                    LineData(value = 72f, label = "Mon"),
                                    LineData(value = 38f, label = "Tue"),
                                    LineData(value = 52f, label = "Wed"),
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp),
                            color = ChartyColor.Solid(Color(0xFF0288D1)),
                            lineConfig = LineChartConfig(smoothCurve = true, showPoints = true),
                            interactionConfig =
                                ChartInteractionConfig(
                                    annotations =
                                        listOf(
                                            ChartAnnotation(
                                                xIndex = 2,
                                                label = "Dip",
                                                style =
                                                    AnnotationStyle(
                                                        lineColor = Color(0xFFE53935),
                                                        labelBackgroundColor = Color(0xFFE53935),
                                                    ),
                                            ),
                                            ChartAnnotation(
                                                xIndex = 7,
                                                label = "Peak",
                                                style =
                                                    AnnotationStyle(
                                                        lineColor = Color(0xFF43A047),
                                                        labelBackgroundColor = Color(0xFF43A047),
                                                        isDashed = false,
                                                    ),
                                            ),
                                        ),
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Crosshair",
                        description = "Long-press and drag to reveal a crosshair that snaps to the nearest data point.",
                    ) {
                        LineChart(
                            data = {
                                listOf(
                                    LineData(value = 10f, label = "Jan"),
                                    LineData(value = 42f, label = "Feb"),
                                    LineData(value = 28f, label = "Mar"),
                                    LineData(value = 65f, label = "Apr"),
                                    LineData(value = 48f, label = "May"),
                                    LineData(value = 80f, label = "Jun"),
                                    LineData(value = 55f, label = "Jul"),
                                    LineData(value = 72f, label = "Aug"),
                                    LineData(value = 30f, label = "Sep"),
                                    LineData(value = 50f, label = "Oct"),
                                    LineData(value = 38f, label = "Nov"),
                                    LineData(value = 68f, label = "Dec"),
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp),
                            color = ChartyColor.Solid(Color(0xFFE91E63)),
                            lineConfig =
                                LineChartConfig(
                                    smoothCurve = true,
                                    showPoints = true,
                                    crosshairConfig =
                                        ChartCrosshairConfig(
                                            verticalLineColor = ChartyColor.Solid(Color(0xFFE91E63).copy(alpha = 0.6f)),
                                            dotRadius = 10f,
                                        ),
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Multiline Crosshair (custom label)",
                        description = "Drag to snap the unified crosshair to the nearest x. The label is your own composable, drawn over the line.",
                    ) {
                        MultilineChart(
                            modifier = Modifier.fillMaxWidth().height(260.dp).padding(16.dp),
                            data = {
                                listOf(
                                    LineGroup(label = "Mon", values = listOf(20f, 35f, 15f)),
                                    LineGroup(label = "Tue", values = listOf(45f, 28f, 38f)),
                                    LineGroup(label = "Wed", values = listOf(30f, 52f, 25f)),
                                    LineGroup(label = "Thu", values = listOf(70f, 40f, 55f)),
                                    LineGroup(label = "Fri", values = listOf(55f, 65f, 45f)),
                                    LineGroup(label = "Sat", values = listOf(40f, 50f, 35f)),
                                )
                            },
                            colors =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                    ),
                                ),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 3f,
                                    smoothCurve = true,
                                    showPoints = true,
                                    animation = Animation.Enabled(duration = 1000),
                                ),
                            crosshair =
                                ChartCrosshair(
                                    config =
                                        ChartCrosshairConfig(
                                            verticalLineColor = ChartyColor.Solid(Color(0xFF607D8B).copy(alpha = 0.7f)),
                                            dotRadius = 8f,
                                        ),
                                    label = {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .background(
                                                        color = Color(0xFF263238),
                                                        shape = RoundedCornerShape(8.dp),
                                                    ).padding(horizontal = 10.dp, vertical = 6.dp),
                                        ) {
                                            Text(
                                                text = "${data.lineGroup.label}: ${data.lineGroup.values.joinToString(" / ") { it.toInt().toString() }}",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                            )
                                        }
                                    },
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Stacked Area Crosshair",
                        description = "Drag to snap the crosshair to the stacked top position at each x.",
                    ) {
                        StackedAreaChart(
                            modifier = Modifier.fillMaxWidth().height(260.dp).padding(16.dp),
                            data = {
                                listOf(
                                    LineGroup(label = "Mon", values = listOf(20f, 15f, 10f)),
                                    LineGroup(label = "Tue", values = listOf(45f, 28f, 12f)),
                                    LineGroup(label = "Wed", values = listOf(30f, 22f, 18f)),
                                    LineGroup(label = "Thu", values = listOf(70f, 30f, 15f)),
                                    LineGroup(label = "Fri", values = listOf(55f, 35f, 20f)),
                                    LineGroup(label = "Sat", values = listOf(40f, 25f, 15f)),
                                )
                            },
                            colors =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                    ),
                                ),
                            lineConfig =
                                LineChartConfig(
                                    lineWidth = 2f,
                                    smoothCurve = true,
                                    animation = Animation.Enabled(duration = 1000),
                                    crosshairConfig =
                                        ChartCrosshairConfig(
                                            verticalLineColor = ChartyColor.Solid(Color(0xFF2196F3).copy(alpha = 0.7f)),
                                            dotRadius = 8f,
                                        ),
                                ),
                            fillAlpha = 0.7f,
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Combo Chart Crosshair",
                        description = "Drag to reveal a crosshair that snaps to the line data points.",
                    ) {
                        ComboChart(
                            modifier = Modifier.fillMaxWidth().height(260.dp).padding(16.dp),
                            data = {
                                listOf(
                                    ComboChartData("Jan", barValue = 100f, lineValue = 80f),
                                    ComboChartData("Feb", barValue = 150f, lineValue = 120f),
                                    ComboChartData("Mar", barValue = 120f, lineValue = 140f),
                                    ComboChartData("Apr", barValue = 180f, lineValue = 160f),
                                    ComboChartData("May", barValue = 160f, lineValue = 145f),
                                    ComboChartData("Jun", barValue = 200f, lineValue = 180f),
                                )
                            },
                            barColor = ChartyColor.Solid(Color(0xFF2196F3)),
                            lineColor = ChartyColor.Solid(Color(0xFFFF5722)),
                            comboConfig =
                                ComboChartConfig(
                                    barWidthFraction = 0.6f,
                                    lineWidth = 3f,
                                    showPoints = true,
                                    animation = Animation.Enabled(duration = 1000),
                                    crosshairConfig =
                                        ChartCrosshairConfig(
                                            verticalLineColor = ChartyColor.Solid(Color(0xFFFF5722).copy(alpha = 0.7f)),
                                            dotRadius = 8f,
                                        ),
                                ),
                        )
                    }
                }

                item {
                    ChartCard(
                        title = "Bubble Chart Crosshair",
                        description = "Drag to reveal a crosshair that snaps to each bubble center.",
                    ) {
                        BubbleChart(
                            modifier = Modifier.fillMaxWidth().height(260.dp).padding(16.dp),
                            data = {
                                listOf(
                                    BubbleData("Product A", yValue = 45f, size = 150f),
                                    BubbleData("Product B", yValue = 72f, size = 280f),
                                    BubbleData("Product C", yValue = 38f, size = 100f),
                                    BubbleData("Product D", yValue = 85f, size = 220f),
                                    BubbleData("Product E", yValue = 55f, size = 180f),
                                )
                            },
                            color =
                                ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFFE91E63),
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                        Color(0xFF9C27B0),
                                    ),
                                ),
                            config =
                                PointChartConfig(
                                    pointRadius = 40f,
                                    animation = Animation.Enabled(duration = 1000),
                                    crosshairConfig =
                                        ChartCrosshairConfig(
                                            verticalLineColor = ChartyColor.Solid(Color(0xFF607D8B).copy(alpha = 0.7f)),
                                            dotRadius = 10f,
                                        ),
                                ),
                            minBubbleRadius = 15f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Color(0xFFEDE6E2)),
    ) {
        Column(
            Modifier
                .background(Color(0xFFEDE6E2))
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth().background(Color(0xff4C2849)).padding(16.dp),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
