package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.bar.config.Bar3DLabelPlacement
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace
import kotlin.math.atan2

private const val SCENE_WIDTH = 100f
private const val SCENE_HEIGHT = 62f
private const val FLOOR_ALPHA = 0.07f
private const val LABEL_GAP = 10f
private const val CATEGORY_LABEL_GAP = 12f
private const val EDGE_PADDING = 16f
private const val LABEL_HEADROOM = 26f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f
private const val RADIANS_TO_DEGREES = 57.29578f

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
    val footroom =
        if (config.showCategoryLabels && config.categoryLabelPlacement != Bar3DLabelPlacement.TOP_FACE) {
            LABEL_HEADROOM
        } else {
            0f
        }
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
        val angle = sceneTextAngle(config = config, y = -height, z = depth / HALF, fit = fit)
        rotate(degrees = angle, pivot = anchor) {
            drawText(
                textLayoutResult = layout,
                topLeft =
                    Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y - layout.size.height - LABEL_GAP),
            )
        }
    }
}

/**
 * The screen angle, in degrees, that the scene's own x axis runs at when projected at [y] and [z].
 *
 * Text drawn at this angle lies in the plane of the scene rather than across it, so a label reads as
 * part of the figure instead of pasted over it. It is measured from the projection rather than
 * assumed, so it stays correct as the viewing angle changes.
 */
private fun sceneTextAngle(
    config: Bar3DChartConfig,
    y: Float,
    z: Float,
    fit: SceneFit,
): Float {
    val start = fit.apply(config.projection.project(point = Point3D(x = 0f, y = y, z = z), origin = Offset.Zero))
    val end = fit.apply(config.projection.project(point = Point3D(x = SCENE_WIDTH, y = y, z = z), origin = Offset.Zero))
    return atan2(y = end.y - start.y, x = end.x - start.x) * RADIANS_TO_DEGREES
}

/**
 * Whether floor labels would actually land on top of the bars at the angle in use.
 *
 * This asks the question directly rather than through a proxy: place each label where the floor
 * would put it, and see whether it falls across a bar at all — including its own, which at a steep
 * yaw leans over its own footing. A gentle tilt leaves the labels in the clear and they stay on the
 * floor where they read best; tilt far enough that a bar covers the ground it stands on, and they
 * move onto the top faces.
 */
private fun floorLabelsCollide(
    dataList: List<BarData>,
    config: Bar3DChartConfig,
    fit: SceneFit,
    faces: List<ProjectedFace<BarData>>,
    labelSize: Size,
): Boolean {
    if (dataList.size < 2) {
        return false
    }
    return dataList.indices.any { index ->
        val (left, width, depth) = barSlot(index = index, count = dataList.size, config = config)
        val anchor =
            fit.apply(
                config.projection.project(
                    point = Point3D(x = left + width / HALF, y = 0f, z = depth / HALF),
                    origin = Offset.Zero,
                ),
            )
        val probes =
            listOf(
                Offset(x = anchor.x, y = anchor.y + CATEGORY_LABEL_GAP + labelSize.height / HALF),
                Offset(x = anchor.x - labelSize.width / HALF, y = anchor.y + CATEGORY_LABEL_GAP),
                Offset(x = anchor.x + labelSize.width / HALF, y = anchor.y + CATEGORY_LABEL_GAP),
            )
        faces.any { face -> probes.any(face::contains) }
    }
}

/**
 * Prints each bar's category label, on the floor beneath it or centred on its top face, rotated into
 * the plane it sits in.
 *
 * The top-face anchor is the average of that face's four **projected** corners rather than the
 * projection of its centre, so the label stays visually centred under a perspective view, where the
 * far edge of the face is narrower than the near one.
 */
internal fun DrawScope.drawBar3DCategoryLabels(
    dataList: List<BarData>,
    maxValue: Float,
    config: Bar3DChartConfig,
    progress: Float,
    fit: SceneFit,
    faces: List<ProjectedFace<BarData>>,
    textMeasurer: TextMeasurer,
) {
    if (dataList.isEmpty()) {
        return
    }
    val sample = textMeasurer.measure(text = dataList.first().label, style = config.categoryLabelStyle)
    val onTop =
        when (config.categoryLabelPlacement) {
            Bar3DLabelPlacement.FLOOR -> false
            Bar3DLabelPlacement.TOP_FACE -> true
            Bar3DLabelPlacement.AUTO ->
                floorLabelsCollide(
                    dataList = dataList,
                    config = config,
                    fit = fit,
                    faces = faces,
                    labelSize = Size(width = sample.size.width.toFloat(), height = sample.size.height.toFloat()),
                )
        }

    dataList.forEachIndexed { index, bar ->
        val (left, width, depth) = barSlot(index = index, count = dataList.size, config = config)
        val top = -barHeight(value = bar.value, maxValue = maxValue, progress = progress)
        val layout = textMeasurer.measure(text = bar.label, style = config.categoryLabelStyle)

        val anchor =
            if (onTop) {
                listOf(
                    Point3D(x = left, y = top, z = 0f),
                    Point3D(x = left + width, y = top, z = 0f),
                    Point3D(x = left + width, y = top, z = depth),
                    Point3D(x = left, y = top, z = depth),
                ).map { point -> fit.apply(config.projection.project(point = point, origin = Offset.Zero)) }
                    .centroid()
            } else {
                fit.apply(
                    config.projection.project(
                        point = Point3D(x = left + width / HALF, y = 0f, z = depth / HALF),
                        origin = Offset.Zero,
                    ),
                )
            }

        val topLeft =
            if (onTop) {
                Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y - layout.size.height / HALF)
            } else {
                Offset(x = anchor.x - layout.size.width / HALF, y = anchor.y + CATEGORY_LABEL_GAP)
            }
        val angle =
            sceneTextAngle(
                config = config,
                y = if (onTop) top else 0f,
                z = depth / HALF,
                fit = fit,
            )
        rotate(degrees = angle, pivot = anchor) {
            drawText(textLayoutResult = layout, topLeft = topLeft)
        }
    }
}

/** The average of a list of points, used to centre a label on a projected face. */
private fun List<Offset>.centroid(): Offset =
    Offset(
        x = sumOf { point -> point.x.toDouble() }.toFloat() / size,
        y = sumOf { point -> point.y.toDouble() }.toFloat() / size,
    )
