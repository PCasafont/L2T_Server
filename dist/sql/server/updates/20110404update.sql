ALTER TABLE `olympiad_nobles`
  ADD `settled` INT(1) NOT NULL DEFAULT 0
  AFTER `competitions_teams`;
ALTER TABLE `olympiad_nobles_eom`
  ADD `settled` INT(1) NOT NULL DEFAULT 0
  AFTER `competitions_teams`;
