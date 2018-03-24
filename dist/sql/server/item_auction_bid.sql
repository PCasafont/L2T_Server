CREATE TABLE IF NOT EXISTS `item_auction_bid` (
  `auctionId` int(11) NOT NULL,
  `playerObjId` int unsigned NOT NULL,
  `playerBid` bigint(20) NOT NULL,
  PRIMARY KEY (`auctionId`,`playerObjId`),
  FOREIGN KEY (`playerObjId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);