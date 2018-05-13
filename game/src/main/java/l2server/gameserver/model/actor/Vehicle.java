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
import l2server.gameserver.ai.CreatureAI;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.knownlist.VehicleKnownList;
import l2server.gameserver.model.actor.stat.VehicleStat;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.CreatureTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;
import l2server.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author DS
 */
public abstract class Vehicle extends Creature {
	
	private static Logger log = LoggerFactory.getLogger(Vehicle.class.getName());
	
	protected int dockId = 0;
	protected final ArrayList<Player> passengers = new ArrayList<>();
	protected Location oustLoc = null;
	private Runnable engine = null;

	protected VehiclePathPoint[] currentPath = null;
	protected int runState = 0;

	public Vehicle(int objectId, CreatureTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2Vehicle);
		setFlying(true);
	}

	public boolean isBoat() {
		return false;
	}

	public boolean isAirShip() {
		return false;
	}

	public boolean isShuttle() {
		return false;
	}

	public boolean canBeControlled() {
		return engine == null;
	}

	public void registerEngine(Runnable r) {
		engine = r;
	}

	public void runEngine(int delay) {
		if (engine != null) {
			ThreadPoolManager.getInstance().scheduleGeneral(engine, delay);
		}
	}

	public void executePath(VehiclePathPoint[] path) {
		runState = 0;
		currentPath = path;

		if (currentPath != null && currentPath.length > 0) {
			final VehiclePathPoint point = currentPath[0];
			if (point.moveSpeed > 0) {
				getStat().setMoveSpeed(point.moveSpeed);
			}
			if (point.rotationSpeed > 0) {
				getStat().setRotationSpeed(point.rotationSpeed);
			}

			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(point.x, point.y, point.z, 0));
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean moveToNextRoutePoint() {
		setMove(null);

		if (currentPath != null) {
			runState++;
			if (runState < currentPath.length) {
				final VehiclePathPoint point = currentPath[runState];
				if (!isMovementDisabled()) {
					if (point.moveSpeed == 0) {
						teleToLocation(point.x, point.y, point.z, point.rotationSpeed, false);
						currentPath = null;
					} else {
						if (point.moveSpeed > 0) {
							getStat().setMoveSpeed(point.moveSpeed);
						}
						if (point.rotationSpeed > 0) {
							getStat().setRotationSpeed(point.rotationSpeed);
						}

						MoveData m = new MoveData();
						m.setDisregardingGeodata(false);
						m.setOnGeodataPathIndex(-1);
						m.setXDestination(point.x);
						m.setYDestination(point.y);
						m.setZDestination(point.z);
						m.setHeading(0);

						final double dx = point.x - getX();
						final double dy = point.y - getY();
						final double distance = Math.sqrt(dx * dx + dy * dy);
						if (distance > 1) // vertical movement heading check
						{
							setHeading(Util.calculateHeadingFrom(getX(), getY(), point.x, point.y));
						}

						m.setMoveStartTime(TimeController.getGameTicks());
						setMove(m);

						TimeController.getInstance().registerMovingObject(this);
						return true;
					}
				}
			} else {
				currentPath = null;
			}
		}

		runEngine(10);
		return false;
	}

	@Override
	public VehicleKnownList initialKnownList() {
		return new VehicleKnownList(this);
	}

	@Override
	public VehicleStat getStat() {
		return (VehicleStat) super.getStat();
	}

	@Override
	public void initCharStat() {
		setStat(new VehicleStat(this));
	}

	public boolean isInDock() {
		return dockId > 0;
	}

	public int getDockId() {
		return dockId;
	}

	public void setInDock(int d) {
		dockId = d;
	}

	public void setOustLoc(Location loc) {
		oustLoc = loc;
	}

	public Location getOustLoc() {
		return oustLoc != null ? oustLoc : MapRegionTable.getInstance().getTeleToLocation(this, MapRegionTable.TeleportWhereType.Town);
	}

	public void oustPlayers() {
		Player player;

		// Use iterator because oustPlayer will try to remove player from passengers
		final Iterator<Player> iter = passengers.iterator();
		while (iter.hasNext()) {
			player = iter.next();
			iter.remove();
			if (player != null) {
				oustPlayer(player);
			}
		}
	}

	public void oustPlayer(Player player) {
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		removePassenger(player);
	}

	public boolean addPassenger(Player player) {
		if (player == null || passengers.contains(player)) {
			return false;
		}

		// already in other vehicle
		if (player.getVehicle() != null && player.getVehicle() != this) {
			return false;
		}

		passengers.add(player);
		return true;
	}

	public void removePassenger(Player player) {
		try {
			passengers.remove(player);
		} catch (Exception ignored) {
		}
	}

	public boolean isEmpty() {
		return passengers.isEmpty();
	}

	public List<Player> getPassengers() {
		return passengers;
	}

	public void broadcastToPassengers(L2GameServerPacket sm) {
		for (Player player : passengers) {
			if (player != null) {
				player.sendPacket(sm);
			}
		}
	}

	/**
	 * Consume ticket(s) and teleport player from boat if no correct ticket
	 *
	 * @param itemId Ticket itemId
	 * @param count  Ticket count
	 */
	public void payForRide(int itemId, int count, int oustX, int oustY, int oustZ) {
		final Collection<Player> passengers = getKnownList().getKnownPlayersInRadius(1000);
		if (passengers != null && !passengers.isEmpty()) {
			Item ticket;
			InventoryUpdate iu;
			for (Player player : passengers) {
				if (player == null) {
					continue;
				}
				if (player.isInBoat() && player.getBoat() == this) {
					if (itemId > 0) {
						ticket = player.getInventory().getItemByItemId(itemId);
						if (ticket == null || player.getInventory().destroyItem("Boat", ticket, count, player, this) == null) {
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
	public boolean updatePosition(int gameTicks) {
		final boolean result = super.updatePosition(gameTicks);

		for (Player player : passengers) {
			if (player != null && player.getVehicle() == this) {
				player.getPosition().setXYZ(getX(), getY(), getZ());
				player.revalidateZone(false);
			}
		}

		return result;
	}

	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset) {
		if (isMoving()) {
			stopMove(null, false);
		}

		setTeleporting(true);

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

		for (Player player : passengers) {
			if (player != null) {
				player.teleToLocation(x, y, z);
			}
		}

		decayMe();
		setXYZ(x, y, z);

		// temporary fix for heading on teleports
		if (heading != 0) {
			getPosition().setHeading(heading);
		}

		onTeleported();
		revalidateZone(true);
	}

	@Override
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects) {
		setMove(null);
		if (pos != null) {
			setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}

		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects) {
			getKnownList().findObjects();
		}
	}

	@Override
	public void deleteMe() {
		engine = null;

		try {
			if (isMoving()) {
				stopMove(null);
			}
		} catch (Exception e) {
			log.error("Failed stopMove().", e);
		}

		try {
			oustPlayers();
		} catch (Exception e) {
			log.error("Failed oustPlayers().", e);
		}

		final WorldRegion oldRegion = getWorldRegion();

		try {
			decayMe();
		} catch (Exception e) {
			log.error("Failed decayMe().", e);
		}

		if (oldRegion != null) {
			oldRegion.removeFromZones(this);
		}

		try {
			getKnownList().removeAllKnownObjects();
		} catch (Exception e) {
			log.error("Failed cleaning knownlist.", e);
		}

		// Remove WorldObject object from allObjects of World
		World.getInstance().removeObject(this);

		super.deleteMe();
	}

	@Override
	public void updateAbnormalEffect() {
	}

	@Override
	public Item getActiveWeaponInstance() {
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem() {
		return null;
	}

	@Override
	public Item getSecondaryWeaponInstance() {
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem() {
		return null;
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public boolean isAutoAttackable(Creature attacker) {
		return false;
	}

	@Override
	public void setAI(CreatureAI newAI) {
		if (getAi() == null) {
			setAi(newAI);
		}
	}
}
