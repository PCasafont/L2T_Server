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

package l2server.gameserver.model.multisell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DS
 */
public class ListContainer
{
	protected int listId;
	protected boolean applyTaxes = false;
	protected boolean maintainEnchantment = false;
	protected boolean isChance = false;
	protected int timeLimit;

	protected List<MultiSellEntry> entries;

	public ListContainer()
	{
		entries = new ArrayList<>();
	}

	/**
	 * This constructor used in PreparedListContainer only
	 * ArrayList not created
	 */
	protected ListContainer(int listId)
	{
		this.listId = listId;
	}

	public final List<MultiSellEntry> getEntries()
	{
		return entries;
	}

	public final void setListId(int listId)
	{
		this.listId = listId;
	}

	public final int getListId()
	{
		return listId;
	}

	public final void setApplyTaxes(boolean applyTaxes)
	{
		this.applyTaxes = applyTaxes;
	}

	public final boolean getApplyTaxes()
	{
		return applyTaxes;
	}

	public final void setMaintainEnchantment(boolean maintainEnchantment)
	{
		this.maintainEnchantment = maintainEnchantment;
	}

	public final boolean getMaintainEnchantment()
	{
		return maintainEnchantment;
	}

	public final void setIsChance(boolean isChance)
	{
		this.isChance = isChance;
	}

	public final boolean isChance()
	{
		return isChance;
	}

	public final void setTimeLimit(final int timeLimit)
	{
		this.timeLimit = timeLimit;
	}

	public final int getTimeLimit()
	{
		return timeLimit;
	}
}
