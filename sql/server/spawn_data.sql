CREATE TABLE IF NOT EXISTS `spawn_data` (
  `name` varchar(55) NOT NULL,
  `dimensionId` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `respawn_time` bigint(13) unsigned NOT NULL DEFAULT 0,
  `current_hp` decimal(8,0) default NULL,
  `current_mp` decimal(8,0) default NULL,
  PRIMARY KEY (`name`)
);
