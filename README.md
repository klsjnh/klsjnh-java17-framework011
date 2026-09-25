# klsjnh-java17-framework011

Java 17 **纯血 DDD** 技术底座 —— Maven 多模块工程，供第三方业务系统依赖引用。全新项目、无历史技术债；对外契约（响应信封 / 状态码 / 路由风格 / 公共表结构）保持稳定。

> AI 协作入口（门牌，先读）：[Agent.md](Agent.md) · 协议全集：[docs/011.agreements.md](docs/011.agreements.md) · 架构选型：[docs/infrastructure011/011.topic-infrastructure.md](docs/infrastructure011/011.topic-infrastructure.md) · 目录结构：[docs/infrastructure011/013.topic-project-structure.md](docs/infrastructure011/013.topic-project-structure.md) · 部署：[docs/infrastructure011/020.topic-deploy-docker.md](docs/infrastructure011/020.topic-deploy-docker.md) · 落地件：[docs/deploy/](docs/deploy/) · **中心 starter 体系文档**：[025 AI](docs/infrastructure011/017.ai-center/011.topic-design.md) · [026 消息](docs/infrastructure011/016.message-center/011.topic-design.md) · [027 存储](docs/infrastructure011/015.storage-center/011.topic-design.md) · **消费方手册**：[docs/020.business-project-quickstart.md](docs/020.business-project-quickstart.md) · **金标准回归**：[docs/infrastructure011/029](docs/infrastructure011/029.topic-golden-verification.md)

## 技术栈

| | |
|---|---|
| Java | 17 (LTS) |
| Spring Boot | 3.4.5 |
| MyBatis-Plus | 3.5.9（spring-boot3-starter） |
| 数据库 | MySQL 8 / Oracle（ojdbc8）/ SQL Server（mssql-jdbc）· Druid 1.2.23；分页方言 SPI 另含 postgresql（**无预置 PG 驱动**） |
| 调度 | Quartz（spring-boot-starter-quartz，内存模式） |
| 对象存储 | MinIO SDK 8.5.7（local011 / minio011 / s3011 适配器，provider 注册表内置） |
| 消息中心 | 内置 `inapp` / `webhook` 渠道 + `MessageChannelPort` SPI（厂商渠道由使用者插件提供） |
| 鉴权 | JJWT 0.12.6 |
| AOP | spring-boot-starter-aop（controller IUD 审计） |
| JSON | Jackson（导出） |
| API 文档 | knife4j 4.5.0 (OpenAPI3) |
| 可观测性 | spring-boot-starter-actuator + micrometer-registry-prometheus（health / 指标 / traceId MDC） |
| 工具 | Lombok 1.18.36（domain 层禁用） |

## 架构总览

**包结构**保持六层洋葱不变（门禁按包名锚定，全部规则不受模块重构影响）：

```
web ──► application ──► domain ◄── infrastructure
              │                        │
              └────────► common ◄──────┘   （common 被各层引用，不反向依赖）
```

**Maven 模块**为 BOM + **瘦 core** + **中心/技术 starter**（共 15 模块）：六层洋葱包名不变；中心能力（access / platform / datasource / storage / message / ai）与调度整栈已纵切进各自 starter，core 只留用户登录、持久化基座、跨中心端口契约与导出/导入/备份内核。依赖方向严格单向（starter → core → security-api）。

