-- 新增 源应用 ID
ALTER TABLE `config`
    ADD COLUMN `value_type` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '配置值类型';
