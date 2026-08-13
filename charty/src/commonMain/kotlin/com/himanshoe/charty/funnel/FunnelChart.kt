package com.himanshoe.charty.funnel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.accessibility.ChartDataPointSemantics
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.funnel.config.FunnelChartConfig
import com.himanshoe.charty.funnel.config.FunnelOrientation
import com.himanshoe.charty.funnel.data.FunnelStage
import com.himanshoe.charty.funnel.internal.FunnelDrawParams
import com.himanshoe.charty.funnel.internal.drawFunnel
import com.himanshoe.charty.funnel.internal.funnelStageDescriptions
import com.himanshoe.charty.funnel.internal.funnelStageFractions
import com.himanshoe.charty.funnel.internal.generateFunnelChartDescription
import com.himanshoe.charty.funnel.internal.rememberFunnelBrushes
import com.himanshoe.charty.funnel.internal.rememberFunnelLabels

/**
 * A funnel chart: stacked trapezoids that taper from the first stage to the last, so the drop-off
 * between steps is the shape of the chart rather than something the reader has to compute. Sign-up
 * flows, sales pipelines, and any stepwise process where each stage keeps a subset of the one before.
 *
 * **Widths are relative to the first stage**, which is drawn at full width; every later stage is
 * drawn at `value / firstValue` of it, and each band tapers from its own width to the next stage's.
 * A stage larger than the first is clamped to full width rather than rescaling the chart, and a
 * first stage of zero draws an empty funnel instead of dividing by zero.
 *
 * The funnel runs downwards by default — the shape most readers expect — and can be laid out left to
 * right with [FunnelChartConfig.orientation].
 *
 * Nothing else in the library draws a taper: [com.himanshoe.charty.block.BlockBarChart] splits one
 * constant-thickness bar into proportional segments, and a pie divides angle rather than width.
 *
 * Example:
 * ```kotlin
 * FunnelChart(
 *     data = {
 *         listOf(
 *             FunnelStage(label = "Visited", value = 5200f),
 *             FunnelStage(label = "Signed up", value = 2400f),
 *             FunnelStage(label = "Purchased", value = 640f),
 *         )
 *     },
 *     funnelConfig = FunnelChartConfig(showConversionLabels = true),
 * )
 * ```
 *
 * @param data Lambda returning the stages, widest first.
 * @param modifier The modifier applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when `null`
 *   (default) a built-in "No data" state is used.
 * @param stageColors Colours cycled across the stages; a stage's own colour wins over these, and an
 *   empty list (default) takes the ambient theme's series palette.
 * @param funnelConfig Appearance and behaviour: orientation, stage spacing, animation, and labels.
 * @param onStageClick Called when a stage band is tapped, with the stage it represents.
 * @param accessibilityDescription Overrides the auto-generated screen-reader description. Pass an
 *   empty string to suppress it.
 */
@Composable
fun FunnelChart(
    data: () -> List<FunnelStage>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    stageColors: List<ChartyColor> = emptyList(),
    funnelConfig: FunnelChartConfig = FunnelChartConfig(),
    onStageClick: ((FunnelStage) -> Unit)? = null,
    accessibilityDescription: String? = null,
) {
    val stages by remember(data) { derivedStateOf { data() } }
    if (stages.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val fractions = remember(stages) { funnelStageFractions(stages = stages) }
    val brushes = rememberFunnelBrushes(stages = stages, stageColors = stageColors)
    val textMeasurer = rememberTextMeasurer()
    val labels = rememberFunnelLabels(stages = stages, config = funnelConfig, textMeasurer = textMeasurer)
    val path = remember { Path() }
    val animationProgress = rememberChartAnimation(funnelConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, FunnelStage>(dataKey = stages)
    val description =
        rememberChartDescription(stages, accessibilityDescription) { series ->
            generateFunnelChartDescription(stages = series, config = funnelConfig)
        }
    val stageDescriptions =
        remember(stages, funnelConfig) { funnelStageDescriptions(stages = stages, config = funnelConfig) }

    val semanticsModifier =
        if (description != null) {
            Modifier.semantics { contentDescription = description }
        } else {
            Modifier
        }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = stages,
            bounds = tooltipManager.bounds,
            onItemClick = onStageClick,
            onTooltipStateChange = tooltipManager::updateTooltip,
            createTooltipContent = { stage: FunnelStage, rect: Rect ->
                TooltipState(
                    content = "${stage.label}: ${funnelConfig.valueFormatter(stage.value)}",
                    x = rect.left,
                    y = rect.top,
                    barWidth = rect.width,
                )
            },
        )

    Box(modifier = clickModifier.then(semanticsModifier)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            tooltipManager.clearBounds()
            drawFunnel(
                FunnelDrawParams(
                    stages = stages,
                    fractions = fractions,
                    brushes = brushes,
                    config = funnelConfig,
                    animationProgress = animationProgress.value,
                    labels = labels,
                    path = path,
                    onStageBoundCalculated = { bounds -> tooltipManager.bounds.add(bounds) },
                    recordBounds = onStageClick != null,
                ),
            )
        }

        ChartDataPointSemantics(
            descriptions = stageDescriptions,
            orientation =
                if (funnelConfig.orientation == FunnelOrientation.VERTICAL) {
                    ChartOrientation.HORIZONTAL
                } else {
                    ChartOrientation.VERTICAL
                },
            modifier = Modifier.matchParentSize(),
        )
    }
}
