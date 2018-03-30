CREATE TABLE IF NOT EXISTS `log_olys` (
  `player1_id`       INT(10)       NOT NULL,
  `player2_id`       INT(10)       NOT NULL,
  `player1_hp`       DOUBLE(10, 0) NOT NULL,
  `player2_hp`       DOUBLE(10, 0) NOT NULL,
  `player1_damage`   INT(10)       NOT NULL,
  `player2_damage`   INT(10)       NOT NULL,
  `points`           INT(10)       NOT NULL,
  `competition_type` VARCHAR(50)   NOT NULL,
  `time`             BIGINT(25)    NOT NULL,
  PRIMARY KEY (`player1_id`, `player2_id`, `time`)
);
