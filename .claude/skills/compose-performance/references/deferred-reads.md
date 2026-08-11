# Compose state deferred reads

## Core principle

State reads invalidate the phase that reads them. If a `State<T>` is read in a composable body, changes invalidate composition. If it is read in layout or draw, changes can invalidate only layout or draw. Frame-rate state such as scroll offsets, animations, and drag positions usually belongs in layout/draw, not composition.

**Back-writing** is the symmetric failure mode: writing observable state from a phase that triggers invalidation of an earlier phase. Compose phases run composition → layout → draw. Writing snapshot-backed state from layout or draw to state read in composition invalidates composition; writing during composition to state read earlier in the same composition does the same. Both schedule extra work — often cascading into sibling lazy items.

The fix is structural: keep the `State<T>` or a provider lambda and read the value inside a layout/draw callback; capture measurements in callbacks and apply them in the measure phase, not by reading measurement state in sibling composable bodies.

## When to use this skill

- `val x by animate*AsState(...)` is passed to `Modifier.offset(x = ...)`, `Modifier.size(...)`, `Modifier.graphicsLayer(...)`, or another value-form modifier.
- `LazyListState.firstVisibleItemScrollOffset`, `ScrollState.value`, `Animatable.value`, or gesture state is read in a composable body.
- A composable takes `scrollOffset: Int`, `progress: Float`, `dragOffset: Offset`, or similar frame-rate values.
- Recomposition counters climb during scroll, animation, or gestures even when data is stable.
- A composable body calls `stateMap[key] = …`, `list.addAll(…)`, or similar on every recomposition (back-writing composition → composition).
- One lazy item captures size with `onSizeChanged` / `onGloballyPositioned` and a sibling reads that height in composition (`Modifier.height(state.dp)`) — back-writing layout → composition.

## 0. Back-writing

**Back-writing** = writing observable state in one phase that triggers invalidation of an earlier (or the current) phase. Compose runs composition → layout → draw, so:

- Writing snapshot state during composition that's read in the same composition.
- Writing snapshot state during layout (e.g. from `Modifier.layout`, `onSizeChanged`, `onGloballyPositioned`) that's read during composition.
- Writing snapshot state during draw that's read during composition or layout.

In all cases the writer schedules extra invalidation passes — often cascading into sibling lazy items.

Do not write to `mutableStateOf`, `mutableStateListOf`, `mutableStateMapOf`, or other snapshot-backed state from the composable body on every pass:

```kotlin
// ❌ BAD — mutates observable map during composition; siblings recompose repeatedly
@Composable
fun MergeOverlay(parent: Map<Key, ViewState>, overlay: Map<Key, ViewState>): Map<Key, ViewState> {
    val merged = remember { mutableStateMapOf<Key, ViewState>() }
    merged.clear()
    merged.putAll(parent)
    merged.putAll(overlay)   // back-writing composition → composition
    return merged
}

// ✅ GOOD — read-only merge; no composition-time writes
@Composable
fun MergeOverlay(parent: Map<Key, ViewState>, overlay: Map<Key, ViewState>): Map<Key, ViewState> =
    remember(parent, overlay) {
        if (overlay.isEmpty()) parent else parent + overlay
    }
```

Prefer `remember(keys) { … }` for derived read-only snapshots. Reserve `mutableState*` writes for event callbacks (`onClick`) or effects — not for rebuilding derived data on every composition.

Callbacks like `onSizeChanged` write *during layout*. That is only safe if no earlier phase reads the resulting state — see cross-row measurement below.

### Cross-row measurement (layout → composition back-write)

When row A measures and row B must match A's height, do not read A's captured size in B's composable body. `onSizeChanged` writes during layout; if B reads it in composition, layout has just back-written into composition:

```kotlin
var anchorHeightPx by remember { mutableIntStateOf(0) }

// ❌ BAD — B reads measurement state in composition; insertion/focus can double-recompose B
RowA(Modifier.onSizeChanged { anchorHeightPx = it.height })
RowB(Modifier.height(with(LocalDensity.current) { anchorHeightPx.toDp() }))  // composition read

// ✅ GOOD — capture on A; apply on B in measure phase only
RowA(Modifier.onSizeChanged { if (it.height != anchorHeightPx) anchorHeightPx = it.height })
RowB(
    Modifier.decorateMeasureConstraints { incoming ->
        if (anchorHeightPx > 0) incoming.copy(minHeight = anchorHeightPx, maxHeight = anchorHeightPx)
        else incoming
    },
)
```

`decorateMeasureConstraints` is a small layout helper (see [Compose component design](../../compose-component-design/SKILL.md)). While height is unknown, siblings use a fixed fallback in composition; once known, only layout invalidates — not an extra composition cascade.

