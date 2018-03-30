CREATE TABLE IF NOT EXISTS `clan_privs` (
  `clan_id` INT NOT NULL DEFAULT 0,
  `rank`    INT NOT NULL DEFAULT 0,
  `party`   INT NOT NULL DEFAULT 0,
  `privs`   INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`clan_id`, `rank`, `party`),
  FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
