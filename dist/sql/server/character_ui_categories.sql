CREATE TABLE IF NOT EXISTS `character_ui_categories` (
  `charId` INT(10) UNSIGNED NOT NULL DEFAULT '0',
  `catId`  TINYINT(4)       NOT NULL,
  `order`  TINYINT(4)       NOT NULL,
  `cmdId`  INT(8)           NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `catId`, `order`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
