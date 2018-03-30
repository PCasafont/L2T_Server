CREATE TABLE IF NOT EXISTS `fort_functions` (
  `fort_id` INT(2)              NOT NULL DEFAULT '0',
  `type`    INT(1)              NOT NULL DEFAULT '0',
  `lvl`     INT(3)              NOT NULL DEFAULT '0',
  `lease`   INT(10)             NOT NULL DEFAULT '0',
  `rate`    DECIMAL(20, 0)      NOT NULL DEFAULT '0',
  `endTime` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`fort_id`, `type`)
);
