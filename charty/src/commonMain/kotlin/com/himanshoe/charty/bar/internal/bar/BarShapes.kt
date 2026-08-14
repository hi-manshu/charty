package com.himanshoe.charty.bar.internal.bar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Draws one bar with [corners] rounded and the rest square.
 *
 * Every bar-family chart drew this itself. The bodies were character-identical in three separate
 * pairs — vertical against comparison, horizontal against grouped-horizontal, stacked-horizontal
 * against normalized-horizontal — right down to a shared KDoc typo, which is what copy-paste leaves
 * behind. Corner geometry is the easiest thing in this package to get subtly wrong, and a fix applied
 * to one copy had no way of reaching its twin.
 */
internal fun DrawScope.drawBarShape(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
    corners: BarCorners,
) {
    val radius = CornerRadius(cornerRadius, cornerRadius)
    val zero = CornerRadius.Zero
    val path =
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius =
                        when (corners) {
                            BarCorners.TOP, BarCorners.LEADING -> radius
                            else -> zero
                        },
                    topRightCornerRadius =
                        when (corners) {
                            BarCorners.TOP, BarCorners.TRAILING -> radius
                            else -> zero
                        },
                    bottomLeftCornerRadius =
                        when (corners) {
                            BarCorners.BOTTOM, BarCorners.LEADING -> radius
                            else -> zero
                        },
                    bottomRightCornerRadius =
                        when (corners) {
                            BarCorners.BOTTOM, BarCorners.TRAILING -> radius
                            else -> zero
                        },
                ),
            )
        }
    drawPath(path = path, brush = brush)
}
