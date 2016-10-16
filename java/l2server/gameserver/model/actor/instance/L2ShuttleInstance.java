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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.model.actor.L2Vehicle;
import l2server.gameserver.network.serverpackets.ExShuttleGetOff;
import l2server.gameserver.network.serverpackets.ExShuttleGetOn;
import l2server.gameserver.network.serverpackets.ExShuttleInfo;
import l2server.gameserver.network.serverpackets.ExShuttleMove;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class L2ShuttleInstance extends L2Vehicle
{
	private int id;
	private List<ShuttleStop> stops = new ArrayList<>();

	private int currentStopId = 0;

	public L2ShuttleInstance(int objectId, L2CharTemplate t, int shuttleId)
	{
		super(objectId, t);
		setInstanceType(InstanceType.L2ShuttleInstance);
		this.id = shuttleId;

		getStat().setMoveSpeed(300);
		getAI();
	}

	@Override
	public boolean isShuttle()
	{
		return true;
	}

	public int getId()
	{
		return this.id;
	}

	public List<ShuttleStop> getStops()
	{
		return this.stops;
	}

	public boolean isClosed()
	{
		for (ShuttleStop ss : this.stops)
		{
			if (ss.isDoorOpen())
			{
				return false;
			}
		}
		return true;
	}

	public void addStop(int x, int y, int z, int time, int doorId, int outerDoorId, int oustX, int oustY, int oustZ)
	{
		if (getX() == 0 && getY() == 0 && getZ() == 0)
		{
			setXYZ(x, y, z);
		}
		this.stops.add(new ShuttleStop(x, y, z, time, doorId, outerDoorId, oustX, oustY, oustZ));
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		ShuttleStop current = this.stops.get(this.currentStopId);
		current.openDoor();

		this.currentStopId++;
		if (this.currentStopId >= this.stops.size())
		{
			this.currentStopId = 0;
		}

		ShuttleStop next = this.stops.get(this.currentStopId);
		if (this.passengers.size() > 0)
		{
			this.passengers.size();
		}
		List<L2PcInstance> passengersToOust = new ArrayList<>();
		for (L2PcInstance toOust : this.passengers)
		{
			passengersToOust.add(toOust);
		}
		ThreadPoolManager.getInstance()
				.scheduleGeneral(new GoTask(passengersToOust, current, next), current.getTime() * 1000L);

		return true;
	}

	public boolean addPassenger(L2PcInstance player, int x, int y, int z)
	{
		if (!super.addPassenger(player))
		{
			return false;
		}

		player.setVehicle(this);
		player.setInVehiclePosition(new Point3D(x, y, z));
		player.broadcastPacket(new ExShuttleGetOn(player, this));
		player.setXYZ(getX(), getY(), getZ());
		player.revalidateZone(true);
		return true;
	}

	public void oustPlayer(L2PcInstance player, int x, int y, int z)
	{
		super.oustPlayer(player);
		if (player.isOnline())
		{
			player.broadcastPacket(new ExShuttleGetOff(player, this, x, y, z));
			player.setXYZ(x, y, z);
			player.revalidateZone(true);
		}
		else
		{
			player.setXYZInvisible(x, y, z);
		}
	}

	@Override
	public void updateAbnormalEffect()
	{
		broadcastPacket(new ExShuttleInfo(this));
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExShuttleInfo(this));
	}

	private class GoTask implements Runnable
	{
		private List<L2PcInstance> playersToOust;
		private ShuttleStop previous, current;
		private boolean doorClosed = false;

		public GoTask(List<L2PcInstance> playersToOust, ShuttleStop previous, ShuttleStop current)
		{
			this.playersToOust = playersToOust;
			this.previous = previous;
			this.current = current;
		}

		@Override
		public void run()
		{
			// oust previous players
			if (!this.doorClosed)
			{
				Point3D pos = this.previous.getOustPosition();
				for (L2PcInstance player : this.playersToOust)
				{
					if (L2ShuttleInstance.this.passengers.contains(player))
					{
						player.teleToLocation(pos.getX(), pos.getY(), pos.getZ());
						L2ShuttleInstance.this.oustPlayer(player, pos.getX(), pos.getY(), pos.getZ());
					}
				}
				this.previous.closeDoor();
				this.doorClosed = true;
				ThreadPoolManager.getInstance().scheduleGeneral(this, 2000L);
				return;
			}
			this.current.moveTo();
		}
	}

	public class ShuttleStop
	{
		private Point3D position;
		private int time;
		private int doorId;
		private int outerDoorId;
		private Point3D oustPosition;
		private boolean isDoorOpen;
		private long lastDoorChange;

		public ShuttleStop(int x, int y, int z, int time, int doorId, int outerDoorId, int oustX, int oustY, int oustZ)
		{
			this.position = new Point3D(x, y, z);
			this.time = time;
			this.doorId = doorId;
			this.outerDoorId = outerDoorId;
			this.oustPosition = new Point3D(oustX, oustY, oustZ);
			this.isDoorOpen = false;
			this.lastDoorChange = System.currentTimeMillis();
		}

		public int getId()
		{
			return this.doorId;
		}

		public Point3D getPosition()
		{
			return this.position;
		}

		public Point3D getOustPosition()
		{
			return this.oustPosition;
		}

		public int getTime()
		{
			return this.time;
		}

		public boolean isDoorOpen()
		{
			return this.isDoorOpen;
		}

		public boolean hasDoorChanged()
		{
			return System.currentTimeMillis() - this.lastDoorChange < 1000L;
		}

		public void moveTo()
		{
			MoveData m = new MoveData();
			m.disregardingGeodata = false;
			m.onGeodataPathIndex = -1;
			m.xDestination = this.position.getX();
			m.yDestination = this.position.getY();
			m.zDestination = this.position.getZ();
			m.heading = 0;

			m.moveStartTime = TimeController.getGameTicks();
			move = m;

			TimeController.getInstance().registerMovingObject(L2ShuttleInstance.this);

			broadcastPacket(new ExShuttleMove(L2ShuttleInstance.this));
		}

		public void openDoor()
		{
			this.isDoorOpen = true;
			this.lastDoorChange = System.currentTimeMillis();
			DoorTable.getInstance().getDoor(this.outerDoorId).openMe();
			updateAbnormalEffect();
		}

		public void closeDoor()
		{
			this.isDoorOpen = false;
			this.lastDoorChange = System.currentTimeMillis();
			DoorTable.getInstance().getDoor(this.outerDoorId).closeMe();
			updateAbnormalEffect();
		}
	}
}
