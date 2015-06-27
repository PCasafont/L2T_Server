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
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Weapon;

/**
 * @author Pere
 */
public class FighterController extends BotController
{
	public FighterController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected int getGeneralAttackRate()
	{
		return 100;
	}
	
	@Override
	protected boolean isOkToEquip(L2Weapon weaponItem)
	{
		return true;
	}
	
	@Override
	protected boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() != L2ArmorType.MAGIC;
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return false;
	}
}
