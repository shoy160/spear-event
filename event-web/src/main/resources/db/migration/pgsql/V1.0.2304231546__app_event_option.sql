-- 应用 & 事件 配置
ALTER TABLE "public"."app"
    ADD COLUMN "options" int4 NOT NULL DEFAULT 0;
COMMENT ON COLUMN "public"."app"."options" IS '应用配置：1.可编排';
ALTER TABLE "public"."event"
    ADD COLUMN "options" int4 NOT NULL DEFAULT 1;
COMMENT ON COLUMN "public"."event"."options" IS '事件配置：1.可编排';
UPDATE "public"."app" SET "options" = 1 WHERE "code" IN ('authing','dingtalk','xinrenxinshi','ad','ldap','wechatwork','lark');
