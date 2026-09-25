#!/bin/bash
# ============================================================
# script011.sh — project single entry point (commit / build / runtime)
#
# Usage:
#   ./script011.sh              no arg = git commit (gate + version bump + commit + push)
#   ./script011.sh gate         standards check only (node) + mvn offline compile
#   ./script011.sh build011     mvn clean package install (full build + install to local repo)
#   ./script011.sh dev011       kill process -> run the existing jar (no compile)
#   ./script011.sh dev013       kill process -> clean rebuild (drops old jar) -> run jar
#   ./script011.sh stop         stop
#   ./script011.sh restart      restart (kill then run the existing jar)
#   ./script011.sh status       show status
#   ./script011.sh log          tail the log
#
# Note: this script is the project's single entry point (commit / build / runtime).
# ============================================================

set -eo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# Java17 toolchain: prefer the java17 JVM for the Maven build
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

JAVA_BIN="java"
[ -x "$JAVA_HOME/bin/java" ] && JAVA_BIN="$JAVA_HOME/bin/java"

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
  dev011     kill process + run the existing jar (no compile)
  dev013     kill process + clean rebuild (drops old jar) + run jar
  stop|restart|status|log   runtime management
EOF
}

# ============================================================
# git: gate + version bump + commit + push
# ============================================================

