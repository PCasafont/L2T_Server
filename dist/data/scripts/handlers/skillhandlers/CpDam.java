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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.SkillType;

/*
 * Just a quick draft to support Wrath skill. Missing angle based calculation etc.
 */

public class CpDam implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.CPDAM};
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (activeChar.isAlikeDead()) {
			return;
		}
		
		Item weaponInst = activeChar.getActiveWeaponInstance();
		double ssMul = Item.CHARGED_NONE;
		if (weaponInst != null) {
			if (skill.isMagic()) {
				ssMul = weaponInst.getChargedSpiritShot();
				weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
			} else {
				ssMul = weaponInst.getChargedSoulShot();
				weaponInst.setChargedSoulShot(Item.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof Summon) {
			Summon activeSummon = (Summon) activeChar;
			if (skill.isMagic()) {
				ssMul = activeSummon.getChargedSpiritShot();
				activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
			} else {
				ssMul = activeSummon.getChargedSoulShot();
				activeSummon.setChargedSoulShot(Item.CHARGED_NONE);
			}
		} else if (activeChar instanceof Npc) {
			if (skill.isMagic()) {
				ssMul = ((Npc) activeChar).soulshotcharged ? Item.CHARGED_SOULSHOT : Item.CHARGED_NONE;
				((Npc) activeChar).soulshotcharged = false;
			} else {
				ssMul = ((Npc) activeChar).spiritshotcharged ? Item.CHARGED_SPIRITSHOT : Item.CHARGED_NONE;
				((Npc) activeChar).spiritshotcharged = false;
			}
		}
		
		for (Creature target : (Creature[]) targets) {
			if (activeChar instanceof Player && target instanceof Player && ((Player) target).isFakeDeath()) {
				target.stopFakeDeath(true);
			} else if (target.isDead() || target.isInvul(activeChar) ||
					target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar) {
				continue;
			}
			
			byte shld = Formulas.calcShldUse(activeChar, target, skill);
			
			int damage = (int) (target.getCurrentCp() - (target.getCurrentCp() - skill.getPower()));
			
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
				target.breakAttack();
				target.breakCast();
			}
			skill.getEffects(activeChar, target, new Env(shld, ssMul));
			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.setCurrentCp(target.getCurrentCp() - damage);
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
