CREATE TABLE IF NOT EXISTS `character_gears_presets` (
  `playerId` int(10) unsigned NOT NULL DEFAULT '0',
  `classId` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `gearGrade` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `presetData` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`playerId`,`classId`,`gearGrade`),
  FOREIGN KEY (`playerId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);
