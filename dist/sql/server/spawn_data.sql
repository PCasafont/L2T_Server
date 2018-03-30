CREATE TABLE IF NOT EXISTS `spawn_data` (
  `name`         VARCHAR(55)         NOT NULL,
  `dimensionId`  TINYINT UNSIGNED    NOT NULL DEFAULT 0,
  `respawn_time` BIGINT(13) UNSIGNED NOT NULL DEFAULT 0,
  `current_hp`   DECIMAL(8, 0)                DEFAULT NULL,
  `current_mp`   DECIMAL(8, 0)                DEFAULT NULL,
  PRIMARY KEY (`name`)
);
