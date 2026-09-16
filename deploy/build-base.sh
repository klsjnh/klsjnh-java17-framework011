#!/bin/bash
# ============================================================
# build-base.sh — 构建基镜像 klsjnh/java17:v0.0.1（ubuntu:26.04 + JDK17）
# 已存在则跳过（--force 重建）
#
# JDK 介质来源优先级：
#   1) MEDIA_DIR 已有介质（直接用）
#   2) 从 MinIO 桶 MINIO_BUCKET 下载（mc 或 python3-minio）—— 需通过环境变量提供
#      MINIO_ENDPOINT / MINIO_BUCKET / MINIO_ACCESS_KEY / MINIO_SECRET_KEY（不入库）
#   3) 回退：从 JDK_SRC 打包本机 JDK（默认 /usr/local/java17）
# ============================================================
set -e

BASE_IMAGE="${BASE_IMAGE:-klsjnh/java17:v0.0.1}"
MEDIA_DIR="${MEDIA_DIR:-/data/media011}"
JDK_FILE="${JDK_FILE:-jdk-17.0.12_linux-x64_bin.tar.gz}"
JDK_SRC="${JDK_SRC:-/usr/local/java17}"

# MinIO 参数一律来自环境变量（凭据 / 内网地址不入库）
MINIO_ENDPOINT="${MINIO_ENDPOINT:-}"
MINIO_BUCKET="${MINIO_BUCKET:-}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-}"

FORCE="${1:-}"
DOCKER="docker"; docker info >/dev/null 2>&1 || DOCKER="sudo docker"
HERE="$(cd "$(dirname "$0")" && pwd)"

if [ "$FORCE" != "--force" ] \
    && $DOCKER images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${BASE_IMAGE}$"; then
    echo "基镜像已存在：$BASE_IMAGE（--force 可重建）"
    exit 0
fi

mkdir -p "$MEDIA_DIR"

fetch_jdk() {
    [ -f "$MEDIA_DIR/$JDK_FILE" ] && { echo "介质已存在：$MEDIA_DIR/$JDK_FILE"; return 0; }

    if [ -z "$MINIO_ENDPOINT" ] || [ -z "$MINIO_BUCKET" ] \
        || [ -z "$MINIO_ACCESS_KEY" ] || [ -z "$MINIO_SECRET_KEY" ]; then
        echo "未提供 MinIO 参数（MINIO_ENDPOINT/BUCKET/ACCESS_KEY/SECRET_KEY），回退打包本机 JDK"
        tar -czf "$MEDIA_DIR/$JDK_FILE" -C "$(dirname "$JDK_SRC")" "$(basename "$JDK_SRC")"
        return 0
    fi

    if command -v mc >/dev/null 2>&1; then
        echo "从 MinIO 下载（mc）：$MINIO_ENDPOINT/$MINIO_BUCKET/$JDK_FILE"
        mc alias set klsjnh011 "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY" >/dev/null
        mc cp "klsjnh011/$MINIO_BUCKET/$JDK_FILE" "$MEDIA_DIR/"
        return 0
    fi

    if python3 -c "import minio" >/dev/null 2>&1; then
        echo "从 MinIO 下载（python-minio）：$MINIO_ENDPOINT/$MINIO_BUCKET/$JDK_FILE"
        MINIO_ENDPOINT="$MINIO_ENDPOINT" MINIO_BUCKET="$MINIO_BUCKET" \
        MINIO_ACCESS_KEY="$MINIO_ACCESS_KEY" MINIO_SECRET_KEY="$MINIO_SECRET_KEY" \
        JDK_FILE="$JDK_FILE" MEDIA_DIR="$MEDIA_DIR" python3 - <<'PY'
import os
from minio import Minio
ep = os.environ["MINIO_ENDPOINT"].replace("http://", "").replace("https://", "")
c = Minio(ep, access_key=os.environ["MINIO_ACCESS_KEY"], secret_key=os.environ["MINIO_SECRET_KEY"], secure=False)
c.fget_object(os.environ["MINIO_BUCKET"], os.environ["JDK_FILE"], os.path.join(os.environ["MEDIA_DIR"], os.environ["JDK_FILE"]))
print("downloaded")
PY
        return 0
    fi

    echo "无 mc / python-minio，回退：打包本机 JDK  $JDK_SRC → $MEDIA_DIR/$JDK_FILE"
    tar -czf "$MEDIA_DIR/$JDK_FILE" -C "$(dirname "$JDK_SRC")" "$(basename "$JDK_SRC")"
}

fetch_jdk
cp "$HERE/Dockerfile.base" "$MEDIA_DIR/Dockerfile.base"
cd "$MEDIA_DIR"
$DOCKER build -t "$BASE_IMAGE" -f Dockerfile.base .
echo "=== 验证 ==="
$DOCKER run --rm --entrypoint java "$BASE_IMAGE" -version
echo "基镜像就绪：$BASE_IMAGE"
