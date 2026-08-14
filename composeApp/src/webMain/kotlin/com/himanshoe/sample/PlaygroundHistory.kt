package com.himanshoe.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window

private const val CHART_PARAMETER = "chart"

/**
 * The playground's navigation, held in the browser's history rather than only in composition.
 *
 * Without this, opening a chart changes what is on screen but not where the browser thinks it is, so
 * Back leaves the site altogether — a reader who taps it to return to the gallery loses the
 * playground instead. Compose has no opinion about browser history, so the two have to be joined by
 * hand.
 *
 * Opening pushes an entry and Back pops it. The in-app back control does not clear the state itself;
 * it asks the browser to go back, and the resulting `popstate` is what changes the selection. That
 * way there is one path through this code rather than two, and the in-app control and the browser
 * button cannot drift into disagreeing.
 *
 * The selection also lives in the URL, which makes every chart linkable: a reader can send someone
 * `?chart=Line` and they arrive on that chart rather than on the gallery with instructions.
 */
internal class PlaygroundNavigation(
    initial: PlaygroundFamily?,
) {
    var family by mutableStateOf(initial)
        private set

    /** Opens [target], adding a history entry so Back returns to whatever was showing. */
    fun open(target: PlaygroundFamily) {
        if (family == target) {
            return
        }
        family = target
        window.history.pushState(data = null, title = "", url = "?$CHART_PARAMETER=${target.name}")
    }

    /**
     * Asks the browser to go back.
     *
     * Deliberately not `family = null`. Clearing it here would leave a history entry pointing at a
     * chart that is no longer open, so the browser's own Back button would then appear to do nothing
     * — the classic broken in-app back button.
     */
    fun back() {
        window.history.back()
    }

    /** Applies a selection that came from the browser, without pushing another entry for it. */
    fun restore(target: PlaygroundFamily?) {
        family = target
    }
}

/**
 * Remembers the playground's navigation and keeps it in step with the browser.
 *
 * The listener is removed when this leaves composition. A listener on `window` outlives the
 * composition that added it, so without that a hot reload — or anything else that recomposes the
 * root — accumulates handlers that all fire on one Back.
 */
@Composable
internal fun rememberPlaygroundNavigation(): PlaygroundNavigation {
    val navigation = remember { PlaygroundNavigation(initial = familyFromLocation()) }

    DisposableEffect(navigation) {
        val listener: (org.w3c.dom.events.Event) -> Unit = { navigation.restore(familyFromLocation()) }
        window.addEventListener(type = "popstate", callback = listener)
        onDispose { window.removeEventListener(type = "popstate", callback = listener) }
    }
    return navigation
}

/**
 * Reads the selected chart out of the address bar.
 *
 * An unrecognised name resolves to no selection rather than to an error, because the name in a URL
 * is whatever a stranger pasted: a link from an older build, or a typo. Landing on the gallery is a
 * reasonable answer to "that chart does not exist"; a crash is not.
 */
private fun familyFromLocation(): PlaygroundFamily? {
    val search = window.location.search.removePrefix("?")
    val value =
        search
            .split("&")
            .firstOrNull { part -> part.startsWith("$CHART_PARAMETER=") }
            ?.substringAfter('=')
            ?: return null
    return PlaygroundFamily.entries.firstOrNull { entry -> entry.name == value }
}
