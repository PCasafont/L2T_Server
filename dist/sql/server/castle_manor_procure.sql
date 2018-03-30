CREATE TABLE IF NOT EXISTS `castle_manor_procure` (
  `castle_id`   INT     NOT NULL DEFAULT '0',
  `crop_id`     INT(11) NOT NULL DEFAULT '0',
  `can_buy`     INT(11) NOT NULL DEFAULT '0',
  `start_buy`   INT(11) NOT NULL DEFAULT '0',
  `price`       INT(11) NOT NULL DEFAULT '0',
  `reward_type` INT(11) NOT NULL DEFAULT '0',
  `period`      INT     NOT NULL DEFAULT '1',
  PRIMARY KEY (`castle_id`, `crop_id`, `period`)
);
