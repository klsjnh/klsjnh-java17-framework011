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
| [infrastructure011/](infrastructure011/) | 整体底层架构设计：011 架构选型 · 013 目录结构 · 015 配置体系 · 016 持久化体系 · 017 动态数据源 · 018 IAM 总设计 · 019 存储中心 · 020 容器化部署 · 021 低代码前端对接指南 · 022 AI 低代码机制 |
| [lowcode011/](lowcode011/) | **低代码知识库（2026-09-18 中收敛，20→10；旧号 017/018/019/022/023/024/025/026 已退役不回收）**：011 体系总览 · 013 契约与数据模型（MetadataContent / JulyMetadata / MetaDTO） · 015 字段体系（FieldType011 逐个 + 字典字段） · 016 展示与服务 · 020 设计器 · 021 发布·数据同步·运行时·开放 API·元数据模板 · 027 低代码核心（业务规则） · 028 设计器与运行时（业务规则） · 029 元数据模板（业务规则） · 030 引擎重构（042 落地记录） |
| [sql/](sql/) | DDL 唯一真源（base-entity-columns.sql 公共列模板 + 各 july_*.sql） |
| requirement011/ | 业务设计：011 菜单 · 013 组织 · 015 用户 · 016 角色 · 021 julyScheduler（已编码）· 029 配置管理 · 030 数据导出 · 031 数据源管理（新域 dataservice011，已编码）· 033 业务建模（已编码）· 035 数据字典（已编码）· 036 AI 模型接入（新域 ai011，已编码）· 037 存储中心管理面（已编码）；（低代码 038/039/040 业务规则已迁至 [lowcode011/](lowcode011/)） |
| requirement013/ | 技术方案（021 julyScheduler 已编码完成；029 配置管理已编码；031 dataservice011 已编码；033 业务建模已编码；035 数据字典已编码；036 ai011 已编码；037 存储中心管理面已编码；038 低代码核心已编码；039 低代码设计器与运行时已编码；040 元数据模板已编码；IAM 各主题按 011→013→编码 推进） |
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
| 017 | 在用 | **顶层** `017.tech-debt-redlines.md`（技术债红线，2026-09-14 落盘）；**专题目录** infrastructure011/017 动态数据源（2026-09-15 修订：表驱动为运行时真源）；julyScheduler 主题对已迁移至 021 |
| 018 | 在用 | infrastructure011/018 IAM 总设计；july-menu 主题已迁移至 011 |
| 019 | 在用 | **顶层** `019.backend-api-review.md`（后端接口质量评审，2026-09-15 已落盘）；**专题目录** infrastructure011/019 存储中心；july-user 主题已迁移至 015 |
| 020 / 021 | 已分配后撤销 | 原独立主题已并入 019（号不回收，永不复用） |
| 021 | 在用 | julyScheduler 主题对（requirement011/013，自 017 迁移） |
| 022 | 已迁移 | july-role 主题已迁移至 016（号不回收） |
| 023 | 已分配后撤销 | 原角色用户独立主题已并入 022 / 019 |
| 025 | 已分配后撤销 | 原角色权限独立主题已并入 022 |
| 026 | 已迁移 | july-organization 主题已迁移至 013（号不回收） |
| 027 / 028 | 已分配后撤销 | 原 requirement011 主题，内容移入 infrastructure011/017、019（号不回收） |
| 029 | 在用 | july-config（requirement011/013，配置管理） |
| 030 | 在用 | export（requirement011/013，平台导出功能） |
| 031 | 在用 | **dataservice011 新域**（requirement011/013 数据源管理，2026-09-15 落盘；DDL `sql/july_datasource.sql`）。本期范围：`july_datasource` 表驱动 + CRUD + 测试连接（**已落码：编译 SUCCESS / 门禁 0 违规 / 9 端点**）；`july_sql`/`july_model` 由 033 取代 |
| 033 | 在用 | **业务建模**（requirement011/013，2026-09-15 **已编码**；DDL `sql/july_business_modeling.sql`）。表 `july_business_modeling`（合并老项目 `july_sql` + `july_model`），归属 `dataservice011`；**产物 = `MetaData011` + `List<FieldInfo011>` 的一份 JSON（`metaData` + `fieldData` 两键，没有别的）**，除公共列 + `object_name` + `sql_content` 外整体存 `model_data` 一列（两列均 `TEXT`，2026-09-15 由 `MEDIUMTEXT` 定案下调）；**一期 = CRUD + SQL 探针推断 + 执行 SQL + 分页执行 SQL**（11 端点）；后期做「抽取同步数据到本地库」。机制：`july_datasource` → `july_business_modeling` → **lowcode011 域** → 低代码操作表 |
| 035 | 在用 | **数据字典**（requirement011/013，2026-09-15 **已编码**；DDL `sql/july_dictionary.sql`）。主子表 `july_dictionary` + `july_dictionary_item`，归属 `system011`；主表/子表都带 `sort_order`（BasePo011）；明细字段 code/label/sort/status/remark；程序读入口 `getByType`/`getLabel`/`isValidItem`（无 HTTP）；接 030 导出（`julyDictionary`）；**不加缓存**；10 端点（含 `/export`） |
| 036 | 在用 | **AI 模型接入（新域 ai011）**（requirement011/013，2026-09-15 落盘并**已编码**；DDL `sql/july_ai_model_provider.sql`）。主子表 `july_ai_model_provider` + `july_ai_model_provider_api`，归属新顶层域 `ai011`；主表/子表都带 `sort_order`（BasePo011）；`api_key` 出参不回显（类型层剔除），明文入库、加密二期；程序读入口 `getByCode`/`getApiList`/`getDefaultApi`；提供商级 + 密钥级 testConnection；**12 端点**（含 `/export`，Swagger 组 `ai011`） |

