CREATE TABLE IF NOT EXISTS `item_ensoul_effects` (
  `itemId`      INT(11) NOT NULL DEFAULT '0',
  `effectIndex` INT(11) NOT NULL DEFAULT '-1',
  `effectId`    INT(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`itemId`, `effectIndex`),
  FOREIGN KEY (`itemId`) REFERENCES `items` (`object_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
