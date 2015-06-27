CREATE TABLE IF NOT EXISTS `character_offline_trade_item_prices` (
  `charId` int(10) unsigned NOT NULL,
  `item` int(10) unsigned NOT NULL DEFAULT '0',
  `priceId` int(10) unsigned NOT NULL DEFAULT '0',
  `count` bigint(20) unsigned NOT NULL DEFAULT '0',
   PRIMARY KEY (`charId`, `item`, `priceId`),
   FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);
