# Compose recomposition performance

Router only — deep fixes live in focused skills below.

## Three axes

1. **Parameter stability / skipping** — can Compose skip this restartable composable; are arguments stable and comparable?
2. **Where `State` is read** — is frame-rate `State` read during composition vs layout/draw?
3. **Back-writing across phases** — does a later phase write snapshot state that invalidates an earlier phase? Examples: map/list mutation during composition that re-invalidates the same composition; `onSizeChanged` (layout phase) writing state read by a sibling in composition.

Axes 2 and 3 often overlap (a sibling reading measured size in composition is both a deferred-read violation and a layout → composition back-write). Axis 1 is independent.

## Route here → focused skill

| Primary suspicion | Next skill |
|---|---|
| Skipping, unstable params, compiler/`composables.txt` churn | [Stability](stability.md) |
| Frame-rate `State` read phase (composition vs layout/draw) | [Deferred reads](deferred-reads.md) |
| `putAll` / map rebuild / cross-row `height(state)` during composition | [Deferred reads](deferred-reads.md) — § back-writing |
| Focus-driven side work in composable body | [Compose state and effects](../../compose-state-and-effects/SKILL.md) — `snapshotFlow` |
| Evidence for multiple axes | Apply matching skills in parallel |

## Review order

1. Reproduce one transition (focus move, insertion, scroll) and note which composables recompose.
2. If counts spike on unchanged lazy items, check back-writing (composition mutations and cross-row measurement) before blaming stability.
3. If counts climb every frame during scroll/animation, check deferred reads.
4. If skipping fails despite stable data, check parameter stability and compiler reports.
5. Re-measure after each fix.

## False leads

These changes often **do not** reduce recomposition count:

| Attempt | Why it fails |
|---|---|
| `remember(index) { isFirstRow(index) }` instead of inline `when (index)` | Same inputs; no skipping benefit |
| Identity cache for read-only derived maps | Can serve stale overlays; `remember(keys)` is enough |
| `mutableIntStateOf` + layout modifier on **both** measured and sibling rows | Sibling still reads size in composition unless measure-only |
| Forcing `Exactly(1)` on both rows in focus-move tests | One row often correctly recomposes 0 times |
| Hoisting without stabilizing lambda captures | New lambda instance each frame still defeats skipping |

## When NOT to apply

- Recomposition tracks real data changes, or the bug is correctness not cost.
- No profiler / compiler signal suggests a problem.

## Related

- [Compose state and effects](../../compose-state-and-effects/SKILL.md) — authoring `mutableState*` safely.
