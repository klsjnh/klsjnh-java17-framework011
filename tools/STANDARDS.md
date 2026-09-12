# tools 目录标准

> 适用项目：`klsjnh-java17-framework011`（Java17 技术底座）

## 原则

- **两类脚本，职责分开**：检查（gate）与修复（fix）分开；修复**永不**进入 git hooks。
- **Node.js only**：不保留 PowerShell 一次性迁移脚本。
- **不合并不存在的东西**：每次规范演进改脚本本体，不加 deprecated 别名层。

## 目录清单

| 文件 | 类型 | 说明 |
|------|------|------|
| `check-klsjnh-standards.mjs` | **检查** | 只读扫描 Java 注释规范 |
| `klsjnh-standards-lib.mjs` | 共享 | check / fix 共享逻辑 |
| `fix-standards.mjs` | **修复** | 统一修复：文件头 + 类/方法 Javadoc + 格式 |
| `push-gate.sh` | **检查** | check + `mvn compile`（Java17/离线环境） |
| `install-githooks.sh` | 辅助 | 一次性启用 `.githooks` |
| `STANDARDS.md` | 文档 | 本文档 |

## 1. 检查（push 门禁）

检测失败**不允许 commit / push**：

```bash
node tools/check-klsjnh-standards.mjs .
```

| 步骤 | 脚本 | 失败结果 |
|------|------|----------|
| 规范 | `check-klsjnh-standards.mjs` | exit 1 |
| 编译 | `mvn compile`（经 `push-gate.sh`） | exit 1 |
| commit/push | 仅上面全部通过 | 否则 BLOCKED |

Git hooks（`bash tools/install-githooks.sh`）：

| Hook | 行为 |
|------|------|
| `pre-commit` | 仅规范检查，失败禁止 commit |
| `pre-push` | 完整 push-gate，失败禁止 push |

### 检查项

- 文件头 `/* TypeName class ... @author xiangrkrs@163.com */`
- `package` 与文件头块之间必须空行
- 文件头 ` */` 到 `import` 之间空行
- **modify history 行必须是小写描述**（如 `base entity class`），禁止直接写类名
- 类/接口 Javadoc
- **类 Javadoc 与类型声明（注解 / `class` 行）之间必须空行**
- **Javadoc 一律英文**（块内禁止中文；中文描述放 `@Schema` / DDL 注释）
- 方法 Javadoc（`@Override` 的为 `{@inheritDoc}`）
- 无连续重复 Javadoc
- Controller **`@*Mapping` 禁止 `/{param}` 路径参数**（用 Query 或 JSON Body）
- 方法内逻辑块之间必须有空行（变量声明→逻辑→返回）
- **013.016**：`if`/`else` 块内紧凑；方法体 / `for` 等逻辑块之间空行（见 `015`）
- **021**：日志格式禁止 `: {}` / `= {}` 键值占位符（`logger.xxx("a {} b {}", ...)`，用空格分隔）

## 2. 修复（仅本地手动）

```bash
node tools/fix-standards.mjs .
node tools/check-klsjnh-standards.mjs .
```

顺序：fix → check。**不得**写入 git hooks。

## 3. 新增 / 修改 Java 后

**`mvn compile` 通过 → 可以 push**。以下在每次新增或大批量修改 Java 文件后执行：

```bash
mvn compile -DskipTests
node tools/fix-standards.mjs .
node tools/check-klsjnh-standards.mjs .
```

## 新增脚本规则

- 检查类：必须只读，可进 `push-gate.sh`
- 修复类：必须改文件，文件名以 `fix-` 开头，**禁止**进 gate
- 一次性迁移/拆分：用完即删，不进仓库长期留存
