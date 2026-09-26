# 2026-09-25 — 模块体系重构报告（六模块单体 → BOM + 胖核心 + 瘦 starter）

> 触发：外部架构审计指出"框架定位与平台应用混作、infrastructure 依赖爆炸、装配靠宿主全包扫描"；结合本日两轮全项目审核结论立项。
> 性质：**结构性重构，行为等价**——Java 包名、类名、URL 契约、DDL、Swagger 分组、门禁规则全部不变；本报告记录最终形态、决策依据与验证证据。
> 配套：任务流水见 [当日日志](2026-09-25.md)；消费方操作手册见 [020 quickstart](020.business-project-quickstart.md)。

---

## 一、结果一句话

框架从「六个按 DDD 技术层切的模块拼成单体应用」重构为「**BOM + 胖核心 + 瘦 starter（14 模块）**」：特性代码全部收进核心，技术适配各归各的 starter，**消费方零框架扫描、按需选配、缺件熔断**。全量验证（gate / 打包含测试 / 启动冒烟 / 权限矩阵 / 摘件负向验证）双向往复全绿。

## 二、目标结构（现状）

```
web ──► application ──► domain ◄── infrastructure     ← 包结构不变（六层洋葱）
              │                        │
              └────────► common ◄──────┘

依赖方向（Maven 模块级，严格单向）：
  starter们 ──► java17-core011 ──► java17-security-api011（纯 Java）
  消费方 ──► BOM 管版本 + 挑 starter
```

| # | 模块 | 角色 | 要点 |
|---|------|------|------|
| 1 | java17-bom011 | BOM | 内部 13 模块 + 三方版本基线；**独立无 parent**（root import 子 BOM 会成 POM 环） |
| 2 | java17-security-api011 | 安全 API | 鉴权 4 端口 + Operator011 + FrameworkStatus011；纯 Java 零框架 |
| 3 | java17-core011 | **胖核心** | common + domain + application + web（iam / system011 / datasource 管理面）；持久化基座家族 + 12 个管理面 Controller；`CoreAutoConfiguration011` 自注册；三中心端口契约留 core |
| 4 | java17-security-autoconfigure011 | 安全装配 | JWT/bcrypt/授权与运行态适配器 + GlobalAuthFilter + AuditLogAspect + 审计记录器 + `KrtSecurityConfig011` |
| 5 | java17-security-starter011 | 安全启动 | 聚合 + jjwt + spring-security-crypto |
| 6 | java17-data-mybatis011-starter | 数据启动 | Druid + 驱动(runtime) + 动态数据源 kernel（池/路由/方言 SPI 4+4/探针）+ **框架 Mapper 自动装配** |
| 7 | center-storage011-starter | 存储 | **完整存储中心**（管理面 + local011/minio011；object 端口契约留 core，backup 软依赖 403 守卫） |
| 9 | java17-scheduler-quartz011-starter | 调度 | Quartz 引擎 + Handler 注册表 + 启动重注册；**真可选** |
| 10 | center-message011-starter | 消息 | **完整消息中心**（出入两套 + 管理面 + 内置渠道 inapp/webhook）；真可选（缺席整体 404） |
| 11 | center-ai011-starter | AI | **完整 AI 中心**（管理面 + 能力引擎 + 媒体落盘，依赖 center-storage）；真可选（缺席整体 404） |
| 12 | java17-observability011-starter | 可观测性 | 新建：actuator + Prometheus + traceId MDC 过滤器 + 3 健康指示器（Quartz 指示器条件化） |
| 14 | java17-app011 | 参考应用 | 唯一 main + 配置 + demo11 样板；`AuthPermissionDemoSeed011` 仅在 **src/test**（fat jar / `dev011`/`dev013` 不跑） |

## 三、刻意保持不变的部分（铁律）

- **Java 包结构** `com.klsjnh.{common,domain,application,web,infrastructure}.*` 与全部类名（含 `011` 后缀）——门禁全按包名锚定，零规则改动（仅 `tools/klsjnh-standards-doc.mjs` 1 行扫描路径）。
- **URL 契约** `/klsjnh/<module>/v1/**` 与六键响应信封——013.api-contract 一字未改。
- **DDL**（docs/sql/）与数据库表结构。
- **业务工程动线**（020 §9）：业务方照旧自建四层模块，基座家族继承方式不变。

