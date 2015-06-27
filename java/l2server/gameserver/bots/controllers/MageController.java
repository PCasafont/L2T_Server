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
package l2server.gameserver.bots.controllers;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncSet;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class MageController extends BotController
{
	public MageController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 30;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		double pAtk = 0.0;
		double mAtk = 0.0;
		Env env  = new Env();
		for (Func func : weaponItem.getStatFuncs(null, null))
		{
			if (!(func instanceof FuncSet))
				continue;

			func.calc(env);
			if (func.stat == Stats.POWER_ATTACK)
				pAtk = env.value;
			else if (func.stat == Stats.MAGIC_ATTACK)
				mAtk = env.value;
		}
		
		return pAtk * 0.9 < mAtk;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.MAGIC;
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return maybeKite(targetedCharacter);
	}
	
	@Override
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		kiteToBestPosition(targetedCharacter, true, 200, 600);
	}
	
	@Override
	protected boolean shouldUseDebuff(final int distanceToTarget)
	{
		return distanceToTarget < _maxDebuffCastRange + 40 && Rnd.get(0, 3) == 0;
	}
}
