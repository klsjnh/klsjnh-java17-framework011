# 审计条目 × 代码现状一一核对（2026-09-26）

> **基线来源**：`前端审计.md`（静态抽查）+ `enterprise-platform-remediation-plan-v1.0.md`（AT-01…AT-15）。  
> **仓库**：`D:\Source\java17\klsjnh-java17-framework011`（核对日代码；未做完整集成/压测/历史密钥扫描）。  
> **结论口径**：`已修复` = 审计表述已不成立；`部分` = 有缓解但核心风险仍在；`仍成立` = 与当前代码一致；`不适用` = 产品决策/范围外或审计前提不再适用。

---

## 一、P0 安全（审计 §一）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| P0-1 | 配置文件提交数据库/Druid/JWT/动态数据源/MinIO 等凭据；应改占位符 + 轮换 + 历史扫描 | `application-development.yml` L8–9 `dev202608011`；L14–15 Druid `klsjnh`；L22–24 JWT `dev-secret-klsjnh-…`；L32–33 动态库密码；L37–39 MinIO `demo011`/`123456789`。`application.yml` L7 默认 `spring.profiles.active: development`。未见 Gitleaks/CI secret scan；无 `.github/` | **仍成立** | production 模板用占位符（`application-production.yml`）且 fat-jar excludes production/local（`java17-app011/pom.xml` L86–92），但 **development 共享凭据仍入库**；JWT 泄露风险仍在 |
| P0-2a | debug 下无 Token 仍放行；免密登录在白名单 | `GlobalAuthFilter` L235–237：`identity == null && !isDebug()` 才 401；debug 不拒绝。`WHITELIST_PATHS` 含 `/loginByUserName`（L94–96）。`FrameworkStatus011.allowsPasswordlessLogin()` 对 DEBUG/DEVELOPMENT 为 true。默认 `krt.status: debug`（development yml L22） | **仍成立** | 默认启动路径仍是「development profile + debug status」 |
| P0-2b | 权限 opt-in：未 assertHas 则只认证不鉴权；用户 CRUD/assignRoles/resetPassword 无权限；UseCase 无 AuthorizationPort；内置角色 `*` | **相对审计时已变**：`JulyUserUseCase` 已注入 `AuthorizationPort`，insert/update/delete/resetPassword/assignRoles（`JulyUserRoleAssignUseCase`）等均 `assertHas`（如 L143、L278）。生产写路径有 `PermissionWhitelistGate011` + `GlobalAuthFilter.enforcePermissionWhitelist`。但：① debug/development 下 `AuthorizationAdapter.assertHas` **no-op**（L162–164）；② 无 access starter 时 `PermissiveAuthorizationPort` 全放行；③ 内置角色仍返回 `"*"`（`BUILTIN_ALL`，AuthorizationAdapter L58/L120）；④ 读类路径仍豁免写白名单 | **部分** | 生产 + access 时管理面挂齐；**默认 debug 下审计描述的提权链仍可走通** |
| P0-3 | profile 与 `krt.status` 双开关；卫兵只拦 production profile + 非 production status；缺统一 security-mode；默认不应激活 development | `KrtSecurityConfig011.validate` L85–92 仅检查 profile 名等于 `production`；`application.yml` 仍默认 `development`。无 `app.security-mode`。缺省 `krt.status` 代码落 PRODUCTION（fail-safe），但被 development yml 覆盖为 debug | **仍成立** | `prod-cn`/`k8s` 等 profile 名仍可绕过「production profile 必须 production status」卫兵 |
| P0-4 | AI/存储/数据源业务密钥库内明文；文档自承未加密 | `docs/infrastructure011/033.topic-platform-security.md` 明确 **未落地**；Po 字段：`AiModelProviderApiPo.apiKey`、`JulyStorageProviderPo.secretKey`、`JulyDatasourcePo.password`。无 `SecretCipherPort` 实现 | **仍成立** | 登录密码 bcrypt 已做，与业务可逆密钥是两类问题 |

---

