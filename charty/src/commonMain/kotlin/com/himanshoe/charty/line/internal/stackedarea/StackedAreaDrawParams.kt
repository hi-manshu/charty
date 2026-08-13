package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint

/**
 * Everything [drawStackedAreaContent] needs for one canvas pass, bundled so the drawing entry point
 * keeps a single parameter.
 *
 * @property dataList The groups being drawn, one x position per group and one value per series.
 * @property chartContext The pixel bounds and value range of the plotting area.
 * @property colorList One colour per series, indexed modulo its size.
 * @property lineConfig The line styling, reference band, tooltip, and crosshair configuration.
 * @property fillAlpha The opacity applied to every area fill, from `0f` to `1f`.
 * @property animationProgress The reveal progress, from `0f` (nothing drawn) to `1f` (fully drawn).
 * @property recordSegmentBounds Whether the pass collects [areaSegmentBounds]; `false` skips the
 *   bookkeeping when nothing hit-tests them.
 * @property areaSegmentBounds Collects each drawn segment's rect and data, for hit-testing.
 * @property crosshairBounds Collects the topmost stack points the crosshair snaps to, or `null`
 *   when the crosshair is off.
 * @property crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @property crosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @property tooltipState The active tooltip, or `null` when none is showing.
 * @property drawTooltipBubble Whether the active tooltip is drawn as the built-in canvas bubble; a
 *   Compose-overlay tooltip is hosted above the canvas instead.
 * @property textMeasurer Measurer used for the reference band, tooltip, and crosshair labels.
 * @property interactionConfig Supplies the annotation, brush-selection, and edge-fade overlays.
 * @property drawCrosshairLabel Whether the crosshair label is drawn on the canvas rather than as a
 *   composable overlay above it.
 */
internal data class StackedAreaDrawParams(
    val dataList: List<LineGroup>,
    val chartContext: ChartContext,
    val colorList: List<Color>,
    val lineConfig: LineChartConfig,
    val fillAlpha: Float,
    val animationProgress: Float,
    val recordSegmentBounds: Boolean,
    val areaSegmentBounds: MutableList<Pair<Rect, StackedAreaPoint>>,
    val crosshairBounds: MutableList<Pair<Offset, LineGroup>>?,
    val crosshairManager: CrosshairManager<LineGroup>?,
    val crosshairState: CrosshairState?,
    val tooltipState: TooltipState?,
    val drawTooltipBubble: Boolean,
    val textMeasurer: TextMeasurer,
    val interactionConfig: ChartInteractionConfig,
    val drawCrosshairLabel: Boolean,
)
