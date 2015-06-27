CREATE TABLE IF NOT EXISTS `log_enchants` (
  `player_id` int(10) NOT NULL,
  `item_id` int(10) NOT NULL,
  `item_object_id` int(10) NOT NULL,
  `scroll` int(10) NOT NULL,
  `support_id` int(10) NOT NULL,
  `chance` int(3) NOT NULL,
  `time` bigint(25) NOT NULL,
  PRIMARY KEY (`player_id`, `time`)
);
