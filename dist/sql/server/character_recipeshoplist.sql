CREATE TABLE IF NOT EXISTS `character_recipeshoplist` (
  `charId`   INT(10) UNSIGNED NOT NULL DEFAULT '0',
  `Recipeid` DECIMAL(11, 0)   NOT NULL DEFAULT '0',
  `Price`    BIGINT(20)       NOT NULL DEFAULT '0',
  `Pos`      INT(5)           NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `Recipeid`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
