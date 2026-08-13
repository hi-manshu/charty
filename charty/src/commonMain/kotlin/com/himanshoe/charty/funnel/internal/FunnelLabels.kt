package com.himanshoe.charty.funnel.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.funnel.config.FunnelChartConfig
import com.himanshoe.charty.funnel.data.FunnelStage

/**
 * The text of a funnel, measured once per data change rather than once per frame, so the draw loop
 * only positions text it has already laid out.
 *
 * @property stage One measured stage label per stage, or `null` where stage labels are off.
 * @property conversion One measured conversion label per stage, `null` for the first stage, for a
 *   stage whose predecessor was empty, and where conversion labels are off.
 */
internal class FunnelLabels(
    val stage: List<TextLayoutResult?>,
    val conversion: List<TextLayoutResult?>,
)

/**
 * Measures the stage and conversion labels.
 *
 * @param stages The stages in funnel order.
 * @param config Supplies the formatters, text styles, and which labels are shown.
 * @param textMeasurer The measurer used to lay the labels out.
 * @return The measured labels; entries are `null` wherever there is nothing to draw.
 */
@Composable
internal fun rememberFunnelLabels(
    stages: List<FunnelStage>,
    config: FunnelChartConfig,
    textMeasurer: TextMeasurer,
): FunnelLabels =
    remember(stages, config, textMeasurer) {
        FunnelLabels(
            stage =
                stages.map { stage ->
                    if (config.showStageLabels) {
                        textMeasurer.measure(
                            text = "${stage.label} ${config.valueFormatter(stage.value)}",
                            style = config.labelStyle,
                        )
                    } else {
                        null
                    }
                },
            conversion =
                stages.mapIndexed { index, _ ->
                    val fraction = funnelConversion(stages = stages, index = index)
                    if (config.showConversionLabels && fraction != null) {
                        textMeasurer.measure(
                            text = config.conversionFormatter(fraction),
                            style = config.conversionLabelStyle,
                        )
                    } else {
                        null
                    }
                },
        )
    }
