package com.himanshoe.sample

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)
private val PaperRaised = Color(0xFFFAFAFA)
private val PaperSunken = Color(0xFFF2F2F2)
private val PaperLine = Color(0xFFEAEAEA)
private val PaperMuted = Color(0xFF525252)
private val InkRaised = Color(0xFF0A0A0A)
private val InkSunken = Color(0xFF161616)
private val InkLine = Color(0xFF262626)
private val InkMuted = Color(0xFFA1A1A1)

/**
 * The playground's chrome: black, white and grey, matching the documentation site it sits inside.
 *
 * Monochrome for the same reason the site is. Every chart on this page is already carrying five or
 * six colours, and they are the thing a reader is here to judge — a purple toolbar and purple
 * sliders around them are one more palette competing with the one that matters. Controls in ink and
 * paper leave the charts as the only coloured thing on screen.
 *
 * The chart colours themselves are untouched: they come from Charty's own palette and from whatever
 * a reader picks in the controls.
 */
internal fun playgroundLightScheme(): ColorScheme =
    lightColorScheme(
        primary = Black,
        onPrimary = White,
        primaryContainer = PaperSunken,
        onPrimaryContainer = Black,
        secondary = PaperMuted,
        onSecondary = White,
        secondaryContainer = PaperSunken,
        onSecondaryContainer = Black,
        background = White,
        onBackground = Black,
        surface = White,
        onSurface = Black,
        surfaceVariant = PaperRaised,
        onSurfaceVariant = PaperMuted,
        surfaceContainer = PaperRaised,
        surfaceContainerHigh = PaperSunken,
        surfaceContainerHighest = PaperSunken,
        outline = PaperLine,
        outlineVariant = PaperLine,
    )

/** The dark half of [playgroundLightScheme]. */
internal fun playgroundDarkScheme(): ColorScheme =
    darkColorScheme(
        primary = White,
        onPrimary = Black,
        primaryContainer = InkSunken,
        onPrimaryContainer = White,
        secondary = InkMuted,
        onSecondary = Black,
        secondaryContainer = InkSunken,
        onSecondaryContainer = White,
        background = Black,
        onBackground = White,
        surface = Black,
        onSurface = White,
        surfaceVariant = InkRaised,
        onSurfaceVariant = InkMuted,
        surfaceContainer = InkRaised,
        surfaceContainerHigh = InkSunken,
        surfaceContainerHighest = InkLine,
        outline = InkLine,
        outlineVariant = InkLine,
    )
