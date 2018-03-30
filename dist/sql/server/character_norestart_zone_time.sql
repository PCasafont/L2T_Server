CREATE TABLE IF NOT EXISTS `character_norestart_zone_time` (
  `charId`     INT(10) UNSIGNED    NOT NULL DEFAULT '0',
  `time_limit` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
