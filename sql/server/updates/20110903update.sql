ALTER TABLE `clan_wars` DROP `wantspeace1`;
ALTER TABLE `clan_wars` DROP `wantspeace2`;
ALTER TABLE `clan_wars` ADD `start_time` bigint(13) NOT NULL DEFAULT '0' AFTER `clan2`;
ALTER TABLE `clan_wars` ADD `end_time` bigint(13) NOT NULL DEFAULT '0' AFTER `start_time`;
ALTER TABLE `clan_wars` ADD `clan1_score` int(10) NOT NULL DEFAULT '0' AFTER `end_time`;
ALTER TABLE `clan_wars` ADD `clan2_score` int(10) NOT NULL DEFAULT '0' AFTER `clan1_score`;
ALTER TABLE `clan_wars` ADD `clan1_war_declarator` int(10) NOT NULL DEFAULT '0' AFTER `clan2_score`;
ALTER TABLE `clan_wars` ADD `clan2_war_declarator` int(10) NOT NULL DEFAULT '0' AFTER `clan1_war_declarator`;