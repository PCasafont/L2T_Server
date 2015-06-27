-- 12/01/2009

ALTER TABLE characters ADD COLUMN `vitality_points` double(7,1) NOT NULL default '0.0' after `death_penalty_level`;