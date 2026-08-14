package com.himanshoe.docsite

/** One page in the sidebar: where its markdown lives, and what the nav calls it. */
data class NavEntry(
    /** Path to the markdown file, relative to the docs root. */
    val path: String,
    /** What the sidebar calls this page, which may be shorter than the page's own heading. */
    val title: String,
    /**
     * The playground chart this page can be opened in, or `null` when the playground has none.
     *
     * Named rather than derived from the title, because the two vocabularies genuinely differ — the
     * docs call a page "Scatter" where the playground calls it `Point` — and a guessed mapping fails
     * silently, landing the reader on the gallery with no idea why.
     */
    val playground: String? = null,
)

/** A titled group of pages in the sidebar. */
data class NavSection(
    /** The group heading shown in the sidebar. */
    val title: String,
    /** The pages in this group, in reading order. */
    val entries: List<NavEntry>,
)

/**
 * The sidebar, in the order a reader should meet it.
 *
 * Written out by hand rather than discovered from the file tree. A generated nav would be
 * alphabetical, which puts `AngularGaugeChart` before `BarChart` and buries the two pages a newcomer
 * actually needs. This also means a new page has to be placed deliberately: it will not appear until
 * someone decides where it belongs, and the build fails if a listed page does not exist, so the two
 * can never quietly drift apart.
 */
val siteNavigation: List<NavSection> =
    listOf(
        NavSection(
            title = "Getting started",
            entries =
                listOf(
                    NavEntry(path = "getting-started/installation.md", title = "Installation"),
                    NavEntry(path = "getting-started/quick-start.md", title = "Quick start"),
                ),
        ),
        NavSection(
            title = "Guides",
            entries =
                listOf(
                    NavEntry(path = "guides/streaming.md", title = "Streaming and live data"),
                    NavEntry(path = "guides/synced-crosshair.md", title = "Synced crosshair"),
                    NavEntry(path = "guides/exporting-charts.md", title = "Exporting as PNG"),
                    NavEntry(path = "guides/datetime-axis.md", title = "Datetime axis"),
                ),
        ),
        NavSection(
            title = "Configuration",
            entries =
                listOf(
                    NavEntry(path = "configurations/common-config.md", title = "Common configuration"),
                    NavEntry(path = "configurations/interactions.md", title = "Interactions"),
                    NavEntry(path = "customization/colors-and-animations.md", title = "Colors and animations"),
                    NavEntry(path = "customization/theming.md", title = "Theming"),
                ),
        ),
        NavSection(
            title = "Bar charts",
            entries =
                listOf(
                    NavEntry(path = "charts/bar/BarChart.md", title = "Bar", playground = "Bar"),
                    NavEntry(
                        path = "charts/bar/HorizontalBarChart.md",
                        title = "Horizontal bar",
                        playground = "HorizontalBar",
                    ),
                    NavEntry(path = "charts/bar/StackedBarChart.md", title = "Stacked bar", playground = "StackedBar"),
                    NavEntry(
                        path = "charts/bar/StackedHorizontalBarChart.md",
                        title = "Stacked horizontal",
                        playground = "BarFamily",
                    ),
                    NavEntry(
                        path = "charts/bar/GroupedHorizontalBarChart.md",
                        title = "Grouped horizontal",
                        playground = "GroupedBar",
                    ),
                    NavEntry(
                        path = "charts/bar/NormalizedHorizontalBarChart.md",
                        title = "Normalized horizontal",
                        playground = "BarFamily",
                    ),
                    NavEntry(
                        path = "charts/bar/DivergingBarChart.md",
                        title = "Diverging bar",
                        playground = "Diverging",
                    ),
                    NavEntry(
                        path = "charts/bar/ComparisonBarChart.md",
                        title = "Comparison bar",
                        playground = "BarFamily",
                    ),
                    NavEntry(path = "charts/bar/WaterfallChart.md", title = "Waterfall", playground = "BarFamily"),
                    NavEntry(path = "charts/bar/LollipopBarChart.md", title = "Lollipop", playground = "BarFamily"),
                    NavEntry(path = "charts/bar/BubbleBarChart.md", title = "Bubble bar", playground = "BarFamily"),
                    NavEntry(path = "charts/bar/MosaicBarChart.md", title = "Mosaic", playground = "BarFamily"),
                    NavEntry(path = "charts/bar/SpanChart.md", title = "Span", playground = "BarFamily"),
                    NavEntry(path = "charts/bar/WavyChart.md", title = "Wavy", playground = "Wavy"),
                ),
        ),
        NavSection(
            title = "Line and area",
            entries =
                listOf(
                    NavEntry(path = "charts/line/LineChart.md", title = "Line", playground = "Line"),
                    NavEntry(path = "charts/line/AreaChart.md", title = "Area", playground = "Area"),
                    NavEntry(path = "charts/line/MultilineChart.md", title = "Multiline", playground = "Multiline"),
                    NavEntry(
                        path = "charts/line/StackedAreaChart.md",
                        title = "Stacked area",
                        playground = "StackedArea",
                    ),
                    NavEntry(path = "charts/line/Sparkline.md", title = "Sparkline", playground = "Sparkline"),
                ),
        ),
        NavSection(
            title = "Point and combo",
            entries =
                listOf(
                    NavEntry(path = "charts/other/PointChart.md", title = "Scatter", playground = "Point"),
                    NavEntry(path = "charts/other/BubbleChart.md", title = "Bubble", playground = "Bubble"),
                    NavEntry(path = "charts/other/ComboChart.md", title = "Combo", playground = "Combo"),
                ),
        ),
        NavSection(
            title = "Circular",
            entries =
                listOf(
                    NavEntry(path = "charts/radial/PieChart.md", title = "Pie and donut", playground = "Pie"),
                    NavEntry(path = "charts/radial/RadarChart.md", title = "Radar", playground = "Radar"),
                    NavEntry(
                        path = "charts/radial/MultipleRadarChart.md",
                        title = "Multiple radar",
                        playground = "MultipleRadar",
                    ),
                    NavEntry(
                        path = "charts/radial/CircularProgressIndicator.md",
                        title = "Circular progress",
                        playground = "Circular",
                    ),
                    NavEntry(
                        path = "charts/radial/AngularGaugeChart.md",
                        title = "Angular gauge",
                        playground = "Gauge",
                    ),
                    NavEntry(path = "charts/radial/BlockBarChart.md", title = "Block bar", playground = "Block"),
                ),
        ),
        NavSection(
            title = "Specialised",
            entries =
                listOf(
                    NavEntry(
                        path = "charts/other/CandlestickChart.md",
                        title = "Candlestick",
                        playground = "Candlestick",
                    ),
                    NavEntry(
                        path = "charts/other/CalendarHeatmapChart.md",
                        title = "Calendar heatmap",
                        playground = "Calendar",
                    ),
                    NavEntry(
                        path = "charts/other/MatrixHeatmapChart.md",
                        title = "Matrix heatmap",
                        playground = "Heatmap",
                    ),
                    NavEntry(path = "charts/other/GanttChart.md", title = "Gantt", playground = "Gantt"),
                    NavEntry(path = "charts/other/FunnelChart.md", title = "Funnel", playground = "Funnel"),
                ),
        ),
        NavSection(
            title = "3D (experimental)",
            entries =
                listOf(
                    NavEntry(path = "charts/3d/README.md", title = "The charty-3d artifact"),
                    NavEntry(path = "charts/3d/Bar3DChart.md", title = "3D bar", playground = "Bar3D"),
                    NavEntry(path = "charts/3d/Pie3DChart.md", title = "3D pie", playground = "Pie3D"),
                ),
        ),
    )

