CREATE TABLE IF NOT EXISTS `clan_recruit_applicants` (
  `applicant_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `clan_id` INT NOT NULL DEFAULT 0,
  `karma` INT NOT NULL DEFAULT 0,
  `application` VARCHAR(255),
  PRIMARY KEY (`applicant_id`),
  FOREIGN KEY (`applicant_id`) REFERENCES `characters`(`charId`) ON UPDATE CASCADE ON DELETE CASCADE
);