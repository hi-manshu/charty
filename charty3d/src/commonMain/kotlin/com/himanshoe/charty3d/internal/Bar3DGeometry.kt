package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace

private const val SCENE_WIDTH = 100f
private const val SCENE_HEIGHT = 62f
private const val FLOOR_ALPHA = 0.07f
private const val LABEL_GAP = 10f
private const val CATEGORY_LABEL_GAP = 12f
private const val EDGE_PADDING = 16f
private const val LABEL_HEADROOM = 26f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f

/**
 * The scale and offset that fit a projected scene inside the canvas.
 *
 * The scene is built in its own units and only then fitted, because how much room a projection needs
 * depends entirely on the angle: a steep pitch makes the scene taller, a wide yaw wider, and a
 * perspective view narrows its far end. Measuring the projected result and scaling to fit is what
 * keeps every angle usable, rather than letting the extreme ones run off the canvas.
 */
internal data class SceneFit(
    val scale: Float,
    val offset: Offset,
) {
    /** Maps a point from unfitted scene space onto the canvas. */
    fun apply(point: Offset): Offset = Offset(x = point.x * scale + offset.x, y = point.y * scale + offset.y)
}

private fun barSlot(
    index: Int,
    count: Int,
    config: Bar3DChartConfig,
): Triple<Float, Float, Float> {
    val slotWidth = SCENE_WIDTH / count
    val barWidth = slotWidth * config.barWidthFraction
    val barDepth = barWidth * config.barDepthFraction
    val left = index * slotWidth + (slotWidth - barWidth) / HALF
    return Triple(left, barWidth, barDepth)
}

private fun barHeight(
    value: Float,
    maxValue: Float,
    progress: Float,
): Float = (value / maxValue) * SCENE_HEIGHT * progress

private fun floorCorners(config: Bar3DChartConfig): List<Point3D> {
    val depth = SCENE_WIDTH * config.barWidthFraction * config.barDepthFraction
    return listOf(
        Point3D(x = 0f, y = 0f, z = 0f),
        Point3D(x = SCENE_WIDTH, y = 0f, z = 0f),
        Point3D(x = SCENE_WIDTH, y = 0f, z = depth),
        Point3D(x = 0f, y = 0f, z = depth),
    )
}

/**
 * Measures the whole projected scene and returns the transform that centres it inside [size].
 *
 * Room is reserved above the tallest bar and below the floor for the value and category labels, so
 * switching either on never pushes the scene off the canvas.
 */
internal fun bar3DFit(
    size: Size,
    dataList: List<BarData>,
    maxValue: Float,
    config: Bar3DChartConfig,
    progress: Float,
): SceneFit {
    val corners =
        dataList.flatMapIndexed { index, bar ->
            val (left, width, depth) = barSlot(index = index, count = dataList.size, config = config)
            val height = barHeight(value = bar.value, maxValue = maxValue, progress = progress)
            listOf(0f, -height).flatMap { y ->
                listOf(
                    Point3D(x = left, y = y, z = 0f),
                    Point3D(x = left + width, y = y, z = 0f),
                    Point3D(x = left, y = y, z = depth),
                    Point3D(x = left + width, y = y, z = depth),
                )
            }
        } + floorCorners(config)

    val projected = corners.map { point -> config.projection.project(point = point, origin = Offset.Zero) }
    if (projected.isEmpty()) {
        return SceneFit(scale = 1f, offset = Offset.Zero)
    }

    val minX = projected.minOf { point -> point.x }
    val maxX = projected.maxOf { point -> point.x }
    val minY = projected.minOf { point -> point.y }
    val maxY = projected.maxOf { point -> point.y }
    val spanX = (maxX - minX).coerceAtLeast(MIN_SPAN)
    val spanY = (maxY - minY).coerceAtLeast(MIN_SPAN)

    val headroom = if (config.showValueLabels) LABEL_HEADROOM else 0f
    val footroom = if (config.showCategoryLabels) LABEL_HEADROOM else 0f
    val availableWidth = (size.width - EDGE_PADDING * HALF).coerceAtLeast(MIN_SPAN)
    val availableHeight = (size.height - EDGE_PADDING * HALF - headroom - footroom).coerceAtLeast(MIN_SPAN)
    val scale = minOf(availableWidth / spanX, availableHeight / spanY)

    val centredX = (size.width - spanX * scale) / HALF - minX * scale
    val centredY = (size.height + headroom - footroom - spanY * scale) / HALF - minY * scale
    return SceneFit(scale = scale, offset = Offset(x = centredX, y = centredY))
}

