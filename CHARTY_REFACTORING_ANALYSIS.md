# Charty Module - Refactoring Analysis & Recommendations

## Executive Summary
This document provides a comprehensive analysis of the Charty module, identifying opportunities for code improvement, common logic extraction, and architectural enhancements to improve maintainability, reduce duplication, and enhance code quality.

---

## 🔴 Critical Issues - High Priority

### 1. **Duplicated Value Calculation Functions**
**Location:** Multiple files across the project
- `/charty/src/commonMain/kotlin/com/himanshoe/charty/point/PointChartExt.kt`
- `/charty/src/commonMain/kotlin/com/himanshoe/charty/line/ext/LineChartExt.kt`
- `/charty/src/commonMain/kotlin/com/himanshoe/charty/bar/ext/BarChartExt.kt`
- `/charty/src/commonMain/kotlin/com/himanshoe/charty/candlestick/ext/CandlestickChartExt.kt` (different implementation)

**Problem:** The `calculateMaxValue()` and `calculateMinValue()` functions are duplicated across 4+ files with identical implementations (except candlestick which has a slightly different approach).

**Current Code Pattern:**
```kotlin
// Repeated in 4 different files
internal fun calculateMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
}

internal fun calculateMinValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}
```

**Recommendation:**
Create a common utility file:
```kotlin
// File: /charty/src/commonMain/kotlin/com/himanshoe/charty/common/util/ValueCalculations.kt
package com.himanshoe.charty.common.util

import kotlin.math.ceil
import kotlin.math.floor

/**
 * Calculate appropriate max value with nice rounding
 * Rounds up to the nearest multiple of stepSize
 */
fun calculateMaxValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val maxData = values.maxOrNull() ?: 0f
    return ceil(maxData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculate appropriate min value with nice rounding
 * Rounds down to the nearest multiple of stepSize
 */
fun calculateMinValue(
    values: List<Float>,
    stepSize: Int = 10,
): Float {
    val minData = values.minOrNull() ?: 0f
    return floor(minData / stepSize).toInt() * stepSize.toFloat()
}

/**
 * Calculate min and max with padding multipliers (for candlestick-style charts)
 */
fun calculateMinMaxWithPadding(
    values: List<Float>,
    paddingMultiplier: Float = 0.05f,
): Pair<Float, Float> {
    val min = values.minOrNull() ?: 0f
    val max = values.maxOrNull() ?: 0f
    return (min * (1f - paddingMultiplier)) to (max * (1f + paddingMultiplier))
}
```

**Impact:** Eliminates ~50 lines of duplicated code, improves maintainability.

---

### 2. **Duplicated Animation Pattern**
**Location:** 18+ chart files

**Problem:** Every chart implements the same animation initialization pattern:
```kotlin
val animationProgress = remember {
    Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
}

LaunchedEffect(config.animation) {
    if (config.animation is Animation.Enabled) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = config.animation.duration),
        )
    }
}
```

**Recommendation:**
Create a common composable utility:
```kotlin
// File: /charty/src/commonMain/kotlin/com/himanshoe/charty/common/animation/AnimationHelper.kt
package com.himanshoe.charty.common.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.himanshoe.charty.common.config.Animation

/**
 * Creates and manages animation progress for charts
 * 
 * @param animation Animation configuration
 * @param initialValue Starting value (default 0f for enabled animations, 1f for disabled)
 * @param targetValue Target value (default 1f)
 * @return Animatable<Float> that tracks animation progress
 */
@Composable
fun rememberChartAnimation(
    animation: Animation,
    initialValue: Float? = null,
    targetValue: Float = 1f,
): Animatable<Float, *> {
    val animationProgress = remember(animation) {
        val initial = initialValue ?: if (animation is Animation.Enabled) 0f else targetValue
        Animatable(initial)
    }
    
    LaunchedEffect(animation) {
        if (animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = targetValue,
                animationSpec = tween(durationMillis = animation.duration),
            )
        }
    }
    
    return animationProgress
}
```

**Usage in charts:**
```kotlin
// Before:
val animationProgress = remember {
    Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
}
LaunchedEffect(config.animation) { ... }

// After:
val animationProgress = rememberChartAnimation(config.animation)
```

**Impact:** Eliminates ~200+ lines of duplicated code across all charts.

---

### 3. **Common Data Interface Missing**
**Location:** `/charty/src/commonMain/kotlin/com/himanshoe/charty/*/data/`

**Problem:** `PointData`, `LineData`, and `BarData` are structurally identical but don't share a common interface:
```kotlin
// PointData.kt
data class PointData(val label: String, val value: Float)

// LineData.kt
data class LineData(val label: String, val value: Float)

// BarData.kt (slightly different with optional color)
data class BarData(val label: String, val value: Float, val color: ChartyColor? = null)
```

