DROP TABLE IF EXISTS `auth_info`;
CREATE TABLE `auth_info` (
  `userIp` varchar(200) NOT NULL,
  `hardwareId` varchar(200) NOT NULL,
  `windowsUser` varchar(200) NOT NULL,
  `version` varchar(200) NOT NULL,
  `processes` mediumtext NOT NULL,
  `lastUpdateTime` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`userIp`,`hardwareId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
