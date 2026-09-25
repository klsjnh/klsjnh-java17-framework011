# 014 — 平台中心（center-platform011-starter）

> 字典（julyDictionary）与系统配置（julyConfig）的管理面纵切。不引入本 starter 则无字典/配置管理 API。
> **DDL 真源**：[july_center_platform011.sql](../../sql/july_center_platform011.sql)（`july_config` · `july_dictionary` · `july_dictionary_item`）。备份/导出/导入无独立物理表。

## 模块

| 项 | 说明 |
|----|------|
| artifactId | `center-platform011-starter`（无 `java17-` 前缀，与 access / storage / message 同构） |
| 迁入 | `domain/application/infrastructure/web` 下的 julyDictionary + julyConfig 全栈，含各自 Export/Import Provider |
| DDL | [july_center_platform011.sql](../../sql/july_center_platform011.sql) |
| 权限种子 | 由 access 侧 `JulyPlatformPermCatalogSeed011` 在 platform 在 classpath 时 `ConditionalOnClass` 播种（与 message / scheduler 同构；perm 仓储在 access） |
| 暂留 core | `domain/application/infrastructure.platform011` 的导出/导入/备份内核（多业务对象 Provider 共用，牵连面大） |

## 依赖方向

```
center-platform011-starter → java17-core011
java17-app011 → center-platform011-starter（参考应用默认挂上）
```

详见 [013.topic-project-structure.md](../013.topic-project-structure.md)。
