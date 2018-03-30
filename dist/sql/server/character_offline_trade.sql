CREATE TABLE IF NOT EXISTS `character_offline_trade` (
  `charId` INT(10) UNSIGNED    NOT NULL,
  `time`   BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `type`   TINYINT(4)          NOT NULL DEFAULT '0',
  `title`  VARCHAR(50)                  DEFAULT NULL,
  PRIMARY KEY (`charId`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
