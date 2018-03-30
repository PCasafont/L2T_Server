CREATE TABLE IF NOT EXISTS `siege_clans` (
  `castle_id`    INT(1)  NOT NULL DEFAULT 0,
  `clan_id`      INT(11) NOT NULL DEFAULT 0,
  `type`         INT(1)           DEFAULT NULL,
  `castle_owner` INT(1)           DEFAULT NULL,
  PRIMARY KEY (`clan_id`, `castle_id`),
  FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
