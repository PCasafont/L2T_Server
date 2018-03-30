CREATE TABLE IF NOT EXISTS `grandboss_data` (
  `boss_id`      SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `loc_x`        MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `loc_y`        MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `loc_z`        MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `heading`      MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `respawn_time` BIGINT(13) UNSIGNED  NOT NULL DEFAULT '0',
  `currentHP`    DECIMAL(30, 15)      NOT NULL,
  `currentMP`    DECIMAL(30, 15)      NOT NULL,
  `status`       TINYINT(1) UNSIGNED  NOT NULL DEFAULT '0',
  PRIMARY KEY (`boss_id`)
);

INSERT IGNORE INTO `grandboss_data` VALUES
  (29001, -21610, 181594, -5734, 0, 0, 152622, 334, 0), -- Queen Ant (40)
  (29006, 17726, 108915, -6480, 0, 0, 413252, 1897, 0), -- Core (50)
  (29014, 55024, 17368, -5412, 10126, 0, 413252, 1897, 0), -- Orfen (50)
  (29020, 116033, 17447, 10104, 40188, 0, 2700852, 19980, 0), -- Baium (75)
  (29028, -105200, -253104, -15264, 0, 0, 11850000, 1998000, 0), -- Valakas (85)
  (29065, 0, 0, 0, 0, 0, 1216600, 11100, 0), -- Sailren (80)
  (29068, 185708, 114298, -8221, 32768, 0, 17850000, 1998000, 0), -- Antharas Strong (99)
  (29054, 0, 0, 0, 0, 0, 1352750, 4519, 0), -- Benom
  (29240, 0, 0, 0, 0, 0, 17850000, 1998000, 0), -- Lindvior (99)
  (25286, 0, 0, 0, 0, 0, 17850000, 1998000, 0), -- Anakim (94)
  (25283, 0, 0, 0, 0, 0, 17850000, 1998000, 0); -- Lilith (94)
