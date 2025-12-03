# Remaining Refactoring Tasks

## Completed ✅

### Common Utilities Created
- ✅ `common/util/ValueCalculations.kt` - Min/max value calculations
- ✅ `common/animation/AnimationHelper.kt` - Animation state management
- ✅ `common/data/ChartDataPoint.kt` - Common data interface
- ✅ `common/constants/ChartConstants.kt` - Centralized constants
- ✅ `common/tooltip/TooltipManager.kt` - Tooltip state management

### Charts Updated
- ✅ PointChart.kt
- ✅ LineChart.kt
- ✅ CandlestickChart.kt
- ✅ AreaChart.kt

### Internal Helpers Updated
- ✅ bar/internal/bar/barchart/BarChartRemember.kt
- ✅ bar/internal/span/SpanChartRemember.kt
- ✅ bar/internal/bar/stacked/StackedBarChartRemember.kt
- ✅ bar/internal/bar/waterfall/WaterfallChartRemember.kt
- ✅ bar/internal/bar/horizontal/HorizontalBarChartRemember.kt

### Extension Files Cleaned
- ✅ point/PointChartExt.kt - Removed duplicates
- ✅ line/ext/LineChartExt.kt - Removed duplicates, kept LineGroup extensions
- ✅ bar/ext/BarChartExt.kt - Removed duplicates, kept BarGroup extensions

### Data Classes Updated
- ✅ PointData implements ChartDataPoint
- ✅ LineData implements ChartDataPoint
- ✅ BarData implements ChartDataPoint

---

## Remaining Tasks 🔄

### Charts Needing Animation Updates (4 files)

#### 1. bar/BubbleBarChart.kt
**Current:**
```kotlin
val animationProgress = remember {
    Animatable(if (animation is Animation.Enabled) 0f else 1f)
}
LaunchedEffect(animation) { ... }
```

**Replace with:**
```kotlin
val animationProgress = rememberChartAnimation(animation)
```

**Additional changes:**
- Remove `import androidx.compose.animation.core.Animatable`
- Remove `import androidx.compose.animation.core.tween`
- Remove `import androidx.compose.runtime.LaunchedEffect`
- Add `import com.himanshoe.charty.common.animation.rememberChartAnimation`

---

#### 2. bar/MosiacBarChart.kt
**Same pattern as BubbleBarChart.kt**

---

#### 3. radar/RadarChart.kt
**Same pattern as BubbleBarChart.kt**

---

#### 4. combo/ComboChart.kt
**Same pattern as BubbleBarChart.kt**

---

### Charts with Inline Animation (6 files)

These charts have animation but initialized inline, need to extract to use common helper:

#### 5. line/StackedAreaChart.kt
Line ~117:
```kotlin
val animationProgress = remember {
    Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
}
```

---

#### 6. line/MultilineChart.kt
Line ~94:
```kotlin
val animationProgress = remember {
    Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
}
```

---

#### 7. pie/PieChart.kt
Line ~167:
```kotlin
val animationProgress = remember { Animatable(if (config.animation is Animation.Enabled) 0f else 1f) }
```
Plus LaunchedEffect block

---

#### 8. point/BubbleChart.kt
Line ~76:
```kotlin
val animationProgress = remember {
    Animatable(if (config.animation is Animation.Enabled) 0f else 1f)
}
```

---

#### 9. bar/LollipopBarChart.kt
Line ~124:
```kotlin
val animationProgress = remember {
    Animatable(if (animation is Animation.Enabled) 0f else 1f)
}
```

---

## Quick Update Commands

For each file, apply these changes:

### Step 1: Update imports
Remove:
- `import androidx.compose.animation.core.Animatable` (unless explicitly used elsewhere)
- `import androidx.compose.animation.core.tween`
- `import androidx.compose.runtime.LaunchedEffect`

Add:
- `import com.himanshoe.charty.common.animation.rememberChartAnimation`

### Step 2: Replace animation initialization
Find:
```kotlin
val animationProgress = remember {
    Animatable(if (SOME_CONFIG.animation is Animation.Enabled) 0f else 1f)
}

LaunchedEffect(SOME_CONFIG.animation) {
    if (SOME_CONFIG.animation is Animation.Enabled) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = SOME_CONFIG.animation.duration),
        )
    }
}
```

Replace with:
```kotlin
val animationProgress = rememberChartAnimation(SOME_CONFIG.animation)
```

---

## Files Requiring Value Calculation Updates

These files may still be using local calculateMinValue/calculateMaxValue:

### Check and update if needed:
- StackedAreaChart.kt
- MultilineChart.kt  
- PieChart.kt (may not need)
- RadarChart.kt
- ComboChart.kt
- All bar chart variants

**Pattern to find:**
```bash
grep -r "calculateMinValue\|calculateMaxValue" --include="*.kt" | grep -v "common/util"
```

**Update to use:**
```kotlin
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.common.util.calculateMaxValue
```

---

## Testing Checklist

After updating each chart, test:
- [ ] Chart renders correctly
- [ ] Animation plays smoothly
- [ ] Click interactions work
- [ ] Tooltips display properly
- [ ] No compilation errors
- [ ] No runtime exceptions

---

## Estimated Time

- **Animation updates (10 files):** ~30-45 minutes
- **Value calculation updates:** ~15-20 minutes
- **Testing all charts:** ~30-45 minutes
- **Total:** ~1.5-2 hours

---

## Benefits After Completion

1. **~300-400 lines of code removed**
2. **Consistent animation behavior** across all charts
3. **Single source of truth** for value calculations
4. **Easier to maintain** - changes in one place affect all charts
5. **Easier to add new features** - e.g., custom animation curves
6. **Better code organization** - clear separation of concerns

---

## Next Phase Ideas

After completing these updates, consider:

1. **Extract gesture handling patterns**
2. **Create chart testing utilities**
3. **Add snapshot tests**
4. **Performance profiling**
5. **Accessibility improvements**
6. **Documentation updates**

