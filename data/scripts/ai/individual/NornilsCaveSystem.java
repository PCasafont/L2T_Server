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

package ai.individual;

import ai.group_template.L2AttackableAIScript;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.network.serverpackets.ExSendUIEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 *         <p>
 *         Nornils Cave AI
 */

public class NornilsCaveSystem extends L2AttackableAIScript
{
	private static final int _pointsPerKill = 2;
	private static final int[] _mobs = {23275, 23273, 23272, 23268, 23269};
	private static Map<L2ZoneType, zoneInfo> _roomInfo = new HashMap<L2ZoneType, zoneInfo>(3);

	public NornilsCaveSystem(int id, String name, String descr)
	{
		super(id, name, descr);

		for (int a : _mobs)
		{
			addKillId(a);
		}

		for (int zoneId = 60025; zoneId <= 60027; zoneId++)
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);

			_roomInfo.put(zone, new zoneInfo());

			//Spawn the normal mobs here
			SpawnTable.getInstance().spawnSpecificTable(zone.getName().toLowerCase().replace(" ", "_"));
		}
	}

	private static final class zoneInfo
	{
		private int currentPoints;
		private int zoneStage;

		private void setZoneStage(int a)
		{
			zoneStage = a;
		}

		private void setCurrentPoint(int a)
		{
			currentPoints = a;
		}

		private int getZoneStage()
		{
			return zoneStage;
		}

		private int getCurrentPoints()
		{
			return currentPoints;
		}

		private void reset()
		{
			currentPoints = 0;
			zoneStage = 0;
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		for (Entry<L2ZoneType, zoneInfo> currentZone : _roomInfo.entrySet())
		{
			if (currentZone.getKey().isInsideZone(npc))
			{
				zoneInfo currentInfo = currentZone.getValue();
				int currentPoints = currentInfo.getCurrentPoints();

				if (currentPoints == 100)
				{
					return super.onKill(npc, killer, isPet);
				}

				int calcPoints = currentPoints + _pointsPerKill;
				if (calcPoints >= 100)
				{
					//At this point the Zone should change the mobs
					calcPoints = 100;

					String zoneName = currentZone.getKey().getName().toLowerCase().replace(" ", "_");

					SpawnTable.getInstance().despawnSpecificTable(zoneName);
					SpawnTable.getInstance().spawnSpecificTable(zoneName.concat("_manifest"));

					ThreadPoolManager.getInstance()
							.scheduleGeneral(new changeZoneStage(currentZone.getKey()), 540000); //9 mins
				}

				currentInfo.setCurrentPoint(calcPoints);
				currentZone.getKey().broadcastPacket(new ExSendUIEvent(5, calcPoints, 100, 1802274));
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	private static final class changeZoneStage implements Runnable
	{
		private final L2ZoneType zone;

		public changeZoneStage(L2ZoneType a)
		{
			zone = a;
		}

		@Override
		public void run()
		{
			try
			{
				zoneInfo currentInfo = _roomInfo.get(zone);
				if (currentInfo.getZoneStage() == 0)
				{
					zone.broadcastPacket(new ExSendUIEvent(0, 0, 60, 0, 1811302));

					ThreadPoolManager.getInstance().scheduleGeneral(new changeZoneStage(zone), 60000); //1 min
				}
				else if (currentInfo.getZoneStage() == 1)
				{
					currentInfo.reset();

					String zoneName = zone.getName().toLowerCase().replace(" ", "_");

					SpawnTable.getInstance().despawnSpecificTable(zoneName.concat("_manifest"));
					SpawnTable.getInstance().spawnSpecificTable(zoneName);

					return;
				}
				currentInfo.setZoneStage(currentInfo.getZoneStage() + 1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getOnKillDelay(int npcId)
	{
		return 0;
	}

	public static void main(String[] args)
	{
		new NornilsCaveSystem(-1, "NornilsCaveSystem", "ai");
	}
}
