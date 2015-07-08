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

import l2tserver.Config;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2World;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
public final class Action extends L2GameClientPacket
{
	private static final String ACTION__C__04 = "[C] 04 Action";
	
	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX;
	@SuppressWarnings("unused")
	private int _originY;
	@SuppressWarnings("unused")
	private int _originZ;
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
			Log.fine("Action:" + _actionId);
		if (Config.DEBUG)
			Log.fine("oid:" + _objectId);
		
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.inObserverMode())
		{
			getClient().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object obj;
		if (activeChar.getTargetId() == _objectId)
			obj = activeChar.getTarget();
		else if (activeChar.isInAirShip()
				&& activeChar.getAirShip().getHelmObjectId() == _objectId)
			obj = activeChar.getAirShip();
		else
			obj = L2World.getInstance().findObject(_objectId);
		
		// If object requested does not exist, add warn msg into logs
		if (obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here
			// Logozo.warning("Character: " + activeChar.getName() + " request action with non existent ObjectID:" + _objectId);
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't interact with objects in the other instances
		// except from multiverse
		if (obj.getInstanceId() != activeChar.getInstanceId()
				&& obj.getInstanceId() != activeChar.getObjectId()
				&& activeChar.getInstanceId() != -1)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Only GMs can directly interact with invisible characters, but an invis char can target itself
		if (obj instanceof L2PcInstance
				&& (((L2PcInstance)obj).getAppearance().getInvisible())
				&& !activeChar.isGM()
				&& obj != activeChar)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		if (activeChar.getActiveRequester() == null)
		{
			switch (_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if (!activeChar.isGM() && !((obj instanceof L2Npc) && Config.ALT_GAME_VIEWNPC))
						obj.onAction(activeChar, false);
					else
						obj.onActionShift(activeChar);
					break;
				default:
					// Ivalid action detected (probably client cheating), log this
					Log.warning("Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
					getClient().sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
		else
			// Actions prohibited when in trade
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
		
		/*if (!L2World.getInstance().getAllPlayersArray().contains(activeChar))
		{
			GmListTable.broadcastMessageToGMs("WARNING: " + activeChar.getName() + "("+activeChar.getAccountName()+") is using the target exploit!");
			Log.warning("WARNING: " + activeChar.getName() + "("+activeChar.getAccountName()+") is using the target exploit!");
			activeChar.getClient().closeNow();
		}*/
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return ACTION__C__04;
	}
}
