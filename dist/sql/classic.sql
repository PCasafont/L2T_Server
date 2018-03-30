DELETE FROM castle
WHERE id > 4;
DELETE FROM castle_siege_guards
WHERE castleId > 4;
DELETE FROM four_sepulchers_spawnlist;
DELETE FROM grandboss_data;
INSERT IGNORE INTO `grandboss_data` VALUES
  (29001, -21610, 181594, -5734, 0, 0, 152622, 334, 0), -- Queen Ant (40)
  (29006, 17726, 108915, -6480, 0, 0, 413252, 1897, 0), -- Core (50)
  (29014, 55024, 17368, -5412, 10126, 0, 413252, 1897, 0); -- Orfen (50)
DELETE FROM random_spawn_loc;
DELETE FROM random_spawn;
