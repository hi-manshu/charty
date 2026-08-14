package com.himanshoe.docsite

private data class Feature(
    val title: String,
    val body: String,
    val href: String,
)

private val features =
    listOf(
        Feature(
            title = "One API, five targets",
            body =
                "Android, iOS, JVM desktop, and JavaScript and WebAssembly in the browser, from a " +
                    "single <code>commonMain</code> call. Most Compose chart libraries are Android only.",
            href = "getting-started/installation.html",
        ),
        Feature(
            title = "Live data that behaves",
            body =
                "A rolling window that slides as points arrive, an easing axis rescale, drag back " +
                    "through history, and a jump-to-latest pill when you have scrolled away.",
            href = "guides/streaming.html",
        ),
        Feature(
            title = "Interactions that compose",
            body =
                "A draggable crosshair, tap and drag tooltips drawn as your own Composable, " +
                    "persistent markers, zoom, pan, and brush selection — combined, not exclusive.",
            href = "configurations/interactions.html",
        ),
        Feature(
            title = "One crosshair, many charts",
            body =
                "Wrap stacked charts in <code>CrosshairSyncScope</code> and a single guide line " +
                    "moves across all of them. Charts enrol themselves; you wire nothing.",
            href = "guides/synced-crosshair.html",
        ),
        Feature(
            title = "Themed from one place",
            body =
                "<code>ChartyTheme</code> carries typography, component colours, shapes and " +
                    "dimensions, and every chart resolves its unset configuration against it.",
            href = "customization/theming.html",
        ),
        Feature(
            title = "Built for large series",
            body =
                "LTTB downsampling reduces tens of thousands of points to a shape-preserving " +
                    "subset before drawing, so a 50k-point line stays interactive.",
            href = "configurations/common-config.html",
        ),
        Feature(
            title = "Readable by screen readers",
            body =
                "A generated whole-chart description <em>and</em> per-point traversal, so a chart " +
                    "can be read value by value instead of announced as one image.",
            href = "configurations/interactions.html",
        ),
        Feature(
            title = "Export anywhere",
            body =
                "Capture any chart as a PNG and hand it to the platform: a share sheet on iOS, a " +
                    "download in the browser, the Downloads folder on desktop.",
            href = "guides/exporting-charts.html",
        ),
    )

private data class Platform(
    val name: String,
    val detail: String,
)

private val platforms =
    listOf(
        Platform(name = "Android", detail = "minSdk 24, with a baseline profile in the AAR"),
        Platform(name = "iOS", detail = "arm64 and simulator"),
        Platform(name = "Desktop", detail = "JVM, any OS"),
        Platform(name = "Web", detail = "JavaScript and WebAssembly"),
    )

private val quickStart =
    """
    LineChart(
        data = { listOf(LineData("Mon", 20f), LineData("Tue", 45f), LineData("Wed", 30f)) },
        color = ChartyColor.Solid(ChartyColors.Blue),
        lineConfig = LineChartConfig(interpolation = LineInterpolation.SMOOTH),
        crosshair = ChartCrosshair(),
    )
    """.trimIndent()

private val install =
    """
    commonMain.dependencies {
        implementation("com.himanshoe:charty:3.0.0")
        implementation("com.himanshoe:charty-3d:1.0.0")
    }
    """.trimIndent()

/** The landing page: what Charty is, every chart it draws, and the two links most readers want. */
fun renderHome(catalog: List<CatalogSection>): String {
    val chartCount = catalog.sumOf { section -> section.charts.size }
    return buildString {
        append(hero(chartCount = chartCount))
        append(marquee(catalog))
        append(featureGrid())
        append(catalogSection(catalog))
        append(platformSection())
        append(PERFORMANCE_SECTION)
        append(CLOSING_SECTION)
    }
}

private fun hero(chartCount: Int): String =
    """
<section class="hero">
  <div class="hero-text">
    <p class="eyebrow">Compose Multiplatform</p>
    <h1>Charts that go everywhere Kotlin does</h1>
    <p class="lede">
      A sleek, lightweight charting library for Jetpack Compose — and, uniquely, for Kotlin
      Multiplatform: one codebase renders your charts on Android, iOS, Desktop, and the Web.
      $chartCount chart types, built with <span class="heart">❤</span> by
      <a href="https://github.com/hi-manshu">@hi-manshu</a>.
    </p>
    <div class="hero-actions">
      <a class="button button--primary" href="getting-started/quick-start.html">Get started</a>
      <a class="button" href="playground/index.html">Try it in your browser</a>
      <a class="button button--sponsor" href="https://buymeacoffee.com/himanshoe" rel="noopener">
        <span aria-hidden="true">☕</span> Sponsor
      </a>
    </div>
    <div class="hero-code">${renderKotlinBlock(quickStart)}</div>
  </div>
  <div class="hero-art">
    <img src="img/banner.png" width="1280" height="640"
         alt="Charty — an elementary compose library" fetchpriority="high">
  </div>
</section>
"""