## 1. Prefer block-form modifiers

Several modifiers have value forms and block forms. The value form receives values already read in composition; the block form can read during layout or draw.

```kotlin
// Before: animated value read in composition by the `by` delegate
@Composable
fun SelectionPill(selectedIndex: Int) {
    val offsetX by animateDpAsState(120.dp * selectedIndex)
    Box(Modifier.offset(x = offsetX))
}

// After: State is kept, value is read in the layout-phase offset block
@Composable
fun SelectionPill(selectedIndex: Int) {
    val offsetX = animateDpAsState(120.dp * selectedIndex)
    Box(
        Modifier.offset {
            IntOffset(offsetX.value.roundToPx(), 0)
        },
    )
}
```

Common replacements:

| Composition read | Deferred read |
|---|---|
| `Modifier.offset(x = animatedX)` | `Modifier.offset { IntOffset(animatedX.value.roundToPx(), 0) }` |
| `Modifier.graphicsLayer(translationY = y)` | `Modifier.graphicsLayer { translationY = yProvider() }` |
| `val radius by animateFloatAsState(...); drawBehind { drawCircle(radius = radius) }` | `val radius = animateFloatAsState(...); drawBehind { drawCircle(radius = radius.value) }` |

The `drawBehind` block is already draw-phase; the important part is that the `State.value` read also happens inside that block.

## 2. Pass providers across composable boundaries

If the fast-changing value would cross a composable boundary, pass a provider lambda instead of a snapshot value:

```kotlin
// Before: HomeScreen reads scroll offset in composition and passes the value down
@Composable
fun HomeScreen() {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        item { HeroImage(scrollOffset = listState.firstVisibleItemScrollOffset) }
    }
}

@Composable
fun HeroImage(scrollOffset: Int, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "...",
        modifier = modifier.graphicsLayer(translationY = -scrollOffset / 2f),
    )
}

// After: the only read happens inside graphicsLayer
@Composable
fun HomeScreen() {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        item {
            HeroImage(
                scrollOffsetProvider = {
                    if (listState.firstVisibleItemIndex == 0) {
                        listState.firstVisibleItemScrollOffset
                    } else {
                        0
                    }
                },
            )
        }
    }
}

@Composable
fun HeroImage(scrollOffsetProvider: () -> Int, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "...",
        modifier = modifier.graphicsLayer {
            translationY = -scrollOffsetProvider() / 2f
        },
    )
}
```

Suffix provider parameters with `Provider` when that clarifies the deferred-read contract.

## 3. Other layout/draw read sites

State reads can also be deferred inside:

- `Modifier.layout { measurable, constraints -> ... }`
- Custom `Alignment.align(...)`
- `drawWithContent`, `drawBehind`, and other draw modifiers
- Block-form layer/layout modifiers such as `graphicsLayer { ... }` and `offset { ... }`

Use these when the state changes where something is placed or painted. If the state decides *which composables exist*, it belongs in composition.

## Quick reference

| Symptom | Diagnosis | Fix |
|---|---|---|
| `val x by animateFloatAsState(...)` then `Modifier.offset(...)` | `by` reads in composition | Keep `State<Float>` and read `.value` in `offset {}` |
| `Modifier.graphicsLayer(translationY = animatedY)` | Property-argument form uses composition values | Use `graphicsLayer { translationY = ... }` |
| `Child(scrollOffset = listState.firstVisibleItemScrollOffset)` | Fast-changing value crosses boundary | `Child(scrollOffsetProvider = { ... })` |
| Draw block still recomposes every frame | Value was read before draw block | Move the `State.value` read inside the draw block |
| State chooses between different UI branches | Composition decision | Keep the read in composition |
| `mergedMap.putAll(overlay)` in composable body | Back-writing composition → composition | `remember(parent, overlay) { parent + overlay }` |
| Sibling `Modifier.height(measuredPx.toDp())` | Back-writing layout → composition | Measure-phase constraint decoration |
| Identity cache for read-only merge | Stale overlay risk | `remember(keys)` on immutable result |

## When NOT to apply

- The state controls which composables are emitted.
- The animation is one-shot, cheap, and clarity wins.
- You are writing tests where direct value assertions are simpler.
- Runtime evidence shows recomposition is not the bottleneck.

## Related

- [Compose state and effects](../../compose-state-and-effects/SKILL.md) — when `mutableState*` belongs in composition versus callbacks, and where state-holder versus plain UI splits apply.
- [Stability](stability.md) — parameter stability and compiler reports.
- [Compose component design](../../compose-component-design/SKILL.md) — measure-phase constraint decoration helpers.
