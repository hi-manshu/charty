# Charty benchmarks

JVM microbenchmarks for Charty's pure per-data / per-frame hot paths, powered by
[kotlinx-benchmark](https://github.com/Kotlin/kotlinx-benchmark) (JMH under the hood). They measure
the compute cost of the code that runs while data changes or the chart draws — no Compose, no device
needed — so a regression shows up in CI.

## Run

```bash
./gradlew :benchmark:mainBenchmark
```

The checked-in numbers use a fast config (3 warmups, 5 iterations). For publishable figures raise
`warmups` / `iterations` in [`build.gradle.kts`](build.gradle.kts).

## Results

Measured on a dev machine (JMH `AverageTime`, lower is better). Your absolute numbers will vary;
what matters is the order of magnitude and tracking them over time.

| Benchmark | What it exercises | Time |
| --- | --- | --- |
| `lttbDownsample50kTo800` | LTTB reduction of a 50,000-point series to 800 points (the large-dataset render path) | ~160 µs |
| `valueRange50k` | `calculateMinValue` + `calculateMaxValue` "nice" rounding over 50k points (runs on data change) | ~87 µs |
| `coordinateTransforms10k` | 10,000 value→pixel transforms (`convertValueToYPosition` + `calculateCenteredXPosition`) — the per-point, per-frame draw cost | ~73 µs |

### Reading the numbers

- **Downsampling is effectively free.** Collapsing 50k points to 800 costs ~0.16 ms — a tiny slice of
  a 16 ms (60 fps) frame budget — which is why `downsampleThreshold` keeps huge series interactive.
- **Coordinate transforms are ~7 ns each.** 10k of them (a very dense chart) total ~0.07 ms, so the
  math is never the bottleneck; overdraw and recomposition are what to watch on device.

## What this does *not* cover

These are pure-compute microbenchmarks. They do **not** measure Compose recomposition, actual draw
time, or frame jank — that needs on-device macrobenchmarking (Android) or the frame-time probe in the
sample app. See the roadmap for the on-device benchmark + baseline-profile follow-up.
