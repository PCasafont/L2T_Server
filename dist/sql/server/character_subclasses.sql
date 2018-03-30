CREATE TABLE IF NOT EXISTS `character_subclasses` (
  `charId`       INT UNSIGNED   NOT NULL DEFAULT 0,
  `class_id`     INT(2)         NOT NULL DEFAULT 0,
  `exp`          DECIMAL(20, 0) NOT NULL DEFAULT 0,
  `sp`           DECIMAL(20, 0) NOT NULL DEFAULT 0,
  `level`        INT(2)         NOT NULL DEFAULT 40,
  `class_index`  INT(1)         NOT NULL DEFAULT 0,
  `is_dual`      INT(1)         NOT NULL DEFAULT 0,
  `certificates` INT(1)         NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`, `class_index`),
  FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
