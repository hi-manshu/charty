@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
    "TooManyFunctions",
    "MultipleEmitters",
)

package com.himanshoe.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.config.CornerRadius
import kotlin.math.roundToInt
import kotlin.random.Random

private val codeBackground = Color(0xFF1E1E2E)
private val codeForeground = Color(0xFFE4E6F1)
private val codeLabel = Color(0xFF9AA0B4)

/** Formats a [Color] as a Kotlin `0xAARRGGBB` literal for the code panel. */
internal fun colorHex(color: Color): String =
    "0x" +
        color
            .toArgb()
            .toUInt()
            .toString(radix = 16)
            .uppercase()
            .padStart(8, '0')

/** Formats a [Float] as a Kotlin float literal (e.g. `3f`, `0.6f`) for the code panel. */
internal fun fc(value: Float): String {
    val rounded = (value * 100).roundToInt() / 100.0
    val text =
        if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    return text + "f"
}

/** Accent colors offered by the playground color pickers. */
internal val playgroundPalette: List<Color> =
    listOf(
        Color(0xFF2962FF),
        Color(0xFFE91E63),
        Color(0xFF00BFA5),
        Color(0xFFFF6D00),
        Color(0xFF8E24AA),
        Color(0xFF43A047),
        Color(0xFFF9A825),
        Color(0xFF3949AB),
    )

/** Deterministic-per-tick random value series in `[10, 100]`; bump [tick] to reshuffle. */
internal fun randomValues(
    count: Int,
    tick: Int,
    min: Float = 10f,
    max: Float = 100f,
): List<Float> {
    val random = Random(seed = tick * 1000 + count)
    return List(count) { min + random.nextFloat() * (max - min) }
}

/**
 * Two-pane playground layout: the live chart and a scrollable controls panel. Side-by-side on wide
 * viewports (desktop/web), stacked on narrow ones (mobile).
 */
@Composable
internal fun PlaygroundScaffold(
    chart: @Composable () -> Unit,
    code: String,
    controls: @Composable ColumnScope.() -> Unit,
    cartesian: Boolean = true,
) {
    val shared = LocalPlaygroundShared.current
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth > 860.dp
        if (wide) {
            Row(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ChartStage(chart = chart, modifier = Modifier.weight(1f).fillMaxWidth())
                    CodePanel(code = code, modifier = Modifier.fillMaxWidth().height(220.dp))
                }
                ControlPanel(
                    shared = shared,
                    controls = controls,
                    cartesian = cartesian,
                    modifier = Modifier.width(360.dp).fillMaxHeight(),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ChartStage(chart = chart, modifier = Modifier.fillMaxWidth().height(300.dp))
                ControlPanel(
                    shared = shared,
                    controls = controls,
                    cartesian = cartesian,
                    modifier = Modifier.fillMaxWidth(),
                    scrolls = false,
                )
                CodePanel(code = code, modifier = Modifier.fillMaxWidth().height(240.dp))
            }
        }
    }
}

/** The surface the live chart sits on, so it reads as a artefact rather than loose ink on the page. */
@Composable
private fun ChartStage(
    chart: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            chart()
        }
    }
}

/**
 * The right-hand panel: this chart's own controls first, then the settings the chart actually
 * honours, so the shared knobs sit in the same place whichever chart is open.
 *
 * A chart that draws no [com.himanshoe.charty.common.ChartScaffold] — the radial family, and the
 * projected 3D charts — is not [cartesian], and is spared the axis and tooltip sections it would
 * ignore. Showing a control that does nothing is worse than showing no control at all.
 */
@Composable
private fun ControlPanel(
    shared: PlaygroundSharedState,
    controls: @Composable ColumnScope.() -> Unit,
    cartesian: Boolean,
    modifier: Modifier = Modifier,
    scrolls: Boolean = true,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (scrolls) {
                            Modifier.verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        },
                    ).padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { shared.reset() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
            controls()
            if (cartesian) {
                AxisAndGridControls(state = shared)
                TooltipStyleControls(state = shared)
            }
            ThemeControls(state = shared)
        }
    }
}

