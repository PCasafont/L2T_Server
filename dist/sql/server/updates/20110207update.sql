ALTER TABLE `olympiad_nobles`
  ADD `competitions_classed` DECIMAL(3, 0) NOT NULL DEFAULT 0
  AFTER `competitions_drawn`;
ALTER TABLE `olympiad_nobles`
  ADD `competitions_nonclassed` DECIMAL(3, 0) NOT NULL DEFAULT 0
  AFTER `competitions_classed`;
ALTER TABLE `olympiad_nobles`
  ADD `competitions_teams` DECIMAL(3, 0) NOT NULL DEFAULT 0
  AFTER `competitions_nonclassed`;
ALTER TABLE `olympiad_nobles_eom`
  ADD `competitions_classed` DECIMAL(3, 0) NOT NULL DEFAULT 0
  AFTER `competitions_drawn`;
ALTER TABLE `olympiad_nobles_eom`
  ADD `competitions_nonclassed` DECIMAL(3, 0) NOT NULL DEFAULT 0
  AFTER `competitions_classed`;
ALTER TABLE `olympiad_nobles_eom`
  ADD `competitions_teams` DECIMAL(3, 0) NOT NULL DEFAULT 0
  AFTER `competitions_nonclassed`;

UPDATE `olympiad_nobles`
SET `competitions_nonclassed` = `competitions_done`;
UPDATE `olympiad_nobles_eom`
SET `competitions_nonclassed` = `competitions_done`;
