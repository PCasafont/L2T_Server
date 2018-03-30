CREATE TABLE IF NOT EXISTS `ban_timers` (
  `identity` VARCHAR(35)  NOT NULL DEFAULT '0',
  `timer`    INT(15)      NOT NULL DEFAULT '0',
  `author`   VARCHAR(20)  NOT NULL DEFAULT '0',
  `reason`   VARCHAR(255) NOT NULL DEFAULT '0',
  PRIMARY KEY (`identity`)
);
