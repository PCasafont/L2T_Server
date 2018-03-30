DROP TABLE IF EXISTS `random_spawn`;
CREATE TABLE `random_spawn` (
  `groupId`        TINYINT(3) UNSIGNED    NOT NULL,
  `npcId`          SMALLINT(5) UNSIGNED   NOT NULL,
  `count`          TINYINT(1) UNSIGNED    NOT NULL DEFAULT '1',
  `initialDelay`   INT(8)                 NOT NULL DEFAULT '-1',
  `respawnDelay`   INT(8)                 NOT NULL DEFAULT '-1',
  `despawnDelay`   INT(8)                 NOT NULL DEFAULT '-1',
  `broadcastSpawn` ENUM ('true', 'false') NOT NULL DEFAULT 'false',
  `randomSpawn`    ENUM ('true', 'false') NOT NULL DEFAULT 'true',
  PRIMARY KEY (`groupId`)
);

INSERT INTO `random_spawn` VALUES
  -- (2,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  -- (3,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  -- (4,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  -- (5,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  -- (6,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  -- (7,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  -- (8,31092,1,-1,60,0,'false','false'), -- Black Marketeer of Mammon
  (112, 27316, 1, 1800000, 14400000, 1800000, 'false', 'false'), -- Fallen Chieftain Vegus
  (133, 32014, 1, -1, 1800000, 1800000, 'false', 'true'), -- Ivan (Runaway Youth quest)
  (134, 32013, 1, -1, 1800000, 1800000, 'false', 'true'), -- Suki (Wild Maiden quest)
  (135, 32049, 1, -1, 1200000, 1200000, 'false', 'true'), -- Rooney (Blacksmith of wind Rooney)
  (136, 32012, 1, -1, 1800000, 1800000, 'false', 'true'), -- Tantan (Aged ExAdventurer quest)
  (137, 32335, 1, -1, 120000, 120000, 'false', 'true'), -- Marksman (Guards on kamael island)
  (138, 32335, 1, -1, 120000, 120000, 'false', 'true'), -- Marksman (Guards on kamael island)
  (139, 32335, 1, -1, 120000, 120000, 'false', 'true'), -- Marksman (Guards on kamael island)
  (140, 32335, 1, -1, 120000, 120000, 'false', 'true'), -- Marksman (Guards on kamael island)
  (141, 32335, 1, -1, 120000, 120000, 'false', 'true'); -- Marksman (Guards on kamael island)
