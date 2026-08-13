package com.himanshoe.charty.funnel.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.util.toChartLabel

private const val DEFAULT_STAGE_GAP = 4f
private const val DEFAULT_LABEL_SP = 12
private const val DEFAULT_CONVERSION_SP = 10
private const val PERCENT_SCALE = 100f

/**
 * The direction a funnel narrows in.
 */
enum class FunnelOrientation {
    /**
     * Stages are stacked downwards and taper in width, the shape most readers expect of a funnel.
     * This is the default.
     */
    VERTICAL,

    /**
     * Stages run left to right and taper in height. Useful when the chart sits in a wide, short
     * space, or beside a vertical list of stage names.
     */
    HORIZONTAL,
}

/**
 * Appearance and behaviour of a [com.himanshoe.charty.funnel.FunnelChart].
 *
 * @property orientation Whether the funnel runs downwards or to the right; see [FunnelOrientation].
 * @property stageGap Pixels of empty space between consecutive stage bands. Must be non-negative.
 * @property animation The entry animation; the bands widen from the funnel's centre line.
 * @property showStageLabels Whether each band states its stage name and value.
 * @property showConversionLabels Whether each band after the first states what fraction of the
 *   previous stage reached it.
 * @property valueFormatter Formats a stage value for its label.
 * @property conversionFormatter Formats a stage-to-stage conversion fraction (`0f`–`1f`).
 * @property labelStyle Text style for the stage labels, drawn over the band.
 * @property conversionLabelStyle Text style for the conversion labels.
 */
@Stable
data class FunnelChartConfig(
    val orientation: FunnelOrientation = FunnelOrientation.VERTICAL,
    val stageGap: Float = DEFAULT_STAGE_GAP,
    val animation: Animation = Animation.Default,
    val showStageLabels: Boolean = true,
    val showConversionLabels: Boolean = false,
    val valueFormatter: (Float) -> String = { value -> value.toChartLabel() },
    val conversionFormatter: (Float) -> String = { fraction -> "${(fraction * PERCENT_SCALE).toChartLabel()}%" },
    val labelStyle: TextStyle =
        TextStyle(
            fontSize = DEFAULT_LABEL_SP.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        ),
    val conversionLabelStyle: TextStyle =
        TextStyle(
            fontSize = DEFAULT_CONVERSION_SP.sp,
            color = Color.White,
        ),
) {
    init {
        require(stageGap >= 0f) { "stageGap must be non-negative" }
    }
}
