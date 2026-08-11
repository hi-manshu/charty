# Third-party skills

The following skill folders are **vendored verbatim** from Chris Banes' skills repository and are
**not** authored by this project:

- `compose-animations/`
- `compose-component-design/`
- `compose-performance/`
- `compose-state-and-effects/`
- `compose-ui-testing-patterns/`
- `kotlin-api-design/`

- **Source:** https://github.com/chrisbanes/skills
- **Commit:** `e04a16e079c578b489d201cbed8a30396e2d67b0` (plugin version `2026.8.5`)
- **Author:** Chris Banes (https://github.com/chrisbanes)
- **License:** Apache License 2.0 — full text in `LICENSE-chrisbanes-skills`.

These folders are copied unmodified. To update them, re-copy from the upstream repository at a newer
commit (or install the upstream Claude Code plugin instead:
`/plugin marketplace add chrisbanes/skills`).

## Project-authored skills (not third-party)

`commit-with-stats/` and `public-api-guard/` are authored by this project. `public-api-guard`
*references* the vendored `compose-performance` and `compose-ui-testing-patterns` skills rather than
duplicating their content.
