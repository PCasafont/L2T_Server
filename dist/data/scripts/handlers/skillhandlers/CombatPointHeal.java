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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */

public class CombatPointHeal implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.COMBATPOINTHEAL};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);

		if (handler != null) {
			handler.useSkill(activeChar, skill, targets);
		}

		for (Creature target : (Creature[]) targets) {
			//if (target.isInvul())
			//	continue;

			double cp = skill.getPower();
			cp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;

			if (target.getCurrentCp() + cp >= target.getMaxCp()) {
				cp = target.getMaxCp() - target.getCurrentCp();
			}

			cp = Math.min(target.calcStat(Stats.GAIN_CP_LIMIT, target.getMaxCp(), null, null), target.getCurrentCp() + cp) - target.getCurrentCp();

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			sm.addNumber((int) cp);
			target.sendPacket(sm);
			target.setCurrentCp(target.getCurrentCp() + cp);
			StatusUpdate sump = new StatusUpdate(target);
			sump.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			target.sendPacket(sump);

			if (skill.hasEffects()) {
				//target.stopSkillEffects(skill.getId());
				skill.getEffects(activeChar, target);
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(skill);
				target.sendPacket(sm);
			}
		}

		if (skill.hasSelfEffects()) {
			Abnormal effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect()) {
				//Replace old effect with new one.
				effect.exit();
			}
			// cast self effect if any
			skill.getEffectsSelf(activeChar);
		}
	}

	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
