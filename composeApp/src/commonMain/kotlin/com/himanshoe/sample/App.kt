package com.himanshoe.sample

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.BarData
import com.himanshoe.charty.bar.ComparisonBarChart
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.StackedBarChart
import com.himanshoe.charty.bar.SpanChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.circular.CircularProgressIndicator
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.data.PointData
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.config.PieChartStyle
import com.himanshoe.charty.pie.config.InteractionConfig
import com.himanshoe.charty.pie.config.LabelConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.StackedAreaChart
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.point.BubbleChart
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.combo.ComboChart
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.candlestick.CandlestickChart
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Candlestick Chart
                item {
                    ChartCard(
                        title = "Candlestick Chart",
                        description = "Financial OHLC chart - shows open, high, low, close prices (auto-filters to 5 labels when >10 data points)"
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
                                        close = 96f
                                    ),
                                    CandleData(
                                        "17:15",
                                        open = 96f,
                                        high = 96.3f,
                                        low = 95.2f,
                                        close = 95.5f
                                    ),
                                    CandleData(
                                        "17:30",
                                        open = 95.5f,
                                        high = 96.8f,
                                        low = 95.5f,
                                        close = 96.4f
                                    ),
                                    CandleData(
                                        "17:45",
                                        open = 96.4f,
                                        high = 96.7f,
                                        low = 95.8f,
                                        close = 96f
                                    ),
                                    CandleData(
                                        "18:00",
                                        open = 96f,
                                        high = 96.2f,
                                        low = 94.8f,
                                        close = 95f
                                    ),
                                    CandleData(
                                        "18:15",
                                        open = 95f,
                                        high = 95.6f,
                                        low = 94.6f,
                                        close = 95.3f
                                    ),
                                    CandleData(
                                        "18:30",
                                        open = 95.3f,
                                        high = 95.5f,
                                        low = 94.5f,
                                        close = 94.8f
                                    ),
                                    CandleData(
                                        "18:45",
                                        open = 94.8f,
                                        high = 95.2f,
                                        low = 94.3f,
                                        close = 94.6f
                                    ),
                                    CandleData(
                                        "19:00",
                                        open = 94.6f,
                                        high = 95f,
                                        low = 94.4f,
                                        close = 94.5f
                                    ),
                                    CandleData(
                                        "19:15",
                                        open = 94.5f,
                                        high = 94.9f,
                                        low = 94.2f,
                                        close = 94.4f
                                    ),
                                    CandleData(
                                        "19:30",
                                        open = 94.4f,
                                        high = 94.8f,
                                        low = 94f,
                                        close = 94.3f
                                    ),
                                    CandleData(
                                        "19:45",
                                        open = 94.3f,
                                        high = 94.7f,
                                        low = 94.2f,
                                        close = 94.5f
                                    ),
                                    CandleData(
                                        "20:00",
                                        open = 94.5f,
                                        high = 95.5f,
                                        low = 94.3f,
                                        close = 95.2f
                                    ),
                                    CandleData(
                                        "20:15",
                                        open = 95.2f,
                                        high = 95.7f,
                                        low = 95f,
                                        close = 95.5f
                                    ),
                                    CandleData(
                                        "20:30",
                                        open = 95.5f,
                                        high = 96f,
                                        low = 95.3f,
                                        close = 95.8f
                                    ),
                                    CandleData(
                                        "20:45",
                                        open = 95.8f,
                                        high = 96.2f,
                                        low = 95.6f,
                                        close = 96f
                                    ),
                                    CandleData(
                                        "21:00",
                                        open = 96f,
                                        high = 96.5f,
                                        low = 95.5f,
                                        close = 96.2f
                                    ),
                                    CandleData(
                                        "21:15",
                                        open = 96.2f,
                                        high = 97f,
                                        low = 96f,
                                        close = 96.8f
                                    ),
                                    CandleData(
                                        "21:30",
                                        open = 96.8f,
                                        high = 97.8f,
                                        low = 96.5f,
                                        close = 97.4f
                                    ),
                                    CandleData(
                                        "21:45",
                                        open = 97.4f,
                                        high = 97.9f,
                                        low = 97.2f,
                                        close = 97.6f
                                    ),
                                    CandleData(
                                        "22:00",
                                        open = 97.6f,
                                        high = 97.8f,
                                        low = 97f,
                                        close = 97.2f
                                    )
                                )
                            },
                            bullishColor = ChartyColor.Solid(Color(0xFFFFC107)), // Yellow/Gold for bullish
                            bearishColor = ChartyColor.Solid(Color(0xFFE91E63)), // Pink for bearish
                            candlestickConfig = CandlestickChartConfig(
                                candleWidthFraction = 0.7f,
                                wickWidthFraction = 0.15f,
                                showWicks = true,
                                minCandleBodyHeight = 2f,
                                cornerRadius = CornerRadius.ExtraLarge,
                                animation = Animation.Enabled(duration = 1000)
                            )
                        )
                    }
                }
                item {
                    ChartCard(
                        title = "Combo Chart (Bar + Line)",
                        description = "Combines bars and line in one chart - perfect for comparing two related metrics"
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
                                    ComboChartData("Jun", barValue = 200f, lineValue = 180f)
                                )
                            },
                            barColor = ChartyColor.Solid(Color(0xFF2196F3)),
                            lineColor = ChartyColor.Solid(Color(0xFFFF5722)),
                            comboConfig = ComboChartConfig(
                                barWidthFraction = 0.6f,
                                lineWidth = 3f,
                                showPoints = true,
                                pointRadius = 6f,
                                smoothCurve = false,
                                animation = Animation.Enabled(duration = 1200)
                            )
                        )
                    }
                }

                // Horizontal Bar Chart
                item {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            rings = {
                                listOf(
                                    CircularRingData(
                                        label = "Move",
                                        progress = 450f,
                                        maxValue = 600f,
                                        color = ChartyColor.Solid(Color(0xFFFF3B58)),
                                        backgroundColor = ChartyColor.Solid(Color(0x33FF3B58))
                                    ),
                                    CircularRingData(
                                        label = "Exercise",
                                        progress = 25f,
                                        maxValue = 30f,
                                        color = ChartyColor.Solid(Color(0xFFACFF3D)),
                                        backgroundColor = ChartyColor.Solid(Color(0x33ACFF3D))
                                    ),
                                    CircularRingData(
                                        label = "Stand",
                                        progress = 10f,
                                        maxValue = 12f,
                                        color = ChartyColor.Solid(Color(0xFF34D5FF)),
                                        backgroundColor = ChartyColor.Solid(Color(0x3334D5FF))
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            config = CircularProgressConfig(
                                centerHoleRatio = 0.4f,
                                gapBetweenRings = 12f,
                                startAngleDegrees = -90f,
                                strokeCap = StrokeCap.Round,
                                showCenterText = false,
                                animation = Animation.Enabled(duration = 1500)
                            ),
                            centerContent = {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val holeRadius = (size.minDimension / 2f) * 0.34f
                                    drawCircle(
                                        color = Color.Black,
                                        radius = holeRadius,
                                        center = Offset(size.width / 2f, size.height / 2f)
                                    )
                                }
                            }
                        )
                    }
                }
                item {
                    ChartCard(
                        title = "Horizontal Bar Chart",
                        description = "Bars extending horizontally - great for long category labels"
                    ) {
                        HorizontalBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarData("Marketing", 50f),
                                    BarData("Development", 60f),
                                    BarData("Sales", 60f),
                                    BarData("Operations", 60f),
                                    BarData("Support", 61f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF9C27B0)),
                            barConfig = BarChartConfig(
                                barWidthFraction = 0.7f,
                                cornerRadius = CornerRadius.Large,
                                animation = Animation.Enabled(duration = 1000)
                            )
                        )
                    }
                }

                // Span Chart
                item {
                    ChartCard(
                        title = "Span Chart (Range Chart)",
                        description = "Shows ranges or time spans horizontally - ideal for schedules and timelines"
                    ) {
                        SpanChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    SpanData("Category 1", startValue = 1f, endValue = 15f),
                                    SpanData("Category 2", startValue = 12f, endValue = 28f),
                                    SpanData("Category 3", startValue = 3f, endValue = 18f),
                                    SpanData("Category 4", startValue = 18f, endValue = 32f),
                                    SpanData("Category 5", startValue = 8f, endValue = 22f)
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFF9800),
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0)
                                )
                            ),
                            barConfig = BarChartConfig(
                                barWidthFraction = 0.7f,
                                cornerRadius = CornerRadius.Medium,
                                animation = Animation.Enabled(duration = 1000)
                            )
                        )
                    }
                }

                // Simple Bar Chart
                item {
                    ChartCard(
                        title = "Simple Bar Chart with Rounded Corners",
                        description = "Animated bar chart with rounded top corners"
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData("Jan", 45f),
                                    BarData("Feb", 78f),
                                    BarData("Mar", 62f),
                                    BarData("Apr", 89f),
                                    BarData("May", 55f)
                                )
                            },
                            barConfig = BarChartConfig(
                                barWidthFraction = 0.7f,
                                cornerRadius = CornerRadius.Large,
                                animation = Animation.Enabled(duration = 1000)
                            ),
                            color = ChartyColor.Solid(Color(0xFF2196F3))
                        )
                    }
                }

                // Bar Chart with Negative Values - Below Axis Mode
                item {
                    ChartCard(
                        title = "Bar Chart with Negative Values - Below Axis",
                        description = "Chart showing both profit and loss - axis centered at zero"
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData("Jan", -45f),
                                    BarData("Feb", 78f),
                                    BarData("Mar", -62f),
                                    BarData("Apr", 89f),
                                    BarData("May", -55f),
                                    BarData("Jun", 35f)
                                )
                            },
                            barConfig = BarChartConfig(
                                barWidthFraction = 0.7f,
                                cornerRadius = CornerRadius.Large,
                                negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
                                animation = Animation.Enabled(duration = 1000)
                            ),
                            color = ChartyColor.Solid(Color(0xFF2196F3))
                        )
                    }
                }

                // Bar Chart with Negative Values - From Min Value Mode
                item {
                    ChartCard(
                        title = "Bar Chart with Negative Values - From Min Value",
                        description = "All bars drawn from minimum value upward for relative comparison"
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData("Jan", -45f),
                                    BarData("Feb", 78f),
                                    BarData("Mar", -62f),
                                    BarData("Apr", 89f),
                                    BarData("May", -55f),
                                    BarData("Jun", 35f)
                                )
                            },
                            barConfig = BarChartConfig(
                                barWidthFraction = 0.7f,
                                cornerRadius = CornerRadius.Large,
                                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE,
                                animation = Animation.Enabled(duration = 1000)
                            ),
                            color = ChartyColor.Solid(Color(0xFF4CAF50))
                        )
                    }
                }

                // Comparison Bar Chart (formerly Grouped)
                item {
                    ChartCard(
                        title = "Comparison Bar Chart",
                        description = "Multiple bars per category with gradient colors"
                    ) {
                        ComparisonBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup("A", listOf(17f, 25f)),
                                    BarGroup("B", listOf(15f, 16f)),
                                    BarGroup("C", listOf(44f, 48f)),
                                    BarGroup("D", listOf(30f, 44f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(Color(0xFFE91E63), Color(0xFF2196F3))
                            )
                        )
                    }
                }

                // Comparison Bar Chart with Negative Values - Below Axis
                item {
                    ChartCard(
                        title = "Comparison Bar Chart - Below Axis Mode",
                        description = "Multiple bars with profit/loss comparison - axis centered at zero"
                    ) {
                        ComparisonBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup("Q1", listOf(45f, -30f, 20f)),
                                    BarGroup("Q2", listOf(-25f, 40f, -15f)),
                                    BarGroup("Q3", listOf(60f, -45f, 35f)),
                                    BarGroup("Q4", listOf(-20f, 55f, -40f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFFE91E63),
                                    Color(0xFF2196F3)
                                )
                            ),
                            comparisonConfig = ComparisonBarChartConfig(
                                negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS
                            )
                        )
                    }
                }

                // Comparison Bar Chart with Negative Values - From Min Value
                item {
                    ChartCard(
                        title = "Comparison Bar Chart - From Min Value Mode",
                        description = "All bars drawn from minimum value for relative comparison"
                    ) {
                        ComparisonBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup("Q1", listOf(45f, -30f, 20f)),
                                    BarGroup("Q2", listOf(-25f, 40f, -15f)),
                                    BarGroup("Q3", listOf(60f, -45f, 35f)),
                                    BarGroup("Q4", listOf(-20f, 55f, -40f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFFE91E63),
                                    Color(0xFF2196F3)
                                )
                            ),
                            comparisonConfig = ComparisonBarChartConfig(
                                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE
                            )
                        )
                    }
                }

                // Point Chart
                item {
                    ChartCard(
                        title = "Point Chart",
                        description = "Scatter plot with individual points"
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData("Mon", 23f),
                                    PointData("Tue", 45f),
                                    PointData("Wed", 31f),
                                    PointData("Thu", 67f),
                                    PointData("Fri", 52f),
                                    PointData("Sat", 39f),
                                    PointData("Sun", 28f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF4CAF50)),
                            pointConfig = PointChartConfig(
                                pointRadius = 8f
                            )
                        )
                    }
                }

                // Line Chart
                item {
                    ChartCard(
                        title = "Line Chart",
                        description = "Connected line chart with points"
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData("Mon", 20f),
                                    LineData("Tue", 45f),
                                    LineData("Wed", 30f),
                                    LineData("Thu", 70f),
                                    LineData("Fri", 55f),
                                    LineData("Sat", 40f),
                                    LineData("Sun", 65f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFFFF9800)),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                strokeCap = StrokeCap.Round,
                                showPoints = true,
                                pointRadius = 6f
                            )
                        )
                    }
                }

                // Point Chart with Negative Values - Below Axis
                item {
                    ChartCard(
                        title = "Point Chart with Negative Values - Below Axis",
                        description = "Points showing both positive and negative values - axis centered at zero"
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData("Mon", -23f),
                                    PointData("Tue", 45f),
                                    PointData("Wed", -31f),
                                    PointData("Thu", 67f),
                                    PointData("Fri", -52f),
                                    PointData("Sat", 39f),
                                    PointData("Sun", -28f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF4CAF50)),
                            pointConfig = PointChartConfig(
                                pointRadius = 8f,
                                negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS
                            )
                        )
                    }
                }

                // Point Chart with Negative Values - From Min Value
                item {
                    ChartCard(
                        title = "Point Chart with Negative Values - From Min Value",
                        description = "All points drawn relative to minimum value - axis at bottom"
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData("Mon", -23f),
                                    PointData("Tue", 45f),
                                    PointData("Wed", -31f),
                                    PointData("Thu", 67f),
                                    PointData("Fri", -52f),
                                    PointData("Sat", 39f),
                                    PointData("Sun", -28f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFFE91E63)),
                            pointConfig = PointChartConfig(
                                pointRadius = 8f,
                                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE
                            )
                        )
                    }
                }

                // Line Chart with Negative Values - Below Axis
                item {
                    ChartCard(
                        title = "Line Chart with Negative Values - Below Axis",
                        description = "Line chart showing trends with positive and negative values - axis centered"
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData("Mon", -20f),
                                    LineData("Tue", 45f),
                                    LineData("Wed", -30f),
                                    LineData("Thu", 70f),
                                    LineData("Fri", -55f),
                                    LineData("Sat", 40f),
                                    LineData("Sun", 65f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFFFF9800)),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                strokeCap = StrokeCap.Round,
                                showPoints = true,
                                pointRadius = 6f,
                                negativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS
                            )
                        )
                    }
                }

                // Line Chart with Negative Values - From Min Value
                item {
                    ChartCard(
                        title = "Line Chart with Negative Values - From Min Value",
                        description = "Line chart with all values drawn from minimum - axis at bottom"
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData("Mon", -20f),
                                    LineData("Tue", 45f),
                                    LineData("Wed", -30f),
                                    LineData("Thu", 70f),
                                    LineData("Fri", -55f),
                                    LineData("Sat", 40f),
                                    LineData("Sun", 65f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF00BCD4)),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                strokeCap = StrokeCap.Round,
                                showPoints = true,
                                pointRadius = 6f,
                                negativeValuesDrawMode = NegativeValuesDrawMode.FROM_MIN_VALUE
                            )
                        )
                    }
                }

                // Bar Chart with Gradient
                item {
                    ChartCard(
                        title = "Bar Chart with Gradient",
                        description = "Bars with vertical gradient effect"
                    ) {
                        BarChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    BarData("Q1", 35f),
                                    BarData("Q2", 58f),
                                    BarData("Q3", 72f),
                                    BarData("Q4", 48f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                            )
                        )
                    }
                }

                // Mixed Point Chart with Gradient
                item {
                    ChartCard(
                        title = "Point Chart with Multi-Colors",
                        description = "Each point gets a different color from gradient"
                    ) {
                        PointChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    PointData("A", 18f),
                                    PointData("B", 42f),
                                    PointData("C", 28f),
                                    PointData("D", 56f),
                                    PointData("E", 35f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFF9800),
                                    Color(0xFF9C27B0)
                                )
                            ),
                            pointConfig = PointChartConfig(
                                pointRadius = 10f
                            )
                        )
                    }
                }

                // Line Chart without points
                item {
                    ChartCard(
                        title = "Line Chart (Lines Only)",
                        description = "Clean line chart without point markers"
                    ) {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData("00:00", 12f),
                                    LineData("04:00", 8f),
                                    LineData("08:00", 25f),
                                    LineData("12:00", 45f),
                                    LineData("16:00", 38f),
                                    LineData("20:00", 22f),
                                    LineData("23:59", 15f)
                                )
                            },
                            color = ChartyColor.Solid(Color(0xFF00BCD4)),
                            lineConfig = LineChartConfig(
                                lineWidth = 4f,
                                smoothCurve = true,
                                showPoints = false
                            )
                        )
                    }
                }


                // Stacked Bar Chart
                item {
                    ChartCard(
                        title = "Stacked Bar Chart",
                        description = "Shows composition and total - segments stacked vertically"
                    ) {
                        StackedBarChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BarGroup("Q1", listOf(20f, 30f, 15f)),
                                    BarGroup("Q2", listOf(25f, 35f, 20f)),
                                    BarGroup("Q3", listOf(30f, 25f, 25f)),
                                    BarGroup("Q4", listOf(28f, 40f, 18f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFF9800)
                                )
                            ),
                            stackedConfig = StackedBarChartConfig(
                                barWidthFraction = 0.7f,
                                topCornerRadius = CornerRadius.Medium,
                                animation = Animation.Enabled(duration = 1000)
                            )
                        )
                    }
                }

                // Area Chart
                item {
                    ChartCard(
                        title = "Area Chart",
                        description = "Line chart with filled area - emphasizes magnitude of change"
                    ) {
                        AreaChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData("Jan", 15f),
                                    LineData("Feb", 28f),
                                    LineData("Mar", 42f),
                                    LineData("Apr", 35f),
                                    LineData("May", 58f),
                                    LineData("Jun", 48f),
                                    LineData("Jul", 65f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF2196F3),
                                    Color(0xFF2196F3).copy(alpha = 0.2f)
                                )
                            ),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                showPoints = true,
                                pointRadius = 6f,
                                smoothCurve = true,
                                animation = Animation.Enabled(duration = 1200)
                            ),
                            fillAlpha = 0.4f
                        )
                    }
                }

                // Area Chart with Smooth Curve
                item {
                    ChartCard(
                        title = "Area Chart (Smooth)",
                        description = "Smooth curved area chart for elegant data visualization"
                    ) {
                        AreaChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = {
                                listOf(
                                    LineData("Mon", 12f),
                                    LineData("Tue", 25f),
                                    LineData("Wed", 18f),
                                    LineData("Thu", 38f),
                                    LineData("Fri", 30f),
                                    LineData("Sat", 45f),
                                    LineData("Sun", 35f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                showPoints = false,
                                smoothCurve = true,
                                animation = Animation.Enabled(duration = 1200)
                            ),
                            fillAlpha = 0.5f
                        )
                    }
                }

                // Multiline Chart - Smooth Curves
                item {
                    ChartCard(
                        title = "Multiline Chart (Smooth)",
                        description = "Multiple series with smooth curves starting from axis (0,0)"
                    ) {
                        MultilineChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    LineGroup("Mon", listOf(20f, 35f, 15f)),
                                    LineGroup("Tue", listOf(45f, 28f, 38f)),
                                    LineGroup("Wed", listOf(30f, 52f, 25f)),
                                    LineGroup("Thu", listOf(70f, 40f, 55f)),
                                    LineGroup("Fri", listOf(55f, 65f, 45f)),
                                    LineGroup("Sat", listOf(40f, 50f, 35f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50)
                                )
                            ),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                smoothCurve = true,
                                showPoints = true,
                                pointRadius = 6f,
                                animation = Animation.Enabled(duration = 1200)
                            )
                        )
                    }
                }

                // Multiline Chart - Straight Lines
                item {
                    ChartCard(
                        title = "Multiline Chart (Straight)",
                        description = "Multiple series with straight lines starting from axis (0,0)"
                    ) {
                        MultilineChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    LineGroup("Jan", listOf(25f, 40f)),
                                    LineGroup("Feb", listOf(35f, 30f)),
                                    LineGroup("Mar", listOf(50f, 45f)),
                                    LineGroup("Apr", listOf(45f, 60f)),
                                    LineGroup("May", listOf(60f, 50f)),
                                    LineGroup("Jun", listOf(55f, 70f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFFFF9800),
                                    Color(0xFF9C27B0)
                                )
                            ),
                            lineConfig = LineChartConfig(
                                lineWidth = 3f,
                                smoothCurve = false,
                                showPoints = true,
                                pointRadius = 7f,
                                animation = Animation.Enabled(duration = 1200)
                            )
                        )
                    }
                }

                // Stacked Area Chart - Smooth
                item {
                    ChartCard(
                        title = "Stacked Area Chart (Smooth)",
                        description = "Cumulative stacked areas with smooth curves from axis (0,0)"
                    ) {
                        StackedAreaChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    LineGroup("Mon", listOf(20f, 15f, 10f)),
                                    LineGroup("Tue", listOf(45f, 28f, 12f)),
                                    LineGroup("Wed", listOf(30f, 22f, 18f)),
                                    LineGroup("Thu", listOf(70f, 30f, 15f)),
                                    LineGroup("Fri", listOf(55f, 35f, 20f)),
                                    LineGroup("Sat", listOf(40f, 25f, 15f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFF9800)
                                )
                            ),
                            lineConfig = LineChartConfig(
                                lineWidth = 2f,
                                smoothCurve = true,
                                animation = Animation.Enabled(duration = 1200)
                            ),
                            fillAlpha = 0.7f
                        )
                    }
                }

                // Stacked Area Chart - Straight
                item {
                    ChartCard(
                        title = "Stacked Area Chart (Straight)",
                        description = "Cumulative stacked areas with straight lines from axis (0,0)"
                    ) {
                        StackedAreaChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    LineGroup("Q1", listOf(30f, 25f, 20f)),
                                    LineGroup("Q2", listOf(40f, 35f, 25f)),
                                    LineGroup("Q3", listOf(50f, 30f, 30f)),
                                    LineGroup("Q4", listOf(45f, 40f, 28f))
                                )
                            },
                            colors = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0),
                                    Color(0xFF00BCD4)
                                )
                            ),
                            lineConfig = LineChartConfig(
                                lineWidth = 2f,
                                smoothCurve = false,
                                animation = Animation.Enabled(duration = 1200)
                            ),
                            fillAlpha = 0.8f
                        )
                    }
                }

                // Bubble Chart
                item {
                    ChartCard(
                        title = "Bubble Chart",
                        description = "Visualizes 3 dimensions: position, value, and size"
                    ) {
                        BubbleChart(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            data = {
                                listOf(
                                    BubbleData("Product A", yValue = 45f, size = 150f),
                                    BubbleData("Product B", yValue = 72f, size = 280f),
                                    BubbleData("Product C", yValue = 38f, size = 100f),
                                    BubbleData("Product D", yValue = 85f, size = 220f),
                                    BubbleData("Product E", yValue = 55f, size = 180f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF2196F3),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFF9800),
                                    Color(0xFF9C27B0)
                                )
                            ),
                            pointConfig = PointChartConfig(
                                pointRadius = 40f,
                                animation = Animation.Enabled(duration = 1000)
                            ),
                            minBubbleRadius = 15f
                        )
                    }
                }

                // Pie Chart
                item {
                    ChartCard(
                        title = "Interactive Pie Chart",
                        description = "Classic pie chart with click interactions, animations, and legend"
                    ) {
                        var clickedSlice by remember { mutableStateOf<String?>(null) }

                        Column {
                            if (clickedSlice != null) {
                                Text(
                                    text = "Selected: $clickedSlice",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            PieChart(
                                modifier = Modifier.fillMaxWidth().height(400.dp),
                                data = {
                                    listOf(
                                        PieData("Product A", 45f),
                                        PieData("Product B", 30f),
                                        PieData("Product C", 15f),
                                        PieData("Product D", 10f)
                                    )
                                },
                                color = ChartyColor.Gradient(
                                    listOf(
                                        Color(0xFF2196F3),
                                        Color(0xFF4CAF50),
                                        Color(0xFFFF9800),
                                        Color(0xFFE91E63)
                                    )
                                ),
                                config = PieChartConfig(
                                    style = PieChartStyle.PIE,
                                    labelConfig = LabelConfig(
                                        shouldShowLabels = true,
                                        shouldShowPercentage = true,
                                        minimumPercentageToShowLabel = 5f
                                    ),
                                    interactionConfig = InteractionConfig(
                                        selectedScaleMultiplier = 1.15f,
                                        selectedSlicePullOutDistance = 12f
                                    ),
                                    animation = Animation.Enabled(duration = 1000)
                                ),
                                onSliceClick = { slice, _ ->
                                    clickedSlice = "${slice.label}: ${slice.value}"
                                }
                            )
                        }
                    }
                }

                // Donut Chart
                item {
                    ChartCard(
                        title = "Donut Chart with Center Content",
                        description = "Modern donut chart with center hole, custom colors, and right-side legend"
                    ) {
                        var selectedCategory by remember { mutableStateOf<String?>(null) }

                        PieChart(
                            modifier = Modifier.fillMaxWidth().height(400.dp),
                            data = {
                                listOf(
                                    PieData("Sales", 120f),
                                    PieData("Marketing", 85f),
                                    PieData("Development", 95f),
                                    PieData("Support", 60f),
                                    PieData("Operations", 40f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF00BCD4),
                                    Color(0xFF9C27B0),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFF9800),
                                    Color(0xFFE91E63)
                                )
                            ),
                            config = PieChartConfig(
                                style = PieChartStyle.DONUT,
                                donutHoleRatio = 0.65f,
                                startAngleDegrees = -90f,
                                labelConfig = LabelConfig(
                                    shouldShowLabels = false
                                ),
                                interactionConfig = InteractionConfig(
                                    selectedScaleMultiplier = 1.1f,
                                    selectedSlicePullOutDistance = 10f,
                                    unselectedSliceOpacity = 0.5f
                                ),
                                animation = Animation.Enabled(duration = 1200),
                                sliceSpacingDegrees = 2f
                            ),
                            onSliceClick = { slice, _ ->
                                selectedCategory = slice.label
                            },
                            centerContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = selectedCategory ?: "Total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (selectedCategory != null) "Selected" else "400",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        )
                    }
                }

                // Donut Chart with Top Legend
                item {
                    ChartCard(
                        title = "Compact Donut Chart",
                        description = "Donut chart with top legend and slice spacing"
                    ) {
                        PieChart(
                            modifier = Modifier.fillMaxWidth().height(450.dp),
                            data = {
                                listOf(
                                    PieData("Electronics", 180f),
                                    PieData("Clothing", 150f),
                                    PieData("Food", 120f),
                                    PieData("Books", 80f),
                                    PieData("Sports", 70f),
                                    PieData("Others", 50f)
                                )
                            },
                            color = ChartyColor.Gradient(
                                listOf(
                                    Color(0xFF3F51B5),
                                    Color(0xFF2196F3),
                                    Color(0xFF00BCD4),
                                    Color(0xFF4CAF50),
                                    Color(0xFFFFEB3B),
                                    Color(0xFFFF5722)
                                )
                            ),
                            config = PieChartConfig(
                                style = PieChartStyle.DONUT,
                                donutHoleRatio = 0.5f,
                                labelConfig = LabelConfig(
                                    shouldShowLabels = true,
                                    shouldShowPercentage = true,
                                    minimumPercentageToShowLabel = 8f
                                ),
                                interactionConfig = InteractionConfig(
                                    selectedScaleMultiplier = 1.08f,
                                    selectedSlicePullOutDistance = 8f
                                ),
                                animation = Animation.Enabled(duration = 800),
                                sliceSpacingDegrees = 3f,
                                shouldShowCenterText = true,
                                centerTextSizeSp = 20f
                            ),
                            onSliceClick = { slice, index ->
                                println("Clicked ${slice.label} at index $index")
                            }
                        )
                    }
                }

                // Footer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ChartContext API",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All charts built using the same ChartContext helpers:\n" +
                                        "• valueToY() - Value to pixel conversion\n" +
                                        "• getBarX() - Bar positioning\n" +
                                        "• getGroupCenterX() - Centered positioning",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
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
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}