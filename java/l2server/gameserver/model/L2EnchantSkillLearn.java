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

import lombok.Getter;
import gnu.trove.TIntHashSet;
import l2server.gameserver.datatables.EnchantCostsTable;
import l2server.gameserver.datatables.EnchantCostsTable.EnchantSkillDetail;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2EnchantSkillLearn
{
	@Getter private final int id;
	private final int baseLvl;
	private final TIntHashSet enchantRoutes = new TIntHashSet();

	public L2EnchantSkillLearn(int id, int baseLvl)
	{
		this.id = id;
		this.baseLvl = baseLvl;
	}

	public void addNewEnchantRoute(int route)
	{
		enchantRoutes.add(route);
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return baseLvl;
	}

	public int[] getAllRoutes()
	{
		return enchantRoutes.toArray();
	}

	public boolean isMaxEnchant(int route, int level)
	{
		if (route < 1 || !enchantRoutes.contains(route))
		{
			return false;
		}

		return level >= EnchantCostsTable.getInstance().getEnchantGroupDetails().size();

	}

	public EnchantSkillDetail getEnchantSkillDetail(int route, int level)
	{
		if (route < 1 || !enchantRoutes.contains(route))
		{
			return null;
		}

		if (level < 1)
		{
			return EnchantCostsTable.getInstance().getEnchantGroupDetails().get(0);
		}
		else if (level > EnchantCostsTable.getInstance().getEnchantGroupDetails().size())
		{
			return EnchantCostsTable.getInstance().getEnchantGroupDetails()
					.get(EnchantCostsTable.getInstance().getEnchantGroupDetails().size() - 1);
		}

		return EnchantCostsTable.getInstance().getEnchantGroupDetails().get(level - 1);
	}
}
