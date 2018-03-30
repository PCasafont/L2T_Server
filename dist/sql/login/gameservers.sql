CREATE TABLE IF NOT EXISTS `gameservers` (
  `server_id` INT(11)     NOT NULL DEFAULT '0',
  `hexid`     VARCHAR(50) NOT NULL DEFAULT '',
  `host`      VARCHAR(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`server_id`)
);