**Recommendation:**
Create a common interface and extension functions:
```kotlin
// File: /charty/src/commonMain/kotlin/com/himanshoe/charty/common/data/ChartData.kt
package com.himanshoe.charty.common.data

/**
 * Common interface for basic chart data with label and value
 */
interface ChartDataPoint {
    val label: String
    val value: Float
}

/**
 * Common extensions for lists of chart data
 */
fun <T : ChartDataPoint> List<T>.getValues(): List<Float> = map { it.value }
fun <T : ChartDataPoint> List<T>.getLabels(): List<String> = map { it.label }
fun <T : ChartDataPoint> List<T>.calculateMinValue(stepSize: Int = 10): Float = 
    com.himanshoe.charty.common.util.calculateMinValue(getValues(), stepSize)
fun <T : ChartDataPoint> List<T>.calculateMaxValue(stepSize: Int = 10): Float = 
    com.himanshoe.charty.common.util.calculateMaxValue(getValues(), stepSize)
```

Then update data classes:
```kotlin
data class PointData(
    override val label: String,
    override val value: Float,
) : ChartDataPoint

data class LineData(
    override val label: String,
    override val value: Float,
) : ChartDataPoint

data class BarData(
    override val label: String,
    override val value: Float,
    val color: ChartyColor? = null,
) : ChartDataPoint
```

**Impact:** Better type safety, reduced extension function duplication, clearer API.

---

## 🟡 Medium Priority Issues

### 4. **Tooltip Logic Duplication**
**Location:** Multiple chart files (PointChart, BarChart, LineChart, etc.)

**Problem:** Each chart reimplements tooltip state management and drawing:
```kotlin
// Repeated pattern in multiple charts
var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
val pointBounds = remember { mutableListOf<Pair<Offset, PointData>>() }

// Later in drawing code:
tooltipState?.let { state ->
    drawTooltipHighlight(...)
}
```

**Recommendation:**
Create a common tooltip manager:
```kotlin
// File: /charty/src/commonMain/kotlin/com/himanshoe/charty/common/tooltip/TooltipManager.kt
package com.himanshoe.charty.common.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Manager for tooltip state and bounds tracking
 */
class TooltipManager<T> {
    var tooltipState: TooltipState? by mutableStateOf(null)
        private set
    
    val bounds = mutableListOf<Pair<Any, T>>() // Offset or Rect
    
    fun updateTooltip(state: TooltipState?) {
        tooltipState = state
    }
    
    fun clearBounds() {
        bounds.clear()
    }
    
    fun dismiss() {
        tooltipState = null
    }
}

@Composable
fun <T> rememberTooltipManager(): TooltipManager<T> {
    return remember { TooltipManager() }
}
```

---

### 5. **Constants Scattered Across Files**
**Location:** Multiple internal constant files

**Problem:** Magic numbers and constants are defined in multiple places:
- `PointChart.kt`: `TAP_RADIUS_MULTIPLIER = 2.5f`, `POINT_RADIUS_MULTIPLIER = 2f`, etc.
- Similar constants in other charts

**Recommendation:**
Centralize common constants:
```kotlin
// File: /charty/src/commonMain/kotlin/com/himanshoe/charty/common/constants/ChartConstants.kt
package com.himanshoe.charty.common.constants

/**
 * Common constants used across multiple chart types
 */
object ChartConstants {
    // Gesture/Touch
    const val DEFAULT_TAP_RADIUS_MULTIPLIER = 2.5f
    const val DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER = 2f
    
    // Visual
    const val DEFAULT_GUIDELINE_ALPHA = 0.1f
    const val DEFAULT_GUIDELINE_WIDTH = 1.5f
    const val DEFAULT_HIGHLIGHT_OUTER_OFFSET = 3f
    const val DEFAULT_HIGHLIGHT_INNER_OFFSET = 2f
    
    // Animation
    const val MIN_ANIMATION_PROGRESS = 0f
    const val MAX_ANIMATION_PROGRESS = 1f
    
    // Axis
    const val DEFAULT_AXIS_STEPS = 6
}
```

---

### 6. **Similar Gesture Handling Across Charts**
**Location:** Multiple chart modifier creation functions

**Problem:** Click detection and gesture handling is reimplemented for each chart type:
```kotlin
// Pattern repeated in PointChart, LineChart, BarChart
private fun createChartModifier(...): Modifier = modifier.then(
    if (onPointClick != null) {
        Modifier.pointerInput(...) {
            detectTapGestures { offset ->
                // Similar logic repeated
            }
        }
    } else {
        Modifier
    }
)
```

