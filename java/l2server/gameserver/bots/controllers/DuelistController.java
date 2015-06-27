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
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class DuelistController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		9, // Sonic Buster
		7, // Sonic Storm
		6, // Sonic Blaster
		1, // Triple Slash
		5, // Double Sonic Slash
		261, // Triple Sonic Slash
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		297, // Duelist Spirit
		78, // War Cry
		287, // Lionheart
	};
	
	private final static int[] GET_ON_TARGET_SKILL_IDS =
	{
		451, // Sonic Move
	};
	
	private static final int BATTLE_ROAR_ID = 121;
	
	private static final int SONIC_FOCUS_ID = 8;
	private static final int SONIC_RAGE_ID = 345;
	
	private static final int BRAVEHEART_ID = 440;
	
	public DuelistController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 10;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.DUAL || (weaponItem.getCrystalType() <= L2Item.CRYSTAL_D
				&& (weaponItem.getItemType() == L2WeaponType.BLUNT || weaponItem.getItemType() == L2WeaponType.DUALBLUNT
				|| weaponItem.getItemType() == L2WeaponType.SWORD || weaponItem.getItemType() == L2WeaponType.BIGSWORD));
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.HEAVY || (armorItem.getCrystalType() <= L2Item.CRYSTAL_D && armorItem.getItemType() == L2ArmorType.LIGHT);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		if (getPlayerHealthPercent() < 30 && isSkillAvailable(BATTLE_ROAR_ID))
			return BATTLE_ROAR_ID;
		
		int distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), target.getX(), target.getY(), target.getZ(), false);
		
		if (_player.getCharges() < 8 && Rnd.get(0, 2) == 0)
		{
			int loadChargeSkillId = 0;
			
			if (_player.getTarget() != null && distance > 200 && isSkillAvailable(SONIC_RAGE_ID))
				loadChargeSkillId = SONIC_RAGE_ID;
			else if (isSkillAvailable(SONIC_FOCUS_ID))
				loadChargeSkillId = SONIC_FOCUS_ID;
			
			if (loadChargeSkillId != 0)
			{
				_refreshRate = 100;
				return loadChargeSkillId;
			}
		}

		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}
