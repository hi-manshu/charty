@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
    "ktlint:standard:max-line-length",
)

package com.himanshoe.sample

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.delay

private const val SLIDE_MILLIS = 260
private const val AUTO_INTERVAL_MILLIS = 700L
private const val BUFFER_CAP = 60
private const val SEED_COUNT = 12
private const val VALUE_MIN = 15f
private const val VALUE_MAX = 95f
private const val AXIS_MAX = 100f

private fun nextValue(): Float = VALUE_MIN + Random.nextFloat() * (VALUE_MAX - VALUE_MIN)

/**
 * Prototype of a rolling / streaming window: shows only the last [windowSize] points; new points
 * append on the right and the line slides left.
 *
 * A single retargetable [scrollPos] tween (the fractional absolute index of the rightmost visible
 * point) tracks the newest point with a tiny lag, so continuous streaming and bursts (auto + manual,
 * or many rapid clicks) both stay smooth — no per-append restart, so no jitter. A lag clamp keeps
 * the visible window inside the retained buffer even under extreme bursts.
 */
@Composable
internal fun StreamingLinePlayground() {
    var windowSize by remember { mutableStateOf(8) }
    var auto by remember { mutableStateOf(true) }
    var showNewestLabel by remember { mutableStateOf(true) }
    var color by remember { mutableStateOf(playgroundPalette[0]) }
    val textMeasurer = rememberTextMeasurer()

    val buffer = remember { mutableStateListOf<Float>().apply { repeat(SEED_COUNT) { add(nextValue()) } } }
    var total by remember { mutableStateOf(SEED_COUNT) }
    val scrollPos = remember { Animatable((SEED_COUNT - 1).toFloat()) }

    fun append() {
        buffer.add(nextValue())
        if (buffer.size > BUFFER_CAP) {
            buffer.removeAt(0)
        }
        total += 1
    }

    LaunchedEffect(total) {
        val target = (total - 1).toFloat()
        val maxLag = (BUFFER_CAP - windowSize - 2).toFloat().coerceAtLeast(1f)
        if (target - scrollPos.value > maxLag) {
            scrollPos.snapTo(target - maxLag)
        }
        scrollPos.animateTo(targetValue = target, animationSpec = tween(durationMillis = SLIDE_MILLIS))
    }
    LaunchedEffect(auto) {
        while (auto) {
            delay(AUTO_INTERVAL_MILLIS)
            append()
        }
    }

    val newest = buffer.lastOrNull()
    val labelStyle = remember { TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
    val code =
        """
        // Prototype: a rolling window of the last $windowSize points.
        // New points append on the right; a retargetable tween tracks the newest index,
        // so continuous streaming and bursts both stay smooth (no jitter). A lag clamp
        // keeps the window inside the retained buffer.
        //
        // Proposed API on the real chart:
        // LineChart(
        //     data = { streamBuffer },
        //     lineConfig = LineChartConfig(visibleWindow = $windowSize),
        // )
        """.trimIndent()

    PlaygroundScaffold(
        code = code,
        chart = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padding = 14f
                val left = padding
                val top = padding
                val right = size.width - padding
                val bottom = size.height - padding
                val plotWidth = right - left
                val plotHeight = bottom - top

                val gridColor = Color(0x14000000)
                for (g in 0..4) {
                    val y = top + plotHeight * g / 4f
                    drawLine(color = gridColor, start = Offset(left, y), end = Offset(right, y), strokeWidth = 1f)
                }

                val slotCount = windowSize
                val spacing = plotWidth / (slotCount - 1).coerceAtLeast(1)
                val firstAbs = total - buffer.size
                val leftmostVisible = scrollPos.value - (slotCount - 1)

                fun xAt(absIndex: Int): Float = left + (absIndex - leftmostVisible) * spacing

                fun yAt(value: Float): Float = bottom - (value.coerceIn(0f, AXIS_MAX) / AXIS_MAX) * plotHeight

                if (buffer.size >= 2) {
                    clipRect(left = left, top = 0f, right = right, bottom = size.height) {
                        val path = Path()
                        buffer.forEachIndexed { index, value ->
                            val point = Offset(xAt(firstAbs + index), yAt(value))
                            if (index == 0) {
                                path.moveTo(point.x, point.y)
                            } else {
                                path.lineTo(point.x, point.y)
                            }
                        }
                        drawPath(path = path, color = color, style = Stroke(width = 3f, cap = StrokeCap.Round))
                        buffer.forEachIndexed { index, value ->
                            drawCircle(color = color, radius = 5f, center = Offset(xAt(firstAbs + index), yAt(value)))
                        }
                    }
                }

                if (showNewestLabel && newest != null && buffer.size >= 1) {
                    val text = newest.toInt().toString()
                    val layout = textMeasurer.measure(text = text, style = labelStyle)
                    val padH = 8f
                    val padV = 4f
                    val bubbleWidth = layout.size.width + padH * 2
                    val bubbleHeight = layout.size.height + padV * 2
                    val pointX = xAt(total - 1).coerceIn(left, right)
                    val pointY = yAt(newest)
                    val bubbleX = (pointX - bubbleWidth / 2f).coerceIn(left, right - bubbleWidth)
                    val bubbleY = (pointY - 14f - bubbleHeight).coerceAtLeast(0f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(bubbleX, bubbleY),
                        size = Size(bubbleWidth, bubbleHeight),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                    drawText(textLayoutResult = layout, topLeft = Offset(bubbleX + padH, bubbleY + padV))
                }
            }
        },
        controls = {
            ControlSection(title = "Stream")
            SwitchRow(label = "Auto (add every 0.7s)", checked = auto, onCheckedChange = { auto = it })
            SwitchRow(
                label = "Label newest point",
                checked = showNewestLabel,
                onCheckedChange = { showNewestLabel = it },
            )
            PlaygroundActionRow(
                primaryLabel = "Add point",
                onPrimary = { append() },
                secondaryLabel = "Clear",
                onSecondary = {
                    buffer.clear()
                    repeat(2) { buffer.add(nextValue()) }
                    total += 2
                },
            )
            ControlSection(title = "Window")
            IntSliderRow(label = "Visible points", value = windowSize, valueRange = 4..20, onValueChange = {
                windowSize =
                    it
            })
            newest?.let {
                ControlSection(title = "Live")
                Text(
                    text = "newest: ${it.toInt()}   ·   total: $total",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            ControlSection(title = "Color")
            ColorRow(label = "Line color", selected = color, onSelect = { color = it })
        },
    )
}
