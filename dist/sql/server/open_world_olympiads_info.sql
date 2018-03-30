CREATE TABLE IF NOT EXISTS `open_world_olympiads_info` (
  `charId`     INT(11)     NOT NULL,
  `charName`   VARCHAR(35) NOT NULL,
  `score`      INT(11)     NOT NULL DEFAULT 0,
  `lastLogout` BIGINT(13)  NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
);
