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

package vehicles.AirShipGludioGracia;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.instancemanager.AirShipManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.VehiclePathPoint;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.AirShipInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.NpcSay;

/**
 * @author DS
 */
public class AirShipGludioGracia extends Quest implements Runnable {
	private static final int[] CONTROLLERS = {32607, 32609};

	private static final int GLUDIO_DOCK_ID = 10;
	private static final int GRACIA_DOCK_ID = 11;

	private static final Location OUST_GLUDIO = new Location(-149379, 255246, -80);
	private static final Location OUST_GRACIA = new Location(-186563, 243590, 2608);

	private static final VehiclePathPoint[] GLUDIO_TO_WARPGATE =
			{new VehiclePathPoint(-151202, 252556, 231), new VehiclePathPoint(-160403, 256144, 222),
					new VehiclePathPoint(-167874, 256731, -509, 0, 41035)
					// teleport: x,y,z,speed=0,heading
			};

	private static final VehiclePathPoint[] WARPGATE_TO_GRACIA =
			{new VehiclePathPoint(-169763, 254815, 282), new VehiclePathPoint(-171822, 250061, 425), new VehiclePathPoint(-172595, 247737, 398),
					new VehiclePathPoint(-174538, 246185, 39), new VehiclePathPoint(-179440, 243651, 1337),
					new VehiclePathPoint(-182601, 243957, 2739), new VehiclePathPoint(-184952, 245122, 2694),
					new VehiclePathPoint(-186936, 244563, 2617)};

	private static final VehiclePathPoint[] GRACIA_TO_WARPGATE =
			{new VehiclePathPoint(-187801, 244997, 2672), new VehiclePathPoint(-188520, 245932, 2465), new VehiclePathPoint(-189932, 245243, 1682),
					new VehiclePathPoint(-191192, 242969, 1523), new VehiclePathPoint(-190408, 239088, 1706),
					new VehiclePathPoint(-187475, 237113, 2768), new VehiclePathPoint(-184673, 238433, 2802),
					new VehiclePathPoint(-184524, 241119, 2816), new VehiclePathPoint(-182129, 243385, 2733),
					new VehiclePathPoint(-179440, 243651, 1337), new VehiclePathPoint(-174538, 246185, 39),
					new VehiclePathPoint(-172595, 247737, 398), new VehiclePathPoint(-171822, 250061, 425),
					new VehiclePathPoint(-169763, 254815, 282), new VehiclePathPoint(-168067, 256626, 343),
					new VehiclePathPoint(-157261, 255664, 221, 0, 64781)
					// teleport: x,y,z,speed=0,heading
			};

	private static final VehiclePathPoint[] WARPGATE_TO_GLUDIO =
			{new VehiclePathPoint(-153414, 255385, 221), new VehiclePathPoint(-149548, 258172, 221), new VehiclePathPoint(-146884, 257097, 221),
					new VehiclePathPoint(-146672, 254239, 221), new VehiclePathPoint(-147855, 252712, 206),
					new VehiclePathPoint(-149378, 252552, 198)};

	private final AirShipInstance ship;
	private int cycle = 0;

	private boolean foundAtcGludio = false;
	private Npc atcGludio = null;
	private boolean foundAtcGracia = false;
	private Npc atcGracia = null;

