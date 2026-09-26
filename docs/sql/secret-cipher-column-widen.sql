-- ============================================================
-- SecretCipher 列宽迁移（已有库执行一次）
-- 2026-09-26：密文格式 enc:v1:<base64(iv+cipher+tag)>，明文域上限不变，
--             库内列加宽到 VARCHAR(512) 以免写库截断。
-- 新库：CREATE 已含 512，可跳过本脚本。
-- 真源：july_center_ai011.sql / july_center_storage011.sql /
--       july_center_datasource011.sql
-- ============================================================

ALTER TABLE july_ai_model_provider_api
    MODIFY COLUMN api_key VARCHAR(512) NOT NULL
        COMMENT 'API Key（SecretCipher enc:v1:；出参不回显；明文域上限 300）';

ALTER TABLE july_storage_provider
    MODIFY COLUMN access_key VARCHAR(512) NULL
        COMMENT 'Access Key（SecretCipher enc:v1:；明文域上限 100）',
    MODIFY COLUMN secret_key VARCHAR(512) NULL
        COMMENT 'Secret Key（SecretCipher enc:v1:；出参打码 ******；明文域上限 300）';

ALTER TABLE july_datasource
    MODIFY COLUMN password VARCHAR(512) NULL
        COMMENT '密码（SecretCipher enc:v1:；出参不回显；明文域上限 300）';
