#!/bin/bash
# ============================================================
# script011.sh — 项目统一入口（提交 / 构建 / 运行态）
#
# 用法:
#   ./script011.sh              空参 = git 提交（gate + 版本号自增 + commit + push）
#   ./script011.sh gate         仅规范检查（node）+ mvn 离线编译
#   ./script011.sh build011     mvn clean package install（全量构建 + 装本地仓库）
#   ./script011.sh dev011       杀进程 → 直接执行现有 jar（不编译）
#   ./script011.sh dev013       杀进程 → 重新编译（clean，旧 jar 一并删除）→ 执行 jar
#   ./script011.sh stop         停止
#   ./script011.sh restart      重启（先杀进程再执行现有 jar）
#   ./script011.sh status       查看状态
#   ./script011.sh log          跟踪日志
#
# 注: 本脚本是项目唯一操作入口（提交 / 构建 / 运行态）。
# ============================================================

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
  dev011     杀进程 + 直接执行现有 jar（不编译）
  dev013     杀进程 + 重新编译（clean 删旧 jar）+ 执行 jar
  stop|restart|status|log   运行态管理
EOF
}

# ============================================================
# git: gate + version bump + commit + push
# ============================================================

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

# ============================================================
# 运行态（app011 启停）
# ============================================================

APP_JAR="$PROJECT_ROOT/java17-app011/target/java17-app011-1.0.0.jar"
APP_LOG="$PROJECT_ROOT/logs/app011.log"
PID_FILE="$PROJECT_ROOT/.app011.pid"
APP_PORT=11610

# debug 模式：development profile + krt.status=debug（application.yml 默认即 debug）
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

# 按端口找占用进程（PID 文件丢失 / 外部启动时的兜底）
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
    echo "已在运行 (PID $(cat "$PID_FILE"))，无需启动 ..."
    exit 0
  fi

  if [ ! -f "$APP_JAR" ]; then
    echo "JAR 不存在：$APP_JAR"
    echo "  请先构建：./script011.sh dev013（重新编译）或 ./script011.sh build011"
    exit 1
  fi

  mkdir -p "$PROJECT_ROOT/logs"
  echo "启动 app011（profile=$PROFILE，krt.status=debug）..."
  nohup "$JAVA_BIN" -jar "$APP_JAR" --spring.profiles.active="$PROFILE" > "$APP_LOG" 2>&1 &
  echo $! > "$PID_FILE"

  if wait_up; then
    echo "启动成功 (PID $(cat "$PID_FILE"))"
    echo "  服务:   http://127.0.0.1:$APP_PORT"
    echo "  文档:   http://127.0.0.1:$APP_PORT/doc.html"
    echo "  Druid:  http://127.0.0.1:$APP_PORT/druid  (klsjnh/klsjnh)"
    echo "  日志:   tail -f $APP_LOG"
  else
    echo "启动失败（30s 内未就绪），查看 $APP_LOG"
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

# 提前杀进程：PID 文件优先，端口占用兜底（先 TERM 优雅关停，超时再 -9）
do_stop() {
  stopped=0

  if [ -f "$PID_FILE" ]; then
    pid="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      echo "停止 app011 (PID $pid) ..."
      kill "$pid" 2>/dev/null || true

      # 等优雅关停（释放端口）真正退出，再起新实例，避免端口占用
      if ! wait_down "$pid"; then
        echo "关停超时，强制终止 ..."
        kill -9 "$pid" 2>/dev/null || true
        wait_down "$pid" || true
      fi

      stopped=1
    fi
    rm -f "$PID_FILE"
  fi

  # 兜底：端口仍被占用（PID 文件丢失、被外部启动）时按端口清理
  for p in $(port_pids); do
    echo "端口 $APP_PORT 被 PID $p 占用，终止 ..."
    kill "$p" 2>/dev/null || true
    sleep 2
    kill -0 "$p" 2>/dev/null && kill -9 "$p" 2>/dev/null || true
    stopped=1
  done

  if [ "$stopped" -eq 0 ]; then
    echo "未在运行"
  else
    wait_port_free || echo "警告: 端口 $APP_PORT 仍未释放"
    echo "已停止"
  fi
}

# dev011: 直接执行现有 jar（不编译）
do_dev011() {
  do_stop
  do_start
}

# dev013: clean 重编译（旧 jar 一并删除）后执行
do_dev013() {
  do_stop

  echo "[$(NOW)] 重新编译: mvn -o clean package -DskipTests ..."
  (cd "$PROJECT_ROOT" && "$MVN_BIN" -o clean package -DskipTests)

  if [ ! -f "$APP_JAR" ]; then
    echo "编译后仍未生成 JAR：$APP_JAR"
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
      echo "运行中 (PID $(cat "$PID_FILE"))"
    else
      echo "未在运行"
    fi
    ;;
  log)
    tail -f "${2:-$APP_LOG}"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    echo "未知命令: $1"
    echo ""
    usage
    exit 1
    ;;
esac
