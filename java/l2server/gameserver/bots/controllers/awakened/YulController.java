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
package l2server.gameserver.bots.controllers.awakened;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class YulController extends RunnerController
{
	//Area Skills
	@SuppressWarnings("unused") // TODO
	private static final int MULTIPLE_ARROW_ID = 10771;
	
	//Attack Skills
	private static final int PINPOINT_SHOT_ID = 10763;
	private static final int TORNADO_SHOT_ID = 10760;
	private static final int QUICK_SHOT_ID = 10762;
	private static final int HEAVY_ARROW_RAIN_ID = 10772;
	private static final int IMPACT_SHOT_ID = 10769;
	private static final int SLOW_SHOT_ID = 10780;
	private static final int RECOIL_SHOT_ID = 10770;
	
	//Toggles
	private static final int DUAL_MAXIMUM_HP = 1986;
	private static final int YUL_FORCE_ID = 1933;
	
	//Special Skills
	@SuppressWarnings("unused") // TODO
	private static final int PHOENIX_ARROW_ID = 10781;
	private static final int BOW_STRIKE_ID = 10761;
	private static final int QUICK_EVASION_ID = 10774;
	private static final int QUICK_CHARGE_ID = 10805;
	@SuppressWarnings("unused") // TODO
	private static final int LURE_SHOT_ID = 10777;
	@SuppressWarnings("unused") // TODO
	private static final int DIVERSION_ID = 10778;
	private static final int FLARE_ID = 10785;
	@SuppressWarnings("unused") // TODO
	private static final int TIME_BOMB_ID = 10786;
	@SuppressWarnings("unused") // TODO
	private static final int REMOTE_CONTROL_ID = 10788;
	@SuppressWarnings("unused") // TODO
	private static final int FROST_TRAP_ID = 10791;
	@SuppressWarnings("unused") // TODO
	private static final int GRAVITY_TRAP_ID = 10792;

	@SuppressWarnings("unused") // TODO
	private static final int FLASH_ID = 10793;
	@SuppressWarnings("unused") // TODO
	private static final int MINDS_EYE_ID = 10783;
	@SuppressWarnings("unused") // TODO
	private static final int CAMOUFLAGE_ID = 10784;
	
	//Buffs
	@SuppressWarnings("unused") // TODO
	private static final int QUICK_FIRE_ID = 10779;
	@SuppressWarnings("unused") // TODO
	private static final int FINAL_ULTIMATE_ESCAPE_ID = 10776;
	private static final int BULLSEYE_ID = 10801;
	private static final int MASS_BULLSEYE_ID = 10802;
	
	//Summons
	@SuppressWarnings("unused") // TODO
	private static final int SUMMON_THUNDER_HAWK_ID = 10787;
	private static final int CONFUSION_DECOY_ID = 10775;
	
	//Stances
	private static final int DEAD_EYE_STANCE_ID = 10757;
	private static final int RAPID_FIRE_STANCE_ID = 10758;
	private static final int SNIPER_STANCE_ID = 10759;
	
	protected int _favoriteStance;
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		YUL_FORCE_ID,
		DUAL_MAXIMUM_HP
	};
		
	private final int[] ATTACK_SKILL_IDS =
	{
		RECOIL_SHOT_ID,
		SLOW_SHOT_ID,
		IMPACT_SHOT_ID,
		HEAVY_ARROW_RAIN_ID,
		QUICK_SHOT_ID,
		TORNADO_SHOT_ID,
		PINPOINT_SHOT_ID
	};
		
	private final int[] DEBUFF_SKILL_IDS =
	{
		BOW_STRIKE_ID,
		FLARE_ID,
		MASS_BULLSEYE_ID,
		BULLSEYE_ID
	};
		
		
	private static final int[] STANCE_SKILL_IDS =
	{
		DEAD_EYE_STANCE_ID,
		RAPID_FIRE_STANCE_ID,
		SNIPER_STANCE_ID,
	};
	
	private static final int[] ARROW_ITEMS_ID =
	{
		17, // Wooden Arrow (No Grade)
		1341, // Bone Arrow (D Grade)
		1342, // Steel Arrow (C Grade)
		1343, // Silver Arrow (B Grade)
		1344, // Mithril Arrow (A Grade)
		1345, // Shining Arrow (S Grade)
		18550, // Orichalcum Arrow (R Grade)
	};
	
	private static final int[] BOLT_ITEMS_ID =
	{
		9632, // Wooden Bolt (No Grade)
		9633, // Bone Bolt (D Grade)
		9634, // Steel Bolt (C Grade)
		9635, // Silver Bolt (B Grade)
		9636, // Mithril Bolt (A Grade)
		9637, // Shining Bolt (S Grade)
		19443, // Orichalcum Bolt (R Grade)
	};
	
	/**
	 * Skills Usage Variation
	 * ...
	 */
	
	private enum UsageStyle
	{
		ULTIMATES_USAGE_STYLE_ONE,
		ULTIMATES_USAGE_STYLE_TWO,
		ULTIMATES_USAGE_STYLE_THREE,
	}

	@SuppressWarnings("unused")
	private UsageStyle _ultimatesUsageStyle;
	
	public YulController(final L2PcInstance player)
	{
		super(player);
		
		// Just choose which Aura you like best and stick to that one...
		_favoriteStance = STANCE_SKILL_IDS[Rnd.get(STANCE_SKILL_IDS.length)];
		
		_ultimatesUsageStyle = UsageStyle.values()
				[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_THREE.ordinal())];
	}
	
	private int _arrowsToUse = 0;
	
	@Override
	public void onEnterWorld(final boolean isHumanBehind)
	{
		super.onEnterWorld(isHumanBehind);
		
		_arrowsToUse = getArrowsToUse();
	}
	
	@Override
	public boolean checkIfIsReadyToFight()
	{
		getItem(_arrowsToUse); // Just make sure they always have enough arrows...
		
		return super.checkIfIsReadyToFight();
	}
	
	private final int getArrowsToUse()
	{
		final L2Weapon activeWeapon = _player.getActiveWeaponItem();
		
		int weaponGrade = 0;
		
		boolean useBolts = true;
		
		if (activeWeapon != null)
		{
			weaponGrade = activeWeapon.getCrystalType();
			
			useBolts = activeWeapon.getItemType() == L2WeaponType.CROSSBOW;
		}
		
		switch(weaponGrade)
		{
			case L2Item.CRYSTAL_NONE:
				return useBolts ? BOLT_ITEMS_ID[0] : ARROW_ITEMS_ID[0];
			case L2Item.CRYSTAL_D:
				return useBolts ? BOLT_ITEMS_ID[1] : ARROW_ITEMS_ID[1];
			case L2Item.CRYSTAL_C:
				return useBolts ? BOLT_ITEMS_ID[2] : ARROW_ITEMS_ID[2];
			case L2Item.CRYSTAL_B:
				return useBolts ? BOLT_ITEMS_ID[3] : ARROW_ITEMS_ID[3];
			case L2Item.CRYSTAL_A:
				return useBolts ? BOLT_ITEMS_ID[4] : ARROW_ITEMS_ID[4];
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				return useBolts ? BOLT_ITEMS_ID[5] : ARROW_ITEMS_ID[5];
			case L2Item.CRYSTAL_R:
			case L2Item.CRYSTAL_R95:
			case L2Item.CRYSTAL_R99:
				return useBolts ? BOLT_ITEMS_ID[6] : ARROW_ITEMS_ID[6];
		}
		
		return useBolts ? BOLT_ITEMS_ID[0] : ARROW_ITEMS_ID[0];
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		// Following skills are used exclusively in PvP situations...
		if (targetedPlayer != null)
		{
			final int distance = getDistanceTo(targetedPlayer);
			
			if (distance < 100)
			{
				if (Rnd.nextBoolean() && isSkillAvailable(QUICK_CHARGE_ID))
					return QUICK_CHARGE_ID;
				
				if (isSkillAvailable(QUICK_EVASION_ID))
					return QUICK_EVASION_ID;
			}
			
			if (isSkillAvailable(CONFUSION_DECOY_ID) && Rnd.get(0, 25) == 0)
				return CONFUSION_DECOY_ID;
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 30;
	}
	
	@Override
	protected int getAttackRange()
	{
		return _player.getTemplate().baseAtkRange;
	}
	
	@Override
	protected int getMinimumRangeToUseCatchupSkill()
	{
		return getAttackRange() * 5;
	}
	
	@Override
	protected int getMinimumRangeToKite(final L2Character targetedCharacter)
	{
		return targetedCharacter.isStunned() ? Rnd.get(400, 600) : super.getMinimumRangeToKite(targetedCharacter);
	}
	
	@Override
	public final int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
	
	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}