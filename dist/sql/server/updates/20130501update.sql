ALTER TABLE `character_buff_lists`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_contacts`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_enchanted_buffs`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_friends`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_hennas`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_instance_time`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_macroses`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_mentees`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_norestart_zone_time`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_offline_trade_items`
  ADD PRIMARY KEY (`charId`, `item`);
ALTER TABLE `character_offline_trade_items`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_offline_trade`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_premium_items`
  MODIFY `charId` INT UNSIGNED;
ALTER TABLE `character_premium_items`
  ADD PRIMARY KEY (`charId`, `itemNum`);
ALTER TABLE `character_premium_items`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_quest_global_data`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_quests`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_raid_points`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_recipebook`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_recipeshoplist`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_reco_bonus`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_shortcuts`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_skills`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_skills_save`
  MODIFY `charId` INT UNSIGNED;
ALTER TABLE `character_skills_save`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_subclasses`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_tpbookmark`
  MODIFY `charId` INT UNSIGNED;
ALTER TABLE `character_tpbookmark`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_ui_actions`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `character_ui_categories`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_data`
  MODIFY `leader_id` INT UNSIGNED;
ALTER TABLE `clan_data`
  ADD FOREIGN KEY (`leader_id`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `cursed_weapons`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `heroes`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `heroes_diary`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
UPDATE `items` i
SET `owner_id` = (SELECT `leader_id`
                  FROM `clan_data`
                  WHERE `clan_id` = i.`owner_id`)
WHERE loc LIKE "CLANWH";
ALTER TABLE `items`
  MODIFY `owner_id` INT UNSIGNED;
ALTER TABLE `items`
  ADD FOREIGN KEY (`owner_id`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `item_auction_bid`
  MODIFY `playerObjId` INT UNSIGNED;
ALTER TABLE `item_auction_bid`
  ADD FOREIGN KEY (`playerObjId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `olympiad_fights`
  ADD FOREIGN KEY (`charOneId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `olympiad_fights`
  ADD FOREIGN KEY (`charTwoId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `olympiad_nobles`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `olympiad_nobles_eom`
  ADD FOREIGN KEY (`charId`) REFERENCES `characters` (`charId`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;

ALTER TABLE `item_attributes`
  ADD FOREIGN KEY (`itemId`) REFERENCES `items` (`object_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `item_elementals`
  ADD FOREIGN KEY (`itemId`) REFERENCES `items` (`object_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `pets`
  MODIFY `item_obj_id` INT;
ALTER TABLE `pets`
  ADD FOREIGN KEY (`item_obj_id`) REFERENCES `items` (`object_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;

ALTER TABLE `auction_bid`
  ADD FOREIGN KEY (`bidderId`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_notices`
  ADD FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_privs`
  ADD FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_skills`
  ADD FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_subpledges`
  ADD FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_wars`
  MODIFY `clan1` INT;
ALTER TABLE `clan_wars`
  ADD FOREIGN KEY (`clan1`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `clan_wars`
  MODIFY `clan2` INT;
ALTER TABLE `clan_wars`
  ADD FOREIGN KEY (`clan2`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
ALTER TABLE `siege_clans`
  ADD FOREIGN KEY (`clan_id`) REFERENCES `clan_data` (`clan_id`)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
