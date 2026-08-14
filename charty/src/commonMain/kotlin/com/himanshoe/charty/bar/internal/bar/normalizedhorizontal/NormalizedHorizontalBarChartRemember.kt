package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor

/**
 * Resolves [colors] into the concrete list the drawer paints with, once per palette change.
 *
 * It used to take the data as well and key on it, which meant every change to the data threw the
 * palette away and rebuilt an identical list — the colours do not depend on the values, and keying on
 * them only defeated the memo it exists to provide.
 */
@Composable
internal fun rememberNormalizedHorizontalColors(colors: ChartyColor): List<Color> = remember(colors) { colors.value }