| 模块 | 角色 | 内容 |
|------|------|------|
| java17-bom011 | BOM | 内部模块 + 三方版本基线（消费方 `<scope>import</scope>` 引入） |
| java17-security-api011 | 安全 API（纯 Java） | 鉴权端口（AuthTokenPort / AuthorizationPort / PasswordPort / RuntimeStatusPort）· Operator011 · FrameworkStatus011 |
| java17-core011 | **核心（已瘦身）** | common + 持久化基座 + **用户 CRUD/登录/用户审计** + platform011 导出/导入/备份内核 + 跨中心端口契约（datasource.kernel / storagecenter.object）；**组织/菜单/角色/权限目录 → access**；**字典/配置 → platform**；**数据源管理面/Sql/Sync → center-datasource**（kernel 端口留 core） |
| java17-security-autoconfigure011 | 安全装配 | JWT · bcrypt · 运行态适配器 · GlobalAuthFilter · 无 access 时的放行 `AuthorizationPort` / 空角色码；**真实授权适配器在 access** |
| java17-security-starter011 | 安全启动 | 聚合 security-autoconfigure + jjwt + spring-security-crypto |
| java17-data-mybatis011-starter | 数据启动 | Druid + JDBC 驱动（mysql/oracle/sqlserver runtime）· 动态数据源 kernel 执行器（池 / 路由 / 方言 SPI 4+4 / 探针）· **框架 Mapper 自动装配**（AutoConfiguration.imports，消费方只声明自己的 @MapperScan） |
| center-datasource011-starter | **数据源中心** | july_datasource 管理面 + JulySql 只读分页 HTTP + Sync（july_sync_rule / S1 引擎）；**kernel 端口契约留 core**；池/路由实现留 data-mybatis |
| center-storage011-starter | 存储 | **完整存储中心**：管理面（provider 实例 / 桶 / 对象 / 在线编辑）+ local011 + minio011 适配器（运行时按 `krt.storage-center.default-type` 选择；厂商走 SPI）；**object 端口契约留 core** |
| center-access011-starter | **访问中心** | 组织 / 角色 / 菜单 / 权限目录 / `AuthorizationPort` 实现 / 生产写白名单门闸标记；**用户 CRUD + 登录留 core** |
| center-platform011-starter | **平台中心** | 字典 julyDictionary + 系统配置 julyConfig 管理面；**不引则无字典/配置 API**；导出导入备份内核仍在 core |
| java17-scheduler-quartz011-starter | 调度（**非「调度中心」**） | **完整 julyScheduler** 主子表：管理面 CRUD/启停 + 执行审计 `july_scheduler_audit`（链接列 **`pk_mt`**）+ Quartz（RAMJobStore）+ Handler 注册表 + 启动重注册；不引则无 start |
| center-message011-starter | 消息 | **完整消息中心**：出入两套（渠道 / 模板 / 消息）管理面 + 内置渠道 inapp / webhook（厂商渠道 SPI 扩展） |
| center-ai011-starter | AI | **完整 AI 中心**：管理面（模型接入主子表、提示词业务域）+ 能力引擎（推理 / 图片 / 语音适配器 + agnes / sensenova）+ 媒体落盘 · krt.ai-center 绑定；依赖 center-storage |
| java17-observability011-starter | 可观测性 | actuator + Prometheus · traceId MDC 过滤器（白名单路径也有 traceId）· 健康指示器（动态数据源池 / 存储默认行 / Quartz 引擎） |
| java17-app011 | 参考应用 | 唯一 main + 配置 + demo11 样板 + 演示账号播种（demo 内容已移出框架） |

> **装配单轨**：宿主主类**不扫描任何框架包**——core 与 starter 通过 `META-INF/spring/...AutoConfiguration.imports` 自注册（core 定向扫描自己的 application/web/infrastructure 三层包），框架 Mapper 由 data-starter 自动装配；`AuthChainPresenceCheck011` 熔断兜底：web 应用若缺安全链（未引 security-starter）**直接拒绝启动**。业务项目主类只扫自己的包。
> **命名口径**：中心类 starter 无 `java17-` 前缀，统一 `center-<名>011-starter`（center-ai011 / center-message011 / center-storage011 / **center-access011** / **center-platform011** / **center-datasource011**）；框架内部模块保持 `java17-*`。
> 原 KrtConfig011 已解散为三个绑定类：`KrtSecurityConfig011`（krt.status/jwt/web）· `KrtDatasourceConfig011`（krt.ci011，core）· `KrtAiConfig011`（krt.ai-center，ai-starter）。

## 平台能力中心

横切能力中心 + IAM，架构底册在 `docs/infrastructure011/`，详细设计在 `docs/requirement013/`：

