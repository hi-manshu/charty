package com.himanshoe.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.localStorage
import kotlinx.browser.window

private const val THEME_KEY = "charty-theme"
private const val DARK = "dark"
private const val LIGHT = "light"

/**
 * Whether the playground is dark, and how a reader changes it.
 *
 * The playground opened light whatever the reader had chosen, which is jarring twice over: it is
 * reached from a documentation site that remembers a preference, and it ignores the one the
 * operating system already publishes. Both are answered here, in the order a reader would expect —
 * a choice they made on the site wins over the machine's default, because it is more specific and
 * more recent.
 *
 * The key is the site's own, so the two halves of the same site agree rather than each keeping a
 * private opinion.
 */
class PlaygroundTheme(
    initiallyDark: Boolean,
) {
    /** Whether the playground is currently showing its dark theme. */
    var dark by mutableStateOf(initiallyDark)
        private set

    /** Flips the theme and records the choice, so it survives a reload and reaches the docs. */
    fun toggle() {
        dark = !dark
        val value =
            if (dark) {
                DARK
            } else {
                LIGHT
            }
        runCatching { localStorage.setItem(key = THEME_KEY, value = value) }
    }

    /** Applies a preference that changed elsewhere, without writing it back. */
    fun follow(isDark: Boolean) {
        dark = isDark
    }
}

/**
 * Remembers the theme and keeps following the system while the reader has expressed no preference.
 *
 * Once someone has chosen, the system no longer overrides them — a reader who picked light at ten in
 * the morning did not ask to be flipped when their machine turns dark at six.
 */
@Composable
fun rememberPlaygroundTheme(): PlaygroundTheme {
    val theme = remember { PlaygroundTheme(initiallyDark = preferredIsDark()) }

    DisposableEffect(theme) {
        val query = runCatching { window.matchMedia("(prefers-color-scheme: dark)") }.getOrNull()
        val listener: (org.w3c.dom.events.Event) -> Unit = {
            if (storedPreference() == null) {
                theme.follow(isDark = query?.matches == true)
            }
        }
        query?.addEventListener(type = "change", callback = listener)
        onDispose { query?.removeEventListener(type = "change", callback = listener) }
    }
    return theme
}

/** The stored choice, or `null` when the reader has never made one. */
private fun storedPreference(): String? =
    runCatching { localStorage.getItem(THEME_KEY) }
        .getOrNull()
        ?.takeIf { value -> value == DARK || value == LIGHT }

/** The theme to open with: the reader's stored choice, else what the operating system asks for. */
private fun preferredIsDark(): Boolean {
    val stored = storedPreference()
    if (stored != null) {
        return stored == DARK
    }
    return runCatching { window.matchMedia("(prefers-color-scheme: dark)").matches }.getOrDefault(false)
}
