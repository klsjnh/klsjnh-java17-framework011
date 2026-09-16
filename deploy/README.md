# deploy — 容器化部署（docker compose）

> 机制与决策的唯一家在 [docs/infrastructure011/020.topic-deploy-docker.md](../docs/infrastructure011/020.topic-deploy-docker.md)。
> **编排约定对齐服务器既有 `/klsjnh/docker/compose.yaml`**：`networks: service011` + `ports` 映射 + `logging`/`healthcheck`。

## 最快上手（服务器）

```bash
cd <项目根>
bash deploy/build-base.sh          # 基镜像 klsjnh/java17:v0.0.1（缺则建，ubuntu 26.04 + JDK17）
bash deploy/deploy.sh              # 默认 mount：编译 + 备 runtime + 打印要追加的服务块
```

`deploy.sh` 会把产物放到 **`/klsjnh/docker/java011/klsjnh-java17-framework011/`**：
`app.jar` · `config/` · `logs/` · `data/` · `storage011/`。

把 `deploy/docker-compose.yml`（mount）或 `docker-compose.bake.yml`（bake）的 **services 段**追加到
`/klsjnh/docker/compose.yaml` 的 `services:` 下（该文件底部已定义 `networks.service011`，**不要重复声明**），然后：

```bash
docker compose -f /klsjnh/docker/compose.yaml up -d
docker compose -f /klsjnh/docker/compose.yaml logs -f klsjnh-java17-framework011
# 访问 http://<host>:23333/doc.html
```

## 文件

| 文件 | 用途 |
|------|------|
| `build-base.sh` / `Dockerfile.base` | 基镜像（JDK 介质：MinIO→本地，参数走环境变量） |
| `deploy.sh` | 编译 → `/klsjnh/docker/java011/<项目>/` → 打印服务块 |
| `docker-compose.yml` | **方案二 mount（默认）** 服务块 |
| `docker-compose.bake.yml` / `Dockerfile.project` | 方案一 bake 服务块 |

## 关键约定（与服务器对齐）

- **网络**：`networks: [service011]`（bridge）；**端口**：`ports: "23333:11610"`（对外 23333 → 容器 11610）。
- **DB**：bridge 网络下用服务名，如 `jdbc:mysql://mysql013:3306/...`（**不能用 127.0.0.1**）。
- **挂载约定**：容器内**所有可挂载目录都在 `/klsjnh/volume` 下** —— `config` / `logs` / `data` / `storage011`（app.jar 除外）。
- **配置**：`/klsjnh/docker/java011/<项目>/config/`（`application.yml` + `application-development.yml`），挂为 `/klsjnh/volume/config`；存储中心 `base-path` 指向 `/klsjnh/volume/storage011`。
- **日志**：`json-file`（max-size 100m / max-file 3）+ 文件 `.../logs/`（`/klsjnh/volume/logs`）。
- **脱敏**：`deploy/` 无内网 IP / 凭据；`runtime/` gitignore；MinIO 参数用 `MINIO_*` 环境变量。
