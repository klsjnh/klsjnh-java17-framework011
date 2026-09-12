#!/usr/bin/env bash
# Shared pre-push gate: klsjnh check + mvn compile (Java17/offline env). Exit 1 on any failure.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

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
  echo "ERROR: mvn not found. Install Maven or edit resolve_mvn() in push-gate.sh" >&2
  exit 1
fi

echo "== klsjnh standards check =="
NODE=""
for candidate in node node.exe; do
  if command -v "$candidate" >/dev/null 2>&1; then
    NODE="$candidate"
    break
  fi
done
if [ -z "$NODE" ]; then
  echo "ERROR: node required for klsjnh standards check" >&2
  exit 1
fi
if ! "$NODE" "$ROOT/tools/check-klsjnh-standards.mjs" .; then
  echo "ERROR: klsjnh standards check FAILED" >&2
  exit 1
fi

echo "== mvn compile =="
if ! "$MVN_BIN" -o -q compile; then
  echo "ERROR: mvn compile FAILED" >&2
  exit 1
fi

echo "== push gate PASSED =="
