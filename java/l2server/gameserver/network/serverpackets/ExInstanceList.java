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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Map;

/**
 * @author Pere
 */

public class ExInstanceList extends L2GameServerPacket
{

	private int current = -1;
	private int objId;

	public ExInstanceList(L2PcInstance player)
	{
		objId = player.getObjectId();

		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if (world != null)
		{
			current = world.templateId;
		}
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(current);

		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(objId);

		int size = instanceTimes.size();

		if (instanceTimes.containsKey(current))
		{
			size--;
		}

		writeD(size);

		for (int instanceId : instanceTimes.keySet())
		{
			if (current == instanceId)
			{
				continue;
			}

			int remainingTime = (int) ((instanceTimes.get(instanceId) - System.currentTimeMillis()) / 1000);

			if (remainingTime < 0)
			{
				continue;
			}

			writeD(instanceId);
			writeD(remainingTime);
		}
	}
}
