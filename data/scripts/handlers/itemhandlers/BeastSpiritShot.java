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

import l2tserver.gameserver.handler.IItemHandler;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.model.actor.L2Summon;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.actor.instance.L2PetInstance;
import l2tserver.gameserver.model.actor.instance.L2SummonInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.MagicSkillUse;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.util.Broadcast;

/**
 * Beast SpiritShot Handler
 *
 * @author Tempy
 */
public class BeastSpiritShot implements IItemHandler
{
	/**
	 * 
	 * @see l2tserver.gameserver.handler.IItemHandler#useItem(l2tserver.gameserver.model.actor.L2Playable, l2tserver.gameserver.model.L2ItemInstance, boolean)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (playable == null)
			return;
		
		L2PcInstance summoner = playable.getActingPlayer();
		
		if (playable instanceof L2Summon)
		{
			summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_CANNOT_USE_ITEM));
			return;
		}
		
		if (summoner.getPet() != null)
			useShot(summoner, summoner.getPet(), item);
		for (L2SummonInstance summon : summoner.getSummons())
			useShot(summoner, summon, item);
	}
	
	public void useShot(L2PcInstance summoner, L2Summon summon, L2ItemInstance item)
	{
		if (summon == null)
		{
			summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME));
			return;
		}
		
		if (summon.isDead())
		{
			summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET));
			return;
		}
		
		int itemId = item.getItemId();
		boolean isBlessed = (itemId == 6647 || itemId == 20334);
		short shotConsumption = summon.getSpiritShotsPerHit();
		
		long shotCount = item.getCount();
		if (!(shotCount > shotConsumption))
		{
			// Not enough SpiritShots to use.
			if (!summoner.disableAutoShot(itemId))
				summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET));
			return;
		}
		
		L2ItemInstance weaponInst = null;
		
		if (summon instanceof L2PetInstance)
			weaponInst = ((L2PetInstance) summon).getActiveWeaponInstance();
		
		if (weaponInst == null)
		{
			if (summon.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
				return;
			
			if (isBlessed)
				summon.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			else
				summon.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT);
		}
		else
		{
			if (weaponInst.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
			{
				// SpiritShots are already active.
				return;
			}
			
			if (isBlessed)
				weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
			else
				weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT);
		}
		
		if (!summoner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
		{
			if (!summoner.disableAutoShot(itemId))
				summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET));
			return;
		}
		
		// Pet uses the power of spirit.
		summoner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USE_SPIRITSHOT));
		int skillId = 0;
		switch (itemId)
		{
			case 6646:
				skillId = 2008;
				break;
			case 6647:
				skillId = 2009;
				break;
			case 20333:
				skillId = 22037;
				break;
			case 20334:
				skillId = 22038;
				break;
		}
		Broadcast.toSelfAndKnownPlayersInRadius(summoner, new MagicSkillUse(summon, summon, skillId, 1, 0, 0, 0), 360000/*600*/);
	}
}
