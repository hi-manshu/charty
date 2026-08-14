package com.himanshoe.docsite

import java.io.File

/** One chart in the landing page's catalog. */
data class CatalogChart(
    /** The chart's display name. */
    val name: String,
    /** Where its documentation page lives on the site. */
    val url: String,
    /** Its thumbnail's file name, or `null` when it has none. */
    val image: String?,
)

/** A family of charts, as the catalog groups them. */
data class CatalogSection(
    /** The family heading, such as "Bar charts". */
    val title: String,
    /** The charts in this family. */
    val charts: List<CatalogChart>,
)

private val chartSectionTitles =
    setOf(
        "Bar charts",
        "Line and area",
        "Point and combo",
        "Circular",
        "Specialised",
        "3D (experimental)",
    )

/**
 * Builds the landing page's catalog from the same navigation the sidebar uses, so the front page
 * cannot advertise a different set of charts from the one the docs contain.
 *
 * Each chart's picture is derived from its filename rather than listed by hand — `BarChart.md` finds
 * `bar_chart.png` — because a hand-written mapping is a second list to forget to update. A derived
 * name that finds no file is reported rather than silently dropped, so a chart quietly losing its
 * picture fails the build instead of showing an empty card to a reader.
 */
fun buildCatalog(docsDir: File): List<CatalogSection> {
    val missing = mutableListOf<String>()
    val sections =
        siteNavigation
            .filter { section -> section.title in chartSectionTitles }
            .map { section ->
                CatalogSection(
                    title = section.title.removeSuffix(" (experimental)"),
                    charts =
                        section.entries
                            .filterNot { entry -> entry.path.endsWith("README.md") }
                            .map { entry -> toCatalogChart(docsDir = docsDir, entry = entry, missing = missing) },
                )
            }

    check(missing.isEmpty()) {
        "No image found for ${missing.size} chart(s): ${missing.joinToString()}"
    }
    return sections
}

private fun toCatalogChart(
    docsDir: File,
    entry: NavEntry,
    missing: MutableList<String>,
): CatalogChart {
    val image = imageNameFor(entry.path)
    if (!docsDir.resolve("img/$image").isFile) {
        missing.add("${entry.title} (expected img/$image)")
    }
    return CatalogChart(name = entry.title, url = entry.path.toSiteUrl(), image = image)
}

/**
 * The picture that belongs to a chart page: its file name, in the snake case the image generator
 * writes. `Bar3DChart.md` becomes `bar3_d_chart.png`, which is what that generator produced from the
 * Composable's name, oddities and all — the point is to match it exactly rather than to be tidy.
 */
private fun imageNameFor(path: String): String {
    val base = path.substringAfterLast('/').removeSuffix(".md")
    val snake =
        base.foldIndexed(StringBuilder()) { index, builder, character ->
            if (character.isUpperCase() && index > 0) {
                builder.append('_')
            }
            builder.append(character.lowercaseChar())
        }
    return "$snake.png"
}
