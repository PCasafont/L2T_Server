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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectAbortCast extends L2Effect
{
	public EffectAbortCast(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() == null || getEffected() == getEffector())
		{
			return false;
		}

		if (getEffected().isRaid())
		{
			return false;
		}

		if (getEffected().isCastingNow() && getEffected().canAbortCast() && getEffected().getLastSkillCast() != null &&
				getEffected().getLastSkillCast().isMagic())
		{
			getEffected().abortCast();
			if (getEffected() instanceof L2PcInstance)
			{
				// Send a system message
				getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			}
		}
		return true;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return true;
	}
}
