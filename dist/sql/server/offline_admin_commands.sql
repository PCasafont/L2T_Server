CREATE TABLE IF NOT EXISTS `offline_admin_commands` (
  `date` INT(15) NOT NULL DEFAULT '0',
  `author` VARCHAR(20) NOT NULL,
  `accessLevel` INT(1) NOT NULL DEFAULT '0',
  `command` VARCHAR(50) NOT NULL,
  `executed` INT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`date`)
);