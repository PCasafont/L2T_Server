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

import l2server.gameserver.ai.CtrlIntention;
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
public class SpectralDancerController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		986, // Deadly Strike
		223, // Sting
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		112, // Deflect Arrow
		307, // Dance of Aqua Guard
		309, // Dance of Earth Guard
		310, // Dance of the Vampire
		989, // Defense Motion
		530, // Dance of Alignment
	};
	
	private static final int HEX_ID = 122;
	private static final int POWER_BREAK_ID = 115;
	private static final int FREEZING_STRIKE_ID = 105;
	private static final int ARREST_ID = 402;
	
	public SpectralDancerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 25;
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
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (Rnd.get(0, 2) == 0)
		{
			if (isSkillAvailable(HEX_ID) && target.getFirstEffect(HEX_ID) == null)
				return HEX_ID;
			
			if (targetedPlayer != null && targetedPlayer.isFighter() && isSkillAvailable(POWER_BREAK_ID) && target.getFirstEffect(POWER_BREAK_ID) == null)
				return POWER_BREAK_ID;
		}
		
		if (Rnd.get(0, 2) == 0 && targetedPlayer != null && targetedPlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
		{
			final int[] movementTrace = targetedPlayer.getMovementTrace();
			final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
			
			final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
			final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);

			// Player is running away...
			if (distanceAfterMove > distanceBeforeMove)
			{
				final boolean isTargetSlownDown = target.getFirstEffect(FREEZING_STRIKE_ID) != null || target.getFirstEffect(ARREST_ID) != null;
				
				// Let's slow him down with either
				if (!isTargetSlownDown)
				{
					int randomSlowSkillId = Rnd.nextBoolean() ? FREEZING_STRIKE_ID : ARREST_ID;	;
					
					if (!isSkillAvailable(randomSlowSkillId))
						randomSlowSkillId = randomSlowSkillId == FREEZING_STRIKE_ID ? ARREST_ID : FREEZING_STRIKE_ID;
					
					if (!isSkillAvailable(randomSlowSkillId))
						randomSlowSkillId = -1;
					
					if (randomSlowSkillId != -1)
						return randomSlowSkillId;
				}
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
