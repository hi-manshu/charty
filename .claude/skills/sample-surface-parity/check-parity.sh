#!/usr/bin/env bash
# Lists public chart composables that are missing from either sample surface.
set -uo pipefail
charts=$(grep -rhE '^fun [A-Z][A-Za-z0-9]*(Chart|Indicator|Sparkline)\(' \
  charty/src/commonMain/kotlin charty3d/src/commonMain/kotlin 2>/dev/null \
  | sed -E 's/^fun ([A-Za-z0-9]+)\(.*/\1/' | sort -u)
web=$(cat composeApp/src/webMain/kotlin/com/himanshoe/sample/*.kt 2>/dev/null)
gallery=$(cat composeApp/src/commonMain/kotlin/com/himanshoe/sample/ChartGallery.kt 2>/dev/null)
missing=0
printf '%-32s %-6s %-6s\n' CHART WEB MOBILE
for c in $charts; do
  w=$(printf '%s' "$web" | grep -c "\b$c(" || true)
  g=$(printf '%s' "$gallery" | grep -c "\b$c(" || true)
  wm=$([ "$w" -gt 0 ] && echo yes || echo NO)
  gm=$([ "$g" -gt 0 ] && echo yes || echo NO)
  if [ "$w" -eq 0 ] || [ "$g" -eq 0 ]; then missing=$((missing+1)); fi
  printf '%-32s %-6s %-6s\n' "$c" "$wm" "$gm"
done
echo "--- $missing chart(s) missing from a surface ---"
[ "$missing" -eq 0 ]
