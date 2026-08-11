# Baseline profile producer (`:baselineprofile`)

A `com.android.test` module that records an [Android Baseline Profile](https://developer.android.com/topic/performance/baselineprofiles/overview)
for Charty. It launches the `:composeApp` sample, scrolls the chart gallery, and captures which
classes/methods are hot so they can be AOT-compiled at install time instead of JIT-compiled on first
render — removing first-frame jank for the charts.

**Android only.** Baseline profiles are an ART (Android Runtime) feature; they have no effect on the
iOS / Desktop / JS / Wasm targets. The pure-Kotlin compute benchmarks live in `:benchmark` instead.

## What ships where

- The profile is filtered to `com.himanshoe.charty.**` and copied into
  `charty/src/androidMain/generated/baselineProfiles/baseline-prof.txt`, which the AAR bundles. **Any
  Android app that depends on Charty automatically benefits** — no consumer setup needed.
- The filter (`baselineProfile { filter { include("com.himanshoe.charty.**") } }`) lives in
  [`charty/build.gradle.kts`](../charty/build.gradle.kts); it keeps the app-wide profile's non-Charty
  rules out of the library artifact.

## Regenerate

Needs a connected device or a booted emulator (API 28+). A physical device gives the most
representative profile; an emulator works for a valid-but-thinner one.

```bash
./gradlew :charty:generateBaselineProfile
```

The task builds a non-minified release of the sample, installs it + this test module, runs
[`BaselineProfileGenerator`](src/main/kotlin/com/himanshoe/baselineprofile/BaselineProfileGenerator.kt),
and rewrites the checked-in `baseline-prof.txt`. Commit the result.

## Making the profile richer

Coverage is only as deep as the interaction in `BaselineProfileGenerator`. The current pass launches
the app and flings the gallery; to capture more of the draw/interaction paths, extend the
`profileBlock` to open individual charts and drive taps/drags (crosshair, tooltips, zoom) before the
profile is collected.