/** Builds every visible face of every bar, already fitted to the canvas; the caller sorts them. */
internal fun bar3DFaces(
    dataList: List<BarData>,
    maxValue: Float,
    config: Bar3DChartConfig,
    progress: Float,
    fit: SceneFit,
): List<ProjectedFace<BarData>> {
    if (dataList.isEmpty()) {
        return emptyList()
    }
    val faces = mutableListOf<ProjectedFace<BarData>>()
    dataList.forEachIndexed { index, bar ->
        val (left, width, depth) = barSlot(index = index, count = dataList.size, config = config)
        val height = barHeight(value = bar.value, maxValue = maxValue, progress = progress)
        faces +=
            boxFaces(
                projection = config.projection,
                origin = Offset.Zero,
                nearLeft = Point3D(x = left, y = 0f, z = 0f),
                width = width,
                height = -height,
                depth = depth,
                payload = bar,
            ).map { face -> face.copy(points = face.points.map(fit::apply)) }
    }
    return faces
}

/** Draws the plane the bars stand on, filled faintly so it grounds the depth without competing. */
internal fun DrawScope.drawBar3DFloor(
    config: Bar3DChartConfig,
    fit: SceneFit,
) {
    val corners =
        floorCorners(config)
            .map { point -> config.projection.project(point = point, origin = Offset.Zero) }
            .map(fit::apply)
    val path =
        Path().apply {
            corners.forEachIndexed { index, point ->
                if (index == 0) {
                    moveTo(point.x, point.y)
                } else {
                    lineTo(point.x, point.y)
                }
            }
            close()
        }
    drawPath(path = path, color = Color.Black.copy(alpha = FLOOR_ALPHA))
}

/** Prints each bar's value above the centre of its top face. */
internal fun DrawScope.drawBar3DLabels(
    dataList: List<BarData>,
    maxValue: Float,
    config: Bar3DChartConfig,
    progress: Float,
    fit: SceneFit,
    textMeasurer: TextMeasurer,
) {
    dataList.forEachIndexed { index, bar ->
        val (left, width, depth) = barSlot(index = index, count = dataList.size, config = config)
        val height = barHeight(value = bar.value, maxValue = maxValue, progress = progress)
        val anchor =
            fit.apply(
                config.projection.project(
                    point = Point3D(x = left + width / HALF, y = -height, z = depth / HALF),
                    origin = Offset.Zero,
                ),
            )
        val layout = textMeasurer.measure(text = config.valueFormatter(bar.value), style = config.valueLabelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y - layout.size.height - LABEL_GAP),
        )
    }
}

/** Prints each bar's category label on the floor beneath it. */
internal fun DrawScope.drawBar3DCategoryLabels(
    dataList: List<BarData>,
    config: Bar3DChartConfig,
    fit: SceneFit,
    textMeasurer: TextMeasurer,
) {
    dataList.forEachIndexed { index, bar ->
        val (left, width, depth) = barSlot(index = index, count = dataList.size, config = config)
        val anchor =
            fit.apply(
                config.projection.project(
                    point = Point3D(x = left + width / HALF, y = 0f, z = depth / HALF),
                    origin = Offset.Zero,
                ),
            )
        val layout = textMeasurer.measure(text = bar.label, style = config.categoryLabelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y + CATEGORY_LABEL_GAP),
        )
    }
}
