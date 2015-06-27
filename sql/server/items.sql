CREATE TABLE IF NOT EXISTS `items` (
  `owner_id` INT UNSIGNED, -- object id of the player or clan,owner of this item
  `object_id` INT NOT NULL DEFAULT 0, -- object id of the item
  `item_id` INT,
  `count` BIGINT UNSIGNED NOT NULL default 0,
  `enchant_level` INT,
  `loc` VARCHAR(10), -- inventory,paperdoll,npc,clan warehouse,pet,and so on
  `loc_data` INT,    -- depending on location: equiped slot,npc id,pet id,etc
  `time_of_use` INT, -- time of item use, for calculate of breackages
  `custom_type1` INT DEFAULT 0,
  `custom_type2` INT DEFAULT 0,
  `mana_left` decimal(5,0) NOT NULL default -1,
  `time` decimal(13) NOT NULL default 0,
  `appearance` INT NOT NULL default 0,
  `mob_id` NT NOT NULL default 0,
  `gm_touched` VARCHAR(20),
  PRIMARY KEY (`object_id`),
  FOREIGN KEY (`owner_id`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE,
  KEY `key_loc` (`loc`),
  KEY `key_item_id` (`item_id`),
  KEY `key_time_of_use` (`time_of_use`)
);