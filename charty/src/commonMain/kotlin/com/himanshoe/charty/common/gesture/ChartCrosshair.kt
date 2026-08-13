package com.himanshoe.charty.common.gesture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.toBrush
import com.himanshoe.charty.common.theme.currentChartyTheme
import com.himanshoe.charty.common.theme.orThemeCrosshair

private val LABEL_HORIZONTAL_PADDING = 8.dp
private val LABEL_VERTICAL_PADDING = 4.dp

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
 * @property config Appearance of the guide line, dot, and dismiss behavior. `null` — the default —
 *   takes the guide line, dot, and value bubble from the ambient `ChartyTheme`.
 * @property label Optional custom label composable, rendered over the line at the snapped point; when
 *   `null` a built-in pill label is shown. The dragged point is available via [CrosshairScope].
 */
@Immutable
data class ChartCrosshair<T>(
    val config: ChartCrosshairConfig? = null,
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
        config = crosshair.config.orThemeCrosshair().tooltipConfig,
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
    val theme = currentChartyTheme
    Box(
        modifier =
            Modifier
                .background(brush = theme.crosshairLabelBackground.toBrush(), shape = theme.shapes.crosshairLabel)
                .padding(horizontal = LABEL_HORIZONTAL_PADDING, vertical = LABEL_VERTICAL_PADDING),
    ) {
        BasicText(text = text, style = theme.crosshairLabelStyle)
    }
}
