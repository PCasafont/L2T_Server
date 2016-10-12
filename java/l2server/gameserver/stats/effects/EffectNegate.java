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

import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author Gnat
 */
public class EffectNegate extends L2Effect
{
	public EffectNegate(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onStart()
	{
		L2Skill skill = getSkill();

		for (int negateSkillId : skill.getNegateId())
		{
			if (negateSkillId != 0)
			{
				getEffected().stopSkillEffects(negateSkillId);
			}
		}
		for (L2AbnormalType negateEffectType : skill.getNegateStats())
		{
			getEffected().stopEffects(negateEffectType);
		}
		if (skill.getNegateAbnormals() != null)
		{
			for (L2Abnormal effect : getEffected().getAllEffects())
			{
				if (effect == null)
				{
					continue;
				}

				for (String negateAbnormalType : skill.getNegateAbnormals().keySet())
				{
					for (String stackType : effect.getStackType())
					{
						if (negateAbnormalType.equalsIgnoreCase(stackType) &&
								skill.getNegateAbnormals().get(negateAbnormalType) >= effect.getStackLvl())
						{
							effect.exit();
							break;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
