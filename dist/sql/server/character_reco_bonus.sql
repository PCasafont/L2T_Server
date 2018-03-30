CREATE TABLE IF NOT EXISTS `character_reco_bonus` (
  `charId`    INT(10) UNSIGNED    NOT NULL,
  `rec_have`  TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `rec_left`  TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `time_left` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