**Recommendation:**
Create a common gesture handler builder:
```kotlin
// File: /charty/src/commonMain/kotlin/com/himanshoe/charty/common/gesture/ChartGestureBuilder.kt
package com.himanshoe.charty.common.gesture

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Creates a modifier with tap gesture handling for point-based charts
 */
fun <T> Modifier.handlePointTapGesture(
    data: List<T>,
    pointBounds: List<Pair<Offset, T>>,
    tapRadius: Float,
    onTap: (T) -> Unit,
    onTooltipUpdate: (TooltipState?) -> Unit,
    tooltipFormatter: (T) -> String,
    tooltipPosition: TooltipPosition,
    tooltipConfig: TooltipConfig,
    itemRadius: Float,
): Modifier = this.pointerInput(data) {
    detectTapGestures { offset ->
        val tappedItem = findNearestPoint(offset, pointBounds, tapRadius)
        
        if (tappedItem != null) {
            val (position, item) = tappedItem
            onTap(item)
            onTooltipUpdate(
                TooltipState(
                    content = tooltipFormatter(item),
                    x = position.x - itemRadius,
                    y = position.y,
                    barWidth = itemRadius * 2f,
                    position = tooltipPosition,
                )
            )
        } else {
            onTooltipUpdate(null)
        }
    }
}
```

---

## 🟢 Low Priority / Nice to Have

### 7. **Config Validation Consistency**
**Problem:** Some config classes have `init` blocks with validation, others don't.

**Recommendation:** Add consistent validation across all config classes.

---

### 8. **Documentation Improvements**
**Observation:** Most files have good documentation, but some internal functions lack KDoc.

**Recommendation:** Add KDoc to all public and internal functions for better IDE support.

---

### 9. **Test Coverage**
**Observation:** No visible test files in the structure provided.

**Recommendation:** Add unit tests for:
- Value calculation utilities
- Gesture detection logic
- Tooltip positioning logic
- Animation helpers

---

### 10. **Extension Function Organization**
**Problem:** Extension functions are scattered across multiple `ext` packages.

**Recommendation:** Consider organizing by functionality rather than chart type:
```
common/
  ext/
    DataExtensions.kt      // All data-related extensions
    ListExtensions.kt      // List operations
    ValueCalculations.kt   // Min/max/range calculations
```

---

## 📊 Metrics Summary

### Code Duplication
- **Duplicated Functions:** ~8 major functions (calculateMin/Max across 4 files)
- **Duplicated Patterns:** ~18 animation blocks, ~10 gesture handlers
- **Estimated Duplication:** ~400-500 lines that could be extracted

### Potential Impact
- **Lines of Code Reduction:** ~30-40%
- **Maintainability Improvement:** High
- **Bug Risk Reduction:** Medium-High
- **API Clarity:** Medium improvement

---

## 🎯 Recommended Implementation Order

### Phase 1: Foundation (1-2 days)
1. Create common value calculation utilities
2. Create common animation helper
3. Create common data interface

### Phase 2: Consolidation (2-3 days)
4. Extract tooltip management
5. Centralize constants
6. Create common gesture handlers

### Phase 3: Polish (1-2 days)
7. Add consistent validation
8. Improve documentation
9. Add tests

### Phase 4: Cleanup (1 day)
10. Reorganize extension functions
11. Remove deprecated code
12. Update existing charts to use new utilities

---

## 🔧 Technical Debt Items

1. **Missing Tests** - No unit tests visible
2. **Magic Numbers** - Still some hardcoded values in chart implementations
3. **Inconsistent Naming** - Some files use `ChartName`, others use `chartName`
4. **No Error Boundaries** - Limited error handling in some chart rendering code

---

## 💡 Additional Recommendations

### Consider Creating:
1. **ChartDefaults** object with common default values
2. **ChartTheme** system for consistent styling across charts
3. **ChartValidator** for data validation
4. **ChartDebugger** utility for development/debugging

### Architecture Improvements:
1. Consider State Hoisting for tooltip and animation states
2. Add snapshot testing for chart rendering
3. Consider adding accessibility support
4. Add performance profiling utilities

---

## 📝 Notes

- The codebase is generally well-structured with good separation of concerns
- The internal package structure (splitting helpers, drawers, modifiers) is a good pattern
- Animation and tooltip systems are sophisticated but could benefit from consolidation
- The code follows Kotlin conventions well
- Good use of sealed interfaces (Animation, ChartOrientation, etc.)

---

## Conclusion

The Charty module is well-architected but has significant opportunities for reducing duplication and improving maintainability. The main areas of improvement are:

1. **Extract common utilities** (value calculations, animations)
2. **Create shared interfaces** (ChartDataPoint)
3. **Consolidate patterns** (gestures, tooltips)
4. **Add comprehensive testing**

Implementing these recommendations would significantly improve code quality while maintaining backward compatibility.

