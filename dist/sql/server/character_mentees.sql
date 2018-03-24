CREATE TABLE IF NOT EXISTS `character_mentees` (
  `charId` int(10) unsigned NOT NULL DEFAULT '0',
  `menteeId` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`menteeId`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);