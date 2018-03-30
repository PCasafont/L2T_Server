CREATE TABLE IF NOT EXISTS `fort` (
  `id`            INT(11)             NOT NULL DEFAULT 0,
  `siegeDate`     BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `lastOwnedTime` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `owner`         INT(11)             NOT NULL DEFAULT 0,
  `state`         INT(1)              NOT NULL DEFAULT 0,
  `castleId`      INT(1)              NOT NULL DEFAULT 0,
  `blood`         INT(3)              NOT NULL DEFAULT 0,
  `supplyLvL`     INT(2)              NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
);
