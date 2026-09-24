# docs — 知识库索引

> 协议全集：[011.agreements.md](011.agreements.md)（跨语言通用：协作铁律 / 七步流程 / 目录语义 / 编号规则 / 归档规则 / 代码先行审查）；项目侧编号占用现状见文末「编号分配台账」。

## 阅读入口

| 文档 | 内容 |
|------|------|
| [../Agent.md](../Agent.md) | **项目门牌层**（先读）：进门先看什么、按什么顺序看（跨项目通用，不展开协议正文） |
| [011.agreements.md](011.agreements.md) | **协议全集**（跨语言通用）：协作铁律 / 七步流程 / 目录语义 / 编号规则 / 归档规则 / 代码先行审查 |
| [013.api-contract.md](013.api-contract.md) | API 契约（信封六键 + 状态码表 + 鉴权口径） |
| [015.project-info.md](015.project-info.md) | 项目信息（定位 / 技术栈 / 构建运行 / 当前能力） |
| [016.coding-standards.md](016.coding-standards.md) | 编码规则（真实代码示例 + 门禁规则映射） |
| [017.tech-debt-redlines.md](017.tech-debt-redlines.md) | 技术债红线（参考实现踩过的坑 + 不可回退的硬约束） |
| 019.backend-api-review.md | 后端接口质量评审（契约层已知缺口 / 哪些不能信 Swagger） |
| [020.business-project-quickstart.md](020.business-project-quickstart.md) | **业务项目快捷手册**（消费者脚手架 / Boot / july_* / SqlRoutingPort；[提示词](020.business-project-quickstart.prompt.md)） |
| [deploy/](deploy/) | **容器化落地件**（脚本 / Dockerfile / compose）；机制见 [infrastructure011/020](infrastructure011/020.topic-deploy-docker.md) |

## 专题与知识库

| 目录 | 内容 |
|------|------|
| [infrastructure011/](infrastructure011/) | **架构类需求承载地**：011 选型 · 013 结构 · 015 配置 · **[018.iam-center IAM 中心](infrastructure011/018.iam-center/011.topic-design.md)**（组织/用户/菜单/角色/目录/opt-in；动态权限详篇 [016](infrastructure011/018.iam-center/016.topic-dynamic-permission.md)；包与 URL = `…iam` / `/klsjnh/iam/**`，**非** system011）· **[011.storage-center 存储中心](infrastructure011/011.storage-center/011.topic-design.md)**（对象元数据 [016 规约·非平台能力](infrastructure011/011.storage-center/016.topic-object-metadata-convention.md)）· 013.message · 015.ai · 017.datasource · 031.persistence · **[033 机密加密](infrastructure011/033.topic-platform-security.md)**（SecretCipher；权限互引 018，不双写）· **[020 部署机制](infrastructure011/020.topic-deploy-docker.md)**（落地件 → [deploy/](deploy/)） |
| [sql/](sql/) | DDL 唯一真源（见 [sql/README](sql/README.md)）：公共列模板 + `july_*.sql`；中心级 **`july_center_<slug>.sql`**（[iam](sql/july_center_iam.sql) / [ai](sql/july_center_ai.sql) / [message](sql/july_center_message.sql) / [storage](sql/july_center_storage.sql) / [datasource](sql/july_center_datasource.sql)） |
| [deploy/](deploy/) | 容器化落地件（自根目录迁入）；操作见 [README](deploy/README.md) |
| requirement011/ | 普通需求：011 菜单 · 013 组织 · 015 用户 · 016 角色（IAM 业务侧，架构见 018）· 022 调度 · 023 配置 · 025 导出 · 026 数据源 · 027 字典 · 028/030 AI · 032 导入（均已编码）。无独立 `requirement011/029` |
| requirement013/ | 详细设计 · 对接代码：022/023/026/027/028/029/030/031/032 均已编码；IAM **代码已齐**，013 补档可选 |
| archive011/ | 历史工作日志归档 |

> **两源一汇（2026-09-20 约定）**：架构类需求（平台能力演进）入 `infrastructure011/`；普通需求（业务诉求）入 `requirement011/`；两路合流至 `requirement013/`（详细设计 · 对接代码）。判据见 [011.agreements.md](011.agreements.md) §015。
>
> **边界速记（对齐现行代码）**：`system011` = 配置 / 调度 / 字典；组织 / 菜单 / 用户 / 角色 / 权限目录 = **IAM**；存储 016 元数据 = **规约**（无 `july_files`、非平台能力）。

## 编号分配台账（现状）

> 自 `011.agreements.md` §017 迁入（2026-09-14）：新版协议 §017 已改为「归档规则」，跨语言通用的协议正文不承载项目实例，故台账下沉至本索引。
> 编号规则本体见 [011.agreements.md](011.agreements.md) §016。

