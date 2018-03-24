CREATE TABLE IF NOT EXISTS `log_enchant_skills` (
  `player_id` int(10) NOT NULL,
  `skill_id` int(10) NOT NULL,
  `skill_level` int(10) NOT NULL,
  `spb_id` int(10) NOT NULL,
  `rate` int(10) NOT NULL,
  `time` bigint(25) NOT NULL,
  PRIMARY KEY (`player_id`, `time`)
);