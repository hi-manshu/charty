package com.himanshoe.charty.color

import androidx.compose.ui.graphics.Color

/**
 * Default color constants for Charty library.
 * These colors are used as defaults across all chart components.
 *
 * Usage:
 * ```kotlin
 * // Using default solid color
 * BarChart(
 *     data = { myData },
 *     color = ChartyColor.Solid(ChartyColors.Blue)
 * )
 *
 * // Using default gradient
 * StackedBarChart(
 *     data = { myData },
 *     colors = ChartyColors.DefaultGradient
 * )
 *
 * // Using a color palette
 * MultilineChart(
 *     data = { myData },
 *     colors = ChartyColors.ModernPalette
 * )
 * ```
 */
object ChartyColors {

    // ==================== Primary Colors ====================

    /**
     * Primary Blue - Default primary color for most charts
     * Hex: #2196F3 (Material Blue 500)
     */
    val Blue = Color(0xFF2196F3)

    /**
     * Primary Green - Success and positive values
     * Hex: #4CAF50 (Material Green 500)
     */
    val Green = Color(0xFF4CAF50)

    /**
     * Primary Orange - Warnings and highlights
     * Hex: #FF9800 (Material Orange 500)
     */
    val Orange = Color(0xFFFF9800)

    /**
     * Primary Red - Errors and negative values
     * Hex: #F44336 (Material Red 500)
     */
    val Red = Color(0xFFF44336)

    /**
     * Primary Purple - Alternative accent color
     * Hex: #9C27B0 (Material Purple 500)
     */
    val Purple = Color(0xFF9C27B0)

    /**
     * Primary Pink - Vibrant accent color
     * Hex: #E91E63 (Material Pink 500)
     */
    val Pink = Color(0xFFE91E63)

    /**
     * Primary Cyan - Cool accent color
     * Hex: #00BCD4 (Material Cyan 500)
     */
    val Cyan = Color(0xFF00BCD4)

    /**
     * Primary Teal - Nature and balance
     * Hex: #009688 (Material Teal 500)
     */
    val Teal = Color(0xFF009688)

    /**
     * Primary Indigo - Deep blue accent
     * Hex: #3F51B5 (Material Indigo 500)
     */
    val Indigo = Color(0xFF3F51B5)

    /**
     * Primary Amber - Warm accent color
     * Hex: #FFC107 (Material Amber 500)
     */
    val Amber = Color(0xFFFFC107)

    // ==================== Default Configurations ====================

    /**
     * Default solid color - Used when no color is specified
     */
    val DefaultSolid = ChartyColor.Solid(Blue)

    /**
     * Default gradient - Used for stacked and multi-value charts
     */
    val DefaultGradient = ChartyColor.Gradient(
        listOf(Blue, Green, Orange)
    )

    /**
     * Default multiline colors - Used for multiline and comparison charts
     */
    val DefaultMultiline = ChartyColor.Gradient(
        listOf(Pink, Blue, Green)
    )

    // ==================== Pre-defined Color Palettes ====================

    /**
     * Modern palette - Contemporary and vibrant colors
     */
    val ModernPalette = ChartyColor.Gradient(
        listOf(Blue, Cyan, Purple, Pink, Orange)
    )

    /**
     * Warm palette - Warm and energetic colors
     */
    val WarmPalette = ChartyColor.Gradient(
        listOf(Red, Orange, Amber, Pink)
    )

    /**
     * Cool palette - Cool and calming colors
     */
    val CoolPalette = ChartyColor.Gradient(
        listOf(Blue, Cyan, Teal, Indigo)
    )

    /**
     * Nature palette - Natural and organic colors
     */
    val NaturePalette = ChartyColor.Gradient(
        listOf(Green, Teal, Cyan, Blue)
    )

    /**
     * Vibrant palette - High contrast and energetic colors
     */
    val VibrantPalette = ChartyColor.Gradient(
        listOf(Red, Orange, Amber, Green, Cyan, Blue, Purple, Pink)
    )

    /**
     * Pastel palette - Soft and muted colors
     */
    val PastelPalette = ChartyColor.Gradient(
        listOf(
            Color(0xFFBBDEFB), // Light Blue
            Color(0xFFC8E6C9), // Light Green
            Color(0xFFFFCCBC), // Light Orange
            Color(0xFFF8BBD0), // Light Pink
            Color(0xFFE1BEE7), // Light Purple
        )
    )

    /**
     * Dark palette - Deep and rich colors
     */
    val DarkPalette = ChartyColor.Gradient(
        listOf(
            Color(0xFF1976D2), // Dark Blue
            Color(0xFF388E3C), // Dark Green
            Color(0xFFF57C00), // Dark Orange
            Color(0xFFC2185B), // Dark Pink
            Color(0xFF7B1FA2), // Dark Purple
        )
    )

    /**
     * Monochrome Blue palette - Different shades of blue
     */
    val MonochromeBlue = ChartyColor.Gradient(
        listOf(
            Color(0xFF0D47A1), // Blue 900
            Color(0xFF1976D2), // Blue 700
            Color(0xFF2196F3), // Blue 500
            Color(0xFF42A5F5), // Blue 400
            Color(0xFF90CAF9), // Blue 200
        )
    )

    /**
     * Business palette - Professional and corporate colors
     */
    val BusinessPalette = ChartyColor.Gradient(
        listOf(
            Color(0xFF1E88E5), // Corporate Blue
            Color(0xFF43A047), // Growth Green
            Color(0xFFFB8C00), // Warning Orange
            Color(0xFF424242), // Professional Grey
        )
    )

    /**
     * Gradient palette for financial data - Red to Green
     */
    val FinancialGradient = ChartyColor.Gradient(
        listOf(Red, Orange, Amber, Green)
    )

    // ==================== Utility Colors ====================

    /**
     * Light Gray - For subtle backgrounds and borders
     */
    val LightGray = Color(0xFFE0E0E0)

    /**
     * Medium Gray - For secondary text and elements
     */
    val MediumGray = Color(0xFF9E9E9E)

    /**
     * Dark Gray - For primary text and strong elements
     */
    val DarkGray = Color(0xFF424242)

    /**
     * White - For light theme backgrounds
     */
    val White = Color(0xFFFFFFFF)

    /**
     * Black - For dark theme backgrounds
     */
    val Black = Color(0xFF000000)

    // ==================== Transparent Variants ====================

    /**
     * Semi-transparent Blue - For overlays and highlights
     */
    val BlueAlpha30 = Blue.copy(alpha = 0.3f)

    /**
     * Semi-transparent Green - For overlays and highlights
     */
    val GreenAlpha30 = Green.copy(alpha = 0.3f)

    /**
     * Semi-transparent Orange - For overlays and highlights
     */
    val OrangeAlpha30 = Orange.copy(alpha = 0.3f)

    /**
     * Semi-transparent Red - For overlays and highlights
     */
    val RedAlpha30 = Red.copy(alpha = 0.3f)
}

