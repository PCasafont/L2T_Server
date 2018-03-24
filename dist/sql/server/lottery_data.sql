DROP TABLE IF EXISTS `lottery_data`;
CREATE TABLE `lottery_data` (
  `name` varchar(35) NOT NULL DEFAULT '0',
  `numbers` char(255) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;