# gate: standards check + offline compile (single gate implementation, shared
# with the git pre-push hook — do NOT duplicate it elsewhere)
do_gate() {
  echo "[$(NOW)] Standards check (node) ..."
  if [ ! -d "$PROJECT_ROOT/tools/node_modules/tree-sitter" ]; then
    echo "ERROR: tools/node_modules missing tree-sitter — run: npm --prefix tools install" >&2
    echo "       (Node 18+; e.g. nvm at D:/Environment/nvm)" >&2
    exit 1
  fi
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

# Version bump: major.minor.patch; each step patch+=1;
# patch>99 → patch=0, minor+=1; minor>99 → minor=0, major+=1.
# .vf stores "X.Y.Z"; legacy single integer N is treated as 0.0.N.
bump_version() {
  local raw major minor patch
  raw="$(echo "$1" | tr -d '[:space:]')"
  [ -n "$raw" ] || raw="0"
  if [[ "$raw" =~ ^[0-9]+$ ]]; then
    major=0; minor=0; patch="$raw"
  elif [[ "$raw" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    major="${BASH_REMATCH[1]}"; minor="${BASH_REMATCH[2]}"; patch="${BASH_REMATCH[3]}"
  else
    echo "ERROR: invalid .vf version '$raw' (want N or X.Y.Z)" >&2
    exit 1
  fi
  patch=$((patch + 1))
  if [ "$patch" -gt 99 ]; then
    patch=0
    minor=$((minor + 1))
  fi
  if [ "$minor" -gt 99 ]; then
    minor=0
    major=$((major + 1))
  fi
  echo "${major}.${minor}.${patch}"
}

# default: gate + version bump + commit + push
do_push() {
  do_gate

  # Commit and push
  echo ""
  echo "[$(NOW)] Preparing commit ..."
  vf="$PROJECT_ROOT/.vf"
  [ -f "$vf" ] || echo "0.0.0" > "$vf"
  HAS_CHANGES=$(git status --porcelain | wc -l)
  HAS_UNPUSHED=$(git log @{u}..HEAD --oneline 2>/dev/null | wc -l || echo 0)

  if [ "$HAS_CHANGES" -eq 0 ] && [ "$HAS_UNPUSHED" -eq 0 ]; then
    echo "[$(NOW)] nothing to change ..."
    exit 0
  fi

  if [ "$HAS_CHANGES" -gt 0 ]; then
    v="$(bump_version "$(cat "$vf")")"
    echo "$v" > "$vf"
    git add .
    git commit -m "ver $v ..."
    echo "[$(NOW)] Commit done"
  fi

  if [ "$HAS_UNPUSHED" -gt 0 ] || [ "$HAS_CHANGES" -gt 0 ]; then
    git push
    echo "[$(NOW)] Push done"
  fi
}

# ============================================================
# runtime (app011 start / stop)
# ============================================================

APP_JAR="$PROJECT_ROOT/java17-reference-app011/target/java17-reference-app011-1.0.0.jar"
APP_LOG="$PROJECT_ROOT/logs/app011.log"
PID_FILE="$PROJECT_ROOT/.app011.pid"
APP_PORT=11160

# debug mode: development profile + krt.status=debug (application.yml defaults to debug)
PROFILE="development"

is_running() {
  [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

wait_up() {
  for i in $(seq 1 30); do
    if grep -q "Started Framework011Application" "$APP_LOG" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done

  return 1
}

# Find the process holding the port (fallback when the PID file is missing or
# the app was started externally)
port_pids() {
  if command -v lsof >/dev/null 2>&1; then
    lsof -ti tcp:"$APP_PORT" 2>/dev/null || true
  elif command -v fuser >/dev/null 2>&1; then
    fuser "$APP_PORT"/tcp 2>/dev/null | tr -s ' ' '\n' | grep -E '^[0-9]+$' || true
  elif command -v ss >/dev/null 2>&1; then
    ss -lptn "sport = :$APP_PORT" 2>/dev/null | grep -o 'pid=[0-9]*' | cut -d= -f2 || true
  fi
  return 0
}

wait_port_free() {
  for i in $(seq 1 15); do
    [ -z "$(port_pids)" ] && return 0
    sleep 1
  done

  return 1
}

do_start() {
  if is_running; then
    echo "already running (PID $(cat "$PID_FILE")), nothing to start ..."
    exit 0
  fi

  if [ ! -f "$APP_JAR" ]; then
    echo "JAR not found: $APP_JAR"
    echo "  build it first: ./script011.sh dev013 (rebuild) or ./script011.sh build011"
    exit 1
  fi

  mkdir -p "$PROJECT_ROOT/logs"
  echo "starting app011 (profile=$PROFILE, krt.status=debug) ..."
  nohup "$JAVA_BIN" -jar "$APP_JAR" --spring.profiles.active="$PROFILE" > "$APP_LOG" 2>&1 &
  echo $! > "$PID_FILE"

  if wait_up; then
    echo "started (PID $(cat "$PID_FILE"))"
    echo "  service: http://127.0.0.1:$APP_PORT"
    echo "  docs:    http://127.0.0.1:$APP_PORT/doc.html"
    echo "  Druid:   http://127.0.0.1:$APP_PORT/druid  (klsjnh/klsjnh)"
    echo "  log:     tail -f $APP_LOG"
  else
    echo "failed to start (not ready within 30s), see $APP_LOG"
    exit 1
  fi
}

wait_down() {
  pid="$1"

  for i in $(seq 1 30); do
    kill -0 "$pid" 2>/dev/null || return 0
    sleep 1
  done

  return 1
}

# Kill early: PID file first, port holder as fallback (TERM for a graceful
# shutdown, then -9 after the timeout)
do_stop() {
  stopped=0

  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "stopping app011 (PID $pid) ..."
      kill "$pid" 2>/dev/null || true

      # Wait for the graceful shutdown (port released) before starting a new
      # instance, so the port is not held.
      if ! wait_down "$pid"; then
        echo "shutdown timed out, force killing ..."
        kill -9 "$pid" 2>/dev/null || true
        wait_down "$pid" || true
      fi

      stopped=1
    fi
    rm -f "$PID_FILE"
  fi

  # Fallback: the port is still held (PID file lost, started externally)
  for p in $(port_pids); do
    echo "port $APP_PORT held by PID $p, killing ..."
    kill "$p" 2>/dev/null || true
    sleep 2
    kill -0 "$p" 2>/dev/null && kill -9 "$p" 2>/dev/null || true
    stopped=1
  done

  if [ "$stopped" -eq 0 ]; then
    echo "not running"
  else
    wait_port_free || echo "warning: port $APP_PORT still not released"
    echo "stopped"
  fi
}

# dev011: run the existing jar (no compile)
do_dev011() {
  do_stop
  do_start
}

# dev013: clean rebuild (drops old jar) then run
do_dev013() {
  do_stop

  echo "[$(NOW)] rebuilding: mvn -o clean package -DskipTests ..."
  (cd "$PROJECT_ROOT" && "$MVN_BIN" -o clean package -DskipTests)

  if [ ! -f "$APP_JAR" ]; then
    echo "no JAR produced after build: $APP_JAR"
    exit 1
  fi

  do_start
}

case "${1:-}" in
  "")
    do_push
    ;;
  push|commit)
    do_push
    ;;
  gate)
    do_gate
    ;;
  build011)
    shift
    do_build011 "$@"
    ;;
  dev011)
    do_dev011
    ;;
  dev013)
    do_dev013
    ;;
  start|restart)
    do_dev011
    ;;
  stop)
    do_stop
    ;;
  status)
    if is_running; then
      echo "running (PID $(cat "$PID_FILE"))"
    else
      echo "not running"
    fi
    ;;
  log)
    tail -f "${2:-$APP_LOG}"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    echo "unknown command: $1"
    echo ""
    usage
    exit 1
    ;;
esac
