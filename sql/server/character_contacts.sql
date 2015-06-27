CREATE TABLE IF NOT EXISTS `character_contacts` (
  `charId` int(10) unsigned NOT NULL DEFAULT '0',
  `contactId` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`contactId`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
) ;