## 四、关键决策记录

| # | 决策 | 理由 / 放弃的替代 |
|---|------|------------------|
| D1 | **胖核心 + 瘦 starter**（非按特性纵切） | 审计报告原始清单即此形态；管理面 API 跟核心走，消费方引 core 即得全平台能力；避免额外增加存储核心/IAM 启动器等清单外模块 |
| D2 | **BOM 独立无 parent** | root import 子模块 BOM 会形成 POM import 环；版本属性由 BOM 自持（仅与 root 的插件版本属性重复一项） |
| D3 | **持久化基座家族留 core**（BaseRepository/BasePo/CommonMapper/MybatisPlusConfig011） | 依赖方向 starter→core 单向不可破：特性 RepositoryImpl（core 内）继承基座；data-starter 只装 kernel 与 Druid/驱动 |
| D4 | **装配单轨：宿主零框架扫描** | core 经 `AutoConfiguration.imports` 定向扫描自己的三层包注册组件；starter 适配器随 classpath 在场被同一扫描收编；框架 Mapper 由 data-starter 的 `@MapperScan` 自动装配，宿主只声明业务 mapper 包 |
| D5 | **缺件熔断（默认安全）** | `AuthChainPresenceCheck011`：web 应用类路径缺 AuthTokenPort（= 没引 security-starter）**拒绝启动**；把审计报告"默认安全"落到打包层 |
| D6 | **可选能力条件化** | 9 个硬依赖可选端口的用例/控制器挂 `@ConditionalOnClass(name=实现类 FQCN)`（字符串形式，core 不编译期依赖 starter）——不引 scheduler/ai starter：应用正常启动，对应端点随 starter 整体消失（含 Swagger） |
| D7 | **KrtConfig011 解散为三个绑定类** | 消费方分布横跨三个模块，单类会造环：`KrtSecurityConfig011`（status/jwt/web + 生产启动卫兵）、`KrtDatasourceConfig011`（ci011，core）、`KrtAiConfig011`（ai-center + media，ai-starter）；键名与语义不变 |
| D8 | **演示内容移出框架** | demo11 纵切面、Demo011Scheduler、`AuthPermissionDemoSeed011` 全部归 `java17-app011`；框架制品（core/starter）不再含演示账号播种；Seed 在 **test 域**，非每次 fat jar 启动 |
| D9 | 顺带修复（审核 P0-2） | GlobalAuthFilter 白名单改 **URI 归一化匹配**（堵 `/v3/api-docs/../` 绕过）+ 放行 `/actuator/health` + traceId 复用 MDC 既有值 |
| D10 | 未夹带其余 P0 | 见 §八 遗留清单；重构期间保持行为等价优先 |
| D11 | **中心类 starter 命名**：`center-<名>011-starter`（无 java17 前缀） | center-ai011 / center-message011 / center-storage011——消费方直接面对的能力线与框架内部模块（java17-*）在命名上分层 |
| D12 | **中心整体剥离**：AI / 消息 / 存储的管理面自 core 全部迁入各自 starter；object 端口契约与备份（ObjectProvider 软依赖，缺席 403 提示）留 core | 消费方语义彻底化：不引 = 中心从未存在；金标准协议 029 固化回归阶梯 |

## 五、对消费方的变化

**接入四步**（详见 [020](020.business-project-quickstart.md)）：import BOM → 引 starters → main 只扫自己的包（框架 mapper 自动装配）→ 配 yml + 执行 DDL。

**必选 / 可选**（缺席行为均已实测）：

| Starter | 必选性 | 缺席时 |
|---|---|---|
| `java17-security-starter011` | 必选（熔断） | web 应用拒绝启动 |
| `java17-data-mybatis011-starter` | 必选 | 数据访问 kernel 缺失 |
| `java17-scheduler-quartz011-starter` | 可选 | 调度端点整体 404（路由级），system011 其余照常 |
| `center-ai011-starter` | 可选 | AI 中心整体 404（管理面 + 推理/TTS/ASR/图片）；不引则 classpath 无 AI |
| `center-storage011-starter` | 可选（用存储时必引；引 AI 会传递依赖） | 存储中心整体 404；备份软依赖 → 403 提示 |
| `center-message011-starter` | 可选 | 消息中心整体 404 |
| `java17-observability011-starter` | 可选 | 无 actuator/指标/traceId MDC |

