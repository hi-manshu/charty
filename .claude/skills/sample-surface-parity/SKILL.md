---
name: sample-surface-parity
description: Every public Charty chart must be reachable in BOTH sample surfaces — the web playground and the mobile/desktop gallery — plus its docs page and README catalog entry. Use when adding a new chart, a new chart module, or a new configurable capability, and before committing any of those. Trigger on "new chart", "add a chart", "3D chart", "expose a config", "add to playground", "wire it up", "is it done".
---

# A chart nobody can reach is not finished

Charty ships a chart three times over: the library API, the **web playground**, and the
**mobile/desktop gallery**. A chart that exists only in the library is invisible — nobody browsing
the playground or the sample app will ever discover it, and nobody reviewing the change can see it
render. Landing the composable is the middle of the job, not the end.

This is the most-missed step in this repo. A parity check written while adding the 3D module found
**14 charts** reachable from only one surface, most of them landed long before.

## The check

Run this before calling a chart done, and before committing:

```bash
.claude/skills/sample-surface-parity/check-parity.sh
```

It lists every public chart composable in `charty` and `charty3d` with a yes/NO for each surface, and
exits non-zero when any chart is missing from one. A `NO` is either work to do or a decision to
record — see *Deliberate omissions* below.

## Wiring a new chart in

**Web playground** — `composeApp/src/webMain/kotlin/com/himanshoe/sample/`

1. Add `<Name>Playground.kt` with an `internal fun <Name>Playground()` that calls
   `PlaygroundScaffold(code = …, chart = …, controls = …)`. Pass `cartesian = false` when the chart
   draws no `ChartScaffold` — the radial family, the projected 3D charts — so it is spared the shared
   axis and tooltip panels it would ignore.
2. Register it in `PlaygroundApp.kt`: an entry in the `PlaygroundFamily` enum **and** a branch in
   `PlaygroundContent`. The `when` is exhaustive, so a missed branch fails the build; a missed enum
   entry does not, and is the usual way a chart goes missing.
3. Every control must map to a real config property, and the live `code` block must show the call a
   caller would actually write.

**Mobile/desktop gallery** — `composeApp/src/commonMain/kotlin/com/himanshoe/sample/ChartGallery.kt`

4. Add a `ChartDemo(title, description, category, accent, variants)` inside `buildGalleryDemos()`,
   with two or more `ChartVariant`s showing the settings worth seeing — not just the default.
5. Put its sample data next to the other datasets at the top of `buildGalleryDemos()`.

**Docs**

6. `docs/charty/charts/<family>/<Name>.md`, following the shape of a neighbouring page: what it is
   best for, a runnable snippet, a section per capability, and an honest **Limitations** section.
7. Link it from the README chart catalog. An unlinked page is as invisible as an unwired chart.
8. Add it to `DocImageGenerator.kt` and regenerate, so the page opens with an image like every other:
   ```bash
   ./gradlew :charty:testDebugUnitTest --tests "com.himanshoe.charty.docs.DocImageGenerator" -Proborazzi.test.record=true
   ```

## New capability, not a new chart

The same rule holds one level down. A config property with no control in the playground is a
property nobody will find. When you add one, add its control in the same change — and if it belongs
to `ChartScaffoldConfig`, `TooltipConfig`, or `ChartyTheme`, it goes in the **shared** panel in
`PlaygroundShared.kt` so every chart picks it up at once rather than each repeating it.

## Deliberate omissions

Some absences are correct. A chart that is a thin variant of another, or one whose demo would be
meaningless without a live feed, may reasonably appear in only one surface. Record the reason in the
commit message when you leave a `NO` standing — an unexplained `NO` reads as an oversight, because
that is what it usually is.

## Commit gate

A staged diff that adds a `fun <Name>Chart(` to `charty/` or `charty3d/` without touching both
`composeApp/src/webMain/…` and `ChartGallery.kt` is not ready. Quick check on staged changes:

```bash
git diff --cached --name-only | grep -qE 'charty3?d?/src/commonMain.*Chart\.kt' && \
  git diff --cached --name-only | grep -qE 'webMain/.*Playground\.kt' && \
  git diff --cached --name-only | grep -q 'ChartGallery.kt' || \
  echo "new chart staged without both sample surfaces"
```