	@Override
	public final String onAdvEvent(String event, Npc npc, Player player) {
		if (player.isTransformed()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_TRANSFORMED);
			return null;
		}
		if (player.isParalyzed()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_PETRIFIED);
			return null;
		}
		if (player.isDead() || player.isFakeDeath()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_DEAD);
			return null;
		}
		if (player.isFishing()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_FISHING);
			return null;
		}
		if (player.isInCombat()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_BATTLE);
			return null;
		}
		if (player.isInDuel()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_IN_A_DUEL);
			return null;
		}
		if (player.isSitting()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_SITTING);
			return null;
		}
		if (player.isCastingNow()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_CASTING);
			return null;
		}
		if (player.isCursedWeaponEquipped()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
			return null;
		}
		if (player.isCombatFlagEquipped()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_HOLDING_A_FLAG);
			return null;
		}
		if (player.getPet() != null || player.isMounted() || !player.getSummons().isEmpty()) {
			player.sendPacket(SystemMessageId.YOU_CANNOT_BOARD_AN_AIRSHIP_WHILE_A_PET_OR_A_SERVITOR_IS_SUMMONED);
			return null;
		}

		if (ship.isInDock() && ship.isInsideRadius(player, 600, true, false)) {
			ship.addPassenger(player);
		}

		return null;
	}

	@Override
	public final String onFirstTalk(Npc npc, Player player) {
		if (player.getQuestState(getName()) == null) {
			newQuestState(player);
		}

		return npc.getNpcId() + ".htm";
	}

	public AirShipGludioGracia(int questId, String name, String descr) {
		super(questId, name, descr);
		for (int id : CONTROLLERS) {
			addStartNpc(id);
			addFirstTalkId(id);
			addTalkId(id);
		}
		ship = AirShipManager.getInstance().getNewAirShip(-149378, 252552, 198, 33837);
		ship.setOustLoc(OUST_GLUDIO);
		ship.registerEngine(this);
		ship.runEngine(60000);
	}

	@Override
	public void run() {
		try {
			switch (cycle) {
				case 0:
					broadcastInGludio(1800223); // The regularly scheduled airship that flies to the Gracia continent has departed.
					ship.setInDock(0);
					ship.executePath(GLUDIO_TO_WARPGATE);
					break;
				case 1:
					//ship.teleToLocation(-167874, 256731, -509, 41035, false);
					ship.setOustLoc(OUST_GRACIA);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
					break;
				case 2:
					ship.executePath(WARPGATE_TO_GRACIA);
					break;
				case 3:
					broadcastInGracia(1800220); // The regularly scheduled airship has arrived. It will depart for the Aden continent in 1 minute.
					ship.setInDock(GRACIA_DOCK_ID);
					ship.oustPlayers();
					ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
					break;
				case 4:
					broadcastInGracia(1800221); // The regularly scheduled airship that flies to the Aden continent has departed.
					ship.setInDock(0);
					ship.executePath(GRACIA_TO_WARPGATE);
					break;
				case 5:
					//					ship.teleToLocation(-157261, 255664, 221, 64781, false);
					ship.setOustLoc(OUST_GLUDIO);
					ThreadPoolManager.getInstance().scheduleGeneral(this, 5000);
					break;
				case 6:
					ship.executePath(WARPGATE_TO_GLUDIO);
					break;
				case 7:
					broadcastInGludio(1800222); // The regularly scheduled airship has arrived. It will depart for the Gracia continent in 1 minute.
					ship.setInDock(GLUDIO_DOCK_ID);
					ship.oustPlayers();
					ThreadPoolManager.getInstance().scheduleGeneral(this, 60000);
					break;
			}
			cycle++;
			if (cycle > 7) {
				cycle = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final void broadcastInGludio(int msg) {
		if (!foundAtcGludio) {
			foundAtcGludio = true;
			atcGludio = findController();
		}
		if (atcGludio != null) {
			atcGludio.broadcastPacket(new NpcSay(atcGludio.getObjectId(), Say2.SHOUT, atcGludio.getNpcId(), msg));
		}
	}

	private final void broadcastInGracia(int msg) {
		if (!foundAtcGracia) {
			foundAtcGracia = true;
			atcGracia = findController();
		}
		if (atcGracia != null) {
			atcGracia.broadcastPacket(new NpcSay(atcGracia.getObjectId(), Say2.SHOUT, atcGracia.getNpcId(), msg));
		}
	}

	private final Npc findController() {
		// check objects around the ship
		for (WorldObject obj : World.getInstance().getVisibleObjects(ship, 600)) {
			if (obj instanceof Npc) {
				for (int id : CONTROLLERS) {
					if (((Npc) obj).getNpcId() == id) {
						return (Npc) obj;
					}
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
		new AirShipGludioGracia(-1, AirShipGludioGracia.class.getSimpleName(), "vehicles");
	}
}
