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
package l2tserver.gameserver.network.clientpackets;

import l2tserver.gameserver.TaskPriority;
import l2tserver.gameserver.instancemanager.BoatManager;
import l2tserver.gameserver.model.actor.instance.L2BoatInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.MoveToLocationInVehicle;
import l2tserver.gameserver.network.serverpackets.StopMoveInVehicle;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.templates.item.L2WeaponType;
import l2tserver.util.Point3D;

public final class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private static final String _C__75_MOVETOLOCATIONINVEHICLE = "[C] 75 RequestMoveToLocationInVehicle";
	
	private int _boatId;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	
	public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
	
	@Override
	protected void readImpl()
	{
		_boatId  = readD();   //objectId of boat
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected
	void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _boatId));
			return;
		}

		if (activeChar.isAttackingNow()
				&& activeChar.getActiveWeaponItem() != null
				&& (activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getPet() != null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RELEASE_PET_ON_BOAT));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isTransformed())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_POLYMORPH_ON_BOAT));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2BoatInstance boat;
		if (activeChar.isInBoat())
		{
			boat = activeChar.getBoat();
			if (boat.getObjectId() != _boatId)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else
		{
			boat = BoatManager.getInstance().getBoat(_boatId);
			if (boat == null || !boat.isInsideRadius(activeChar, 300, true, false))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.setVehicle(boat);
		}
		
		final Point3D pos = new Point3D(_targetX, _targetY, _targetZ);
		final Point3D originPos = new Point3D(_originX, _originY, _originZ);
		activeChar.setInVehiclePosition(pos);
		activeChar.broadcastPacket(new MoveToLocationInVehicle(activeChar, pos, originPos));
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__75_MOVETOLOCATIONINVEHICLE;
	}
}