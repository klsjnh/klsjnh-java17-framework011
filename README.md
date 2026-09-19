# klsjnh-java17-framework011

Java 17 **纯血 DDD** 技术底座 —— Maven 多模块工程，供第三方业务系统依赖引用。全新项目、无历史技术债；对外契约（响应信封 / 状态码 / 路由风格 / 公共表结构）保持稳定。

> AI 协作入口（门牌，先读）：[Agent.md](Agent.md) · 协议全集：[docs/011.agreements.md](docs/011.agreements.md) · 架构选型：[docs/infrastructure011/011.topic-infrastructure.md](docs/infrastructure011/011.topic-infrastructure.md) · 目录结构：[docs/infrastructure011/013.topic-project-structure.md](docs/infrastructure011/013.topic-project-structure.md) · 部署：[docs/infrastructure011/020.topic-deploy-docker.md](docs/infrastructure011/020.topic-deploy-docker.md)

## 技术栈

| | |
|---|---|
| Java | 17 (LTS) |
| Spring Boot | 3.4.5 |
| MyBatis-Plus | 3.5.9（spring-boot3-starter） |
| 数据库 | MySQL 8 / Oracle（ojdbc8）/ SQL Server（mssql-jdbc）· Druid 1.2.23 连接池 |
| 调度 | Quartz（spring-boot-starter-quartz，内存模式） |
| 对象存储 | MinIO SDK 8.5.7（local011 / minio011 / s3011 适配器，provider 注册表内置） |
| 消息中心 | 内置 `inapp` / `webhook` 渠道 + `MessageChannelPort` SPI（厂商渠道由使用者插件提供） |
| 鉴权 | JJWT 0.12.6 |
| AOP | spring-boot-starter-aop（controller IUD 审计） |
| JSON | Jackson（导出） |
| API 文档 | knife4j 4.5.0 (OpenAPI3) |
| 工具 | Lombok 1.18.36（domain 层禁用） |

## 架构总览

```
web ──► application ──► domain ◄── infrastructure
              │                        │
              └────────► common ◄──────┘   （common 被各层引用，不反向依赖）
```

| 层 | 职责 | 依赖 |
|----|------|------|
| common | 跨层契约：枚举、响应信封、共享 VO | 无 |
| domain | 聚合、值对象、仓储接口、Port | common 共享内核（纯 Java 枚举），禁框架 |
| application | 用例编排、事务边界 | domain + common |
| infrastructure | PO、Mapper、RepositoryImpl、技术适配 | domain + common |
| web | Controller、统一信封、全局异常、鉴权过滤器、IUD 审计切面 | application + common |
| app | 唯一 main、装配、profile 配置、启动播种 | 全部 |

## 模块

| 模块 | 层 | 内容 |
|------|-----|------|
| java17-common011 | common | 枚举（FrameworkStatus011 / HttpCodeEnum011 / Status011 / AuditType011 / ExportFormat011）· 开放字符串常量（DatabaseTypes011 / StorageProviderCodes011 / MessageProviderTypes011 / AuditObjectCodes011）· Response011 + IdVo011 · BusinessException · 分页对 / 批量删除对 · Operator011 · 工具（DateUtil011 / StringUtil011 / MarkdownUtil011 / HttpUtil011） |
| java17-domain011 | domain | shared（EntityId / AuditInfo）· iam（用户/角色 + Port）· datasource（kernel Port + 方言 SPI `SqlDialectPort011` + management）· storagecenter（object：ObjectStoragePort / ObjectStorageProviderFactory SPI；storage：JulyStorageProvider / JulyStorageProviderBucket）· messagecenter（channel SPI `MessageChannelPort` / template / message）· system011（menu / config / organization / scheduler / dictionary）· ai011（AiModelProvider + Api + Probe / chat）· platform011（export / backup Port） |
| java17-application011 | application | system011（config / menu / organization / scheduler / dictionary）· iam（user / role）· ai011（模型接入 + chat）· storagecenter（实例 / 桶 / 对象 + 在线编辑）· **messagecenter（send / 渠道 / 模板 / 记录）** · platform011（export / backup） |
| java17-infrastructure011 | infrastructure | 基座家族五层（BaseRepository / 011 / Tree / Tree011 / MasterSub021）+ AuditMetaObjectHandler · system011 / datasource / ai011 / storagecenter / messagecenter 持久化 · 动态数据源路由 + 方言注册表 + 探针 · provider 注册表（local011/minio011/s3011）+ Resolver + 播种 · 内置渠道（inapp / webhook）· IAM 适配器（bcrypt / JWT / 审计记录器） |
| java17-web011 | web | 各域 Controller · GlobalExceptionHandler · GlobalAuthFilter（JWT）· AuditLogAspect（IUD 审计）· Swagger 6 组（system011 / iam / storagecenter / datasource / ai011 / messagecenter） |
| java17-app011 | app | 唯一 main + 配置 + 参考样板（demo11 纵切面 / Demo011Scheduler）+ 启动播种（ci011 / storage） |

