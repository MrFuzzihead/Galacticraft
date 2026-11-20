#!/usr/bin/env bash
# Rebase the non-GTNH restoration branch onto the latest GTNH master and build the mod.
# Run from the repository root:  bash scripts/rebase-gtnh.sh
set -euo pipefail

FEATURE="${FEATURE_BRANCH:-gtnh-compat}"
UPSTREAM="${UPSTREAM_REMOTE:-upstream}"
FORK="${FORK_REMOTE:-origin}"

git config rerere.enabled true
git config rerere.autoupdate true

echo "==> Fetching ${UPSTREAM}"
git fetch "$UPSTREAM" --prune

# The fork remote is optional: it is only useful for seeing what is already pushed.
if git remote get-url "$FORK" >/dev/null 2>&1; then
    echo "==> Fetching ${FORK}"
    git fetch "$FORK" --prune
else
    echo "==> No '${FORK}' remote; skipping fork fetch (set FORK_REMOTE to override)"
fi

echo "==> Rebasing ${FEATURE} onto ${UPSTREAM}/master"
git switch "$FEATURE"
git rebase "${UPSTREAM}/master"

echo "==> Building"
./gradlew build

if [ -n "$(git status --porcelain)" ]; then
    echo "WARNING: working tree is not clean; the jar will have a '-dirty' suffix."
fi

echo
NEWEST=$(ls -1t build/libs/*.jar 2>/dev/null | grep -vE -- '(-dev|-sources|-api)\.jar$' | head -1 || true)
echo "==> Newest release jar: ${NEWEST:-<none>}"
echo "    (full list: build/libs/)"
echo
echo "Copy that jar into your modpack's mods/ folder."
