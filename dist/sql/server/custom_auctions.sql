CREATE TABLE IF NOT EXISTS `custom_auctions` (
  `id` int(11) NOT NULL,
  `itemId` int(11) NOT NULL,
  `templateId` int(11) NOT NULL,
  `currencyId` int(5) NOT NULL,
  `currentBid` bigint(13) NOT NULL,
  `ownerId` int(11) NOT NULL,
  `endTime` bigint(13) NOT NULL,
  PRIMARY KEY (`id`)
);
