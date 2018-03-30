CREATE TABLE IF NOT EXISTS `clan_data` (
  `clan_id`                  INT                 NOT NULL DEFAULT 0,
  `clan_name`                VARCHAR(45),
  `clan_level`               INT,
  `reputation_score`         INT                 NOT NULL DEFAULT 0,
  `hasCastle`                INT,
  `ally_id`                  INT,
  `ally_name`                VARCHAR(45),
  `leader_id`                INT UNSIGNED,
  `crest_id`                 INT,
  `crest_large_id`           INT,
  `ally_crest_id`            INT,
  `auction_bid_at`           INT                 NOT NULL DEFAULT 0,
  `ally_penalty_expiry_time` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `ally_penalty_type`        DECIMAL(1)          NOT NULL DEFAULT 0,
  `char_penalty_expiry_time` BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  `dissolving_expiry_time`   BIGINT(13) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan_id`),
  FOREIGN KEY (`leader_id`) REFERENCES `characters` (`charId`)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  KEY `leader_id` (`leader_id`),
  KEY `ally_id` (`ally_id`)
);
