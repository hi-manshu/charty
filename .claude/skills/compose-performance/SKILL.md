---
name: compose-performance
description: Use when investigating Jetpack Compose recomposition cost, compiler stability reports, skippability, unstable parameters, frame-rate State reads, cross-phase snapshot back-writing, or @ReadOnlyComposable contracts.
---

# Compose performance

## Core principle

Measure one user-visible transition, identify the runtime axis that causes the
work, then apply the smallest correction at the phase or boundary where that
axis begins.

## Procedure

1. Reproduce one concrete transition and capture the observable evidence:
   recomposition counts, compiler reports, profiler data, or a clear trace.
2. Classify the primary axis: parameter stability and skipping, State read
   phase, or snapshot state written back into an earlier phase.
3. Check for a false lead: a real data change, a correctness defect, or an
   unchanged lazy item that is expected to recompose.
4. Read the corresponding focused reference before proposing a change.
5. Change one axis at a time and re-measure the same transition.
6. Finish when the evidence improves at the observed boundary without hiding
   state changes, caching stale values, or moving work to a less correct owner.

## Topic router

| Signal | Read |
|---|---|
| Cause is unknown, or several axes may interact | [Diagnosis](references/diagnosis.md) |
| `classes.txt`, `composables.txt`, strong skipping, unstable parameters, or collection stability | [Stability](references/stability.md) |
| Scroll, animation, gesture, layout, or draw State read at frame rate; measured state fed back into composition | [Deferred reads](references/deferred-reads.md) |
| A composable only reads composition locals or accessor-style values | [Composition contracts](references/composition-contracts.md) |
| State ownership or effect lifecycle is the root cause | [Compose state and effects](../compose-state-and-effects/SKILL.md) |

## RED/GREEN agent scenarios

1. RED blames unstable parameters for unchanged lazy rows that recompose during
   a focus transition. GREEN checks composition and layout back-writing first.
2. Novel case: an animation value controls only drawing. GREEN reads the State
   in a draw or layout lambda instead of propagating it through composition.
3. Counterexample: a screen visibly recomposes because its displayed model
   actually changed. GREEN does not add stability wrappers or caches merely to
   lower a count.
