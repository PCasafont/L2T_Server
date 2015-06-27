DROP TABLE IF EXISTS `shop_item_counts`;
CREATE TABLE `shop_item_counts` (
  `shop_id` mediumint(7) unsigned NOT NULL DEFAULT '0',
  `item_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `count` tinyint(2) NOT NULL DEFAULT '-1',
  `time` bigint(13) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`shop_id`,`item_id`)
);
