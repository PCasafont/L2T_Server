CREATE TABLE IF NOT EXISTS `character_recipebook` (
  `charId` INT UNSIGNED NOT NULL default 0,
  `id` decimal(11) NOT NULL default 0,
  `classIndex` TINYINT NOT NULL DEFAULT 0,
  `type` INT NOT NULL default 0,
  PRIMARY KEY (`id`,`charId`,`classIndex`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);