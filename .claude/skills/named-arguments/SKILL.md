---
name: named-arguments
description: Call-site convention for the Charty codebase — use when writing or editing Kotlin in charty/ or composeApp/, and before committing. Function/constructor calls with two or more arguments must pass them by name (foo(a = x, b = y)), not positionally, for readability and safe refactoring. Trigger when adding a call, a drawer/helper invocation, or a chart composable call.
---

# Named arguments at call sites

When you **call** a function or constructor with **two or more arguments**, pass them **by name**:

```kotlin
// Do
drawReferenceBand(
    chartContext = chartContext,
    orientation = ChartOrientation.VERTICAL,
    config = band,
    textMeasurer = textMeasurer,
)

// Don't
drawReferenceBand(chartContext, ChartOrientation.VERTICAL, band, textMeasurer)
```

Named arguments make call sites self-documenting, prevent silent breakage when parameters are
reordered, and are mandatory for any boolean or multiple same-typed arguments (where position is
easy to get wrong).

## Scope

Applies to calls with **2+ value arguments**. Exempt (naming adds noise or isn't possible):

- **Single-argument** calls — `remember { … }`, `require(cond)`, `listOf(x)`, `it.copy()`.
- A **trailing lambda** that is the only "argument" written outside the parens —
  `dataList.fastForEachIndexed { i, v -> … }`, `Modifier.drawBehind { … }`. If a call has other
  value args plus a trailing lambda, name the value args: `measure(text = s, style = style)`.
- **Operators / infix** — `a to b`, `a + b`, `x..y`, `a.coerceIn(0f, 1f)` (infix-like stdlib scale
  functions are fine positional).
- **`vararg` spreads** and calls where the callee can't accept names (some Java interop).
- Simple geometry/value **constructors** are encouraged but not required when the order is a strong
  convention: `Offset(x, y)`, `Size(w, h)`, `Color(0xFF…)`, `CornerRadius(r, r)`.

## Applying it

- New/changed multi-arg calls: write them named from the start.
- When you edit a file, name the multi-arg calls in the code you touch (don't rewrite the whole
  file just for this).
- **Commit gate:** a staged diff that adds a positional 2+-arg call (outside the exemptions) is not
  ready. `commit-with-stats` checks for it.

Note: no formatter converts positional calls to named automatically — the parameter names come from
each callee's signature. Adopt this incrementally (every file you touch) rather than in one bulk
rewrite; the gate keeps new code compliant.

Quick check on a staged diff (manual review — the callee's real param names must be filled in):
```bash
git diff --cached -U0 -- '*.kt' | grep -E '^\+' | grep -E '\b[a-z][A-Za-z]*\([^)]*,[^)=]*\)'
```
