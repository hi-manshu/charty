package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.ribbon.config.Ribbon3DChartConfig
import com.himanshoe.charty3d.ribbon.data.RibbonSegment

private const val SCENE_LENGTH = 100f
private const val SCENE_HEIGHT = 55f
private const val SCENE_DEPTH = 70f
private const val EDGE_PADDING = 20f
private const val LABEL_ROOM = 40f
private const val HALF = 2f
private const val MIN_SPAN = 0.0001f

/** The highest value across every series, which all of them are scaled against. */
internal fun ribbonValueRange(dataList: List<LineGroup>): ClosedFloatingPointRange<Float> {
    val values = dataList.flatMap { group -> group.values }
    if (values.isEmpty()) {
        return 0f..1f
    }
    val min = minOf(0f, values.min())
    val max = values.max()
    return if (min == max) {
        min..(min + 1f)
    } else {
        min..max
    }
}

/** How many points the longest series has, which sets the length of the shared x axis. */
internal fun ribbonLength(dataList: List<LineGroup>): Int = dataList.maxOfOrNull { group -> group.values.size } ?: 0

/**
 * Builds every ribbon as a strip of quads, one per gap between consecutive points.
 *
 * Each series sits on its own depth slot, which is what separates six crossing lines into six
 * readable bands. Depth here encodes series identity rather than a measured variable — it is not
 * decoration, but it is not a third axis either.
 */
internal fun ribbon3DFaces(
    dataList: List<LineGroup>,
    config: Ribbon3DChartConfig,
    range: ClosedFloatingPointRange<Float>,
    progress: Float,
    fit: SceneFit,
): List<ProjectedFace<RibbonSegment>> {
    val length = ribbonLength(dataList)
    if (length < 2 || dataList.isEmpty()) {
        return emptyList()
    }
    val span = (range.endInclusive - range.start).coerceAtLeast(MIN_SPAN)
    val slotDepth = SCENE_DEPTH / dataList.size
    val ribbonDepth = slotDepth * config.ribbonWidthFraction
    val faces = mutableListOf<ProjectedFace<RibbonSegment>>()

    dataList.forEachIndexed { seriesIndex, group ->
        val near = seriesIndex * slotDepth + (slotDepth - ribbonDepth) / HALF
        val far = near + ribbonDepth

        fun heightAt(index: Int): Float {
            val value = group.values.getOrNull(index) ?: return 0f
            return ((value - range.start) / span) * SCENE_HEIGHT * progress
        }

        fun xAt(index: Int): Float = index.toFloat() / (length - 1) * SCENE_LENGTH

        for (index in 0 until group.values.size - 1) {
            val leftHeight = heightAt(index)
            val rightHeight = heightAt(index + 1)
            val segment =
                RibbonSegment(
                    seriesIndex = seriesIndex,
                    seriesName = group.label,
                    pointIndex = index,
                    value = group.values[index],
                )

            fun add(
                corners: List<Point3D>,
                side: FaceSide,
            ) {
                val view = corners.map { point -> config.projection.toViewSpace(point) }
                faces.add(
                    ProjectedFace(
                        points =
                            corners.map { point ->
                                fit.apply(config.projection.project(point = point, origin = Offset.Zero))
                            },
                        side = side,
                        depth = view.sumOf { point -> point.z.toDouble() }.toFloat() / view.size,
                        payload = segment,
                    ),
                )
            }

            add(
                corners =
                    listOf(
                        Point3D(x = xAt(index), y = -leftHeight, z = near),
                        Point3D(x = xAt(index + 1), y = -rightHeight, z = near),
                        Point3D(x = xAt(index + 1), y = -rightHeight, z = far),
                        Point3D(x = xAt(index), y = -leftHeight, z = far),
                    ),
                side = FaceSide.TOP,
            )
            if (config.fillUnderRibbon) {
                add(
                    corners =
                        listOf(
                            Point3D(x = xAt(index), y = 0f, z = near),
                            Point3D(x = xAt(index + 1), y = 0f, z = near),
                            Point3D(x = xAt(index + 1), y = -rightHeight, z = near),
                            Point3D(x = xAt(index), y = -leftHeight, z = near),
                        ),
                    side = FaceSide.FRONT,
                )
            }
        }
    }
    return faces
}

/** Measures the projected scene and returns the transform that centres it inside [size]. */
internal fun ribbon3DFit(
    size: Size,
    config: Ribbon3DChartConfig,
): SceneFit {
    val corners =
        listOf(0f, SCENE_LENGTH).flatMap { x ->
            listOf(0f, -SCENE_HEIGHT).flatMap { y ->
                listOf(0f, SCENE_DEPTH).map { z -> Point3D(x = x, y = y, z = z) }
            }
        }
    val projected = corners.map { point -> config.projection.project(point = point, origin = Offset.Zero) }
    val minX = projected.minOf { it.x }
    val maxX = projected.maxOf { it.x }
    val minY = projected.minOf { it.y }
    val maxY = projected.maxOf { it.y }
    val spanX = (maxX - minX).coerceAtLeast(MIN_SPAN)
    val spanY = (maxY - minY).coerceAtLeast(MIN_SPAN)

    val room = if (config.showSeriesLabels) LABEL_ROOM else 0f
    val availableWidth = (size.width - EDGE_PADDING * HALF - room).coerceAtLeast(MIN_SPAN)
    val availableHeight = (size.height - EDGE_PADDING * HALF).coerceAtLeast(MIN_SPAN)
    val scale = minOf(availableWidth / spanX, availableHeight / spanY)
    return SceneFit(
        scale = scale,
        offset =
            Offset(
                x = (size.width + room - spanX * scale) / HALF - minX * scale,
                y = (size.height - spanY * scale) / HALF - minY * scale,
            ),
    )
}

/** The canvas position a series' name sits at: level with its ribbon, off its near end. */
internal fun ribbonLabelAnchor(
    seriesIndex: Int,
    seriesCount: Int,
    config: Ribbon3DChartConfig,
    fit: SceneFit,
): Offset {
    val slotDepth = SCENE_DEPTH / seriesCount
    return fit.apply(
        config.projection.project(
            point = Point3D(x = 0f, y = 0f, z = seriesIndex * slotDepth + slotDepth / HALF),
            origin = Offset.Zero,
        ),
    )
}