| 037 | 在用 | **存储中心管理面 + 在线编辑**（requirement011/013，2026-09-16 **已编码并 E2E**）。`july_storage` 表驱动多实例（`StorageResolver011` + `ObjectStorageFactory011`，适配器由 bean 改按实例配置造）+ 实例 CRUD/testConnection + 桶 CRUD + 对象 列表/分页/stat/上传/下载/删除/批删/预签名 + **在线文本编辑（readText/saveText + `EditableTextPolicy`，sql/markdown，≤1MB）** + yaml 播种（`StorageSeed011`）。端点前缀 `/klsjnh/storagecenter/julyStorage/v1`（实例+桶）· `/klsjnh/storagecenter/julyObject/v1`（对象）（2026-09-17 包名收敛 `storagecenter`、三控制器并为两模块，并对齐老 f016：对象分页带元数据、桶列表 `{bucketName,creationDate}`、`getBucket` `{bucketName,exists}`、`testConnection` 带 `bucketCount+basePath|endpoint`、实例 AK/SK 打码 + `secure` "1"/"0"）；AK/SK 出参打码 `******`。默认桶由适配器配置声明（`local011.default-bucket: backup011` → 落 `base-path/backup011`，`backup011` 读它，空则回退实例行） |
| 038 | 在用 | **低代码核心（一主三子）**（业务规则 [lowcode011/027](lowcode011/027.topic-lowcode-core.md) · 技术方案 requirement013/038；2026-09-16 **已编码并 E2E**；DDL `sql/july_metadata.sql`）。`july_metadata` + `july_metadata_field/_display/_service`，归属 `lowcode011` 域；四表都带 `sort_order`；**仅 CRUD**（7 端点）；主子表**整替**（子行物理删）+ 级联逻辑删除；原「不改 033 依赖的 `model` 值对象」约束已被 042 解除（该族删除，统一 `MetadataContent`） |
| 039 | 在用 | **低代码设计器与运行时**（业务规则 [lowcode011/028](lowcode011/028.topic-lowcode-designer-runtime.md) · 技术方案 requirement013/039；2026-09-17）。在 038 之上补「设计 → 发布建表（**DDL 执行**）→ 数据同步 → 运行时 → 开放 API」。**已完成**：DDL 增量（`july_metadata` 发布态列 + `july_metadata_version` + `july_metadata_open_api`）；设计 `listModels/load/save/previewDdl`；发布 `publish`（首建 `CREATE` / 后仅 `ADD COLUMN` / 快照 / 版本 / 开关 `krt.lowcode.ddl-execute.enabled` 默认关）；数据 `importDataFromSql/importStatus`（分页 → 按业务键 `businessField` upsert，**幂等**）；开放 API `openApiConfig/saveOpenApiConfig/rotateApiKey` + `/klsjnh/open/v1`（api_key 公共 CRUD + meta，JWT 白名单）；运行时 `publishMenu/listRuntimeMenus` + `/klsjnh/lowcode011/runtime/v1` 动态 CRUD（含元数据 required/类型/长度校验）。**约定**：业务键 `businessField` 固定 `VARCHAR(33)`（口径见 [045](requirement011/045.topic-business-field.md)）；外键列 `pk_xxx` 固定 `VARCHAR(33)`；业务字段映射平台基列（B 口径，导入 SQL 带列即可）。**挂起/低优先（2026-09-17 用户口径：价值有限，保持现状、不再扩展）**：动态 `objectCode` 审计、运行时权限模型、真字段映射、开放 API 限流、`syncData`、模板批量 `templates[]`、`/runtime` 运行页（前端）。端点 `/klsjnh/lowcode011/julyMetadata/v1`；E2E 用例 `bpm011.tuser → bpm_user011`（物理表同名，4579 行） |

