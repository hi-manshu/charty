package com.himanshoe.docsite

/** One page in the sidebar: where its markdown lives, and what the nav calls it. */
data class NavEntry(
    val path: String,
    val title: String,
)

/** A titled group of pages in the sidebar. */
data class NavSection(
    val title: String,
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
                    NavEntry(path = "charts/bar/BarChart.md", title = "Bar"),
                    NavEntry(path = "charts/bar/HorizontalBarChart.md", title = "Horizontal bar"),
                    NavEntry(path = "charts/bar/StackedBarChart.md", title = "Stacked bar"),
                    NavEntry(path = "charts/bar/StackedHorizontalBarChart.md", title = "Stacked horizontal"),
                    NavEntry(path = "charts/bar/GroupedHorizontalBarChart.md", title = "Grouped horizontal"),
                    NavEntry(path = "charts/bar/NormalizedHorizontalBarChart.md", title = "Normalized horizontal"),
                    NavEntry(path = "charts/bar/DivergingBarChart.md", title = "Diverging bar"),
                    NavEntry(path = "charts/bar/ComparisonBarChart.md", title = "Comparison bar"),
                    NavEntry(path = "charts/bar/WaterfallChart.md", title = "Waterfall"),
                    NavEntry(path = "charts/bar/LollipopBarChart.md", title = "Lollipop"),
                    NavEntry(path = "charts/bar/BubbleBarChart.md", title = "Bubble bar"),
                    NavEntry(path = "charts/bar/MosaicBarChart.md", title = "Mosaic"),
                    NavEntry(path = "charts/bar/SpanChart.md", title = "Span"),
                    NavEntry(path = "charts/bar/WavyChart.md", title = "Wavy"),
                ),
        ),
        NavSection(
            title = "Line and area",
            entries =
                listOf(
                    NavEntry(path = "charts/line/LineChart.md", title = "Line"),
                    NavEntry(path = "charts/line/AreaChart.md", title = "Area"),
                    NavEntry(path = "charts/line/MultilineChart.md", title = "Multiline"),
                    NavEntry(path = "charts/line/StackedAreaChart.md", title = "Stacked area"),
                    NavEntry(path = "charts/line/Sparkline.md", title = "Sparkline"),
                ),
        ),
        NavSection(
            title = "Point and combo",
            entries =
                listOf(
                    NavEntry(path = "charts/other/PointChart.md", title = "Scatter"),
                    NavEntry(path = "charts/other/BubbleChart.md", title = "Bubble"),
                    NavEntry(path = "charts/other/ComboChart.md", title = "Combo"),
                ),
        ),
        NavSection(
            title = "Circular",
            entries =
                listOf(
                    NavEntry(path = "charts/radial/PieChart.md", title = "Pie and donut"),
                    NavEntry(path = "charts/radial/RadarChart.md", title = "Radar"),
                    NavEntry(path = "charts/radial/MultipleRadarChart.md", title = "Multiple radar"),
                    NavEntry(path = "charts/radial/CircularProgressIndicator.md", title = "Circular progress"),
                    NavEntry(path = "charts/radial/AngularGaugeChart.md", title = "Angular gauge"),
                    NavEntry(path = "charts/radial/BlockBarChart.md", title = "Block bar"),
                ),
        ),
        NavSection(
            title = "Specialised",
            entries =
                listOf(
                    NavEntry(path = "charts/other/CandlestickChart.md", title = "Candlestick"),
                    NavEntry(path = "charts/other/CalendarHeatmapChart.md", title = "Calendar heatmap"),
                    NavEntry(path = "charts/other/MatrixHeatmapChart.md", title = "Matrix heatmap"),
                    NavEntry(path = "charts/other/GanttChart.md", title = "Gantt"),
                    NavEntry(path = "charts/other/FunnelChart.md", title = "Funnel"),
                ),
        ),
        NavSection(
            title = "3D (experimental)",
            entries =
                listOf(
                    NavEntry(path = "charts/3d/README.md", title = "The charty-3d artifact"),
                    NavEntry(path = "charts/3d/Bar3DChart.md", title = "3D bar"),
                    NavEntry(path = "charts/3d/Pie3DChart.md", title = "3D pie"),
                ),
        ),
    )
