CREATE TABLE IF NOT EXISTS `fort` (
  `id` int(11) NOT NULL default 0,
  `siegeDate` bigint(13) unsigned NOT NULL DEFAULT '0',
  `lastOwnedTime` bigint(13) unsigned NOT NULL DEFAULT '0',
  `owner` int(11) NOT NULL default 0,
  `state` int(1) NOT NULL default 0,
  `castleId` int(1) NOT NULL default 0,
  `blood` int(3) NOT NULL default 0,
  `supplyLvL` int(2) NOT NULL default 0,
  PRIMARY KEY (`id`)
);
