CREATE TABLE IF NOT EXISTS `survey` (
  `survey_id` INT(5) NOT NULL DEFAULT '0',
  `question` VARCHAR(255) DEFAULT NULL,
  `description` varchar(1023) DEFAULT NULL, -- The survey html will show a "more info" button which will lead to this description
  `active` INT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`survey_id`)
);
