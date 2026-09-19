-- ============================================================
-- 逻辑删除 + 唯一键 根治：唯一索引只约束存活行（dr='0'）
-- 原理：新增生成列 alive_key，dr='0' 时 = 业务键，dr='1' 时 = NULL；
--       UNIQUE 建在生成列上 —— MySQL 允许多个 NULL，故：
--         · 存活行业务键仍唯一；
--         · 逻辑删除后墓碑（多个）不再挡重插；
--         · 墓碑保留（不物理删，符合逻辑删除语义）。
-- 设计：docs/requirement011/037（无关）；缺口来源：docs/019.backend-api-review.md 缺口 #2
-- 说明：ALTER 为增量（加生成列 + 换唯一索引），不改业务列、不丢数据。
-- ============================================================

-- 单列唯一键：生成列 = 业务列
ALTER TABLE july_config
    ADD COLUMN alive_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', code, NULL)) STORED,
    DROP INDEX uk_code,
    ADD UNIQUE KEY uk_code (alive_code);

ALTER TABLE july_menu
    ADD COLUMN alive_menu_code VARCHAR(30) GENERATED ALWAYS AS (IF(dr = '0', menu_code, NULL)) STORED,
    DROP INDEX uk_menu_code,
    ADD UNIQUE KEY uk_menu_code (alive_menu_code);

ALTER TABLE july_organization
    ADD COLUMN alive_org_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', org_code, NULL)) STORED,
    DROP INDEX uk_org_code,
    ADD UNIQUE KEY uk_org_code (alive_org_code);

ALTER TABLE july_role
    ADD COLUMN alive_role_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', role_code, NULL)) STORED,
    DROP INDEX uk_role_code,
    ADD UNIQUE KEY uk_role_code (alive_role_code);

ALTER TABLE july_user
    ADD COLUMN alive_user_account VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', user_account, NULL)) STORED,
    DROP INDEX uk_user_account,
    ADD UNIQUE KEY uk_user_account (alive_user_account);

ALTER TABLE july_datasource
    ADD COLUMN alive_ds_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', ds_code, NULL)) STORED,
    DROP INDEX uk_ds_code,
    ADD UNIQUE KEY uk_ds_code (alive_ds_code);

ALTER TABLE july_scheduler
    ADD COLUMN alive_scheduler_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', scheduler_code, NULL)) STORED,
    DROP INDEX uk_scheduler_code,
    ADD UNIQUE KEY uk_scheduler_code (alive_scheduler_code);

ALTER TABLE july_dictionary
    ADD COLUMN alive_dictionary_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', dictionary_code, NULL)) STORED,
    DROP INDEX uk_dictionary_code,
    ADD UNIQUE KEY uk_dictionary_code (alive_dictionary_code);

ALTER TABLE july_ai_model_provider
    ADD COLUMN alive_provider_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', provider_code, NULL)) STORED,
    DROP INDEX uk_provider_code,
    ADD UNIQUE KEY uk_provider_code (alive_provider_code);

ALTER TABLE july_storage_provider
    ADD COLUMN alive_storage_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', storage_code, NULL)) STORED,
    DROP INDEX uk_storage_code,
    ADD UNIQUE KEY uk_storage_code (alive_storage_code);

-- 复合唯一键：生成列 = CONCAT_WS('#', 各列)
ALTER TABLE july_dictionary_item
    ADD COLUMN alive_key VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, item_code), NULL)) STORED,
    DROP INDEX uk_pk_mt_item_code,
    ADD UNIQUE KEY uk_pk_mt_item_code (alive_key);

ALTER TABLE july_ai_model_provider_api
    ADD COLUMN alive_key VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, api_code), NULL)) STORED,
    DROP INDEX uk_pk_mt_api_code,
    ADD UNIQUE KEY uk_pk_mt_api_code (alive_key);

ALTER TABLE july_storage_provider_bucket
    ADD COLUMN alive_key VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, bucket_code), NULL)) STORED,
    DROP INDEX uk_pk_mt_bucket_code,
    ADD UNIQUE KEY uk_pk_mt_bucket_code (alive_key);

ALTER TABLE july_role_permissions
    ADD COLUMN alive_key VARCHAR(300) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, pk_menu, permission_code), NULL)) STORED,
    DROP INDEX uk_role_menu_code,
    ADD UNIQUE KEY uk_role_menu_code (alive_key);

ALTER TABLE july_user_role
    ADD COLUMN alive_key VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, pk_role), NULL)) STORED,
    DROP INDEX uk_user_role,
    ADD UNIQUE KEY uk_user_role (alive_key);

ALTER TABLE july_demo011
    ADD COLUMN alive_code VARCHAR(30) GENERATED ALWAYS AS (IF(dr = '0', code, NULL)) STORED,
    DROP INDEX uk_code,
    ADD UNIQUE KEY uk_code (alive_code);