/**
 * Every chart the playground can open, exactly as its URL spells it.
 *
 * Listed here so a mapping typo fails the build. A wrong name does not error at runtime — the
 * playground treats an unknown chart as "no selection" and shows the gallery — so the reader would
 * simply arrive somewhere they did not ask for, with nothing to explain why.
 */
private val playgroundCharts =
    setOf(
        "Streaming",
        "StreamingDashboard",
        "Gauge",
        "Heatmap",
        "Sparkline",
        "Synced",
        "Export",
        "Line",
        "Area",
        "Bar",
        "HorizontalBar",
        "StackedBar",
        "Diverging",
        "GroupedBar",
        "Point",
        "Bubble",
        "Pie",
        "Combo",
        "Multiline",
        "StackedArea",
        "Candlestick",
        "Radar",
        "Circular",
        "Wavy",
        "Block",
        "Calendar",
        "Gantt",
        "Funnel",
        "Bar3D",
        "Pie3D",
        "BarFamily",
        "MultipleRadar",
    )

/** Fails the build when a page points at a chart the playground does not have. */
fun verifyPlaygroundNames() {
    val unknown =
        siteNavigation
            .flatMap { section -> section.entries }
            .mapNotNull { entry -> entry.playground }
            .filterNot { name -> name in playgroundCharts }
            .distinct()
    check(unknown.isEmpty()) { "Navigation points at unknown playground charts: $unknown" }
}