| 编号 | 状态 | 当前用途 |
|------|------|----------|
| 011 | 在用 | 协议全集（011.agreements）；infrastructure011/011 架构选型；requirement011/011 july-menu |
| 012 | 禁用 | — |
| 013 | 在用 | api-contract；infrastructure011/013 项目结构；requirement011/013 july-organization |
| 014 | 禁用（含 4） | — |
| 015 | 在用 | project-info；infrastructure011/015 配置体系；requirement011/015 july-user |
| 016 | 在用 | coding-standards；requirement011/016 july-role（原 infrastructure011/016 持久化体系已删除，折入 031 持久化中心） |
| 017 | 在用 | 顶层 017.tech-debt-redlines；infrastructure011/017.datasource-center 数据源中心（原 `017.topic-datasource` 已折入其 `016.topic-datasource`） |
| 018 | 在用 | infrastructure011/018.iam-center **IAM 中心**（011 设计 · 013 架构 · 015 用法 · **016 动态权限详篇**） |
| 019 | 在用 | 顶层 019.backend-api-review；infrastructure011 存储中心（现 `011.storage-center/`：011/013/015 + **016 对象元数据规约**，019 底册已拆） |
| 020 | 在用 | **顶层** 020.business-project-quickstart（业务项目消费者快捷手册 + `.prompt`）；infrastructure011/020 容器化部署（落地件 **`docs/deploy/`**） |
| 021 | 在用 | infrastructure011 消息中心（现 `013.message-center/`，021 底册已拆；平台架构能力 · 多渠道可插拔 · P1 已实现） |
| 022 | 在用 | requirement011/013 july-scheduler（定时任务） |
| 023 | 在用 | requirement011/013 july-config（配置管理） |
| 025 | 在用 | requirement011 平台导出（export） |
| 026 | 在用 | requirement011/013 数据源管理（datasource 域 / july_datasource） |
| 027 | 在用 | requirement011 数据字典（system011 / july_dictionary） |
| 028 | 在用 | requirement011/013 AI 模型接入（aicenter / july_ai_model_provider） |
| 029 | 在用 | requirement013 存储中心管理面（`029.topic-storage-migration.md`）；架构底册 `infrastructure011/011.storage-center/`（含 [016 对象元数据规约](infrastructure011/011.storage-center/016.topic-object-metadata-convention.md)；**无** `requirement011/029`） |
| 030 | 在用 | requirement011/013 AI 模型调用（推理 / 文生图 / 图生图 / TTS / 语音识别 / 提示词，aicenter） |
| 031 | 在用 | requirement013 数据源中心（读写分页 / 类型契约 / 同步 S1，datasource）；infrastructure011/031 持久化中心（形态 1–8；[017](infrastructure011/031.persistence-center/017.topic-sort-support-simplify.md)/[018](infrastructure011/031.persistence-center/018.topic-master-sub-api.md) 已验收落地；[019](infrastructure011/031.persistence-center/019.topic-persistence-followups.md) 可选） |
| 032 | 在用 | requirement011/013 平台导入（xlsx；与 025 导出对称；字典打样） |
| 033 | 在用 | infrastructure011/033 机密加密存储提案（SecretCipher only；权限 → 018/016） |

> 历史：`020 / 021`（并入 019）、`022 / 023 / 025 / 026 / 027 / 028`（旧主题/角色/组织等）为 **2026-09-14 前的历史占用**，号不回收；本表仅登记**现行**用途。

**下一可用编号：035。**（034 含 4 跳过；044/046/048/049 含 4，跳过）

> ✅ 2026-09-14 已办：① 表中 `016` 原有两行已合并为一行（原重复行信息并入）；③ 顶层常驻文档已按新版协议改名 —— `016.api-contract`→`013.api-contract`、`013.project-info`→`015.project-info`、`015.coding-standards`→`016.coding-standards`（编号不释放、不复用）。
> ✅ 2026-09-15 已办：② `019.backend-api-review.md` 已落盘（后端接口质量评审：Swagger 可信度 / 鉴权口径 / 已知缺口 / 新端点自检清单）并登记台账。
> ✅ 2026-09-24 已办：根目录 `deploy/` → **`docs/deploy/`**；文档收敛对齐 IAM 包/URL、中心 SQL `july_center_<slug>`、存储 016 规约边界；`system011` 不再写成菜单/组织归属。
> 📌 **号位口径（用户裁定 2026-09-14）：跨目录不算同号位。** 顶层文档号与专题目录号**各自成位**，故 `017.tech-debt-redlines.md` 与 `infrastructure011/017.datasource-center/` **不构成冲突**，无需避让；`011.agreements.md` 与 `infrastructure011/011.storage-center/` 同理。判重只在**同一目录、同一序列**内进行。
