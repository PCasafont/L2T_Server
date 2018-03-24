ALTER TABLE `olympiad_nobles` ADD `settled` int(1) NOT NULL default 0 AFTER `competitions_teams`;
ALTER TABLE `olympiad_nobles_eom` ADD `settled` int(1) NOT NULL default 0 AFTER `competitions_teams`;
