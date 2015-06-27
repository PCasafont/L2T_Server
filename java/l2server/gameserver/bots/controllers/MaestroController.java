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

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class MaestroController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		100, // Stun Attack
		260, // Hammer Crush
		362, // Armor Crush
		997, // Crushing Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		94, // Rage
		826, // Spike
		828, // Case Harden
	};
	
	private final static int[] CRITICAL_STATE_SKILL_IDS =
	{
		1561, // Battle Cry
	};
	
	public MaestroController(final L2PcInstance player)
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
		return weaponItem.getItemType() == L2WeaponType.BLUNT;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.HEAVY || (armorItem.getCrystalType() <= L2Item.CRYSTAL_D && armorItem.getItemType() == L2ArmorType.LIGHT);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		int selectedSkillId = -1;
		
		if (target instanceof L2PcInstance)
		{
			final L2PcInstance targetedPlayer = (L2PcInstance) target;
			
			// TODO
			// Frenzy, Braveheart
		}
		
		if (selectedSkillId != -1)
			return selectedSkillId;
		
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
