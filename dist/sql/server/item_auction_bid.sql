CREATE TABLE IF NOT EXISTS `item_auction_bid` (
  `auctionId`   INT(11)      NOT NULL,
  `playerObjId` INT UNSIGNED NOT NULL,
  `playerBid`   BIGINT(20)   NOT NULL,
  PRIMARY KEY (`auctionId`, `playerObjId`),
  FOREIGN KEY (`playerObjId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
