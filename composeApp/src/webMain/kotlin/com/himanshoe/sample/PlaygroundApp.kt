@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
)

package com.himanshoe.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.theme.ChartyTheme
import com.himanshoe.charty.common.theme.ChartyThemeProvider

internal enum class WebTab { Playground, Gallery }

/** A configurable chart shown in the playground: its name, accent, and the interactive screen. */
internal enum class PlaygroundFamily(
    val title: String,
    val blurb: String,
    val accent: Color,
) {
    Line("Line", "Trend over an index", Color(0xFF2962FF)),
    Area("Area", "Filled line", Color(0xFF00BFA5)),
    Bar("Bar", "Categorical values", Color(0xFFFF6D00)),
    Point("Scatter", "Points on a plane", Color(0xFF8E24AA)),
    Bubble("Bubble", "Points sized by value", Color(0xFFE91E63)),
    Pie("Pie / Donut", "Part-to-whole", Color(0xFF43A047)),
    Combo("Combo", "Bars + line", Color(0xFF3949AB)),
    Multiline("Multiline", "Several line series", Color(0xFF1E88E5)),
    StackedArea("Stacked area", "Cumulative bands", Color(0xFF00897B)),
    Candlestick("Candlestick", "OHLC price data", Color(0xFF6D4C41)),
    Radar("Radar", "Multi-axis profile", Color(0xFFD81B60)),
    Circular("Circular progress", "Concentric rings", Color(0xFFF9A825)),
    Wavy("Wavy", "Wave-styled bars", Color(0xFF7CB342)),
    Block("Block bar", "Proportional blocks", Color(0xFF5E35B1)),
    Calendar("Calendar heatmap", "Values over days", Color(0xFF039BE5)),
}

/**
 * The web-only entry point: an interactive playground where every control maps to a real Charty
 * config or data change and the chart re-renders live. Also exposes the static gallery via a tab.
 */
@Composable
fun WebApp() {
    var dark by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(WebTab.Playground) }
    var family by remember { mutableStateOf<PlaygroundFamily?>(null) }

    MaterialTheme(
        colorScheme =
            if (dark) {
                darkColorScheme()
            } else {
                lightColorScheme()
            },
    ) {
        ChartyThemeProvider(
            theme =
                if (dark) {
                    ChartyTheme.dark()
                } else {
                    ChartyTheme.light()
                },
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(modifier = Modifier.fillMaxSize()) {
                    WebTopBar(
                        tab = tab,
                        onTab = { tab = it },
                        showBack = tab == WebTab.Playground && family != null,
                        backTitle = family?.title,
                        onBack = { family = null },
                        dark = dark,
                        onToggleDark = { dark = !dark },
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when {
                            tab == WebTab.Gallery -> AllChartsShowcase(modifier = Modifier.fillMaxSize())
                            family != null -> PlaygroundContent(family = family!!)
                            else -> PlaygroundHome(onOpen = { family = it })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebTopBar(
    tab: WebTab,
    onTab: (WebTab) -> Unit,
    showBack: Boolean,
    backTitle: String?,
    onBack: () -> Unit,
    dark: Boolean,
    onToggleDark: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            Text(
                text = "‹ $backTitle",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp),
            )
        } else {
            Text(text = "Charty", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.weight(1f))
        TabPill(label = "Playground", selected = tab == WebTab.Playground, onClick = { onTab(WebTab.Playground) })
        Spacer(modifier = Modifier.width(6.dp))
        TabPill(label = "Gallery", selected = tab == WebTab.Gallery, onClick = { onTab(WebTab.Gallery) })
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text =
                if (dark) {
                    "☀︎"
                } else {
                    "☾"
                },
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onToggleDark)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color =
            if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ).clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 6.dp),
    )
}

@Composable
private fun PlaygroundHome(onOpen: (PlaygroundFamily) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpanFull() }) {
            Column {
                Text(text = "Playground", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = "Pick a chart, then tweak its data and configuration live.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        items(PlaygroundFamily.entries.toList()) { family ->
            FamilyCard(family = family, onClick = { onOpen(family) })
        }
    }
}

private fun GridItemSpanFull() =
    androidx.compose.foundation.lazy.grid
        .GridItemSpan(currentLineSpan = Int.MAX_VALUE)

@Composable
private fun FamilyCard(
    family: PlaygroundFamily,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(family.accent.copy(alpha = 0.12f))
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(50)).background(family.accent))
        Column {
            Text(text = family.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = family.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlaygroundContent(family: PlaygroundFamily) {
    when (family) {
        PlaygroundFamily.Line -> LinePlayground()
        PlaygroundFamily.Area -> AreaPlayground()
        PlaygroundFamily.Bar -> BarPlayground()
        PlaygroundFamily.Point -> PointPlayground()
        PlaygroundFamily.Bubble -> BubblePlayground()
        PlaygroundFamily.Pie -> PiePlayground()
        PlaygroundFamily.Combo -> ComboPlayground()
        PlaygroundFamily.Multiline -> MultilinePlayground()
        PlaygroundFamily.StackedArea -> StackedAreaPlayground()
        PlaygroundFamily.Candlestick -> CandlestickPlayground()
        PlaygroundFamily.Radar -> RadarPlayground()
        PlaygroundFamily.Circular -> CircularPlayground()
        PlaygroundFamily.Wavy -> WavyPlayground()
        PlaygroundFamily.Block -> BlockPlayground()
        PlaygroundFamily.Calendar -> CalendarPlayground()
    }
}
