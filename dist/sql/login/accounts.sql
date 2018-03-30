CREATE TABLE IF NOT EXISTS `accounts` (
  `login`       VARCHAR(45)         NOT NULL DEFAULT '',
  `password`    VARCHAR(100),
  `lastactive`  BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `accessLevel` TINYINT             NOT NULL DEFAULT 0,
  `lastIP`      VARCHAR(15)         NULL     DEFAULT NULL,
  `lastIP2`     VARCHAR(15)         NULL     DEFAULT NULL,
  `lastIP3`     VARCHAR(15)         NULL     DEFAULT NULL,
  `lastServer`  TINYINT                      DEFAULT 1,
  `userIP`      CHAR(15)                     DEFAULT NULL,
  `pcIp`        CHAR(15)                     DEFAULT NULL,
  `hop1`        CHAR(15)                     DEFAULT NULL,
  `hop2`        CHAR(15)                     DEFAULT NULL,
  `hop3`        CHAR(15)                     DEFAULT NULL,
  `hop4`        CHAR(15)                     DEFAULT NULL,
  `site`        VARCHAR(45)         NOT NULL DEFAULT '',
  PRIMARY KEY (`login`)
);
