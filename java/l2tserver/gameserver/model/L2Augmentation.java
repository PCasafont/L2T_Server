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

package l2tserver.gameserver.model;

import l2tserver.gameserver.datatables.AugmentationData;
import l2tserver.gameserver.datatables.AugmentationData.Augment;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.SkillCoolTime;
import l2tserver.log.Log;

/**
 * @author Pere
 */
public final class L2Augmentation
{
	private final Augment _augment1;
	private final Augment _augment2;
	
	public L2Augmentation(Augment augment1, Augment augment2)
	{
		_augment1 = augment1;
		_augment2 = augment2;
	}
	
	public L2Augmentation(long id)
	{
		int id1 = (int)(id >> 32);
		int id2 = (int)(id - ((long)id1 << 32));
		
		// Temp fix to import old augment ids
		if (id2 > Short.MAX_VALUE)
		{
			id1 = (int)(id >> 16);
			id2 = (int)(id - (id1 << 16));
		}
		
		_augment1 = AugmentationData.getInstance().getAugment(id1);
		_augment2 = AugmentationData.getInstance().getAugment(id2);
		if (_augment1 == null)
			Log.warning("Null augment1 for augment with id = " + id + " and calculated id1 = " + id1);
		//if (_augment2 == null)
		//	Log.warning("Null augment2 for augment with id = " + id + " and calculated id2 = " + id2);
	}
	
	public Augment getAugment1()
	{
		return _augment1;
	}
	
	public Augment getAugment2()
	{
		return _augment2;
	}
	
	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public long getId()
	{
		long id = (long)_augment1.getId() << 32;
		if (_augment2 != null)
			id += _augment2.getId();
		return id;
	}
	
	public L2Skill getSkill()
	{
		if (_augment2 == null)
			return null;
		
		return _augment2.getSkill();
	}
	
	/**
	 * Applies the bonuses to the player.
	 * @param player
	 */
	public void applyBonus(L2PcInstance player)
	{
		_augment1.applyBonus(player);
		if (_augment2 != null)
			_augment2.applyBonus(player);
		
		boolean updateTimeStamp = false;
		
		// add the skill if any
		L2Skill skill = getSkill();
		if (skill != null)
		{
			player.addSkill(skill);
			if (skill.isActive())
			{
				if (skill.isOffensive())
				{
					player.disableSkill(skill, skill.getReuseDelay());
					player.addTimeStamp(skill, skill.getReuseDelay());
					updateTimeStamp = true;
				}
				else if (!player.getReuseTimeStamp().isEmpty() && player.getReuseTimeStamp().containsKey(skill.getReuseHashCode()))
				{
					final long delay = player.getReuseTimeStamp().get(skill.getReuseHashCode()).getRemaining();
					if (delay > 0)
					{
						player.disableSkill(skill, delay);
						player.addTimeStamp(skill, delay);
						updateTimeStamp = true;
					}
				}
			}
			player.sendSkillList();
			if (updateTimeStamp)
				player.sendPacket(new SkillCoolTime(player));
		}
	}
	
	/**
	 * Removes the augmentation bonuses from the player.
	 * @param player
	 */
	public void removeBonus(L2PcInstance player)
	{
		_augment1.removeBonus(player);
		if (_augment2 != null)
			_augment2.removeBonus(player);
		
		// remove the skill if any
		L2Skill skill = getSkill();
		if (skill != null)
		{
			if (skill.isPassive() /*|| Config.isServer(Config.LOW | Config.MID | Config.HIGH)*/)
				player.removeSkill(skill, false, true);
			else
				player.removeSkill(skill, false, false);
			
			player.sendSkillList();
		}
	}
}
