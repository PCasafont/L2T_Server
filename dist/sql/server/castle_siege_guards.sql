DROP TABLE IF EXISTS `castle_siege_guards`;
CREATE TABLE IF NOT EXISTS `castle_siege_guards` (
  `castleId`     TINYINT(1) UNSIGNED  NOT NULL DEFAULT '0',
  `id`           SMALLINT(4) UNSIGNED NOT NULL AUTO_INCREMENT,
  `npcId`        SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `x`            MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `y`            MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `z`            MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `heading`      MEDIUMINT(6)         NOT NULL DEFAULT '0',
  `respawnDelay` MEDIUMINT(5)         NOT NULL DEFAULT '0',
  `isHired`      TINYINT(1)           NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `id` (`castleId`)
);