| 040 | 在用 | **元数据模板导入导出**（业务规则 [lowcode011/029](lowcode011/029.topic-metadata-template.md) · 技术方案 requirement013/040；2026-09-17 **已实现并 E2E**）。自包含 JSON 模板（1 主 3 子 + `source` + `templateVersion`，`_guide` 代注释）：`GET /getTemplate011`（默认骨架）、`GET /downloadTemplate011?objectName=`（导出）、`POST /uploadTemplate011`（multipart 单文件或 JSON，**审核**零副作用 → `{valid,errors,plan,previewDdl}`）、`POST /deployObject`（`{template|objectName, overwrite?}` → 落库 + 发布）。校验：子 code 大小写不敏感唯一（基列允许一次并归一类型）、`businessField` 非基列且固定 `VARCHAR(33)`、`pk_*` 固定 `VARCHAR(33)`、枚举、display 绑定。**不新增表、复用 038/039** |

| 041 | 在用 | **AI 模型调用（chat）**（requirement011/013，2026-09-17 **已实现并 E2E**）。`ai011` 域新增调用能力：传 `provider`（**id 或 code**）+ `api`（**id 或 code**，缺省默认启用密钥）+ `model`（缺省取 `provider.models` 首个）+ `messages` → 模型回复；OpenAI 兼容、非流式；出参不含 apiKey；调用审计。端点 `/klsjnh/ai011/julyAiChat/v1/chat`；供平台内部（**AI 自开发**）与 HTTP。示例：`longcat` |
| 042 | 在用 | **低代码内核重构（元模型收口 + 内核抽取）**（评审记录 [lowcode011/030](lowcode011/030.topic-engine-refactor.md) · 技术方案 requirement013/042；2026-09-17 **已完成并 E2E**（S1 + 期一~期四全部））。四期：① 契约化 —— `MetaData011` 族降格 record 契约（更名 `MetadataContent`，JSON 键不变），`JulyMetadata` **组合**契约并删自有三套子实体（**显式解除 038「不改 model/ 值对象」约束**，033 侧 6 文件联动）；② 内核 —— `ObjectTablePolicy`（纯策略）+ `ObjectTableGateway`，消灭 Runtime/OpenApi/DataSync 三处逐行重复；③ 边界定型（`ObjectQueryCommand`/`RowView`，`meta`→`getMeta`）；④ 横切裁剪版（`CurrentOperatorPort`、审计基列入网关、`DateUtil011` 归口 9 处、`@Value`→`KrtConfig011`、开放 API NPE→401、pageSize clamp [1,500]）。**不改表、不改端点形状**；039 挂起项不翻案 |

