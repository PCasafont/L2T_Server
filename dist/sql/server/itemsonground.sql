CREATE TABLE IF NOT EXISTS `itemsonground` (
  `object_id`     INT(11)             NOT NULL DEFAULT '0',
  `item_id`       INT(11)                      DEFAULT NULL,
  `count`         BIGINT UNSIGNED     NOT NULL DEFAULT 0,
  `enchant_level` INT(11)                      DEFAULT NULL,
  `x`             INT(11)                      DEFAULT NULL,
  `y`             INT(11)                      DEFAULT NULL,
  `z`             INT(11)                      DEFAULT NULL,
  `drop_time`     BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `equipable`     INT(1)                       DEFAULT '0',
  PRIMARY KEY (`object_id`)
);
