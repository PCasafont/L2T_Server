DROP TABLE IF EXISTS `offline_buffers`;
CREATE TABLE `offline_buffers` (
  `charId` int(10) unsigned NOT NULL,
  `description` varchar(50) DEFAULT '0',
  `buffs` varchar(200) DEFAULT NULL,
  `coinId` int(10) unsigned NOT NULL,
  PRIMARY KEY (`charId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;