## 六、验证记录（dev016，Ubuntu 26.04 + JDK 17.0.12 + Maven 3.9.12）

| # | 验证 | 结果 |
|---|------|------|
| V1 | gate（正则 + tree-sitter AST + doc-api-path，582 文件） | PASSED，0 issue |
| V2 | `mvn -o clean package` 含测试 | BUILD SUCCESS，18 用例全绿（core 5 / security 4 / message 9） |
| V3 | 启动冒烟（零扫描装配 fat jar） | Started ~13.8s；health 200 `UP`；doc.html 200；启动日志 0 ERROR、无重复 bean |
| V4 | 接口级权限矩阵（tools/auth-demo-matrix.sh） | EXIT=0：超管全 200；test015 仅 select、test016 仅 backup，其余 403 缺权限 |
| V5 | 负向验证（摘 scheduler + ai starter） | 打包成功、启动成功、条件端点 404（路由级）、留守端点业务 404/401 正常 |
| V6 | 正向复跑（还原后） | V1–V4 全绿复现 |

## 七、文档同步清单

README（架构总览/模块表/装配说明）· 015.project-info（Maven 骨架/运行命令）· 020 quickstart（依赖模型/main 模板/必选可选表/pitfalls）· infrastructure011/013（结构全篇）· 015.config（krt 键拆分）· 020.deploy（产物/日志）· 017 红线（卫兵实现类）· 033（密钥位表述）· storage/message 用法篇（BOM 片段）· 018.datasource-center（模块归属图）· requirement 两源 13 篇（映射 + 注记）· script011.sh / deploy.sh / doc-checker / logback / spring.application.name（5 处硬编码）。

## 八、遗留（P0 / 口径勘误，2026-09-26 反向审核）

> 下列条目相对 2026-09-25 原文已核对代码；**已过时的表述划掉并注明现行事实**。

1. ~~**演示播种：每次启动重置全部演示账号密码**~~ → **现行**：`AuthPermissionDemoSeed011` 在 `java17-app011/src/test`，**仅测试阶段运行**；fat jar / `dev011`/`dev013` **不**自动重置。若跑集成测试仍会重置并发密码日志——测试场景仍须警惕，生产勿依赖该 Seed。
2. ~~**功能级鉴权仍是全局 opt-in**~~ → **现行**：按 `krt.status` **双模式**（开发/测试 opt-in；**生产写操作白名单**，未核对 → 403）。未挂 `assertHas` 的写入口在 production 仍会被 Filter 白名单拦住；开发/测试仍可能裸奔——详 [013 · 016](infrastructure011/013.access-center/016.topic-dynamic-permission.md)。
3. **同步链路可写主库 `july_user`**（SyncSink 无目标表白名单）；`SqlGuard011` 缺 `INTO OUTFILE`/`LOAD_FILE` 禁词 —— **仍开放**。
4. **凭据治理**：入库 development 配置含明文凭据与可预测 JWT secret（需轮换 + 历史清理 + Gitleaks 进 CI）；production 模板改占位符 —— **仍开放**。
5. ~~工程债：gate 不跑测试~~ → **现行**：`script011.sh gate` = 规范检查 + **`mvn -o clean install`（含 surefire + 装 .m2）**。其余工程债仍在：Druid 探测池泄漏；消息中心克隆家族；starter `@ConditionalOnMissingBean` 覆写完整化。
6. **033 SecretCipher**：**提案未落地**（无 `SecretCipherPort` 实现），见 [033](infrastructure011/033.topic-platform-security.md)。

> **2026-09-26 勘误**：033 已落地 **B0–B3**（含消息渠道 config 字段级加密）；Token 吊销见 `token_version` / AT-05。上文保留当日重构时事实。
