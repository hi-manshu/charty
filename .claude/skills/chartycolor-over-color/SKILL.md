---
name: chartycolor-over-color
description: Public color API convention for Charty — use when adding or changing any public config property, data-class field, or composable parameter that sets the color of a chart element (fill, stroke, series, segment, arc, band, needle, point), and before committing. Such colors must be typed as ChartyColor (Solid or Gradient), never a raw androidx.compose.ui.graphics.Color. Trigger on "add a config", "new chart", "expose a color", "gauge/arc/zone color".
---

# Prefer ChartyColor over raw Color in public APIs

Charty's colors are expressed with `com.himanshoe.charty.color.ChartyColor` — a sealed type with
`Solid(color)` and `Gradient(colors)`. Every color a caller can set for a drawn chart element goes
through it, so any element can be a solid **or** a gradient, and the API stays consistent.

## Rule

**A public `val`/parameter that colors a chart element must be `ChartyColor`, not `Color`.** This
covers fills, strokes, series/segment/arc/band/needle/point/zone colors — anything painted.

```kotlin
// Do
val fillColor: ChartyColor = ChartyColor.Solid(Color(0xFF1E88E5))
val zones: List<GaugeZone>            // where GaugeZone.color: ChartyColor

// Don't
val fillColor: Color = Color(0xFF1E88E5)
```

Resolve to a `Brush` at draw time (private helper, at the drawing site):

```kotlin
private fun ChartyColor.toBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.verticalGradient(colors)
    }
```

A default ARGB literal lives in a `private const val ...ARGB = 0xFF……` (avoids detekt MagicNumber),
wrapped as `ChartyColor.Solid(Color(THE_ARGB))`.

## Not covered (raw Color is fine here)

- **`TextStyle`** — it already carries its own `Color`; keep using `TextStyle`.
- **Internal / private drawing functions** — may take a resolved `Color` or `Brush` parameter; the
  rule is about the **public** surface callers configure.
- **Pre-existing raw-`Color` properties** (`ChartScaffoldConfig.axisColor`/`gridColor`,
  `ReferenceLineConfig.color`/`borderColor`, `PersistentMarker.guideLineColor`,
  crosshair line colors): legacy. Don't sweep them just to change types, but when you add a **new**
  color property next to them, make the new one `ChartyColor`.

## Applying it

- New config/data/composable color property → type it `ChartyColor`, default a `Solid(...)`.
- Add a private `toBrush()` (or reuse an existing one in that drawer) and paint with `brush = ...`.
- **Commit gate:** a staged diff that adds a public `Color`-typed color property (outside the
  exemptions above) is not ready — switch it to `ChartyColor`. `commit-with-stats` checks for it.

Quick self-check on a staged diff:
```bash
git diff --cached -U0 -- '*/config/*.kt' '*/data/*.kt' | grep -E '^\+' | grep -E 'val [A-Za-z]*[Cc]olor[A-Za-z]*: Color'
```
Every hit should be justified by an exemption above; otherwise make it `ChartyColor`.