| 中心 | 一句话 | 架构底册 · **starter 体系文档**（设计 / 架构 / 使用 / 二开） |
|------|--------|----------|
| **存储中心** | 对象存储统一端口：local011 / minio011 / s3011 + **工厂型 provider 注册表**；表驱动多实例 + 实例/桶/对象管理 + 在线编辑；对象元数据为**规约**（非平台能力，见 [016](docs/archive011/infrastructure011/011.storage-center/016.topic-object-metadata-convention.md)） | **现行** [027 starter](docs/infrastructure011/015.storage-center/011.topic-design.md) · 历史底册 [011.storage-center](docs/archive011/infrastructure011/011.storage-center/011.topic-design.md)（已归档） |
| **消息中心** | 出入两套、渠道可插拔：出站 `MessageChannelPort` + 入站 `MessageInboundPort`；内置 `inapp`/`webhook`，厂商渠道 SPI 扩展 | **现行** [026 starter](docs/infrastructure011/016.message-center/011.topic-design.md) · 历史 [013.message-center](docs/archive011/013.message-center/011.topic-design.md)（已归档） |
| **AI 中心** | 三大能力模块：**推理（SSE 流式）/ 图片（文生图·图生图）/ 语音（TTS·ASR）**；能力 SPI + 通用 OpenAI 兼容适配器；**提示词管理（主子表 + `render`）**；产物落盘（生命周期归使用方） | **现行** [025 starter](docs/infrastructure011/017.ai-center/011.topic-design.md) · 历史 [015.ai-center](docs/archive011/015.ai-center/011.topic-design.md)（已归档） |
| **数据源中心** | 多数据源管理 + **参数化只读分页查询** + **同步体系（S1 单表）**：表驱动多实例 / 方言 SPI / 主子表对照 | [018.datasource-center](docs/infrastructure011/018.datasource-center/011.topic-design.md) · **starter：`center-datasource011-starter`**（kernel 端口留 core；执行器在 data-mybatis） |
| **访问中心**（原 IAM / 权限中心口径） | 组织 / 角色 / 菜单 / 权限目录 / 授权适配；用户 CRUD + 登录在 core | [013.access-center](docs/infrastructure011/013.access-center/011.topic-design.md)（现称访问中心 · **starter：`center-access011-starter`**） |
| **平台中心** | 字典 julyDictionary + 系统配置 julyConfig 管理面 | [014.platform-center](docs/infrastructure011/014.platform-center/README.md)（**starter：`center-platform011-starter`**） |

> 共同口径：**能力 / 厂商用开放字符串 + 注册表**，扩展不改底座（见 [docs/011.agreements.md](docs/011.agreements.md)）。

## 业务项目接入（消费方）

四个 starter 起步（BOM 管版本，宿主**零框架扫描**）：

```xml
<dependency>
  <groupId>com.klsjnh</groupId><artifactId>java17-bom011</artifactId>
  <version>1.0.0</version><type>pom</type><scope>import</scope>
</dependency>
<dependency><groupId>com.klsjnh</groupId><artifactId>java17-security-starter011</artifactId></dependency>
<dependency><groupId>com.klsjnh</groupId><artifactId>java17-data-mybatis011-starter</artifactId></dependency>
<!-- 中心能力按需：center-access011-starter / center-platform011-starter /
     center-datasource011-starter / center-storage011-starter /
     center-message011-starter / center-ai011-starter /
     java17-scheduler-quartz011-starter / java17-observability011-starter -->
```

不引的 starter 整个中心不存在（端点 404、代码不在 classpath）；缺安全链拒绝启动。**不引 `center-access011-starter` 时**：无组织/角色/菜单/权限目录管理面，用户登录仍可用，生产写白名单门闸不生效。**不引 `center-platform011-starter` 时**：无字典/系统配置管理面。**不引 `center-datasource011-starter` 时**：无 `/klsjnh/datasource/**` 管理面 / Sql HTTP / Sync；kernel（`SqlRoutingPort` 等）仍可由 `java17-data-mybatis011-starter` 提供。完整四步 + 必选/可选表 + demo 照抄入口：**[docs/020.business-project-quickstart.md](docs/020.business-project-quickstart.md)**；活示例：最小登录 + `july_demo011` CRUD **`java17-demo011-app011`**（库 `july_demo011core`，11161）+ 全量 CRUD/调度 **`java17-demo013-app013`**（见 020 §3.5）。

## 快速开始

前置：JDK 17 · Maven 3.9+ · Node 18+（门禁脚本用）

```bash
mvn -o clean package -DskipTests          # 离线构建，产出 java17-app011 可执行 jar
./script011.sh gate                       # 规范检查（正则 + AST）+ mvn -o clean install（含单测 + 装 .m2）
./script011.sh dev013                     # 杀进程 + 重新编译 + 启动（11160）
```

> **结构性改动后**（模块边界 / 装配 / 契约）：按 [029 金标准协议](docs/infrastructure011/029.topic-golden-verification.md) 跑 G1–G8 全量回归，并建议 **G9a**（`tools/demo011-dual-mode-smoke.sh`：`MODE=dev` + `MODE=prod` 均 EXIT=0，jar :11161）与 **G9b**（demo013 调度 + `selectExecListByPage` 用 **`pkMt`/`pk_mt`**）。

> 说明：`application.yml` 默认 `spring.profiles.active=development`；入库的 `application-development.yml` 显式 `krt.status: debug`（鉴权放行、PEP opt-in——**勿当生产契约**）。数据源与 dev 用 krt 落 development yml；本机差异走忽略的 `application-local.yml`（**fat jar 已 excludes local/production**，见 [015.topic-config](docs/infrastructure011/015.topic-config.md)）。

