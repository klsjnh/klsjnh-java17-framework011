# docs/sql — DDL 唯一真源（平台底座）

> 本目录是**平台底座**数据库结构的唯一真源：表/列变更先改这里，再落库。
> **低代码产品表**（`july_metadata*`、`july_business_modeling`）已迁至低代码产品项目 `klsjnh-java17-lowcode011`，不在本目录。
> 建表规范（公共列 / 列顺序 / 逻辑删除唯一键）见 [base-entity-columns.sql](base-entity-columns.sql)。

## 命名惯例

- **中心级 DDL**（按中心归并多表）：统一 **`july_center_<slug>.sql`** → `july_center_iam` · `july_center_ai` · `july_center_message` · `july_center_storage` · `july_center_datasource`。
- **已废文件名**（勿新建、勿引用）：`july_*_center.sql`、`july_iam.sql`、`july_perm.sql`、`july_storage_provider.sql`、`july_datasource.sql`、`july_sync_rule.sql`，以及分拆的 `july_organization` / `july_menu` / `july_role` / `july_user` 独立 SQL（已并入 `july_center_iam.sql`）。
- 非中心归并的单域文件仍用 `july_<domain>.sql`（如 `july_config.sql`、`july_scheduler.sql`）。
- **只改 SQL 文件名**；表名不变（如 `july_organization`、`july_perm_object`、`july_ai_model_provider`、`july_storage_provider`）。

## 新库初始化顺序

1. **各域建表**（顺序无关，均 `CREATE TABLE IF NOT EXISTS`）：

   | 域 | 文件 | 表 |
   |----|------|----|
   | **iam** | **`july_center_iam.sql`**（组织/菜单/角色/用户/权限目录，按中心归并） | `july_organization` · `july_menu` · `july_role` · `july_role_permissions` · `july_user` · `july_user_role` · `july_user_audit` · `july_perm_object` · `july_perm_action` |
   | system011 | `july_config.sql` · `july_scheduler.sql` · `july_dictionary.sql` | `july_config` · `july_scheduler` · `july_dictionary` · `july_dictionary_item` |
   | datasource | **`july_center_datasource.sql`**（数据源 + 同步规则，按中心归并） | `july_datasource` · `july_sync_rule` · `july_sync_rule_column` |
   | aicenter | **`july_center_ai.sql`**（模型接入 + 提示词，按中心归并） | `july_ai_model_provider` · `july_ai_model_provider_api` · `july_ai_domain` · `july_ai_domain_prompt` |
   | storagecenter | **`july_center_storage.sql`**（实例 + 桶，按中心归并） | `july_storage_provider` · `july_storage_provider_bucket` |
   | messagecenter | **`july_center_message.sql`**（出站 + 入站，按中心归并） | `july_message_outbound_channel` · `july_message_outbound_template` · `july_message_outbound` · `july_message_inbound_channel` · `july_message_inbound_template` · `july_message_inbound` |
   | demo | `july_demo011.sql` | `july_demo011` |

2. **逻辑删除唯一键迁移**：`logic-delete-unique-fix.sql` —— 把业务唯一键切到 `alive_*` 生成列，**必须在对应表建好之后执行一次**。

3. `base-entity-columns.sql` 仅**模板/说明**，不执行。

## 不在本目录的表

- 低代码产品表（`july_metadata*` / `july_business_modeling`）与低代码运行时发布的物理表（`<objectName>`）：见 `klsjnh-java17-lowcode011` 的 `docs/sql`。
