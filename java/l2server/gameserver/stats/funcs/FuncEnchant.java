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

import l2server.Config;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2WeaponType;

public class FuncEnchant extends Func
{
	private Lambda _lambda;

	public FuncEnchant(Stats pStat, Object owner, Lambda lambda)
	{
		super(pStat, owner);
		_lambda = lambda;
	}

	@Override
	public int getOrder()
	{
		return 0x0c;
	}

	@Override
	public void calc(Env env)
	{
		if (cond != null && !cond.test(env))
		{
			return;
		}

		L2ItemInstance item = (L2ItemInstance) funcOwner;

		int enchant = item.getEnchantLevel();

		if (enchant <= 0)
		{
			return;
		}

		int overenchant = 0;

		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}

		boolean isBlessed = item.getItem().isBlessed();

		if (env.player != null && env.player instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) env.player;
			if (player.isInOlympiadMode() && Config.ALT_OLY_ENCHANT_LIMIT >= 0 &&
					enchant + overenchant > Config.ALT_OLY_ENCHANT_LIMIT)
			{
				if (Config.ALT_OLY_ENCHANT_LIMIT > 3)
				{
					overenchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
				}
				else
				{
					overenchant = 0;
					enchant = Config.ALT_OLY_ENCHANT_LIMIT;
				}
			}
		}

		double baseAddVal = _lambda.calc(env);
		if (baseAddVal > 0.0)
		{
			env.value += baseAddVal * overenchant;
			if (overenchant > 3)
			{
				env.value += baseAddVal * (overenchant - 3);
			}

			return;
		}

		if (stat == Stats.MAGIC_DEFENSE || stat == Stats.PHYS_DEFENSE)
		{
			if (item.getItem().getItemGradePlain() == L2Item.CRYSTAL_R ||
					(item.getItem().getBodyPart() & (L2Item.SLOT_HAIR | L2Item.SLOT_HAIR2 | L2Item.SLOT_HAIRALL)) > 0)
			{
				int base = isBlessed ? 3 : 2;
				int add = overenchant > 3 ? (overenchant - 3) * base : 0;
				env.value += base * enchant + base * 2 * overenchant + add;
			}
			else
			{
				env.value += enchant + 3 * overenchant;
			}

			return;
		}

		if (stat == Stats.MAGIC_ATTACK)
		{
			switch (item.getItem().getItemGradePlain())
			{
				case L2Item.CRYSTAL_R:
					int base = isBlessed ? 8 : 5;
					int add = overenchant > 3 ? (overenchant - 3) * base : 0;
					add += overenchant > 6 ? (overenchant - 6) * base : 0;
					add += overenchant > 9 ? (overenchant - 9) * base : 0;
					env.value += base * enchant + base * 2 * overenchant + add;
					break;
				case L2Item.CRYSTAL_S:
					env.value += 4 * enchant + 8 * overenchant;
					break;
				case L2Item.CRYSTAL_A:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case L2Item.CRYSTAL_B:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case L2Item.CRYSTAL_C:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case L2Item.CRYSTAL_D:
					env.value += 2 * enchant + 4 * overenchant;
					break;
			}

			return;
		}

		if (item.isWeapon())
		{
			L2WeaponType type = (L2WeaponType) item.getItemType();

			switch (item.getItem().getItemGradePlain())
			{
				case L2Item.CRYSTAL_R:
					int base = isBlessed ? 9 : 6;
					switch (type)
					{
						case BOW:
							base = isBlessed ? 18 : 12;
							break;
						case POLE:
						case DUALFIST:
						case BIGBLUNT:
						case BIGSWORD:
						case DUAL:
						case DUALBLUNT:
						case CROSSBOW:
						case CROSSBOWK:
							base = isBlessed ? 11 : 7;
							break;
					}
					int add = overenchant > 3 ? (overenchant - 3) * base : 0;
					add += overenchant > 6 ? (overenchant - 6) * base : 0;
					add += overenchant > 9 ? (overenchant - 9) * base : 0;
					env.value += base * enchant + base * 2 * overenchant + add;
					break;
				case L2Item.CRYSTAL_S:
					switch (type)
					{
						case BOW:
						case CROSSBOW:
						case CROSSBOWK:
							env.value += 10 * enchant + 20 * overenchant;
							break;
						default:
							env.value += 5 * enchant + 10 * overenchant;
							break;
					}
					break;
				case L2Item.CRYSTAL_A:
					switch (type)
					{
						case BOW:
						case CROSSBOW:
						case CROSSBOWK:
							env.value += 8 * enchant + 16 * overenchant;
							break;
						default:
							env.value += 4 * enchant + 8 * overenchant;
							break;
					}
					break;
				case L2Item.CRYSTAL_B:
				case L2Item.CRYSTAL_C:
					switch (type)
					{
						case BOW:
						case CROSSBOW:
						case CROSSBOWK:
							env.value += 6 * enchant + 12 * overenchant;
							break;
						default:
							env.value += 3 * enchant + 6 * overenchant;
							break;
					}
					break;
				case L2Item.CRYSTAL_D:
				case L2Item.CRYSTAL_NONE:
					switch (type)
					{
						case BOW:
						case CROSSBOW:
						case CROSSBOWK:
						{
							env.value += 4 * enchant + 8 * overenchant;
							break;
						}
						default:
							env.value += 2 * enchant + 4 * overenchant;
							break;
					}
					break;
			}
		}
	}
}
