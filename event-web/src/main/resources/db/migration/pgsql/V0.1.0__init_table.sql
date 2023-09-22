-- ----------------------------
-- Table structure for client
-- ----------------------------
DROP TABLE IF EXISTS "public"."client";
CREATE TABLE "public"."client" (
    "id" int8 NOT NULL,
    "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "secret" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
    "status" int2 NOT NULL DEFAULT 1,
    "created_at" timestamp NOT NULL DEFAULT now(),
    "updated_at" timestamp,
    "is_deleted" bool NOT NULL DEFAULT false
);
ALTER TABLE "public"."client" ADD CONSTRAINT "pk_client" PRIMARY KEY ("id");

COMMENT ON COLUMN "public"."client"."id" IS 'ID';
COMMENT ON COLUMN "public"."client"."name" IS '名称';
COMMENT ON COLUMN "public"."client"."secret" IS '密钥';
COMMENT ON COLUMN "public"."client"."status" IS '状态：1.启用，4.停用';
COMMENT ON COLUMN "public"."client"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."client"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."client"."is_deleted" IS '是否删除';

-- ----------------------------
-- Table structure for client_grant
-- ----------------------------
DROP TABLE IF EXISTS "public"."client_grant";
CREATE TABLE "public"."client_grant" (
    "id" int8 NOT NULL,
    "client_id" int8 NOT NULL,
    "topic" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "operation" int2,
    "group" varchar(128) COLLATE "pg_catalog"."default",
    "created_at" timestamp NOT NULL DEFAULT now(),
    "updated_at" timestamp,
    "is_deleted" bool NOT NULL DEFAULT false,
    "status" int2 NOT NULL DEFAULT 1,
    "type" int2 NOT NULL,
    "source" varchar(255) COLLATE "pg_catalog"."default",
    "pattern_type" int2 NOT NULL DEFAULT 1,
    "host" varchar(255) COLLATE "pg_catalog"."default"
);
ALTER TABLE "public"."client_grant" ADD CONSTRAINT "pk_client_grant" PRIMARY KEY ("id");

