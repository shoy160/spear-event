-- 新增 源应用 ID
ALTER TABLE "public"."config"
    ADD COLUMN "value_type" VARCHAR(512) NOT NULL DEFAULT '';
COMMENT ON COLUMN "public"."config"."value_type" IS '配置值类型';
