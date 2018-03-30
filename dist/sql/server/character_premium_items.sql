CREATE TABLE IF NOT EXISTS `character_premium_items` (
  `charId`     INT(11) UNSIGNED    NOT NULL,
  `itemNum`    INT(11)             NOT NULL,
  `itemId`     INT(11)             NOT NULL,
  `itemCount`  BIGINT(20) UNSIGNED NOT NULL,
  `itemSender` VARCHAR(50)         NOT NULL,
  PRIMARY KEY (`charId`, `itemNum`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
