CREATE TABLE IF NOT EXISTS `character_instance_time` (
  `charId`     INT UNSIGNED        NOT NULL DEFAULT '0',
  `instanceId` INT(3)              NOT NULL DEFAULT '0',
  `time`       BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `instanceId`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
