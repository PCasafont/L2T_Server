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
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class FeohController extends RunnerController
{
	//Attack Skills
	private static final int UNLEASH_HELL = 11066;
	private static final int ELEMENTAL_DESTRUCTION = 11023;
	private static final int ELEMENTAL_STORM = 11040;
	private static final int ELEMENTAL_BLAST = 11034;
	private static final int ELEMENTAL_CRASH = 11017;
	private static final int ELEMENTAL_SPIKE = 11011;
	private static final int DEVILS_CURSE = 11047;
	
	//Area Attack Skills
	private static final int DEATH_MASS_UNLEASH_HELL = 11067;
	private static final int MAGICAL_CHARGE = 11094;
	private static final int DEATH_HOWL = 11032;
	//Area Debuff Skills
	private static final int MASS_HELL_BINDING = 11052;
	private static final int MASS_DEATH_FEAR = 11056;
	private static final int MASS_DEVILS_CURSE = 11048;
	
	//Debuff
	private static final int DEATH_BREATH = 11029;
	private static final int DEATH_LORD = 11030;
	private static final int IGNORE_DIVINITY = 11049;
	private static final int HELL_BINDING = 11050;
	private static final int DEATH_FEAR = 11055;
	
	//Special Skills
	private static final int MAGICAL_EVASION = 11057;
	private static final int WIZARD_SPIRIT = 11046;
	@SuppressWarnings("unused") // TODO
	private static final int SHADOW_SNARE = 11058;
	@SuppressWarnings("unused") // TODO
	private static final int AIR_RIDER = 11062;
	@SuppressWarnings("unused") // TODO
	private static final int ULTIMATE_BODY_TO_MIND = 11064;
	private static final int DOUBLE_CASTING = 11068;
	private static final int CRYSTAL_FORM = 11093;
	@SuppressWarnings("unused") // TODO
	private static final int DEVILS_SWAY = 11095;
	
	//Stances
	private static final int FIRE_STANCE_ID = 11007;
	private static final int WATER_STANCE_ID = 11008;
	private static final int WIND_STANCE_ID = 11009;
	private static final int EARTH_STANCE_ID = 11010;
	
	//Toggles
	private static final int DUAL_MAXIMUM_HP = 1986;
	private static final int FEOH_FORCE_ID = 1935;
	
	protected int _favoriteStance;
	
	private static final int[][] AREA_OF_EFFECT_SKILLS =
	{
		// [0] = SkillId
		// [1] = CastChances
		// [2] = The amount of nearby targets for granted cast, if the target is also in range.
		// [3] = The amount of nearby targets for granted cast, even if the target isn't in range.
		{ MASS_DEVILS_CURSE, 10, 2, 4 },
		{ MASS_DEATH_FEAR, 10, 2, 4 },
		{ MASS_HELL_BINDING, 5, 2, 4 },
		{ DEATH_HOWL, 25, 2, 4 },
		{ MAGICAL_CHARGE, 25, 2, 4 },
		{ DEATH_MASS_UNLEASH_HELL, 10, 2, 4 },
	};
	
	private final int[] ATTACK_SKILL_IDS =
	{
		ELEMENTAL_SPIKE,
		ELEMENTAL_CRASH,
		ELEMENTAL_BLAST,
		ELEMENTAL_STORM,
		ELEMENTAL_DESTRUCTION,
		UNLEASH_HELL,
	};
	
	private final int[] DEBUFF_SKILL_IDS =
	{
		IGNORE_DIVINITY,
		DEATH_FEAR
	};
	
	private static final int[] STANCE_SKILL_IDS =
	{
		FIRE_STANCE_ID,
		WATER_STANCE_ID,
		WIND_STANCE_ID,
		EARTH_STANCE_ID,
	};
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
			// Toggles
			FEOH_FORCE_ID,
			DUAL_MAXIMUM_HP
	};
	
	private enum UsageStyle
	{
		DEATH_LORD_USAGE_STYLE_ONE,
		DEATH_LORD_USAGE_STYLE_TWO,
		DEATH_LORD_USAGE_STYLE_THREE,
		DEATH_LORD_USAGE_STYLE_FOUR,
		
		ULTIMATES_USAGE_STYLE_ONE,
		ULTIMATES_USAGE_STYLE_TWO,
		ULTIMATES_USAGE_STYLE_THREE,
	}
	
	private UsageStyle _deathLordUsageStyle;
	private UsageStyle _ultimatesUsageStyle;
	
	public FeohController(final L2PcInstance player)
	{
		super(player);
		
		// Just choose which Aura you like best and stick to that one...
		_favoriteStance = STANCE_SKILL_IDS[Rnd.get(STANCE_SKILL_IDS.length)];
		
		_deathLordUsageStyle = UsageStyle.values()
				[Rnd.get(UsageStyle.DEATH_LORD_USAGE_STYLE_ONE.ordinal(), UsageStyle.DEATH_LORD_USAGE_STYLE_FOUR.ordinal())];
		
		_ultimatesUsageStyle = UsageStyle.values()
				[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_THREE.ordinal())];
		
		player.sendMessage("[FeohController]");
		player.sendMessage("DeathLordUsageStyle = " + _deathLordUsageStyle + ".");
		player.sendMessage("UltimatesUsageStyle = " + _ultimatesUsageStyle + ".");
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		// Make sure the Aura is always up...
		if (!isStanceActive() && isSkillAvailable(_favoriteStance))
			return _favoriteStance;
		
		// Use Death Breath if it isn't active on the target...
		if (Rnd.nextBoolean() && isSkillAvailable(DEATH_BREATH) && target.getFirstEffect(DEATH_BREATH) == null)
			return DEATH_BREATH;
		
		// Same story for Devils Curse...
		if (isSkillAvailable(DEVILS_CURSE) && target.getFirstEffect(DEVILS_CURSE) == null)
			return DEVILS_CURSE;
		
		switch (_deathLordUsageStyle)
		{
			// Style 1:
			// Use Death Lord when you only have between 25 to 50% of HP left and no enemy hit you for at least 2.5 to 5 seconds.
			case DEATH_LORD_USAGE_STYLE_ONE:
			{
				if (getPlayerHealthPercent() < Rnd.get(25, 50) && isSkillAvailable(DEATH_LORD) && getLastDamagesReceivedTime() + Rnd.get(2500, 5000) < System.currentTimeMillis())
					return DEATH_LORD;
				
				break;
			}
			// Style 2:
			// Use Death Lord when you only have between 5 to 15% of HP left.
			case DEATH_LORD_USAGE_STYLE_TWO:
			{
				if (getPlayerHealthPercent() < Rnd.get(5, 15) && isSkillAvailable(DEATH_LORD))
					return DEATH_LORD;
				
				break;
			}
			// Style 3:
			// Use Death Lord when you have less than 50% HP and no enemy hit you for at least 2.5 to 5 seconds.
			case DEATH_LORD_USAGE_STYLE_THREE:
			{
				if (getPlayerHealthPercent() < 50 && isSkillAvailable(DEATH_LORD) && getLastDamagesReceivedTime() + Rnd.get(2500, 5000) < System.currentTimeMillis())
					return DEATH_LORD;
				
				break;
			}
			// Style 4:
			// Use Death Lord when you have less than 95% HP and no enemy hit you for at least 5 seconds.
			case DEATH_LORD_USAGE_STYLE_FOUR:
			{
				if (getPlayerHealthPercent() < 95 && isSkillAvailable(DEATH_LORD) && getLastDamagesReceivedTime() + 5000 < System.currentTimeMillis())
					return DEATH_LORD;
				
				break;
			}
		}
		
		switch (_ultimatesUsageStyle)
		{
			// Style 1:
			// Use Double Casting whenever possible.
			// Use Magical Evasion whenever possible.
			// Use Wizard Spirit whenever possible.
			// Use Crystal Form if you get rooted or stunned.
			// Use Hell Binding randomly, whenever possible and when no ultimate buffs are up.
			case ULTIMATES_USAGE_STYLE_ONE:
			{
				if (isSkillAvailable(DOUBLE_CASTING))
					return DOUBLE_CASTING;
				else if (isSkillAvailable(MAGICAL_EVASION))
					return MAGICAL_EVASION;
				else if (isSkillAvailable(WIZARD_SPIRIT))
					return WIZARD_SPIRIT;
				
				if (isSkillAvailable(CRYSTAL_FORM) && Rnd.get(0, 2) == 0 && (_player.isRooted() || _player.isStunned()))
					return CRYSTAL_FORM;
				
				if (isSkillAvailable(HELL_BINDING) && _player.getFirstEffect(DOUBLE_CASTING) == null && _player.getFirstEffect(MAGICAL_EVASION) == null)
					return HELL_BINDING;
				
				break;
			}
			// Style 2:
			// Use Double Casting whenever you hit 25% HP.
			// Use Magical Evasion whenever you hit 25% HP.
			// Use Crystal Form if you get rooted or stunned.
			// Do not use Hell Binding...
			case ULTIMATES_USAGE_STYLE_TWO:
			{
				if (isSkillAvailable(DOUBLE_CASTING) && getPlayerHealthPercent() < 25)
					return DOUBLE_CASTING;
				
				if (isSkillAvailable(MAGICAL_EVASION) && getPlayerHealthPercent() < 25)
					return MAGICAL_EVASION;
				
				if (isSkillAvailable(WIZARD_SPIRIT) && getPlayerHealthPercent() < 25)
					return WIZARD_SPIRIT;
				
				if (isSkillAvailable(CRYSTAL_FORM) && Rnd.get(0, 5) == 0 && (_player.isRooted() || _player.isStunned()))
					return CRYSTAL_FORM;
				
				break;
			}
			// Style 3:
			// Use Double Casting when you hit between 25 to 75% HP.
			// Use Magical Evasion when you hit between 25 to 75% HP.
			// Use Crystal Form randomly...
			// Use Hell Binding randomly, whenever possible and when no ultimate buffs are up.
			case ULTIMATES_USAGE_STYLE_THREE:
			{
				int healthPercent = getPlayerHealthPercent();
				
				if (isSkillAvailable(DOUBLE_CASTING)  && healthPercent < Rnd.get(25, 75))
					return DOUBLE_CASTING;
				
				if (isSkillAvailable(MAGICAL_EVASION) && healthPercent < Rnd.get(25, 75))
					return MAGICAL_EVASION;
				
				if (isSkillAvailable(WIZARD_SPIRIT) && healthPercent < Rnd.get(25, 75))
					return WIZARD_SPIRIT;
				
				if (isSkillAvailable(CRYSTAL_FORM) && Rnd.get(0, 15) == 0)
					return CRYSTAL_FORM;
				
				if (isSkillAvailable(HELL_BINDING) && Rnd.get(0, 15) == 0 && _player.getFirstEffect(DOUBLE_CASTING) == null && _player.getFirstEffect(MAGICAL_EVASION) == null)
					return HELL_BINDING;
				
				break;
			}
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 0;
	}
	
	@Override
	protected int getMinimumRangeToKite(final L2Character targetedCharacter)
	{
		return targetedCharacter.isStunned() ? Rnd.get(400, 600) : super.getMinimumRangeToKite(targetedCharacter);
	}
	
	@Override
	protected int getKiteRate(final L2Character targetedCharacter)
	{
		// Chance to kite increases to 70/100% while muted.
		if (_player.isMuted())
			return Rnd.get(70, 100);
		else if (_player.getRunSpeed() < 100)
			return 0;
		
		return super.getKiteRate(targetedCharacter);
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return maybeKite(targetedCharacter);
	}
	
	@Override
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		kiteToBestPosition(targetedCharacter, true, 200, Rnd.get(300, 600));
	}
	
	protected final boolean isStanceActive()
	{
		if (_player.getFirstEffect(_favoriteStance) == null)
			return false;
		
		 return true;
	}
	
	public final int getFavoriteStanceId()
	{
		return _favoriteStance;
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
	public final int[][] getAreaOfEffectSkills()
	{
		return AREA_OF_EFFECT_SKILLS;
	}
	
	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}