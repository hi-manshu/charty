package com.himanshoe.charty.common.gesture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.theme.currentChartyTheme

private const val CROSSHAIR_LABEL_TEXT_SP = 12

/**
 * The draggable crosshair for a chart: a guide line (drawn on the chart canvas) that snaps to the
 * nearest point as you drag, plus a label drawn over the line. Pass one to a chart's `crosshair`
 * parameter; `null` turns it off.
 *
 * ```kotlin
 * crosshair = ChartCrosshair()                        // line + built-in label
 * crosshair = ChartCrosshair(config = myLineStyle)    // styled line + built-in label
 * crosshair = ChartCrosshair(label = { point -> … })  // line + your own label, over the line
 * ```
 *
 * @property config Appearance of the guide line, dot, and dismiss behavior.
 * @property label Optional custom label composable, rendered over the line at the snapped point; when
 *   `null` a built-in pill label is shown. The dragged point is available via [CrosshairScope].
 */
@Immutable
data class ChartCrosshair<T>(
    val config: ChartCrosshairConfig = ChartCrosshairConfig(),
    val label: (@Composable CrosshairScope<T>.() -> Unit)? = null,
)

/**
 * The context a [ChartCrosshair] label renders in: the snapped [data] point and the chart's formatted
 * [text] for it.
 *
 * @property data The point currently under the crosshair.
 * @property text The chart's formatted label for [data].
 */
@Stable
class CrosshairScope<T> internal constructor(
    val data: T,
    val text: String,
)

/**
 * Renders a [ChartCrosshair]'s label over the guide line. Place it as a sibling over the chart
 * canvas; it draws nothing when there is no active crosshair selection.
 *
 * @param crosshair The crosshair configuration.
 * @param item The snapped data point, or `null` when the crosshair is inactive.
 * @param state The crosshair position/label, or `null` when inactive.
 * @param modifier Modifier for the overlay container (typically `Modifier.matchParentSize()`).
 */
@Composable
fun <T> ChartCrosshairHost(
    crosshair: ChartCrosshair<T>,
    item: T?,
    state: CrosshairState?,
    modifier: Modifier = Modifier,
) {
    if (item == null || state == null) {
        return
    }
    ChartCrosshairOverlay(
        item = item,
        state = state,
        config = crosshair.config.tooltipConfig,
        modifier = modifier,
    ) { data ->
        RenderCrosshairLabel(
            scope = CrosshairScope(data = data, text = state.label),
            label = crosshair.label,
        )
    }
}

@Composable
private fun <T> RenderCrosshairLabel(
    scope: CrosshairScope<T>,
    label: (@Composable CrosshairScope<T>.() -> Unit)?,
) {
    if (label != null) {
        scope.label()
    } else {
        DefaultCrosshairLabel(text = scope.text)
    }
}

@Composable
private fun DefaultCrosshairLabel(text: String) {
    Box(
        modifier =
            Modifier
                .background(brush = SolidColor(currentChartyTheme.primaryColor), shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        BasicText(
            text = text,
            style =
                TextStyle(
                    color = currentChartyTheme.labelTextStyle.color,
                    fontSize = CROSSHAIR_LABEL_TEXT_SP.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
        )
    }
}
