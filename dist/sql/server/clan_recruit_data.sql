CREATE TABLE IF NOT EXISTS `clan_recruit_data` (
  `clan_id`            INT NOT NULL DEFAULT 0,
  `karma`              INT NOT NULL DEFAULT 0,
  `introduction`       VARCHAR(31),
  `large_introduction` VARCHAR(255),
  PRIMARY KEY (`clan_id`),
  FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
