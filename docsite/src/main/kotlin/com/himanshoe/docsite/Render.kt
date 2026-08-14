package com.himanshoe.docsite

import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

/** A heading a reader can jump to, for the on-page contents. */
data class Heading(
    val id: String,
    val text: String,
    val level: Int,
)

/** A markdown file, rendered. */
data class RenderedPage(
    val title: String,
    val html: String,
    val headings: List<Heading>,
    val searchText: String,
)

private val flavour = GFMFlavourDescriptor()

private val headingPattern = Regex("""<h([1-3])>(.*?)</h\1>""", RegexOption.DOT_MATCHES_ALL)
private val codeBlockPattern =
    Regex("""<pre>(<code[^>]*>)([\s\S]*?)</code></pre>""")
private val linkPattern = Regex("""href="([^"]+)"""")
private val tagPattern = Regex("""<[^>]+>""")

/** Renders one markdown document into the HTML the page template drops into its content column. */
fun renderMarkdown(markdown: String): RenderedPage {
    val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
    val raw = HtmlGenerator(markdown, tree, flavour).generateHtml()
    val body = raw.removePrefix("<body>").removeSuffix("</body>")

    val headings = mutableListOf<Heading>()
    val withAnchors = addHeadingAnchors(html = body, collected = headings)
    val withLinks = rewriteLinks(withAnchors)
    val withCode = highlightCodeBlocks(withLinks)

    return RenderedPage(
        title = headings.firstOrNull { heading -> heading.level == 1 }?.text ?: "Charty",
        html = withCode,
        headings = headings.filter { heading -> heading.level == 2 },
        searchText = plainText(body),
    )
}

/**
 * Gives every heading an id and records it, so the on-page contents can link to it and so a heading
 * can be linked to from anywhere else.
 *
 * The id comes from the heading's own text rather than a counter, because a counter changes meaning
 * the moment a heading is inserted above it and every link that pointed at the old anchor silently
 * lands somewhere else.
 */
private fun addHeadingAnchors(
    html: String,
    collected: MutableList<Heading>,
): String {
    val used = mutableSetOf<String>()
    return headingPattern.replace(html) { match ->
        val level = match.groupValues[1].toInt()
        val inner = match.groupValues[2]
        val text = plainText(inner).trim()
        val id = uniqueSlug(text = text, used = used)
        collected.add(Heading(id = id, text = text, level = level))
        """<h$level id="$id">$inner<a class="anchor" href="#$id" aria-label="Link to this section">#</a></h$level>"""
    }
}

/** A URL-safe form of a heading, made unique within the page by suffixing repeats. */
private fun uniqueSlug(
    text: String,
    used: MutableSet<String>,
): String {
    val base =
        text
            .lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "-")
            .trim('-')
            .ifEmpty { "section" }
    var candidate = base
    var suffix = 2
    while (!used.add(candidate)) {
        candidate = "$base-$suffix"
        suffix++
    }
    return candidate
}

/**
 * Points links at the generated pages.
 *
 * The site mirrors the docs directory, so a relative link between two documents already resolves
 * correctly and only its extension is wrong. That is the whole rewrite — no path rebuilding, which
 * is the part that usually breaks when a page moves. Links to `README.md` become `index.html` so a
 * directory can be linked to as a directory. External links and anchors are left alone.
 */
private fun rewriteLinks(html: String): String =
    linkPattern.replace(html) { match ->
        val href = match.groupValues[1]
        val rewritten =
            when {
                href.startsWith("http") || href.startsWith("#") || href.startsWith("mailto:") -> href
                else -> href.replace("README.md", "index.html").replace(".md", ".html")
            }
        """href="$rewritten""""
    }

/** Colours the Kotlin in every fenced block, numbers its lines, and hangs a copy button off it. */
private fun highlightCodeBlocks(html: String): String =
    codeBlockPattern.replace(html) { match ->
        val openTag = match.groupValues[1]
        val kotlin = openTag.contains("language-kotlin")
        val body = match.groupValues[2].trimEnd('\n')
        val language =
            if (kotlin) {
                "Kotlin"
            } else {
                ""
            }
        codeWindow(
            language = language,
            inner = "<pre>$openTag${numberedLines(code = body, kotlin = kotlin)}</code></pre>",
        )
    }

/**
 * Wraps a code block in a window frame: title bar, three lights, the language, and the copy button.
 *
 * The frame is doing real work rather than decoration. It marks where a snippet begins and ends on a
 * page that is otherwise a single dark column, and it gives the copy button somewhere to live that
 * is always visible — hovering to discover a control only works for readers who already suspect it
 * is there.
 */
private fun codeWindow(
    language: String,
    inner: String,
): String =
    """<div class="code-block"><div class="code-chrome">""" +
        """<span class="code-lights" aria-hidden="true"><i></i><i></i><i></i></span>""" +
        """<span class="code-lang">$language</span>""" +
        """<button class="copy" type="button" aria-label="Copy code">Copy</button></div>$inner</div>"""

/**
 * A standalone Kotlin snippet, drawn exactly as one that came from a markdown fence.
 *
 * The landing page writes its snippets in Kotlin source rather than in markdown, and without this
 * they would render as the only code on the site with no line numbers — a difference a reader
 * notices as sloppiness long before they work out what it is.
 */
fun renderKotlinBlock(code: String): String =
    codeWindow(
        language = "Kotlin",
        inner =
            """<pre><code class="language-kotlin">""" +
                numberedLines(code = escapeText(code), kotlin = true) +
                """</code></pre>""",
    )

/**
 * Wraps each line in its own element so the stylesheet can number them.
 *
 * Highlighting happens per line rather than across the whole block, which means a construct
 * spanning several lines is coloured a line at a time. That is the right trade: a span that opened
 * on one line and closed on another would have to be cut in half to insert the line wrapper, and
 * broken markup is a worse outcome than an unstyled block comment. The docs use only `//` comments,
 * so nothing in them spans lines anyway.
 */
private fun numberedLines(
    code: String,
    kotlin: Boolean,
): String =
    code
        .split("\n")
        .joinToString("") { line ->
            val painted =
                if (kotlin) {
                    highlightKotlin(line)
                } else {
                    line
                }
            """<span class="line">$painted</span>"""
        }

/** The visible words of a fragment of HTML, for titles and the search index. */
private fun plainText(html: String): String =
    tagPattern
        .replace(html, " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .replace(Regex("""\s+"""), " ")
        .trim()
