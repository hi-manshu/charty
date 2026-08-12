package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint

/**
 * Everything [drawMultilineContent] needs for one canvas pass, bundled so the drawing entry point
 * keeps a single parameter.
 *
 * @property dataList The groups being drawn, one x position per group and one value per series.
 * @property chartContext The pixel bounds and value range of the plotting area.
 * @property colorList One colour per series, indexed modulo its size.
 * @property lineConfig The line styling, reference band, tooltip, and crosshair configuration.
 * @property animationProgress The reveal progress, from `0f` (nothing drawn) to `1f` (fully drawn).
 * @property pointBounds Collects every drawn point paired with its position, for tap hit-testing.
 * @property crosshairBounds Collects the first series' points, which the crosshair snaps to.
 * @property onPointClick Invoked when a point is tapped; `null` skips filling [pointBounds].
 * @property crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @property crosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @property tooltipState The active tooltip, or `null` when none is showing.
 * @property textMeasurer Measurer used for the reference band, tooltip, and crosshair labels.
 * @property interactionConfig Supplies the annotation, brush-selection, and edge-fade overlays.
 * @property drawCrosshairLabel Whether the crosshair label is drawn on the canvas rather than as a
 *   composable overlay above it.
 */
internal data class MultilineDrawParams(
    val dataList: List<LineGroup>,
    val chartContext: ChartContext,
    val colorList: List<Color>,
    val lineConfig: LineChartConfig,
    val animationProgress: Float,
    val pointBounds: MutableList<Pair<Offset, MultilinePoint>>,
    val crosshairBounds: MutableList<Pair<Offset, MultilinePoint>>,
    val onPointClick: ((MultilinePoint) -> Unit)?,
    val crosshairManager: CrosshairManager<MultilinePoint>?,
    val crosshairState: CrosshairState?,
    val tooltipState: TooltipState?,
    val textMeasurer: TextMeasurer,
    val interactionConfig: ChartInteractionConfig,
    val drawCrosshairLabel: Boolean,
)
