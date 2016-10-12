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

package l2server.gameserver.model;

import java.util.Arrays;

/**
 * /*
 * <p>
 * Special thanks to nuocnam
 * Author: LittleVexy
 *
 * @version $Revision: 1.1.4.4 $ $Date: 2005/03/29 23:15:15 $
 */
public class L2DropData
{
	public static final int MAX_CHANCE = 100;

	private int _itemId;
	private int _minDrop;
	private int _maxDrop;
	private float _chance;
	private String _questID = null;
	private String[] _stateID = null;
	private boolean _custom = false;

	public L2DropData(int itemId, int min, int max, float chance)
	{
		_itemId = itemId;
		_minDrop = min;
		_maxDrop = max;
		_chance = chance;
	}

	/**
	 * Returns the ID of the item dropped
	 *
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Sets the ID of the item dropped
	 *
	 * @param itemId : int designating the ID of the item
	 */
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	/**
	 * Returns the minimum quantity of items dropped
	 *
	 * @return int
	 */
	public int getMinDrop()
	{
		return _minDrop;
	}

	/**
	 * Returns the maximum quantity of items dropped
	 *
	 * @return int
	 */
	public int getMaxDrop()
	{
		return _maxDrop;
	}

	/**
	 * Returns the chance of having a drop
	 *
	 * @return float
	 */
	public float getChance()
	{
		return _chance;
	}

	/**
	 * Sets the value for minimal quantity of dropped items
	 *
	 * @param mindrop : int designating the quantity
	 */
	public void setMinDrop(int mindrop)
	{
		_minDrop = mindrop;
	}

	/**
	 * Sets the value for maximal quantity of dopped items
	 *
	 * @param maxdrop : int designating the quantity of dropped items
	 */
	public void setMaxDrop(int maxdrop)
	{
		_maxDrop = maxdrop;
	}

	/**
	 * Sets the chance of having the item for a drop
	 *
	 * @param chance : int designating the chance
	 */
	public void setChance(int chance)
	{
		_chance = chance;
	}

	/**
	 * Returns the stateID.
	 *
	 * @return String[]
	 */
	public String[] getStateIDs()
	{
		return _stateID;
	}

	/**
	 * Adds states of the dropped item
	 *
	 * @param list : String[]
	 */
	public void addStates(String[] list)
	{
		_stateID = list;
	}

	/**
	 * Returns the questID.
	 *
	 * @return String designating the ID of the quest
	 */
	public String getQuestID()
	{
		return _questID;
	}

	/**
	 * Sets the questID
	 */
	public void setQuestID(String questID)
	{
		_questID = questID;
	}

	/**
	 * Returns if the dropped item is requested for a quest
	 *
	 * @return boolean
	 */
	public boolean isQuestDrop()
	{
		return _questID != null && _stateID != null;
	}

	public void setCustom()
	{
		_custom = true;
	}

	public boolean isCustom()
	{
		return _custom;
	}

	/**
	 * Returns a report of the object
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		String out = "ItemID: " + getItemId() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " +
				getChance() + "%";
		if (isQuestDrop())
		{
			out += " QuestID: " + getQuestID() + " StateID's: " + Arrays.toString(getStateIDs());
		}

		return out;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + _itemId;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (!(obj instanceof L2DropData))
		{
			return false;
		}
		final L2DropData other = (L2DropData) obj;
		return _itemId == other._itemId;
	}
}
