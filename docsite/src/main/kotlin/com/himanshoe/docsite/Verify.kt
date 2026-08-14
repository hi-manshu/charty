package com.himanshoe.docsite

import java.io.File

private val hrefPattern = Regex("""(?:href|src)="([^"]+)"""")

/**
 * The playground is not built by this generator — the deploy assembles it in beside the docs — so
 * its pages are absent here and every link to them would look broken. Skipping the prefix is
 * narrower and more honest than skipping missing files in general, which would hide the real thing
 * this check exists to catch.
 */
private const val ASSEMBLED_LATER = "playground/"

/**
 * Fails the build on a link or image that points at nothing.
 *
 * Worth doing here rather than trusting the rewrite, because a broken link is invisible in a
 * generated site: the page still builds, still renders, still looks finished, and the mistake is
 * found by a reader. The one thing a generator can do that a reader cannot is check every link on
 * every page every time, so it does.
 */
fun verifyLinks(outputDir: File) {
    val broken = mutableListOf<String>()
    outputDir
        .walkTopDown()
        .filter { file -> file.extension == "html" }
        .forEach { page -> collectBroken(outputDir = outputDir, page = page, into = broken) }

    check(broken.isEmpty()) {
        "The generated site has ${broken.size} dead link(s):\n" + broken.joinToString("\n") { line -> "  $line" }
    }
}

private fun collectBroken(
    outputDir: File,
    page: File,
    into: MutableList<String>,
) {
    val html = page.readText()
    hrefPattern.findAll(html).forEach { match ->
        val target = match.groupValues[1].substringBefore('#')
        if (isCheckable(target)) {
            val resolved = page.parentFile.resolve(target).normalize()
            if (!resolved.exists()) {
                into.add("${page.relativeTo(outputDir)} -> $target")
            }
        }
    }
}

private fun isCheckable(target: String): Boolean {
    val external =
        target.isEmpty() ||
            target.startsWith("http") ||
            target.startsWith("mailto:") ||
            target.startsWith("data:")
    return !external && !target.contains(ASSEMBLED_LATER)
}
