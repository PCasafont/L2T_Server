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
package l2tserver.gameserver.stats.effects;

import l2tserver.gameserver.model.L2Abnormal;
import l2tserver.gameserver.model.L2Effect;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.templates.skills.L2AbnormalType;
import l2tserver.gameserver.templates.skills.L2EffectTemplate;
import l2tserver.gameserver.templates.skills.L2EffectType;

public class EffectRemoveHide extends L2Effect
{
	public EffectRemoveHide(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.DEBUFF;
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_INVUL;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (!(getEffected() instanceof L2Playable))
			return false;
		
		for (L2Abnormal e : getEffected().getAllEffects())
		{
			if (e != null && e.getType() == L2AbnormalType.HIDE)
			{
				getEffected().onExitChanceEffect(e.getSkill(), e.getSkill().getElement());
				e.exit();
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// Simply stop the effect
		return false;
	}
}