COMMENT ON COLUMN "public"."client_grant"."topic" IS '事件编码';
COMMENT ON COLUMN "public"."client_grant"."operation" IS '操作';
COMMENT ON COLUMN "public"."client_grant"."group" IS '分组';
COMMENT ON COLUMN "public"."client_grant"."status" IS '状态：1.启用，4.停用';
COMMENT ON COLUMN "public"."client_grant"."type" IS '授权类型：1.生产，2.消费';
COMMENT ON COLUMN "public"."client_grant"."source" IS '来源标记';
COMMENT ON COLUMN "public"."client_grant"."pattern_type" IS '匹配类型：1.精确模式；2.前缀模式';
COMMENT ON COLUMN "public"."client_grant"."host" IS '授权主机';

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS "public"."event";
CREATE TABLE "public"."event" (
  "id" int8 NOT NULL,
  "code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "desc" varchar(255) COLLATE "pg_catalog"."default",
  "sort"       int4          null default 0,
  "creator_id" varchar(64) NULL,
  "modifier_id" varchar(64) NULL,
  "is_deleted" bool NOT NULL DEFAULT false,
  "updated_at" timestamp,
  "created_at" timestamp NOT NULL DEFAULT now(),
  "topic" varchar(128) COLLATE "pg_catalog"."default",
  "partition" int4 NOT NULL DEFAULT 1,
  "replications" int4 NOT NULL DEFAULT 1,
  "retention" int4 NOT NULL DEFAULT 30,
  "tags" varchar(1024) COLLATE "pg_catalog"."default",
  "app_code" varchar(128) COLLATE "pg_catalog"."default",
  "module_code" varchar(128) COLLATE "pg_catalog"."default",
  "message_count" int8 NOT NULL DEFAULT 0,
  "type" int2 NOT NULL DEFAULT 1,
  "private_id" varchar(128) COLLATE "pg_catalog"."default"
);
ALTER TABLE "public"."event" ADD CONSTRAINT "pk_event" PRIMARY KEY ("id");
CREATE INDEX "idx_event_code" ON "public"."event" USING btree (
   "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_event_module" ON "public"."event" USING btree (
    "app_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
    "module_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_event_private_id" ON "public"."event" USING btree (
                                                                     "private_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
    );

COMMENT ON COLUMN "public"."event"."name" IS '事件名称';
COMMENT ON COLUMN "public"."event"."desc" IS '事件描述';
COMMENT ON COLUMN "public"."event"."is_deleted" IS '是否删除';
COMMENT ON COLUMN "public"."event"."topic" IS '主题名称';
COMMENT ON COLUMN "public"."event"."partition" IS '副本数';
COMMENT ON COLUMN "public"."event"."replications" IS '分区数';
COMMENT ON COLUMN "public"."event"."retention" IS '数据有效期(天)';
COMMENT ON COLUMN "public"."event"."tags" IS '事件标签';
COMMENT ON COLUMN "public"."event"."app_code" IS '应用编码';
COMMENT ON COLUMN "public"."event"."module_code" IS '模块编码';
COMMENT ON COLUMN "public"."event"."message_count" IS '消息数量';
COMMENT ON COLUMN "public"."event"."type" IS '事件类型，1:公共事件，2:私有事件';
COMMENT ON COLUMN "public"."event"."private_id" IS '私有 ID';

-- ----------------------------
-- Table structure for event_data
-- ----------------------------
DROP TABLE IF EXISTS "public"."event_data";
CREATE TABLE "public"."event_data" (
   "id" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
   "data" bytea NOT NULL,
   "send_at" timestamp,
   "created_at" timestamp NOT NULL DEFAULT now(),
   "updated_at" timestamp,
   "is_deleted" bool NOT NULL DEFAULT false,
   "topic" varchar(255) COLLATE "pg_catalog"."default" NOT NULL DEFAULT ''::character varying
);
ALTER TABLE "public"."event_data" ADD CONSTRAINT "pk_event_data" PRIMARY KEY ("id");
COMMENT ON COLUMN "public"."event_data"."data" IS '事件实体';
COMMENT ON COLUMN "public"."event_data"."send_at" IS '发送时间';
COMMENT ON COLUMN "public"."event_data"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."event_data"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."event_data"."is_deleted" IS '是否删除';

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS "public"."app";
CREATE TABLE "public"."app" (
    "id" int8 NOT NULL,
    "type" int2 NOT NULL DEFAULT 1,
    "code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
    "name" varchar(255) COLLATE "pg_catalog"."default",
    "logo" varchar(255) COLLATE "pg_catalog"."default",
    "private_id" varchar(128) COLLATE "pg_catalog"."default",
    "sort"       int4      NOT NULL default 0,
    "created_at" timestamp NOT NULL DEFAULT now(),
    "updated_at" timestamp,
    "is_deleted" bool NOT NULL DEFAULT false
);
ALTER TABLE "public"."app" ADD CONSTRAINT "pk_app" PRIMARY KEY ("id");

COMMENT ON COLUMN "public"."app"."type" IS '应用类型：1、系统应用，2.自定义应用';
COMMENT ON COLUMN "public"."app"."name" IS '应用名称';
COMMENT ON COLUMN "public"."app"."logo" IS '应用 Logo';
COMMENT ON COLUMN "public"."app"."private_id" IS '私有 ID';
COMMENT ON COLUMN "public"."app"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."app"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."app"."is_deleted" IS '是否删除';


-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS "public"."group";
CREATE TABLE "public"."group"
(
    "id"         int8         NOT NULL,
    "app_code"   varchar(128) NOT NULL,
    "parent_id"  int8         NULL,
    "type"       int2         NOT NULL DEFAULT 1,
    "name"       varchar(255) COLLATE "pg_catalog"."default",
    "logo"       varchar(255) COLLATE "pg_catalog"."default",
    "sort"       int4         NOT NULL default 0,
    "created_at" timestamp NOT NULL DEFAULT now(),
    "updated_at" timestamp,
    "is_deleted" bool         NOT NULL DEFAULT false
);
ALTER TABLE "public"."group"
    ADD CONSTRAINT "pk_group" PRIMARY KEY ("id");
COMMENT ON COLUMN "public"."group"."app_code" IS '应用标识';
COMMENT ON COLUMN "public"."group"."parent_id" IS '父级 ID';
COMMENT ON COLUMN "public"."group"."type" IS '分组类型：1.分组，2.标签';
COMMENT ON COLUMN "public"."group"."name" IS '名称';
COMMENT ON COLUMN "public"."group"."logo" IS 'Logo';
COMMENT ON COLUMN "public"."group"."sort" IS '排序';
COMMENT ON COLUMN "public"."group"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."group"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."group"."is_deleted" IS '是否删除';


-- ----------------------------
-- Table structure for event_schema
-- ----------------------------
DROP TABLE IF EXISTS "public"."event_schema";
CREATE TABLE "public"."event_schema"
(
    "id"         int8         NOT NULL,
    "code"       varchar(128) NOT NULL,
    "schema"     text         NOT NULL,
    "created_at" timestamp NOT NULL DEFAULT now(),
    "updated_at" timestamp,
    "is_deleted" bool         NOT NULL DEFAULT false
);
ALTER TABLE "public"."event_schema"
    ADD CONSTRAINT "pk_event_schema" PRIMARY KEY ("id");
CREATE INDEX "idx_event_schema_code" ON "public"."event_schema" USING btree (
                                                                             "code" COLLATE "pg_catalog"."default"
                                                                             "pg_catalog"."text_ops" ASC NULLS LAST
    );
COMMENT ON COLUMN "public"."event_schema"."id" IS '事件 ID';
COMMENT ON COLUMN "public"."event_schema"."code" IS '事件编码';
COMMENT ON COLUMN "public"."event_schema"."schema" IS '事件 Schema';
COMMENT ON COLUMN "public"."event_schema"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."event_schema"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."event_schema"."is_deleted" IS '是否删除';

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS "public"."config";
CREATE TABLE "public"."config"
(
    "id"          int8         NOT NULL,
    "key"         varchar(128) NOT NULL,
    "value"       text         NOT NULL,
    "profile"     varchar(32)  NULL,
    "creator_id"  VARCHAR(64)  NOT NULL DEFAULT '',
    "modifier_id" VARCHAR(64)  NOT NULL DEFAULT '',
    "created_at"  timestamp    NOT NULL DEFAULT now(),
    "updated_at"  timestamp    NOT NULL DEFAULT now(),
    "is_deleted"  bool         NOT NULL DEFAULT FALSE
);
ALTER TABLE "public"."config"
    ADD CONSTRAINT "pk_config" PRIMARY KEY ("id");
COMMENT ON COLUMN "public"."config"."key" IS '配置 key';
COMMENT ON COLUMN "public"."config"."profile" IS '环境';
COMMENT ON COLUMN "public"."config"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."config"."updated_at" IS '修改时间';
COMMENT ON COLUMN "public"."config"."is_deleted" IS '是否删除 0.未删除 1.已删除';
COMMENT ON COLUMN "public"."config"."creator_id" IS '操作者 ID';
COMMENT ON COLUMN "public"."config"."modifier_id" IS '修改者 ID';
