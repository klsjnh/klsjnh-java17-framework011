# 2026-09-26 — 工作日志（续：接口一致性 P0/P1 全修）

> 同日另有 [审计核对](2026-09-26-audit-reconciliation.md)、[AUTH-2 / SecretCipher B3](2026-09-26.md)。本篇记录静态核对 + WSL 实打矩阵后的缺陷收敛。

## 任务

| # | 项 | 状态 | 说明 |
|---|----|------|------|
| 1 | **消息渠道 config 明文（corpSecret 等）** | ✅ | `ChannelConfigCipher011` 敏感键扩展 + 后缀匹配；mask / encrypt / merge 同步生效 |
| 2 | **存储 update 回传 ****** 污染** | ✅ | `JulyStorageProvider.update`：accessKey/secretKey 的 mask 与 blank 均保留旧值 |
| 3 | **downloadObject(Stream) 缺鉴权** | ✅ | 传入 operator + `assertHas(SELECT)`；`presignObjectUrl` 同步 |
| 4 | **缺参 500 → 400** | ✅ | `MissingServletRequestParameterException` / `MultipartException` → 信封 400 |
| 5 | **规范 gate 5 处** | ✅ | control-blank / import-order / method-javadoc |
| 6 | **文档漂移** | ✅ | getByCode 无 HTTP；ai011→aicenter；demo11 vs demo011；033/storage/message/audit/工作日志 |
| 7 | **开发态加固（不关 debug）** | ✅ | debug 启动醒目 WARN；yml `${ENV:default}` + `.env.example` |

## 仍故意保留（开发态已知风险）

- `krt.status=debug`：认证 Filter 不拒、`assertHas` no-op（本地体验优先；production profile 卫兵仍在）。
- development yml 默认值仍可跑通本地；**真正生产**须切 production + 环境注入密钥。
- Export/Import 平台总线未在各 ExportProvider 内重复 `assertHas`（有意）；`logicDeleteBatch` / `selectByPage` vs `selectListByPage` 命名差异有意不做「齐」。

## 验证

- 单测：`ChannelConfigCipher011Test` · `JulyStorageProviderSecretMaskTest` · `GlobalExceptionHandlerValidationTest` 等
- `node tools/check-klsjnh-standards.mjs` ERROR 0
- WSL `:11160` 可复探针时：渠道 list 无明文 secret；download 无 token 在 production 应 401