## 二、架构（审计 §二）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| A-1 | 框架 vs 平台应用定位混淆：全包扫描、统一入口、能力全装一应用 | 现为多 Maven 模块 + `center-*011-starter` / `java17-*-starter`（根 `pom.xml` modules）。仍有统一入口 `java17-app011` 聚合多中心。文档倾向「模块化单体 + 可选 starter」（README / 020 quickstart）。无 ADR-001 定稿文件 | **部分** | 边界比审计时清晰；定位 ADR 仍缺；参考应用仍「一锅端」可选 |
| A-2 | infrastructure 过大：MySQL/Oracle/SQLServer/MinIO/POI/Quartz/JWT 一次引入 | 能力已拆：`java17-data-mybatis011-starter`（驱动+方言）、`center-storage`、`java17-scheduler-quartz011-starter`、security 模块等。但 **data-starter 仍 runtime 带 ojdbc8 + mssql-jdbc**（其 pom）；core 仍含 Web/POI/MyBatis | **部分** | 爆炸半径缩小，未按「最小能力集」彻底切开 |
| A-3 | common 变共享垃圾场（依赖 MyBatis/Lombok） | 独立 `common` 模块已并入 `java17-core011`；core pom 直接依赖 mybatis-plus、web、poi、knife4j | **部分** | 名字没了，**胖内核**问题转移到 core |
| A-4 | 多库名义大于实际：分页写死 `DbType.MYSQL`；缺兼容矩阵 | `MybatisPlusConfig011` L41 仍 `DbType.MYSQL`。动态 SQL 有 mysql/pg/oracle/sqlserver 方言 SPI（`java17-data-mybatis011-starter`）。README 仍写支持多库。未见 Testcontainers 兼容矩阵门禁 | **部分** | 动态路由分页有方言；框架主库分页仍 MySQL 写死 |
| A-5 | 通用 Repository 过度泛化；physicalDelete 在基类；唯一性竞态 | `BaseRepository.physicalDelete` 仍在（core）；文档称业务零调用。唯一性仍依赖应用预检 + DB 约束 | **仍成立** | 风险形态未变；属设计债 |

---

## 三、认证授权（审计 §三）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| AUTH-1 | 认证与授权割裂；未声明权限默认放行；应默认拒绝 | 生产：写路径未 `assertHas` → 403（白名单 PEP）。开发：assertHas no-op + 无写闸。读路径（select/get…）生产也豁免写闸。无「新增用例缺策略 → **构建失败**」机制 | **部分** | 生产写 fail-closed；非全站默认拒绝；CI 不扫缺策略 |
| AUTH-2 | Token 生命周期不完整：长有效期、logout 不吊销、无 credentialVersion/jti | `JwtAuthTokenService`：仅 `id`+subject+exp，无 jti/version（L94–100）。默认 `expire-minutes: 480`。`JulyUserUseCase.logout` 注释明确「JWT 无状态不可吊销」仅写审计（L418–430）。改密/禁用不抬版本 | **仍成立** | |
| AUTH-3 | 权限查询每请求 N+1（角色循环查） | `AuthorizationAdapter.listCodes` L107–128：按 roleId 循环 `findById` + `findPermissionCodes`。无 Join SQL / Redis 缓存 | **仍成立** | |

---

## 四、接口与输入（审计 §四）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| API-1 | 缺系统 Bean Validation：`@Valid` / `@NotBlank` 等 | 全仓 `*Controller` **无** `@Valid`；pom **无** `spring-boot-starter-validation`；VO 无 jakarta.validation 注解 | **仍成立** | |
| API-1b | pageSize / 批量 / 文件上限 | `PageQuery011` 仅把 ≤0 改为 10，**无上限**。动态 SQL `SqlRoutingExecutor` clamp **[10,500]**。未见统一批量 ID / 导出条数上限实现 | **部分** | 仅 Sql 分页有 clamp |
| API-2 | HttpUtil `ofByteArray` 内存风险 + URL 直传 SSRF | `HttpUtil011` 仍 `ofByteArray`（L139、L209）；另有 `ofInputStream`（L236）。无域名 allowlist / 禁私网。033/消息架构文档指向出口治理未落地 | **仍成立** | 流式下载部分能力有，出口治理无 |
| API-3 | 限流与幂等不能只靠网关 | `docs/019.backend-api-review.md` L48 仍记「无速率限制/幂等键 → 网关解决」。应用层无统一幂等键中间件 | **仍成立** | 部分业务（sync upsert、调度重注册）自有幂等，非平台能力 |

