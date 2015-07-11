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

import l2tserver.gameserver.handler.ISkillHandler;
import l2tserver.gameserver.handler.SkillHandler;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Skill;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.StatusUpdate;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.stats.Stats;
import l2tserver.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */

public class CombatPointHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.COMBATPOINTHEAL
	};
	
	/**
	 * 
	 * @see l2tserver.gameserver.handler.ISkillHandler#useSkill(l2tserver.gameserver.model.actor.L2Character, l2tserver.gameserver.model.L2Skill, l2tserver.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character actChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);
		
		if (handler != null)
			handler.useSkill(actChar, skill, targets);
		
		for (L2Character target: (L2Character[]) targets)
		{
			//if (target.isInvul())
			//	continue;
			
			double cp = skill.getPower();
			cp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			
			if ((target.getCurrentCp() + cp) >= target.getMaxCp())
				cp = target.getMaxCp() - target.getCurrentCp();

			cp = Math.min(target.calcStat(Stats.GAIN_CP_LIMIT, target.getMaxCp(), null, null), target.getCurrentCp() + cp) - target.getCurrentCp();
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
			sm.addNumber((int)cp);
			target.sendPacket(sm);
			target.setCurrentCp(target.getCurrentCp() + cp);
			StatusUpdate sump = new StatusUpdate(target);
			sump.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
			target.sendPacket(sump);
		}
	}
	
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}