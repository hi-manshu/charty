package com.himanshoe.charty.common.annotation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor

private const val DEFAULT_CALLOUT_CORNER_RADIUS = 6f
private const val DEFAULT_CALLOUT_CONNECTOR_WIDTH = 1.5f
private const val DEFAULT_CALLOUT_PADDING_HORIZONTAL = 8f
private const val DEFAULT_CALLOUT_PADDING_VERTICAL = 5f
private const val DEFAULT_CALLOUT_POINTER_LENGTH = 14f
private const val DEFAULT_CALLOUT_FONT_SIZE = 11
private val DEFAULT_CALLOUT_COLOR = ChartyColor.Solid(Color(0xFF212121))
private val DEFAULT_CALLOUT_TEXT_STYLE = TextStyle(color = Color.White, fontSize = DEFAULT_CALLOUT_FONT_SIZE.sp)

/**
 * Which side of its anchor a [ChartAnnotation.Callout] bubble prefers to sit on.
 */
enum class CalloutSide {
    /** Place the bubble above the anchor point. */
    ABOVE,

    /** Place the bubble below the anchor point. */
    BELOW,

    /** Place the bubble above the anchor when it fits there, otherwise below. */
    AUTO,
}

/**
 * Visual style for a [ChartAnnotation.Callout] bubble and its connector.
 *
 * @property bubbleColor Fill of the rounded bubble.
 * @property textStyle Style of the text drawn inside the bubble.
 * @property cornerRadius Corner radius of the bubble in pixels.
 * @property connectorWidth Stroke width of the connector line in pixels.
 * @property connectorColor Color of the connector line joining the bubble to the anchor.
 * @property paddingHorizontal Horizontal padding between the text and the bubble edge, in pixels.
 * @property paddingVertical Vertical padding between the text and the bubble edge, in pixels.
 * @property pointerLengthPx Gap in pixels between the anchor point and the nearest bubble edge.
 * @property preferredSide Which side of the anchor the bubble prefers to occupy.
 */
@Stable
data class CalloutStyle(
    val bubbleColor: ChartyColor = DEFAULT_CALLOUT_COLOR,
    val textStyle: TextStyle = DEFAULT_CALLOUT_TEXT_STYLE,
    val cornerRadius: Float = DEFAULT_CALLOUT_CORNER_RADIUS,
    val connectorWidth: Float = DEFAULT_CALLOUT_CONNECTOR_WIDTH,
    val connectorColor: ChartyColor = DEFAULT_CALLOUT_COLOR,
    val paddingHorizontal: Float = DEFAULT_CALLOUT_PADDING_HORIZONTAL,
    val paddingVertical: Float = DEFAULT_CALLOUT_PADDING_VERTICAL,
    val pointerLengthPx: Float = DEFAULT_CALLOUT_POINTER_LENGTH,
    val preferredSide: CalloutSide = CalloutSide.AUTO,
) {
    init {
        require(cornerRadius >= 0f) { "cornerRadius must be >= 0, was $cornerRadius" }
        require(connectorWidth > 0f) { "connectorWidth must be > 0, was $connectorWidth" }
        require(paddingHorizontal >= 0f) { "paddingHorizontal must be >= 0, was $paddingHorizontal" }
        require(paddingVertical >= 0f) { "paddingVertical must be >= 0, was $paddingVertical" }
        require(pointerLengthPx >= 0f) { "pointerLengthPx must be >= 0, was $pointerLengthPx" }
    }
}
