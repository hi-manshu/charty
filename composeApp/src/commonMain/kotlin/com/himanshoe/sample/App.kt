package com.himanshoe.sample

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.himanshoe.charty.getPlatformName
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.BarData
import com.himanshoe.charty.bar.ComparisonBarChart
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.StackedBarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.PointData
import com.himanshoe.charty.point.BubbleChart
import com.himanshoe.charty.point.BubbleData
import com.himanshoe.charty.point.config.PointChartConfig
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.LineData
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius

@Composable
@Preview
fun App() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎨 Charty Library",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Platform: ${getPlatformName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Horizontal Bar Chart
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