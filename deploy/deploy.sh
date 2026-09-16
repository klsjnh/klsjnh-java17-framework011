#!/bin/bash
# ============================================================
# deploy.sh — klsjnh-java17-framework011 Docker 部署（docker compose）
# 用法:  bash deploy/deploy.sh [mount|bake]      (默认 mount)
#   mount = 方案二：轻量挂载（jar 从 target 挂入，换 jar 只重启）→ deploy/docker-compose.yml
#   bake  = 方案一：依赖拷贝进镜像（构建项目镜像）           → deploy/docker-compose.bake.yml
# 前置:  基镜像 klsjnh/java17:v0.0.1（缺则 bash deploy/build-base.sh）
# 说明:  编排在项目内（不依赖 /klsjnh/docker）；无内网地址 / 凭据入库
# ============================================================
set -e

MODE="${1:-mount}"
[ "$MODE" = "mount" ] || [ "$MODE" = "bake" ] || { echo "用法: $0 [mount|bake]"; exit 1; }

PROJECT="klsjnh-java17-framework011"
APP_JAR="java17-app011/target/java17-app011-1.0.0.jar"
BASE_IMAGE="${BASE_IMAGE:-klsjnh/java17:v0.0.1}"

DOCKER="docker"
docker info >/dev/null 2>&1 || DOCKER="sudo docker"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== 0. 基镜像: $BASE_IMAGE ==="
$DOCKER images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${BASE_IMAGE}$" \
    || { echo "缺基镜像，请先: bash deploy/build-base.sh"; exit 1; }

echo "=== 1. 编译 ==="
mvn -o clean package -DskipTests
[ -f "$APP_JAR" ] || { echo "jar 未找到：$APP_JAR"; exit 1; }

echo "=== 2. 版本（git commit ver x.x.x）==="
VER="$(git log -1 --format=%s | grep -oP 'ver \K\d+\.\d+\.\d+' || true)"
[ -n "$VER" ] || { read -rp "未取到版本，请输入(如 0.0.17): " VER; }

echo "=== 3. runtime 目录 ==="
mkdir -p deploy/runtime/logs deploy/runtime/data deploy/runtime/storage011
chmod -R 777 deploy/runtime

if [ "$MODE" = "bake" ]; then
    echo "=== 4. 构建项目镜像（bake）==="
    cp "$APP_JAR" deploy/runtime/app_v${VER}.jar
    $DOCKER build -t "${PROJECT}:v${VER}" -f deploy/Dockerfile.project deploy/runtime
    export APP_IMAGE="${PROJECT}:v${VER}"
    COMPOSE="deploy/docker-compose.bake.yml"
else
    echo "=== 4. 轻量挂载（mount，不建项目镜像）==="
    COMPOSE="deploy/docker-compose.yml"
fi

echo "=== 5. docker compose up ==="
$DOCKER compose -f "$COMPOSE" up -d
$DOCKER compose -f "$COMPOSE" ps

echo ""
echo "=== 6. 完成（mode=$MODE，ver=$VER）==="
echo "访问:  http://127.0.0.1:23333/doc.html"
echo "日志:  $DOCKER compose -f $COMPOSE logs -f app    （文件：deploy/runtime/logs/）"
