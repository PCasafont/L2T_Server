ALTER TABLE `characters`
  DROP `language`;
ALTER TABLE `characters`
  DROP `petId`;
ALTER TABLE `characters`
  DROP `hunting_bonus`;
ALTER TABLE `characters`
  DROP `unf_pk`;
ALTER TABLE `characters`
  ADD `show_hat` TINYINT UNSIGNED NOT NULL DEFAULT 1;
