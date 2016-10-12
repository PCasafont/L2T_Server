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
	private int _id;
	private List<ShuttleStop> _stops = new ArrayList<>();

	private int _currentStopId = 0;

	public L2ShuttleInstance(int objectId, L2CharTemplate t, int shuttleId)
	{
		super(objectId, t);
		setInstanceType(InstanceType.L2ShuttleInstance);
		_id = shuttleId;

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
		return _id;
	}

	public List<ShuttleStop> getStops()
	{
		return _stops;
	}

	public boolean isClosed()
	{
		for (ShuttleStop ss : _stops)
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
		_stops.add(new ShuttleStop(x, y, z, time, doorId, outerDoorId, oustX, oustY, oustZ));
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		ShuttleStop current = _stops.get(_currentStopId);
		current.openDoor();

		_currentStopId++;
		if (_currentStopId >= _stops.size())
		{
			_currentStopId = 0;
		}

		ShuttleStop next = _stops.get(_currentStopId);
		if (_passengers.size() > 0)
		{
			_passengers.size();
		}
		List<L2PcInstance> passengersToOust = new ArrayList<>();
		for (L2PcInstance toOust : _passengers)
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
		private List<L2PcInstance> _playersToOust;
		private ShuttleStop _previous, _current;
		private boolean _doorClosed = false;

		public GoTask(List<L2PcInstance> playersToOust, ShuttleStop previous, ShuttleStop current)
		{
			_playersToOust = playersToOust;
			_previous = previous;
			_current = current;
		}

		@Override
		public void run()
		{
			// oust previous players
			if (!_doorClosed)
			{
				Point3D pos = _previous.getOustPosition();
				for (L2PcInstance player : _playersToOust)
				{
					if (L2ShuttleInstance.this._passengers.contains(player))
					{
						player.teleToLocation(pos.getX(), pos.getY(), pos.getZ());
						L2ShuttleInstance.this.oustPlayer(player, pos.getX(), pos.getY(), pos.getZ());
					}
				}
				_previous.closeDoor();
				_doorClosed = true;
				ThreadPoolManager.getInstance().scheduleGeneral(this, 2000L);
				return;
			}
			_current.moveTo();
		}
	}

	public class ShuttleStop
	{
		private Point3D _position;
		private int _time;
		private int _doorId;
		private int _outerDoorId;
		private Point3D _oustPosition;
		private boolean _isDoorOpen;
		private long _lastDoorChange;

		public ShuttleStop(int x, int y, int z, int time, int doorId, int outerDoorId, int oustX, int oustY, int oustZ)
		{
			_position = new Point3D(x, y, z);
			_time = time;
			_doorId = doorId;
			_outerDoorId = outerDoorId;
			_oustPosition = new Point3D(oustX, oustY, oustZ);
			_isDoorOpen = false;
			_lastDoorChange = System.currentTimeMillis();
		}

		public int getId()
		{
			return _doorId;
		}

		public Point3D getPosition()
		{
			return _position;
		}

		public Point3D getOustPosition()
		{
			return _oustPosition;
		}

		public int getTime()
		{
			return _time;
		}

		public boolean isDoorOpen()
		{
			return _isDoorOpen;
		}

		public boolean hasDoorChanged()
		{
			return System.currentTimeMillis() - _lastDoorChange < 1000L;
		}

		public void moveTo()
		{
			MoveData m = new MoveData();
			m.disregardingGeodata = false;
			m.onGeodataPathIndex = -1;
			m._xDestination = _position.getX();
			m._yDestination = _position.getY();
			m._zDestination = _position.getZ();
			m._heading = 0;

			m._moveStartTime = TimeController.getGameTicks();
			_move = m;

			TimeController.getInstance().registerMovingObject(L2ShuttleInstance.this);

			broadcastPacket(new ExShuttleMove(L2ShuttleInstance.this));
		}

		public void openDoor()
		{
			_isDoorOpen = true;
			_lastDoorChange = System.currentTimeMillis();
			DoorTable.getInstance().getDoor(_outerDoorId).openMe();
			updateAbnormalEffect();
		}

		public void closeDoor()
		{
			_isDoorOpen = false;
			_lastDoorChange = System.currentTimeMillis();
			DoorTable.getInstance().getDoor(_outerDoorId).closeMe();
			updateAbnormalEffect();
		}
	}
}
