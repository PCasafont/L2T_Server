CREATE TABLE IF NOT EXISTS `character_last_summons` (
  `charId` INT UNSIGNED NOT NULL default 0,
  `summonIndex` int(1) NOT NULL default 0,
  `npcId` int(2) NOT NULL default 0,
  PRIMARY KEY (`charId`,`summonIndex`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);
