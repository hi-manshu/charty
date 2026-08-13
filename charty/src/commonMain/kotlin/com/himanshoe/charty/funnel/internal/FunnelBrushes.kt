package com.himanshoe.charty.funnel.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.funnel.data.FunnelStage

/**
 * Resolves each stage's colour — the stage's own, then the caller's palette, then the ambient theme
 * — and prepares it as a [Brush] during composition, so the draw path never builds paint.
 *
 * @param stages The stages in funnel order.
 * @param stageColors The caller's palette, cycled across the stages; empty falls back to the theme.
 * @return One brush per stage, in stage order.
 */
@Composable
internal fun rememberFunnelBrushes(
    stages: List<FunnelStage>,
    stageColors: List<ChartyColor>,
): List<Brush> {
    val resolved = mutableListOf<ChartyColor>()
    stages.forEachIndexed { index, stage ->
        val themeColor = ChartyThemeDefaults.seriesColor(index = index)
        val fallback =
            if (stageColors.isEmpty()) {
                themeColor
            } else {
                stageColors[index.mod(stageColors.size)]
            }
        resolved.add(stage.color ?: fallback)
    }
    return remember(resolved) {
        resolved.map { color ->
            when (color) {
                is ChartyColor.Solid -> SolidColor(color.color)
                is ChartyColor.Gradient -> Brush.linearGradient(colors = color.colors)
            }
        }
    }
}
