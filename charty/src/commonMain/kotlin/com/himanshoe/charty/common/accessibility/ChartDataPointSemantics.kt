package com.himanshoe.charty.common.accessibility

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.ChartOrientation

/**
 * Builds one screen-reader description per data point, e.g. `"Point 2 of 5: Feb, 45"`, so assistive
 * tech can announce a point's position and value as the user traverses the chart.
 *
 * @param labels The per-point labels (x-axis categories).
 * @param values The per-point values, parallel to [labels].
 * @return A description for each point; empty when [labels] is empty.
 */
fun buildDataPointDescriptions(
    labels: List<String>,
    values: List<Float>,
): List<String> =
    labels.indices.map { index ->
        val value = values.getOrNull(index)
        val valueText =
            if (value == null) {
                ""
            } else {
                ", ${value.toReadableString()}"
            }
        "Point ${index + 1} of ${labels.size}: ${labels[index]}$valueText"
    }

/**
 * Overlays one invisible, focusable semantics node per data point so screen readers (TalkBack,
 * VoiceOver) can traverse a canvas-drawn chart point by point instead of hearing a single summary.
 *
 * The nodes are transparent and carry no pointer input, so they never change the chart's appearance
 * or block its touch interactions; they exist purely for the accessibility tree. Nodes are laid out
 * left-to-right for [ChartOrientation.VERTICAL] charts and top-to-bottom for
 * [ChartOrientation.HORIZONTAL] ones, matching the visual order of the data.
 *
 * @param descriptions One description per point (see [buildDataPointDescriptions]); nothing is drawn
 *   when empty.
 * @param orientation The chart's orientation, which decides the traversal axis.
 * @param modifier Modifier for the overlay; typically `Modifier.matchParentSize()`.
 */
@Composable
fun ChartDataPointSemantics(
    descriptions: List<String>,
    orientation: ChartOrientation,
    modifier: Modifier = Modifier,
) {
    if (descriptions.isEmpty()) {
        return
    }
    if (orientation == ChartOrientation.HORIZONTAL) {
        Column(modifier = modifier) {
            descriptions.fastForEach { description ->
                Box(modifier = Modifier.weight(1f).fillMaxWidth().semantics { contentDescription = description })
            }
        }
    } else {
        Row(modifier = modifier) {
            descriptions.fastForEach { description ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight().semantics { contentDescription = description })
            }
        }
    }
}
