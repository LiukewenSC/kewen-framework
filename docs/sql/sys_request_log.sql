
DROP TABLE IF EXISTS `sys_request_log`;
CREATE TABLE `sys_request_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户id',
  `user_name` varchar(64)   DEFAULT NULL COMMENT '用户姓名',
  `url` varchar(128)   DEFAULT NULL COMMENT '请求路径',
  `method` varchar(32)   DEFAULT NULL COMMENT '请求方式',
  `millisecond` int NULL DEFAULT NULL COMMENT '请求持续时间',
  `ip_address` varchar(64)   DEFAULT NULL COMMENT 'IP地址',
  `ip_comment` varchar(64)   DEFAULT NULL COMMENT 'IP地址描述',
  `params` varchar(512)   DEFAULT NULL COMMENT '请求param参数',
  `body` text   COMMENT '请求body参数',
  `trace_id` varchar(64)   DEFAULT NULL COMMENT '请求跟踪号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB   COMMENT = '请求日志记录器' ROW_FORMAT = DYNAMIC;
