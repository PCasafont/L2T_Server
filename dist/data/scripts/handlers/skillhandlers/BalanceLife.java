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
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.MobSummonInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.templates.skills.SkillType;

/**
 * This class ...
 *
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class BalanceLife implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.BALANCE_LIFE};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		// Creature activeChar = activeChar;
		// check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);

		if (handler != null) {
			handler.useSkill(activeChar, skill, targets);
		}

		Player player = null;
		if (activeChar instanceof Player) {
			player = (Player) activeChar;
		}

		double fullHP = 0;
		double currentHPs = 0;

		for (Creature target : (Creature[]) targets) {
			// We should not heal if char is dead/
			if (target == null || target.isDead() || target instanceof MobSummonInstance) // Tenkai custom - don't consider coke mobs
			{
				continue;
			}

			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar) {
				if (target instanceof Player && ((Player) target).isCursedWeaponEquipped()) {
					continue;
				} else if (player != null && player.isCursedWeaponEquipped()) {
					continue;
				}
			}

			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}

		double percentHP = currentHPs / fullHP;

		for (Creature target : (Creature[]) targets) {
			if (target == null || target.isDead() || target instanceof MobSummonInstance) // Tenkai custom - don't consider coke mobs
			{
				continue;
			}

			// Player holding a cursed weapon can't be healed and can't heal
			if (target != activeChar) {
				if (target instanceof Player && ((Player) target).isCursedWeaponEquipped()) {
					continue;
				} else if (player != null && player.isCursedWeaponEquipped()) {
					continue;
				}
			}

			double newHP = target.getMaxHp() * percentHP;

			target.setCurrentHp(newHP);

			StatusUpdate su = new StatusUpdate(target, player, StatusUpdateDisplay.NORMAL);
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
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
