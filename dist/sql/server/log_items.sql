CREATE TABLE IF NOT EXISTS `log_items` (
  `owner_id`       INT(10)    NOT NULL,
  `item_id`        INT(10)    NOT NULL,
  `item_object_id` INT(10)    NOT NULL,
  `count`          BIGINT(25) NOT NULL,
  `process`        VARCHAR(255) DEFAULT NULL,
  `time`           BIGINT(25) NOT NULL,
  PRIMARY KEY (`item_object_id`, `time`)
);
