CREATE TABLE IF NOT EXISTS `character_last_summons` (
  `charId`      INT UNSIGNED NOT NULL DEFAULT 0,
  `summonIndex` INT(1)       NOT NULL DEFAULT 0,
  `npcId`       INT(2)       NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`, `summonIndex`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
