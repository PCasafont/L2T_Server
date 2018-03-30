CREATE TABLE IF NOT EXISTS `castle_manor_production` (
  `castle_id`     INT     NOT NULL DEFAULT '0',
  `seed_id`       INT(11) NOT NULL DEFAULT '0',
  `can_produce`   INT(11) NOT NULL DEFAULT '0',
  `start_produce` INT(11) NOT NULL DEFAULT '0',
  `seed_price`    INT(11) NOT NULL DEFAULT '0',
  `period`        INT     NOT NULL DEFAULT '1',
  PRIMARY KEY (`castle_id`, `seed_id`, `period`)
);
