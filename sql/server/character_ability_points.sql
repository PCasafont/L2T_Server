CREATE TABLE IF NOT EXISTS `character_ability_points` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `classIndex` INT UNSIGNED NOT NULL DEFAULT 0,
  `points` INT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`, `classIndex`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);