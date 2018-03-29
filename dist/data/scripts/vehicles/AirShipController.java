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

package vehicles;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.AirShipManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.VehiclePathPoint;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2AirShipInstance;
import l2server.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2ScriptZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.NpcSay;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AirShipController extends Quest
{
	public static final Logger log = Logger.getLogger(AirShipController.class.getName());

	protected int dockZone = 0;

	protected int shipSpawnX = 0;
	protected int shipSpawnY = 0;
	protected int shipSpawnZ = 0;
	protected int shipHeading = 0;

	protected Location oustLoc = null;

	protected int locationId = 0;
	protected VehiclePathPoint[] arrivalPath = null;
	protected VehiclePathPoint[] departPath = null;

	protected VehiclePathPoint[][] teleportsTable = null;
	protected int[] fuelTable = null;

	protected int movieId = 0;

	protected boolean isBusy = false;

	protected L2ControllableAirShipInstance dockedShip = null;

	private final Runnable decayTask = new DecayTask();
	private final Runnable departTask = new DepartTask();
	private Future<?> departSchedule = null;

	private NpcSay arrivalMessage = null;

	private static final int DEPART_INTERVAL = 300000; // 5 min

	private static final int LICENSE = 13559;
	private static final int STARSTONE = 13277;
	private static final int SUMMON_COST = 5;

	private static final SystemMessage SM_ALREADY_EXISTS =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_IS_ALREADY_EXISTS);
	private static final SystemMessage SM_ALREADY_SUMMONED =
			SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_AIRSHIP_ALREADY_SUMMONED);
	private static final SystemMessage SM_NEED_LICENSE =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_LICENSE_TO_SUMMON);
	private static final SystemMessage SM_NEED_CLANLVL5 =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
	private static final SystemMessage SM_NO_PRIVS =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
	private static final SystemMessage SM_ALREADY_USED =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_ALREADY_USED);
	private static final SystemMessage SM_LICENSE_ALREADY_ACQUIRED =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);
	private static final SystemMessage SM_LICENSE_ENTERED =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ENTERED);
	private static final SystemMessage SM_NEED_MORE =
			SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_MORE_S1).addItemName(STARSTONE);

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("summon"))
		{
			if (dockedShip != null)
			{
				if (dockedShip.isOwner(player))
				{
					player.sendPacket(SM_ALREADY_EXISTS);
				}
				return null;
			}
			if (isBusy)
			{
				player.sendPacket(SM_ALREADY_SUMMONED);
				return null;
			}
			if ((player.getClanPrivileges() & L2Clan.CP_CL_SUMMON_AIRSHIP) != L2Clan.CP_CL_SUMMON_AIRSHIP)
			{
				player.sendPacket(SM_NO_PRIVS);
				return null;
			}
			int ownerId = player.getClanId();
			if (!AirShipManager.getInstance().hasAirShipLicense(ownerId))
			{
				player.sendPacket(SM_NEED_LICENSE);
				return null;
			}
			if (AirShipManager.getInstance().hasAirShip(ownerId))
			{
				player.sendPacket(SM_ALREADY_USED);
				return null;
			}
			if (!player.destroyItemByItemId("AirShipSummon", STARSTONE, SUMMON_COST, npc, true))
			{
				player.sendPacket(SM_NEED_MORE);
				return null;
			}

			isBusy = true;
			final L2AirShipInstance ship = AirShipManager.getInstance()
					.getNewAirShip(shipSpawnX, shipSpawnY, shipSpawnZ, shipHeading, ownerId);
			if (ship != null)
			{
				if (arrivalPath != null)
				{
					ship.executePath(arrivalPath);
				}

				if (arrivalMessage == null)
				{
					arrivalMessage = new NpcSay(npc.getObjectId(), Say2.SHOUT, npc.getNpcId(),
							1800219); // The airship has been summoned. It will automatically depart in 5 minutes.
				}

				npc.broadcastPacket(arrivalMessage);
			}
			else
			{
				isBusy = false;
			}

			return null;
		}
		else if (event.equalsIgnoreCase("board"))
		{
			if (player.isTransformed())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED);
				return null;
			}
			else if (player.isParalyzed())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED);
				return null;
			}
			else if (player.isDead() || player.isFakeDeath())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD);
				return null;
			}
			else if (player.isFishing())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING);
				return null;
			}
			else if (player.isInCombat())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE);
				return null;
			}
			else if (player.isInDuel())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL);
				return null;
			}
			else if (player.isSitting())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING);
				return null;
			}
			else if (player.isCastingNow())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING);
				return null;
			}
			else if (player.isCursedWeaponEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
				return null;
			}
			else if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG);
				return null;
			}
			else if (player.getPet() != null || player.isMounted() || !player.getSummons().isEmpty())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED);
				return null;
			}
			else if (player.isFlyingMounted())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_NOT_MEET_REQUEIREMENTS);
				return null;
			}

			if (dockedShip != null)
			{
				dockedShip.addPassenger(player);
			}

			return null;
		}
		else if (event.equalsIgnoreCase("register"))
		{
			if (player.getClan() == null || player.getClan().getLevel() < 5)
			{
				player.sendPacket(SM_NEED_CLANLVL5);
				return null;
			}
			if (!player.isClanLeader())
			{
				player.sendPacket(SM_NO_PRIVS);
				return null;
			}
			final int ownerId = player.getClanId();
			if (AirShipManager.getInstance().hasAirShipLicense(ownerId))
			{
				player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
				return null;
			}
			if (!player.destroyItemByItemId("AirShipLicense", LICENSE, 1, npc, true))
			{
				player.sendPacket(SM_NEED_MORE);
				return null;
			}

			AirShipManager.getInstance().registerLicense(ownerId);
			player.sendPacket(SM_LICENSE_ENTERED);
			return null;
		}
		else
		{
			return event;
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}

		return npc.getNpcId() + ".htm";
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2ControllableAirShipInstance)
		{
			if (dockedShip == null)
			{
				dockedShip = (L2ControllableAirShipInstance) character;
				dockedShip.setInDock(dockZone);
				dockedShip.setOustLoc(oustLoc);

				// Ship is not empty - display movie to passengers and dock
				if (!dockedShip.isEmpty())
				{
					if (movieId != 0)
					{
						for (L2PcInstance passenger : dockedShip.getPassengers())
						{
							if (passenger != null)
							{
								passenger.showQuestMovie(movieId);
							}
						}
					}

					ThreadPoolManager.getInstance().scheduleGeneral(decayTask, 1000);
				}
				else
				{
					departSchedule = ThreadPoolManager.getInstance().scheduleGeneral(departTask, DEPART_INTERVAL);
				}
			}
		}
		return null;
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2ControllableAirShipInstance)
		{
			if (character.equals(dockedShip))
			{
				if (departSchedule != null)
				{
					departSchedule.cancel(false);
					departSchedule = null;
				}

				dockedShip.setInDock(0);
				dockedShip = null;
				isBusy = false;
			}
		}
		return null;
	}

	protected void validityCheck()
	{
		L2ScriptZone zone = ZoneManager.getInstance().getZoneById(dockZone, L2ScriptZone.class);
		if (zone == null)
		{
			log.log(Level.WARNING, getName() + ": Invalid zone " + dockZone + ", controller disabled");
			isBusy = true;
			return;
		}

		VehiclePathPoint p;
		if (arrivalPath != null)
		{
			if (arrivalPath.length == 0)
			{
				log.log(Level.WARNING, getName() + ": Zero arrival path length.");
				arrivalPath = null;
			}
			else
			{
				p = arrivalPath[arrivalPath.length - 1];
				if (!zone.isInsideZone(p.x, p.y, p.z))
				{
					log.log(Level.WARNING, getName() + ": Arrival path finish point (" + p.x + "," + p.y + "," + p.z +
							") not in zone " + dockZone);
					arrivalPath = null;
				}
			}
		}
		if (arrivalPath == null)
		{
			if (!ZoneManager.getInstance().getZoneById(dockZone, L2ScriptZone.class)
					.isInsideZone(shipSpawnX, shipSpawnY, shipSpawnZ))
			{
				log.log(Level.WARNING, getName() + ": Arrival path is null and spawn point not in zone " + dockZone +
						", controller disabled");
				isBusy = true;
				return;
			}
		}

		if (departPath != null)
		{
			if (departPath.length == 0)
			{
				log.log(Level.WARNING, getName() + ": Zero depart path length.");
				departPath = null;
			}
			else
			{
				p = departPath[departPath.length - 1];
				if (zone.isInsideZone(p.x, p.y, p.z))
				{
					log.log(Level.WARNING,
							getName() + ": Departure path finish point (" + p.x + "," + p.y + "," + p.z + ") in zone " +
									dockZone);
					departPath = null;
				}
			}
		}

		if (teleportsTable != null)
		{
			if (fuelTable == null)
			{
				log.log(Level.WARNING, getName() + ": Fuel consumption not defined.");
			}
			else
			{
				if (teleportsTable.length != fuelTable.length)
				{
					log.log(Level.WARNING, getName() + ": Fuel consumption not match teleport list.");
				}
				else
				{
					AirShipManager.getInstance()
							.registerAirShipTeleportList(dockZone, locationId, teleportsTable, fuelTable);
				}
			}
		}
	}

	private final class DecayTask implements Runnable
	{
		@Override
		public void run()
		{
			if (dockedShip != null)
			{
				dockedShip.deleteMe();
			}
		}
	}

	private final class DepartTask implements Runnable
	{
		@Override
		public void run()
		{
			if (dockedShip != null && dockedShip.isInDock() && !dockedShip.isMoving())
			{
				if (departPath != null)
				{
					dockedShip.executePath(departPath);
				}
				else
				{
					dockedShip.deleteMe();
				}
			}
		}
	}

	public AirShipController(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
}
