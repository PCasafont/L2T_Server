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

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Fishing;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.templates.skills.SkillType;

public class FishingSkill implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.PUMPING, SkillType.REELING};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		Player player = (Player) activeChar;

		L2Fishing fish = player.getFishCombat();
		if (fish == null) {
			if (skill.getSkillType() == SkillType.PUMPING) {
				//Pumping skill is available only while fishing
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
			} else if (skill.getSkillType() == SkillType.REELING) {
				//Reeling skill is available only while fishing
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING));
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		WeaponTemplate weaponItem = player.getActiveWeaponItem();
		Item weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst == null || weaponItem == null) {
			return;
		}
		int SS = 1;
		int pen = 0;
		if (weaponInst.getChargedFishshot()) {
			SS = 2;
		}
		double gradebonus = 1 + weaponItem.getCrystalType() * 0.1;
		int dmg = (int) (skill.getPower() * gradebonus * SS);
		if (player.getSkillLevel(1315) <= skill.getLevel() - 2) //1315 - Fish Expertise
		{//Penalty
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY));
			pen = 50;
			int penatlydmg = dmg - pen;
			if (player.isGM()) {
				player.sendMessage("Dmg w/o penalty = " + dmg);
			}
			dmg = penatlydmg;
		}
		if (SS > 1) {
			weaponInst.setChargedFishshot(false);
		}
		if (skill.getSkillType() == SkillType.REELING)//Realing
		{
			fish.useRealing(dmg, pen);
		} else
		//Pumping
		{
			fish.usePomping(dmg, pen);
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
