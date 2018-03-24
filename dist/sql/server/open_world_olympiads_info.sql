CREATE TABLE IF NOT EXISTS `open_world_olympiads_info` (
  `charId` int(11) NOT NULL,
  `charName` VARCHAR(35) NOT NULL,
  `score` int(11) NOT NULL DEFAULT 0,
  `lastLogout` bigint(13) NOT NULL DEFAULT 0,
  PRIMARY KEY (`charId`)
);
