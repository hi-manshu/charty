package com.himanshoe.docsite

private const val FAVICON =
    "data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'>" +
        "<rect width='32' height='32' rx='8' fill='black'/>" +
        "<path d='M8 21 L14 13 L19 17 L25 8' fill='none' stroke='white' stroke-width='2.6' " +
        "stroke-linecap='round' stroke-linejoin='round'/>" +
        "<circle cx='25' cy='8' r='2.6' fill='white'/></svg>"

/**
 * The Charty mark: a rising series with its latest point called out.
 *
 * Drawn in `currentColor` rather than a fixed colour, so it is black on the light theme and white on
 * the dark one without a second asset — which is also what stops it going invisible if the palette
 * ever changes underneath it.
 */
private const val LOGO_MARK =
    """<svg class="logo-mark" viewBox="0 0 28 28" width="26" height="26" aria-hidden="true">""" +
        """<path d="M3 20 L10.5 10.5 L16 15.5 L24 5" fill="none" stroke="currentColor" """ +
        """stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>""" +
        """<circle cx="24" cy="5" r="2.6" fill="currentColor"/></svg>"""

/** `../` repeated enough times to climb back to the site root from [depth]. */
fun rootPrefix(depth: Int): String = "../".repeat(depth)

/**
 * The shell every page shares: head, top bar, sidebar, content column, on-page contents.
 *
 * Every link is written relative to the page rather than absolute from the site root. The site is
 * served from a project path (`/charty/`), not a domain root, so an absolute `/assets/docs.css`
 * would resolve to the wrong place — and would work locally while failing only once deployed, which
 * is the worst way for a link to be wrong.
 */
