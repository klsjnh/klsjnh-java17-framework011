# klsjnh-java17-framework011

Java 17 **纯血 DDD** 技术底座 —— Maven 多模块工程，供第三方业务系统依赖引用。全新项目、无历史技术债；对外契约（响应信封 / 状态码 / 路由风格 / 公共表结构）保持稳定。

> AI 协作入口（门牌，先读）：[Agent.md](Agent.md) · 协议全集：[docs/011.agreements.md](docs/011.agreements.md) · 架构选型：[docs/infrastructure011/011.topic-infrastructure.md](docs/infrastructure011/011.topic-infrastructure.md) · 目录结构：[docs/infrastructure011/013.topic-project-structure.md](docs/infrastructure011/013.topic-project-structure.md)

## 技术栈

| | |
|---|---|
| Java | 17 (LTS) |
| Spring Boot | 3.4.5 |
| MyBatis-Plus | 3.5.9（spring-boot3-starter） |
| 数据库 | MySQL 8 · Druid 1.2.23（仅 dependencyManagement 声明，尚未接入模块） |
| 鉴权 | JJWT 0.12.6 |
| API 文档 | knife4j 4.5.0 (OpenAPI3) |
| 工具 | Lombok 1.18.36（domain 层禁用） |

## 架构总览

```
web ──► application ──► domain ◄── infrastructure
              │                        │
              └────────► common ◄──────┘   （common 被各层引用，不反向依赖）
```

| 层 | 职责 | 依赖 |
|----|------|------|
| common | 跨层契约：枚举、响应信封、共享 VO | 无 |
| domain | 聚合、值对象、仓储接口、Port | common 共享内核（纯 Java 枚举），禁框架 |
| application | 用例编排、事务边界 | domain + common |
| infrastructure | PO、Mapper、RepositoryImpl、技术适配 | domain + common |
| web | Controller、统一信封、全局异常、鉴权过滤器 | application + common |
| app | 唯一 main、装配、profile 配置 | 全部 |

## 模块

| 模块 | 层 | 内容 |
|------|-----|------|
| java17-common011 | common | FrameworkStatus011 / DatabaseType011 / HttpCodeEnum011 / Status011、Response011 + IdVo011、BusinessException、分页对、批量删除对、AuthAttribute011 |
| java17-domain011 | domain | shared（EntityId / AuditInfo）· iam（用户/角色 + 4 Port）· datasource / storage（设施 Port）· system011（menu / config / scheduler 聚合与仓储 + JobHandler） |
| java17-application011 | application | JulyScheduler / JulyUser / JulyRole 用例 |
| java17-infrastructure011 | infrastructure | 基座家族五层 + 业务持久化（system011：user/role/关联/审计/scheduler）+ IAM 适配器（bcrypt / JWT / 审计记录器） |
| java17-web011 | web | Controller / 全局异常 / GlobalAuthFilter（JWT 校验） |
| java17-app011 | app | 唯一 main + 配置 + 参考样板（demo11 纵切面 / Demo011Scheduler） |

## 快速开始

前置：JDK 17 · Maven 3.9+ · Node 18+（门禁脚本用）

```bash
mvn -o clean package -DskipTests          # 离线构建，产出 app011 可执行 jar
node tools/check-klsjnh-standards.mjs .   # 注释规范门禁（只读）
./script011.sh gate                       # 规范检查 + 编译，一键提交门禁
```

> 说明：`application.yml` 配置端口 11610 与默认 development profile（公共结构、无密钥）；数据源与 dev 用 krt 配置落在入库的 `application-development.yml`，本机差异走忽略的 `application-local.yml`；应用已在 11610 实测启动。

## API 契约要点

所有端点统一返回六键信封（JSON 键 **camelCase**，禁止改名/删除/重排）：

`statusCode` · `message` · `errorMessage` · `timestamp` · `traceId` · `data`

- `statusCode` 与 HTTP 传输状态码保持一致；`errorMessage` **仅 debug 态填充**
- 状态码常量：SUCCESS(200) / BAD_REQUEST(400) / UNAUTHORIZED(401) / FORBIDDEN(403) / NOT_FOUND(404) / ERROR(500)
- 完整契约（含新增常量的流程）：[docs/013.api-contract.md](docs/013.api-contract.md)

## 编码规范要点

- 文件头：`/* TypeName class ... @author ... */` 块，`package` 与头块之间空行，history 行用**小写描述**（如 `base po class`），门禁强制
- Javadoc 一律**英文**；`@Schema` 中文描述只出现在 web 层 VO；DDL 注释中文
- domain 层零依赖（纯 Java + record）；`@Transactional` 只出现在 application；PO 只活在 infrastructure
- 逻辑删除统一走 `dr`（'0' 正常 / '1' 已删除）；主键由用例显式调 `EntityId.generate()`（32 位无连字符 UUID，列宽 33）

完整编码规则（含门禁映射）：[docs/016.coding-standards.md](docs/016.coding-standards.md)

## 状态与路线

| 状态 | 项 |
|------|-----|
| ✅ | 六模块骨架 + 依赖铁律 |
| ✅ | PO 四件套 + 公共列 DDL 模板（[docs/sql](docs/sql/base-entity-columns.sql)） |
| ✅ | domain 内核值对象（EntityId / AuditInfo） |
| ✅ | 统一响应信封 + 状态码 + 契约文档 |
| ✅ | 编码规则门禁（正则 + AST，经 script011.sh 强制） |
| ✅ | 仓库基座家族（BaseRepository / 011 / Tree / Tree011 / MasterSub021，替代旧版含注入防护重设计） |
| ✅ | 审计时间列自动填充（AuditMetaObjectHandler） |
| ✅ | 启动配置（port 11610 / development 默认 / krt.status）——应用已在 11610 实测启动 |
| ✅ | 首个业务聚合全链路（021 julyScheduler：CRUD / 启停 / 执行一次，Quartz 内存模式） |
| ✅ | 审计操作人填充（JWT 请求属性 `AuthAttribute011.OPERATOR_ID` → create_by/update_by，无 OperatorContext） |
| ✅ | 全局异常处理器（BusinessException → 统一信封，HTTP 与 statusCode 同步） |
| ✅ | Swagger 分组（system011 / 存储中心 / 第三方 app 组条件装配）+ JobHandler 调度样板 + demo11 DDD 参考样板 |
| ✅ | 配置管理 july_config（运行时键值参数，每次查库，getByCode 程序读取入口） |
| ✅ | 组织机构 july_organization（树 CRUD / 层级维护与防环移动 / 人数角标 / 删除双约束） |
| ✅ | 平台数据导出（注册制 Provider + json/csv + 500 行上限 + EXPORT 审计）+ 登出端点 |
| ✅ | JWT 鉴权过滤器（GlobalAuthFilter，Authorization: Bearer，401 统一） |
| ✅ | 动态数据源（yaml ci011 懒加载 + SqlRoutingPort 读写/方言分页 clamp[10,500]） |
| ✅ | 存储中心（对象 + 桶 CRUD + stat 元数据，local011/minio011 双适配器 E2E，厂商适配器分期） |
| ✅ | production 数据源注入与部署验证 |

## 版本

`com.klsjnh:java17-framework011:1.0.0`（开发中）
