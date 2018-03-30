DROP TABLE IF EXISTS `shop_item_counts`;
CREATE TABLE `shop_item_counts` (
  `shop_id` MEDIUMINT(7) UNSIGNED NOT NULL DEFAULT '0',
  `item_id` SMALLINT(5) UNSIGNED  NOT NULL DEFAULT '0',
  `count`   TINYINT(2)            NOT NULL DEFAULT '-1',
  `time`    BIGINT(13) UNSIGNED   NOT NULL DEFAULT '0',
  PRIMARY KEY (`shop_id`, `item_id`)
);
