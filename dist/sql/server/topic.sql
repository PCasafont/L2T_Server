CREATE TABLE IF NOT EXISTS `topic` (
  `topic_id`        INT(8)              NOT NULL DEFAULT '0',
  `topic_forum_id`  INT(8)              NOT NULL DEFAULT '0',
  `topic_name`      VARCHAR(255)        NOT NULL DEFAULT '',
  `topic_date`      BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `topic_ownername` VARCHAR(255)        NOT NULL DEFAULT '0',
  `topic_ownerid`   INT(8)              NOT NULL DEFAULT '0',
  `topic_type`      INT(8)              NOT NULL DEFAULT '0',
  `topic_reply`     INT(8)              NOT NULL DEFAULT '0'
);
