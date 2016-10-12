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
	private ArrayList<Integer> _routes; //skill lvls for each route

	private final int _id;
	private final int _lvl;
	private final int _enchant;
	private boolean _maxEnchanted = false;

	public ExEnchantSkillInfo(int id, int lvl, int enchRoute, int enchLvl)
	{
		_routes = new ArrayList<>();
		_id = id;
		_lvl = lvl;
		_enchant = enchRoute * 1000 + enchLvl;

		L2EnchantSkillLearn enchantLearn = EnchantCostsTable.getInstance().getSkillEnchantmentBySkillId(_id);
		// do we have this skill?
		if (enchantLearn != null)
		{
			// skill already enchanted?
			if (enchRoute > 0)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(enchRoute, enchLvl);

				// get detail for next level
				EnchantSkillDetail esd = enchantLearn.getEnchantSkillDetail(enchRoute, enchLvl);

				// if it exists add it
				if (esd != null)
				{
					_routes.add(_lvl + (enchRoute * 1000 + enchLvl + (_maxEnchanted ? 0 : 1) << 16));
				}

				for (int route : enchantLearn.getAllRoutes())
				{
					if (route == enchRoute) // skip current
					{
						continue;
					}
					// add other levels of all routes - same lvl as enchanted
					// lvl
					_routes.add(_lvl + (route * 1000 + enchLvl << 16));
				}
			}
			else
			// not already enchanted
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					_routes.add(_lvl + (route * 1000 + 1 << 16));
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
		writeD(_id);
		writeH(_lvl);
		writeH(_enchant);
		writeD(_maxEnchanted ? 0 : 1);
		writeD(_lvl > 100 ? 1 : 0); // enchanted?

		writeD(_routes.size());
		for (Integer level : _routes)
		{
			writeD(level);
		}
	}
}
