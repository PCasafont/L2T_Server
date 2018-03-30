CREATE TABLE IF NOT EXISTS `character_skills_save` (
  `charId`          INT UNSIGNED        NOT NULL DEFAULT 0,
  `skill_id`        INT                 NOT NULL DEFAULT 0,
  `skill_level`     INT(3)              NOT NULL DEFAULT 1,
  `effect_count`    INT                 NOT NULL DEFAULT 0,
  `effect_cur_time` INT                 NOT NULL DEFAULT 0,
  `reuse_delay`     INT(8)              NOT NULL DEFAULT 0,
  `systime`         BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `restore_type`    INT(1)              NOT NULL DEFAULT 0,
  `class_index`     INT(1)              NOT NULL DEFAULT 0,
  `buff_index`      INT(2)              NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`, `skill_id`, `skill_level`, `class_index`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
