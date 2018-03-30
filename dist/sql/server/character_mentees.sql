CREATE TABLE IF NOT EXISTS `character_mentees` (
  `charId`   INT(10) UNSIGNED NOT NULL DEFAULT '0',
  `menteeId` INT(10) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `menteeId`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
