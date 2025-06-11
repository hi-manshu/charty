package com.himanshoe.charty.common.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Checks if a given click offset is inside a rectangle defined by its top-left corner and size.
 *
 * @param clickOffset The offset of the click.
 * @param rectTopLeft The top-left offset of the rectangle.
 * @param rectSize The size of the rectangle.
 * @return True if the click is inside the rectangle, false otherwise.
 */
internal fun isClickInsideRect(
    clickOffset: Offset,
    rectTopLeft: Offset,
    rectSize: Size,
): Boolean = (
    clickOffset.x in rectTopLeft.x..(rectTopLeft.x + rectSize.width) &&
        clickOffset.y in rectTopLeft.y..(rectTopLeft.y + rectSize.height)
    )
