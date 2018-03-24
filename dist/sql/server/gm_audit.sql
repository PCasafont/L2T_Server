CREATE TABLE IF NOT EXISTS `gm_audit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gm` varchar(350) NOT NULL,
  `action` varchar(350) NOT NULL,
  `target` varchar(350) DEFAULT NULL,
  `params` varchar(350) DEFAULT NULL,
  `time` int(11) NOT NULL,
  PRIMARY KEY (`id`)
);