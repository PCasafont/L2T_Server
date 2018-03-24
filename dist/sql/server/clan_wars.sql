CREATE TABLE IF NOT EXISTS `clan_wars` (
  `clan1` int NOT NULL DEFAULT '0',
  `clan2` int NOT NULL DEFAULT '0',
  `start_time` bigint(13) NOT NULL DEFAULT '0',
  `end_time` bigint(13) NOT NULL DEFAULT '0',
  `delete_time` bigint(13) NOT NULL DEFAULT '0',
  `clan1_score` int(10) NOT NULL DEFAULT '0',
  `clan2_score` int(10) NOT NULL DEFAULT '0',
  `clan1_war_declarator` int NOT NULL,
  `clan2_war_declarator` int NOT NULL,
  `clan1_deaths_for_war` int(10) NOT NULL DEFAULT '0',
  `clan1_shown_score` int(10) NOT NULL DEFAULT '0',
  `clan2_shown_score` int(10) NOT NULL DEFAULT '0',
  `loserId` int(10) NOT NULL DEFAULT '0',
  `winnerId` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan1`,`clan2`),
  FOREIGN KEY (`clan1`) REFERENCES `clan_data`(`clan_id`) ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (`clan2`) REFERENCES `clan_data`(`clan_id`) ON UPDATE CASCADE ON DELETE CASCADE
);