/**
 * A band of chart images that scrolls on its own.
 *
 * Charty's whole argument is visual, so the page shows the charts before it describes them. The row
 * is duplicated end to end and translated by exactly half its width, which is what makes the loop
 * seamless — at the moment it resets, the second copy is sitting exactly where the first began.
 */
private fun marquee(catalog: List<CatalogSection>): String {
    val shots = catalog.flatMap { section -> section.charts }.filter { chart -> chart.image != null }
    val row =
        shots.joinToString("") { chart ->
            """<a href="${chart.url}" title="${escapeAttribute(chart.name)}">""" +
                """<img src="img/${chart.image}" alt="${escapeAttribute(chart.name)}" loading="lazy"></a>"""
        }
    return """<section class="marquee" aria-hidden="true"><div class="marquee-track">$row$row</div></section>"""
}

private fun featureGrid(): String {
    val cards =
        features.joinToString("") { feature ->
            """<a class="card" href="${feature.href}">
                 <h3>${escapeText(feature.title)}</h3>
                 <p>${feature.body}</p>
               </a>"""
        }
    return """
<section class="band">
  <div class="band-inner">
    <h2 class="section-title">Everything in the box</h2>
    <p class="section-lede">The things you would otherwise build yourself, already built and documented.</p>
    <div class="cards">$cards</div>
  </div>
</section>
"""
}

private fun catalogSection(catalog: List<CatalogSection>): String {
    val groups =
        catalog.joinToString("") { section ->
            val items =
                section.charts.joinToString("") { chart ->
                    val art =
                        if (chart.image == null) {
                            """<div class="chart-card-art chart-card-art--empty"></div>"""
                        } else {
                            """<div class="chart-card-art">""" +
                                """<img src="img/${chart.image}" alt="" loading="lazy"></div>"""
                        }
                    """<a class="chart-card" href="${chart.url}">$art
                         <span class="chart-card-name">${escapeText(chart.name)}</span></a>"""
                }
            """<div class="catalog-group">
                 <h3 class="catalog-heading">${escapeText(section.title)}
                   <span class="count">${section.charts.size}</span></h3>
                 <div class="chart-grid">$items</div>
               </div>"""
        }
    return """
<section class="band band--alt" id="charts">
  <div class="band-inner">
    <h2 class="section-title">Every chart</h2>
    <p class="section-lede">
      Each one has a page with a picture, the code that produced it, and its full configuration.
    </p>
    $groups
  </div>
</section>
"""
}

private fun platformSection(): String {
    val items =
        platforms.joinToString("") { platform ->
            """<div class="platform"><strong>${escapeText(platform.name)}</strong>
                 <span>${escapeText(platform.detail)}</span></div>"""
        }
    return """
<section class="band">
  <div class="band-inner">
    <h2 class="section-title">Write once</h2>
    <p class="section-lede">One dependency, one API, and the same rendering on every target.</p>
    <div class="platforms">$items</div>
    <div class="install">
      ${renderKotlinBlock(install)}
      <p class="note">
        The 3D charts are a separate artifact on its own version, and every declaration in it is
        <code>@ChartyExperimental</code> — see <a href="charts/3d/index.html">charty-3d</a> for the
        opt-in, and for an honest account of what tilting a chart costs you.
      </p>
    </div>
  </div>
</section>
"""
}

private const val PERFORMANCE_SECTION =
    """
<section class="band band--alt">
  <div class="band-inner narrow">
    <h2 class="section-title">And it is quick</h2>
    <p class="section-lede">
      Charty reduces a large series to a shape-preserving subset before drawing, so a 50,000-point
      line stays interactive. Measured with JMH on a development machine — treat them as orders of
      magnitude rather than guarantees.
    </p>
    <div class="stats">
      <div class="stat"><strong>0.16 ms</strong><span>LTTB downsample, 50,000 to 800 points</span></div>
      <div class="stat"><strong>0.09 ms</strong><span>Axis min and max over 50,000 points</span></div>
      <div class="stat"><strong>0.07 ms</strong><span>10,000 value-to-pixel transforms</span></div>
    </div>
  </div>
</section>
"""

private const val CLOSING_SECTION =
    """
<section class="closing">
  <h2>Start with a chart on screen</h2>
  <p>Two minutes from an empty file to something rendering, on whichever platform you are on.</p>
  <div class="hero-actions">
    <a class="button button--primary" href="getting-started/quick-start.html">Quick start</a>
    <a class="button" href="playground/index.html">Open the playground</a>
    <a class="button button--sponsor" href="https://buymeacoffee.com/himanshoe" rel="noopener">
      <span aria-hidden="true">☕</span> Sponsor
    </a>
  </div>
</section>
"""
