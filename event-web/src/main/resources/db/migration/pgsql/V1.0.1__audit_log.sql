DROP table IF exists "public"."audit_log";
create table "public"."audit_log"
(
    "id"             int8         NOT NULL,
    "operation_type" int2         NOT NULL,
    "resource_type"  int2         NOT Null,
    "resource_id"    int8         NOT NULL,
    "desc"           VARCHAR(255) NULL,
    "detail"         json         NULL,
    "result"         bool         NOT NULL,
    "user_agent"     varchar(128) NULL,
    "client_ip"      varchar(64)  NULL,
    "creator_id"     varchar(64)  NULL,
    "creator_name"   varchar(128) NULL,
    "created_at"     timestamp    NOT NULL DEFAULT now(),
    "updated_at"     timestamp    NULL,
    "is_deleted"     bool         NOT NULL DEFAULT false
);
COMMENT ON COLUMN "public"."audit_log"."creator_id" IS '操作者 ID';
COMMENT ON COLUMN "public"."audit_log"."creator_name" IS '操作者名称';
COMMENT ON COLUMN "public"."audit_log".client_ip IS '客户端 IP';
COMMENT ON COLUMN "public"."audit_log".user_agent IS 'userAgent';
COMMENT ON COLUMN "public"."audit_log"."resource_type" IS '资源类型: 1.应用 2.事件 3.客户端 4.授权关系 5.分组标签  ';
COMMENT ON COLUMN "public"."audit_log"."resource_id" IS '资源 ID';
COMMENT ON COLUMN "public"."audit_log"."operation_type" IS '操作类型: 1.新增 2.删除 3.编辑 4.状态更新';
COMMENT ON COLUMN "public"."audit_log"."desc" IS '日志描述';
COMMENT ON COLUMN "public"."audit_log"."detail" IS '审计详情';
COMMENT ON COLUMN "public"."audit_log"."result" IS '操作状态';
COMMENT ON COLUMN "public"."audit_log"."created_at" IS '创建时间';
COMMENT ON COLUMN "public"."audit_log"."updated_at" IS '更新时间';
COMMENT ON COLUMN "public"."audit_log"."is_deleted" IS '是否已删除';
ALTER TABLE "public"."audit_log"
    ADD CONSTRAINT "audit_log_pkey" PRIMARY KEY ("id");