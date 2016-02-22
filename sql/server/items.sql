CREATE TABLE IF NOT EXISTS `items` (
  `owner_id` INT UNSIGNED, -- object id of the player or clan,owner of this item
  `object_id` INT NOT NULL DEFAULT 0, -- object id of the item
  `item_id` INT,
  `count` BIGINT UNSIGNED NOT NULL default 0,
  `enchant_level` INT,
  `loc` VARCHAR(10), -- inventory,paperdoll,npc,clan warehouse,pet,and so on
  `loc_data` INT,    -- depending on location: equiped slot,npc id,pet id,etc
  `custom_type1` INT DEFAULT 0,
  `custom_type2` INT DEFAULT 0,
  `mana_left` decimal(5,0) NOT NULL default -1,
  `time` decimal(13) NOT NULL default 0,
  `appearance` INT NOT NULL default 0,
  `mob_id` SMALLINT NOT NULL default 0,
  PRIMARY KEY (`object_id`),
  FOREIGN KEY (`owner_id`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE,
  KEY `key_loc` (`loc`),
  KEY `key_item_id` (`item_id`)
);