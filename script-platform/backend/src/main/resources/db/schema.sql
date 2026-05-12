-- Python Script Management Platform - MySQL 5.7 schema
CREATE DATABASE IF NOT EXISTS `script_platform`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `script_platform`;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
  `password`    VARCHAR(255) NOT NULL COMMENT '加密密码(bcrypt)',
  `role`        VARCHAR(32)  NOT NULL DEFAULT 'admin' COMMENT '角色: super_admin/admin/readonly',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `script_info`;
CREATE TABLE `script_info` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `script_name`     VARCHAR(128)  NOT NULL COMMENT '脚本名称',
  `script_path`     VARCHAR(512)  NOT NULL COMMENT '脚本路径',
  `module`          VARCHAR(64)   NOT NULL COMMENT '归属模块',
  `description`     TEXT          COMMENT '功能说明',
  `owner`           VARCHAR(64)   NOT NULL COMMENT '负责人',
  `alert_dingtalk`  VARCHAR(256)  DEFAULT NULL COMMENT '预警钉钉',
  `alert_phone`     VARCHAR(64)   DEFAULT NULL COMMENT '预警电话',
  `schedule_cron`   VARCHAR(64)   DEFAULT NULL COMMENT 'Cron表达式',
  `schedule_desc`   VARCHAR(128)  DEFAULT NULL COMMENT '调度频率描述',
  `script_content`  LONGTEXT      NOT NULL COMMENT '脚本内容',
  `version`         INT           NOT NULL DEFAULT 1 COMMENT '版本号',
  `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '状态:1启用0禁用',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_owner`  (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脚本信息表';

DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `event_time`      DATETIME      NOT NULL COMMENT '操作时间',
  `event_type`      VARCHAR(64)   NOT NULL COMMENT '事件类型',
  `operator`        VARCHAR(64)   NOT NULL COMMENT '操作人',
  `operator_ip`     VARCHAR(64)   DEFAULT NULL COMMENT '操作人IP',
  `target_id`       BIGINT        DEFAULT NULL COMMENT '操作对象ID',
  `target_name`     VARCHAR(256)  DEFAULT NULL COMMENT '操作对象名称',
  `request_params`  TEXT          COMMENT '请求参数JSON',
  `response_result` TEXT          COMMENT '响应结果JSON',
  `status`          TINYINT       NOT NULL DEFAULT 1 COMMENT '结果:1成功0失败',
  `remark`          VARCHAR(512)  DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_event_time` (`event_time`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_operator`   (`operator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
