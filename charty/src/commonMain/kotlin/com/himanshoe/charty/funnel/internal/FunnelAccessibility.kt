package com.himanshoe.charty.funnel.internal

import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.funnel.config.FunnelChartConfig
import com.himanshoe.charty.funnel.data.FunnelStage

/**
 * Builds the chart-level screen-reader summary: the stages, and the overall survival from the first
 * stage to the last, which is the number a funnel exists to answer.
 *
 * @param stages The stages in funnel order.
 * @param config Supplies the value and conversion formatters.
 * @return A sentence describing the chart.
 */
internal fun generateFunnelChartDescription(
    stages: List<FunnelStage>,
    config: FunnelChartConfig,
): String {
    if (stages.isEmpty()) {
        return "Empty funnel chart."
    }
    val first = stages.first()
    val last = stages.last()
    val overall =
        if (first.value > 0f) {
            config.conversionFormatter(last.value / first.value)
        } else {
            null
        }
    return buildString {
        append("Funnel chart, ${stages.size} stages. ")
        append("${first.label} ${config.valueFormatter(first.value)} ")
        append("to ${last.label} ${config.valueFormatter(last.value)}. ")
        overall?.let { fraction -> append("Overall conversion: $fraction.") }
    }
}

/**
 * Builds one description per stage, each naming the stage, its value, and the fraction of the
 * previous stage that reached it, so assistive technology can walk the funnel step by step.
 *
 * @param stages The stages in funnel order.
 * @param config Supplies the value and conversion formatters.
 * @return One description per stage, in stage order.
 */
internal fun funnelStageDescriptions(
    stages: List<FunnelStage>,
    config: FunnelChartConfig,
): List<String> =
    stages.fastMapIndexed { index, stage ->
        val conversion =
            funnelConversion(stages = stages, index = index)
                ?.let { fraction -> ", ${config.conversionFormatter(fraction)} of the previous stage" }
                .orEmpty()
        "Stage ${index + 1} of ${stages.size}: ${stage.label}, ${config.valueFormatter(stage.value)}$conversion"
    }
