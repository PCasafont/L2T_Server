ALTER TABLE `character_offline_trade_items`
  MODIFY `charId` INT(10) UNSIGNED NOT NULL;

ALTER TABLE `character_offline_trade`
  MODIFY `charId` INT(10) UNSIGNED NOT NULL;

UPDATE `character_reco_bonus`
SET `rec_have` = 255
WHERE `rec_have` > 255;
ALTER TABLE `character_reco_bonus`
  MODIFY `rec_have` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0';

