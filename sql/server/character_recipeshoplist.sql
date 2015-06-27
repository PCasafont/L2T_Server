CREATE TABLE IF NOT EXISTS `character_recipeshoplist` (
  `charId` int(10) unsigned NOT NULL DEFAULT '0',
  `Recipeid` decimal(11,0) NOT NULL DEFAULT '0',
  `Price` bigint(20) NOT NULL DEFAULT '0',
  `Pos` int(5) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`Recipeid`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);