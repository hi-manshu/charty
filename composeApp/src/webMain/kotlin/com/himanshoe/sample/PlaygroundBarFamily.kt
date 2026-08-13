@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "ktlint:standard:max-line-length",
    "ktlint:standard:function-naming",
)

package com.himanshoe.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.charty.bar.BubbleBarChart
import com.himanshoe.charty.bar.ComparisonBarChart
import com.himanshoe.charty.bar.LollipopBarChart
import com.himanshoe.charty.bar.MosaicBarChart
import com.himanshoe.charty.bar.NormalizedHorizontalBarChart
import com.himanshoe.charty.bar.SpanChart
import com.himanshoe.charty.bar.StackedHorizontalBarChart
import com.himanshoe.charty.bar.WaterfallChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import kotlin.random.Random

/** The bar-family charts that share this one screen, each with its own shape of data. */
internal enum class BarFamilyVariant(
    val label: String,
    val blurb: String,
) {
    Waterfall(
        label = "Waterfall",
        blurb = "Running total built from signed steps. Each bar floats between the totals either side of it.",
    ),
    Lollipop(
        label = "Lollipop",
        blurb = "A bar reduced to a stem and a head — less ink for the same reading.",
    ),
    BubbleBar(
        label = "Bubble bar",
        blurb = "A bar drawn as a stack of bubbles; the count carries the value.",
    ),
    Span(
        label = "Span",
        blurb = "One start-to-end range per row: confidence intervals, highs and lows, durations.",
    ),
    Comparison(
        label = "Comparison",
        blurb = "Two or more bars per category, side by side against one baseline.",
    ),
    Mosaic(
        label = "Mosaic",
        blurb = "Column width carries the group total while its segments carry the split.",
    ),
    StackedHorizontal(
        label = "Stacked horizontal",
        blurb = "Segments accumulating rightwards, one row per group.",
    ),
    NormalizedHorizontal(
        label = "Normalized horizontal",
        blurb = "The same rows stretched to equal length, so the split is comparable across groups.",
    ),
}

private fun familyBars(tick: Int): List<BarData> {
    val random = Random(seed = tick * 13 + 3)
    return listOf("Q1", "Q2", "Q3", "Q4", "Q5").map { label ->
        BarData(label = label, value = 20f + random.nextFloat() * 70f)
    }
}

private fun familySteps(tick: Int): List<BarData> {
    val random = Random(seed = tick * 17 + 9)
    return listOf("Open", "Sales", "Refunds", "Costs", "Close").mapIndexed { index, label ->
        val magnitude = 15f + random.nextFloat() * 35f
        BarData(label = label, value = if (index % 2 == 0) magnitude else -magnitude)
    }
}

private fun familyGroups(tick: Int): List<BarGroup> {
    val random = Random(seed = tick * 29 + 5)
    return listOf("North", "South", "East", "West").map { label ->
        BarGroup(
            label = label,
            values = List(3) { 10f + random.nextFloat() * 40f },
        )
    }
}

private fun familySpans(tick: Int): List<SpanData> {
    val random = Random(seed = tick * 37 + 11)
    return listOf("Mon", "Tue", "Wed", "Thu", "Fri").map { label ->
        val start = 5f + random.nextFloat() * 10f
        SpanData(label = label, startValue = start, endValue = start + 6f + random.nextFloat() * 14f)
    }
}

/**
 * One screen for the bar-family charts that each need too little configuration to earn a card of
 * their own. Pick a variant and its own controls appear.
 */
