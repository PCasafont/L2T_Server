CREATE TABLE IF NOT EXISTS `custom_auctions` (
  `id`         INT(11)    NOT NULL,
  `itemId`     INT(11)    NOT NULL,
  `templateId` INT(11)    NOT NULL,
  `currencyId` INT(5)     NOT NULL,
  `currentBid` BIGINT(13) NOT NULL,
  `ownerId`    INT(11)    NOT NULL,
  `endTime`    BIGINT(13) NOT NULL,
  PRIMARY KEY (`id`)
);
