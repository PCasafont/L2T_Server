CREATE TABLE IF NOT EXISTS `castle` (
  `id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `taxPercent` int(11) NOT NULL DEFAULT '15',
  `treasury` bigint(20) NOT NULL DEFAULT '0',
  `siegeDate` bigint(13) unsigned NOT NULL DEFAULT '0',
  `regTimeOver` enum('true','false') NOT NULL DEFAULT 'true',
  `regTimeEnd` bigint(13) unsigned NOT NULL DEFAULT '0',
  `showNpcCrest` enum('true','false') NOT NULL DEFAULT 'false',
  `bloodAlliance` int(10) NOT NULL DEFAULT '0',
  `tendency` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
);

INSERT IGNORE INTO `castle` VALUES
(1,'Gludio',0,0,0,'true',0,'false', 0, 1),
(2,'Dion',0,0,0,'true',0,'false', 0, 1),
(3,'Giran',0,0,0,'true',0,'false', 0, 1),
(4,'Oren',0,0,0,'true',0,'false', 0, 1),
(5,'Aden',0,0,0,'true',0,'false', 0, 1),
(6,'Innadril',0,0,0,'true',0,'false', 0, 1),
(7,'Goddard',0,0,0,'true',0,'false', 0, 1),
(8,'Rune',0,0,0,'true',0,'false', 0, 1),
(9,'Schuttgart',0,0,0,'true',0,'false', 0, 1);