@Composable
internal fun BarFamilyPlayground() {
    var variant by remember { mutableStateOf(BarFamilyVariant.Waterfall) }
    var tick by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf<String?>(null) }
    var widthFraction by remember { mutableStateOf(60) }

    val bars = remember(tick) { familyBars(tick) }
    val steps = remember(tick) { familySteps(tick) }
    val groups = remember(tick) { familyGroups(tick) }
    val spans = remember(tick) { familySpans(tick) }
    val animation = if (LocalPlaygroundAnimate.current) Animation.Default else Animation.Disabled
    val accent = ChartyColor.Solid(playgroundPalette[3])

    val code =
        """
        // ${variant.blurb}
        ${variant.label.replace(" ", "")}Chart(
            data = { data },
            modifier = Modifier.fillMaxWidth().height(300.dp),
            onClick = { item -> /* ${clicked ?: "tap the chart"} */ },
        )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            val chartModifier = Modifier.fillMaxSize()
            when (variant) {
                BarFamilyVariant.Waterfall ->
                    WaterfallChart(
                        data = { steps },
                        modifier = chartModifier,
                        config = WaterfallChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onBarClick = { clicked = "${it.label} ${it.value.toInt()}" },
                    )

                BarFamilyVariant.Lollipop ->
                    LollipopBarChart(
                        data = { bars },
                        modifier = chartModifier,
                        colors = accent,
                        config = LollipopBarChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onBarClick = { clicked = "${it.label} ${it.value.toInt()}" },
                    )

                BarFamilyVariant.BubbleBar ->
                    BubbleBarChart(
                        data = { bars },
                        modifier = chartModifier,
                        color = accent,
                        bubbleConfig = BubbleBarChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onBarClick = { clicked = "${it.label} ${it.value.toInt()}" },
                    )

                BarFamilyVariant.Span ->
                    SpanChart(
                        data = { spans },
                        modifier = chartModifier,
                        colors = accent,
                        barConfig =
                            BarChartConfig(
                                animation = animation,
                                barWidthFraction = widthFraction / 100f,
                            ),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onSpanClick = { clicked = "${it.label} ${it.startValue.toInt()}–${it.endValue.toInt()}" },
                    )

                BarFamilyVariant.Comparison ->
                    ComparisonBarChart(
                        data = { groups },
                        modifier = chartModifier,
                        comparisonConfig = ComparisonBarChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onBarClick = { clicked = "${it.barGroup.label}[${it.barIndex}]" },
                    )

                BarFamilyVariant.Mosaic ->
                    MosaicBarChart(
                        data = { groups },
                        modifier = chartModifier,
                        config = MosaicBarChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onSegmentClick = { clicked = "${it.barGroup.label}[${it.segmentIndex}]" },
                    )

                BarFamilyVariant.StackedHorizontal ->
                    StackedHorizontalBarChart(
                        data = { groups },
                        modifier = chartModifier,
                        config = StackedHorizontalBarChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onSegmentClick = { clicked = "${it.barGroup.label}[${it.segmentIndex}]" },
                    )

                BarFamilyVariant.NormalizedHorizontal ->
                    NormalizedHorizontalBarChart(
                        data = { groups },
                        modifier = chartModifier,
                        config = NormalizedHorizontalBarChartConfig(animation = animation),
                        scaffoldConfig = playgroundScaffoldConfig(),
                        onSegmentClick = { clicked = "${it.barGroup.label}[${it.segmentIndex}]" },
                    )
            }
        },
        controls = {
            ControlSection(title = "Variant")
            ChoiceRow(
                label = "Chart",
                options = BarFamilyVariant.entries.toList(),
                selected = variant,
                labelOf = { it.label },
                onSelect = {
                    variant = it
                    clicked = null
                },
            )
            Text(
                text = variant.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (variant == BarFamilyVariant.Span) {
                ControlSection(title = "Spans")
                IntSliderRow(
                    label = "Thickness (%)",
                    value = widthFraction,
                    valueRange = 20..95,
                    onValueChange = { widthFraction = it },
                )
            }
            ControlSection(title = "Data")
            PlaygroundActionRow(primaryLabel = "Shuffle data", onPrimary = { tick++ })
            ControlSection(title = "Last click")
            Text(
                text = clicked ?: "Tap the chart",
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}
