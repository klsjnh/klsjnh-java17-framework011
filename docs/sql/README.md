# docs/sql — DDL 唯一真源（平台底座）

> 本目录是**平台底座**数据库结构的唯一真源：表/列变更先改这里，再落库。
> **低代码产品表**（`july_metadata*`、`july_business_modeling`）已迁至低代码产品项目 `klsjnh-java17-lowcode011`，不在本目录。
> 建表规范（公共列 / 列顺序 / 逻辑删除唯一键）见 [base-entity-columns.sql](base-entity-columns.sql)。

## 新库初始化顺序

1. **各域建表**（顺序无关，均 `CREATE TABLE IF NOT EXISTS`）：

   | 域 | 文件 |
   |----|------|
   | system011 | `july_config` · `july_menu` · `july_organization` · `july_user` · `july_role` · `july_scheduler` · `july_dictionary` |
   | datasource | `july_datasource` |
   | ai011 | `july_ai_model_provider` |
   | storagecenter | `july_storage_provider` · `july_storage_provider_bucket` |
   | demo | `july_demo011` |

2. **逻辑删除唯一键迁移**：`logic-delete-unique-fix.sql` —— 把业务唯一键切到 `alive_*` 生成列，**必须在对应表建好之后执行一次**。

3. `base-entity-columns.sql` 仅**模板/说明**，不执行。

## 不在本目录的表

- 低代码产品表（`july_metadata*` / `july_business_modeling`）与低代码运行时发布的物理表（`<objectName>`）：见 `klsjnh-java17-lowcode011` 的 `docs/sql`。
