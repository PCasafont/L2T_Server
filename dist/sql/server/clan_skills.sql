CREATE TABLE IF NOT EXISTS `clan_skills` (
  `clan_id` int(11) NOT NULL default 0,
  `skill_id` int(11) NOT NULL default 0,
  `skill_level` int(5) NOT NULL default 0,
  `skill_name` varchar(26) default NULL,
  `sub_pledge_id` INT NOT NULL default '-2',
  PRIMARY KEY (`clan_id`,`skill_id`,`sub_pledge_id`),
  FOREIGN KEY (`clan_id`) REFERENCES `clan_data`(`clan_id`) ON UPDATE CASCADE ON DELETE CASCADE
);