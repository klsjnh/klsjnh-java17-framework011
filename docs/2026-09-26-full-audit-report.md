# klsjnh-java17-framework011 全面审计报告（架构 · 代码 · 文档 · 协作 · 实机）

> **文件命名说明**：本文件名采用 `{日期}-{topic}.md` 形态。协作审计已指出现行约定没有定义这种形态（[COLLAB-15](#ch7-collab)、[DOC-11](#ch6-doc)），待约定补充后，可迁入 `archive011/` 或专门的 `reports/` 目录。
>
> **脱敏说明**：本文涉及的凭据、口令、账号、内网 IP 一律只保留前 3 个字符加 `***`（例如 `dev***`、`192.***`）。回环地址 `127.0.0.1` 是实测目标，保留原样。

## 0. 元信息

| 项 | 内容 |
|---|---|
| 日期 | 2026-09-26 |
| 项目 | klsjnh-java17-framework011（Java 17 / Spring Boot 3.4.5 多模块框架，15 个 Maven 模块） |
| 版本 | HEAD `21446a5`（`ver 0.0.63`），与 `origin/main` 一致 |
| 审计范围 | 全部 Java 源码（666 个文件）、pom 与 BOM、`application*.yml`、`docs/` 下全部 md（98 份，含根 README 和 `Agent.md`）、`docs/sql/`、`docs/deploy/`、`script011.sh`、`.github/workflows/gate.yml`、`tools/`、git 历史（68 个提交、5 个分支） |
| 方法 | ① 静态精读：代码、文档、协作机制三份独立子审计；② dev016（Ubuntu 26.04 / WSL2）实机运行：全量构建与测试、双模式启动、接口冒烟、防护验证。冲突时**以实测为准** |
| 审计人 | AI 助手 |
| 约束 | 全程只读；实机验证在 `/tmp` 下的独立克隆中进行，请求只打向本机 `127.0.0.1` 上审计自己启动的实例；审计与实机验证期间原仓库工作区保持干净。所有行号均以 HEAD `21446a5` 为准 |

**分级口径**

- 代码：**T0** 立即修复（已实测或证据确定，可导致未授权访问或凭据失效）；**T1** 上线前修复；**T2** 迭代内修复；**T3** 改进项。
- 文档：严重（阻断操作或造成误导）/ 中 / 低。
- 协作：高（泄露、错误提交、门禁失效）/ 中（流程与约定失真）/ 低（规范性）。

---

## 1. 结论摘要

**总体评价**：这个项目的分层意图、文档体系和功能完整度都不错，实机上构建测试全绿、主要功能正常；但它现在是「按 Maven 模块切开的一个应用」，还不是「可插拔、可发版的框架」，而且安全默认放行、部署默认进入 debug。

**T0（立即修复，共 3 条，详见[第 3 节](#ch3-t0)）**

1. **部署默认进入 debug，非 production 状态不会 fail-closed**（SEC-01 + DOC-01 + SEC-06）。实测复现：debug 下无 token、伪造 token、写接口全部返回 200。
2. **生产不拒绝仓库里的默认 JWT secret / master-key**（SEC-14）。实测复现：用默认 secret 以 production 状态启动成功，debug 实例签发的 token 被 production 实例接受。
3. **出站请求没有地址限制**（SEC-02 + SEC-10）。ASR 的 `audioUrl` 由普通有权限用户控制（静态证据）；Webhook 实测可以请求本机回环地址。

**最重要的 5 个架构问题（详见[第 2 节](#ch2-arch)）**

1. 模块只在 Maven 层面拆开，运行时连成一片：core 扫描所有中心的包，依赖方向反了，中心无法插拔、没有开关。**这是最该先改的一处。**
2. 安全靠一个开关、默认放行：debug 全放行，部署文件默认进 debug，启动守卫只认名为 `production` 的 profile，不拒绝默认密钥。
3. 对外调用没有统一出口：Webhook、AI、ASR、MinIO、JDBC 各自出站，没有地址策略和响应大小上限。
4. 作为框架，版本号不起作用：Maven 永远 `1.0.0`，git 用 `ver 0.0.63`，文件头统一写 `ver 0.0.1`，没有 tag 和 CHANGELOG。
5. 表结构演进没有归属：共用一个库，手工 SQL 散落，增量脚本不幂等、没有执行登记，新库按 README 初始化会报错。

**实测一句话**：`mvn -o clean install` 成功，16 个测试类 70 个用例 0 失败（7 个模块没有测试）；启动 9 秒、0 ERROR；debug 全放行、production 鉴权生效；token 吊销、存储、消息加密脱敏均正常；本地存储路径逃逸**未复现**，Webhook 回环请求**已复现**，Webhook **不跟随**重定向，上传**有** 1MB 默认上限。

**发现数量统计**

| 来源 | 合计 | T0 / 严重 / 高 | T1 / 中 | T2 / 低 | T3 |
|---|---|---|---|---|---|
| 代码（SEC-01…28、CODE-01…13） | 41 | T0：3 条，涵盖 5 个 ID | T1：10 | T2：16 | T3：10 |
| 文档（DOC-01…38） | 38 | 严重：7 | 中：15 | 低：16 | — |
| 协作（COLLAB-01…24） | 24 | 高：7 | 中：13 | 低：4 | — |
| **合计** | **103** | | | | |

代码的 T0 由 SEC-01、SEC-06、SEC-14、SEC-02、SEC-10 五个 ID 合并成 3 条。其中 SEC-06、SEC-10、SEC-14 在代码审计中原为 T1，本报告依据实测证据上调；分级变化见[第 8 节](#ch8-corrections)。

---

<a id="ch2-arch"></a>

## 2. 架构评估

下面 8 点按「现状 → 问题 → 为什么是架构问题 → 建议 → 证据」展开。前 4 点决定这个框架能不能被别的项目放心引用，建议优先处理。

### 2.1 模块只在 Maven 层面拆开，运行时连成一片（最该先改）

**现状**

- `java17-core011/.../CoreAutoConfiguration011.java:32` 用 `@ComponentScan(basePackages = { "com.klsjnh.application", "com.klsjnh.web", "com.klsjnh.infrastructure" })` 扫描所有中心的 application、web、infrastructure 包。
- `java17-data-mybatis011-starter/.../DataMybatisAutoConfiguration011.java:30-32` 的 `@MapperScan` 把 iam、system011、datasource、aicenter、storagecenter、messagecenter 这几个中心的 mapper 包全部登记在一处。
- 6 个 center 模块、security-autoconfigure、scheduler 都没有自己的 `AutoConfiguration.imports`，完全靠 core 扫包加载。实机启动日志证实只加载了 core、data-mybatis、observability 三个模块的 AutoConfiguration（`CoreAutoConfiguration011`、`DataMybatisAutoConfiguration011`、`ObservabilityAutoConfiguration011`、`QuartzObservabilityAutoConfiguration011`、`StorageObservabilityAutoConfiguration011`）。
- `PermissiveAuthorizationPort.java:33-34` 在被扫描到的 `@Component` 上使用 `@ConditionalOnMissingBean(AccessCenterMarker011.class)`，而 Spring 文档明确说明这种用法的结果取决于 bean 定义的注册顺序。

**问题**：依赖方向反了。底座（core、data-mybatis）认识每一个上层中心的包名和 mapper 位置；中心只要 jar 在 classpath 上就生效，没有开关可以关掉；「没装 access 中心就放行」的兜底逻辑依赖加载顺序，是不确定的。

**为什么是架构问题**：每新增一个中心都要改底座；业务项目想只用其中两三个中心时，无法关闭其余中心；兜底 bean 的顺序问题一旦出错，结果就是生产环境没有权限控制（附录 F 列为待实测项）。中心越多，问题越大。

**建议**：每个中心自带 `XxxAutoConfiguration011`（登记在自己的 `AutoConfiguration.imports`）、自己的 `@MapperScan`，并提供 `krt.center.xxx.enabled` 开关（`@ConditionalOnProperty(..., matchIfMissing = true)`）；core 不再扫描任何中心；兜底 bean 改为在 `@AutoConfiguration(after = AccessAutoConfiguration011.class)` 中用 `@Bean @ConditionalOnMissingBean(AuthorizationPort.class)` 注册；配套 `ApplicationContextRunner` 装配测试（见 2.7）。

**证据**：[CODE-06](#ch5-t2)；实机启动日志（[4.4](#ch4-boot)）。

### 2.2 中心之间的协作没有约定

**现状**

- AI 中心硬依赖存储中心：`center-ai011-starter/pom.xml:33` 引入 `center-storage011-starter`，且不是 optional。
- 权限目录种子通过可选依赖接入 access 中心：platform、datasource、message、storage、ai 五个中心的 pom 里 `center-access011-starter` 都是 `<optional>true</optional>`。
- 消息中心的 application 层直接引用 infrastructure 层的加密类：`JulyInboundChannelUseCase.java:32`、`JulyOutboundChannelUseCase.java:32` 都 `import com.klsjnh.infrastructure.messagecenter.crypto.ChannelConfigCipher011;`。
- AI 中心在自己的事务里写存储中心：`JulyAiDomainUseCase.store`（`:550-567`）在 `@Transactional` 内写文件，事务回滚后文件还在，成为孤儿文件；反过来，文件写成功而数据库回滚时，指针会丢失。

**问题**：中心之间用什么方式调用（直接依赖实现、可选依赖、还是事件）没有统一规则；跨中心操作没有一致性策略。

**为什么是架构问题**：中心数量增长后，依赖会变成网状；任何一个中心改内部实现，都可能破坏依赖它的中心；跨中心的数据一致性问题会随调用点增多而增多。

**建议**：每个中心拆出 `center-xxx-api`（只放接口和 DTO），中心之间只能依赖这一层；同步调用走接口，异步协作走事件（或 outbox）；跨中心写操作放到事务提交后执行（`TransactionSynchronization.afterCommit`），或配孤儿清理任务；在 domain 层抽 `ChannelConfigCryptoPort`，由 infrastructure 实现。

**证据**：[CODE-04](#ch5-t2)、[CODE-02](#ch5-t2)、[附录 B](#appx-b)、[5.5 事务使用情况](#ch5-tx)。

### 2.3 作为框架，版本号不起作用

**现状**：同时存在三套版本号——Maven 版本永远是 `1.0.0`（`pom.xml:9`，`script011.sh:173` 的 `APP_JAR` 也写死为 `-1.0.0.jar`）；git 提交信息和 `.vf` 用 `ver 0.0.63`；666 个 Java 文件的文件头统一写 `@version ver 0.0.1`（汇总时抽查）。仓库没有任何 tag，也没有 CHANGELOG。根 `pom.xml:50-63` 与 `java17-bom011/pom.xml:16-28` 有 13 个版本属性重复，`lombok.version` 只在根 pom 里定义；技术债红线文档却写「版本一律由父 `pom.xml` 管理」。

**问题**：消费方从 `.m2` 拿到的永远是 `1.0.0`，无法区分构件；两份版本表会漂移；没有不兼容变更的约定。

**为什么是架构问题**：框架的价值在于被多个业务项目引用。没有可用的版本号，业务项目无法锁定版本、无法评估升级影响，框架也就无法同时支持多个下游。

**建议**：采用 SemVer；`java17-bom011` 作为唯一版本表（lombok 也纳入 BOM 的 `dependencyManagement`），根 pom 只保留插件需要的属性；发版时 `mvn versions:set` 加 `git tag vX.Y.Z` 并更新 CHANGELOG；在约定里写明不兼容变更的处理方式（主版本号递增、迁移说明）；文件头的 `@version` 要么随发版更新，要么删除。

**证据**：[CODE-11](#ch5-t3)、[DOC-15](#ch6-doc)、[COLLAB-09](#ch7-collab)、[COLLAB-14](#ch7-collab)。

### 2.4 安全靠一个开关，而且默认放行

**现状**

- `krt.status=debug` 时鉴权过滤器不拒绝请求，`assertHas` 直接返回（`GlobalAuthFilter.java:235`、`AuthorizationAdapter.java:162-164`）。
- 部署文件默认进入 debug：`application.yml:8` 默认 `active: development`，`application-development.yml:24` 为 `status: ${KRT_STATUS:debug}`，`docs/deploy/docker-compose.yml:16-19` 没有设置 profile 和任何 `KRT_*`。
- 启动守卫只认名字恰好叫 `production` 的 profile（`KrtSecurityConfig011.java:92`），`prod`、`prd` 等名字不会触发。
- 生产状态下只检查 secret 非空、master-key 至少 32 字节（`KrtSecurityConfig011.java:97-104`），不拒绝仓库里的默认值。实测：默认 secret 下以 production 状态启动成功，跨实例 token 互认。
- 权限闸门在业务执行后才检查（`GlobalAuthFilter.java:240-241` 先 `doFilter` 再 `enforcePermissionWhitelist`）。
- JWT 名义上无状态，实际每个请求都查一次用户表且没有缓存（`JwtAuthTokenService.java:148`）。

**问题**：安全的正确性取决于部署者记得设置两三个环境变量；任何一处遗漏，结果都是「全部放行」而不是「启动失败」。

**为什么是架构问题**：部署实例越多、下游项目越多，总会有一次忘记设置；而一旦忘记，后果是整个系统裸奔。安全机制的默认值必须是拒绝。

**建议**：fail-closed——非 production 状态必须显式声明允许（例如 `krt.allow-insecure-status=true`），否则启动失败；生产拒绝与仓库默认值相同的密钥；把执行后的权限闸门改为执行前的「权限声明检查」（`@RequiresPermission` + `HandlerInterceptor`），保留现有闸门作兜底；为 token 状态建立独立的服务并加约 30 秒本地缓存，改密、停用时主动清除；鉴权实现保持可替换（沿用 `java17-security-api011` 的端口接口思路）。

**证据**：[SEC-01、SEC-06、SEC-14（T0）](#ch3-t0)、[DOC-01](#ch6-doc)、[SEC-05](#ch5-t1)、[SEC-19](#ch5-t2)；实测见 [4.6](#ch4-auth)。

### 2.5 表结构演进没有归属

**现状**：所有中心共用一个库，靠表前缀区分；`docs/sql/` 下有 9 个全量建表文件和 3 个增量脚本。增量脚本没有序号或日期前缀、没有执行登记，其中 `token-version-column.sql` 和 `logic-delete-unique-fix.sql` 是裸 `ADD COLUMN`，重跑就报错；修复脚本 `alive_user_account VARCHAR(30)` 与建表 `varchar(60)` 不一致。`docs/sql/README.md:31` 要求新库执行逻辑删除唯一键迁移，但建表文件已包含 `alive_*`，新库执行到第 2 步就报 `Duplicate column`。

**问题**：哪个中心拥有哪些表、升级该跑哪些脚本、某个库当前处于哪个版本，都没有记录。

**为什么是架构问题**：中心和部署实例越多，手工维护的升级顺序就越不可靠；新库与升级库的结构会越来越不一致；中心也就无法独立演进。

**建议**：每个中心自带 Flyway 迁移目录 `db/migration/<center>`（随 starter 打包、随开关启用），全量建表脚本转为 `V1__baseline`；增量脚本改名为 `V{日期}_{序号}__{说明}.sql` 格式并保证幂等；统一列宽。

**证据**：[DOC-06、DOC-16、DOC-07](#ch6-doc)、[6.4 SQL 专项](#ch6-sql)。

### 2.6 对外调用没有统一出口

**现状**：Webhook（`WebhookMessageChannel.java:69-76`）、AI `baseUrl`（`AiModelProvider.java:172-174`，只检查 `http://` / `https://` 前缀）、ASR `audioUrl`（`OpenAiCompatAsrAdapter.java:85-86`）、MinIO endpoint、数据源 JDBC URL（`JulyDatasource.java:247-259`，只检查 `jdbc:` 前缀，驱动类任意）各自出站。`HttpUtil011` 没有 scheme 或主机校验（`:330-331` 直接 `URI.create(url)`），`getBytes` 用 `ofByteArray()` 整体读入内存，没有大小上限。实测：Webhook 配成 `http://127.0.0.1:18999/probe`，服务端确实向该端口发起了 POST。

**问题**：出站的地址策略、超时、响应大小上限、审计都没有集中控制点。

**为什么是架构问题**：每新增一个外部集成（新 AI 厂商、新消息渠道）都会再多一个不受控的出口；安全策略只能逐个补，漏一个就是一个 SSRF 点。

**建议**：在 core 中提供统一的出站 HTTP 端口（地址策略：scheme 白名单、拒绝回环/链路本地/私网/组播地址，连接时用解析出的 IP 直连以防 DNS 重绑定；超时；响应大小上限；出站审计），所有中心只能通过它出站；JDBC 按 `dbType` 建立 URL 前缀和驱动类白名单，并拒绝危险参数。

**证据**：[SEC-02、SEC-10（T0）](#ch3-t0)、[SEC-09](#ch5-t1)、[SEC-24、SEC-28](#ch5-t3)；实测见 [4.9](#ch4-guard)。

### 2.7 质量保障的结构

**现状**

- 两套门禁口径：本地 `script011.sh gate` 跑 `mvn -o clean install -q`（离线、静默、依赖当前目录）；CI `gate.yml` 跑 `mvn -B clean test`（在线、不打包、不 install），Node 版本也不同（本地 v22、CI 20）。任何文档都没有提到 CI。
- 测试规模：实机 surefire 统计 16 个测试类、70 个用例（HEAD 中有 17 个测试源文件，差异见附录 F）；7 个模块没有测试（bom、data-mybatis、observability、security-starter、datasource、ai、app011，其中 bom、security-starter、app011 以组装为主）。
- 没有装配测试（`ApplicationContextRunner`），也没有契约测试。
- 关键安全路径没有测试：`GlobalAuthFilter.doFilterInternal`、`SqlGuard011`、`LocalObjectStorageAdapter.resolve`、`JulyUserRoleAssignUseCase`、JDBC URL 校验、`assertHas` 覆盖率等。

**问题**：门禁「绿」只能说明已有测试通过，而已有测试没有覆盖最危险的路径；本地和 CI 可能出现一绿一红。

**为什么是架构问题**：2.1 的装配改造、2.4 的 fail-closed 改造都需要装配测试兜底，否则改造本身就无法验证；中心越多，缺少契约测试的代价越大。

**建议**：抽出 `tools/gate.sh`，本地与 CI 共用；CI 改为 `mvn -B clean install`；为每个 starter 补 `ApplicationContextRunner` 装配测试（开关开/关、有无 access 中心）；为 `center-xxx-api` 补契约测试；用 ArchUnit 断言分层方向和「写方法必须调用 `assertHas`」；按附录 D 补齐安全路径测试。

**证据**：[COLLAB-13、COLLAB-08](#ch7-collab)、[CODE-08](#ch5-t2)、[附录 D](#appx-d)；实测见 [4.3](#ch4-test)。

### 2.8 文档与协作体系

**现状与问题**

- **设计是好的，但没有自动校验**：把文档作为 AI 的唯一真源，这个方向正确；但文档门禁（`tools/klsjnh-standards-doc.mjs:4-13`）只有一条 `doc-api-path` 规则，结果是 25 条一致性抽查中 10 条不一致，配置文档自称「配置键事实来源」却漏了 6 组 `krt.*` 键，新人从 clone 到登录的路径有 4 处断点（缺 MySQL 前置、缺建库建用户、SQL 第 2 步报错、没有种子用户；另有首次构建用了离线模式）。
- **约定与实际脱节**：4 个提交（`8588f64`、`cd5b06e`、`f9d9fb2`、`1ca542e`）绕过了 `script011.sh`，其中两个是改动 481 和 182 个文件的最大提交；15 个有提交的日子里，chat 日志只覆盖 4 天。
- **脚本本身不可靠**：`script011.sh` 的门禁和 `git add .` 没有先切换到仓库根目录，从子目录运行会检查错对象、提交错范围；push 前不 fetch，远端领先时会留下「已提交、未推送、`.vf` 已自增」的半完成状态。
- **执行环境没有写进文档**：约定只写「本机执行，禁沙箱」，全仓文档里没有 dev016、WSL、`/mnt/d` 的说明；dev016 上 `/klsjnh/java202608/klsjnh-java17-framework011` 是一份与正式仓库没有共同历史的旧副本（HEAD `3fa5d77`，ver 0.0.12），origin 相同，险些被当作执行副本误操作。

**为什么是架构问题**：这个项目的协作模式是「AI 按文档行事、按脚本提交」。文档漂移会直接变成 AI 的错误操作；脚本缺乏身份校验，会把一次误操作放大为错误提交甚至历史覆盖。协作者和 AI 会话越多，风险越高。

**建议**：文档门禁补 `doc-link`、`doc-heading-seq`、`doc-root-dated`、`doc-number-4` 四条规则，并加入配置键与 `@ConfigurationProperties` 的比对；服务端兜底（main 分支保护、Gate 设为必选检查）；脚本开头 `cd "$PROJECT_ROOT"`，并校验仓库根、根提交、origin、JDK 主版本；push 前 `fetch` + `rebase`；在 `015.project-info.md` 里加一张执行环境卡；废弃 dev016 上的旧副本。

**证据**：[DOC-22、DOC-03、DOC-04](#ch6-doc)、[6.5 新人上手路径](#ch6-onboard)、[COLLAB-01、COLLAB-02、COLLAB-06、COLLAB-15、COLLAB-22、COLLAB-23](#ch7-collab)、[附录 E](#appx-e)。

### 2.9 目标形态

```text
java17-app011（宿主，只做组装）
├─ center-xxx-starter        自带 AutoConfiguration + 开关 + 自己的 Flyway 迁移
│   └─ 依赖 → center-yyy-api （只有接口和 DTO，中心之间只能依赖这一层）
├─ security-starter          接口化鉴权；生产检测到 debug/默认密钥即拒绝启动
├─ data / observability / scheduler starter
└─ core                      公共能力 + 统一出站 HTTP 端口，不扫描任何中心
java17-bom011                唯一版本来源（SemVer + tag + CHANGELOG）
```

---

<a id="ch3-t0"></a>

## 3. T0 清单（立即修复）

### T0-1 部署默认进入 debug，非 production 状态不 fail-closed（SEC-01 + DOC-01 + SEC-06）

**证据**

- `java17-app011/src/main/resources/application.yml:7-10`：`profiles.active: development`。
- `application-development.yml:23-24`：`krt.status: ${KRT_STATUS:debug}`。
- `docs/deploy/docker-compose.yml:16-19`：`environment` 只有 `JAVA_OPTS`、`LOGGING_FILE_PATH`，没有 `SPRING_PROFILES_ACTIVE` 和任何 `KRT_*`；`docs/deploy/README.md:41` 要求把 `application.yml` 和 `application-development.yml` 挂进容器。这与 `015.topic-config.md` §017（L78-80）要求的 `SPRING_PROFILES_ACTIVE=production` 相矛盾。
- `KrtSecurityConfig011.java:92`：守卫只在 profile 名字恰好为 `production` 时触发。
- `JulyUserUseCase.java:335-352`：只要 `allowsPasswordlessLogin()` 为真（debug 和 development 状态），`loginByUserName` 就直接签发 token；该接口在 JWT 白名单里。
- 汇总时抽查补记：`application-development.yml:21` 的注释写「生产必须改为 production + 真实 secret（见 application-production.yml）」，但仓库中不存在 `application-production.yml`（`java17-app011/src/main/resources/` 下只有 `application.yml` 和 `application-development.yml`）。

**触发链路**：按 compose 启动 → profile 为 development → `krt.status=debug` → 过滤器对无 token 请求放行（`GlobalAuthFilter.java:235`）→ `assertHas` 不生效（`AuthorizationAdapter.java:162-164`）→ 免密登录可用 → 兜底异常处理在 debug 下返回异常类名和 message。

**实测结果**（dev016，11165 端口，development / debug）：启动日志打印 4 条安全横幅 WARN「`krt.status=debug: auth filter does NOT reject requests; AuthorizationPort.assertHas is a no-op. NEVER ship this.`」；免密登录 200；受保护读接口无 token、合法 token、他人 token、伪造 token、垃圾 token 一律 200；受保护写接口（新增用户）无 token 200。

**影响**：任何能访问到服务的人都可以匿名调用全部管理接口，包括执行 SQL、增删改用户和角色、读写存储对象。

**修复方案**

1. 从 `application.yml` 删除 `spring.profiles.active: development`，本地开发通过 IDE 或 `-Dspring.profiles.active=development` 指定。
2. `application-development.yml:24` 改为 `status: ${KRT_STATUS:development}`，不再默认 debug。
3. 启动守卫改为：非 production 状态必须显式允许，否则启动失败。

```java
@PostConstruct
public void validate() {
    boolean insecureAllowed = Boolean.parseBoolean(environment.getProperty("krt.allow-insecure-status", "false"));
    if (status != FrameworkStatus011.PRODUCTION && !insecureAllowed) {
        throw new IllegalStateException("krt.status=" + status.getCode()
                + " requires krt.allow-insecure-status=true (never set in deployed environments)");
    }
    // ... 原有 jwt / master-key 校验
}
```

4. 免密登录只在 debug 状态开放，并且只接受回环来源：`if (!runtimeStatusPort.isDebug() || !InetAddress.getByName(ip).isLoopbackAddress()) { throw ... }`。
5. compose 显式加上 `SPRING_PROFILES_ACTIVE=production`、`KRT_STATUS=production`、`KRT_JWT_SECRET`、`KRT_CRYPTO_MASTER_KEY`、`SPRING_DATASOURCE_*`，使用 `${VAR:?}` 必填形式；deploy README 改为挂载生产配置，并补上仓库中缺失的 `application-production.yml`（或删除 dev yml 中对它的引用）。

### T0-2 生产不拒绝仓库默认的 JWT secret / master-key（SEC-14）

**证据**

- `application-development.yml:26`：`secret: ${KRT_JWT_SECRET:dev***}`（默认值写在仓库里）。
- `application-development.yml:30`：`master-key: ${KRT_CRYPTO_MASTER_KEY:dev***}`。
- `application-development.yml:42-44`：MinIO 凭据默认值（`dem***` / `123***`）。
- `KrtSecurityConfig011.java:97-104`：production 状态下只检查 secret 非空、master-key 不少于 32 字节，不检查是否为仓库默认值。

**实测结果**：以 `--krt.status=production` 在 11161 端口启动第二个实例（profile 仍为 development，未注入 `KRT_JWT_SECRET`），启动成功（8.927 秒），日志为 `krt.status = PRODUCTION (passwordless login disabled, permission PEP whitelist)`。两个实例共用默认 secret，**debug 实例签发的 token 被 production 实例接受**（受保护读 200）。这正是按部署手册挂载 `application-development.yml` 并只改 `KRT_STATUS` 时会得到的状态。

**影响**：任何读过仓库的人都能用默认 secret 伪造 token（前提是知道用户 id，且 token 版本号 `tv` 对得上）；拿到数据库备份的人能用默认 master-key 解密所有存储凭据、数据源密码、AI apiKey。

**修复方案**

1. 删除 `application-development.yml` 中密钥类配置的默认值，本地开发改用 `.env`；缺失时启动失败。
2. production 状态下，启动守卫拒绝与仓库内已知默认值相同的 secret / master-key（按 SHA-256 指纹比较，避免把默认值明文写进守卫代码），并要求 secret 至少 32 字节（与签发时的要求一致，见 SEC-19）。
3. 因为默认值已进入 git 历史，已部署环境如使用过默认值，必须轮换。

> 实机验证报告把这一条归为「部署配置卫生问题，非代码缺陷」。本报告不采纳这个定性：框架的启动守卫本来就负责拦截不安全的生产配置，它没有拦截已知默认密钥，属于框架层面的 fail-open，因此定为 T0。

### T0-3 出站请求没有地址限制（SEC-02 + SEC-10）

**证据**

- ASR（静态证据）：`OpenAiCompatAsrAdapter.java:85-86` 在请求没有音频字节时，直接 `HttpUtil011.getBytes(request.audioUrl())`。数据流：`AiAsrRequestVo011.audioUrl` → `AiAudioController.recognize`（`:121`）→ `AiAsrUseCase:83`（有 ASR 权限校验）→ adapter → `HttpUtil011.getBytes`。`audioUrl` 由持有 ASR 权限的普通用户控制。
- `HttpUtil011.java:136-141` 用 `BodyHandlers.ofByteArray()` 整体读入内存，没有大小上限；`:330-331` 直接 `URI.create(url)`，没有 scheme 或主机校验。
- Webhook：`WebhookMessageChannel.java:69-76` 从渠道配置取 `url` 直接 `postJson`。
- AI `baseUrl`：`AiModelProvider.java:172-174` 只检查前缀；被探测、推理、生图、TTS、ASR 六个调用点使用，请求会带上 `Authorization: Bearer <apiKey>`。

**实测结果**：Webhook 渠道 url 配为 `http://127.0.0.1:18999/probe`，触发发送后，本机监听收到 `POST /probe ... from=127.0.0.1 UA=Java-http-client/17.0.12`，**确认可以请求回环地址**。ASR 的 `audioUrl` 路径未实测（需要可用的 AI 厂商配置），属于静态证据（附录 F）。

**影响**：持有 ASR 权限的用户可以让服务端请求云元数据地址（`169.***`）、本机 actuator 或其他内网地址，下载到的内容还会被转发给 AI 厂商；指向超大文件的 URL 可以打满服务端内存。管理员账号一旦被盗，可以让服务端向内网发 POST，并把 AI apiKey 发到攻击者指定的地址。叠加 T0-1 时，连登录都不需要。

**修复方案**：新增统一的出站地址检查，所有出站调用先经过它（代码审计给出的参考实现）。

```java
public final class OutboundUrlGuard011 {
    public static URI assertSafe(String raw, boolean allowHttp) {
        URI uri = URI.create(raw).normalize();
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"https".equals(scheme) && !(allowHttp && "http".equals(scheme))) {
            throw BusinessException.badRequest("outbound scheme not allowed");
        }
        try {
            for (InetAddress a : InetAddress.getAllByName(uri.getHost())) {
                if (a.isAnyLocalAddress() || a.isLoopbackAddress() || a.isLinkLocalAddress()
                        || a.isSiteLocalAddress() || a.isMulticastAddress()) {
                    throw BusinessException.badRequest("outbound host not allowed");
                }
            }
        } catch (UnknownHostException ex) {
            throw BusinessException.badRequest("outbound host unresolvable");
        }
        return uri;
    }
}
```

- `getBytes` 改为 `ofInputStream()`，边读边计数，超过上限（例如 25MB）即中断。
- ASR 最好去掉 `audioUrl` 参数，或只接受本系统存储中心生成的预签名地址。
- 只在解析时检查一次 IP，仍可能被 DNS 重绑定绕过；更彻底的做法是连接时用解析出的 IP 直连，或走统一出站代理（见 2.6）。
- 生产环境强制 HTTPS，必要时按厂商域名设白名单。

---

<a id="ch4-verify"></a>

## 4. 实机运行验证（dev016）

构建和两次启动都在 `/tmp/audit011_src`（从同一仓库 `git clone`，HEAD 一致）中进行；标准检查在原仓库只读执行。所有请求只打向本机 `127.0.0.1` 上审计自己启动的实例（11165、11161）和临时监听端口（18998、18999）。结束时两个实例均已停止、端口释放，探针用户和 Webhook 渠道已逻辑删除，原仓库 `git status --porcelain` 为空。占用 11160 端口的既有实例在另一个发行版中，全程未干预。

### 4.1 环境

| 项 | 值 |
|---|---|
| 发行版 | dev016（Ubuntu 26.04 / WSL2） |
| JDK | 17.0.12 LTS（Oracle，`/usr/local/java17`） |
| Maven | 3.9.12（`/usr/local/maven`） |
| Node | v22.22.0 |
| 数据库 | `jdbc:mysql://127.0.0.1:33306/klsjnh_framework011`，docker 容器 `klsjnh-framework011-mysql`，MySQL 5.7.26；账号 `dev***` / 口令 `dev***` |
| 其他默认凭据（来自 dev yml 与 `.env.example`） | Druid `kls***` / `kls***`；JWT secret `dev***`；master-key `dev***`；MinIO `dem***` / `123***` |
| 连通性 | `nc 127.0.0.1 33306` 成功；`mysql` 客户端需加 `--ssl-mode=DISABLED`（5.7 的旧 TLS 与新客户端不兼容，应用侧 `useSSL=false` 不受影响）；库和全部 `july_*` 表存在 |

### 4.2 标准检查

`node tools/check-klsjnh-standards.mjs .` → **PASSED**：扫描 666 个 Java 文件，AST 检查 0 问题，doc 检查 0 问题。

<a id="ch4-test"></a>

### 4.3 全量构建与测试

`mvn -o clean install`（含测试）→ **BUILD SUCCESS**，Reactor 耗时 38.1 秒，生成 `java17-app011/target/java17-app011-1.0.0.jar`。另有 5 处非致命 POM 风格告警（例如 `java17-app011` 的 `maven-install-plugin` 缺版本、聚合 jar「JAR will be empty」）。

| 模块 | 测试类 | 用例 | 失败 | 错误 | 跳过 | 用时 |
|---|---|---|---|---|---|---|
| java17-security-api011 | 1 | 6 | 0 | 0 | 0 | 0.06s |
| java17-core011 | 4 | 13 | 0 | 0 | 0 | 2.06s |
| java17-security-autoconfigure011 | 3 | 14 | 0 | 0 | 0 | 1.79s |
| center-access011-starter | 1 | 6 | 0 | 0 | 0 | 1.44s |
| center-storage011-starter | 1 | 2 | 0 | 0 | 0 | 0.05s |
| center-platform011-starter | 1 | 2 | 0 | 0 | 0 | 1.44s |
| java17-scheduler-quartz011-starter | 2 | 7 | 0 | 0 | 0 | 1.27s |
| center-message011-starter | 3 | 20 | 0 | 0 | 0 | 1.47s |
| **合计** | **16** | **70** | **0** | **0** | **0** | — |

没有测试的 7 个模块：`java17-bom011`、`java17-data-mybatis011-starter`、`java17-observability011-starter`、`java17-security-starter`、`center-datasource011-starter`、`center-ai011-starter`、`java17-app011`。

<a id="ch4-boot"></a>

### 4.4 启动要点

- 11165 端口，development profile，`krt.status` 为默认 debug：`Started Framework011Application in 8.983 seconds`（进程 9.632 秒），Tomcat 10.1.40。
- **ERROR 0 条**；WARN 5 条，其中 4 条是 `KrtSecurityConfig011` 的安全横幅，1 条是数据源种子 `page-demo` 因逻辑删除墓碑被跳过（业务正常）。
- 日志：`krt.status = DEBUG (passwordless login enabled, permission PEP assertHas-noop)`。
- 加载的框架 AutoConfiguration：`CoreAutoConfiguration011`、`DataMybatisAutoConfiguration011`、`ObservabilityAutoConfiguration011`、`QuartzObservabilityAutoConfiguration011`、`StorageObservabilityAutoConfiguration011`；`CoreAutoConfiguration011` 组件扫描 `com.klsjnh.{application,web,infrastructure}`（佐证 2.1）。
- 其他：Druid 初始化；动态数据源 `enabled 6 registered 6`；Quartz 2.3.2（佐证 DOC-13）；iam、platform、scheduler、ai、message、storage、datasource 各中心权限目录种子就绪；actuator `Exposing 4 endpoints`。

### 4.5 接口冒烟（公共与文档接口）

| 请求 | HTTP | 摘要 |
|---|---|---|
| `GET /actuator/health` | 200 | `{"status":"UP"}` |
| `GET /doc.html` | 200 | Knife4j 页面 |
| `GET /v3/api-docs/swagger-config` | 200 | 6 个分组：aicenter、iam、storagecenter、datasource、messagecenter、system011 |
| 各分组 api-docs | 200 | 36 + 47 + 29 + 16 + 33 + 29，去重后 **190 个路径 / 190 个操作** |
| `GET /actuator/{metrics,prometheus}` | 200 | 指标可读 |
| `GET /druid/index.html` | 200 | 监控台开放（开发态） |

**接口数口径**：代码审计扫描 27 个 Controller 共 **195** 个接口方法；实机 api-docs 去重后为 **190** 个路径。两者口径不同（前者按源码中的 Controller 方法计数，后者按 api-docs 发布的去重路径计数），差值没有逐一核对，列入附录 F。

<a id="ch4-auth"></a>

### 4.6 debug 与 production 对比

受保护读接口为 `selectListByPage`，账号为 `docs/029` 中的种子超管账号。

| 场景 | debug（11165） | production（11161，`--krt.status=production`） |
|---|---|---|
| `loginByUserName`（免密） | 200，返回 token | **401** `passwordless login is not allowed in production` |
| `login` 错误口令 | 401 `wrong account or password` | 未测 |
| `login` 正确口令 | 200 | 未执行（不掌握种子账号真实口令） |
| 受保护读，无 token | **200** | **401** `unauthorized` |
| 受保护读，伪造 token | **200** | **401** `unauthorized` |
| 受保护读，他人 / 垃圾 token | **200** | 未测 |
| 受保护读，debug 实例签发的合法 token | 200 | **200**（共用默认 secret，见 T0-2） |
| 受保护写，无 token（新增用户） | **200** | 未测 |
| 受保护写，合法 token（超管，有权限） | — | 200（权限校验实际执行并通过） |

### 4.7 token_version 吊销验证（debug 态）

| 步骤 | HTTP | 结论 |
|---|---|---|
| `logout`（持旧 token） | 200 | 递增 `token_version` |
| 旧 token 调 `changePassword` | **401** `not authenticated` | 旧 token 已失效 |
| 重新登录取新 token 调 `changePassword` | 200 | 成功 |
| 改密后再用该 token 调 `changePassword` | **401** | 改密同样使旧 token 失效 |

### 4.8 存储与消息功能验证

- **存储中心**（本地 `local011`，桶 `backup011`）：上传 `audit_probe/hello.txt`（200），实际落在 `storage011/backup011/audit_probe/hello.txt`；列表返回该键；stat 返回 size=21；下载内容与上传一致；删除后列表为空。存储 provider 列表中 `accessKey` / `secretKey` 输出为 `******`。
- **消息中心**（Webhook 渠道）：新增渠道时 config 含 `secret`、`accessToken`；`getById` 返回 `"secret":"******","accessToken":"******"`；数据库中实际存储为 `enc:v1:...`。脱敏与加密均生效。

<a id="ch4-guard"></a>

### 4.9 防护验证

| 项 | 结果 | 证据 |
|---|---|---|
| 本地存储 objectName 绝对路径逃逸 | **未复现（防护有效）** | `objectName=/tmp/audit_probe/marker.txt` 上传后，`/tmp/audit_probe` 始终为空，文件落在 `/tmp/audit011_src/storage011/backup011/tmp/audit_probe/marker.txt`。`Paths.get(basePath, bucket, key)` 把前导 `/` 当作分隔符，结果仍在桶目录内 |
| objectName 含 `..` | **被拒** | `../`、`a/../../`、`..%2F`、`/../../../tmp/...` 四种均返回 HTTP 500 `invalid object key`（状态码语义不对，见 CODE-01） |
| 桶名逃逸（`../..`、`/tmp/...`） | **被拒** | HTTP 500 `invalid bucket name` |
| Webhook 请求本机回环 | **已复现（无出站限制）** | 见 T0-3 |
| Webhook 跟随 302 重定向 | **不跟随** | 目标为返回 302 的 18998 端口时，结果为 `success:false, error:"webhook returned HTTP 302"`，重定向目标 18999 未被请求（JDK `HttpClient` 默认 `NEVER`） |
| 上传大小限制 | **有限制（Spring 默认单文件 1MB）** | 20MB 上传返回 HTTP **400** `MaxUploadSizeExceededException`，约 1.8MB 时连接即被切断；2MB 同样被拒 |

### 4.10 未执行项及原因

| 项 | 原因 |
|---|---|
| production 状态下用真实口令登录 | 不掌握种子账号真实口令（尝试一个猜测值返回 401）；改用「免密被拒 401 + 无 token / 伪造 token 被拒 401」证明生产鉴权闸门生效 |
| 以 `--spring.profiles.active=production` 启动 | 未执行；仓库里没有 `application-production.yml`，需要单独准备配置 |
| ASR `audioUrl` SSRF | 需要可用的 AI 厂商配置，未实测 |
| SEC-04 编码路径、SEC-08 括号闭合 SQL、SEC-09 JDBC 参数、SEC-07 停用角色、CODE-06 jar 顺序 | 不在本轮实测范围，列入附录 F |
| GitHub 分支保护、必选检查、Actions 运行记录 | 本机没有 `gh`，无法核实（COLLAB-02、COLLAB-13） |

---

<a id="ch5-code"></a>

## 5. 代码问题分级

T0 见[第 3 节](#ch3-t0)（SEC-01、SEC-06、SEC-14、SEC-02、SEC-10）。以下为其余 36 条。

<a id="ch5-t1"></a>

### 5.1 T1：上线前修复（10 条）

| ID | 标题 | 位置 | 影响 | 修复方向 |
|---|---|---|---|---|
| SEC-03 | 数据同步源端 SQL 不经过 `SqlGuard011`，`table` 模式直接拼接表名 | `SyncSource011.java:54` | 有同步规则权限的人可以读取任意已注册数据源的任意表，绕开 `julySql` 的只读限制 | 源端用 `SyncSink011.quote` 的标识符白名单 + `SqlGuard011.assertReadOnly`；保存规则时就校验 |
| SEC-04 | 鉴权过滤器用未解码的 `getRequestURI()` 匹配白名单，与容器路由路径不一致 | `GlobalAuthFilter.java:286-292` | `%2e%2e`、`%6b` 这类编码可能绕过白名单判断或闸门（待实测） | 用 `getServletPath()` + `getPathInfo()`；直接拒绝 `%2e`、`%2f`、`%5c`、`;` |
| SEC-05 | production 的写接口权限闸门在业务执行之后才检查 | `GlobalAuthFilter.java:240-241` | 只能改写响应，已提交的数据改不回来；select/get/stat/download 前缀和 logout 被豁免（`PermissionWhitelistGate011.java:107-108`） | 执行前的 `@RequiresPermission` 声明检查，保留现有闸门兜底 |
| SEC-07 | 有 `ASSIGN_ROLES` 权限就能把内置角色分配给自己；停用角色仍生效 | `JulyUserRoleAssignUseCase.java:84-92`、`AuthorizationAdapter.java:115-127` | 越权获得全部权限 `*` | 分配时校验角色存在、启用，内置角色只能由内置操作员授予；计算权限时跳过停用角色 |
| SEC-08 | SQL 守卫只有关键字黑名单，括号闭合可绕过；执行无超时、非只读连接 | `SqlGuard011.java:33-67`、`SqlRoutingExecutor.java:296-298` | `SLEEP`/`BENCHMARK` 占住连接池；`load_file`、闭合后 `INTO OUTFILE` 待实测 | `setQueryTimeout`、`setMaxRows`、只读连接；剥注释、拒绝不配平括号、扩充黑名单；最好用 Druid WallFilter 或 JSqlParser；数据库账号只授 SELECT |
| SEC-09 | 数据源可配置任意 JDBC URL 和驱动类 | `JulyDatasource.java:247-259` | `allowLoadLocalInfile` 可让攻击者的 MySQL 反读应用服务器文件；`autoDeserialize` 待实测 | 按 `dbType` 建 URL 前缀和驱动白名单；拒绝危险参数；测试连接与正式建池同一套校验 |
| SEC-11 | 本地存储 `basePath` 可配成任意目录；`resolve` 缺少「规范化后仍在根目录下」的纵深防御 | `JulyStorageProvider.java:272-287`、`LocalObjectStorageAdapter.java:255-261` | 有存储实例修改权限的人把 `basePath` 改为 `/` 或 `C:\`，即可读写系统文件。objectName 路径逃逸**未复现**（见第 8 节） | `basePath` 限定在 `krt.storage-center.local011.allowed-roots` 之下；`resolve` 改为 normalize + `startsWith` |
| SEC-12 | 登录没有限流和失败锁定 | `JulyUserUseCase.java:305-311` | 可无限次尝试密码 | 按账号 + IP 滑动窗口计数（例如 5 分钟 5 次锁 15 分钟），单实例 Caffeine、多实例 Redis |
| SEC-13 | Webhook URL 中的 `access_token`/`sign` 不脱敏；嵌套字段不加密不脱敏 | `ChannelConfigCipher011.java:50-52,143-148,252` | 查看渠道详情时机器人 URL 原样返回前端 | `url` 类字段纳入加密集合，query 部分脱敏；`encryptFields`、`mask`、`merge` 改为递归 |
| CODE-01 | 全局异常处理映射不全，并把内部信息透给客户端 | `GlobalExceptionHandler.java:83-88,259-270` | 405/415/400 类异常落到兜底变 500；`MaxUploadSizeExceededException` 返回 400 而非 413；存储非法 key 返回 500 并刷 error 日志；解密失败返回 400 且带底层 message | 补齐 `@ExceptionHandler`；adapter 改抛 `BusinessException.badRequest`；解密失败改 500，只写服务端日志 |

**关键项证据片段**

SEC-05：权限闸门在 `doFilter` 之后执行。

```java
// java17-security-autoconfigure011/.../GlobalAuthFilter.java:240-241
            filterChain.doFilter(request, response);
            enforcePermissionWhitelist(request, response);
```

SEC-07：分配角色时不校验角色类型和状态；计算权限时遇到内置角色直接返回全部权限。

```java
// center-access011-starter/.../JulyUserRoleAssignUseCase.java:84-92
        for (String pkRole : desired) {
            if (!current.contains(pkRole)) {
                userRoleRepository.assign(id, pkRole);
            }
        }
// center-access011-starter/.../AuthorizationAdapter.java:115-127
            if (role != null && "1".equals(role.isBuiltin())) {
                return Set.of(BUILTIN_ALL);
            }
```

SEC-03：`table` 模式直接拼接。

```java
// center-datasource011-starter/.../SyncSource011.java:53-57
        String sql = endpoint.isSql() ? endpoint.data() : "SELECT * FROM " + endpoint.data();
```

SEC-11：`resolve` 只做子串判断，没有 normalize + `startsWith`。

```java
// center-storage011-starter/.../LocalObjectStorageAdapter.java:255-261
    private Path resolve(String bucket, String key) {
        if (key == null || key.contains("..")) {
            throw new IllegalStateException("invalid object key");
        }
        return Paths.get(config.basePath(), safe(bucket), key);
    }
```

建议的纵深防御写法：

```java
private Path resolve(String bucket, String key) {
    if (key == null || key.isBlank()) throw new IllegalStateException("invalid object key");
    Path root = Paths.get(config.basePath(), safe(bucket)).toAbsolutePath().normalize();
    Path target = root.resolve(key).normalize();
    if (!target.startsWith(root) || target.equals(root)) throw new IllegalStateException("invalid object key");
    return target;
}
```

<a id="ch5-t2"></a>

### 5.2 T2：迭代内修复（16 条）

| ID | 标题 | 位置 | 影响 | 修复方向 |
|---|---|---|---|---|
| SEC-15 | 权限目录接口和 Demo011 的 5 个接口没做权限校验 | `JulyPermCatalogUseCase.java:66,76`、`Demo011Controller.java:73-133` | 任何登录用户可查全部权限码；Demo 读写无控制 | UseCase 注入 `AuthorizationPort` 并 `assertHas` |
| SEC-16 | AI apiKey 更新时提交脱敏占位符 `******` 会覆盖真实密钥 | `AiModelProviderApi.java:153-155` | 真实 apiKey 丢失 | 与存储模块一致，忽略 `SECRET_MASK` |
| SEC-17 | 客户端 IP 默认取 X-Forwarded-For 最左段 | `KrtSecurityConfig011.java:178-188` | 审计、登录日志 IP 可伪造，未来按 IP 限流会失效 | production 强制配置 `trustedProxies`，未配置时用 `getRemoteAddr()` |
| SEC-18 | 接口文档路径在所有环境免登录 | `GlobalAuthFilter.java:101-103` | 生产暴露全部接口签名 | production 关闭 springdoc，并移出白名单 |
| SEC-19 | JWT：启动只校验 secret 非空但签发要求 ≥32 字节；每请求查库；8 小时无刷新 | `KrtSecurityConfig011.java:97`、`JwtAuthTokenService.java:104,148` | 短 secret 能启动但登录全失败；每请求多一次查询；token 被盗 8 小时有效 | 守卫与签发一致；用户状态约 30 秒本地缓存并主动失效；缩短有效期 + refresh token |
| SEC-20 | 消息接收接口允许匿名调用，但仓库里没有验签实现 | `MessageInboundUseCase.java:108-110,128` | 加入白名单后任何人可伪造入站消息 | `MessageInboundPort` 规定 `verify(config, headers, rawBody)`，匿名分支先验签 |
| SEC-21 | AES-GCM 细节：`enc:v1:` 开头明文不加密；无 AAD；解密失败返回 400；旧明文不迁移 | `AesGcmSecretCipher011.java:92-94,122-124,145-146` | 可存入未加密数据；密文可跨行互换；无密钥轮换 | 更长前缀并拒绝该前缀输入；表.字段.行 id 作 AAD；格式加 kid；启动扫描旧明文 |
| SEC-22 | 通用 Mapper 有 `${sql}` 直接拼接的底层方法 | `CommonMapper.java:40,54` | 当前调用方有约束，未来误用即注入 | 标记 `@Deprecated`，门禁禁止新增调用 |
| CODE-02 | 事务内夹外部 HTTP 调用；先发消息后落库 | `MessageOutboundUseCase.java:99-131,157-183` | HTTP 期间占住连接；落库失败回滚后重试会重复发送 | outbox：短事务落「发送中」→ 事务外发送 → 短事务更新结果 |
| CODE-03 | 吞异常、静默失败共 14 处 | 见 [5.4](#ch5-swallow) | 配置错误、审计丢失、删除失败均不可见 | 按清单逐项改为抛出或上报 |
| CODE-04 | application 层直接引用 infrastructure 加密类（2 处） | `JulyInboundChannelUseCase.java:32`、`JulyOutboundChannelUseCase.java:32` | 分层越界，UseCase 绑定具体实现 | domain 层抽 `ChannelConfigCryptoPort` |
| CODE-05 | 入站/出站渠道与模板两套代码几乎一样，单侧约 3,140 行 | message 模块 | 重复度约 85–95%（集合比对估计），修改需两边同步 | 泛型基类或合表加 `direction` 列 |
| CODE-06 | 自动装配靠 core 大范围扫包；`@ConditionalOnMissingBean` 用在被扫描的 `@Component` 上 | `CoreAutoConfiguration011.java:31-32`、`PermissiveAuthorizationPort.java:33-34` | 中心无开关；兜底 bean 结果依赖注册顺序 | 见 2.1 |
| CODE-07 | 上传整读进内存；下载、分页列表全量读入内存；未显式配置 multipart 大小 | `JulyObjectController.java:160`、`StorageObjectUseCase.java:85` 等 | 实测默认 1MB 上限偏小；调大后整读会 OOM；桶内对象多时分页遍历整棵目录树 | 用 `uploadStream`、流式下载、`listPage` + marker；显式配置 `spring.servlet.multipart.*` |
| CODE-08 | 关键安全路径没有测试 | 见[附录 D](#appx-d) | 安全修复无回归保护 | 按附录 D 补测试 |
| CODE-09 | 权限目录初始化失败只打日志，应用照常启动 | 7 个 `*PermCatalogSeedRunner` | 生产下角色无权限码可分配，只能从日志发现 | production 下启动失败或 health 置 down |

<a id="ch5-t3"></a>

### 5.3 T3：改进项（10 条）

| ID | 标题 | 位置 | 影响 | 修复方向 |
|---|---|---|---|---|
| SEC-23 | 本地存储：`..` 子串判断误伤合法文件名；桶名 `.` 能通过；presign 返回 `file:` URI 暴露绝对路径 | `LocalObjectStorageAdapter.java:256,443-452,432-434` | 功能误伤与信息泄露 | 用 SEC-11 的 `startsWith` 方案；桶名正则；presign 返回应用内下载链接 |
| SEC-24 | HttpClient 未显式设置重定向策略、协议版本、代理 | `HttpUtil011.java:64-66` | 依赖 JDK 默认 `NEVER`（实测不跟随，安全），但意图未写明 | 显式 `.followRedirects(NEVER).version(HTTP_1_1)` |
| SEC-25 | `X-Trace-Id` 原样写入日志和响应 | `GlobalAuthFilter.java:309-312` | 超长或特殊字符进入 MDC | 正则校验，不通过则重新生成 |
| SEC-26 | 白名单前缀匹配没有路径边界；`/klsjnh/open/` 整段免登录 | `GlobalAuthFilter.java:101-103,175` | 下游在这些前缀下的接口会意外匿名可访问 | 精确路径或以 `/` 结尾；`/klsjnh/open/` 改为配置开启 |
| SEC-27 | 账号不存在时跳过 bcrypt | `JulyUserUseCase.java:306-308` | 时间差可枚举账号 | 对假 hash 执行一次 `matches` |
| SEC-28 | 出站失败时把异常 message 原样返回 | `WebhookMessageChannel.java:87-88`、`AiModelProbe011.java:56-57` | 可能带出内网主机名和端口 | 对外只返回 `upstream call failed` |
| CODE-10 | AI 适配器重新抛异常时丢失原始 cause（4 处） | `OpenAiCompatImageAdapter.java:132-133` 等 | 排查看不到根因堆栈 | 传入原异常作 cause |
| CODE-11 | 根 pom 与 BOM 有 13 个版本属性重复；lombok 版本只在根 pom | `pom.xml:50-63`、`java17-bom011/pom.xml:16-28` | 版本漂移；外部引用 BOM 时 lombok 版本不一致 | 见 2.3 |
| CODE-12 | MinIO 删除失败返回 false，调用方不看返回值 | `MinioObjectStorageAdapter.java:192-194` | 前端显示删除成功，对象仍在 | 失败时抛异常 |
| CODE-13 | 超长类与超长方法 | 见[附录 C](#appx-c) | 方法普遍较短（最长 77 行），类最长约 500 行 | 按查询、命令、导入导出拆分超大 UseCase |

<a id="ch5-swallow"></a>

### 5.4 CODE-03 吞异常与静默失败清单

| 位置 | 行为 | 风险 |
|---|---|---|
| `MessageOutboundUseCase.java:329-331` | 渠道配置 JSON 解析失败返回 `Map.of()` | 配置写错不报错，发送时才以「url 为空」失败 |
| `MessageOutboundUseCase.java:348-350` | payload 序列化失败返回 null | 发送记录丢 payload，重发内容不完整 |
| `MessageInboundUseCase.java:301-303,320-322` | 同上两种 | 同上 |
| `AiInferenceUseCase.java:170-172` | 审计写入失败完全不处理 | 审计静默丢失 |
| `MinioObjectStorageAdapter.java:192-194` | 删除失败返回 false，调用方 `StorageObjectUseCase:166,182` 不看 | 对象实际还在 |
| `MinioObjectStorageAdapter.java:213-215` | `exists` 遇任何异常返回 false | 网络故障被当成「不存在」 |
| `MinioObjectStorageAdapter.java:88-90` | 构造时确保默认桶失败只打 warn | 首次上传才报错 |
| `LocalObjectStorageAdapter.java:141-143` | 清理空目录失败忽略 | 可接受（best effort，注释已写明） |
| `UserAuditRecorder.java:92-95` | 用户审计写入失败只打 warn | 登录审计丢失，合规风险 |
| `SchedulerHandlerJob.java:157-159` | 任务编码快照失败只打 warn | 执行审计缺字段 |
| `StorageSeed011.java:123-125` | 存储初始化失败只打 warn | 默认存储实例缺失 |
| 7 个 `*PermCatalogSeedRunner` | 见 CODE-09 | — |

<a id="ch5-tx"></a>

### 5.5 事务使用情况

- 全仓 25 个类使用 `@Transactional`；只有两处 `REQUIRES_NEW`，都是有意为之的审计写入（`UserAuditRecorder.java:77`、`SchedulerExecAuditRecorder.java:75`）。
- 全仓没有 `readOnly = true`；同一个类里混着开事务的写方法和不开事务的读方法，没有统一约定。
- 跨中心：`JulyAiDomainUseCase.store`（`:550-567`）在 AI 中心事务内写存储中心文件，回滚后留下孤儿文件（见 2.2）。

---

<a id="ch6-doc"></a>

## 6. 文档问题

### 6.1 严重（7 条）

| ID | 位置 | 问题 | 建议 |
|---|---|---|---|
| DOC-01 | `docs/deploy/docker-compose.yml` L16-19；`docs/deploy/README.md` L41 | compose 没有 `SPRING_PROFILES_ACTIVE` 和任何 `KRT_*`，README 要求挂 dev yml，部署即进入 debug（并入 T0-1） | compose 补必填环境变量；README 改挂生产配置 |
| DOC-02 | `docs/015.project-info.md` L45；`013.topic-project-structure.md` L37 | 称 storage/AI starter 在 pom 中注释掉、fat jar 无存储/AI 管理面；实际 `java17-app011/pom.xml` L44-47、L56-59 都在挂 | 改为「app 挂全部 11 个 starter」 |
| DOC-03 | `015.topic-config.md` L3、L35-40、L52-57、L76-80；`017.tech-debt-redlines.md` L112-116 | 自称配置键事实来源，只列 3 个键；漏 `krt.jwt.*`、`krt.crypto.master-key`、`krt.ci011`、`krt.storage-center.*`、`krt.ai-center.*`、`krt.springdoc.*` 6 组；生产守卫表漏 master-key 一条 | 补齐 6 组键和第 3 条守卫；§017 补 `KRT_JWT_SECRET`、`KRT_CRYPTO_MASTER_KEY` |
| DOC-04 | 根 `README.md` L93-101 | 快速开始没提 MySQL；首步 `mvn -o` 在空 `.m2` 必败；全仓无建库建用户说明；`docs/sql/*.sql` 无种子数据 | 提供 `000-init-db.sql` 与最小 admin 种子；首次构建去掉 `-o` |
| DOC-05 | `029.topic-golden-verification.md` L10、L49、L66、L96 | 金标准回归依赖被 `.gitignore:33` 忽略的 `tmp/` 脚本；L10 错误归因于 `Agent.md` | 脚本迁入 `tools/` 入库；删除错误归因 |
| DOC-06 | `docs/sql/README.md` L31 | 要求新库执行 `logic-delete-unique-fix.sql`，但建表已含 `alive_*`，第一条 `ADD COLUMN` 即报 `Duplicate column` | 2-4 步移到「仅存量库升级」小节 |
| DOC-07 | `015.project-info.md` L89 | 称部分表仍用 `UNIQUE(业务列)`，实际 32 处全部建在 `alive_*` 上 | 删除或改为「已收口」 |

### 6.2 中（15 条）

| ID | 位置 | 问题 | 建议 |
|---|---|---|---|
| DOC-08 | `infrastructure011/014.platform-center/`；`docs/README.md` L25、L61 | 约定跳过含 4 的号，台账写「014 禁用」却又链接 014 目录 | 改名为下一个可用号，更新台账和链接 |
| DOC-09 | `011.agreements.md` L131、L137；`docs/README.md` L54 | 「同号」规则可多种解读；同一目录内 011/013/015、两个 `016.*` 重号；L170 列出不存在的 021 | 明确「同一父目录不得重号」或写明例外 |
| DOC-10 | 小节编号（附录 E.3） | `## 012.`、5 处 `## 024.`、同级 `## 023.` 重号、十进制子号、阿拉伯序号、约 50 份奇数步长 | 门禁加 `doc-heading-seq` |
| DOC-11 | `docs/` 根目录 | 约定只留当天文件，仍有 09-25 两份；另有未定义的 `{日期}-{topic}.md` 形态；常驻文档 029 链接根目录日期文件 | 并入有效内容后归档；约定补充命名形态 |
| DOC-12 | 根 `README.md` L5、L65-67、L144/148/152 | 链接文字仍用旧编号（025/026/027），指向 017/016/015 | 统一新编号 |
| DOC-13 | `015.project-info.md` L18 | 写 Quartz 2.5.0，Boot 3.4.5 管理的是 2.3.2（实机日志也为 2.3.2） | 改为 2.3.2 |
| DOC-14 | 根 `README.md` L40；`013.topic-project-structure.md` L49、L62-63 | Port 清单列 4/5 个，security-api 实有 7 个；kernel/object 端口也缺项 | 补齐，README 只链接 013 |
| DOC-15 | `017.tech-debt-redlines.md` L127-130；`pom.xml`；`java17-bom011/pom.xml` | 红线称版本在父 pom 且子模块不带 `<version>`；实际在 BOM，父 pom 重复一份，13 个子 pom 带 `${lombok.version}`；BOM 注释自相矛盾 | 红线改为「BOM 是唯一版本表」（见 2.3） |
| DOC-16 | `docs/sql/` 增量脚本 | 无序号、无执行登记、不幂等、列宽漂移、未覆盖 perm 两表 | Flyway 命名 + 幂等 + 登记表（见 2.5） |
| DOC-17 | SQL 头注释 5 处 | 引用不存在或已迁移的文档、已解散的 `KrtConfig011` | 逐一改正 |
| DOC-18 | `Agent.md` L14-31；`011.agreements.md` L102、L105-118 | `docs/README.md` 从两个入口都不可达（入链 0）；目录语义表遗漏 4 项 | Agent.md 和根 README 链接知识库索引 |
| DOC-19 | `020.business-project-quickstart.md` L139、L142；`018.datasource-center/015.topic-usage.md` L20；`016.topic-datasource.md` L39；`archive011/2026-09-23.md` L16；`archive011/2026-09-17.md` L53 | 文档写有数据库 root 口令（`roo***`）、开发库口令（`dev***`）和内网地址（`192.***`），违反约定 L173 | 改为占位符或环境变量；archive 中打码 |
| DOC-20 | `020.business-project-quickstart.md` | Maven 版本、构建命令、接入方式（继承父 POM + 本机 relativePath vs import BOM）与根 README 冲突；13 处 Obsidian wikilink 不渲染 | 统一为 BOM import，改标准链接 |
| DOC-21 | 仓外链接与本机绝对路径 | 链接兄弟仓相对路径、`D:\...`、`/mnt/d/...` | 改仓库 URL 或纯文本 |
| DOC-22 | `tools/klsjnh-standards-doc.mjs` L4-13 | 文档门禁只有 `doc-api-path` 一条规则，是 DOC-08/10/11/18 反复出现的根因 | 增加 `doc-link`、`doc-heading-seq`、`doc-root-dated`、`doc-number-4` |

### 6.3 低（16 条）

| ID | 问题 | 建议 |
|---|---|---|
| DOC-23 | `archive011/` 内 17 条断链（附录 E.1） | 归档时改写相对路径 |
| DOC-24 | 裸 `§` 引用几十处 | 纳入 DOC-22 规则 |
| DOC-25 | `011.agreements.md` L158 两个列表项写在同一行 | 断行 |
| DOC-26 | `requirement011/011、013、015、016、025` 无对应 requirement013 文档，违反「不跳层」 | 补方案或写明豁免 |
| DOC-27 | `016.coding-standards.md` L333 示例仍用已解散的 `KrtConfig011` | 改为 `KrtSecurityConfig011` |
| DOC-28 | `015.project-info.md` L67 日期滞后；L20 漏 `postJsonStream` | 更新 |
| DOC-29 | `013.topic-project-structure.md` L113 把 test 域的种子列在 main；L22「tools 只查不改」与已入库的修复脚本矛盾 | 修正表述 |
| DOC-30 | 根 `README.md` L27-33 写「六层洋葱」，图中只有 5 个节点 | 写清第六层 |
| DOC-31 | `011.topic-infrastructure.md` L73 与 L90 两种安装方式矛盾 | 只保留 `npm --prefix tools install` |
| DOC-32 | `015.topic-config.md` L4、L10 对 `application.yml` 内容的描述不全；dev yml 注释指向不存在的 druid 段 | 修正 |
| DOC-33 | `docs/deploy/docker-compose.yml` L4、L22-26 卷路径相对 `docs/deploy/` 解析，与 deploy.sh 产出不一致 | 删除「项目根可直接 up」或用绝对路径变量 |
| DOC-34 | `docs/README.md` L31 称消息 config 列宽见加宽脚本，实际脚本不涉及消息表 | 改为「config 为 TEXT」 |
| DOC-35 | 09-25、09-26 没有 `-chat.md` | 流程合规提示 |
| DOC-36 | 中英文混用 | 约定「正文中文，标识与命令原样」 |
| DOC-37 | 3 份孤儿文档（附录 E.2） | 在台账中改为链接 |
| DOC-38 | `archive011/029.topic-storage-migration.md` 与 `requirement013/029...` 同名不同内容 | archive 版加 `-legacy` 或注明被取代 |

<a id="ch6-sql"></a>

### 6.4 SQL 专项（`docs/sql`）

| 文件 | 类型 | CREATE / IF NOT EXISTS | ALTER | 备注 |
|---|---|---|---|---|
| `base-entity-columns.sql` | 模板（不执行） | 1 / 0 | 0 | L56 引用路径失效（DOC-17） |
| `july_core011.sql` | 全量 | 2 / 2 | 0 | 含 `token_version`（L14） |
| `july_center_access011.sql` | 全量 | 7 / 7 | 0 | |
| `july_center_platform011.sql` | 全量 | 3 / 3 | 0 | L10 仍写 `KrtConfig011` |
| `july_center_datasource011.sql` | 全量 | 3 / 3 | 0 | password 512 |
| `july_center_ai011.sql` | 全量 | 4 / 4 | 0 | api_key 512；L57 引用路径失效 |
| `july_center_storage011.sql` | 全量 | 2 / 2 | 0 | AK/SK 512；L6 引用路径失效 |
| `july_center_message011.sql` | 全量 | 6 / 6 | 0 | config 为 TEXT |
| `july_scheduler011.sql` | 全量 | 2 / 2 | 0 | 审计表 `pk_mt` |
| `july_demo011.sql` | 全量（demo） | 1 / 1 | 0 | |
| `logic-delete-unique-fix.sql` | 增量 | — | 25 | 不幂等；列宽 30 与建表 60 不一致；未覆盖 perm 两表 |
| `secret-cipher-column-widen.sql` | 增量 | — | 3 | `MODIFY`，可重跑 |
| `token-version-column.sql` | 增量 | — | 1 | 不幂等 |

PO 与建表字段抽查 4 张表（`july_user`、`july_message_outbound_channel`、`july_storage_provider`、`july_scheduler_audit`）均一致。

<a id="ch6-onboard"></a>

### 6.5 新人上手路径（从 clone 到登录）

| 步骤 | 该读哪里 | 是否闭环 |
|---|---|---|
| JDK 17 / Maven | 根 README L95（3.9+）；020 L38-39（3.8+） | 版本口径冲突 |
| Node + 门禁依赖 | `011.topic-infrastructure.md` L73 / L90 | 根 README 没提；两种装法冲突 |
| MySQL | 015 L63「docker 容器，宿主 33306」；dev yml 写 5.7.26，README 写 MySQL 8 | **README 前置里没有 MySQL**，也没有 docker run 命令 |
| 建库 / 建用户 | 没有 | **断点** |
| 导入 DDL | `docs/sql/README.md`（根 README 未链接） | **第 2 步报错**（DOC-06） |
| 配置 | `015.topic-config.md` | 基本可用 |
| 构建 | 根 README L98 `mvn -o ...` | 空 `.m2` 离线构建失败 |
| 启动 | `./script011.sh dev013` | 没说明 Windows 需要 Git Bash / WSL，也没写执行环境（COLLAB-23） |
| 登录 | 029 L51 `loginByUserName` | 新库**没有用户**，种子只在 test 域，**断点** |

建议在根 README 增加 Getting started：`docker run mysql → 000-init-db.sql → 各建表文件 → mvn clean install → npm --prefix tools install → dev013 → 种子 admin 登录`，每一步链接到它唯一的文档位置。

---

<a id="ch7-collab"></a>

## 7. 沟通与协作机制问题

### 7.1 发现汇总（24 条）

| ID | 级别 | 位置 | 问题 | 建议 |
|---|---|---|---|---|
| COLLAB-01 | 高 | `script011.sh:86`、`:139`、`:154` | 门禁 `mvn` 和 `git status`/`git add .` 前没有 `cd "$PROJECT_ROOT"`；从子目录运行会只构建子模块、只提交部分文件；跨副本调用会落到错误仓库 | 开头 `cd "$PROJECT_ROOT"`，校验 toplevel 与 origin |
| COLLAB-02 | 高 | `8588f64`、`cd5b06e`、`f9d9fb2`、`1ca542e`（09-26） | 4 个提交绕过 `script011.sh`，其中两个分别改 481、182 个文件；`gate.yml` 也随绕过的提交入库 | main 分支保护 + Gate 必选检查（服务端兜底） |
| COLLAB-03 | 高 | `docs/archive011/2026-09-23.md:16` | 日志写有内网地址（`192.***`）、库名、账号和口令尾号；约定只约束 chat 文件 | 评估轮换；脱敏规则扩展到 `docs/**`；CI 加 gitleaks |
| COLLAB-04 | 高 | `application-development.yml:8-45`、`.env.example:6-31` | 入库的弱默认凭据（DB `dev***`、Druid `kls***`、JWT `dev***`、master-key `dev***`、MinIO `123***`，endpoint 指向 `192.***`）；Druid 白名单含 `192.***/24`；017 前后条款自相矛盾 | 删除默认值，缺失即启动失败；密钥只从 `.env` 注入 |
| COLLAB-05 | 高 | `script011.sh:154` + `.gitignore` | `git add .` 一次提交全部，不列清单、不扫密钥；`.gitignore` 未覆盖 `*.pem`、`*.key`、`.env.*` 等 | 提交前列 `git status --porcelain`；`gitleaks protect --staged`；补 ignore |
| COLLAB-06 | 高 | `script011.sh:159-161` | push 前不 fetch/rebase；被拒时留下已提交、未推送、`.vf` 已自增的半完成状态 | 先 `fetch` + `rebase`，确认快进再提交；版本改用 tag |
| COLLAB-07 | 中 | `011.agreements.md:21-25`、`script011.sh:75-91` | 约定称脚本自动校验「接口与逻辑自测」，实际没有 | 门禁可选接入冒烟脚本，或改约定措辞 |
| COLLAB-08 | 中 | `script011.sh:86` | 熔断有效，但 `-q` 隐藏失败细节，node 失败无提示，`-o` 易诱发绕过；多个模块 0 测试 | 失败时打印 surefire 摘要；提示 `dependency:go-offline`；覆盖率基线 |
| COLLAB-09 | 中 | `script011.sh:155`、`.vf`、`pom.xml:9` | 提交信息写死 `ver $v ...`，历史无语义；Maven 恒为 1.0.0，无 tag | 提交脚本必须带 message 并附 `Refs:`；发版 `versions:set` + tag（见 2.3） |
| COLLAB-10 | 中 | `script011.sh:179`、`:231`、`:239` | `PROFILE="development"` 写死；启动时向终端打印 Druid 账号口令（`kls***`） | `PROFILE="${PROFILE:-development}"`；删除凭据打印 |
| COLLAB-11 | 中 | `script011.sh:198-205`、`:283-289` | `lsof -ti tcp:11160` 没有 `-sTCP:LISTEN`，`stop` 可能误杀客户端进程 | 只取 LISTEN，并校验命令行含 `$APP_JAR` |
| COLLAB-12 | 中 | `script011.sh:24` | 外部 `JAVA_HOME` 原样使用，不校验版本 | 断言主版本为 17 |
| COLLAB-13 | 中 | `.github/workflows/gate.yml:1-35` | CI 与本地门禁口径不同（见 7.2）；无 permissions/concurrency/timeout；文档从未提及 CI | 抽 `tools/gate.sh` 共用；文档写明 CI |
| COLLAB-14 | 中 | `.github/` | 没有 PR 模板、CODEOWNERS、CONTRIBUTING、CHANGELOG、SECURITY.md | 补齐，CHANGELOG 与 tag 绑定 |
| COLLAB-15 | 中 | `docs/` 根、`docs/archive011/` | 根目录残留 09-25 文件；chat 只覆盖 15 天中的 4 天、日志覆盖 8 天；`{日期}-{topic}.md` 形态未定义 | 归档与 chat 存在性纳入门禁；约定定义该形态 |
| COLLAB-16 | 中 | 最近 11 个提交 | 提交与文档对不上（改提交脚本无文档、252 文件提交无文档、日期错位） | 提交信息必须含 `Refs:` |
| COLLAB-17 | 中 | `Agent.md:59`、`011.agreements.md:30` | 把 `pull`、`checkout`、`switch` 归为「只读」 | 改为「本地写，需汇报」 |
| COLLAB-18 | 中 | `tools/fix-import-order.mjs:2`、`tools/check-klsjnh-standards.mjs:4`、`:32` | 约定禁止修复脚本和 git hooks，仓库却有修复脚本、注释写「Used by git hooks」 | 删除或归档，修正注释 |
| COLLAB-19 | 低 | `017:6`、`:135`，`016.coding-standards.md:421` | 章节引用写法与编号违反约定 | 统一编号，自查纳入门禁 |
| COLLAB-20 | 低 | 缺少 `.gitattributes` | 12 个 CRLF、1 个混合换行文件；Windows 与 WSL 两套 git 换行设置不同 | 添加 `.gitattributes` 并归一化 |
| COLLAB-21 | 低 | `script011.sh` 索引权限 `100644`；`/mnt/d` 为 9p | 原生 Linux 克隆会 Permission denied；9p 上 `git status` 7.2 秒 | `update-index --chmod=+x`；在 WSL ext4 上建执行副本 |
| COLLAB-22 | 高（本次案例） | dev016 `/klsjnh/java202608/klsjnh-java17-framework011` | 旧副本 HEAD `3fa5d77`（ver 0.0.12），与正式仓库无共同历史但 origin 相同，`ahead 13, behind 65`，有未提交修改 | 归档改名并删除 remote；脚本校验根提交 `924f5fa` |
| COLLAB-23 | 中（本次案例） | `011.agreements.md:39-54`、`015.project-info.md:49-63`、`016:445`、`script011.sh:79` | 执行环境（发行版、路径、JDK/Maven/Node、调用方式）没有写进文档，文档反而给出 Windows 路径 | 在 015 加执行环境卡 |
| COLLAB-24 | 低 | 作者统计 | 68 个提交作者全部为同一人（`xia***@163.com`），无 AI 参与标识 | AI 参与的提交加 `Assisted-by:` trailer |

### 7.2 `gate.yml` 与 `script011.sh gate` 对照

| 维度 | `script011.sh gate`（`:75-91`） | `gate.yml` | 是否等价 |
|---|---|---|---|
| 触发 | 手动；提交前执行，任何分支 | 所有 `pull_request`；`push` 仅 `[main, master]` | 否 |
| 运行环境 | dev016：Ubuntu 26.04 / WSL2，`/mnt/d`（9p） | `ubuntu-latest` | 否 |
| JDK | `$JAVA_HOME`，默认 `/usr/local/java17`（Oracle 17.0.12），不校验 | Temurin 17 | 近似 |
| Node | PATH 上的版本（v22.22.0），`engines >=18` | `"20"` | 否 |
| 工具依赖 | 只检查 `tools/node_modules/tree-sitter` 是否存在 | `npm --prefix tools ci` | 否 |
| 规范检查 | `node tools/check-klsjnh-standards.mjs "$PROJECT_ROOT"` | `node tools/check-klsjnh-standards.mjs` | 等价 |
| Maven | `mvn -o clean install -q`（离线、静默、依赖当前目录） | `mvn -B clean test`（在线） | 否 |
| 跑测试 | 是 | 是 | 等价 |
| 打包、repackage、装入 `.m2` | 是 | 否 | 否 |
| 超时、并发、权限 | 无 | 无（`permissions` 为默认） | — |
| 报告 | `-q` 隐藏输出 | 不上传报告 | — |
| 接口自测 | 无 | 无 | 两者都缺 |
| 能否绕过 | 能（原生 git 提交） | 是否为必选检查未能核实 | — |

### 7.3 提交粒度统计（最近 60 个提交）

| 文件数区间 | 提交数 | 增删行数区间 | 提交数 |
|---|---|---|---|
| ≤5 | 5 | ≤100 | 5 |
| 6–20 | 15 | 101–500 | 13 |
| 21–50 | 16 | 501–2000 | 19 |
| 51–200 | 20 | 2001–10000 | 20 |
| >200 | **4** | >10000 | **3** |

- 中位数 30 个文件、1445 行；平均 69.9 个文件、2560 行；最大 647 个文件（`dd1210e`）、19240 行（`27c67b6`，删除 19201 行）。
- 超过 200 个文件的提交（全历史 4 个）：`8588f64`（481 个文件，+10026/−3718，**未走提交脚本**）、`dd1210e`（647 个）、`66f356e`（252 个，无配套文档）、`395bc21`（244 个，其中 171 个为重命名）。
- 提交信息：63/68 为 `ver X.Y.Z ...`；其余为 `initialize project ...` 和 COLLAB-02 的 4 个。没有合并提交；15 天 68 个提交，单日最多 9 个（09-19）。
- 分支：除 `main` 外 4 个分支（`cursor/rename-base-master-sub-repo`、`docs/sync-032-027-audit`、`docs/full-audit-fix`、`fix/alive-unique-ddl`）均已快进合并入 `origin/main`，属陈旧分支；因为都是快进合并，没有 PR 评审留痕。没有 tag。

### 7.4 推荐流程（需要用户授权后执行，本次审计未执行）

一次性修复：

```bash
# 1) 在 WSL ext4 上建立唯一执行副本，并废弃旧副本
wsl -d dev016 -e bash -lc 'mv /klsjnh/java202608/klsjnh-java17-framework011 /klsjnh/java202608/OBSOLETE-framework011-v0.0.12 && git -C /klsjnh/java202608/OBSOLETE-framework011-v0.0.12 remote remove origin'
wsl -d dev016 -e bash -lc 'mkdir -p ~/src && git clone git@github.com:klsjnh/klsjnh-java17-framework011.git ~/src/klsjnh-java17-framework011'
# 2) 统一换行和可执行位
printf '* text=auto eol=lf\n*.ps1 text eol=crlf\n*.bat text eol=crlf\n' > .gitattributes
git add --renormalize . && git update-index --chmod=+x script011.sh
# 3) 清理已合并的远端分支
git push origin --delete cursor/rename-base-master-sub-repo docs/full-audit-fix docs/sync-032-027-audit fix/alive-unique-ddl
```

`script011.sh` 关键补丁要点：

```bash
cd "$PROJECT_ROOT"
[ "$(git rev-list --max-parents=0 HEAD)" = "924f5fa<完整hash>" ] || { echo "wrong repo copy"; exit 1; }
java -version 2>&1 | grep -q '"17\.' || { echo "need JDK 17"; exit 1; }
MSG="${2:?usage: ./script011.sh commit \"feat(scope): summary\"}"
git fetch origin && git rebase "origin/$(git branch --show-current)"
git status --porcelain                            # 打印清单
gitleaks protect --staged --no-banner || exit 1   # 在 git add 之后执行
git commit -m "$MSG" -m "ver $v" -m "Refs: docs/$(date +%F).md"
git push -u origin HEAD
# runtime：lsof -t -iTCP:$APP_PORT -sTCP:LISTEN；PROFILE="${PROFILE:-development}"；删除第 239 行的凭据打印
```

日常流程：

```bash
wsl -d dev016 -e bash -lc 'cd ~/src/klsjnh-java17-framework011 && git fetch && git switch -c feat/<topic> origin/main'
# Step1/3：写 docs/$(date +%F)-chat.md（全 docs 脱敏）和 docs/$(date +%F).md；归档昨日文件
wsl -d dev016 -e bash -lc 'cd ~/src/klsjnh-java17-framework011 && setsid nohup ./script011.sh gate > /tmp/gate.log 2>&1 &'
# 读取 /tmp/gate.log 确认 "Gate PASSED"；再跑接口冒烟 tools/demo011-dual-mode-smoke.sh
# 用户明确说「提交」后：
wsl -d dev016 -e bash -lc 'cd ~/src/klsjnh-java17-framework011 && ./script011.sh commit "feat(iam): <summary>"'
# 在 GitHub 开 PR（PR 模板 + CODEOWNERS 评审），Gate 为必选检查，squash 合并
# 发版：mvn versions:set -DnewVersion=1.0.x && git tag v1.0.x && git push --tags，同时更新 CHANGELOG
```

CI 配套：抽出 `tools/gate.sh` 本地与 CI 共用；CI 改为 `mvn -B clean install`，加 `timeout-minutes: 20`、`permissions: contents: read`、`concurrency`、上传 surefire 报告、gitleaks 扫描；本地与 CI 的 Node 版本统一为 22。

---

<a id="ch8-corrections"></a>

## 8. 对前一版审计的更正

实机验证推翻或修正了前一版审计中的以下判断。本报告全文按实测结果书写。

| # | 前一版判断 | 实测结果 | 新定级 |
|---|---|---|---|
| 1 | 本地存储 objectName 可用绝对路径（例如 `/tmp/x`）逃逸出存储根目录，定为 T0 | **未复现**。`Paths.get(base, bucket, "/tmp/x")` 把前导 `/` 当作分隔符，文件仍落在桶目录内；含 `..` 的 key（4 种写法）和非法桶名均被拒绝 | 降为 **T1**，归入 SEC-11。真实风险是 `basePath` 可配置为任意目录，以及 `resolve` 缺少 normalize + `startsWith` 的纵深防御 |
| 2 | Webhook 会跟随重定向，可借 302 绕过地址检查 | **不跟随**。JDK `HttpClient` 默认 `NEVER`，302 时发送结果为 `success:false, error:"webhook returned HTTP 302"`，重定向目标未被请求 | 不单列风险；保留 SEC-24（T3），建议显式写明 `followRedirects(NEVER)` |
| 3 | 上传没有大小限制，可上传超大文件打满内存 | **错误**。Spring 默认单文件 1MB，20MB 上传返回 400，2MB 也被拒 | 不作为安全风险。反过来的问题是：1MB 对存储中心可能过小；超限返回 400 而非 413（CODE-01）；一旦调大限制，整读进内存会导致 OOM（CODE-07） |

同时依据实测上调或确认的判断：

- **Webhook 回环可达**：实测确认成立，SEC-10 由 T1 并入 T0-3。
- **debug 全放行 / production 鉴权生效**：实测确认成立，SEC-01、SEC-06 合并为 T0-1。
- **生产不拒绝默认 secret**：实测确认成立，SEC-14 由 T1 上调为 T0-2；实机验证报告认为这是「部署配置卫生问题」，本报告不采纳该定性（理由见 T0-2）。
- **统计口径**：代码审计正文开头写「共 44 条：T0 2、T1 15、T2 17、T3 10」，但其发现索引实际列出 41 个 ID（T0 2、T1 13、T2 16、T3 10）。本报告按索引中实际存在的 ID 重新统计为 41 条。

---

<a id="ch9-roadmap"></a>

## 9. 整改路线图

### 第一阶段：止血（建议 1 周内）

| 事项 | 对应发现 |
|---|---|
| 非 production 状态必须显式允许，否则启动失败；部署默认不再进入 debug；compose 补必填环境变量；补 `application-production.yml` | SEC-01、SEC-06、DOC-01、DOC-03、COLLAB-10 |
| 删除仓库内密钥默认值；生产拒绝已知默认密钥；已用过默认值的环境轮换密钥 | SEC-14、COLLAB-04、SEC-19 |
| 统一出站地址检查与响应大小上限，先覆盖 ASR、Webhook、AI | SEC-02、SEC-10、SEC-28 |
| 统一本地与 CI 门禁口径（`tools/gate.sh`），main 分支保护 + Gate 必选 | COLLAB-13、COLLAB-02、COLLAB-08 |
| 执行环境写入文档；废弃 dev016 旧副本 | COLLAB-23、COLLAB-22 |
| `script011.sh` 加根目录切换、仓库身份（toplevel、origin、根提交）校验、JDK 17 校验、push 前 fetch/rebase、提交清单与密钥扫描 | COLLAB-01、COLLAB-06、COLLAB-12、COLLAB-05 |
| 文档中的明文口令和内网 IP 打码 | DOC-19、COLLAB-03 |

### 第二阶段：理清边界（建议 1 个迭代）

| 事项 | 对应发现 |
|---|---|
| 每个 starter 自带 AutoConfiguration、MapperScan 和 `krt.center.xxx.enabled` 开关；core 不扫描中心；兜底 bean 改为 `@AutoConfiguration(after=...)` | CODE-06、2.1 |
| 拆出 `center-xxx-api`，中心之间只依赖接口和 DTO；跨中心写操作放到提交后；消息中心加密改为端口 | CODE-04、CODE-02、2.2 |
| 每个中心自带 Flyway 迁移；增量脚本幂等化；修正 SQL README | DOC-06、DOC-16、DOC-07、DOC-17、2.5 |
| 统一出站 HTTP 端口扩展到 MinIO、数据源；JDBC URL / 驱动白名单 | SEC-09、SEC-24、2.6 |
| 鉴权链 T1：执行前权限声明检查、编码路径处理、内置角色授予限制、登录限流 | SEC-05、SEC-04、SEC-07、SEC-12 |
| 数据与存储边界：SQL 守卫加固、同步源端校验、`basePath` 白名单 + normalize/startsWith | SEC-08、SEC-03、SEC-11、SEC-23 |
| 其余 T1：渠道配置脱敏、异常映射 | SEC-13、CODE-01 |
| 新人上手路径打通 | DOC-04、DOC-05、DOC-02、6.5 |

### 第三阶段：产品化（建议 1–2 个迭代）

| 事项 | 对应发现 |
|---|---|
| SemVer 发版：BOM 唯一版本表、tag、CHANGELOG、不兼容变更约定；提交信息带语义和 `Refs:` | CODE-11、DOC-15、COLLAB-09、COLLAB-14、COLLAB-16、2.3 |
| 装配测试（`ApplicationContextRunner`）、契约测试、ArchUnit 分层与 `assertHas` 覆盖断言、附录 D 安全路径测试 | CODE-08、COLLAB-08、2.7 |
| 文档与代码一致性门禁：`doc-link`、`doc-heading-seq`、`doc-root-dated`、`doc-number-4`，配置键比对 | DOC-22、DOC-08、DOC-10、DOC-11、DOC-18、DOC-24 |
| 其余 T2/T3 与低级别文档、协作项 | 第 5–7 节其余 ID |

---

## 10. 附录

<a id="appx-a"></a>

### 附录 A：未做权限校验的接口清单

框架约定：权限统一在 UseCase 层用 `authorizationPort.assertHas` 校验。代码审计扫描 27 个 Controller、195 个接口方法，除下表外都能在对应 UseCase 中找到 `assertHas`。

| # | HTTP 与路径 | Controller:行 | 现状 | 结论 |
|---|---|---|---|---|
| 1 | POST `/klsjnh/demo11/v1/insert` | `Demo011Controller.java:73` | 无 | 缺失（写） |
| 2 | POST `/klsjnh/demo11/v1/update` | `:87` | 无 | 缺失（写） |
| 3 | POST `/klsjnh/demo11/v1/logicDelete` | `:103` | 无 | 缺失（写） |
| 4 | GET `/klsjnh/demo11/v1/getById` | `:119` | 无 | 缺失（读） |
| 5 | POST `/klsjnh/demo11/v1/selectListByPage` | `:133` | 无 | 缺失（读；action 以 select 开头，被闸门豁免） |
| 6 | GET `/klsjnh/iam/julyPermCatalog/v1/selectObjects` | `JulyPermCatalogController.java:63` | 无（`JulyPermCatalogUseCase:66`） | 缺失（敏感读） |
| 7 | GET `/klsjnh/iam/julyPermCatalog/v1/selectActions` | `:78` | 无（`:76`） | 缺失（敏感读） |
| 8 | GET `/klsjnh/iam/julyMenu/v1/getUserMenuTree` | `JulyMenuController.java:223` | 只要求已登录 | 设计如此（只返回自己的菜单） |
| 9 | POST `/klsjnh/iam/julyUser/v1/logout` | `JulyUserController.java:288` | 无；闸门豁免 | 设计如此 |
| 10 | POST `/klsjnh/messagecenter/julyInboundMessage/v1/receive` | `JulyInboundMessageController.java:97` | 只有带身份时才校验 | 条件校验；加入白名单即匿名，见 SEC-20 |
| 11 | POST `/klsjnh/iam/julyUser/v1/login` | `JulyUserController.java:232` | JWT 白名单 | 认证入口；缺限流，见 SEC-12 |
| 12 | POST `/klsjnh/iam/julyUser/v1/loginByUserName` | `:251` | JWT 白名单 | 认证入口；非 production 下可免密，见 T0-1 |

补充：存储对象全部操作、`julySql/selectByPage`、同步规则 `run` 只校验全局对象级权限码，不区分数据范围；所有权限校验在 debug 和 development 下都不生效（`AuthorizationAdapter.java:162`），没有引入 access 中心时也不生效（`PermissiveAuthorizationPort.java:57`）。

<a id="appx-b"></a>

### 附录 B：分层越界 import 清单

检查范围：application 与 domain 包中引用 `com.klsjnh.infrastructure.*`、`com.klsjnh.web.*`、`org.springframework.web.*`、`jakarta.servlet.*`、`MultipartFile`、`com.baomidou.*` 的地方，以及 domain 引用 application 的地方。

| 文件:行 | import |
|---|---|
| `center-message011-starter/src/main/java/com/klsjnh/application/messagecenter/inbound/channel/JulyInboundChannelUseCase.java:32` | `import com.klsjnh.infrastructure.messagecenter.crypto.ChannelConfigCipher011;` |
| `center-message011-starter/src/main/java/com/klsjnh/application/messagecenter/outbound/channel/JulyOutboundChannelUseCase.java:32` | `import com.klsjnh.infrastructure.messagecenter.crypto.ChannelConfigCipher011;` |

除这 2 处外没有其他越界。另：存储对象接口直接把 domain 的 `ObjectStat`、`ObjectListing` 作为响应体返回（`JulyObjectController:28,110,273`），方向上不越界，但领域对象成了 API 契约，建议改进。

<a id="appx-c"></a>

### 附录 C：超长类与方法 Top 10

**类**（main 源码，非空行数）

| # | 非空行数 | 文件 |
|---|---|---|
| 1 | 506 | `center-ai011-starter/.../application/aicenter/prompt/JulyAiDomainUseCase.java` |
| 2 | 505 | `center-platform011-starter/.../application/system011/dictionary/JulyDictionaryUseCase.java` |
| 3 | 497 | `center-ai011-starter/.../application/aicenter/modelprovider/AiModelProviderUseCase.java` |
| 4 | 490 | `center-storage011-starter/.../object/adapter/LocalObjectStorageAdapter.java` |
| 5 | 451 | `center-storage011-starter/.../object/adapter/MinioObjectStorageAdapter.java` |
| 6 | 414 | `java17-core011/.../application/iam/user/JulyUserUseCase.java` |
| 7 | 399 | `center-datasource011-starter/.../domain/datasource/sync/JulySyncRule.java` |
| 8 | 398 | `center-storage011-starter/.../application/storagecenter/storage/JulyStorageProviderUseCase.java` |
| 9 | 388 | `center-ai011-starter/.../web/aicenter/controller/AiDomainController.java` |
| 10 | 380 | `center-datasource011-starter/.../application/datasource/management/JulyDatasourceUseCase.java` |

**方法**（按缩进启发式统计，仅供参考）

| # | 行数 | 位置 | 方法 |
|---|---|---|---|
| 1 | 77 | `JulyDictionaryImportSupport.java:74` | `importWorkbook` |
| 2 | 75 | `DynamicDataSourceRegistryImpl.java:99` | `reloadAll` |
| 3 | 66 | `XlsxWorkbookCodec011.java:115` | `read` |
| 4 | 60 | `OpenAiCompatTtsAdapter.java:81` | `synthesize` |
| 5 | 54 | `OpenAiCompatImageAdapter.java:82` | `generate` |
| 6 | 53 | `MessageInboundUseCase.java:107` | `receive` |
| 7 | 48 | `SenseNovaImageAdapter.java:91` | `generate` |
| 8 | 46 | `SyncSink011.java:60` | `writePage` |
| 9 | 46 | `OpenAiCompatAsrAdapter.java:81` | `recognize` |
| 10 | 45 | `JulyPermCatalogSeed011.java:84` | `seed` |

<a id="appx-d"></a>

### 附录 D：测试清单与安全路径测试缺口

实机统计为 16 个测试类、70 个用例（见 [4.3](#ch4-test)）。

| 模块 | 测试类 | 覆盖对象 |
|---|---|---|
| security-api | `PermissionWhitelistGate011Test` | 闸门判定函数 |
| security-autoconfigure | `KrtSecurityConfigValidateTest011` | 启动守卫 |
| security-autoconfigure | `AesGcmSecretCipher011Test` | AES-GCM 加解密 |
| security-autoconfigure | `JwtAuthTokenServiceTest` | JWT 签发、校验、`tv` |
| security-autoconfigure | `GlobalAuthFilterWhitelistTest` | 只测 `isJwtWhitelisted`（4 个用例） |
| core | `JulyUserUseCaseLoginTest` | 登录 |
| core | `BaseMasterSubRepositoryTest` | 主子表仓储 |
| core | `SortSupportTest` | 排序辅助 |
| core | `GlobalExceptionHandlerValidationTest` | 参数校验异常映射 |
| access | `AuthorizationAdapterTest` | 权限判定 |
| message | `MessageInboundUseCaseReceiveTest` | 消息接收 |
| message | `ChannelConfigCipher011Test` | 渠道配置加密与脱敏 |
| message | `WebhookMessageChannelTest` | Webhook 发送 |
| platform | `JulyConfigUseCaseCrudTest` | 配置增删改查 |
| storage | `JulyStorageProviderSecretMaskTest` | 存储凭据脱敏 |
| scheduler | `JulySchedulerAuditUseCaseTest` | 调度审计查询 |
| scheduler | `SchedulerExecAuditRecorderTest` | 执行审计写入 |
| app（test 目录） | `AuthPermissionDemoSeed011`、`AuthPermissionDemoSeedRunner` | 演示种子数据，不是测试 |

> 汇总时抽查：HEAD 中 security-autoconfigure 有 4 个测试源文件（上表 4 个类），全仓合计 17 个测试类；实机 surefire 报告中该模块只有 3 个类、全仓 16 个。少掉的 1 个类是哪一个、为什么没有被执行，尚未核对（列入附录 F）。

**关键安全路径测试缺口**

| 安全项 | 有没有测试 |
|---|---|
| `LocalObjectStorageAdapter.resolve` / `safe`，`StorageObjectUseCase` 的权限 | 没有 |
| `SqlGuard011`、`SqlQueryUseCase`、`SyncSource011` | 没有 |
| `HttpUtil011` 出站地址校验 | 没有（本来就没有校验逻辑） |
| `GlobalAuthFilter.doFilterInternal`：路径规范化、编码路径、debug 放行、执行后改写 403 | 没有 |
| `JulyUserRoleAssignUseCase`（内置角色越权） | 没有 |
| `JulyDatasource` 的 JDBC URL 校验 | 没有 |
| `GlobalExceptionHandler` 除参数校验外的分支 | 没有 |
| 各 UseCase 的 `assertHas` 覆盖率（ArchUnit） | 没有 |
| 各 starter 的装配结果（`ApplicationContextRunner`） | 没有 |

<a id="appx-e"></a>

### 附录 E：文档断链、孤儿、编号违规、一致性抽查

**E.1 断链（相对链接 17 条，全部在 `archive011/`）**

| 源文件:行 | 目标 |
|---|---|
| `archive011/2026-09-22-standards-audit.md:4` | `016.coding-standards.md`、`017.tech-debt-redlines.md`、`infrastructure011/011.persistence-spec/017…`、`/018…`、`/019…`（5 条） |
| `archive011/2026-09-22.md:7 / 9 / 12` | `infrastructure011/011.persistence-spec/017…` / `018…` / `019…` |
| `archive011/2026-09-23.md:9` | `infrastructure011/011.persistence-spec/020…` |
| `archive011/2026-09-24-chat.md:7` | `infrastructure011/013.access-center/016…` |
| `archive011/2026-09-24.md:17 / 33 / 34 / 35 / 36 / 36` | `013.access-center/016…`、`013.access-center/011…`、`033.topic-platform-security.md`、`sql/README.md`、`020.topic-deploy-docker.md`、`deploy/` |
| `archive011/015.ai-center/016.topic-prompt-templates.md:4` | `../018.datasource-center/016.topic-type-contract.md` |

根 `README.md` 与 `docs/README.md`：0 条断链。纯文本路径失效见 DOC-17。

**E.2 孤儿文档**

- 没有任何 md 链接：`docs/README.md`（索引本身）、`requirement011/013.topic-july-organization.md`、`requirement011/016.topic-july-role.md`、`requirement013/031.topic-datasource-center.md`；归档日志 11 份（可接受）。
- 只能经子文档到达：requirement011 全部 12 份、requirement013 全部 9 份、各中心 013/015/016 子文档、根目录 4 份日期文件。

**E.3 编号违规**

- 含 4 的目录：`infrastructure011/014.platform-center/`。
- 同一父目录重号：`infrastructure011/` 下 011、013、015；`018.datasource-center/` 下两个 `016.*`；`docs/020.*` 两份。
- 根目录非法文件名：`2026-09-25-restructure-report.md`、`2026-09-26-api-consistency-fix.md`、`2026-09-26-audit-reconciliation.md`（本报告同属此形态，见文首说明）。
- 禁用小节号：`022.topic-july-scheduler.md` L14 `## 012.`；`## 024.` 5 处（`015.storage-center/016` L56、`016.message-center/013` L105、`016.message-center/016` L91、`017.ai-center/013` L65、`017.ai-center/016` L56）。
- 同级重号：`029.topic-golden-verification.md` L94 与 L109 都是 `## 023.`。
- 十进制子号：`requirement013/022、026、027、028、029`，`018.datasource-center/011、013、016.*`，`011.persistence-spec/015、020`。
- 阿拉伯序号：`016.coding-standards.md`、`013.topic-project-structure.md` 全文。
- 标题非「号 — 名称」格式：`011.agreements.md` L1、`020.topic-deploy-docker.md` L1、requirement011 全部 12 份、requirement013 全部 9 份。

**E.4 一致性抽查（25 条，10 条不一致）**

| # | 文档位置 | 代码/配置位置 | 结论 |
|---|---|---|---|
| 1 | README L35、L141；013 结构 L3：15 模块 | `pom.xml` L22-38 | 一致 |
| 2 | 015 L26-42 模块树 | `pom.xml` modules | 一致 |
| 3 | 015 L45、013 结构 L37：storage/AI 注释掉 | app pom L44-47、L56-59 在挂 | **不一致** |
| 4 | 端口 11160 | `application.yml` L2 | 一致 |
| 5 | README 技术栈版本 | BOM L16-28、父 pom L50-63 | 一致 |
| 6 | 015 L18 Quartz 2.5.0 | Boot 3.4.5 为 2.3.2 | **不一致** |
| 7 | README L40 / 013 结构 L49 Port 清单 | security-api 实有 7 个 | **不一致（缺项）** |
| 8 | `WebPaths011.MESSAGE_INBOUND_RECEIVE` | `WebPaths011` L36-50 | 一致 |
| 9 | 各文档 `/klsjnh/*/v1` 路径 | 29 个 `@RequestMapping` 全部匹配 | 一致 |
| 10 | 015-config 键表 3 个键 | 6 个 `@ConfigurationProperties` 前缀 | **不一致（缺 6 组）** |
| 11 | 守卫表 2 行 | `KrtSecurityConfig011` L92-104 有 3 条 | **不一致** |
| 12 | 016 L333、platform SQL L10：`KrtConfig011` | 已拆为三个绑定类 | **不一致** |
| 13 | `july_user.token_version` | `july_core011.sql` L14；`JulyUserPo` L62 | 一致 |
| 14 | `EntityId.generate()` + `ASSIGN_UUID` 兜底 | `BasePo` L39 等 | 一致 |
| 15 | 内置 s3011 | `StorageProviderCodes011` L33 等 | 一致 |
| 16 | storage AK/SK 列宽 512 | `JulyStorageProviderPo` | 一致 |
| 17 | 调度审计 `pk_mt` | `JulySchedulerAuditPo` L40 | 一致 |
| 18 | `scanBasePackages` 只含 demo/scheduler | `Framework011Application` L31-32 | 一致 |
| 19 | Seed 仅在 test 域 | `java17-app011/src/test/...` | 一致（013 结构 L113 表述有误，见 DOC-29） |
| 20 | knife4j 6 个分组 | `SpringDocConfig011` L96-173（实机亦为 6 组） | 一致 |
| 21 | 015 L89：CREATE 仍用 UNIQUE(业务列) | 32 处全部在 `alive_*` | **不一致** |
| 22 | 017 L127-130：版本在父 pom，子模块不带 version | 版本在 BOM；13 个子 pom 带 `${lombok.version}` | **不一致** |
| 23 | docs/README L31：消息 config 列宽见加宽脚本 | 脚本不涉及消息表 | **不一致（指向错误）** |
| 24 | 015 L20 与 L82 HttpUtil011 方法清单 | 代码 L228 有 `postJsonStream` | 部分一致 |
| 25 | 013 结构 L62-63 kernel/object 端口 | 实有 10 个，文档列 7 个 | 部分一致 |

<a id="appx-f"></a>

### 附录 F：需要进一步实测确认的项

| 项 | 需要确认的内容 | 关联 |
|---|---|---|
| ASR `audioUrl` SSRF | 配置可用 AI 厂商后，确认服务端会请求内网地址、超大响应是否导致 OOM | SEC-02（T0-3） |
| 外部卷配置 | `/klsjnh/volume/config/` 下是否另外设置了 `spring.profiles.active` 或 `krt.status` | SEC-01（T0-1） |
| 以 production profile 启动 | 准备生产配置后，确认守卫、默认密钥拒绝逻辑修复后的行为 | SEC-06、SEC-14 |
| 编码路径 | `%2e%2e`、`%6b` 在 Tomcat 10.1 + Spring 6.2 上的实际路由 | SEC-04 |
| SQL 守卫绕过 | 括号闭合后的 `INTO OUTFILE`、`load_file` 能否读到文件 | SEC-08 |
| JDBC 参数 | `autoDeserialize`、`allowLoadLocalInfile` 能否利用 | SEC-09 |
| 停用角色 | `roleRepository.findById` 是否过滤停用角色 | SEC-07 |
| 装配顺序 | 调整 jar 顺序后是否出现两个 `AuthorizationPort` 或误用 Permissive 实现 | CODE-06 |
| 接口数差值 | 源码 195 个接口方法与 api-docs 190 个路径的 5 个差值具体是哪些 | 4.5 |
| 测试类数差值 | HEAD 有 17 个测试类，surefire 报告只有 16 个（security-autoconfigure 少 1 个），确认是哪个类未执行及原因 | 附录 D |
| GitHub 设置 | main 分支保护、Gate 是否为必选检查、Actions 运行记录 | COLLAB-02、COLLAB-13 |
| 历史口令 | 日志中出现过尾号的口令是否已轮换 | COLLAB-03、DOC-19 |
