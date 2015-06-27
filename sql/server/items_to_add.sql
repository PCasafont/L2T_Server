CREATE TABLE IF NOT EXISTS `items_to_add` (
  `user_name` VARCHAR(35) NOT NULL,
  `item_id` INT(5) NOT NULL,
  `count` INT(4) NOT NULL DEFAULT '0',
  `online` INT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`user_name`,`item_id`)
);
