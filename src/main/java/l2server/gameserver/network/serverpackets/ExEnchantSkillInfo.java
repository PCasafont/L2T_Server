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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.datatables.EnchantCostsTable;
import l2server.gameserver.datatables.EnchantCostsTable.EnchantSkillDetail;
import l2server.gameserver.model.L2EnchantSkillLearn;

import java.util.ArrayList;

public final class ExEnchantSkillInfo extends L2GameServerPacket
{
	private ArrayList<Integer> routes; //skill lvls for each route

	private final int id;
	private final int lvl;
	private final int enchant;
	private boolean maxEnchanted = false;

	public ExEnchantSkillInfo(int id, int lvl, int enchRoute, int enchLvl)
	{
		routes = new ArrayList<>();
		this.id = id;
		this.lvl = lvl;
		enchant = enchRoute * 1000 + enchLvl;

		L2EnchantSkillLearn enchantLearn = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(id);
		// do we have this skill?
		if (enchantLearn != null)
		{
			// skill already enchanted?
			if (enchRoute > 0)
			{
				maxEnchanted = enchantLearn.isMaxEnchant(enchRoute, enchLvl);

				// get detail for next level
				EnchantSkillDetail esd = enchantLearn.getEnchantSkillDetail(enchRoute, enchLvl);

				// if it exists add it
				if (esd != null)
				{
					routes.add(lvl + (enchRoute * 1000 + enchLvl + (maxEnchanted ? 0 : 1) << 16));
				}

				for (int route : enchantLearn.getAllRoutes())
				{
					if (route == enchRoute) // skip current
					{
						continue;
					}
					// add other levels of all routes - same lvl as enchanted
					// lvl
					routes.add(lvl + (route * 1000 + enchLvl << 16));
				}
			}
			else
			// not already enchanted
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					routes.add(lvl + (route * 1000 + 1 << 16));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(id);
		writeH(lvl);
		writeH(enchant);
		writeD(maxEnchanted ? 0 : 1);
		writeD(lvl > 100 ? 1 : 0); // enchanted?

		writeD(routes.size());
		for (Integer level : routes)
		{
			writeD(level);
		}
	}
}
