-- 12/01/2009

ALTER TABLE characters
  ADD COLUMN `vitality_points` DOUBLE(7, 1) NOT NULL DEFAULT '0.0'
  AFTER `death_penalty_level`;
