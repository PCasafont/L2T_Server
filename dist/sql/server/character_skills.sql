CREATE TABLE IF NOT EXISTS `character_skills` (
  `charId`      INT UNSIGNED NOT NULL DEFAULT 0,
  `skill_id`    INT          NOT NULL DEFAULT 0,
  `skill_level` INT(3)       NOT NULL DEFAULT 1,
  `class_index` INT(1)       NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`, `skill_id`, `class_index`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
