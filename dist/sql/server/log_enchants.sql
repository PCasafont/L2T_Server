CREATE TABLE IF NOT EXISTS `log_enchants` (
  `player_id`      INT(10)    NOT NULL,
  `item_id`        INT(10)    NOT NULL,
  `item_object_id` INT(10)    NOT NULL,
  `scroll`         INT(10)    NOT NULL,
  `support_id`     INT(10)    NOT NULL,
  `chance`         INT(3)     NOT NULL,
  `time`           BIGINT(25) NOT NULL,
  PRIMARY KEY (`player_id`, `time`)
);