@Composable
private fun CodePanel(
    code: String,
    modifier: Modifier = Modifier,
) {
    if (code.isBlank()) {
        return
    }
    val clipboard = LocalClipboardManager.current
    Column(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(codeBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "CODE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = codeLabel,
            )
            Text(
                text = "Copy",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = codeForeground,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3B3B54))
                        .clickable { clipboard.setText(AnnotatedString(code)) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        ) {
            Text(text = code, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = codeForeground)
        }
    }
}

@Composable
internal fun ControlSection(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
internal fun SliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    decimals: Int = 1,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatFloat(value = value, decimals = decimals),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
internal fun IntSliderRow(
    label: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
        )
    }
}

@Composable
internal fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun <T> ChoiceRow(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                val isSelected = option == selected
                Text(
                    text = labelOf(option),
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            ).clickable { onSelect(option) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
internal fun ColorRow(
    label: String,
    selected: Color,
    onSelect: (Color) -> Unit,
    palette: List<Color> = playgroundPalette,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            palette.forEach { color ->
                Box(
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                border =
                                    BorderStroke(
                                        width = if (color == selected) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                shape = CircleShape,
                            ).clickable { onSelect(color) },
                )
            }
        }
    }
}

@Composable
internal fun PlaygroundActionRow(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlaygroundButton(label = primaryLabel, onClick = onPrimary, modifier = Modifier.weight(1f))
        if (secondaryLabel != null && onSecondary != null) {
            PlaygroundButton(label = secondaryLabel, onClick = onSecondary, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlaygroundButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
    )
}

/** A quieter heading for a group of related controls nested under a [ControlSection]. */
@Composable
internal fun ControlSubSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp),
    )
}

private const val CUSTOM_CORNER_LABEL = "Custom"

/**
 * Corner-radius picker: the [CornerRadius] presets plus a `Custom` option that reveals a free
 * slider feeding [CornerRadius.Custom].
 */
@Composable
internal fun CornerRadiusControls(
    label: String,
    corner: CornerRadius,
    onCornerChange: (CornerRadius) -> Unit,
) {
    var customRadius by remember { mutableStateOf(DEFAULT_CUSTOM_CORNER_RADIUS) }
    val options = cornerOptions.map(::cornerLabel) + CUSTOM_CORNER_LABEL
    val selected =
        if (corner is CornerRadius.Custom) {
            CUSTOM_CORNER_LABEL
        } else {
            cornerLabel(corner)
        }
    ChoiceRow(
        label = label,
        options = options,
        selected = selected,
        labelOf = { it },
        onSelect = { choice ->
            if (choice == CUSTOM_CORNER_LABEL) {
                onCornerChange(CornerRadius.Custom(radius = customRadius))
            } else {
                onCornerChange(cornerOptions[options.indexOf(choice)])
            }
        },
    )
    if (corner is CornerRadius.Custom) {
        SliderRow(
            label = "Custom radius",
            value = customRadius,
            valueRange = 0f..48f,
            onValueChange = {
                customRadius = it
                onCornerChange(CornerRadius.Custom(radius = it))
            },
            decimals = 0,
        )
    }
}

private const val DEFAULT_CUSTOM_CORNER_RADIUS = 22f

/**
 * The consumer-supplied placeholder handed to a chart's `emptyContent` slot, so the playground
 * shows that the slot is fully yours to design.
 */
