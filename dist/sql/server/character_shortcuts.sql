CREATE TABLE IF NOT EXISTS `character_shortcuts` (
  `charId`      INT UNSIGNED NOT NULL DEFAULT 0,
  `slot`        DECIMAL(3)   NOT NULL DEFAULT 0,
  `page`        DECIMAL(3)   NOT NULL DEFAULT 0,
  `type`        DECIMAL(3),
  `shortcut_id` DECIMAL(16),
  `level`       INT,
  `levelRange`  INT                   DEFAULT -1,
  `class_index` INT(1)       NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `slot`, `page`, `class_index`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  KEY `shortcut_id` (`shortcut_id`)
);
