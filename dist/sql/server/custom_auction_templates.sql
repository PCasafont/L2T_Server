CREATE TABLE IF NOT EXISTS `custom_auction_templates` (
  `id` int(11) NOT NULL,
  `lastAuctionCreation` bigint(13) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
