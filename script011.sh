#!/bin/bash

set -eo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# Java17 toolchain: prefer java17 JVM for Maven build
export JAVA_HOME="${JAVA_HOME:-/usr/local/java17}"
export PATH="$JAVA_HOME/bin:$PATH"

# Resolve Maven: PATH first, then common known locations
resolve_mvn() {
  if command -v mvn >/dev/null 2>&1; then
    echo "mvn"
    return
  fi
  for candidate in /usr/local/maven/bin/mvn /opt/maven*/bin/mvn /usr/share/maven/bin/mvn; do
    if [ -x "$candidate" ]; then
      echo "$candidate"
      return
    fi
  done
  echo ""
}

MVN_BIN="$(resolve_mvn)"
if [ -z "$MVN_BIN" ]; then
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: mvn not found. Install Maven or edit resolve_mvn() in script011.sh ..." >&2
  exit 1
fi

NOW() {
  date '+%Y-%m-%d %H:%M:%S'
}

usage() {
  cat <<EOF
Usage: $0 [command]

Commands:
  (default)  gate + version bump + commit + push
  gate       push gate only: standards check (node) + mvn offline compile
  build011   mvn clean package install (full build + install to local repo)
EOF
}

# gate: standards check + offline compile (single gate implementation, shared
# with the git pre-push hook — do NOT duplicate it elsewhere)
do_gate() {
  echo "[$(NOW)] Standards check (node) ..."
  node "$PROJECT_ROOT/tools/check-klsjnh-standards.mjs" "$PROJECT_ROOT"

  echo ""
  echo "[$(NOW)] Maven offline compile ..."
  "$MVN_BIN" -o compile -q
  echo "[$(NOW)] Gate PASSED ..."
}

# build011: full build and install
do_build011() {
  echo "[$(NOW)] mvn clean package install ..."
  "$MVN_BIN" clean package install "$@"
}

# The gate is enforced by the single commit entry (this script, default
# command) — no git hooks layer (by design).

# default: gate + version bump + commit + push
do_push() {
  do_gate

  # Commit and push
  echo ""
  echo "[$(NOW)] Preparing commit ..."
  vf="$PROJECT_ROOT/.vf"
  [ -f "$vf" ] || echo 0 > "$vf"
  HAS_CHANGES=$(git status --porcelain | wc -l)
  HAS_UNPUSHED=$(git log @{u}..HEAD --oneline 2>/dev/null | wc -l)

  if [ "$HAS_CHANGES" -eq 0 ] && [ "$HAS_UNPUSHED" -eq 0 ]; then
    echo "[$(NOW)] nothing to change ..."
    exit 0
  fi

  if [ "$HAS_CHANGES" -gt 0 ]; then
    v=$(expr $(cat "$vf") + 1)
    echo "$v" > "$vf"
    git add .
    git commit -m "ver 0.0.$v ..."
    echo "[$(NOW)] Commit done"
  fi

  if [ "$HAS_UNPUSHED" -gt 0 ] || [ "$HAS_CHANGES" -gt 0 ]; then
    git push
    echo "[$(NOW)] Push done"
  fi
}

case "${1:-}" in
  gate)
    do_gate
    ;;
  build011)
    shift
    do_build011 "$@"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    do_push
    ;;
esac
