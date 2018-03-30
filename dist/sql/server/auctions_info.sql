CREATE TABLE IF NOT EXISTS `auctions_info` (
  `auctionID`  INT(11)             NOT NULL DEFAULT '0',
  `sellerID`   INT(11)             NOT NULL DEFAULT '0',
  `itemName`   VARCHAR(50)                  DEFAULT NULL,
  `itemOID`    INT(11)             NOT NULL,
  `price`      BIGINT(20)          NOT NULL,
  `count`      BIGINT(20) UNSIGNED NOT NULL DEFAULT '0',
  `category`   INT(3)              NOT NULL,
  `duration`   INT(11)             NOT NULL,
  `finishTime` BIGINT(13)          NOT NULL,
  `itemID`     INT(13)             NOT NULL,
  PRIMARY KEY (`sellerID`, `auctionID`)
);
