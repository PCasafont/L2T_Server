CREATE TABLE IF NOT EXISTS `item_auction` (
  `auctionId`      INT(11)             NOT NULL,
  `instanceId`     INT(11)             NOT NULL,
  `auctionItemId`  INT(11)             NOT NULL,
  `startingTime`   BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `endingTime`     BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `auctionStateId` TINYINT(1)          NOT NULL,
  PRIMARY KEY (`auctionId`)
);
