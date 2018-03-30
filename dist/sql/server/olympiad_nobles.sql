CREATE TABLE IF NOT EXISTS `olympiad_nobles` (
  `charId`                  INT UNSIGNED   NOT NULL DEFAULT 0,
  `class_id`                DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `olympiad_points`         DECIMAL(10, 0) NOT NULL DEFAULT 0,
  `competitions_done`       DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `competitions_won`        DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `competitions_lost`       DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `competitions_drawn`      DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `competitions_classed`    DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `competitions_nonclassed` DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `competitions_teams`      DECIMAL(3, 0)  NOT NULL DEFAULT 0,
  `settled`                 INT(1)         NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
);
