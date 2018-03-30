CREATE TABLE IF NOT EXISTS `gm_audit` (
  `id`     INT(11)      NOT NULL AUTO_INCREMENT,
  `gm`     VARCHAR(350) NOT NULL,
  `action` VARCHAR(350) NOT NULL,
  `target` VARCHAR(350)          DEFAULT NULL,
  `params` VARCHAR(350)          DEFAULT NULL,
  `time`   INT(11)      NOT NULL,
  PRIMARY KEY (`id`)
);
