CREATE TABLE IF NOT EXISTS `clan_wars` (
  `clan1`                INT        NOT NULL DEFAULT '0',
  `clan2`                INT        NOT NULL DEFAULT '0',
  `start_time`           BIGINT(13) NOT NULL DEFAULT '0',
  `end_time`             BIGINT(13) NOT NULL DEFAULT '0',
  `delete_time`          BIGINT(13) NOT NULL DEFAULT '0',
  `clan1_score`          INT(10)    NOT NULL DEFAULT '0',
  `clan2_score`          INT(10)    NOT NULL DEFAULT '0',
  `clan1_war_declarator` INT        NOT NULL,
  `clan2_war_declarator` INT        NOT NULL,
  `clan1_deaths_for_war` INT(10)    NOT NULL DEFAULT '0',
  `clan1_shown_score`    INT(10)    NOT NULL DEFAULT '0',
  `clan2_shown_score`    INT(10)    NOT NULL DEFAULT '0',
  `loserId`              INT(10)    NOT NULL DEFAULT '0',
  `winnerId`             INT(10)    NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan1`, `clan2`),
  FOREIGN KEY (`clan1`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (`clan2`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
