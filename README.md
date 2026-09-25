# klsjnh-java17-framework011

Java 17 **纯血 DDD** 技术底座 —— Maven 多模块工程，供第三方业务系统依赖引用。全新项目、无历史技术债；对外契约（响应信封 / 状态码 / 路由风格 / 公共表结构）保持稳定。

> AI 协作入口（门牌，先读）：[Agent.md](Agent.md) · 协议全集：[docs/011.agreements.md](docs/011.agreements.md) · 架构选型：[docs/infrastructure011/011.topic-infrastructure.md](docs/infrastructure011/011.topic-infrastructure.md) · 目录结构：[docs/infrastructure011/013.topic-project-structure.md](docs/infrastructure011/013.topic-project-structure.md) · 部署：[docs/infrastructure011/020.topic-deploy-docker.md](docs/infrastructure011/020.topic-deploy-docker.md) · 落地件：[docs/deploy/](docs/deploy/)

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

**包结构**保持六层洋葱不变（门禁按包名锚定，全部规则不受模块重构影响）：

```
web ──► application ──► domain ◄── infrastructure
              │                        │
              └────────► common ◄──────┘   （common 被各层引用，不反向依赖）
```

**Maven 模块**已重构为 BOM + 胖核心 + 瘦 starter 体系：特性代码（domain / application / 各特性持久化 / 管理面 Controller）全部在核心，starter 只装技术适配，消费方按需组装；依赖方向严格单向（starter → core → security-api）。

| 模块 | 角色 | 内容 |
|------|------|------|
| java17-bom011 | BOM | 内部 13 模块 + 三方版本基线（消费方 `<scope>import</scope>` 引入） |
| java17-security-api011 | 安全 API（纯 Java） | 鉴权端口（AuthTokenPort / AuthorizationPort / PasswordPort / RuntimeStatusPort）· Operator011 · FrameworkStatus011 |
| java17-core011 | **胖核心** | common（信封 / 异常 / 分页 / 常量 / 工具）+ domain + application + web 全部特性层；持久化基座家族（BaseRepository / BaseTree* / BaseMasterSub* / BaseTreeSub*）+ 各特性 PO/Mapper/RepositoryImpl；25 个管理面 Controller · GlobalExceptionHandler · Swagger 6 组；krt.ci011 绑定 |
| java17-security-autoconfigure011 | 安全装配 | JWT 签发/校验 · bcrypt · 授权/运行态适配器 · GlobalAuthFilter（**归一化白名单** + /actuator/health）· AuditLogAspect（IUD 审计）· 审计记录器 · krt.jwt/status 绑定 |
| java17-security-starter011 | 安全启动 | 聚合 security-autoconfigure + jjwt + spring-security-crypto |
| java17-data-mybatis-starter011 | 数据启动 | Druid + JDBC 驱动（mysql/oracle/sqlserver runtime）· 动态数据源 kernel（池 / 路由 / 方言 SPI 4+4 / 探针 / 同步引擎依赖面）· **框架 Mapper 自动装配**（AutoConfiguration.imports，消费方只声明自己的 @MapperScan） |
| center-storage011-starter | 存储 | local011 + minio011 适配器与工厂（provider 按 `krt.storage-center.default-type` 运行时选择；厂商走 SPI） |
| java17-scheduler-quartz-starter011 | 调度 | Quartz 引擎（RAMJobStore）+ Handler 注册表 + 启动重注册 |
| center-message011-starter | 消息 | 内置出站渠道 inapp / webhook（厂商渠道 SPI 扩展） |
| center-ai011-starter | AI | OpenAI 兼容适配器（inference / tts / asr / image）+ agnes / sensenova · 媒体落盘 · krt.ai-center 绑定 |
| java17-observability-starter011 | 可观测性 | actuator + Prometheus · traceId MDC 过滤器（白名单路径也有 traceId）· 健康指示器（动态数据源池 / 存储默认行 / Quartz 引擎） |
| java17-test011 | 测试套件 | JUnit5 + Mockito + AssertJ 聚合 · BaseUseCaseTest011 基类（严格 stub 的用例单测基座） |
| java17-reference-app011 | 参考应用 | 唯一 main + 配置 + demo11 样板 + 演示账号播种（demo 内容已移出框架） |

> **装配单轨**：宿主主类**不扫描任何框架包**——core 与 starter 通过 `META-INF/spring/...AutoConfiguration.imports` 自注册（core 定向扫描自己的 application/web/infrastructure 三层包），框架 Mapper 由 data-starter 自动装配；`AuthChainPresenceCheck011` 熔断兜底：web 应用若缺安全链（未引 security-starter）**直接拒绝启动**。业务项目主类只扫自己的包。
> **命名口径**：三个中心类 starter 无 `java17-` 前缀，统一 `center-<名>011-starter`（center-ai011 / center-message011 / center-storage011）；框架内部模块保持 `java17-*`。
> 原 KrtConfig011 已解散为三个绑定类：`KrtSecurityConfig011`（krt.status/jwt/web）· `KrtDatasourceConfig011`（krt.ci011，core）· `KrtAiConfig011`（krt.ai-center，ai-starter）。

## 平台能力中心

横切能力中心 + IAM，架构底册在 `docs/infrastructure011/`，详细设计在 `docs/requirement013/`：

