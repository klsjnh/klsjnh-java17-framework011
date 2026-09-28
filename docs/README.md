# docs — 知识库索引

> 协议全集：[011.agreements.md](011.agreements.md)（跨语言通用：协作铁律 / 七步流程 / 目录语义 / 编号规则 / 归档规则 / 代码先行审查）；项目侧编号占用现状见文末「编号分配台账」。

## 阅读入口（先读这些）

| 文档 | 内容 |
|------|------|
| [../Agent.md](../Agent.md) | **项目门牌层**（先读）：进门先看什么、按什么顺序看 |
| [011.agreements.md](011.agreements.md) | **协议全集**（跨语言通用） |
| [015.project-info.md](015.project-info.md) | 项目事实卡（栈 / Maven 骨架 / **gate=`mvn -o clean install`** / 能力） |
| [013.api-contract.md](013.api-contract.md) | API 契约（信封六键 + 状态码 + 鉴权） |
| [016.coding-standards.md](016.coding-standards.md) | 编码规则 + 门禁映射 |
| [017.tech-debt-redlines.md](017.tech-debt-redlines.md) | 技术债红线 |
| [019.backend-api-review.md](019.backend-api-review.md) | 后端接口质量评审（契约层缺口） |
| [020.business-project-quickstart.md](020.business-project-quickstart.md) | 业务项目消费者快捷手册（[提示词](020.business-project-quickstart.prompt.md)） |
| [deploy/](deploy/) | 容器化落地件；机制 → [infrastructure011/020](infrastructure011/020.topic-deploy-docker.md) |

## 中心 / 专题入口（现行 · 一张表）

| 能力 | 现行入口 | 代码 / DDL 真源 |
|------|----------|-----------------|
| **持久化规约** | [011.persistence-spec](infrastructure011/011.persistence-spec/011.topic-design.md) | core（PO / 仓库基座 / SortSupport） |
| **访问中心** | [013.access-center](infrastructure011/013.access-center/011.topic-design.md) · 动态权限 [016](infrastructure011/013.access-center/016.topic-dynamic-permission.md) | `center-access011-starter`（用户登录在 core）；URL `/klsjnh/iam/**`；用户 DDL [july_core011.sql](sql/july_core011.sql) · 访问中心 DDL [july_center_access011.sql](sql/july_center_access011.sql) |
| **平台中心** | [014.platform-center](infrastructure011/014.platform-center/README.md) | `center-platform011-starter`；[july_center_platform011.sql](sql/july_center_platform011.sql)（`july_config` + 字典主子表） |
| **存储中心** | [015.storage-center](infrastructure011/015.storage-center/011.topic-design.md) | `center-storage011-starter`；[july_center_storage011.sql](sql/july_center_storage011.sql) |
| **消息中心** | [016.message-center](infrastructure011/016.message-center/011.topic-design.md)（[013 架构](infrastructure011/016.message-center/013.topic-architecture.md) · [015 用法](infrastructure011/016.message-center/015.topic-usage.md) · [016 二开](infrastructure011/016.message-center/016.topic-secondary-dev.md)） | `center-message011-starter`；web=`…messagecenter.inbound|outbound.*`；URL `/klsjnh/messagecenter/**`；[july_center_message011.sql](sql/july_center_message011.sql) |
| **AI 中心** | [017.ai-center](infrastructure011/017.ai-center/011.topic-design.md) | `center-ai011-starter`；[july_center_ai011.sql](sql/july_center_ai011.sql) |
| **数据源中心** | [018.datasource-center](infrastructure011/018.datasource-center/011.topic-design.md) | `center-datasource011-starter`（管理面 / Sql HTTP / Sync）；kernel 端口在 core、执行器（含 `SqlRoutingPort`）在 `java17-data-mybatis011-starter`；[july_center_datasource011.sql](sql/july_center_datasource011.sql) |
| **调度 julyScheduler** | [022](infrastructure011/022.topic-july-scheduler.md) | `java17-scheduler-quartz011-starter`（随 `@EnablePlatform011Center` 扫入）；[july_scheduler011.sql](sql/july_scheduler011.sql) · 设计 [requirement013/022](requirement013/022.topic-july-scheduler.md) |
| **机密加密** | [033](infrastructure011/033.topic-platform-security.md) | `SecretCipherPort` + AES-GCM；AI/存储/数据源 + 消息渠道 config 字段级；DDL 列宽见 [secret-cipher-column-widen.sql](sql/secret-cipher-column-widen.sql) |
| **金标准回归** | [029](infrastructure011/029.topic-golden-verification.md) | `script011.sh gate` = 规范 + `mvn -o clean install` |
| **部署机制** | [020](infrastructure011/020.topic-deploy-docker.md) | 落地件 [deploy/](deploy/) |
| **选型 / 结构 / 配置** | [011](infrastructure011/011.topic-infrastructure.md) · [013](infrastructure011/013.topic-project-structure.md) · [015](infrastructure011/015.topic-config.md) | 根 `pom.xml`（BOM + 瘦 core + center/tech starters） |

