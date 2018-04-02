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
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.item.WeaponType;
import l2server.gameserver.templates.skills.SkillType;

/**
 * @author _tomciaaa_
 */
public class StrSiegeAssault implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.STRSIEGEASSAULT};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {

		if (!(activeChar instanceof Player)) {
			return;
		}

		Player player = (Player) activeChar;

		if (!player.isRidingStrider()) {
			return;
		}
		if (!(player.getTarget() instanceof DoorInstance)) {
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);

		if (castle == null && fort == null) {
			return;
		}

		if (castle != null) {
			if (!player.checkIfOkToUseStriderSiegeAssault(castle)) {
				return;
			}
		} else {
			if (!player.checkIfOkToUseStriderSiegeAssault(fort)) {
				return;
			}
		}

		try {
			// damage calculation
			int damage = 0;

			for (Creature target : (Creature[]) targets) {
				Item weapon = activeChar.getActiveWeaponInstance();
				if (activeChar instanceof Player && target instanceof Player && ((Player) target).isFakeDeath()) {
					target.stopFakeDeath(true);
				} else if (target.isDead()) {
					continue;
				}

				boolean dual = activeChar.isUsingDualWeapon();
				byte shld = Formulas.calcShldUse(activeChar, target, skill);
				boolean crit = Formulas.calcCrit(activeChar.getCriticalHit(target, skill), target);
				double soul = Item.CHARGED_NONE;
				if (weapon != null && weapon.getItemType() != WeaponType.DAGGER) {
					soul = weapon.getChargedSoulShot();
				}

				if (!crit && (skill.getCondition() & Skill.COND_CRIT) != 0) {
					damage = 0;
				} else {
					damage = (int) Formulas.calcPhysSkillDam(activeChar, target, skill, shld, crit, dual, soul);
				}

				if (damage > 0) {
					target.reduceCurrentHp(damage, activeChar, skill);
					if (weapon != null) {
						weapon.setChargedSoulShot(Item.CHARGED_NONE);
					}

					activeChar.sendDamageMessage(target, damage, false, false, false);
				} else {
					activeChar.sendMessage(skill.getName() + " failed.");
				}
			}
		} catch (Exception e) {
			player.sendMessage("Error using siege assault:" + e);
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}

	public static void main(String[] args) {
		new StrSiegeAssault();
	}
}
