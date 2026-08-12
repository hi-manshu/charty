@file:Suppress(
    "MagicNumber",
    "LongMethod",
    "FunctionNaming",
    "UndocumentedPublicFunction",
    "MaxLineLength",
    "CyclomaticComplexMethod",
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth > 720.dp
        if (wide) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        chart()
                    }
                    CodePanel(
                        code = code,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    190.dp,
                                ).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    )
                }
                Column(
                    modifier =
                        Modifier
                            .width(340.dp)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = controls,
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp).padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    chart()
                }
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    controls()
                    CodePanel(code = code, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            }
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
