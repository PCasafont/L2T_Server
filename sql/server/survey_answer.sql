CREATE TABLE IF NOT EXISTS `survey_answer` (
  `charId` INT UNSIGNED NOT NULL DEFAULT '0',
  `survey_id` INT(5) NOT NULL DEFAULT '0',
  `answer_id` INT(5) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`survey_id`)
);