---

## 五、测试 / CI / 发布（审计 §五）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| CI-1 | 门禁只 compile；`skipTests`；无 GH Actions | `script011.sh` `do_gate`：**`mvn -o clean install`**（含单测，L85–86）。`dev`/`rebuild` 路径仍有 `-DskipTests`。**无** `.github/`、无 mvnw、无 gitleaks、无 SBOM/依赖扫描流水线 | **部分** | 相对审计「只 compile」已加强；企业 CI 闭环仍缺 |
| CI-2 | 提交脚本：gate→版本递增→`git add .`→`ver x.y.z`→push；JAVA_HOME 写死；版本双真源 | `do_push` L129–157 仍如此；`JAVA_HOME` 默认 `/usr/local/java17`（script L24）；Maven 版本 `1.0.0` vs `.vf` 自增；JAR 路径写死 `…-1.0.0.jar`（L164） | **仍成立** | |
| CI-3 | 无 Flyway/Liquibase；靠手工 SQL | 无 flyway/liquibase 依赖；`docs/sql/` 为真源。无 migration 测试门禁 | **仍成立** | |

---

## 六、可观测与调度（审计 §六）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| OBS-1 | 无 Actuator/Micrometer/OTel；Trace 应对齐 W3C | 已有 `java17-observability011-starter`（actuator + prometheus）；`application.yml` exposure health/info/metrics/prometheus。Trace 仍为自定义 `X-Trace-Id`（`GlobalAuthFilter.resolveTraceId`），**无** charset/长度校验、**无** `traceparent`/OpenTelemetry | **部分** | 基线可观测已有；企业级追踪/告警/SLO 未齐 |
| OBS-2 | Quartz RAM 不适合多实例 HA | README / requirement022：明确 **RAMJobStore**；启动按 status 重注册。无 JDBC JobStore/cluster | **仍成立** | |

---

## 七、沟通 / 文档 / 易忽视项（审计 §七–九）

| # | 审计原文要点 | 代码现状（路径/证据） | 结论 | 备注 |
|---|---|---|---|---|
| DOC-1 | 流程过重；原始对话入库 | 仍有 `docs/archive011/*-chat.md`、`docs/011.agreements.md` 七步流程；未见 R0–R3 落地 ADR 集 | **仍成立** | 整改方案建议的分级尚未成为仓库硬约束 |
| DOC-2 | 缺 CONTRIBUTING / SECURITY / CODEOWNERS / CHANGELOG 等 | 根目录仅 README/LICENSE/Agent.md；**均不存在**上述文件；无 ADR 索引目录 | **仍成立** | |
| MISC-1 | 审计依赖 Controller AOP，任务/消息可绕过 | `@AuditLog` 挂在各 Controller；调度另有执行子表。UseCase 层无统一审计总线；消息消费路径未见等价强制审计 | **仍成立** | |
| MISC-2 | 数据权限 / 租户隔离未成型 | 仅功能权限码；无组织数据范围 / 租户模型代码证据 | **仍成立** | |
| MISC-3 | JSON 字段顺序不应成为契约 | `docs/013.api-contract.md` / `017.tech-debt-redlines.md` 仍要求六键「不许…重排」 | **仍成立** | 与审计建议冲突；属文档契约债 |
| MISC-4 | 文件头作者/修改历史与 Git 重复 | 源文件普遍保留 `@author` / modify history 块 | **仍成立** | |

---

## 八、整改方案验收场景 AT-01…AT-15

