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
| [infrastructure011/](infrastructure011/) | 整体底层架构设计：011 架构选型 · 013 目录结构 · 015 配置体系 · 016 持久化体系 · 017 动态数据源 · 018 IAM 总设计 · 019 存储中心 |
| [sql/](sql/) | DDL 唯一真源（base-entity-columns.sql 公共列模板） |
| requirement011/ | 业务设计：011 菜单 · 013 组织 · 015 用户 · 016 角色 · 021 julyScheduler（已编码）· 029 配置管理 · 030 数据导出——总设计 infrastructure011/018 |
| requirement013/ | 技术方案（021 julyScheduler 已编码完成；IAM 各主题按 011→013→编码 推进） |
| archive011/ | 历史工作日志归档 |

## 编号分配台账（现状）

> 自 `011.agreements.md` §017 迁入（2026-09-14）：新版协议 §017 已改为「归档规则」，跨语言通用的协议正文不承载项目实例，故台账下沉至本索引。
> 编号规则本体见 [011.agreements.md](011.agreements.md) §016。

| 编号 | 状态 | 当前用途 |
|------|------|----------|
| 011 | 在用 | 协议全集（011.agreements：铁律 / 七步流程 / 目录语义 / 编号规则 / 归档规则 / 代码先行审查）；infrastructure011/011 架构选型；requirement011/011 july-menu |
| 012 | 禁用 | — |
| 013 | 在用 | **api-contract 已落**（2026-09-14 自 `016.api-contract` 迁入；原 `013.project-info` 迁出至 015，号不释放、不复用）；infrastructure011/013 项目结构；requirement013 阶段目录保留号；requirement011/013 july-organization |
| 014 | 禁用（含 4） | — |
| 015 | 在用 | **project-info 已落**（2026-09-14 自 `013.project-info` 迁入，号不释放、不复用）；原「编码标准」已迁出至 016；infrastructure011/015 配置体系；requirement011/015 july-user（requirement015 已撤销，号不回收） |
| 016 | 在用 | **coding-standards 已落**（2026-09-14 自 `015.coding-standards` 迁入）；原「API 契约」已迁出至 013；infrastructure011/016 持久化体系；requirement011/016 july-role（自 022 迁入） |
| 017 | 在用 | **顶层** `017.tech-debt-redlines.md`（技术债红线，2026-09-14 落盘）；**专题目录** infrastructure011/017 动态数据源；julyScheduler 主题对已迁移至 021 |
| 018 | 在用 | infrastructure011/018 IAM 总设计；july-menu 主题已迁移至 011 |
| 019 | 在用 | **顶层** `019.backend-api-review.md`（后端接口质量评审，待落盘）；**专题目录** infrastructure011/019 存储中心；july-user 主题已迁移至 015 |
| 020 / 021 | 已分配后撤销 | 原独立主题已并入 019（号不回收，永不复用） |
| 021 | 在用 | julyScheduler 主题对（requirement011/013，自 017 迁移） |
| 022 | 已迁移 | july-role 主题已迁移至 016（号不回收） |
| 023 | 已分配后撤销 | 原角色用户独立主题已并入 022 / 019 |
| 025 | 已分配后撤销 | 原角色权限独立主题已并入 022 |
| 026 | 已迁移 | july-organization 主题已迁移至 013（号不回收） |
| 027 / 028 | 已分配后撤销 | 原 requirement011 主题，内容移入 infrastructure011/017、019（号不回收） |
| 029 | 在用 | july-config（requirement011/013，配置管理） |
| 030 | 在用 | export（requirement011/013，平台导出功能） |

**下一可用编号：031。**

> ✅ 2026-09-14 已办：① 表中 `016` 原有两行已合并为一行（原重复行信息并入）；③ 顶层常驻文档已按新版协议改名 —— `016.api-contract`→`013.api-contract`、`013.project-info`→`015.project-info`、`015.coding-standards`→`016.coding-standards`（编号不释放、不复用）。
> ⚠️ 未办：② `019.backend-api-review.md` 仍未落盘（`017.tech-debt-redlines.md` 已于 2026-09-14 落盘并登记）；落盘后需在本台账登记（行已预置用途）。
> 📌 **号位口径（用户裁定 2026-09-14）：跨目录不算同号位。** 顶层文档号与专题目录号**各自成位**，故 `017.tech-debt-redlines.md` 与 `infrastructure011/017.topic-dynamic-datasource.md` **不构成冲突**，无需避让；`019.backend-api-review.md` 与 `infrastructure011/019.topic-storage-center.md` 同理。判重只在**同一目录、同一序列**内进行。
