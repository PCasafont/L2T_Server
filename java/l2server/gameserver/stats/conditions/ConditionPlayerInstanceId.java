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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;

import java.util.ArrayList;

/**
 * The Class ConditionPlayerInstanceId.
 */
public class ConditionPlayerInstanceId extends Condition
{
	private final ArrayList<Integer> _instanceIds;

	/**
	 * Instantiates a new condition player instance id.
	 *
	 * @param instanceIds the instance ids
	 */
	public ConditionPlayerInstanceId(ArrayList<Integer> instanceIds)
	{
		_instanceIds = instanceIds;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}

		final int instanceId = env.player.getInstanceId();

		if (instanceId <= 0)
		{
			return false; // player not in instance
		}

		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld((L2PcInstance) env.player);

		if (world == null)
		{
			return false;
		}

		if (world.instanceId != instanceId)
		{
			return false; // player in the different instance
		}

		return _instanceIds.contains(world.templateId);
	}
}
