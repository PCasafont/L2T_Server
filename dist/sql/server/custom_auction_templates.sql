CREATE TABLE IF NOT EXISTS `custom_auction_templates` (
  `id`                  INT(11)    NOT NULL,
  `lastAuctionCreation` BIGINT(13) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
