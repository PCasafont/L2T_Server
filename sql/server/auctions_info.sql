CREATE TABLE IF NOT EXISTS `auctions_info` (
  `auctionID` int(11) NOT NULL DEFAULT '0',
  `sellerID` int(11) NOT NULL DEFAULT '0',
  `itemName` varchar(50) DEFAULT NULL,
  `itemOID` int(11) NOT NULL,
  `price` bigint(20) NOT NULL,
  `count` bigint(20) unsigned NOT NULL DEFAULT '0',
  `category` int(3) NOT NULL,
  `duration` int(11) NOT NULL,
  `finishTime` bigint(13) NOT NULL,
  `itemID` int(13) NOT NULL,
  PRIMARY KEY (`sellerID`,`auctionID`)
);