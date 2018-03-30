CREATE TABLE IF NOT EXISTS `character_gears_presets` (
  `playerId`   INT(10) UNSIGNED    NOT NULL DEFAULT '0',
  `classId`    TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `gearGrade`  TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `presetData` VARCHAR(1000)                DEFAULT NULL,
  PRIMARY KEY (`playerId`, `classId`, `gearGrade`),
  FOREIGN KEY (`playerId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