## 快速开始

前置：JDK 17 · Maven 3.9+ · Node 18+（门禁脚本用）

```bash
mvn -o clean package -DskipTests          # 离线构建，产出 app011 可执行 jar
./script011.sh gate                       # 规范检查（正则 + AST）+ 离线编译
./script011.sh dev013                     # 杀进程 + 重新编译 + 启动（11160）
```

> 说明：`application.yml` 配置端口 11160 与默认 development profile；数据源与 dev 用 krt 配置落在入库的 `application-development.yml`，本机差异走忽略的 `application-local.yml`。

## 部署（Docker）

```bash
bash deploy/build-base.sh                 # 基镜像 klsjnh/java17:v0.0.1（ubuntu 26.04 + JDK17）
bash deploy/deploy.sh                     # 默认 mount（轻量挂载）→ docker compose up -d
docker compose -f deploy/docker-compose.yml logs -f app
```

机制 / 两方案（mount·bake）/ 脱敏 / 日志：见 [docs/infrastructure011/020](docs/infrastructure011/020.topic-deploy-docker.md) 与 [deploy/README.md](deploy/README.md)。

## API 契约要点

所有端点统一返回六键信封（JSON 键 **camelCase**，禁止改名/删除/重排）：

`statusCode` · `message` · `errorMessage` · `timestamp` · `traceId` · `data`

- `statusCode` 与 HTTP 传输状态码保持一致；`errorMessage` **仅 debug 态填充**
- 状态码常量：SUCCESS(200) / BAD_REQUEST(400) / UNAUTHORIZED(401) / FORBIDDEN(403) / NOT_FOUND(404) / ERROR(500)
- 关键约定：点查 `getByXxx` 用 GET；写动作 POST；单个/批量删除分离；入参一律具名 `*Vo011`；敏感字段类型层剔除
- 已知缺口与自检清单：[docs/019.backend-api-review.md](docs/019.backend-api-review.md)；完整契约：[docs/013.api-contract.md](docs/013.api-contract.md)

## 编码规范要点

- 文件头 `/* TypeName ... */` 块；Javadoc 英文；`@Schema` 中文仅 web VO；DDL 注释中文
- domain 零框架依赖（纯 Java + record）；`@Transactional` 只在 application；PO 只在 infrastructure
- 逻辑删除统一 `dr`；**唯一键只约束存活行**（生成列 `alive_*`，见 [docs/sql/base-entity-columns.sql](docs/sql/base-entity-columns.sql)）
- 主键 `EntityId.generate()`（32 位无连字符 UUID，列宽 33）
- 完整规则：[docs/016.coding-standards.md](docs/016.coding-standards.md)

## 状态与路线

| 状态 | 项 |
|------|-----|
| ✅ | 六模块骨架 + 依赖铁律 + 统一响应信封 / 状态码 / 全局异常 |
| ✅ | 仓库基座家族（BaseRepository / 011 / Tree / Tree011 / MasterSub021）+ 审计自动填充 |
| ✅ | 编码规则门禁（正则 + AST，经 script011.sh 强制） |
| ✅ | system011：配置（023）· 组织（013）· 用户（015）· 角色（016）· 菜单（011）· 调度（022）· 数据字典（027） |
| ✅ | platform011：数据导出（025，注册制 Provider / EXPORT 审计）+ 备份（BACKUP 审计） |
| ✅ | datasource：数据源管理（026，表驱动 + 双向驱动 + 测试连接 + 排序 + 通用 SQL 执行/分页 + **分页方言开放 SPI**，内置 mysql/postgresql/oracle/sqlserver） |
| ✅ | ai011：模型接入（028，主子表 + 密钥脱敏 + 提供商/密钥级探测 + 导出）· 模型调用 chat（030，OpenAI 兼容，id/code 解析） |
| ✅ | storagecenter：存储中心管理面 + 在线编辑（029，**主子表** `july_storage_provider` + `_bucket` + 桶/对象 + readText/saveText + 预签名 + **provider 注册表**） |
| ✅ | messagecenter：消息中心（021，**出入两套**：出站 `MessageChannelPort` + 入站 `MessageInboundPort`/监听 + 6 表 + send/接收回调/去重/分发 + 内置出站 inapp/webhook，2026-09-19） |
| ✅ | 审计：AuditType011 枚举 + controller IUD 切面 + 平台事件（EXPORT/BACKUP 在 use case 写，独立事务）；`objectCode` 统一 `AuditObjectCodes011` |
| ✅ | 逻辑删除 + 唯一键根治（生成列 `alive_*`，墓碑不挡重插）；批量删除**全有或全无**（原子） |
| ✅ | 扩展点开放化：存储 provider / 分页方言 / 消息渠道 = 开放字符串 + 注册表（禁封闭枚举做路由键） |
| ✅ | 容器化部署（docker compose + ubuntu 26.04 基镜像 + mount/bake，见 020） |

## 版本

`com.klsjnh:java17-framework011:1.0.0`（开发中）
