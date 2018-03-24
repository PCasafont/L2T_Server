CREATE TABLE IF NOT EXISTS `item_ensoul_effects` (
  `itemId` int(11) NOT NULL DEFAULT '0',
  `effectIndex` int(11) NOT NULL DEFAULT '-1',
  `effectId` int(11) NOT NULL DEFAULT '-1',
  PRIMARY KEY (`itemId`, `effectIndex`),
  FOREIGN KEY (`itemId`) REFERENCES `items`(`object_id`) ON UPDATE CASCADE ON DELETE CASCADE
);