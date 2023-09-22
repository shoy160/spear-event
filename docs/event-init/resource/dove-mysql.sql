SET FOREIGN_KEY_CHECKS = 0;
-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app`
(
    `id`         bigint unsigned NOT NULL,
    `code`       varchar(128)             DEFAULT NULL,
    `name`       varchar(255)             DEFAULT NULL,
    `logo`       varchar(255)             DEFAULT NULL,
    `private_id` varchar(128)             DEFAULT NULL,
    `created_at` datetime        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime        NULL ON UPDATE CURRENT_TIMESTAMP,
    `type`       tinyint         NOT NULL,
    `is_deleted` tinyint(1)      NOT NULL DEFAULT '0'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`
(
    `id`             bigint              NOT NULL,
    `creator_id`     varchar(128)                 DEFAULT NULL,
    `creator_name`   varchar(128)                 DEFAULT NULL,
    `user_agent`     varchar(128)                 DEFAULT NULL,
    `resource_type`  tinyint unsigned    NOT NULL,
    `resource_id`    bigint              NOT NULL,
    `operation_type` tinyint             NOT NULL,
    `desc`           varchar(128)                 DEFAULT NULL,
    `detail`         json                         DEFAULT NULL,
    `result`         tinyint(1) unsigned NOT NULL,
    `client_ip`      char(16)                     DEFAULT NULL,
    `created_at`     datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     datetime            NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`     tinyint(1) unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for client
-- ----------------------------
DROP TABLE IF EXISTS `client`;
CREATE TABLE `client`
(
    `id`         bigint              NOT NULL,
    `name`       varchar(64)         NOT NULL,
    `secret`     varchar(64)         NOT NULL,
    `status`     tinyint unsigned    NOT NULL DEFAULT '1',
    `created_at` datetime            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime            NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for client_grant
-- ----------------------------
DROP TABLE IF EXISTS `client_grant`;
CREATE TABLE `client_grant`
(
    `id`           bigint           NOT NULL,
    `client_id`    bigint unsigned  NOT NULL,
    `topic`        varchar(128)     NOT NULL,
    `operation`    tinyint(1) unsigned       DEFAULT NULL,
    `group`        varchar(128)              DEFAULT NULL,
    `created_at`   datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   datetime         NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`   tinyint(1)       NOT NULL DEFAULT '0',
    `status`       tinyint unsigned NOT NULL DEFAULT '1',
    `type`         tinyint unsigned NOT NULL,
    `source`       varchar(255)              DEFAULT NULL,
    `pattern_type` tinyint unsigned NOT NULL DEFAULT '1',
    `host`         varchar(255)              DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for config
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`
(
    `id`          bigint       NOT NULL,
    `key`         varchar(128) NOT NULL,
    `value`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
    `profile`     varchar(32)           DEFAULT NULL,
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  datetime     NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`  tinyint(1)            DEFAULT '0',
    `creator_id`  varchar(64)           DEFAULT NULL,
    `modifier_id` varchar(64)           DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event
-- ----------------------------
DROP TABLE IF EXISTS `event`;
CREATE TABLE `event`
(
    `id`            bigint           NOT NULL,
    `code`          varchar(128)     NOT NULL,
    `name`          varchar(128)     NOT NULL,
    `desc`          varchar(255)              DEFAULT NULL,
    `creator_id`    varchar(64)               DEFAULT NULL,
    `modifier_id`   varchar(64)               DEFAULT NULL,
    `is_deleted`    tinyint(1)       NOT NULL DEFAULT '0',
    `updated_at`    datetime         NULL ON UPDATE CURRENT_TIMESTAMP,
    `created_at`    datetime         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `topic`         varchar(128)              DEFAULT NULL,
    `partition`     bigint unsigned  NOT NULL DEFAULT '1',
    `replications`  bigint unsigned  NOT NULL DEFAULT '1',
    `retention`     bigint unsigned  NOT NULL DEFAULT '30',
    `tags`          varchar(1024)             DEFAULT NULL,
    `app_code`      varchar(128)              DEFAULT NULL,
    `module_code`   varchar(128)              DEFAULT NULL,
    `message_count` bigint                    DEFAULT '0',
    `type`          tinyint unsigned NOT NULL DEFAULT '1',
    `private_id`    varchar(128)              DEFAULT NULL,
    `sort`          bigint unsigned  NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_data
-- ----------------------------
DROP TABLE IF EXISTS `event_data`;
CREATE TABLE `event_data`
(
    `id`         varchar(128) NOT NULL,
    `data`       text         NOT NULL,
    `send_at`    timestamp(6) NULL     DEFAULT NULL,
    `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime     NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1)   NOT NULL DEFAULT '0',
    `topic`      varchar(255) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for event_schema
-- ----------------------------
DROP TABLE IF EXISTS `event_schema`;
CREATE TABLE `event_schema`
(
    `id`         bigint       NOT NULL,
    `code`       varchar(128) NOT NULL,
    `schema`     text         NOT NULL,
    `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime     NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1)   NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for group
-- ----------------------------
DROP TABLE IF EXISTS `group`;
CREATE TABLE `group`
(
    `id`         bigint       NOT NULL,
    `app_code`   varchar(128) NOT NULL,
    `parent_id`  bigint                DEFAULT NULL,
    `type`       tinyint      NOT NULL DEFAULT '1',
    `name`       varchar(255)          DEFAULT NULL,
    `logo`       varchar(255)          DEFAULT NULL,
    `sort`       bigint unsigned       DEFAULT NULL,
    `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` datetime     NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` tinyint(1)   NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
