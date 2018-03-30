-- 
-- Table structure for table `mods_couples`
-- 

CREATE TABLE IF NOT EXISTS `mods_wedding` (
  `id`           INT(11) NOT NULL AUTO_INCREMENT,
  `player1Id`    INT(11) NOT NULL DEFAULT '0',
  `player2Id`    INT(11) NOT NULL DEFAULT '0',
  `married`      VARCHAR(5)       DEFAULT NULL,
  `affianceDate` DECIMAL(20, 0)   DEFAULT '0',
  `weddingDate`  DECIMAL(20, 0)   DEFAULT '0',
  PRIMARY KEY (`id`)
)
  ENGINE = MyISAM;