| 043 | 在用 | **多入口建模与数据同步**（requirement011/013，2026-09-18 **P1+P2 部分实现**）。**已落地**：`ModelSourcePort` + `ModelingIntakeUseCase` + 适配器 `copy`/`template`/`sql`/`modeling`/`ai`（**一句话开发**，E2E）；端点 `/klsjnh/lowcode011/intake/v1/{review,create}`。`table` 适配器与 P3（多来源同步/mock/checkpoint）/P4（方言/权限）未做。**一个引擎、N 个门**：`sql` / `table` / `template` / `ai`（自然语言）/ `modeling`（033 交接）/ `copy` 等入口 → 统一契约 `MetadataContent`（030 共享契约）→ 审核 → 发布 → 数据同步（含 mock 造数 / source 回放 / 异表直拷 / checkpoint）。入口 = 源适配器（`ModelSourcePort`），下游唯一。依赖 030/039/040/041。**配套**：前端 SSR 按 `meta`（MetaDTO）动态渲染表单/列表（低代码运行时契约） |

| 045 | 在用 | **业务字段 `businessField` 口径**（[req011/045](requirement011/045.topic-business-field.md) · [req013/045](requirement013/045.topic-business-field.md)；2026-09-18 **已裁决·已实现并 E2E**）。**规则一条**：除 **AI** 外所有建模场景**必须指定 `businessField`**（`string(33)` + `UNIQUE KEY` + 同步去重依据）；**AI 例外**（缺 → 回退 `id`）；业务键非空**仅代码校验、不加 DB 约束**；**`sid` 不讨论**；**取消「已存在的表」场景（原场景 8）** |

| 047 | 已作废 | **字段对照器（sourceColumn ↔ targetColumn）提案**（[requirement011/047](requirement011/047.topic-field-mapper.md)，2026-09-18）。随 [045] 收口**取消**：不做「已存在的表」（场景 8），字段对照器无驱动场景；保留备查 |

| 050 | 在用 | **低代码模板多格式导入导出**（[req011/050](requirement011/050.topic-template-formats.md) · [req013/050](requirement013/050.topic-template-formats.md)；2026-09-18 **已裁决·P1 已实现并 E2E**（JSON/CSV/Markdown；XLSX 占位））。模板是**低代码模块的一部分**（不独立域）、**只为导入后发布、不落库**；格式 JSON（现有）/ **CSV（分段单文件，A 方案）** / **Markdown（分段表格）** / XLSX（占位后做）；任意格式解析成 MetaDTO 后复用现有 validate/review/deploy；非 AI 必须有 `businessField`（045） |

**下一可用编号：051。**（044/046/048/049 含 4，跳过）

> ✅ 2026-09-14 已办：① 表中 `016` 原有两行已合并为一行（原重复行信息并入）；③ 顶层常驻文档已按新版协议改名 —— `016.api-contract`→`013.api-contract`、`013.project-info`→`015.project-info`、`015.coding-standards`→`016.coding-standards`（编号不释放、不复用）。
> ✅ 2026-09-15 已办：② `019.backend-api-review.md` 已落盘（后端接口质量评审：Swagger 可信度 / 鉴权口径 / 已知缺口 / 新端点自检清单）并登记台账。
> 📌 **号位口径（用户裁定 2026-09-14）：跨目录不算同号位。** 顶层文档号与专题目录号**各自成位**，故 `017.tech-debt-redlines.md` 与 `infrastructure011/017.topic-dynamic-datasource.md` **不构成冲突**，无需避让；`019.backend-api-review.md` 与 `infrastructure011/019.topic-storage-center.md` 同理。判重只在**同一目录、同一序列**内进行。