@Composable
internal fun ChartEmptyState(onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "📭", fontSize = 40.sp)
        Text(
            text = "Nothing to plot",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "emptyContent is your slot — any composable goes here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "↺ Load sample data",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onAction)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

private const val EMPTY_BUILT_IN = "Built-in"
private const val EMPTY_CUSTOM = "Custom"

/** Switch that forces the data list empty, plus the built-in / custom placeholder chooser. */
@Composable
internal fun EmptyStateControls(
    forceEmpty: Boolean,
    onForceEmptyChange: (Boolean) -> Unit,
    custom: Boolean,
    onCustomChange: (Boolean) -> Unit,
) {
    ControlSection(title = "Empty state")
    SwitchRow(label = "Force empty data", checked = forceEmpty, onCheckedChange = onForceEmptyChange)
    ChoiceRow(
        label = "Placeholder",
        options = listOf(EMPTY_BUILT_IN, EMPTY_CUSTOM),
        selected =
            if (custom) {
                EMPTY_CUSTOM
            } else {
                EMPTY_BUILT_IN
            },
        labelOf = { it },
        onSelect = { onCustomChange(it == EMPTY_CUSTOM) },
    )
}

/** Enable switch and value slider for a reference line; the caller builds the config. */
@Composable
internal fun ReferenceLineControls(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    SwitchRow(label = "Reference line", checked = enabled, onCheckedChange = onEnabledChange)
    if (enabled) {
        SliderRow(
            label = "Line value",
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            decimals = 0,
        )
    }
}

/** Enable switch and low/high sliders for a reference band; the caller builds the config. */
@Composable
internal fun ReferenceBandControls(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    low: Float,
    onLowChange: (Float) -> Unit,
    high: Float,
    onHighChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
) {
    SwitchRow(label = "Reference band", checked = enabled, onCheckedChange = onEnabledChange)
    if (enabled) {
        SliderRow(
            label = "Band low",
            value = low,
            valueRange = valueRange,
            onValueChange = onLowChange,
            decimals = 0,
        )
        SliderRow(
            label = "Band high",
            value = high,
            valueRange = valueRange,
            onValueChange = onHighChange,
            decimals = 0,
        )
    }
}

/** Enable switch and size slider for a rolling visible window; the caller applies `visibleWindow`. */
@Composable
internal fun VisibleWindowControls(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    size: Int,
    onSizeChange: (Int) -> Unit,
    sizeRange: IntRange,
) {
    SwitchRow(label = "Visible window", checked = enabled, onCheckedChange = onEnabledChange)
    if (enabled) {
        IntSliderRow(label = "Window size", value = size, valueRange = sizeRange, onValueChange = onSizeChange)
    }
}

/** The `emptyContent` argument for a chart: [ChartEmptyState] when custom, `null` for the built-in. */
internal fun emptyPlaceholder(
    custom: Boolean,
    onAction: () -> Unit,
): (@Composable () -> Unit)? =
    if (custom) {
        { ChartEmptyState(onAction = onAction) }
    } else {
        null
    }

/** A one-entry legend label list when enabled, otherwise the empty default. */
internal fun legendLabelsOf(enabled: Boolean): List<String> =
    if (enabled) {
        listOf("Series A")
    } else {
        emptyList()
    }

/** The nullable `downsampleThreshold` / `visibleWindow` style argument. */
internal fun thresholdOf(
    enabled: Boolean,
    value: Int,
): Int? =
    if (enabled) {
        value
    } else {
        null
    }

/** The nullable `visibleWindow` argument. */
internal fun windowOf(
    enabled: Boolean,
    size: Int,
): Int? =
    if (enabled) {
        size
    } else {
        null
    }

/** Renders one optional argument line for a code snippet, indented to sit inside the call. */
internal fun codeArg(
    include: Boolean,
    text: String,
    indent: Int = 12,
): String =
    if (include) {
        "\n" + " ".repeat(indent) + text
    } else {
        ""
    }

/** The `emptyContent = { … }` snippet line, shown only when the custom placeholder is selected. */
internal fun emptyContentArg(custom: Boolean): String =
    codeArg(include = custom, text = "emptyContent = { ChartEmptyState(onAction = { }) },")

private fun formatFloat(
    value: Float,
    decimals: Int,
): String {
    if (decimals <= 0) {
        return value.roundToInt().toString()
    }
    var factor = 1
    repeat(decimals) { factor *= 10 }
    val rounded = (value * factor).roundToInt().toString()
    val padded = rounded.padStart(decimals + 1, '0')
    val dot = padded.length - decimals
    return padded.substring(0, dot) + "." + padded.substring(dot)
}
