package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace

private const val PLOT_WIDTH_FRACTION = 0.78f
private const val PLOT_HEIGHT_FRACTION = 0.7f
private const val ORIGIN_X_FRACTION = 0.16f
private const val ORIGIN_Y_FRACTION = 0.8f
private const val FLOOR_ALPHA = 0.07f
private const val LABEL_GAP = 10f
private const val CATEGORY_LABEL_GAP = 12f
private const val HALF = 2f

/** The canvas position the scene's origin — the near-left corner of the floor — sits at. */
private fun originFor(size: Size): Offset =
    Offset(x = size.width * ORIGIN_X_FRACTION, y = size.height * ORIGIN_Y_FRACTION)

/** The scene's extent along x, in the same units the projection works in. */
private fun plotWidth(size: Size): Float = size.width * PLOT_WIDTH_FRACTION

/** The scene's extent along y, used to scale a bar's value into a height. */
private fun plotHeight(size: Size): Float = size.height * PLOT_HEIGHT_FRACTION

/**
 * Builds every visible face of every bar, in no particular order — the caller sorts.
 *
 * Bars are laid out left to right along x and share one depth, so the scene reads as a single row of
 * solids standing on a floor rather than a scattered cloud.
 *
 * @param size The canvas size.
 * @param dataList The bars to build.
 * @param maxValue The value that reaches the full plot height.
 * @param config The projection and sizing to build with.
 * @param progress Animation progress from `0f` to `1f`; bars grow from the floor.
 */
internal fun bar3DFaces(
    size: Size,
    dataList: List<BarData>,
    maxValue: Float,
    config: Bar3DChartConfig,
    progress: Float,
): List<ProjectedFace<BarData>> {
    if (dataList.isEmpty()) {
        return emptyList()
    }
    val origin = originFor(size)
    val slotWidth = plotWidth(size) / dataList.size
    val barWidth = slotWidth * config.barWidthFraction
    val barDepth = barWidth * config.barDepthFraction
    val faces = mutableListOf<ProjectedFace<BarData>>()

    dataList.forEachIndexed { index, bar ->
        val height = (bar.value / maxValue) * plotHeight(size) * progress
        val left = index * slotWidth + (slotWidth - barWidth) / HALF
        faces +=
            boxFaces(
                projection = config.projection,
                origin = origin,
                nearLeft = Point3D(x = left, y = 0f, z = 0f),
                width = barWidth,
                height = -height,
                depth = barDepth,
                payload = bar,
            )
    }
    return faces
}

/**
 * Draws the floor the bars stand on: the plane the scene's y is zero on, projected and filled faintly
 * so it grounds the depth without competing with the bars.
 */
internal fun DrawScope.drawBar3DFloor(
    size: Size,
    config: Bar3DChartConfig,
) {
    val origin = originFor(size)
    val width = plotWidth(size)
    val depth = width * config.barDepthFraction * config.barWidthFraction
    val corners =
        listOf(
            Point3D(x = 0f, y = 0f, z = 0f),
            Point3D(x = width, y = 0f, z = 0f),
            Point3D(x = width, y = 0f, z = depth),
            Point3D(x = 0f, y = 0f, z = depth),
        ).map { point -> config.projection.project(point = point, origin = origin) }

    val path =
        androidx.compose.ui.graphics.Path().apply {
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

/**
 * Prints each bar's value above the centre of its top face, projected the same way the bar is so the
 * label travels with it when the viewing angle changes.
 */
internal fun DrawScope.drawBar3DLabels(
    size: Size,
    dataList: List<BarData>,
    maxValue: Float,
    config: Bar3DChartConfig,
    progress: Float,
    textMeasurer: TextMeasurer,
) {
    val origin = originFor(size)
    val slotWidth = plotWidth(size) / dataList.size
    val barWidth = slotWidth * config.barWidthFraction
    val barDepth = barWidth * config.barDepthFraction

    dataList.forEachIndexed { index, bar ->
        val height = (bar.value / maxValue) * plotHeight(size) * progress
        val left = index * slotWidth + (slotWidth - barWidth) / HALF
        val topCentre =
            config.projection.project(
                point = Point3D(x = left + barWidth / HALF, y = -height, z = barDepth / HALF),
                origin = origin,
            )
        val layout = textMeasurer.measure(text = config.valueFormatter(bar.value), style = config.valueLabelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft =
                Offset(
                    x = topCentre.x - layout.size.width / HALF,
                    y = topCentre.y - layout.size.height - LABEL_GAP,
                ),
        )
    }
}

/**
 * Prints each bar's category label on the floor beneath it, projected so the labels sit on the same
 * plane the bars stand on and travel with them when the viewing angle changes.
 */
internal fun DrawScope.drawBar3DCategoryLabels(
    size: Size,
    dataList: List<BarData>,
    config: Bar3DChartConfig,
    textMeasurer: TextMeasurer,
) {
    val origin = originFor(size)
    val slotWidth = plotWidth(size) / dataList.size
    val barWidth = slotWidth * config.barWidthFraction
    val barDepth = barWidth * config.barDepthFraction

    dataList.forEachIndexed { index, bar ->
        val left = index * slotWidth + (slotWidth - barWidth) / HALF
        val footCentre =
            config.projection.project(
                point = Point3D(x = left + barWidth / HALF, y = 0f, z = barDepth / HALF),
                origin = origin,
            )
        val layout = textMeasurer.measure(text = bar.label, style = config.categoryLabelStyle)
        drawText(
            textLayoutResult = layout,
            topLeft =
                Offset(
                    x = footCentre.x - layout.size.width / HALF,
                    y = footCentre.y + CATEGORY_LABEL_GAP,
                ),
        )
    }
}
