CREATE TABLE IF NOT EXISTS `character_friends` (
  `charId` INT UNSIGNED NOT NULL default 0,
  `friendId` INT UNSIGNED NOT NULL DEFAULT 0,
  `relation` INT UNSIGNED NOT NULL DEFAULT 0,
  `memo` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`charId`,`friendId`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);
