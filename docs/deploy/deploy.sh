#!/bin/bash
# ============================================================
# deploy.sh — klsjnh-java17-framework011 部署（docker compose）
# 约定对齐 /klsjnh/docker/compose.yaml：networks service011 + ports 23333:11160
# 用法:  bash docs/deploy/deploy.sh [mount|bake]      (默认 mount)
#   mount = 轻量挂载（jar 从宿主挂入，换 jar 只重启）
#   bake  = 依赖拷进镜像（构建项目镜像）
# 前置:  基镜像 klsjnh/java17:v0.0.1（缺则 bash docs/deploy/build-base.sh）
# 说明:  服务块在 docs/deploy/docker-compose*.yml（追加到 /klsjnh/docker/compose.yaml）
#        运行时目录 /klsjnh/docker/java011/<项目>/（app.jar + config + logs + data + storage011）
# ============================================================
set -e

MODE="${1:-mount}"
[ "$MODE" = "mount" ] || [ "$MODE" = "bake" ] || { echo "用法: $0 [mount|bake]"; exit 1; }

PROJECT="klsjnh-java17-framework011"
APP_JAR="java17-reference-app011/target/java17-reference-app011-1.0.0.jar"
BASE_IMAGE="${BASE_IMAGE:-klsjnh/java17:v0.0.1}"
DOCKER_ROOT="${DOCKER_ROOT:-/klsjnh/docker}"
PROJECT_DIR="$DOCKER_ROOT/java011/$PROJECT"
COMPOSE_FILE="$DOCKER_ROOT/compose.yaml"
DEPLOY_DIR="docs/deploy"

DOCKER="docker"; docker info >/dev/null 2>&1 || DOCKER="sudo docker"
# 本脚本在 docs/deploy/，项目根为其上两级
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"; cd "$ROOT"

echo "=== 0. 基镜像: $BASE_IMAGE ==="
$DOCKER images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${BASE_IMAGE}$" \
    || { echo "缺基镜像，请先: bash docs/deploy/build-base.sh"; exit 1; }

echo "=== 1. 编译 ==="
mvn -o clean package -DskipTests
[ -f "$APP_JAR" ] || { echo "jar 未找到：$APP_JAR"; exit 1; }

echo "=== 2. 版本（git commit ver x.x.x）==="
VER="$(git log -1 --format=%s | grep -oP 'ver \K\d+\.\d+\.\d+' || true)"
[ -n "$VER" ] || VER="1.0.0"

echo "=== 3. 运行时目录: $PROJECT_DIR ==="
mkdir -p "$PROJECT_DIR/config" "$PROJECT_DIR/logs" "$PROJECT_DIR/data" "$PROJECT_DIR/storage011"
cp "$APP_JAR" "$PROJECT_DIR/app.jar"
cp java17-reference-app011/src/main/resources/application.yml "$PROJECT_DIR/config/application.yml"
[ -f java17-reference-app011/src/main/resources/application-development.yml ] \
    && cp java17-reference-app011/src/main/resources/application-development.yml "$PROJECT_DIR/config/application-development.yml"
# 约定：容器内可挂载目录一律在 /klsjnh/volume 下——存储中心 base-path 统一指过去
sed -i 's#base-path: \./storage011#base-path: /klsjnh/volume/storage011#' "$PROJECT_DIR/config/application.yml"
chmod -R 777 "$PROJECT_DIR/logs" "$PROJECT_DIR/data" "$PROJECT_DIR/storage011"

if [ "$MODE" = "bake" ]; then
    echo "=== 4. 构建项目镜像（bake）==="
    mkdir -p "$DEPLOY_DIR/runtime" && cp "$APP_JAR" "$DEPLOY_DIR/runtime/app.jar"
    $DOCKER build -t "${PROJECT}:v${VER}" -f "$DEPLOY_DIR/Dockerfile.project" "$DEPLOY_DIR/runtime"
    echo "镜像: ${PROJECT}:v${VER}"
else
    echo "=== 4. 轻量挂载（mount，不建项目镜像）==="
fi

echo ""
echo "=== 5. 完成（mode=$MODE，ver=$VER）==="
echo "运行时: $PROJECT_DIR （app.jar / config/ / logs/ / data/ / storage011/）"
echo ""
echo "把 $DEPLOY_DIR/docker-compose${MODE/bake/.bake}.yml 的 services 段追加到 $COMPOSE_FILE 后："
echo "  docker compose -f $COMPOSE_FILE up -d"
echo "  docker compose -f $COMPOSE_FILE logs -f $PROJECT"
