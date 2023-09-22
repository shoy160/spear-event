DROP table IF exists audit_log;
create table audit_log
(
    id             bigint unsigned     NOT NULL PRIMARY KEY COMMENT 'ID',
    operation_type tinyint unsigned    NOT NULL COMMENT '操作类型  1.新增 2.删除 3.编辑 4.状态更新 ',
    resource_type  tinyint unsigned    NOT Null COMMENT '资源类型 1.应用 2.事件 3.客户端 4.授权关系 5.分组标签 ',
    resource_id    bigint unsigned     NOT NULL COMMENT '资源 ID',
    `desc`         VARCHAR(128)        NULL COMMENT '日志描述',
    detail         json                NULL COMMENT '审计详情',
    result         tinyint(1) unsigned NOT NULL default 0 COMMENT '操作结果',
    user_agent     VARCHAR(255)        NULL COMMENT 'userAgent',
    client_ip      VARCHAR(64)         NULL COMMENT '客户端 IP',
    creator_id     VARCHAR(64)         NULL COMMENT '操作者 ID',
    creator_name   VARCHAR(128)        NULL COMMENT '操作者名称',
    created_at     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at     timestamp           NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted     tinyint(1) unsigned NOT NULL DEFAULT 0 COMMENT '是否删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC;