CREATE TABLE IF NOT EXISTS `custom_armorsets` (
  `id`              SMALLINT(5) UNSIGNED NOT NULL AUTO_INCREMENT,
  `chest`           SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `legs`            SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `head`            SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `gloves`          SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `feet`            SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `skill`           VARCHAR(70)          NOT NULL DEFAULT '0-0;',
  `shield`          SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `shield_skill_id` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `enchant6skill`   SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `mw_legs`         SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `mw_head`         SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `mw_gloves`       SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `mw_feet`         SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  `mw_shield`       SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`, `chest`)
);
