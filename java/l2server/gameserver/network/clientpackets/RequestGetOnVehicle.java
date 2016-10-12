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

import l2server.gameserver.instancemanager.BoatManager;
import l2server.gameserver.model.actor.instance.L2BoatInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.GetOnVehicle;
import l2server.util.Point3D;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestGetOnVehicle extends L2GameClientPacket
{

	private int _boatId;
	private Point3D _pos;

	@Override
	protected void readImpl()
	{
		int x, y, z;
		_boatId = readD();
		x = readD();
		y = readD();
		z = readD();
		_pos = new Point3D(x, y, z);
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2BoatInstance boat;
		if (activeChar.isInBoat())
		{
			boat = activeChar.getBoat();
			if (boat.getObjectId() != _boatId)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else
		{
			boat = BoatManager.getInstance().getBoat(_boatId);
			if (boat == null || boat.isMoving() || !activeChar.isInsideRadius(boat, 1000, true, false))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		activeChar.setInVehiclePosition(_pos);
		activeChar.setVehicle(boat);
		activeChar.broadcastPacket(new GetOnVehicle(activeChar.getObjectId(), boat.getObjectId(), _pos));

		activeChar.setXYZ(boat.getX(), boat.getY(), boat.getZ());
		activeChar.revalidateZone(true);
	}
}
