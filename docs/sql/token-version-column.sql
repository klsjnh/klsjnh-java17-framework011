-- ============================================================
-- Token revocation: july_user.token_version (existing DBs)
-- 2026-09-26：JWT claim tv 与库内 token_version 比对；status 变更 /
--             改密 / 重置密码 / logout 时 bump，旧 token 校验失败。
-- 新库：july_core011.sql CREATE 已含 token_version，可跳过本脚本。
-- ============================================================

ALTER TABLE july_user
    ADD COLUMN token_version INT NOT NULL DEFAULT 0
        COMMENT 'JWT 凭证版本（改密/改状态/logout 递增；与 token claim tv 比对）'
        AFTER last_login_time;