| AT | 场景 | 结论 | 证据 |
|---|---|---|---|
| AT-01 | 无 Token 访问受保护接口应拒绝 | **部分** | production/`krt.status≠debug`：`GlobalAuthFilter` 写 401。**默认 debug：放行**。未见自动化安全回归绑定制品 |
| AT-02 | 普通用户高权操作（重置密码/分配角色）拒绝并审计 | **部分** | 生产路径 `assertHas(RESET_PASSWORD/ASSIGN_ROLES)`。debug no-op。拒绝审计是否完备未核（Aspect 偏成功路径） |
| AT-03 | 新增未声明访问策略的用例 → CI 阻断；运行时不默认放行 | **仍成立（未达标）** | 无 CI Arch/策略扫描。生产写闸运行时 403；读路径与无 access 装配仍可放行；开发完全不拦 |
| AT-04 | 缺环境/错 Profile/debug 冲突 → 安全模式或拒启 | **部分** | `production` profile + 非 production status → 启动失败；缺省 status→PRODUCTION。默认仍激活 development+debug；非 `production` 名的「类生产」profile 无卫兵 |
| AT-05 | 改密/禁用/退出后旧 Token 失效 | **仍成立（未达标）** | 无 credentialVersion；logout 不吊销；JWT 仅过期失效 |
| AT-06 | 实例 A 撤权，实例 B 下次请求不放行 | **不适用/未知** | 无授权缓存；多实例一致性依赖每请求查库。未见双实例自动化证据 → 标 **未知（设计上无缓存窗口，亦无正式验收）** |
| AT-07 | 业务密钥读取/列表/故障不回退明文 | **仍成立（未达标）** | SecretCipher 未落地；库内明文 |
| AT-08 | 异常分页/超大批量/超大文件受控 | **部分** | Sql 分页 clamp；通用 `PageQuery011` 无上限；无 `@Valid`；Http 二进制仍可整包入堆 |
| AT-09 | 空库部署/升级/应用回退与 DB 兼容 | **仍成立（未达标）** | 无 Flyway/Liquibase |
| AT-10 | 双实例调度/重复消息/导出/中途重启无重复副作用 | **仍成立（未达标）** | Quartz RAM；无集群锁验收 |
| AT-11 | 跨用户/部门/租户越界阻断 | **仍成立（未达标）** | 无数据范围模型 |
| AT-12 | 非授权出站/重定向/超时 | **仍成立（未达标）** | 无 allowlist SSRF 治理 |
| AT-13 | 错误/依赖故障可关联日志·trace·指标；敏感不泄露 | **部分** | traceId + actuator/prometheus 有；无 OTel/告警闭环验收 |
| AT-14 | 发布失败与备份恢复按 Runbook | **仍成立（未达标）** | 无企业级 runbook/演练记录目录实证 |
| AT-15 | 非作者按文档完成试点部署 | **未知** | 有 `020.business-project-quickstart.md`；未见非作者实测记录 |

---

## 九、计数汇总

### 审计发现条目（上表合并计）

| 结论 | 数量 | 条目 ID |
|---|---:|---|
| 已修复 | **0** | —（无整项从「成立」变为「完全不成立」） |
| 部分 | **14** | P0-2b, A-1, A-2, A-3, A-4, AUTH-1, API-1b, CI-1, OBS-1, AT-01, AT-02, AT-04, AT-08, AT-13 |
| 仍成立 | **24** | P0-1, P0-2a, P0-3, P0-4, A-5, AUTH-2, AUTH-3, API-1, API-2, API-3, CI-2, CI-3, OBS-2, DOC-1, DOC-2, MISC-1…4, 及 AT 未达标项计入下表 |
| 不适用/未知 | **2** | AT-06（未知）, AT-15（未知） |

> **主表（不含 AT）计数**：已修复 **0** / 部分 **9** / 仍成立 **18** / 不适用 **0**。  
> **含 AT-01…15**：已修复 **0** / 部分 **14** / 仍成立 **24**（含 AT 未达标） / 不适用·未知 **2**。

### 相对审计时的主要正向变化（仍不够「已修复」）

1. 用户/管理面 UseCase **普遍挂上** `assertHas`；生产写白名单 PEP。  
2. infrastructure 已拆为 **center / starter** 模块。  
3. gate 从「仅 compile」改为 **`clean install`（跑测）**。  
4. **observability starter**（actuator + prometheus）已存在。  
5. 动态 SQL **pageSize clamp [10,500]**；部分 HTTP 流式 API。

### 仍阻断企业底座的 P0 残余

默认 **development + debug**、仓库内 **开发凭据/JWT**、debug **免认证 + assertHas no-op**、业务密钥 **明文**、Token **不可撤销**、无 **secret scan / 企业 CI / Flyway**。

---

*本文件为静态核对产物，不替代安全渗透或 G1 放行签字。*
