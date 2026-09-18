# docs/sql — DDL 唯一真源

> 本目录是**数据库结构的唯一真源**：表/列变更先改这里，再落库。
> 建表规范（公共列 / 列顺序 / 逻辑删除唯一键）见 [base-entity-columns.sql](base-entity-columns.sql)。

## 新库初始化顺序

1. **各域建表**（顺序无关，均 `CREATE TABLE IF NOT EXISTS`）：

   | 域 | 文件 |
   |----|------|
   | system011 | `july_config` · `july_menu` · `july_organization` · `july_user` · `july_role` · `july_scheduler` · `july_dictionary` |
   | datasource | `july_datasource` · `july_business_modeling` |
   | ai011 | `july_ai_model_provider` |
   | storagecenter | `july_storage` |
   | demo | `july_demo011` |

2. **逻辑删除唯一键迁移**：`logic-delete-unique-fix.sql` —— 把业务唯一键切到 `alive_*` 生成列，**必须在对应表建好之后执行一次**。

3. `base-entity-columns.sql` 仅**模板/说明**，不执行。

## 顺序依赖（易踩）

- `july_metadata.sql` **必须先于** `july_metadata_publish.sql`（后者对 `july_metadata` 做 `ALTER` 加发布态列，并建 `july_metadata_version` / `july_metadata_open_api`）。
- `logic-delete-unique-fix.sql` 必须在相关表存在后执行（否则 `DROP INDEX` 报错）。

## 不在本目录的表

