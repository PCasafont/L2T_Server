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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.VehiclePathPoint;
import l2server.gameserver.model.actor.instance.L2BoatInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2CharTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BoatManager
{
	private Map<Integer, L2BoatInstance> _boats = new HashMap<>();
	private boolean[] _docksBusy = new boolean[3];

	public static final int TALKING_ISLAND = 1;
	public static final int GLUDIN_HARBOR = 2;
	public static final int RUNE_HARBOR = 3;

	public static BoatManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private BoatManager()
	{
		for (int i = 0; i < _docksBusy.length; i++)
		{
			_docksBusy[i] = false;
		}
	}

	public L2BoatInstance getNewBoat(int boatId, int x, int y, int z, int heading)
	{
		if (!Config.ALLOW_BOAT)
		{
			return null;
		}

		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", boatId);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");

		npcDat.set("STR", 0);
		npcDat.set("CON", 0);
		npcDat.set("DEX", 0);
		npcDat.set("INT", 0);
		npcDat.set("WIT", 0);
		npcDat.set("MEN", 0);

		// npcDat.set("name", "");
		npcDat.set("collisionRadius", 0);
		npcDat.set("collisionHeight", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("atkRange", 0);
		npcDat.set("mpMax", 0);
		npcDat.set("cpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("pAtk", 0);
		npcDat.set("mAtk", 0);
		npcDat.set("pAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("mAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("walkSpd", 0);
		npcDat.set("runSpd", 0);
		npcDat.set("hpMax", 50000);
		npcDat.set("hpReg", 3.e-3f);
		npcDat.set("mpReg", 3.e-3f);
		npcDat.set("pDef", 100);
		npcDat.set("mDef", 100);
		L2CharTemplate template = new L2CharTemplate(npcDat);
		L2BoatInstance boat = new L2BoatInstance(IdFactory.getInstance().getNextId(), template);
		_boats.put(boat.getObjectId(), boat);
		boat.setHeading(heading);
		boat.setXYZInvisible(x, y, z);
		boat.spawnMe();
		return boat;
	}

	/**
	 * @param boatId
	 * @return
	 */
	public L2BoatInstance getBoat(int boatId)
	{
		return _boats.get(boatId);
	}

	/**
	 * Lock/unlock dock so only one ship can be docked
	 *
	 * @param h     Dock Id
	 * @param value True if dock is locked
	 */
	public void dockShip(int h, boolean value)
	{
		try
		{
			_docksBusy[h] = value;
		}
		catch (ArrayIndexOutOfBoundsException ignored)
		{
		}
	}

	/**
	 * Check if dock is busy
	 *
	 * @param h Dock Id
	 * @return Trye if dock is locked
	 */
	public boolean dockBusy(int h)
	{
		try
		{
			return _docksBusy[h];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return false;
		}
	}

	/**
	 * Broadcast one packet in both path points
	 */
	public void broadcastPacket(VehiclePathPoint point1, VehiclePathPoint point2, L2GameServerPacket packet)
	{
		double dx, dy;
		final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers().values();
		for (L2PcInstance player : players)
		{
			if (player == null)
			{
				continue;
			}

			dx = (double) player.getX() - point1.x;
			dy = (double) player.getY() - point1.y;
			if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS)
			{
				player.sendPacket(packet);
			}
			else
			{
				dx = (double) player.getX() - point2.x;
				dy = (double) player.getY() - point2.y;
				if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS)
				{
					player.sendPacket(packet);
				}
			}
		}
	}

	/**
	 * Broadcast several packets in both path points
	 */
	public void broadcastPackets(VehiclePathPoint point1, VehiclePathPoint point2, L2GameServerPacket... packets)
	{
		double dx, dy;
		final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers().values();
		for (L2PcInstance player : players)
		{
			if (player == null)
			{
				continue;
			}
			dx = (double) player.getX() - point1.x;
			dy = (double) player.getY() - point1.y;
			if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS)
			{
				for (L2GameServerPacket p : packets)
				{
					player.sendPacket(p);
				}
			}
			else
			{
				dx = (double) player.getX() - point2.x;
				dy = (double) player.getY() - point2.y;
				if (Math.sqrt(dx * dx + dy * dy) < Config.BOAT_BROADCAST_RADIUS)
				{
					for (L2GameServerPacket p : packets)
					{
						player.sendPacket(p);
					}
				}
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final BoatManager _instance = new BoatManager();
	}
}
