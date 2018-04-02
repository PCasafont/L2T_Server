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

package l2server.gameserver.stats.effects;

import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.AbnormalType;
import l2server.gameserver.templates.skills.EffectTemplate;
import l2server.gameserver.templates.skills.EffectType;

import java.util.HashMap;
import java.util.Map;

public class EffectDisarmArmor extends L2Effect {
	private static Map<Integer, Integer> armors = new HashMap<>();
	
	public EffectDisarmArmor(Env env, EffectTemplate template) {
		super(env, template);
	}
	
	/**
	 * @see Abnormal#getType()
	 */
	@Override
	public EffectType getEffectType() {
		return EffectType.DISARM_ARMOR;
	}
	
	@Override
	public AbnormalType getAbnormalType() {
		return AbnormalType.DISARM_ARMOR;
	}
	
	/**
	 * @see Abnormal#onStart()
	 */
	@Override
	public boolean onStart() {
		if (!(getEffected() instanceof Player)) {
			return false;
		}
		
		Player player = (Player) getEffected();
		if (player == null) {
			return false;
		}
		
		// Unequip the armor
		Item armor = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (armor != null) {
			Item[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(armor.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (Item itm : unequiped) {
				iu.addModifiedItem(itm);
				
				synchronized (armors) {
					armors.put(player.getObjectId(), itm.getObjectId());
				}
			}
			
			player.sendPacket(iu);
			
			player.broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0) {
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0]);
				} else {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0]);
				}
				player.sendPacket(sm);
			}
		}
		return true;
	}
	
	/**
	 * @see Abnormal#onExit()
	 */
	@Override
	public void onExit() {
		if (!(getEffected() instanceof Player)) {
			return;
		}
		
		Player player = (Player) getEffected();
		if (player == null) {
			return;
		}
		
		synchronized (armors) {
			if (armors.containsKey(player.getObjectId())) {
				Item armor = player.getInventory().getItemByObjectId(armors.get(player.getObjectId()));
				if (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) == null) {
					if (armor != null) {
						player.useEquippableItem(armor, false);
					}
				}
				armors.remove(player.getObjectId());
			}
		}
		player.broadcastUserInfo();
	}
	
	/**
	 * @see Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime() {
		return false;
	}
}
