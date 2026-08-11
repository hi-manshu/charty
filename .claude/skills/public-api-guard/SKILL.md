---
name: public-api-guard
description: Use when adding or changing any PUBLIC declaration (composable, function, class, property, enum) in the Charty library under charty/src — before the change may be committed. Enforces three gates on public API: (1) complete KDoc, (2) a matching test, (3) Compose performance-matrix compliance. Trigger on "add a chart", "new public function", "expose a config", "document the API", or right before committing library changes.
---

# Public API guard

Every **public** declaration added to or changed in `charty/src/commonMain` must clear three gates
before it is committed: **KDoc**, **Test**, **Performance matrix**. This skill is the checklist.

Scope: applies to `public` (the Kotlin default) and `protected` declarations in the `charty`
library module — composables, top-level functions, classes, data classes, enums, and their public
properties. It does **not** apply to `internal`/`private` declarations, to the `composeApp` sample,
or to test code. If a new helper does not need to be public, prefer making it `internal` — that is
the cheapest way to pass this gate.

Run all three gates yourself before proposing a commit. If any gate fails, fix it or make the
declaration non-public; do not commit public API that fails a gate.

---

## Gate 1 — KDoc on every public declaration

Match the existing Charty KDoc style (see `LineChart.kt`, `BarChartConfig.kt`). Required elements:

- **Summary sentence** — what it is / does, in one line. Composables start with
  "A composable function that displays …".
- **`@param`** for every parameter (functions/composables) — describe meaning and units, note
  defaults and interactions with other params.
- **`@property`** for every public constructor property (data/`@Immutable` classes).
- **`@return`** when the function returns a non-Unit value that isn't obvious from the name.
- **Runnable example** in a ```kotlin fence for every public **composable** and every new **chart
  type**. The example must reference real symbols (it should compile).
- Keep example fences as ```kotlin (not bare ```), matching the repo convention.

Reject KDoc that just restates the signature ("the modifier" for `modifier`). Say something useful
(what it's applied to, what the default does).

Quick self-check before committing:
```bash
# List public funcs/classes changed on this branch that may need KDoc review:
git diff --cached -U0 -- 'charty/src/commonMain/**/*.kt' | grep -E '^\+\s*(fun|class|data class|enum class|sealed|object|val|var) ' | grep -viE 'internal|private'
```
For each line returned, confirm the declaration directly above it in the file has KDoc.

---

## Gate 2 — A test must exist before commit

No new or changed public behavior is committed without a test.

**Apply the vendored [`compose-ui-testing-patterns`](../compose-ui-testing-patterns/SKILL.md) skill**
for how to test (test the smallest contract; plain state-driven tests over full dependency graphs;
semantics assertions; fakes for images/network). This gate does not restate it — read it.

Charty-specific mechanics:
- Framework: `kotlin-test` (`charty/src/commonTest`, wired in `charty/build.gradle.kts`).
  **There is currently no `commonTest` source set on disk — create it** the first time this gate
  runs, mirroring the source package:
  `charty/src/commonTest/kotlin/com/himanshoe/charty/<pkg>/<Name>Test.kt`.
- Most of Charty's public surface is pure logic (value-range math, `formatAxisLabel`,
  `AxisConfig.valueFormatter`, `baselineValueRange`, config `init { require(...) }` blocks) — cover
  it with plain `kotlin.test` assertions; `assertFailsWith<IllegalArgumentException>` for `require`
  guards.
- For composables, prefer testing the drawing-math / config helpers they delegate to over
  instrumenting the Canvas. If a public function is untestable in isolation, extract a pure helper
  and test that.
- Verify green before committing: `./gradlew :charty:allTests`.

---

## Gate 3 — Performance

**Apply the vendored [`compose-performance`](../compose-performance/SKILL.md) skill** — it is the
authority here (measure one transition → classify the axis: stability/skipping, state-read phase,
snapshot back-write → correct at that boundary). Its `references/` cover stability reports, deferred
reads, composition contracts, and diagnosis. Do not guess or add caches/wrappers just to lower a
recomposition number without a measured cause.

On top of that, keep these **Charty house rules** (already followed pervasively in the codebase):
- Public config & data classes are `@Immutable` (or `@Stable`).
- Iterate data/draw loops with `fastForEach` / `fastForEachIndexed` / `fastMap` (from
  `androidx.compose.ui.util`), never `forEach`/`map`, in per-frame paths.
- No allocation inside a per-frame `DrawScope` block — hoist `PathEffect`, `Paint`, `FloatArray`,
  lists, and lambdas to a top-level `private val` or `remember` (e.g. the hoisted
  `CROSSHAIR_DASH_EFFECT`).
- Memoize derived values (min/max, positions, color lists) with `remember(keys)` /
  `derivedStateOf`, keyed correctly (e.g. the shared `baselineValueRange`).

For deeper Compose work also consult the vendored `compose-state-and-effects`,
`compose-component-design`, and `compose-animations` skills.

---

## Commit gate

A commit that touches public API in `charty/src` is only ready when **all three gates pass**:
KDoc complete, test present and green (`./gradlew :charty:allTests`), performance matrix satisfied,
and the existing project checks are clean (`./gradlew detekt` and no new ktlint violations). The
`commit-with-stats` skill references this gate as a precondition.

---

## Attribution

Gates 2 and 3 defer to **Chris Banes' skills**, vendored verbatim into `.claude/skills/`
(Apache License 2.0 — see `.claude/skills/ATTRIBUTION.md` and `LICENSE-chrisbanes-skills`):
`compose-performance`, `compose-ui-testing-patterns`, `kotlin-api-design`,
`compose-state-and-effects`, `compose-component-design`, `compose-animations`.

This file (`public-api-guard`) and the KDoc gate are project-authored; they orchestrate the three
gates and reference the vendored skills rather than duplicating them.
