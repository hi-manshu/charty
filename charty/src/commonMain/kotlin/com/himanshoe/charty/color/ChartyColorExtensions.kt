package com.himanshoe.charty.color

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle

/**
 * Converts this [ChartyColor] into a [Brush] suitable for filling a shape: a [SolidColor] for
 * [ChartyColor.Solid] and a top-to-bottom gradient for [ChartyColor.Gradient].
 *
 * A [ChartyColor.Solid] brush paints exactly what passing the raw [androidx.compose.ui.graphics.Color]
 * to the same draw call would paint, so swapping a color parameter for a `ChartyColor` never changes
 * rendering.
 */
fun ChartyColor.toBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> brush
        is ChartyColor.Gradient -> Brush.verticalGradient(colors)
    }

/**
 * Converts this [ChartyColor] into a [Brush] whose gradient runs left-to-right instead of
 * top-to-bottom. Use it for horizontal elements — a horizontal crosshair line, an edge scrim — where
 * a vertical gradient would collapse into a single visible color.
 */
fun ChartyColor.toHorizontalBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> brush
        is ChartyColor.Gradient -> Brush.horizontalGradient(colors)
    }

/**
 * Returns this [TextStyle] painted with [color]: a [ChartyColor.Solid] becomes the style's text
 * color, and a [ChartyColor.Gradient] becomes a linear brush across the text bounds — which is how
 * Charty supports gradient text while still typing every public color as a [ChartyColor].
 *
 * Passing `null` returns the style unchanged, which is what a config's `null` text-color property
 * means: "keep whatever color the text style itself carries".
 */
fun TextStyle.withChartyColor(color: ChartyColor?): TextStyle =
    when (color) {
        null -> this
        is ChartyColor.Solid -> copy(color = color.color)
        is ChartyColor.Gradient -> copy(brush = Brush.linearGradient(color.colors))
    }
