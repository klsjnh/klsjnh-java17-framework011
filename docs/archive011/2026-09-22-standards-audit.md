# 2026-09-22 — 底座技术标准全仓审核报告

> **范围**：仅 `klsjnh-java17-framework011`。  
> **依据**：[016.coding-standards](016.coding-standards.md) · [017.tech-debt-redlines](017.tech-debt-redlines.md) · 持久化 [011/017](infrastructure011/011.persistence-spec/017.topic-sort-support-simplify.md) / [011/018](infrastructure011/011.persistence-spec/018.topic-master-sub-api.md) / [011/019](infrastructure011/011.persistence-spec/019.topic-persistence-followups.md)。  
> **机检**：`node tools/check-klsjnh-standards.mjs` → **PASSED**（544 Java · AST 0 · doc 0）。

---

## 011.结论一句话

门禁全绿；持久化 SortSupport / 主子三分基座与方案对齐。剩余为**已文档化例外** 2 条 + **级联行为债**（本轮只记、不改语义）。

---

## 013.机检

| 项 | 结果 |
|----|------|
| 扫描 Java | 544 |
| AST（funcName / log-concat） | 0 |
| doc-api-path | 0 |
| 总评 | **PASSED** |

---

## 015.红线对照

| 红线 | 结果 |
|------|------|
| `@Transactional` 仅 application | **例外已写清**：`UserAuditRecorder#record` 允许 `REQUIRES_NEW`（017 §011.013） |
| app/web 禁 `*Po` / `*Mapper` | ✅ |
| 禁路径参数 `/{id}` | ✅ |
| 禁 `OperatorContext` | ✅（审计读 `FrameConst011` 请求属性） |
| 业务禁物理删 | ✅（`physicalDelete` 零调用方） |
| 构造器注入 | **例外已写清**：Quartz `SchedulerHandlerJob` 允许字段 `@Autowired`（017 §011.016） |
| 主键应用侧生成 / `dr` 逻辑删 / 信封契约 | ✅（抽检未发现破口） |

---

## 016.持久化方案对照

| 项 | 结果 |
|----|------|
| 排序层 `*011` 仓储基类已删 | ✅ |
| `SortSupport`；无手写 `orderByAsc("sort_order")` | ✅ |
| 主子三分 API（saveMaster / getMaster / getChildren / saveChildren） | ✅ |
| 子 PO `MasterLinked` | ✅ |
| 019 任务 A 去后缀 | ✅ 工作树仅 `BaseMasterSubRepository`（git 显示 rename 待提交） |
| 019 B/C | 仍按拍板暂缓 |

### 级联债（本轮不改行为 · 另单）

| 级 | 项 | 说明 |
|----|----|------|
| MED | 存储提供商删主 | `getChildServices` 已注册桶，但 use case `logicDelete` 只 `logicDeleteById` 主表 → 可能孤儿桶 |
| MED | 角色删除 | `getChildServices` 空（toggle 不入级联）；删角色不清理 `role_permissions` |
| LOW | 用户批量删 | 单删 `cascadeDelete` 含 `user_role`；批量走 `batchLogicDelete` 不级联（use case 已注释「刻意」） |
| LOW | `parent_id` 空串 | 用例层多有归一；Menu/Org 域构造未强制 |

字典 / AI 域 / 模型提供商等「有子则 refuse」属 018 允许模式，**不算违规**。

---

## 017.本轮处置

| 项 | 动作 |
|----|------|
| `UserAuditRecorder` REQUIRES_NEW | 保留 + 017 明示例外 + 类 Javadoc |
| `SchedulerHandlerJob` 字段注入 | 保留（Quartz 约束）+ 017 明示例外 + 类 Javadoc |
| `JulyUserAuditQueryRepositoryImpl` | 去掉无用 `UserAuditRecorder` import |
| 级联债 | **仅记本报告**；不擅自改删除语义 |

---

## 018.建议后续单（非本轮）

1. 存储：删提供商时 `cascadeDelete` 或 refuse-if-buckets。  
2. 角色：删前清理 / 拒绝仍有 `role_permissions`。  
3. 用户批量：与单删对齐 cascade，或文档升格为正式口径。  
4. `TreePo` / Menu / Org：写入路径统一 `parent_id=""`。

---

## 019.回归

| 命令 | 结果 |
|------|------|
| `node tools/check-klsjnh-standards.mjs` | PASSED（修存储 Controller Javadoc 乱序后） |
| `mvn -pl java17-infrastructure011 test` | EXIT=0 |
| `mvn -q package -DskipTests` | EXIT=0 |

未 commit。
