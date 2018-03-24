CREATE TABLE IF NOT EXISTS `log_items` (
  `owner_id` int(10) NOT NULL,
  `item_id` int(10) NOT NULL,
  `item_object_id` int(10) NOT NULL,
  `count` bigint(25) NOT NULL,
    `process` varchar(255) DEFAULT NULL,
  `time` bigint(25) NOT NULL,
  PRIMARY KEY (`item_object_id`, `time`)
);