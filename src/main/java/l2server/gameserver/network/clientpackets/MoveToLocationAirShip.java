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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.TaskPriority;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.AirShipManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.World;
import l2server.gameserver.model.VehiclePathPoint;
import l2server.gameserver.model.actor.instance.AirShipInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class MoveToLocationAirShip extends L2GameClientPacket {
	public static final int MIN_Z = -895;
	public static final int MAX_Z = 6105;
	public static final int STEP = 300;
	
	private int command;
	private int param1;
	private int param2 = 0;
	
	public TaskPriority getPriority() {
		return TaskPriority.PR_HIGH;
	}
	
	@Override
	protected void readImpl() {
		command = readD();
		param1 = readD();
		if (buf.remaining() > 0) {
			param2 = readD();
		}
	}
	
	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		if (!activeChar.isInAirShip()) {
			return;
		}
		
		final AirShipInstance ship = activeChar.getAirShip();
		if (!ship.isCaptain(activeChar)) {
			return;
		}
		
		int z = ship.getZ();
		
		switch (command) {
			case 0:
				if (!ship.canBeControlled()) {
					return;
				}
				if (param1 < World.GRACIA_MAX_X) {
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(param1, param2, z, 0));
				}
				break;
			case 1:
				if (!ship.canBeControlled()) {
					return;
				}
				ship.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			case 2:
				if (!ship.canBeControlled()) {
					return;
				}
				if (z < World.GRACIA_MAX_Z) {
					z = Math.min(z + STEP, World.GRACIA_MAX_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(ship.getX(), ship.getY(), z, 0));
				}
				break;
			case 3:
				if (!ship.canBeControlled()) {
					return;
				}
				if (z > World.GRACIA_MIN_Z) {
					z = Math.max(z - STEP, World.GRACIA_MIN_Z);
					ship.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(ship.getX(), ship.getY(), z, 0));
				}
				break;
			case 4:
				if (!ship.isInDock() || ship.isMoving()) {
					return;
				}
				
				final VehiclePathPoint[] dst = AirShipManager.getInstance().getTeleportDestination(ship.getDockId(), param1);
				if (dst == null) {
					return;
				}
				
				// Consume fuel, if needed
				final int fuelConsumption = AirShipManager.getInstance().getFuelConsumption(ship.getDockId(), param1);
				if (fuelConsumption > 0) {
					if (fuelConsumption > ship.getFuel()) {
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_CANNOT_TELEPORT));
						return;
					}
					ship.setFuel(ship.getFuel() - fuelConsumption);
				}
				
				ship.executePath(dst);
				break;
		}
	}
}
