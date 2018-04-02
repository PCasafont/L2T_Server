DROP TABLE IF EXISTS `lottery_data`;
CREATE TABLE `lottery_data` (
  `name`    VARCHAR(35) NOT NULL DEFAULT '0',
  `numbers` CHAR(255)   NOT NULL DEFAULT '0',
  `ownerId` INT,
  PRIMARY KEY (`name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
