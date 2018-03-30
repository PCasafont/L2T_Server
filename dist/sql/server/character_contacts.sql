CREATE TABLE IF NOT EXISTS `character_contacts` (
  `charId`    INT(10) UNSIGNED NOT NULL DEFAULT '0',
  `contactId` INT(10) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `contactId`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
