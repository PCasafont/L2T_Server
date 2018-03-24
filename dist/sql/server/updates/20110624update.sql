ALTER TABLE `characters` ADD `templateId` TINYINT unsigned NOT NULL default 0 AFTER `race`;
ALTER TABLE `characters` DROP `race`;