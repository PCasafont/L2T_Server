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
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2server.gameserver.network.serverpackets.StartRotation;
import l2server.gameserver.network.serverpackets.StopRotation;
import l2server.gameserver.stats.Env;
import l2server.gameserver.templates.skills.L2EffectTemplate;

/**
 * @author decad
 *         <p>
 *         Implementation of the Bluff Effect
 */
public class EffectBluff extends L2Effect
{
	public EffectBluff(Env env, L2EffectTemplate template)
	{
		super(env, template);
	}

	/**
	 * @see l2server.gameserver.model.L2Abnormal#onStart()
	 */
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2NpcInstance)
		{
			return false;
		}

		if (getEffected() instanceof L2Npc && ((L2Npc) getEffected()).getNpcId() == 35062)
		{
			return false;
		}

		if (getEffected() instanceof L2SiegeSummonInstance)
		{
			return false;
		}

		getEffected()
				.broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
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
