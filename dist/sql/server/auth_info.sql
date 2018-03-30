DROP TABLE IF EXISTS `auth_info`;
CREATE TABLE `auth_info` (
  `userIp`         VARCHAR(200) NOT NULL,
  `hardwareId`     VARCHAR(200) NOT NULL,
  `windowsUser`    VARCHAR(200) NOT NULL,
  `version`        VARCHAR(200) NOT NULL,
  `processes`      MEDIUMTEXT   NOT NULL,
  `lastUpdateTime` BIGINT(20) UNSIGNED DEFAULT NULL,
  PRIMARY KEY (`userIp`, `hardwareId`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
