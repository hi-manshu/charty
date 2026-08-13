package com.himanshoe.charty.common.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val TOOLTIP_CORNER = 8.dp
private val CROSSHAIR_LABEL_CORNER = 6.dp
private const val MARKER_LABEL_CORNER_PX = 6f
private const val ANNOTATION_LABEL_CORNER_PX = 4f

/**
 * The corner treatments Charty uses for its floating surfaces.
 *
 * Composable surfaces (the Compose-overlay tooltip, the crosshair pill) take a [Shape]; surfaces
 * painted straight onto the canvas take a radius instead, in device pixels for the canvas drawers and
 * in [Dp] for the tooltip whose drawer converts it. Map a design system by handing over the same
 * shapes it uses for cards and chips.
 *
 * ```kotlin
 * ChartyShapes(
 *     tooltip = MaterialTheme.shapes.small,
 *     crosshairLabel = MaterialTheme.shapes.extraSmall,
 * )
 * ```
 *
 * @property tooltip Shape of the Compose-overlay tooltip container.
 * @property tooltipCornerRadius Corner radius the canvas tooltip rounds its bubble by.
 * @property crosshairLabel Shape of the built-in crosshair value pill.
 * @property markerLabelCornerRadius Corner radius of a persistent marker's callout pill, in pixels.
 * @property annotationLabelCornerRadius Corner radius of an annotation's label chip, in pixels.
 */
@Immutable
data class ChartyShapes(
    val tooltip: Shape = RoundedCornerShape(TOOLTIP_CORNER),
    val tooltipCornerRadius: Dp = TOOLTIP_CORNER,
    val crosshairLabel: Shape = RoundedCornerShape(CROSSHAIR_LABEL_CORNER),
    val markerLabelCornerRadius: Float = MARKER_LABEL_CORNER_PX,
    val annotationLabelCornerRadius: Float = ANNOTATION_LABEL_CORNER_PX,
) {
    init {
        require(tooltipCornerRadius.value >= 0f) { "tooltipCornerRadius must be non-negative" }
        require(markerLabelCornerRadius >= 0f) { "markerLabelCornerRadius must be non-negative" }
        require(annotationLabelCornerRadius >= 0f) { "annotationLabelCornerRadius must be non-negative" }
    }

    /** The single set of Charty shapes; corner treatments do not differ between light and dark. */
    companion object {
        /** The built-in shapes, matching the corners Charty has always drawn. */
        fun default(): ChartyShapes = ChartyShapes()
    }
}
