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

package handlers.itemhandlers;

import java.util.Map;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance.TimeStamp;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * temp handler
 * here u can found items that yet cannot be unhardcoded due to missing better core support
 *
 */
public class Potions extends ItemSkills
{
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance)
	 */
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar; // use activeChar only for L2PcInstance checks where cannot be used PetInstance
		
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;
		
		if ((activeChar.getEvent() != null) && !activeChar.getEvent().onPotionUse(activeChar.getObjectId()))
		{
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		usePotion(activeChar, 2005, 1);
		playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
	
	/**
	 *
	 * @param activeChar
	 * @param magicId
	 * @param level
	 * @return
	 */
	public boolean usePotion(L2Playable activeChar, int magicId, int level)
	{
		
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		
		if (skill != null)
		{
			if (!skill.checkCondition(activeChar, activeChar, false))
				return false;
			// Return false if potion is in reuse so it is not destroyed from inventory
			if (activeChar.isSkillDisabled(skill.getId()) || activeChar.isAllSkillsDisabled())
			{
				displayReuse(activeChar, skill);
				return false;
			}
			if (skill.isPotion())
				activeChar.doSimultaneousCast(skill);
			else
				activeChar.doCast(skill);
			
			if (activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar;
				// Only for Heal potions
				if ((magicId == 2031) || (magicId == 2032) || (magicId == 2037))
					player.shortBuffStatusUpdate(magicId, level, 15);
				
				if (!(player.isSitting() && !skill.isPotion()))
					return true;
			}
			else if (activeChar instanceof L2PetInstance)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
				sm.addString(skill.getName());
				((L2PetInstance) (activeChar)).getOwner().sendPacket(sm);
				return true;
			}
		}
		return false;
	}
	
	private final void displayReuse(L2Playable activeChar, L2Skill skill)
	{
		final L2PcInstance player = activeChar.getActingPlayer();
		if (player == null)
			return;
		
		final Map<Integer, TimeStamp> timeStamp = player.getReuseTimeStamp();
		SystemMessage sm = null;
		
		if ((timeStamp != null) && timeStamp.containsKey(skill.getId()))
		{
			final int remainingTime = (int) (player.getReuseTimeStamp().get(skill.getId()).getRemaining() / 1000);
			final int hours = remainingTime / 3600;
			final int minutes = (remainingTime % 3600) / 60;
			final int seconds = (remainingTime % 60);
			if (hours > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
				sm.addSkillName(skill);
				sm.addNumber(hours);
				sm.addNumber(minutes);
			}
			else if (minutes > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
				sm.addSkillName(skill);
				sm.addNumber(minutes);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
				sm.addSkillName(skill);
			}
			sm.addNumber(seconds);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addSkillName(skill);
		}
		player.sendPacket(sm);
	}
}
