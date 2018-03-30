CREATE TABLE IF NOT EXISTS `olympiad_fights` (
  `charOneId`    INT(10) UNSIGNED    NOT NULL,
  `charTwoId`    INT(10) UNSIGNED    NOT NULL,
  `charOneClass` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `charTwoClass` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `winner`       TINYINT(1) UNSIGNED NOT NULL DEFAULT '0',
  `start`        BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `time`         BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `classed`      TINYINT(1) UNSIGNED NOT NULL DEFAULT '0',
  KEY `charOneId` (`charOneId`),
  KEY `charTwoId` (`charTwoId`),
  FOREIGN KEY (`charOneId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (`charTwoId`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);
