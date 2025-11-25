@file:Suppress(
    "LongMethod",
    "LongParameterList",
    "FunctionNaming",
    "CyclomaticComplexMethod",
    "WildcardImport",
    "MagicNumber",
    "MaxLineLength",
    "ReturnCount",
    "UnusedImports",
)

package com.himanshoe.charty.common.axis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.ext.drawHorizontalChartAxes
import com.himanshoe.charty.common.ext.drawVerticalChartAxes


/**
 * Draws axis lines, grid lines, and labels.
 * Handles both vertical and horizontal orientations.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
internal fun DrawAxisAndLabels(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    orientation: ChartOrientation,
    leftLabelRotation: LabelRotation,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = config.labelTextStyle

    Canvas(modifier = Modifier.fillMaxSize()) {
        when (orientation) {
            ChartOrientation.VERTICAL ->
                drawVerticalChartAxes(
                    xLabels = xLabels,
                    yAxisConfig = yAxisConfig,
                    config = config,
                    textMeasurer = textMeasurer,
                    labelStyle = labelStyle,
                    leftLabelRotation = leftLabelRotation,
                )

            ChartOrientation.HORIZONTAL ->
                drawHorizontalChartAxes(
                    xLabels = xLabels,
                    yAxisConfig = yAxisConfig,
                    config = config,
                    textMeasurer = textMeasurer,
                    labelStyle = labelStyle,
                    leftLabelRotation = leftLabelRotation,
                )
        }
    }
}
