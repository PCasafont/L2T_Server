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

import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 * @author Ghost
 */

public class SigelController extends BotController
{
	private static final int SIGEL_AURA_ID = 1927;
	private static final int FOCUS_SHIELD_ID = 10020;
	private static final int SUMMON_KNIGHT_CUBIC_ID = 10043;

	private static final int GUST_BLADE_ID = 10013;
	private static final int CHAIN_GALAXY_ID = 10014;
	@SuppressWarnings("unused") // TODO
	private static final int CHAIN_HYDRA_ID = 10016;
	@SuppressWarnings("unused") // TODO
	private static final int KING_OF_BEASTS_ID = 10023;
	@SuppressWarnings("unused") // TODO
	private static final int SUPERIOR_AGGRESSION_AURA_ID = 10027;
	private static final int SHIELD_WAVE_ID = 10012;
	
	private static final int CHAIN_STRIKE_ID = 10015;
	private static final int SHIELD_CHARGE_ID = 10008;
	private static final int LAST_JUDGMENT_ID = 10009;
	private static final int JUSTICE_PUNISHMENT_ID = 10010;
	private static final int SHIELD_IMPACT_ID = 10011;
	private static final int SUPERIOR_AGGRESSION_ID = 10026;

	//Summon
	private static final int SUMMON_GOLDEN_LION_ID = 10040;
	
	private static final int FINAL_ULTIMATE_DEFENSE_ID = 10017;
	private static final int PROTECTION_OF_FAITH_ID = 10019;
	private static final int SPIKE_SHIELD_ID = 10021;
	private static final int IGNORE_DEATH_ID = 10022;
	private static final int KNIGHT_FRENZY_ID = 10025;
	@SuppressWarnings("unused") // TODO
	private static final int PARTY_RESCUE_ID = 10024;
	@SuppressWarnings("unused") // TODO
	private static final int SOUL_OF_THE_PHOENIX_ID = 438;
	@SuppressWarnings("unused") // TODO
	private static final int NOBLE_SACRIFICE_ID = 10018;
	@SuppressWarnings("unused") // TODO
	private static final int TRUE_VANGUARD_ID = 10244;
	
	private static final int RAGE_AURA_ID = 10028;
	private static final int CHALLENGE_AURA_ID = 10030;
	private static final int IRON_AURA_ID = 10032;
	private static final int AURA_RESISTANCE_ID = 10034;
	private static final int RECOVERY_AURA_ID = 10036;
	private static final int SPIRIT_AURA_ID = 10038;
	
