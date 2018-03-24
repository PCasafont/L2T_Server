CREATE TABLE IF NOT EXISTS `character_premium_items` (
  `charId` int(11) unsigned NOT NULL,
  `itemNum` int(11) NOT NULL,
  `itemId` int(11) NOT NULL,
  `itemCount` bigint(20) unsigned NOT NULL,
  `itemSender` varchar(50) NOT NULL,
  PRIMARY KEY (`charId`, `itemNum`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);