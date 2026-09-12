#!/usr/bin/env bash
# One-time setup: enable .githooks so commit/push are blocked on check failure.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

chmod +x "$ROOT/.githooks/pre-commit" "$ROOT/.githooks/pre-push" "$ROOT/tools/push-gate.sh" 2>/dev/null || true

git config core.hooksPath .githooks

echo "Git hooks installed (core.hooksPath=.githooks)"
echo "  pre-commit: klsjnh standards check — fail = no commit"
echo "  pre-push:   check + mvn compile — fail = no push"
