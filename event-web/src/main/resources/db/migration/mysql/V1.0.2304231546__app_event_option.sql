-- 应用 & 事件 配置
ALTER TABLE `app`
    ADD COLUMN `options` INT NOT NULL DEFAULT 0 COMMENT '应用配置：1.可编排';
ALTER TABLE `event`
    ADD COLUMN `options` INT NOT NULL DEFAULT 1 COMMENT '事件配置：1.可编排';
UPDATE `app` SET `options` = 1 WHERE `code` IN ('authing','dingtalk','xinrenxinshi','ad','ldap','wechatwork','lark');
