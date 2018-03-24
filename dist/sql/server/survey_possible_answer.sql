CREATE TABLE IF NOT EXISTS `survey_possible_answer` (
  `survey_id` INT(5) NOT NULL DEFAULT '0',
  `answer_id` INT(5) NOT NULL DEFAULT '0',
  `answer` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`survey_id`,`answer_id`)
);
