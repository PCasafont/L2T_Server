CREATE TABLE IF NOT EXISTS `clanhall_auction_bid` (
  `id`         INT                 NOT NULL DEFAULT 0,
  `auctionId`  INT                 NOT NULL DEFAULT 0,
  `bidderId`   INT                 NOT NULL DEFAULT 0,
  `bidderName` VARCHAR(50)         NOT NULL,
  `clan_name`  VARCHAR(50)         NOT NULL,
  `maxBid`     BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `time_bid`   BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`auctionId`, `bidderId`),
  KEY `id` (`id`),
  FOREIGN KEY (`bidderId`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
