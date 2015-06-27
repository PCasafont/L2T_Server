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
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class DreadnoughtController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		920, // Power Crush
		921, // Cursed Piece
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		421, // Fell Swoop
		78, // War Cry
		287, // Lionheart
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		361, // Shock Blast
	};
	
	private static final int REVIVAL_ID = 181;
	private static final int BATTLE_ROAR_ID = 121;
	
	public DreadnoughtController(final L2PcInstance player)
	{
		super(player);
		
		_battleRoarUsageStyle = (byte) Rnd.get(BATTLE_ROAR_USAGE_STYLE_ONE, BATTLE_ROAR_USAGE_STYLE_THREE);
	}
	
	private static final byte BATTLE_ROAR_USAGE_STYLE_ONE = 0;
	private static final byte BATTLE_ROAR_USAGE_STYLE_TWO = 1;
	private static final byte BATTLE_ROAR_USAGE_STYLE_THREE = 2;
	
	private final byte _battleRoarUsageStyle;
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 10;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.POLE || (weaponItem.getCrystalType() <= L2Item.CRYSTAL_D
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
		final int healthPercent = getPlayerHealthPercent();
		
		switch (_battleRoarUsageStyle)
		{
			case BATTLE_ROAR_USAGE_STYLE_ONE:
			{
				if (isSkillAvailable(REVIVAL_ID) && isSkillAvailable(BATTLE_ROAR_ID) && healthPercent < 50)
					return BATTLE_ROAR_ID;
				
				break;
			}
			case BATTLE_ROAR_USAGE_STYLE_TWO:
			{
				if (!isSkillAvailable(REVIVAL_ID) && isSkillAvailable(BATTLE_ROAR_ID) && healthPercent < 50)
					return BATTLE_ROAR_ID;
				
				break;
			}
			case BATTLE_ROAR_USAGE_STYLE_THREE:
			{
				if (isSkillAvailable(REVIVAL_ID) && isSkillAvailable(BATTLE_ROAR_ID) && healthPercent < 25)
					return BATTLE_ROAR_ID;
				
				break;
			}
		}

		// TODO - Thrill Fight
		if (healthPercent <= 10 && isSkillAvailable(REVIVAL_ID))
			return REVIVAL_ID;
		
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
	
	@Override
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
}
