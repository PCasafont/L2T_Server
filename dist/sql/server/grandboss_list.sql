CREATE TABLE IF NOT EXISTS `grandboss_list` (
  `player_id` DECIMAL(11, 0) NOT NULL,
  `zone`      DECIMAL(11, 0) NOT NULL,
  PRIMARY KEY (`player_id`, `zone`)
);
