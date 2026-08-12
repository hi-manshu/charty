package com.himanshoe.charty.common.interaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.color.ChartyColor

/**
 * A composable function that displays a horizontal row of preset period pill buttons.
 *
 * Feed the selected option's [RangeOption.pointCount] into the chart config's `visibleWindow`
 * (e.g. `LineChartConfig(visibleWindow = selected.pointCount)`): the chart already handles the
 * rolling-window clipping and slide animation, so the selector needs no chart-specific glue.
 * The row exposes single-selection semantics for screen readers via a selectable group.
 *
 * @param options The preset periods to offer, rendered left to right.
 * @param selected The currently active option, hoisted by the caller and updated in [onSelect].
 * @param onSelect Invoked with the tapped option; store it in state and pass it back as [selected].
 * @param modifier Modifier applied to the selector row.
 * @param style Colors, corner radius, text style, and padding of the pill buttons.
 *
 * Example:
 * ```kotlin
 * var selected by remember { mutableStateOf(RangeOption.All) }
 * ChartRangeSelector(
 *     options = listOf(RangeOption.last(count = 7, label = "7D"), RangeOption.All),
 *     selected = selected,
 *     onSelect = { selected = it },
 * )
 * LineChart(
 *     data = { points },
 *     lineConfig = LineChartConfig(visibleWindow = selected.pointCount),
 * )
 * ```
 */
@Composable
fun ChartRangeSelector(
    options: List<RangeOption>,
    selected: RangeOption,
    onSelect: (RangeOption) -> Unit,
    modifier: Modifier = Modifier,
    style: RangeSelectorStyle = RangeSelectorStyle(),
) {
    if (options.isEmpty()) {
        return
    }
    val pillShape = remember(style.cornerRadius) { RoundedCornerShape(size = style.cornerRadius) }
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(space = style.itemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.fastForEach { option ->
            RangeSelectorPill(
                option = option,
                isSelected = option == selected,
                onSelect = onSelect,
                style = style,
                shape = pillShape,
            )
        }
    }
}

@Composable
private fun RangeSelectorPill(
    option: RangeOption,
    isSelected: Boolean,
    onSelect: (RangeOption) -> Unit,
    style: RangeSelectorStyle,
    shape: Shape,
) {
    val background =
        if (isSelected) {
            style.selectedColor
        } else {
            style.unselectedColor
        }
    val textColor =
        if (isSelected) {
            style.selectedTextColor
        } else {
            style.unselectedTextColor
        }
    BasicText(
        text = option.label,
        modifier =
            Modifier
                .clip(shape)
                .pillBackground(color = background)
                .selectable(
                    selected = isSelected,
                    role = Role.Button,
                    onClick = { onSelect(option) },
                ).padding(horizontal = style.horizontalPadding, vertical = style.verticalPadding),
        style = style.textStyle.copy(color = textColor.firstColor()),
    )
}

/**
 * Fills the pill with [color]: a flat fill for [ChartyColor.Solid], a horizontal gradient for
 * [ChartyColor.Gradient].
 */
private fun Modifier.pillBackground(color: ChartyColor): Modifier =
    when (color) {
        is ChartyColor.Solid -> background(color = color.color)
        is ChartyColor.Gradient -> background(brush = Brush.horizontalGradient(colors = color.colors))
    }

/** The color a pill label renders with: the solid color, or a gradient's first stop. */
private fun ChartyColor.firstColor(): Color = value.first()
