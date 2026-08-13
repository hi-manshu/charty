package com.himanshoe.charty.funnel.internal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import com.himanshoe.charty.funnel.config.FunnelChartConfig
import com.himanshoe.charty.funnel.data.FunnelStage

/**
 * Everything [drawFunnel] needs for one frame.
 *
 * @property stages The stages in funnel order.
 * @property fractions Each stage's width as a fraction of the first stage.
 * @property brushes One prepared brush per stage, in stage order, built in composition so the
 *   draw path allocates no paint.
 * @property config The chart's appearance configuration.
 * @property animationProgress The entry animation's progress from `0f` to `1f`.
 * @property labels The pre-measured stage and conversion labels.
 * @property path A reusable path the drawer resets per stage, so the taper costs no allocation per
 *   frame.
 * @property onStageBoundCalculated Receives each band's bounds paired with its stage, for hit testing.
 * @property recordBounds Whether bounds are worth recording at all, i.e. whether anything consumes them.
 */
internal data class FunnelDrawParams(
    val stages: List<FunnelStage>,
    val fractions: List<Float>,
    val brushes: List<Brush>,
    val config: FunnelChartConfig,
    val animationProgress: Float,
    val labels: FunnelLabels,
    val path: Path,
    val onStageBoundCalculated: (Pair<Rect, FunnelStage>) -> Unit,
    val recordBounds: Boolean,
)
