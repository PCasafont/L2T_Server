CREATE TABLE IF NOT EXISTS `character_reco_bonus` (
  `charId` int(10) unsigned NOT NULL,
  `rec_have` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `rec_left` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `time_left` bigint(13) unsigned NOT NULL DEFAULT '0',
  UNIQUE KEY `charId` (`charId`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);