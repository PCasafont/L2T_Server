CREATE TABLE IF NOT EXISTS `fortsiege_clans` (
  `fort_id` INT(1)  NOT NULL DEFAULT '0',
  `clan_id` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan_id`, `fort_id`)
);