	protected int _favoriteAura;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		SIGEL_AURA_ID,
		FOCUS_SHIELD_ID,
		SUMMON_KNIGHT_CUBIC_ID
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_GOLDEN_LION_ID
	};
	
	private static final int[] AURA_SKILL_IDS =
	{
		RAGE_AURA_ID,
		CHALLENGE_AURA_ID,
		IRON_AURA_ID,
		AURA_RESISTANCE_ID,
		RECOVERY_AURA_ID,
		SPIRIT_AURA_ID
	};
	
	private enum UsageStyle
	{
		ULTIMATES_USAGE_STYLE_ONE,
		ULTIMATES_USAGE_STYLE_TWO,
		
		IGNORE_DEATH_USAGE_STYLE_ONE,
		
		AGGRESSION_USAGE_STYLE_ONE,
		AGGRESSION_USAGE_STYLE_TWO
	}
	
	private UsageStyle _ultimatesUsageStyle;
	private UsageStyle _ignoreDeathUsageStyle;
	
	private boolean _canUseSummon;
	private boolean _isSummonAllowedToAssist;
	private boolean _isAllowedToSummonInCombat;
	
	public SigelController(final L2PcInstance player)
	{
		super(player);
		
		// Just choose which Aura you like best and stick to that one...
		_favoriteAura = AURA_SKILL_IDS[Rnd.get(AURA_SKILL_IDS.length)];
		
		_canUseSummon = Rnd.nextBoolean();
		_isSummonAllowedToAssist = Rnd.get(0, 9) != 0;
		_isAllowedToSummonInCombat = Rnd.nextBoolean();
		
		_ignoreDeathUsageStyle = UsageStyle.values()
			[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_TWO.ordinal())];
		
		_ultimatesUsageStyle = UsageStyle.values()
			[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_TWO.ordinal())];
	}
	
	@Override
	protected boolean isSummonAllowedToAssist()
	{
		return _isSummonAllowedToAssist;
	}
	
	@Override
	protected boolean isAllowedToSummonInCombat()
	{
		return _isAllowedToSummonInCombat;
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final int distanceToTarget = 
				target == null ? 0 : (int) Util.calculateDistance(_player.getX(), _player.getY(), target.getX(), target.getY());
		
		// Make sure the Aura is always up...
		if (!isAuraActive() && isSkillAvailable(_favoriteAura))
			return _favoriteAura;
		
		switch (_ignoreDeathUsageStyle)
		{
			// Style 1:
			// Use Ignore Death randomly when you are between 5 to 10% of HP.
			case IGNORE_DEATH_USAGE_STYLE_ONE:
			{
				if (getPlayerHealthPercent() < Rnd.get(5, 10) && isSkillAvailable(IGNORE_DEATH_ID))
					return IGNORE_DEATH_ID;
				
				break;
			}
		}
		
		// If the target is "far"...
		if (distanceToTarget > 100)
		{
			// Attempt pulling it.
			if (isSkillAvailable(CHAIN_STRIKE_ID))
				return CHAIN_STRIKE_ID;
			
			// From time to time, aggro it.
			if (Rnd.get(0, 3) == 0 && isSkillAvailable(SUPERIOR_AGGRESSION_ID))
				return SUPERIOR_AGGRESSION_ID;
		}
		
		/**
		 * If there are nearby enemies, we prioritize the use of Area of Effect skills.
		 * ...
		 */
		
		// We only use Shield Wave if there's actually more than just a dude nearby.
		// It stuns and has a fat re-use delay (60 seconds).
		if (isSkillAvailable(SHIELD_WAVE_ID) && getEnemiesAmountNearby(getSkillRadius(SHIELD_WAVE_ID)) > 1)
			return SHIELD_WAVE_ID;
		
		if (isSkillAvailable(CHAIN_GALAXY_ID) && Rnd.nextBoolean() && getEnemiesAmountNearby(getSkillRadius(CHAIN_GALAXY_ID)) > 0)
			return CHAIN_GALAXY_ID;
		
		if (isSkillAvailable(GUST_BLADE_ID) && Rnd.nextBoolean() && getEnemiesAmountNearby(getSkillRadius(GUST_BLADE_ID)) > 0)
			return GUST_BLADE_ID;
		
		/**
		 * Single target skills now.
		 * ...
		 */
		
		if (isSkillAvailable(LAST_JUDGMENT_ID) && Rnd.nextBoolean())
			return LAST_JUDGMENT_ID;
		
		// TODO
		// Make this one 100% use if the target is a mage.
		if (isSkillAvailable(JUSTICE_PUNISHMENT_ID) && Rnd.get(0, 3) == 0)
			return JUSTICE_PUNISHMENT_ID;
		
		if (isSkillAvailable(SHIELD_IMPACT_ID))
			return SHIELD_IMPACT_ID;
		
		if (isSkillAvailable(SHIELD_CHARGE_ID))
			return SHIELD_CHARGE_ID;

		switch (_ultimatesUsageStyle)
		{
			// Style 1: 
			// Use Knight Frenzy whenever possible.
			// Use Spike Shield whenever possible.
			// Use Protection of Faith when hitting less than 50% HP.
			// Use Final Ultimate Defense when hitting less than 25% HP.
			// Use Ignore Death when hitting less than 5% HP.
			case ULTIMATES_USAGE_STYLE_ONE:
			{
				if (isSkillAvailable(KNIGHT_FRENZY_ID))
					return KNIGHT_FRENZY_ID;
				
				if (isSkillAvailable(SPIKE_SHIELD_ID))
					return SPIKE_SHIELD_ID;
				
				final int healthPercent = getHealthPercent(_player);
				
				if (healthPercent < 50 && isSkillAvailable(PROTECTION_OF_FAITH_ID))
					return PROTECTION_OF_FAITH_ID;
				
				if (healthPercent < 25 && isSkillAvailable(FINAL_ULTIMATE_DEFENSE_ID))
					return FINAL_ULTIMATE_DEFENSE_ID;
				
				if (healthPercent < 5 && isSkillAvailable(IGNORE_DEATH_ID))
					return IGNORE_DEATH_ID;
				
				break;
			}
			// Style 2: 
			// Use Knight Frenzy whenever possible.
			// Use Spike Shield whenever Protection of Faith and Final Ultimate Defense aren't on.
			// Use Protection of Faith when hitting less than 50% HP.
			// Use Final Ultimate Defense when hitting less than 50% HP once Protection of Faith has worn off.
			// Use Ignore Death when hitting less than 5% HP.
			case ULTIMATES_USAGE_STYLE_TWO:
			{
				final int healthPercent = getHealthPercent(_player);
				
				if (isSkillAvailable(KNIGHT_FRENZY_ID))
					return KNIGHT_FRENZY_ID;
				
				if (isSkillAvailable(SPIKE_SHIELD_ID) && !isEffectActive(PROTECTION_OF_FAITH_ID) && !isEffectActive(FINAL_ULTIMATE_DEFENSE_ID))
					return SPIKE_SHIELD_ID;
				
				if (healthPercent < 50 && isSkillAvailable(PROTECTION_OF_FAITH_ID))
					return PROTECTION_OF_FAITH_ID;
				
				if (healthPercent < 50 && isSkillAvailable(FINAL_ULTIMATE_DEFENSE_ID) && !isEffectActive(PROTECTION_OF_FAITH_ID))
					return FINAL_ULTIMATE_DEFENSE_ID;
				
				if (healthPercent < 5 && isSkillAvailable(IGNORE_DEATH_ID))
					return IGNORE_DEATH_ID;
				
				break;
			}
		}
		
		return super.pickSpecialSkill(target);
	}
	
	protected final boolean isAuraActive()
	{
		if (_player.getFirstEffect(_favoriteAura) == null)
			return false;
		
		 return true;
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
	
	@Override
	public int[] getSummonSkillIds()
	{
		if (!_canUseSummon)
			return super.getSummonSkillIds();
		
		return SUMMON_SKILL_IDS;
	}
}