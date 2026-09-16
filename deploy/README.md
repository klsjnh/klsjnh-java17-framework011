# deploy — 容器化部署（docker compose）

> **机制与决策的唯一家在** [docs/infrastructure011/020.topic-deploy-docker.md](../docs/infrastructure011/020.topic-deploy-docker.md)。本文件只给最快上手命令，不复制正文。

## 最快上手

```bash
cd <项目根>
bash deploy/build-base.sh          # 基镜像 klsjnh/java17:v0.0.1（缺则建，ubuntu 26.04 + JDK17）
bash deploy/deploy.sh              # 默认 mount（轻量挂载）→ docker compose up -d
# 或 bash deploy/deploy.sh bake    # 依赖拷进镜像

docker compose -f deploy/docker-compose.yml ps
docker compose -f deploy/docker-compose.yml logs -f app
docker compose -f deploy/docker-compose.yml down
# 访问 http://<host>:23333/doc.html
```

## 文件

| 文件 | 用途 |
|------|------|
| `build-base.sh` / `Dockerfile.base` | 基镜像（JDK 介质：MinIO→本地，参数走环境变量） |
| `deploy.sh` | 编译 → runtime → `docker compose up -d` |
| `docker-compose.yml` | **方案二 mount（默认）** |
| `docker-compose.bake.yml` / `Dockerfile.project` | 方案一 bake |

> 脱敏：`deploy/` 无内网 IP / 凭据；`runtime/` gitignore；MinIO 参数用 `MINIO_*` 环境变量。
