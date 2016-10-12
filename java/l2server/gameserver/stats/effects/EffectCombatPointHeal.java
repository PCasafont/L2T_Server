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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2EffectTemplate;

public class EffectCombatPointHeal extends L2Effect
{
	public EffectCombatPointHeal(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2AbnormalType getAbnormalType()
	{
		return L2AbnormalType.BUFF;
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if (target.isInvul(getEffector()))
		{
			return false;
		}

		double cp = calc();

		if (target.getCurrentCp() + cp > target.getMaxCp())
		{
			cp = target.getMaxCp() - target.getCurrentCp();
		}
		target.setCurrentCp(cp + target.getCurrentCp());

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
		sm.addNumber((int) cp);
		target.sendPacket(sm);
		return false;
	}
}
