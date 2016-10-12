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

package l2server.gameserver.model.actor;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TimeController;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.knownlist.VehicleKnownList;
import l2server.gameserver.model.actor.stat.VehicleStat;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * @author DS
 */
public abstract class L2Vehicle extends L2Character
{
	protected int _dockId = 0;
	protected final ArrayList<L2PcInstance> _passengers = new ArrayList<>();
	protected Location _oustLoc = null;
	private Runnable _engine = null;

	protected VehiclePathPoint[] _currentPath = null;
	protected int _runState = 0;

	public L2Vehicle(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2Vehicle);
		setIsFlying(true);
	}

	public boolean isBoat()
	{
		return false;
	}

	public boolean isAirShip()
	{
		return false;
	}

	public boolean isShuttle()
	{
		return false;
	}

	public boolean canBeControlled()
	{
		return _engine == null;
	}

	public void registerEngine(Runnable r)
	{
		_engine = r;
	}

	public void runEngine(int delay)
	{
		if (_engine != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(_engine, delay);
		}
	}

	public void executePath(VehiclePathPoint[] path)
	{
		_runState = 0;
		_currentPath = path;

		if (_currentPath != null && _currentPath.length > 0)
		{
			final VehiclePathPoint point = _currentPath[0];
			if (point.moveSpeed > 0)
			{
				getStat().setMoveSpeed(point.moveSpeed);
			}
			if (point.rotationSpeed > 0)
			{
				getStat().setRotationSpeed(point.rotationSpeed);
			}

			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(point.x, point.y, point.z, 0));
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		_move = null;

		if (_currentPath != null)
		{
			_runState++;
			if (_runState < _currentPath.length)
			{
				final VehiclePathPoint point = _currentPath[_runState];
				if (!isMovementDisabled())
				{
					if (point.moveSpeed == 0)
					{
						teleToLocation(point.x, point.y, point.z, point.rotationSpeed, false);
						_currentPath = null;
					}
					else
					{
						if (point.moveSpeed > 0)
						{
							getStat().setMoveSpeed(point.moveSpeed);
						}
						if (point.rotationSpeed > 0)
						{
							getStat().setRotationSpeed(point.rotationSpeed);
						}

						MoveData m = new MoveData();
						m.disregardingGeodata = false;
						m.onGeodataPathIndex = -1;
						m._xDestination = point.x;
						m._yDestination = point.y;
						m._zDestination = point.z;
						m._heading = 0;

						final double dx = point.x - getX();
						final double dy = point.y - getY();
						final double distance = Math.sqrt(dx * dx + dy * dy);
						if (distance > 1) // vertical movement heading check
						{
							setHeading(Util.calculateHeadingFrom(getX(), getY(), point.x, point.y));
						}

						m._moveStartTime = TimeController.getGameTicks();
						_move = m;

						TimeController.getInstance().registerMovingObject(this);
						return true;
					}
				}
			}
			else
			{
				_currentPath = null;
			}
		}

		runEngine(10);
		return false;
	}

	@Override
	public void initKnownList()
	{
		setKnownList(new VehicleKnownList(this));
	}

	@Override
	public VehicleStat getStat()
	{
		return (VehicleStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new VehicleStat(this));
	}

	public boolean isInDock()
	{
		return _dockId > 0;
	}

	public int getDockId()
	{
		return _dockId;
	}

	public void setInDock(int d)
	{
		_dockId = d;
	}

	public void setOustLoc(Location loc)
	{
		_oustLoc = loc;
	}

	public Location getOustLoc()
	{
		return _oustLoc != null ? _oustLoc :
				MapRegionTable.getInstance().getTeleToLocation(this, MapRegionTable.TeleportWhereType.Town);
	}

	public void oustPlayers()
	{
		L2PcInstance player;

		// Use iterator because oustPlayer will try to remove player from _passengers
		final Iterator<L2PcInstance> iter = _passengers.iterator();
		while (iter.hasNext())
		{
			player = iter.next();
			iter.remove();
			if (player != null)
			{
				oustPlayer(player);
			}
		}
	}

	public void oustPlayer(L2PcInstance player)
	{
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		removePassenger(player);
	}

	public boolean addPassenger(L2PcInstance player)
	{
		if (player == null || _passengers.contains(player))
		{
			return false;
		}

		// already in other vehicle
		if (player.getVehicle() != null && player.getVehicle() != this)
		{
			return false;
		}

		_passengers.add(player);
		return true;
	}

	public void removePassenger(L2PcInstance player)
	{
		try
		{
			_passengers.remove(player);
		}
		catch (Exception ignored)
		{
		}
	}

	public boolean isEmpty()
	{
		return _passengers.isEmpty();
	}

	public List<L2PcInstance> getPassengers()
	{
		return _passengers;
	}

	public void broadcastToPassengers(L2GameServerPacket sm)
	{
		for (L2PcInstance player : _passengers)
		{
			if (player != null)
			{
				player.sendPacket(sm);
			}
		}
	}

	/**
	 * Consume ticket(s) and teleport player from boat if no correct ticket
	 *
	 * @param itemId Ticket itemId
	 * @param count  Ticket count
	 * @param oustX
	 * @param oustY
	 * @param oustZ
	 */
	public void payForRide(int itemId, int count, int oustX, int oustY, int oustZ)
	{
		final Collection<L2PcInstance> passengers = getKnownList().getKnownPlayersInRadius(1000);
		if (passengers != null && !passengers.isEmpty())
		{
			L2ItemInstance ticket;
			InventoryUpdate iu;
			for (L2PcInstance player : passengers)
			{
				if (player == null)
				{
					continue;
				}
				if (player.isInBoat() && player.getBoat() == this)
				{
					if (itemId > 0)
					{
						ticket = player.getInventory().getItemByItemId(itemId);
						if (ticket == null ||
								player.getInventory().destroyItem("Boat", ticket, count, player, this) == null)
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_CORRECT_BOAT_TICKET));
							player.teleToLocation(oustX, oustY, oustZ, true);
							continue;
						}
						iu = new InventoryUpdate();
						iu.addModifiedItem(ticket);
						player.sendPacket(iu);
					}
					addPassenger(player);
				}
			}
		}
	}

	@Override
	public boolean updatePosition(int gameTicks)
	{
		final boolean result = super.updatePosition(gameTicks);

		for (L2PcInstance player : _passengers)
		{
			if (player != null && player.getVehicle() == this)
			{
				player.getPosition().setXYZ(getX(), getY(), getZ());
				player.revalidateZone(false);
			}
		}

		return result;
	}

	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if (isMoving())
		{
			stopMove(null, false);
		}

		setIsTeleporting(true);

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		for (L2PcInstance player : _passengers)
		{
			if (player != null)
			{
				player.teleToLocation(x, y, z);
			}
		}

		decayMe();
		setXYZ(x, y, z);

		// temporary fix for heading on teleports
		if (heading != 0)
		{
			getPosition().setHeading(heading);
		}

		onTeleported();
		revalidateZone(true);
	}

	@Override
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		_move = null;
		if (pos != null)
		{
			setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}

		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
		{
			getKnownList().findObjects();
		}
	}

	@Override
	public void deleteMe()
	{
		_engine = null;

		try
		{
			if (isMoving())
			{
				stopMove(null);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed stopMove().", e);
		}

		try
		{
			oustPlayers();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed oustPlayers().", e);
		}

		final L2WorldRegion oldRegion = getWorldRegion();

		try
		{
			decayMe();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed decayMe().", e);
		}

		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}

		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Failed cleaning knownlist.", e);
		}

		// Remove L2Object object from _allObjects of L2World
		L2World.getInstance().removeObject(this);

		super.deleteMe();
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public int getLevel()
	{
		return 0;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public void setAI(L2CharacterAI newAI)
	{
		if (_ai == null)
		{
			_ai = newAI;
		}
	}

	public class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public void detachAI()
		{
		}
	}
}
