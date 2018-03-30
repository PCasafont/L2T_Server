CREATE TABLE IF NOT EXISTS `log_chat` (
  `type`     VARCHAR(20)  NOT NULL,
  `talker`   VARCHAR(45)  NOT NULL,
  `listener` VARCHAR(45) DEFAULT NULL,
  `text`     VARCHAR(500) NOT NULL,
  `time`     BIGINT(20)   NOT NULL,
  PRIMARY KEY (`talker`, `time`)
);
