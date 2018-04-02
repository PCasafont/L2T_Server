/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExItemAppearanceResult;
import l2server.gameserver.network.serverpackets.ExPutTemplateResultForItemAppearance;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.ArmorType;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.ItemType;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public final class RequestPutTemplateForItemAppearance extends L2GameClientPacket {
	private int objectId1;
	private int objectId2;
	
	@Override
	protected void readImpl() {
		objectId1 = readD();
		objectId2 = readD();
	}
	
	@Override
	protected void runImpl() {
		if (objectId1 == objectId2) {
			return;
		}
		
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		
		Item stone = player.getActiveAppearanceStone();
		if (stone == null) {
			return;
		}
		
		Item target = player.getInventory().getItemByObjectId(objectId1);
		if (target == null) {
			return;
		}
		
		Item template = player.getInventory().getItemByObjectId(objectId2);
		if (template == null) {
			return;
		}
		
		if (!template.getItem().canBeUsedAsApp() || template.getTime() > 0) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_CANNOT_APPEARENCE_WEAPON));
			return;
		}

		/*boolean isCostume = false;
		switch (template.getItemId())
		{
			case 23448: // Metal Suit Top
			case 23449: // Metal Suit Pants
			case 23450: // Metal Suit Gloves
			case 23451: // Metal Suit Shoes
			case 23452: // Metal Suit Hair Accessory
			case 23453: // Military Top
			case 23454: // Military Pants
			case 23455: // Military Gloves
			case 23456: // Military Shoes
			case 23457: // Military Hair Accessory
			case 23179: // Ninja Top
			case 23180: // Ninja Pants
			case 23181: // Ninja Gloves
			case 23182: // Ninja Shoes
			case 23183: // Ninja Hair Accessory
			case 23184: // Traditional Taiwanese Top
			case 23185: // Traditional Taiwanese Pants
			case 23186: // Traditional Taiwanese Gloves
			case 23187: // Traditional Taiwanese Shoes
			case 23188: // Traditional Taiwanese Hair Accessory
			case 23883: // Vampire Top
			case 23884: // Vampire Pants
			case 23885: // Vampire Gloves
			case 23886: // Vampire Shoes
			case 23887: // Vampire Hair Accessory
			case 23881: // Anakim Outfit
			case 23458: // Maid Costume
			case 6408: // Formal Wear
			case 23879: // Samurai Outfit
			case 23877: // Samurai Hair Accessory
			case 24133: // Dark Assassin Suit
			case 26355: // Pirate Captain Outfit
			case 26356: // Pirate Crew Outfit
			case 46203: // Halloween Outfit
			case 46260: // Dark Knight Outfit
			case 46548: // Freya Top
			case 46549: // Freya Pants
			case 46550: // Freya Gloves
			case 46551: // Freya Shoes
			case 46543: // Kelbim Top
			case 46544: // Kelbim Pants
			case 46545: // Kelbim Gloves
			case 46546: // Kelbim Shoes
			case 46538: // Tauti Top
			case 46539: // Tauti Pants
			case 46540: // Tauti Gloves
			case 46541: // Tauti Shoes
			case 46595: // Dynasty Soul Heavy Outfit
			case 46596: // Dynasty Soul Light Outfit
			case 46597: // Dynasty Soul Robe Outfit
			case 46598: // Zubei Soul Heavy Outfit
			case 46599: // Zubei Soul Light Outfit
			case 46600: // Zubei Soul Robe Outfit
				isCostume = true;
				break;
		}*/
		
		int type = stone.getStoneType();
		int itemType = target.getItem().getType2();
		int itemTypeTemp = template.getItem().getType2();
		if (target.getItem().getBodyPart() == ItemTemplate.SLOT_BACK) {
			itemType = ItemTemplate.TYPE2_SHIELD_ARMOR;
		}
		if (template.getItem().getBodyPart() == ItemTemplate.SLOT_BACK) {
			itemTypeTemp = ItemTemplate.TYPE2_SHIELD_ARMOR;
		}
		
		boolean isCorrectType = type == -1 || itemType == itemTypeTemp;
		boolean isCorrectGrade =
				target.getItem().getItemGradePlain() == stone.getItem().getItemGradePlain() || target.getItem().getBodyPart() == ItemTemplate.SLOT_BACK;
		
		boolean valid = true;
		if (!isCorrectGrade || !isCorrectType || target.getItem().getItemGrade() < template.getItem().getItemGrade()) {
			valid = false;
		}
		
		ItemType templateType = template.getItem().getItemType();
		ItemType targetType = target.getItem().getItemType();
		int targetBodyPart = target.getItem().getBodyPart();
		int templateBodyPart = template.getItem().getBodyPart();
		if (valid && targetType != templateType) {
			valid = templateType == ArmorType.NONE && templateBodyPart == ItemTemplate.SLOT_ALLDRESS;
			
			if (Config.isServer(Config.TENKAI)) {
				Map<ItemType, Integer> typeGroups = new HashMap<>();
				typeGroups.put(WeaponType.SWORD, 0);
				typeGroups.put(WeaponType.BLUNT, 0);
				typeGroups.put(WeaponType.DUAL, 1);
				typeGroups.put(WeaponType.DUALBLUNT, 1);
				typeGroups.put(WeaponType.ANCIENTSWORD, 2);
				typeGroups.put(WeaponType.BIGBLUNT, 2);
				typeGroups.put(WeaponType.BIGSWORD, 2);
				
				typeGroups.put(ArmorType.HEAVY, 3);
				typeGroups.put(ArmorType.LIGHT, 3);
				typeGroups.put(ArmorType.MAGIC, 3);
				typeGroups.put(ArmorType.NONE, 4);
				
				int templateTypeGroup = -1;
				if (typeGroups.containsKey(templateType)) {
					templateTypeGroup = typeGroups.get(templateType);
				}
				
				int targetTypeGroup = -1;
				if (typeGroups.containsKey(targetType)) {
					targetTypeGroup = typeGroups.get(targetType);
				}
				
				if (templateTypeGroup >= 0 && templateTypeGroup == targetTypeGroup) {
					valid = true;
				}
			}
		}
		
		if (valid && targetBodyPart != templateBodyPart) {
			valid = (targetBodyPart == ItemTemplate.SLOT_FULL_ARMOR || targetBodyPart == ItemTemplate.SLOT_CHEST) &&
					(templateBodyPart == ItemTemplate.SLOT_ALLDRESS || templateBodyPart == ItemTemplate.SLOT_FULL_ARMOR || templateBodyPart == ItemTemplate.SLOT_CHEST);
		}
		
		if (!valid) {
			sendPacket(new ExPutTemplateResultForItemAppearance(0));
			return;
		}
		
		sendPacket(new ExPutTemplateResultForItemAppearance(2));
		
		int templateId = template.getItemId();
		if (!player.destroyItem("Appearance", stone.getObjectId(), 1, player, true) || !player.destroyItem("Appearance", template, player, true)) {
			sendPacket(new ExPutTemplateResultForItemAppearance(0));
			return;
		}
		
		target.setAppearance(templateId);
		sendPacket(new ExItemAppearanceResult(1, target));
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(target);
		player.sendPacket(iu);
		player.broadcastUserInfo();
		
		Util.logToFile(player.getName() + " is applying " + template.getName() + " on his " + target.getName() + ".",
				"Appearances",
				"txt",
				true,
				true);
	}
}
