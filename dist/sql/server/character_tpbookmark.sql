CREATE TABLE IF NOT EXISTS `character_tpbookmark` (
  `charId` INT(20) UNSIGNED NOT NULL,
  `Id`     INT(20)          NOT NULL,
  `x`      INT(20)          NOT NULL,
  `y`      INT(20)          NOT NULL,
  `z`      INT(20)          NOT NULL,
  `icon`   INT(20)          NOT NULL,
  `tag`    VARCHAR(50) DEFAULT NULL,
  `name`   VARCHAR(50)      NOT NULL,
  PRIMARY KEY (`charId`, `Id`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
