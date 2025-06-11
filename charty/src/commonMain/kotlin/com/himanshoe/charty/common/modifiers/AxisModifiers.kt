package com.himanshoe.charty.common.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.config.LabelConfig
import com.himanshoe.charty.common.getTetStyle

internal fun Modifier.CommonDrawYAxisLabels(
    labelConfig: LabelConfig,
    textMeasurer: TextMeasurer,
    minValue: Float,
    maxValue: Float,
    axisColor: ChartColor, // For consistency, but labelConfig.textColor is used
    steps: Int = 4,
    dataCount: Int,
    fontSizeSelector: ((canvasWidth: Float, canvasHeight: Float, dataCount: Int) -> TextUnit)? = null,
    labelFormatter: ((value: Float) -> String)? = null
): Modifier = this.drawWithCache {
    onDrawBehind {
        if (labelConfig.showYLabel) {
            val yRange = maxValue - minValue
            val stepSize = if (steps > 0) yRange / steps else 0f

            for (i in 0..steps) {
                val value = minValue + i * stepSize
                val displayValue = labelFormatter?.invoke(value) ?: value.toString().take(4)

                val yPosition = if (yRange == 0f) size.height else size.height - ((value - minValue) / yRange) * size.height
                val selectedFontSize = fontSizeSelector?.invoke(size.width, size.height, dataCount) ?: 12.sp
                val textStyle = labelConfig.getTetStyle(fontSize = selectedFontSize)

                val textLayoutResult = textMeasurer.measure(
                    text = displayValue,
                    style = textStyle,
                    overflow = TextOverflow.Clip,
                    maxLines = 1,
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = -textLayoutResult.size.width - 8f,
                        y = yPosition - textLayoutResult.size.height / 2
                    ),
                    brush = Brush.linearGradient(labelConfig.textColor.value)
                )
            }
        }
    }
}

internal fun Modifier.CommonDrawAxisLines(
    axisColor: ChartColor,
    strokeWidth: Float,
    centerHorizontallyIfNegative: Boolean = false
): Modifier = this.drawWithCache {
    onDrawBehind {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val xAxisYPosition = if (centerHorizontallyIfNegative) canvasHeight / 2 else canvasHeight

        drawLine(
            brush = Brush.linearGradient(axisColor.value),
            start = Offset(0f, xAxisYPosition),
            end = Offset(canvasWidth, xAxisYPosition),
            strokeWidth = strokeWidth
        )
        drawLine(
            brush = Brush.linearGradient(axisColor.value),
            start = Offset(0f, 0f),
            end = Offset(0f, canvasHeight),
            strokeWidth = strokeWidth
        )
    }
}

internal fun Modifier.CommonDrawHorizontalGridLines(
    gridLineColor: ChartColor,
    strokeWidth: Float,
    pathEffect: PathEffect?,
    steps: Int = 4,
    centerHorizontallyIfNegative: Boolean = false // This now determines if lines are only in positive, or both pos/neg space from center
): Modifier = this.drawWithCache {
    onDrawBehind {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val brush = Brush.linearGradient(gridLineColor.value)

        if (steps <= 0) return@onDrawBehind

        if (centerHorizontallyIfNegative) {
            val yCenter = canvasHeight / 2
            // Draw 'steps' lines above center and 'steps' lines below center
            // Total lines will be 2 * steps. Example: steps = 2 means 2 above, 2 below.
            val actualStepSize = (canvasHeight / 2) / (steps +1) // +1 to create space from axis and top/bottom

            for (i in 1..steps) {
                val yOffsetTop = yCenter - i * actualStepSize
                val yOffsetBottom = yCenter + i * actualStepSize
                drawLine(brush = brush, start = Offset(0f, yOffsetTop), end = Offset(canvasWidth, yOffsetTop), strokeWidth = strokeWidth, pathEffect = pathEffect)
                drawLine(brush = brush, start = Offset(0f, yOffsetBottom), end = Offset(canvasWidth, yOffsetBottom), strokeWidth = strokeWidth, pathEffect = pathEffect)
            }
        } else {
            // Draw 'steps' lines total, effectively creating 'steps' intervals from top to bottom.
            // The last line is the X-axis, so we draw 'steps - 1' grid lines.
            val yStepSize = canvasHeight / steps
            for (i in 1 until steps) {
                val yOffset = i * yStepSize // Drawing from top down
                 drawLine(brush = brush, start = Offset(0f, yOffset), end = Offset(canvasWidth, yOffset), strokeWidth = strokeWidth, pathEffect = pathEffect)
            }
        }
    }
}

internal fun Modifier.CommonDrawXAxisLabels(
    labelConfig: LabelConfig,
    textMeasurer: TextMeasurer,
    xPositions: List<Float>, // Center X for each label
    labelTexts: List<String>,
    // axisColor: ChartColor, // labelConfig.textColor will be used
    xAxisYPosition: Float, // Y position of the X-axis (bottom of chart or center)
    dataCount: Int, // For fontSizeSelector and textCharCountProvider
    fontSizeSelector: ((canvasWidth: Float, canvasHeight: Float, dataCount: Int) -> TextUnit)? = null,
    labelFormatter: ((originalText: String, index: Int) -> String)? = null,
    textCharCountProvider: ((originalText: String, displayDataCount: Int) -> Int)? = null
): Modifier = this.drawWithCache {
    onDrawBehind {
        if (labelConfig.showXLabel && xPositions.size == labelTexts.size) {
            xPositions.forEachIndexed { index, xPos ->
                val originalText = labelTexts.getOrElse(index) { "" }
                val charCount = textCharCountProvider?.invoke(originalText, dataCount)
                var textToDisplay = labelFormatter?.invoke(originalText, index) ?: originalText

                if (charCount != null && charCount > 0 && charCount < textToDisplay.length) {
                    textToDisplay = textToDisplay.take(charCount)
                }

                val selectedFontSize = fontSizeSelector?.invoke(size.width, size.height, dataCount) ?: 12.sp
                val textStyle = labelConfig.getTetStyle(fontSize = selectedFontSize)

                val textLayoutResult = textMeasurer.measure(
                    text = textToDisplay,
                    style = textStyle,
                    overflow = TextOverflow.Clip, // Or Visible, depending on desired behavior
                    maxLines = 1
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = xPos - textLayoutResult.size.width / 2, // Center text on xPos
                        y = xAxisYPosition + 4.dp.toPx() // Position below the X-axis line
                    ),
                    brush = Brush.linearGradient(labelConfig.textColor.value)
                )
            }
        }
    }
}
