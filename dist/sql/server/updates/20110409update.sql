ALTER TABLE `characters`
  ADD `hunting_bonus` SMALLINT UNSIGNED NOT NULL DEFAULT 0
  AFTER `petId`;
