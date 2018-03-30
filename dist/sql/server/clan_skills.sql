CREATE TABLE IF NOT EXISTS `clan_skills` (
  `clan_id`       INT(11) NOT NULL DEFAULT 0,
  `skill_id`      INT(11) NOT NULL DEFAULT 0,
  `skill_level`   INT(5)  NOT NULL DEFAULT 0,
  `skill_name`    VARCHAR(26)      DEFAULT NULL,
  `sub_pledge_id` INT     NOT NULL DEFAULT '-2',
  PRIMARY KEY (`clan_id`, `skill_id`, `sub_pledge_id`),
  FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
