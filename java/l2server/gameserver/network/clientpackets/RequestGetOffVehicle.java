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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.GetOffVehicle;
import l2server.gameserver.network.serverpackets.StopMoveInVehicle;

/**
 * @author Maktakien
 */
public final class RequestGetOffVehicle extends L2GameClientPacket
{
	private int boatId, x, y, z;

	@Override
	protected void readImpl()
	{
		boatId = readD();
		x = readD();
		y = readD();
		z = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (!activeChar.isInBoat() || activeChar.getBoat().getObjectId() != boatId ||
				activeChar.getBoat().isMoving() || !activeChar.isInsideRadius(x, y, z, 1000, true, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.broadcastPacket(new StopMoveInVehicle(activeChar, boatId));
		activeChar.setVehicle(null);
		activeChar.setInVehiclePosition(null);
		sendPacket(ActionFailed.STATIC_PACKET);
		activeChar.broadcastPacket(new GetOffVehicle(activeChar.getObjectId(), boatId, x, y, z));
		activeChar.setXYZ(x, y, z + 50);
		activeChar.revalidateZone(true);
	}
}
