# Agent.md — AI 助手项目手册（Java17 纯血 DDD 技术底座）

> **面向对象**：接入 `klsjnh-java17-framework011` 仓库的 AI 助手 / 新开发者。

## 1. 项目是什么

- Java17 **纯血 DDD** 技术底座，供第三方业务系统 Maven 依赖引用；全新项目、无历史技术债
- 技术栈：Java 17 · Spring Boot 3.4.5 · MyBatis-Plus 3.5.9 · MySQL 8 · Druid · JJWT 0.12.6 · knife4j 4.5.0 · Lombok
- 版本统一在父 `pom.xml` 的 `dependencyManagement` 管理，子模块不许自带版本号
- 文档地图：架构选型与设计思路 → [docs/infrastructure011/011.topic-infrastructure.md](docs/infrastructure011/011.topic-infrastructure.md)；目录结构与命名 → [docs/infrastructure011/013.topic-project-structure.md](docs/infrastructure011/013.topic-project-structure.md)；API 契约 → [docs/016.api-contract.md](docs/016.api-contract.md)

## 2. 模块与依赖方向（DDD 分层）

| 模块 | 层 | 依赖 | 内容 |
|------|-----|------|------|
| java17-common011 | common | 无 | 跨层契约：三个枚举、Response011 + IdVo（后续：分页/异常/常量/IdUtil） |
| java17-domain011 | domain | **零依赖** | 纯内核：EntityId / AuditInfo（后续：聚合/值对象/仓储接口/Port） |
| java17-application011 | application | domain + common | 用例编排 + @Transactional（将落） |
| java17-infrastructure011 | infrastructure | domain + common | PO / Mapper / RepositoryImpl；BasePo 四件套已落 |
| java17-web011 | web | application + common | Controller / 统一信封 / 全局异常 / 鉴权过滤器（将落） |
| java17-app011 | app | 全部 | 唯一 main：Framework011Application |

依赖箭头：`web → application → domain ← infrastructure`；common 被各层引用，不反向依赖任何层。

## 3. 已落地清单

### PO 四件套（`com.klsjnh.infrastructure.persistence.entity`）

| PO | 字段 |
|------|------|
| BasePo | id · status · create_by · update_by · create_time · update_time · dr |
| BasePo011 | BasePo + sort_order |
| TreePo | BasePo + parent_id |
| TreePo011 | TreePo + parent_id + sort_order |

### domain 内核（`com.klsjnh.domain.shared`）

- `EntityId`：主键值对象（record），32 位无连字符 UUID，`generate()` / `of()`
- `AuditInfo`：审计值对象（record），对应 create_by / update_by / create_time / update_time

### 约定

- id：用例显式调用 `EntityId.generate()` 生成 32 位无连字符 UUID，列宽 33；BasePo 的 `ASSIGN_UUID` 仅在 id 为空时兜底，不是主生成路径（应用侧生成是多数据源架构下的一致性要求，主键约束兜底冲突）
- dr 逻辑删除：`@TableLogic`，'0' 正常 / '1' 已删除；树形根节点 parent_id 为空串
- sort_order 越小越靠前，默认 9999（建表默认值兜底，Java 侧不设初值）
- 审计四列由 MetaObjectHandler 配合 OperatorContext 自动填充（将落，接 JWT 登录上下文）
- DDL 模板：[docs/sql/base-entity-columns.sql](docs/sql/base-entity-columns.sql)
- API 契约（信封六键 + 状态码表）：[docs/016.api-contract.md](docs/016.api-contract.md)；JSON 键 camelCase，加状态码先扩契约文档

## 4. 工具与门禁

| 文件 | 类型 | 说明 |
|------|------|------|
| `tools/check-klsjnh-standards.mjs` | **检查** | 只读扫描 Java 注释规范（文件头/类与方法 Javadoc/空行/英文注释） |
| `tools/klsjnh-standards-lib.mjs` | 共享 | check / fix 共享逻辑 |
| `tools/fix-standards.mjs` | **修复** | 统一修复：文件头 + Javadoc + 格式；**仅本地手动，不进 hooks** |
| `tools/push-gate.sh` | **检查** | check + `mvn -o compile`（离线环境） |
| `tools/install-githooks.sh` | 辅助 | 一次性启用 `.githooks` |
| `tools/STANDARDS.md` | 文档 | tools 目录标准 |

提交门禁（失败不许 commit / push）：

```bash
node tools/check-klsjnh-standards.mjs .
bash tools/push-gate.sh          # 规范检查 + mvn 离线编译
```

## 5. 工作流

1. 收到需求 → 先读本手册与 `tools/STANDARDS.md`
2. 大改动先「代码先行审查」
3. 开发 → `mvn -o compile` → `node tools/check-klsjnh-standards.mjs .` → 自测
4. 只有用户明确说「提交/推送」才执行提交；**不跑 fix-standards**（自动修复会误改代码，只跑 check）

## 6. 红线（DDD）

- **domain 零依赖**：只允许纯 Java + record，禁 Lombok、禁 common、禁任何框架注解
- **@Transactional 只出现在 application**
- **PO 只活在 infrastructure**：application / web 不 import 任何 `Po` / `Mapper`
- RepositoryImpl 实现 domain 仓储接口；Controller 只注入 application 服务
- 构造器注入；业务表 PO 继承 BasePo 家族，逻辑删除一律走 dr，不物理删
- POST 为主、禁 @PathVariable；JSON 键 camelCase（与 docs/016 契约一致）
- 堆栈/SQL 不出站；error_message 仅 debug
- 注释规范（沿袭积累版代码）：文件头块后空行再接 import；Javadoc 一律英文，字段用单行 `/** ... */`；Swagger/DDL 注释用中文；实体/PO 层不挂 @Schema（中文描述放 web 层 VO）
