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
import l2server.gameserver.model.actor.instance.L2AirShipInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExMoveToLocationInAirShip;
import l2server.gameserver.network.serverpackets.StopMoveInVehicle;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.util.Point3D;

/**
 * format: ddddddd
 * X:%d Y:%d Z:%d OriginX:%d OriginY:%d OriginZ:%d
 *
 * @author GodKratos
 */
public class MoveToLocationInAirShip extends L2GameClientPacket
{

	private int _shipId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	@Override
	protected void readImpl()
	{
		_shipId = readD();
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _shipId));
			return;
		}

		if (activeChar.isAttackingNow() && activeChar.getActiveWeaponItem() != null &&
				activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!activeChar.isInAirShip())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final L2AirShipInstance airShip = activeChar.getAirShip();
		if (airShip.getObjectId() != _shipId)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.setInVehiclePosition(new Point3D(_targetX, _targetY, _targetZ));
		activeChar.broadcastPacket(new ExMoveToLocationInAirShip(activeChar));
	}
}
