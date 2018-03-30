ALTER TABLE `characters`
  ADD `templateId` TINYINT UNSIGNED NOT NULL DEFAULT 0
  AFTER `race`;
ALTER TABLE `characters`
  DROP `race`;
