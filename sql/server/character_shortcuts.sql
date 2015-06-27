CREATE TABLE IF NOT EXISTS `character_shortcuts` (
  `charId` INT UNSIGNED NOT NULL default 0,
  `slot` decimal(3) NOT NULL default 0,
  `page` decimal(3) NOT NULL default 0,
  `type` decimal(3),
  `shortcut_id` decimal(16) ,
  `level` int,
  `class_index` int(1) NOT NULL default '0',
  PRIMARY KEY (`charId`,`slot`,`page`,`class_index`),
  FOREIGN KEY (`charId`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE,
  KEY `shortcut_id` (`shortcut_id`)
);