fun renderPage(
    title: String,
    description: String,
    context: PageContext,
    navigation: String,
    contents: String,
    body: String,
    landing: Boolean = false,
): String {
    val root = rootPrefix(context.depth)
    return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$title</title>
<meta name="description" content="${escapeAttribute(description)}">
<meta property="og:title" content="${escapeAttribute(title)}">
<meta property="og:description" content="${escapeAttribute(description)}">
<meta name="theme-color" content="#6650a4">
<link rel="icon" href="$FAVICON">
<link rel="stylesheet" href="${root}assets/docs.css">
<script>
// Applied before the first paint. Reading the stored theme from a deferred script instead would
// paint the default first and then correct it, which is the flash of the wrong colours that makes a
// docs site feel cheap on every single navigation.
(function () {
  try {
    var stored = localStorage.getItem('charty-theme');
    var dark = stored ? stored === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches;
    document.documentElement.dataset.theme = dark ? 'dark' : 'light';
  } catch (e) {
    document.documentElement.dataset.theme = 'light';
  }
})();
</script>
</head>
<body>
<a class="skip" href="#content">Skip to content</a>
<header class="topbar">
  <button class="menu-toggle" type="button" aria-label="Toggle navigation" aria-expanded="false">☰</button>
  <a class="brand" href="${root}index.html">
    $LOGO_MARK
    <span class="brand-name">charty</span>
  </a>
  <div class="search">
    <span class="search-icon" aria-hidden="true">⌕</span>
    <input type="search" id="search-input" placeholder="Search documentation" autocomplete="off"
           aria-label="Search the documentation" aria-controls="search-results">
    <kbd class="search-hint">/</kbd>
    <div id="search-results" class="search-results" role="listbox" hidden></div>
  </div>
  <nav class="topbar-links">
    <a href="${root}playground/index.html">Playground</a>
    <a href="https://github.com/hi-manshu/charty" rel="noopener">GitHub</a>
    <a class="sponsor-link" href="https://github.com/sponsors/hi-manshu" rel="noopener">
      <span aria-hidden="true">♥</span> Sponsor
    </a>
    <button class="theme-toggle" type="button" aria-label="Switch between light and dark"></button>
  </nav>
</header>
${renderShell(landing = landing, navigation = navigation, contents = contents, body = body, root = root)}
<script src="${root}assets/docs.js" data-root="$root"></script>
</body>
</html>
""".trimStart()
}

/**
 * The page's body: a documentation page keeps its sidebar and contents column, a landing page has
 * neither and runs the full width.
 *
 * A landing page carrying a docs sidebar is the thing that makes a project's front door read as an
 * internal page someone forgot to design. The two really are different layouts, so they are written
 * as two rather than as one with parts hidden.
 */
private fun renderShell(
    landing: Boolean,
    navigation: String,
    contents: String,
    body: String,
    root: String,
): String =
    if (landing) {
        """<main class="landing" id="content">$body</main>${renderFooter(root)}"""
    } else {
        """<div class="layout">
  <aside class="sidebar" id="sidebar">$navigation</aside>
  <main class="content" id="content">$body</main>
  $contents
</div>"""
    }

/** The landing page's footer. Documentation pages end at their content and do not want one. */
private fun renderFooter(root: String): String =
    """
<footer class="footer">
  <div class="footer-inner">
    <div>
      <span class="footer-brand">$LOGO_MARK<strong>charty</strong></span>
      <p>Charts for Compose Multiplatform, by <a href="https://github.com/hi-manshu">Himanshu Singh</a>.</p>
      <p class="footer-license">Apache 2.0</p>
    </div>
    <div>
      <h3>Docs</h3>
      <a href="${root}getting-started/installation.html">Installation</a>
      <a href="${root}getting-started/quick-start.html">Quick start</a>
      <a href="${root}guides/streaming.html">Streaming</a>
      <a href="${root}customization/theming.html">Theming</a>
    </div>
    <div>
      <h3>Charts</h3>
      <a href="${root}charts/bar/BarChart.html">Bar</a>
      <a href="${root}charts/line/LineChart.html">Line</a>
      <a href="${root}charts/radial/PieChart.html">Pie</a>
      <a href="${root}charts/3d/index.html">3D</a>
    </div>
    <div>
      <h3>Project</h3>
      <a href="${root}playground/index.html">Playground</a>
      <a href="https://github.com/sponsors/hi-manshu">Sponsor on GitHub</a>
      <a href="https://buymeacoffee.com/himanshoe">Buy me a coffee</a>
      <a href="https://github.com/hi-manshu/charty">GitHub</a>
      <a href="https://central.sonatype.com/artifact/com.himanshoe/charty">Maven Central</a>
      <a href="https://github.com/hi-manshu/charty/blob/main/CHANGELOG.md">Changelog</a>
    </div>
  </div>
</footer>
"""

/**
 * The sidebar: collapsible groups, with the current page marked.
 *
 * Built from `<details>` rather than a scripted accordion, so the groups open and close with no
 * JavaScript at all — which also means they still work while the page is loading and for anyone who
 * has scripting turned off. The group holding the current page is the one that starts open, so a
 * reader always lands with their surroundings visible and the other eight sections folded away.
 */
fun renderNavigation(
    sections: List<NavSection>,
    currentUrl: String,
    depth: Int,
): String {
    val root = rootPrefix(depth)
    val html = StringBuilder()
    html.append("""<nav aria-label="Documentation">""")
    html.append(
        """<a class="nav-home${activeSuffix(currentUrl == "index.html")}" href="${root}index.html">""" +
            """<span class="nav-icon" aria-hidden="true">◆</span>Overview</a>""",
    )
    html.append(
        """<a class="nav-home" href="${root}playground/index.html">""" +
            """<span class="nav-icon" aria-hidden="true">▶</span>Playground</a>""",
    )
    sections.forEach { section ->
        val holdsCurrent = section.entries.any { entry -> entry.path.toSiteUrl() == currentUrl }
        val open =
            if (holdsCurrent) {
                " open"
            } else {
                ""
            }
        html.append("""<details class="nav-section"$open><summary>${escapeText(section.title)}</summary><ul>""")
        section.entries.forEach { entry ->
            val url = entry.path.toSiteUrl()
            val active = activeSuffix(url == currentUrl)
            html.append("""<li><a class="nav-link$active" href="$root$url">${escapeText(entry.title)}</a></li>""")
        }
        html.append("</ul></details>")
    }
    html.append("</nav>")
    return html.toString()
}

private fun activeSuffix(active: Boolean): String =
    if (active) {
        " is-active"
    } else {
        ""
    }

/**
 * The invitation to open this chart in the playground.
 *
 * Sits at the top of the page, because a reader deciding whether a chart suits them is better served
 * by turning its knobs than by reading the next paragraph. It is a plain link, so it is a real
 * navigation: the browser's Back returns to this page with no history handling of our own.
 */
fun renderPlaygroundLink(
    family: String?,
    chartName: String,
    depth: Int,
): String {
    if (family == null) {
        return ""
    }
    val root = rootPrefix(depth)
    // The slot keeps the card's height in the flow. When the card lifts out to become a floating
    // button the page must not jump by its height, which is what happens if the element simply
    // leaves the layout.
    return """<div class="try-it-slot" data-try-it-slot>""" +
        """<a class="try-it" href="${root}playground/?chart=$family">""" +
        """<span class="try-it-icon" aria-hidden="true">▶</span>""" +
        """<span class="try-it-text"><strong>Experiment with ${escapeText(chartName)}</strong>""" +
        """<span class="try-it-sub">Open it in the playground and turn every option</span></span>""" +
        """<span class="try-it-go" aria-hidden="true">→</span></a></div>"""
}

/** The on-page contents, omitted entirely when a page has too few headings to need one. */
fun renderContents(headings: List<Heading>): String {
    if (headings.size < 2) {
        return ""
    }
    val links =
        headings.joinToString("") { heading ->
            """<li><a href="#${heading.id}">${escapeText(heading.text)}</a></li>"""
        }
    return """<aside class="toc"><h2>On this page</h2><ul>$links</ul></aside>"""
}

/**
 * The pair of cards that close a documentation page, pointing at the pages either side of it in the
 * sidebar's order.
 *
 * Reading documentation straight through is a real way to use it, and without these a reader who
 * finishes a page has to go back to the sidebar and work out where they were.
 */
fun renderPagination(
    previous: NavEntry?,
    next: NavEntry?,
    depth: Int,
): String {
    if (previous == null && next == null) {
        return ""
    }
    val root = rootPrefix(depth)
    val previousCard =
        previous?.let { entry ->
            """<a class="page-nav-card" href="$root${entry.path.toSiteUrl()}">""" +
                """<span class="page-nav-arrow" aria-hidden="true">←</span>""" +
                """<span class="page-nav-text"><span class="page-nav-label">Previous</span>""" +
                """<span class="page-nav-title">${escapeText(entry.title)}</span></span></a>"""
        } ?: """<span></span>"""
    val nextCard =
        next?.let { entry ->
            """<a class="page-nav-card page-nav-card--next" href="$root${entry.path.toSiteUrl()}">""" +
                """<span class="page-nav-text"><span class="page-nav-label">Next</span>""" +
                """<span class="page-nav-title">${escapeText(entry.title)}</span></span>""" +
                """<span class="page-nav-arrow" aria-hidden="true">→</span></a>"""
        } ?: """<span></span>"""
    return """<nav class="page-nav" aria-label="Previous and next page">$previousCard$nextCard</nav>"""
}

/** The site path a markdown file is published at. */
fun String.toSiteUrl(): String = replace("README.md", "index.html").replace(".md", ".html")

/** How deep in the site a path sits, for building its relative links back to the root. */
fun String.urlDepth(): Int = count { character -> character == '/' }

/** HTML-escapes text destined for an element's content. */
fun escapeText(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

/** HTML-escapes text destined for an attribute value, where a quote would end the attribute. */
fun escapeAttribute(value: String): String = escapeText(value).replace("\"", "&quot;")
