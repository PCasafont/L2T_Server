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

import l2server.gameserver.model.L2Effect;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectServitorShare extends L2Effect
{
	public EffectServitorShare(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		if (!(getEffected() instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) getEffected();
		for (L2Summon summon : player.getSummons())
		{
			summon.updateAndBroadcastStatus(1);
		}

		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public void onExit()
	{
		if (!(getEffected() instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) getEffected();
		if (player.getSummons().size() != 0)
		{
			for (L2Summon summon : player.getSummons())
			{
				summon.updateAndBroadcastStatus(1);
			}
		}
	}
}
