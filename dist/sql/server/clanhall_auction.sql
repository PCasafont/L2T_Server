CREATE TABLE IF NOT EXISTS `clanhall_auction` (
  `id`             INT(11)             NOT NULL DEFAULT '0',
  `sellerId`       INT(11)             NOT NULL DEFAULT '0',
  `sellerName`     VARCHAR(50)         NOT NULL DEFAULT 'NPC',
  `sellerClanName` VARCHAR(50)         NOT NULL DEFAULT '',
  `itemType`       VARCHAR(25)         NOT NULL DEFAULT '',
  `itemId`         INT(11)             NOT NULL DEFAULT '0',
  `itemObjectId`   INT(11)             NOT NULL DEFAULT '0',
  `itemName`       VARCHAR(40)         NOT NULL DEFAULT '',
  `itemQuantity`   BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `startingBid`    BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `currentBid`     BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `endDate`        BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`itemType`, `itemId`, `itemObjectId`),
  KEY `id` (`id`)
);
