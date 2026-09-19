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

## 专题与知识库

| 目录 | 内容 |
|------|------|
| [infrastructure011/](infrastructure011/) | 整体底层架构设计：011 架构选型 · 013 目录结构 · 015 配置体系 · 016 持久化体系 · 017 动态数据源 · 018 IAM 总设计 · 019 存储中心 · 020 容器化部署 · 021 消息中心（多渠道可插拔 · P1 已实现） |
| [sql/](sql/) | DDL 唯一真源（base-entity-columns.sql 公共列模板 + 各 july_*.sql） |
| requirement011/ | 业务设计：011 菜单 · 013 组织 · 015 用户 · 016 角色 · 022 julyScheduler（已编码）· 023 配置管理（已编码）· 025 平台导出（已编码）· 026 数据源管理（datasource 域，已编码）· 027 数据字典（已编码）· 028 AI 模型接入（ai011，已编码）· 029 存储中心管理面（已编码）· 030 AI 模型调用 + **AI 能力（chat/图片/TTS，已编码）** |
| requirement013/ | 技术方案（022 julyScheduler / 023 配置管理 / 026 datasource / 027 数据字典 / 028 ai011 / 029 存储中心管理面 / 030 AI 模型调用（含图片/TTS 能力） 均已编码；IAM 各主题按 011→013→编码 推进） |
| archive011/ | 历史工作日志归档 |

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
| 016 | 在用 | coding-standards；infrastructure011/016 持久化体系；requirement011/016 july-role |
| 017 | 在用 | 顶层 017.tech-debt-redlines；infrastructure011/017 动态数据源 |
| 018 | 在用 | infrastructure011/018 IAM 总设计 |
| 019 | 在用 | 顶层 019.backend-api-review；infrastructure011/019 存储中心 |
| 020 | 在用 | infrastructure011/020 容器化部署 |
| 021 | 在用 | infrastructure011/021 消息中心（平台架构能力 · 多渠道可插拔 · P1 已实现） |
| 022 | 在用 | requirement011/013 july-scheduler（定时任务） |
| 023 | 在用 | requirement011/013 july-config（配置管理） |
| 025 | 在用 | requirement011 平台导出（export） |
| 026 | 在用 | requirement011/013 数据源管理（datasource 域 / july_datasource） |
| 027 | 在用 | requirement011 数据字典（system011 / july_dictionary） |
| 028 | 在用 | requirement011/013 AI 模型接入（ai011 / july_ai_model_provider） |
| 029 | 在用 | requirement011/013 存储中心管理面（storagecenter / july_storage_provider + july_storage_provider_bucket） |
| 030 | 在用 | requirement011/013 AI 模型调用（chat / 文生图 / 图生图 / TTS 能力，ai011） |

> 历史：`020 / 021`（并入 019）、`022 / 023 / 025 / 026 / 027 / 028`（旧主题/角色/组织等）为 **2026-09-14 前的历史占用**，号不回收；本表仅登记**现行**用途。

**下一可用编号：053。**（044/046/048/049 含 4，跳过）

> ✅ 2026-09-14 已办：① 表中 `016` 原有两行已合并为一行（原重复行信息并入）；③ 顶层常驻文档已按新版协议改名 —— `016.api-contract`→`013.api-contract`、`013.project-info`→`015.project-info`、`015.coding-standards`→`016.coding-standards`（编号不释放、不复用）。
> ✅ 2026-09-15 已办：② `019.backend-api-review.md` 已落盘（后端接口质量评审：Swagger 可信度 / 鉴权口径 / 已知缺口 / 新端点自检清单）并登记台账。
> 📌 **号位口径（用户裁定 2026-09-14）：跨目录不算同号位。** 顶层文档号与专题目录号**各自成位**，故 `017.tech-debt-redlines.md` 与 `infrastructure011/017.topic-datasource.md` **不构成冲突**，无需避让；`019.backend-api-review.md` 与 `infrastructure011/019.topic-storage-center.md` 同理。判重只在**同一目录、同一序列**内进行。
