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
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class MysticMuseController extends NukerController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1177, // Wind Strike ^_^ 
		1175, // Aqua Swirl
		1264, // Solar Spark
		1295, // Aqua Splash
		1290, // Blizzard
		1265, // Solar Flare
		1235, // Hydro Blast
		1468, // Star Fall
		1340, // Ice Vortex
		1554, // Aura Blast
		1293, // Elemental Symphony
		1288, // Aura Symphony
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		1182, // Resist Aqua
		1047, // Mana Regeneration
		1238, // Freezing Skin
		1286, // Seed of Water
		1493, // Frost Armor
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		1164, // Curse Weakness
		1169, // Curse Fear
		1237, // Ice Dagger
		1338, // Arcane Chaos
		1056, // Cancellation
	};
	
	private static final int SURRENDER_TO_WATER_ID = 1071;
	private static final int ICE_VORTEX_ID = 1340;
	private static final int ICE_VORTEX_CRUSHER_ID = 1453;
	private static final int ARCANE_SHIELD_ID = 1556;
	private static final int CANCELLATION_ID = 1056;
	
	private byte _vortexCrusherUsageStyle;
	private byte _arcanaShieldUsageStyle;
	
	// Style 1: Slug right away when the Vortex effect shows up on target.
	private static final byte VORTEX_CRUSHER_USAGE_STYLE_ONE = 0;
	
	// Style 2: Slug when the Vortex effect is about to fade off from the target.
	private static final byte VORTEX_CRUSHER_USAGE_STYLE_TWO = 1;
	
	// Style 1: Use Arcane Shield when hitting 75% HP.
	private static final byte ARCANE_SHIELD_USAGE_STYLE_ONE = 0;
	
	// Style 2: Use Arcane Shield when hitting 50% HP.
	private static final byte ARCANE_SHIELD_USAGE_STYLE_TWO = 1;
	
	// Style 3: Use Arcane Shield when hitting 25% HP.
	private static final byte ARCANE_SHIELD_USAGE_STYLE_THREE = 2;
	
	public MysticMuseController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (target.getFirstEffect(SURRENDER_TO_WATER_ID) == null)
			return SURRENDER_TO_WATER_ID;
		
		if (targetedPlayer != null)
		{
			if (Rnd.get(0, 3) == 0 && targetedPlayer.isMage() && isSkillAvailable(CANCELLATION_ID))
				return CANCELLATION_ID;
		}
		
		if (target.getFirstEffect(ICE_VORTEX_ID) != null)
		{
			if (isSkillAvailable(ICE_VORTEX_CRUSHER_ID))
			{
				if (_vortexCrusherUsageStyle == -1)
					_vortexCrusherUsageStyle = (byte) Rnd.get(VORTEX_CRUSHER_USAGE_STYLE_ONE, VORTEX_CRUSHER_USAGE_STYLE_TWO);
				
				// Just use it right away.
				if (_vortexCrusherUsageStyle == VORTEX_CRUSHER_USAGE_STYLE_ONE)
					return ICE_VORTEX_CRUSHER_ID;
				else if (_vortexCrusherUsageStyle == VORTEX_CRUSHER_USAGE_STYLE_TWO)
				{
					// Slug only when the Vortex is about to fade off.
					if (target.getFirstEffect(ICE_VORTEX_ID).getTime() < 5)
						return ICE_VORTEX_CRUSHER_ID;
				}
			}
		}
		else
			_vortexCrusherUsageStyle = -1;
		
		final int healthPercent = getPlayerHealthPercent();
		
		if (isSkillAvailable(ARCANE_SHIELD_ID))
		{
			if (_arcanaShieldUsageStyle == -1)
				_arcanaShieldUsageStyle = (byte) Rnd.get(ARCANE_SHIELD_USAGE_STYLE_ONE, ARCANE_SHIELD_USAGE_STYLE_THREE);
			
			// Use Arcane Shield when the HP gets lower than 75%...
			if (_arcanaShieldUsageStyle == ARCANE_SHIELD_USAGE_STYLE_ONE)
			{
				if (healthPercent < 75)
					return ARCANE_SHIELD_ID;
			}
			// Use Arcane Shield when the HP gets lower than 50%...
			else if (_arcanaShieldUsageStyle == ARCANE_SHIELD_USAGE_STYLE_TWO)
			{
				if (healthPercent < 50)
					return ARCANE_SHIELD_ID;
			}
			// Use Arcane Shield when the HP gets lower than 50%...
			else if (_arcanaShieldUsageStyle == ARCANE_SHIELD_USAGE_STYLE_THREE)
			{
				if (healthPercent < 25)
					return ARCANE_SHIELD_ID;
			}
		}
		else
			_arcanaShieldUsageStyle = -1;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final boolean isAllowedToUseEmergencySkills()
	{
		return false;
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