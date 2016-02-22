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

import java.util.ArrayList;

import l2server.Config;
import l2server.gameserver.TaskPriority;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExFlyMove;
import l2server.gameserver.network.serverpackets.ExFlyMoveBroadcast;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.StopMove;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.4.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class MoveBackwardToLocation extends L2GameClientPacket
{
	//
	// cdddddd
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _moveMovement;
	
	//For geodata
	private int _curX;
	private int _curY;
	@SuppressWarnings("unused")
	private int _curZ;
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}
	
	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_moveMovement = readD(); // is 0 if cursor keys are used  1 if mouse is used
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}
		
		// Correcting targetZ from floor level to head level (?)
		// Client is giving floor level as targetZ but that floor level doesn't
		// match our current geodata and teleport coords as good as head level!
		// L2J uses floor, not head level as char coordinates. This is some
		// sort of incompatibility fix.
		// Validate position packets sends head level.
		_targetZ += activeChar.getTemplate().collisionHeight;
		
		_curX = activeChar.getX();
		_curY = activeChar.getY();
		_curZ = activeChar.getZ();
		
		activeChar.stopWatcherMode();
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			if (activeChar.getTeleMode() == 3)
			{
				activeChar.sendPacket(new ExFlyMove(activeChar, 100, -1, _targetX, _targetY, _targetZ));
				ExFlyMoveBroadcast packet = new ExFlyMoveBroadcast(activeChar, _targetX, _targetY, _targetZ);
				for (L2PcInstance known : activeChar.getKnownList().getKnownPlayers().values())
					known.sendPacket(packet);
			}
			else
				activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}
		
		if ((_moveMovement == 0) && ((Config.GEODATA < 1) || activeChar.isPlayingEvent() || activeChar.isInOlympiadMode())) // keys movement without geodata is disabled
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			double dx = _targetX - _curX;
			double dy = _targetY - _curY;
			// Can't move if character is confused, or trying to move a huge distance
			if (activeChar.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_targetX, _targetY, _targetZ, 0));
			
			//if (activeChar.isInOlympiadMode())
			//	activeChar.broadcastPacket(new ValidateLocation(activeChar));
			/*if (activeChar.getParty() != null)
				activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));*/
			
			if (activeChar.getInstanceId() != activeChar.getObjectId())
				InstanceManager.getInstance().destroyInstance(activeChar.getObjectId());
			
			if ((activeChar.getQueuedSkill() != null) && (activeChar.getQueuedSkill().getSkillId() == 30001))
				activeChar.setQueuedSkill(null, false, false);
		}
		
		if (Config.isServer(Config.DREAMS))
		{
			int playerLevel = activeChar.getLevel();
			int maxAllowedGrade = 0;
			
			if (playerLevel >= 99)
				maxAllowedGrade = L2Item.CRYSTAL_R99;
			else if (playerLevel >= 95)
				maxAllowedGrade = L2Item.CRYSTAL_R95;
			else if (playerLevel >= 85)
				maxAllowedGrade = L2Item.CRYSTAL_R;
			else if (playerLevel >= 80)
				maxAllowedGrade = L2Item.CRYSTAL_S80;
			else if (playerLevel >= 76)
				maxAllowedGrade = L2Item.CRYSTAL_S;
			else if (playerLevel >= 61)
				maxAllowedGrade = L2Item.CRYSTAL_A;
			else if (playerLevel >= 52)
				maxAllowedGrade = L2Item.CRYSTAL_B;
			else if (playerLevel >= 40)
				maxAllowedGrade = L2Item.CRYSTAL_C;
			else if (playerLevel >= 20)
				maxAllowedGrade = L2Item.CRYSTAL_D;
			else
				maxAllowedGrade = L2Item.CRYSTAL_NONE;
			
			ArrayList<L2ItemInstance> modifiedItems = new ArrayList<L2ItemInstance>();
			
			// Remove over enchanted items and hero weapons
			for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
			{
				L2ItemInstance equippedItem = activeChar.getInventory().getPaperdollItem(i);
				
				if (equippedItem == null)
					continue;
				
				if (equippedItem.getItem().getCrystalType() > maxAllowedGrade)
				{
					activeChar.getInventory().unEquipItemInSlotAndRecord(i);
					
					SystemMessage sm = null;
					
					if (equippedItem.getEnchantLevel() > 0)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
						sm.addNumber(equippedItem.getEnchantLevel());
						sm.addItemName(equippedItem);
					}
					else
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
						sm.addItemName(equippedItem);
					}
					
					activeChar.sendPacket(sm);
					
					modifiedItems.add(equippedItem);
				}
			}
			
			if (modifiedItems.size() != 0)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(modifiedItems);
				
				activeChar.sendPacket(iu);
			}
		}
	}
}
