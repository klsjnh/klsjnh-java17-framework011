# 014 — 平台中心（center-platform011-starter）

> 字典（julyDictionary）与系统配置（julyConfig）的管理面纵切。不引入本 starter 则无字典/配置管理 API。
> **DDL 真源**：[july_center_platform011.sql](../../sql/july_center_platform011.sql)（`july_config` · `july_dictionary` · `july_dictionary_item`）。备份/导出/导入无独立物理表。

## 模块

| 项 | 说明 |
|----|------|
| artifactId | `center-platform011-starter`（无 `java17-` 前缀，与 access / storage / message 同构） |
| 迁入 | `domain/application/infrastructure/web` 下的 julyDictionary + julyConfig 全栈，含各自 Export/Import Provider |
| DDL | [july_center_platform011.sql](../../sql/july_center_platform011.sql) |
| 权限种子 | 本 starter 内 `JulyPlatformPermCatalogSeed011`，由 `JulyPlatformPermCatalogSeedConfig011`（`@ConditionalOnClass(JulyPermObjectRepository)`）注册；**中心 + access 同时在场**才播种，`center-access011-starter` 为 optional（与 ai / datasource / message / storage / scheduler 同构） |
| 暂留 core | `domain/application/infrastructure.platform011` 的导出/导入/备份内核（多业务对象 Provider 共用，牵连面大） |

## 依赖方向

```
center-platform011-starter → java17-core011
java17-app011 → center-platform011-starter（参考应用默认挂上）
```

详见 [013.topic-project-structure.md](../013.topic-project-structure.md)。

## 099.怎么接入（注解式 · 2026-09-28）

> **本中心默认关**：jar 在 classpath 上不会自动生效 —— 必须由业务项目**显式开启**（bean 与 mapper 都随注解注册）。

- **依赖**：`center-platform011-starter`
- **access 可选**：权限种子由 `JulyPlatformPermCatalogSeedConfig011` 用 `@ConditionalOnClass(JulyPermObjectRepository)` 挂条件；不装 `center-access011-starter` 时整段跳过、本中心照常启动
- **开启**：启动类加 `@EnablePlatform011Center`
- **不开启**：本中心的 HTTP 全部 404、bean 与 mapper 都不注册（详见 [013 模块清单与怎么用](../013.topic-project-structure.md) §025）
- **注解式而非配置开关**：开关写在代码里（一眼看出装了哪些中心），且"默认关"才是真隔离
