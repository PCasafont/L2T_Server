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

package handlers.skillhandlers;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.model.zone.type.FishingZone;
import l2server.gameserver.model.zone.type.WaterZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

public class Fishing implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.FISHING};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		Player player = (Player) activeChar;

		/*
		 * If fishing is disabled, there isn't much point in doing anything
		 * else, unless you are GM. so this got moved up here, before anything
		 * else.
		 */
		if (!Config.ALLOWFISHING && !player.isGM()) {
			player.sendMessage("Fishing server is currently offline");
			return;
		}
		if (player.isFishing()) {
			if (player.getFishCombat() != null) {
				player.getFishCombat().doDie(false);
			} else {
				player.endFishing(false);
			}
			// Cancels fishing
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISHING_ATTEMPT_CANCELLED));
			return;
		}
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		if (weaponItem == null || weaponItem.getItemType() != WeaponType.FISHINGROD) {
			// Fishing poles are not installed
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FISHING_POLE_NOT_EQUIPPED));
			return;
		}
		Item lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null) {
			// Bait not equiped.
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING));
			return;
		}
		player.setLure(lure);
		Item lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (lure2 == null || lure2.getCount() < 1) // Not enough bait.
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_BAIT));
			return;
		}
		if (player.isInBoat()) {
			// You can't fish while you are on boat
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_FISH_ON_BOAT));
			if (!player.isGM()) {
				return;
			}
		}
		if (player.isInCraftMode() || player.isInStoreMode()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
			if (!player.isGM()) {
				return;
			}
		}
		if (player.isInsideZone(CreatureZone.ZONE_WATER)) {
			// You can't fish in water
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_FISH_UNDER_WATER));
			if (!player.isGM()) {
				return;
			}
		}
		/*
		 * If fishing is enabled, here is the code that was striped from
		 * startFishing() in Player. Decide now where will the hook be
		 * cast...
		 */
		int rnd = Rnd.get(150) + 50;
		double angle = Util.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int x = player.getX() + (int) (cos * rnd);
		int y = player.getY() + (int) (sin * rnd);
		int z = player.getZ() + 50;
		/*
		 * ...and if the spot is in a fishing zone. If it is, it will then
		 * position the hook on the water surface. If not, you have to be GM to
		 * proceed past here... in that case, the hook will be positioned using
		 * the old Z lookup method.
		 */
		FishingZone aimingTo = null;
		WaterZone water = null;
		boolean canFish = false;
		for (ZoneType zone : ZoneManager.getInstance().getZones(x, y)) {
			if (zone instanceof FishingZone) {
				aimingTo = (FishingZone) zone;
				continue;
			}
			if (zone instanceof WaterZone) {
				water = (WaterZone) zone;
			}
		}
		if (aimingTo != null) {
			// fishing zone found, we can fish here
			if (Config.GEODATA > 0) {
				// geodata enabled, checking if we can see end of the pole
				if (GeoData.getInstance().canSeeTarget(player.getX(), player.getY(), z, x, y, z)) {
					// finding z level for hook
					if (water != null) {
						// water zone exist
						if (GeoData.getInstance().getHeight(x, y, z) < water.getWaterZ()) {
							// water Z is higher than geo Z
							z = water.getWaterZ() + 10;
							canFish = true;
						}
					} else {
						// no water zone, using fishing zone
						if (GeoData.getInstance().getHeight(x, y, z) < aimingTo.getWaterZ()) {
							// fishing Z is higher than geo Z
							z = aimingTo.getWaterZ() + 10;
							canFish = true;
						}
					}
				}
			} else {
				// geodata disabled
				// if water zone exist using it, if not - using fishing zone
				if (water != null) {
					z = water.getWaterZ() + 10;
				} else {
					z = aimingTo.getWaterZ() + 10;
				}
				canFish = true;
			}
		}
		if (!canFish) {
			// You can't fish here
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_FISH_HERE));
			if (!player.isGM()) {
				return;
			}
		}
		// Has enough bait, consume 1 and update inventory. Start fishing
		// follows.
		lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(lure2);
		player.sendPacket(iu);
		// If everything else checks out, actually cast the hook and start
		// fishing... :P
		player.startFishing(x, y, z);
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