| 中心 | 一句话 | 架构底册 |
|------|--------|----------|
| **存储中心** | 对象存储统一端口：local011 / minio011 / s3011 + **工厂型 provider 注册表**；表驱动多实例 + 实例/桶/对象管理 + 在线编辑；对象元数据为**规约**（非平台能力，见 [016](docs/infrastructure011/011.storage-center/016.topic-object-metadata-convention.md)） | [011.storage-center](docs/infrastructure011/011.storage-center/011.topic-design.md) |
| **消息中心** | 出入两套、渠道可插拔：出站 `MessageChannelPort` + 入站 `MessageInboundPort`；内置 `inapp`/`webhook`，厂商渠道 SPI 扩展 | [013.message-center](docs/infrastructure011/013.message-center/011.topic-design.md) |
| **AI 中心** | 三大能力模块：**推理（SSE 流式）/ 图片（文生图·图生图）/ 语音（TTS·ASR）**；能力 SPI + 通用 OpenAI 兼容适配器；**提示词管理（主子表 + `render`）**；产物落盘（生命周期归使用方） | [015.ai-center](docs/infrastructure011/015.ai-center/011.topic-design.md) |
| **数据源中心** | 多数据源管理 + **参数化只读分页查询** + **同步体系（S1 单表）**：表驱动多实例 / 方言 SPI / 主子表对照 | [017.datasource-center](docs/infrastructure011/017.datasource-center/011.topic-design.md) |
| **IAM 中心** | 组织 / 用户 / 菜单 / 角色 / 权限目录；包与 URL 均在 `…iam` / `/klsjnh/iam/**`（**非** system011；system011 仅配置/调度/字典） | [018.iam-center](docs/infrastructure011/018.iam-center/011.topic-design.md) |

> 共同口径：**能力 / 厂商用开放字符串 + 注册表**，扩展不改底座（见 [docs/011.agreements.md](docs/011.agreements.md)）。

## 快速开始

前置：JDK 17 · Maven 3.9+ · Node 18+（门禁脚本用）

```bash
mvn -o clean package -DskipTests          # 离线构建，产出 reference-app011 可执行 jar
./script011.sh gate                       # 规范检查（正则 + AST）+ 离线编译
./script011.sh dev013                     # 杀进程 + 重新编译 + 启动（11160）
```

> 说明：`application.yml` 配置端口 11160 与默认 development profile；数据源与 dev 用 krt 配置落在入库的 `application-development.yml`，本机差异走忽略的 `application-local.yml`。

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
| ✅ | 六模块骨架 + 依赖铁律 + 统一响应信封 / 状态码 / 全局异常 |
| ✅ | 仓库基座家族（BaseRepository / BaseTree* / BaseMasterSub* / BaseTreeSub*）+ SortSupport + 审计自动填充 |
| ✅ | 编码规则门禁（正则 + AST，经 script011.sh 强制） |
| ✅ | IAM（018）：组织（013）· 用户（015）· 菜单（011）· 角色（016）· 权限目录；system011：配置（023）· 调度（022）· 数据字典（027） |
| ✅ | platform011：数据导出（025，注册制 Provider / EXPORT 审计）+ 数据导入（032，xlsx；Controller 按需；IMPORT 审计）+ 备份（BACKUP 审计） |
| ✅ | datasource：数据源管理（026，表驱动 + 双向驱动 + 测试连接 + 排序 + 通用 SQL 执行/分页 + **分页方言开放 SPI**，内置 mysql/postgresql/oracle/sqlserver） |
| ✅ | aicenter：模型接入（028，主子表 + 密钥脱敏 + 提供商/密钥级探测 + 导出）· 三大能力（030，**推理（SSE 流式）/ 图片 / 语音（TTS+ASR）**，能力 SPI + 通用 OpenAI 兼容适配器 + 产物落盘（调用必传实例+桶，无默认），id/code 解析、model 直传）· **提示词管理（主子表 + `render` 公共件）** |
| ✅ | storagecenter：存储中心管理面 + 在线编辑（029，**主子表** `july_storage_provider` + `_bucket` + 桶/对象 + readText/saveText + 预签名 + **provider 注册表**） |
| ✅ | messagecenter：消息中心（021，**出入两套**：出站 `MessageChannelPort` + 入站 `MessageInboundPort`/监听 + 6 表 + send/接收回调/去重/分发 + 内置出站 inapp/webhook，2026-09-19） |
| ✅ | 审计：AuditType011 枚举 + controller IUD 切面 + 平台事件（EXPORT / IMPORT / BACKUP 在 use case 写，独立事务）；`objectCode` 统一 `AuditObjectCodes011` |
| ✅ | 逻辑删除 + 唯一键根治（生成列 `alive_*`，墓碑不挡重插）；批量删除**全有或全无**（原子） |
| ✅ | 扩展点开放化：存储 provider / 分页方言 / 消息渠道 = 开放字符串 + 注册表（禁封闭枚举做路由键） |
| ✅ | 容器化部署（docker compose + ubuntu 26.04 基镜像 + mount/bake；落地件 `docs/deploy/`，机制见 020） |

## 版本

`com.klsjnh:java17-framework011:1.0.0`（开发中）
