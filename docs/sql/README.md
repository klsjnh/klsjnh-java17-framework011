# docs/sql — DDL 唯一真源（平台底座）

> 本目录是**平台底座**数据库结构的唯一真源：表/列变更先改这里，再落库。
> **低代码产品表**（`july_metadata*`、`july_business_modeling`）已迁至低代码产品项目 `klsjnh-java17-lowcode011`，不在本目录。
> 建表规范（公共列 / 列顺序 / 逻辑删除唯一键）见 [base-entity-columns.sql](base-entity-columns.sql)。

## 命名惯例

- **中心级 DDL**（按中心归并多表）：统一 **`july_center_<center>011.sql`** → `access` · `platform` · `ai` · `message` · `storage` · `datasource`。
- **核心用户纵切**：`july_core011.sql`（`july_user` / `july_user_audit`；属 `java17-core011`，不并入访问中心）。
- **调度技术栈**（非中心）：`july_scheduler011.sql`（属 `java17-scheduler-quartz011-starter`；**勿**命名为 `july_center_*`）。
- **已废文件名**（勿新建、勿引用）：`july_center_iam.sql`、`july_center_ai.sql` / `july_center_message.sql` / `july_center_storage.sql` / `july_center_datasource.sql`（无 `011` 后缀）、`july_config.sql`、`july_dictionary.sql`、`july_scheduler.sql`、以及更早的 `july_*_center.sql` / `july_iam.sql` / `july_perm.sql` / 分拆单表 SQL。
- **只改 SQL 文件名**；表名不变（如 `july_organization`、`july_perm_object`、`july_config`、`july_ai_model_provider`）。

## 新库初始化顺序

1. **各域建表**（顺序无关，均 `CREATE TABLE IF NOT EXISTS`）：

   | 域 | 文件 | 表 |
   |----|------|----|
   | **core · 用户** | **`july_core011.sql`**（账号/认证/用户审计） | `july_user` · `july_user_audit` |
   | **访问中心** | **`july_center_access011.sql`**（组织/菜单/角色/权限目录） | `july_organization` · `july_menu` · `july_role` · `july_role_permissions` · `july_user_role` · `july_perm_object` · `july_perm_action` |
   | **平台中心** | **`july_center_platform011.sql`**（配置 + 字典） | `july_config` · `july_dictionary` · `july_dictionary_item` |
   | **调度（技术）** | **`july_scheduler011.sql`**（Quartz 事实源，非中心；主子） | `july_scheduler` · `july_scheduler_audit` |
   | **数据源中心** | **`july_center_datasource011.sql`** | `july_datasource` · `july_sync_rule` · `july_sync_rule_column` |
   | **AI 中心** | **`july_center_ai011.sql`** | `july_ai_model_provider` · `july_ai_model_provider_api` · `july_ai_domain` · `july_ai_domain_prompt` |
   | **存储中心** | **`july_center_storage011.sql`**（实例 + 桶；不含 ObjectStorage 端口） | `july_storage_provider` · `july_storage_provider_bucket` |
   | **消息中心** | **`july_center_message011.sql`** | `july_message_outbound_channel` · `july_message_outbound_template` · `july_message_outbound` · `july_message_inbound_channel` · `july_message_inbound_template` · `july_message_inbound` |
   | **demo** | `july_demo011.sql` | `july_demo011` |

2. **逻辑删除唯一键迁移**：`logic-delete-unique-fix.sql` —— 把业务唯一键切到 `alive_*` 生成列，**必须在对应表建好之后执行一次**（新库若 CREATE 已含 `alive_*` 可跳过）。

3. **SecretCipher 列宽（已有库）**：`secret-cipher-column-widen.sql` —— 将 `api_key` / `access_key` / `secret_key` / `july_datasource.password` 加宽到 `VARCHAR(512)`；**新库 CREATE 已含 512 可跳过**。

4. `base-entity-columns.sql` 仅**模板/说明**，不执行。

## 不在本目录的表

- 低代码产品表（`july_metadata*` / `july_business_modeling`）与低代码运行时发布的物理表（`<objectName>`）：见 `klsjnh-java17-lowcode011` 的 `docs/sql`。
- 平台 backup / export / import：**无独立物理表**（内核写对象存储）。
