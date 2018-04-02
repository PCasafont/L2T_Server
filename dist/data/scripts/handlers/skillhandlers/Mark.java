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
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.SkillType;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.8.2.9 $ $Date: 2005/04/05 19:41:23 $
 */

public class Mark implements ISkillHandler {
	private static final Logger logDamage = Logger.getLogger("damage");
	
	private static final SkillType[] SKILL_IDS = {SkillType.MARK};
	
	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		if (activeChar.isAlikeDead()) {
			return;
		}
		
		Item weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null) {
			weaponInst.setChargedSpiritShot(Item.CHARGED_NONE);
		} else if (activeChar instanceof Summon) {
			Summon activeSummon = (Summon) activeChar;
			activeSummon.setChargedSpiritShot(Item.CHARGED_NONE);
		}
		
		for (WorldObject obj : targets) {
			if (!(obj instanceof Creature)) {
				continue;
			}
			
			Creature target = (Creature) obj;
			
			if (activeChar instanceof Player && target instanceof Player && ((Player) target).isFakeDeath()) {
				target.stopFakeDeath(true);
			} else if (target.isDead()) {
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			
			if (mcrit) {
				int damage = (int) skill.getPower();
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				target.reduceCurrentHp(damage, activeChar, skill);
				
				// Logging damage
				if (Config.LOG_GAME_DAMAGE && activeChar instanceof Playable && damage > Config.LOG_GAME_DAMAGE_THRESHOLD) {
					LogRecord record = new LogRecord(Level.INFO, "");
					record.setParameters(new Object[]{activeChar, " did damage ", damage, skill, " to ", target});
					record.setLoggerName("mdam");
					logDamage.log(record);
				}
			}
			
			if (skill.hasEffects()) {
				skill.getEffects(activeChar,
						target,
						new Env(shld,
								activeChar.getActiveWeaponInstance() != null ? activeChar.getActiveWeaponInstance().getChargedSoulShot() :
										Item.CHARGED_NONE));
			}
		}
		
		// self Effect
		if (skill.hasSelfEffects()) {
			final Abnormal effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
		
		if (skill.isSuicideAttack()) {
			activeChar.doDie(activeChar);
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
