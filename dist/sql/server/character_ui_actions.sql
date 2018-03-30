CREATE TABLE IF NOT EXISTS `character_ui_actions` (
  `charId` INT(10) UNSIGNED NOT NULL DEFAULT '0',
  `cat`    TINYINT(4)       NOT NULL,
  `order`  TINYINT(4)       NOT NULL,
  `cmd`    INT(8)           NOT NULL DEFAULT '0',
  `key`    INT(8)           NOT NULL,
  `tgKey1` INT(8)                    DEFAULT NULL,
  `tgKey2` INT(8)                    DEFAULT NULL,
  `show`   TINYINT(4)       NOT NULL,
  PRIMARY KEY (`charId`, `cat`, `cmd`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