## 部署（Docker）

落地件与操作手册在 **`docs/deploy/`**（根目录无部署正文）：

```bash
bash docs/deploy/build-base.sh            # 基镜像 klsjnh/java17:v0.0.1
bash docs/deploy/deploy.sh                # 默认 mount → 备 runtime + 打印服务块
```

机制 / 两方案（mount·bake）/ 脱敏 / 日志：见 [docs/infrastructure011/020](docs/infrastructure011/020.topic-deploy-docker.md) · [docs/deploy/README.md](docs/deploy/README.md)。

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
| ✅ | BOM + 瘦 core + 中心/技术 starter（**15** 模块）+ 包层六层洋葱 + 依赖铁律 + 统一响应信封 / 状态码 / 全局异常 |
| ✅ | 仓库基座家族（BaseRepository / BaseTree* / BaseMasterSub* / BaseTreeSub*）+ SortSupport + 审计自动填充 |
| ✅ | 编码规则门禁（正则 + AST，经 script011.sh 强制） |
| ✅ | **访问中心**（`center-access011-starter` / 文档 013）：组织 · 菜单 · 角色 · 权限目录；**用户 CRUD/登录在 core**；**平台中心**配置（023）· 字典（027）；**调度**（022，quartz011 starter，含执行审计主子表） |
| ✅ | platform011：数据导出（025，注册制 Provider / EXPORT 审计）+ 数据导入（032，xlsx；Controller 按需；IMPORT 审计）+ 备份（BACKUP 审计） |
| ✅ | datasource：数据源管理（026，表驱动 + 双向驱动 + 测试连接 + 排序 + 通用 SQL 执行/分页 + **分页方言开放 SPI**，内置 mysql/postgresql/oracle/sqlserver）——**管理面/Sql/Sync 在 `center-datasource011-starter`** |
| ✅ | aicenter：模型接入（028，主子表 + 密钥脱敏 + 提供商/密钥级探测 + 导出）· 三大能力（030，**推理（SSE 流式）/ 图片 / 语音（TTS+ASR）**，能力 SPI + 通用 OpenAI 兼容适配器 + 产物落盘（调用必传实例+桶，无默认），id/code 解析、model 直传）· **提示词管理（主子表 + `render` 公共件）** |
| ✅ | storagecenter：存储中心管理面 + 在线编辑（029，**主子表** `july_storage_provider` + `_bucket` + 桶/对象 + readText/saveText + 预签名 + **provider 注册表**） |
| ✅ | 装配单轨（宿主零框架扫描 + starter imports 自注册 + 缺安全链熔断）· 中心 starter 命名 `center-<名>011-starter` · 金标准协议 029 |
| ✅ | **中心整体剥离**（0925b）：AI / 消息 / 存储的 domain+application+infrastructure+web 全部迁入 `center-ai011-starter` / `center-message011-starter` / `center-storage011-starter`；core 仅保留端口契约（storagecenter.object）与备份软依赖；金标准协议 [029](docs/infrastructure011/029.topic-golden-verification.md) |
| ✅ | **数据源中心剥离**：management / Sql HTTP / Sync 迁入 `center-datasource011-starter`；`domain.datasource.kernel` 端口与 `KrtDatasourceConfig011` 留 core；池/路由/方言执行器留 `java17-data-mybatis011-starter` |
| ✅ | messagecenter：消息中心（021，**出入两套**：出站 `MessageChannelPort` + 入站 `MessageInboundPort`/监听 + 6 表 + send/接收回调/去重/分发 + 内置出站 inapp/webhook，2026-09-19） |
| ✅ | 审计：AuditType011 枚举 + controller IUD 切面 + 平台事件（EXPORT / IMPORT / BACKUP 在 use case 写，独立事务）；`objectCode` 统一 `AuditObjectCodes011` |
| ✅ | 逻辑删除 + 唯一键根治（生成列 `alive_*`，墓碑不挡重插）；批量删除**全有或全无**（原子） |
| ✅ | 扩展点开放化：存储 provider / 分页方言 / 消息渠道 = 开放字符串 + 注册表（禁封闭枚举做路由键） |
| ✅ | 容器化部署（docker compose + ubuntu 26.04 基镜像 + mount/bake；落地件 `docs/deploy/`，机制见 020） |

## 许可证

[MIT](LICENSE) —— 完全开源：可自由使用、修改、商用、再分发（保留版权与许可声明即可）。

## 版本

`com.klsjnh:java17-framework011:1.0.0`（开发中）
