CREATE TABLE IF NOT EXISTS `item_attributes` (
  `itemId`        INT(11)    NOT NULL DEFAULT '0',
  `augAttributes` BIGINT(21) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`itemId`),
  FOREIGN KEY (`itemId`) REFERENCES `items` (`object_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
