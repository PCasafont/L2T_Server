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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;
import l2server.gameserver.templates.skills.L2EffectType;

/**
 * @author decad
 */
public class EffectBetray extends L2Effect
{
	public EffectBetray(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#getType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BETRAY;
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.BETRAY;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffector() instanceof L2PcInstance && getEffected() instanceof L2Summon)
		{
			L2PcInstance targetOwner = getEffected().getActingPlayer();
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetOwner);
			return true;
		}
		return false;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
