CREATE TABLE IF NOT EXISTS `character_recipebook` (
  `charId`     INT UNSIGNED NOT NULL DEFAULT 0,
  `id`         DECIMAL(11)  NOT NULL DEFAULT 0,
  `classIndex` TINYINT      NOT NULL DEFAULT 0,
  `type`       INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`, `charId`, `classIndex`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
