CREATE TABLE IF NOT EXISTS `log_enchant_skills` (
  `player_id`   INT(10)    NOT NULL,
  `skill_id`    INT(10)    NOT NULL,
  `skill_level` INT(10)    NOT NULL,
  `spb_id`      INT(10)    NOT NULL,
  `rate`        INT(10)    NOT NULL,
  `time`        BIGINT(25) NOT NULL,
  PRIMARY KEY (`player_id`, `time`)
);
