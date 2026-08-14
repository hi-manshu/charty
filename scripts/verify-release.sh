#!/usr/bin/env bash
#
# Publishes charty and charty-3d locally, then compiles a consumer against the published
# coordinates. Run it before a release; the release workflow's dry run runs the same two steps.
#
# The point is the second step. Publishing succeeds long before an artifact is usable, and nothing
# in the publish task's output tells you whether a dependency made it into the POM, whether every
# platform variant got built, or whether the two artifacts agree on a version. Compiling against
# them does.
#
#   ./scripts/verify-release.sh                 # the versions in CHANGELOG.md
#   ./scripts/verify-release.sh 3.0.1 1.1.0     # a specific pairing
#
set -euo pipefail

CHARTY_VERSION="${1:-3.0.0}"
CHARTY_3D_VERSION="${2:-1.0.0}"

cd "$(dirname "$0")/.."

echo "📦 Publishing charty:$CHARTY_VERSION and charty-3d:$CHARTY_3D_VERSION to Maven Local..."
./gradlew :charty:publishToMavenLocal :charty3d:publishToMavenLocal \
  -PVERSION_NAME="$CHARTY_VERSION" \
  -PVERSION_NAME_3D="$CHARTY_3D_VERSION" \
  --no-configuration-cache

echo
echo "🔎 Compiling a consumer against the published artifacts..."
./gradlew -p release-verification verifyRelease \
  -PchartyVersion="$CHARTY_VERSION" \
  -Pcharty3dVersion="$CHARTY_3D_VERSION"
