CREATE TABLE IF NOT EXISTS `log_olys` (
  `player1_id` int(10) NOT NULL,
  `player2_id` int(10) NOT NULL,
  `player1_hp` double(10,0) NOT NULL,
  `player2_hp` double(10,0) NOT NULL,
  `player1_damage` int(10) NOT NULL,
  `player2_damage` int(10) NOT NULL,
  `points` int(10) NOT NULL,
  `competition_type` varchar(50) NOT NULL,
  `time` bigint(25) NOT NULL,
  PRIMARY KEY (`player1_id`, `player2_id`, `time`)
);