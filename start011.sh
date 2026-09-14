#!/bin/bash
# ============================================================
# start011.sh — 后端启动脚本（app011，debug 模式）
# 用法:
#   ./start011.sh          启动（已运行则提示）
#   ./start011.sh stop     停止
#   ./start011.sh restart  重启
#   ./start011.sh status   查看状态
#   ./start011.sh log      跟踪日志
# ============================================================

set -eo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
JAR="$ROOT/java17-app011/target/java17-app011-1.0.0.jar"
LOG="$ROOT/logs/app011.log"
PID_FILE="$ROOT/.app011.pid"

mkdir -p "$ROOT/logs"

# debug 模式：development profile + krt.status=debug（application.yml 默认即 debug）
PROFILE="development"

is_running() {
    [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

wait_up() {
    for i in $(seq 1 30); do
        if grep -q "Started Framework011Application" "$LOG" 2>/dev/null; then
            return 0
        fi
        sleep 1
    done

    return 1
}

do_start() {
    if is_running; then
        echo "已在运行 (PID $(cat "$PID_FILE"))，无需启动 ..."
        exit 0
    fi

    if [ ! -f "$JAR" ]; then
        echo "JAR 不存在，先构建 ..."
        (cd "$ROOT" && mvn -o -q clean package -DskipTests)
    fi

    mkdir -p "$ROOT/logs"
    echo "启动 app011（profile=$PROFILE，krt.status=debug）..."
    nohup java -jar "$JAR" --spring.profiles.active="$PROFILE" > "$LOG" 2>&1 &
    echo $! > "$PID_FILE"

    if wait_up; then
        echo "启动成功 (PID $(cat "$PID_FILE"))"
        echo "  服务:   http://127.0.0.1:11610"
        echo "  文档:   http://127.0.0.1:11610/doc.html"
        echo "  Druid:  http://127.0.0.1:11610/druid  (klsjnh/klsjnh)"
        echo "  日志:   tail -f $LOG"
    else
        echo "启动失败（30s 内未就绪），查看 $LOG"
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

do_stop() {
    if is_running; then
        pid="$(cat "$PID_FILE")"
        echo "停止 app011 (PID $pid) ..."
        kill "$pid"

        # 等优雅关停（释放 11610 端口）真正退出，再起新实例，避免端口占用
        if ! wait_down "$pid"; then
            echo "关停超时，强制终止 ..."
            kill -9 "$pid" 2>/dev/null || true
            wait_down "$pid" || true
        fi

        rm -f "$PID_FILE"
        echo "已停止"
    else
        echo "未在运行"
        rm -f "$PID_FILE"
    fi
}

case "${1:-start}" in
    start)
        do_start
        ;;
    stop)
        do_stop
        ;;
    restart)
        do_stop
        do_start
        ;;
    status)
        if is_running; then
            echo "运行中 (PID $(cat "$PID_FILE"))"
        else
            echo "未在运行"
        fi
        ;;
    log)
        tail -f "${2:-$LOG}"
        ;;
    *)
        echo "用法: $0 [start|stop|restart|status|log]"
        exit 1
        ;;
esac
