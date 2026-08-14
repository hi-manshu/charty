package com.himanshoe.docsite

import java.io.File

private const val EXPECTED_ARGUMENTS = 3

/**
 * Renders the documentation into a static site.
 *
 * Usage: `generateDocsSite <docsDir> <outputDir> <brandDir>`.
 */
fun main(args: Array<String>) {
    require(args.size == EXPECTED_ARGUMENTS) {
        "Expected <docsDir> <outputDir> <brandDir>, got ${args.size} argument(s)"
    }
    val docsDir = File(args[0])
    val outputDir = File(args[1])
    val brandDir = File(args[2])

    outputDir.deleteRecursively()
    outputDir.mkdirs()

    val pages = buildPages(docsDir = docsDir)
    val order = siteNavigation.flatMap { section -> section.entries }
    pages.forEachIndexed { index, page ->
        writePage(
            outputDir = outputDir,
            page = page,
            previous = order.getOrNull(index - 1),
            next = order.getOrNull(index + 1),
        )
    }
    writeHome(outputDir = outputDir, catalog = buildCatalog(docsDir = docsDir))
    copyImages(docsDir = docsDir, outputDir = outputDir)
    copyBrand(brandDir = brandDir, outputDir = outputDir)
    copyAssets(outputDir = outputDir)
    writeSearchIndex(outputDir = outputDir, pages = pages)
    verifyLinks(outputDir = outputDir)

    println("Wrote ${pages.size + 1} pages to ${outputDir.absolutePath}")
}

/** A page after rendering, with everything the template and the search index need. */
private data class SitePage(
    val url: String,
    val section: String,
    val navTitle: String,
    val rendered: RenderedPage,
)

/**
 * Renders every page the navigation lists.
 *
 * Driven by the navigation rather than by a directory walk, so a markdown file that nobody put in
 * the sidebar cannot appear in the build as an unreachable page — and a sidebar entry pointing at a
 * file that no longer exists fails the build here rather than 404ing for a reader.
 */
private fun buildPages(docsDir: File): List<SitePage> =
    siteNavigation.flatMap { section ->
        section.entries.map { entry ->
            val file = docsDir.resolve(entry.path)
            require(file.isFile) { "Navigation lists ${entry.path}, which does not exist" }
            SitePage(
                url = entry.path.toSiteUrl(),
                section = section.title,
                navTitle = entry.title,
                rendered = renderMarkdown(file.readText()),
            )
        }
    }

private fun writePage(
    outputDir: File,
    page: SitePage,
    previous: NavEntry?,
    next: NavEntry?,
) {
    val depth = page.url.urlDepth()
    val html =
        renderPage(
            title = "${page.rendered.title} · Charty",
            description = page.rendered.searchText.take(DESCRIPTION_LENGTH),
            context = PageContext(url = page.url, depth = depth),
            navigation = renderNavigation(sections = siteNavigation, currentUrl = page.url, depth = depth),
            contents = renderContents(page.rendered.headings),
            body =
                page.rendered.html +
                    renderPagination(previous = previous, next = next, depth = depth),
        )
    val target = outputDir.resolve(page.url)
    target.parentFile.mkdirs()
    target.writeText(html)
}

private fun writeHome(
    outputDir: File,
    catalog: List<CatalogSection>,
) {
    val html =
        renderPage(
            title = "Charty · Charts for Compose Multiplatform",
            description =
                "A charting library for Compose Multiplatform: one API rendering on Android, iOS, " +
                    "desktop and the web.",
            context = PageContext(url = "index.html", depth = 0),
            navigation = renderNavigation(sections = siteNavigation, currentUrl = "index.html", depth = 0),
            contents = "",
            body = renderHome(catalog = catalog),
            landing = true,
        )
    outputDir.resolve("index.html").writeText(html)
}

private fun copyImages(
    docsDir: File,
    outputDir: File,
) {
    val images = docsDir.resolve("img")
    if (images.isDirectory) {
        images.copyRecursively(target = outputDir.resolve("img"), overwrite = true)
    }
}

/**
 * Copies the stylesheet and script out of the generator's own resources.
 *
 * They live as real files rather than Kotlin strings so an editor treats them as CSS and JavaScript
 * — syntax highlighting, formatting and errors all work, which they do not inside a string literal.
 */

/** Copies the brand art the home page opens with, which lives outside the docs tree. */
private fun copyBrand(
    brandDir: File,
    outputDir: File,
) {
    val banner = brandDir.resolve("banner.png")
    require(banner.isFile) { "Missing brand asset: ${banner.absolutePath}" }
    banner.copyTo(target = outputDir.resolve("img/banner.png"), overwrite = true)
}

private fun copyAssets(outputDir: File) {
    val assets = outputDir.resolve("assets")
    assets.mkdirs()
    listOf("docs.css", "docs.js").forEach { name ->
        val stream =
            checkNotNull(object {}.javaClass.getResourceAsStream("/$name")) {
                "Missing generator resource: $name"
            }
        assets.resolve(name).writeBytes(stream.use { input -> input.readBytes() })
    }
}

/**
 * Writes the search index the top bar reads.
 *
 * Built here rather than in the browser because the browser would have to download every page to
 * build it. One small JSON file is fetched once, on the first keystroke.
 */
private fun writeSearchIndex(
    outputDir: File,
    pages: List<SitePage>,
) {
    val entries =
        pages.joinToString(",") { page ->
            val fields =
                listOf(
                    "u" to page.url,
                    "t" to page.rendered.title,
                    "s" to page.section,
                    "b" to page.rendered.searchText.take(SEARCH_BODY_LENGTH),
                )
            fields.joinToString(",", prefix = "{", postfix = "}") { (key, value) ->
                """"$key":"${jsonEscape(value)}""""
            }
        }
    outputDir.resolve("assets/search-index.json").writeText("[$entries]")
}

private fun jsonEscape(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", " ")
        .replace("\r", " ")
        .replace("\t", " ")

private const val DESCRIPTION_LENGTH = 180
private const val SEARCH_BODY_LENGTH = 1200
