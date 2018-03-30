CREATE TABLE IF NOT EXISTS `character_offline_trade_items` (
  `charId` INT(10) UNSIGNED    NOT NULL,
  `item`   INT(10) UNSIGNED    NOT NULL DEFAULT '0',
  `count`  BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',
  `price`  BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`, `item`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