> **边界速记**：`system011` 配置 / 字典 → `center-platform011-starter`；**调度** → `java17-scheduler-quartz011-starter`（见 [022](infrastructure011/022.topic-july-scheduler.md)）；组织 / 菜单 / 角色 / 权限目录 = **访问中心**（`center-access011-starter`，URL `/klsjnh/iam/**`，用户登录在 core）；存储对象元数据 016 = **规约**（非平台能力）。
>
> **中心 = 可选装配**：6 个中心都可独立装配——5 个业务中心（datasource / ai / message / platform / storage）可独立于 `center-access011-starter` 运行，权限种子由各中心 `XxxPermCatalogSeedConfig011`（`@ConditionalOnClass`）条件注册；`java17-data-mybatis011-starter` 单独提供 `SqlRoutingPort`。中心 `*CenterAutoConfiguration011` 位于各自 `com.klsjnh.enabled`，只由 `@Enable` 拉入；access 组件 / mapper 在 `com.klsjnh.….iam.access.*`，core 只扫 `iam.user` / `iam.auth`。**"默认关"对全部中心成立**。

## 目录台账

| 目录 | 状态 | 内容 |
|------|------|------|
| [infrastructure011/](infrastructure011/) | **现行** | 架构类需求；上表入口 |
| [sql/](sql/) | **现行** | DDL 唯一真源：公共列模板 + `july_*.sql` + `july_center_<center>011.sql` |
| [deploy/](deploy/) | **现行** | 容器化落地件 |
| [requirement011/](requirement011/) | **现行** | 普通需求：011 菜单 · 013 组织 · 015 用户 · 016 角色（访问中心）· 022 调度 · 023 配置 · 025 导出 · 026 数据源 · 027 字典 · 028/030 AI · 032 导入（无 029） |
| [requirement013/](requirement013/) | **现行** | 详细设计 · 对接代码（含 029 存储管理面迁移） |
| [archive011/](archive011/) | **归档** | 历史归档（规则见 [011.agreements.md](011.agreements.md) §017） |

> **两源一汇**：架构类 → `infrastructure011/`；普通需求 → `requirement011/`；合流 → `requirement013/`。判据见 [011.agreements.md](011.agreements.md) §015。

## 编号分配台账（现状）

> 自 `011.agreements.md` §017 迁入。编号规则本体见 [011.agreements.md](011.agreements.md) §016。
> **号位口径**：跨目录不算同号位（顶层号与专题目录号各自成位）。

| 编号 | 状态 | 当前用途 |
|------|------|----------|
| 011 | 在用 | 协议全集（011.agreements）；infrastructure011/011 架构选型；**011.persistence-spec** 持久化规约；requirement011/011 july-menu |
| 012 | 禁用 | — |
| 013 | 在用 | api-contract；infrastructure011/013 项目结构；**013.access-center** 访问中心；requirement011/013 july-organization |
| 014 | 禁用（含 4） | — |
| 015 | 在用 | project-info；infrastructure011/015 配置体系；**015.storage-center**；requirement011/015 july-user |
| 016 | 在用 | coding-standards；requirement011/016 july-role；**016.message-center**；013.access-center/016 动态权限 |
| 017 | 在用 | 顶层 017.tech-debt-redlines；**017.ai-center** |
| 018 | 在用 | **018.datasource-center** |
| 019 | 在用 | 顶层 019.backend-api-review；[015.storage-center](infrastructure011/015.storage-center/011.topic-design.md) |
| 020 | 在用 | 顶层 020.business-project-quickstart；infrastructure011/020 容器化（落地件 `docs/deploy/`） |
| 021 | 在用 | [016.message-center](infrastructure011/016.message-center/011.topic-design.md) |
| 022 | 在用 | requirement011/022 july-scheduler；**infrastructure011/022** 调度 starter 架构入口 |
| 023 | 在用 | requirement011/023 july-config |
| 025 | 在用 | requirement011 平台导出；[017.ai-center](infrastructure011/017.ai-center/011.topic-design.md) |
| 026 | 在用 | requirement011 数据源管理；[016.message-center](infrastructure011/016.message-center/011.topic-design.md) |
| 027 | 在用 | requirement011 数据字典；[015.storage-center](infrastructure011/015.storage-center/011.topic-design.md) |
| 028 | 在用 | requirement011/028 AI 模型接入 |
| 029 | 在用 | requirement013 存储管理面迁移；infrastructure011/029 金标准验证协议（无 requirement011/029） |
| 030 | 在用 | requirement011/030 AI 模型调用 |
| 031 | 在用 | requirement013 数据源中心；[011.persistence-spec](infrastructure011/011.persistence-spec/011.topic-design.md) |
| 032 | 在用 | requirement011/032 平台导入 |
| 033 | 在用 | infrastructure011/033 机密加密存储（SecretCipher）；权限 → 013.access-center/016 |

> 历史占用号不回收。本表仅登记**现行**用途。

**下一可用编号：035。**（034 含 4 跳过；044/046/048/049 含 4，跳过）
