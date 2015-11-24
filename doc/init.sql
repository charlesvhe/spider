CREATE TABLE `tb_robot` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '机器人id',
  `group_id` int(11) NOT NULL COMMENT '机器人组，一个大任务需要多个机器人配合完成',
  `description` varchar(64) NOT NULL COMMENT '说明',
  `count` int(11) NOT NULL DEFAULT '1' COMMENT '同时执行的机器人数量',
  `script_engine` varchar(64) NOT NULL COMMENT '脚本引擎名:\ngroovy',
  `script` text NOT NULL COMMENT '脚本',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态\n0:禁用\n1:启用\n',
  PRIMARY KEY (`id`)
);

CREATE TABLE `tb_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) DEFAULT NULL COMMENT '父任务id\n任务链',
  `name` varchar(256) NOT NULL COMMENT '任务名，唯一键',
  `robot_id` int(11) NOT NULL COMMENT '当前任务处理机器人id',
  `context` longtext NOT NULL COMMENT '当前任务上下文',
  `request` longtext NOT NULL COMMENT '当前任务URL请求',
  `response` longtext NOT NULL COMMENT '当前任务URL响应',
  `output` longtext NOT NULL COMMENT '当前任务输出',
  `start_time` datetime DEFAULT NULL COMMENT '任务执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '任务执行结束时间',
  `refresh_time` datetime DEFAULT NULL COMMENT '下次执行任务时间',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态\n0:未开始\n1:请求发出\n2:响应接收\n3:已完成\n',
  PRIMARY KEY (`id`)
);
