SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app`
(
    `id`         bigint UNSIGNED     NOT NULL,
    `type`       tinyint UNSIGNED    NOT NULL DEFAULT 0 comment '应用类型：1.系统应用，2.自定义应用',
    `code`       varchar(128)        NOT NULL DEFAULT '' comment '应用编码',
    `name`       varchar(255)        NOT NULL DEFAULT '' comment '应用名称',
    `logo`       varchar(255)        NULL comment '应用logo',
    `private_id` varchar(128)        NULL comment '私有 ID',
    `sort`       int UNSIGNED        NOT NULL DEFAULT '0' comment '排序值',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at` datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' comment '是否删除'
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for client
-- ----------------------------
DROP TABLE IF EXISTS `client`;
CREATE TABLE `client`
(
    `id`         bigint UNSIGNED     NOT NULL,
    `name`       varchar(64)         NOT NULL comment '名称',
    `secret`     varchar(64)         NOT NULL comment '秘钥',
    `status`     tinyint unsigned    NOT NULL DEFAULT '1' comment '状态： 0.关闭 1.开启',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at` datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' comment '是否删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for client_grant
-- ----------------------------
DROP TABLE IF EXISTS `client_grant`;
CREATE TABLE `client_grant`
(
    `id`           bigint UNSIGNED     NOT NULL,
    `type`         tinyint UNSIGNED    NOT NULL comment '授权类型 1.发布 2.订阅 3.发布订阅',
    `client_id`    bigint unsigned     NOT NULL comment '客户端 ID',
    `topic`        varchar(128)        NOT NULL comment '事件主题',
    `operation`    tinyint UNSIGNED    NOT NULL DEFAULT 0 comment '操作类型 1.创建 2.写 3.读',
    `group`        varchar(128)        NULL comment '授权分组',
    `status`       tinyint UNSIGNED    NOT NULL DEFAULT '1' comment '状态 1.启用 4.停用',
    `source`       varchar(255)        NULL comment '授权源',
    `pattern_type` tinyint UNSIGNED    NOT NULL DEFAULT '1' comment '匹配类型:1.精确模式 2.前缀模式',
    `host`         varchar(255)        NOT NULL DEFAULT '*' comment '主机',
    `created_at`   datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at`   datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`   tinyint(1) UNSIGNED NOT NULL DEFAULT '0' comment '是否删除 0.未删除 1.已删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`
(
    `id`          bigint UNSIGNED     NOT NULL,
    `key`         varchar(128)        NOT NULL comment '配置key',
    `value`       text                NOT NULL comment '配置值',
    `profile`     varchar(32)         NULL comment '环境',
    `creator_id`  varchar(64)         NULL comment '操作者 ID',
    `modifier_id` varchar(64)         NULL comment '操作者姓名',
    `created_at`  datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at`  datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted`  tinyint(1) UNSIGNED NOT NULL DEFAULT '0' comment '是否删除 0.未删除 1.已删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event`
(
    `id`            bigint UNSIGNED  NOT NULL,
    `code`          varchar(128)     NOT NULL comment '事件编码',
    `name`          varchar(128)     NOT NULL comment '事件名称',
    `desc`          varchar(255)     NULL comment '事件描述',
    `topic`         varchar(128)     NULL comment '事件主题',
    `partition`     int unsigned     NOT NULL DEFAULT '1' comment '分区数 默认: 1',
    `replications`  int unsigned     NOT NULL DEFAULT '1' comment '副本数 默认: 1',
    `retention`     int unsigned     NOT NULL DEFAULT '30' comment '有效时间，默认 30天',
    `tags`          varchar(1024)              NULL comment '标签组',
    `app_code`      varchar(128)               NULL comment '应用编码',
    `module_code`   varchar(128)               NULL comment '模块编码',
    `message_count` bigint UNSIGNED  NOT NULL DEFAULT '0' comment '消息总数',
    `type`          tinyint UNSIGNED NOT NULL DEFAULT '1' comment '事件类型: 1.公共事件 2.私有事件',
    `private_id`    varchar(128)     NULL comment '私有 ID（用户池ID）',
    `sort`          int UNSIGNED     NOT NULL DEFAULT '0' comment '排序值',
    `creator_id`    varchar(64)      NULL comment '操作者 ID',
    `modifier_id`   varchar(64)      NULL comment '操作者姓名',
    `created_at`    datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at`    datetime         NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted`    tinyint(1)       NOT NULL DEFAULT '0' comment '是否删除 0.未删除 1.已删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_data
-- ----------------------------
DROP TABLE IF EXISTS `event_data`;
CREATE TABLE `event_data`
(
    `id`         varchar(128)        NOT NULL,
    `topic`      varchar(255)        NOT NULL DEFAULT '' comment '事件主题',
    `data`       text                NOT NULL,
    `send_at`    timestamp(3)        NULL comment '发送时间',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at` datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted` tinyint(1) UNSIGNED NOT NULL DEFAULT '0' comment '是否删除 0.未删除 1.已删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_schema
-- ----------------------------
DROP TABLE IF EXISTS `event_schema`;
CREATE TABLE `event_schema`
(
    `id`         bigint UNSIGNED     NOT NULL,
    `code`       varchar(128)        NOT NULL comment '事件编码',
    `schema`     text                NOT NULL comment '事件 schema',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at` datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted` tinyint(1) UNSIGNED NOT NULL DEFAULT '0' comment '是否删除 0.未删除 1.已删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;
CREATE TABLE `group`
(
    `id`         bigint UNSIGNED     NOT NULL,
    `app_code`   varchar(128)        NOT NULL comment '应用编码',
    `parent_id`  bigint UNSIGNED     NULL comment '父级ID',
    `type`       tinyint UNSIGNED    NOT NULL DEFAULT '1' comment '类型:1.分组 2.标签',
    `name`       varchar(255)        NOT NULL DEFAULT '' comment '名称',
    `logo`       varchar(255)        NULL comment 'logo',
    `sort`       int unsigned        NOT NULL DEFAULT '0' comment '排序值',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `updated_at` datetime            NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间',
    `is_deleted` tinyint(1) UNSIGNED NOT NULL DEFAULT '0' comment '是否删除 0.未删除 1.已删除',
    PRIMARY KEY (`id`) USING BTREE
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
