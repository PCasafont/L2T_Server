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

package l2server.gameserver.stats.funcs;

import l2server.gameserver.datatables.EnchantHPBonusData;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;

/**
 * @author Yamaneko
 */
public class FuncEnchantHp extends Func
{
	public FuncEnchantHp(Stats pStat, Object owner, Lambda lambda)
	{
		super(pStat, owner);
	}

	@Override
	public int getOrder()
	{
		return 0x60;
	}

	@Override
	public void calc(Env env)
	{
		if (cond != null && !cond.test(env))
		{
			return;
		}

		final L2ItemInstance item = (L2ItemInstance) funcOwner;
		if (item.getEnchantLevel() > 0)
		{
			env.value += EnchantHPBonusData.getInstance().getHPBonus(item);
		}
	}
}
