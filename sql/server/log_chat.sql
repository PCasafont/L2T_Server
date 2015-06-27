CREATE TABLE IF NOT EXISTS `log_chat` (
  `type` varchar(20) NOT NULL,
  `talker` varchar(45) NOT NULL,
  `listener` varchar(45) DEFAULT NULL,
  `text` varchar(500) NOT NULL,
  `time` bigint(20) NOT NULL,
  PRIMARY KEY (`talker`, `time`)
);
