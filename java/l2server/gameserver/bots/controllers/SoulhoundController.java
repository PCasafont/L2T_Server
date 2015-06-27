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
import l2server.gameserver.bots.DamageType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class SoulhoundController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1434, // Dark Explosion
		1431, // Fallen Arrow
		1433, // Abyssal Blaze
		1555, // Aura Cannon
		1439, // Curse of Divinity
		1438, // Annihilation Circle
		1436, // Soul of Pain
		1516, // Soul Strike
		1512, // Soul Vortex
		1469, // Leopold
		1554, // Aura Blast
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		1443, // Dark Weapon
		1442, // Protection from Darkness
		1515, // Lightning Barrier
		1444, // Pride of Kamael
		1441, // Soul to Empower
	};
	
	private static final int SHINING_EDGE_ID = 505;
	private static final int CHECKMATE_ID = 506; // Requires Death Mark on target.
	private static final int TRIPLE_THRUST_ID = 504;
	private static final int DOUBLE_THRUST_ID = 478;
	
	private static final int DARK_FLAME_ID = 1437;
	
	private static final int SHADOW_BIND_ID = 1437; // Roots the target.
	private static final int VOICE_BIND_ID = 1447; // Silences the target.
	
	private static final int BLINK_ID = 1448;
	
	private static final int STEAL_DIVINITY_ID = 1440;
	private static final int DEATH_MARK_ID = 1435;
	private static final int CURSE_OF_LIFE_FLOW_ID = 1511;
	private static final int SOUL_WEB_ID = 1529;
	private static final int SURRENDER_TO_DARK_ID = 1445;
	
	private static final int LIFE_TO_SOUL_ID = 502;
	
	private static final int PAINKILLER_ID = 837;
	private static final int COURAGE_ID = 499;
	private static final int SWORD_SHIELD_ID = 483;
	private static final int ARCANE_SHIELD_ID = 1556;
	private static final int FINAL_FORM_ID = 538;
	
	private static final int SOUL_CLEANSE_ID = 1510;
	
	// Toggles
	private static final int STRIKE_BACK_ID = 475;
	private static final int HARD_MARCH_ID = 479;
	private static final int DARK_BLADE_ID = 480;
	private static final int DARK_ARMOR_ID = 481;
	
	private byte _arcanaShieldUsageStyle;
	private byte _finalFormUsageStyle;
	
	// Style 1: Use Arcane Shield when hitting 75% HP.
	private static final byte ARCANE_SHIELD_USAGE_STYLE_ONE = 0;
	
	// Style 2: Use Arcane Shield when hitting 50% HP.
	private static final byte ARCANE_SHIELD_USAGE_STYLE_TWO = 1;
	
	// Style 3: Use Arcane Shield when hitting 25% HP.
	private static final byte ARCANE_SHIELD_USAGE_STYLE_THREE = 2;
	
	public SoulhoundController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.RAPIER;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.LIGHT;
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		// 1 chance / 4 to use Curse of Life Flow.
		if (Rnd.get(0, 3) == 0 && isSkillAvailable(CURSE_OF_LIFE_FLOW_ID) && target.getFirstEffect(CURSE_OF_LIFE_FLOW_ID) == null)
			return CURSE_OF_LIFE_FLOW_ID;
		
		// 1 chance / 6 to use Soul Cleanse if a debuff is active.
		if (Rnd.get(0, 5) == 0 && _player.getAllDebuffs().length != 0 && isSkillAvailable(SOUL_CLEANSE_ID))
			return SOUL_CLEANSE_ID;
		
		final int totalPhysicalSkillDamages = getTotalDamagesByType(DamageType.PHYSICAL_SKILL);
		final int totalPhysicalAttackDamages = getTotalDamagesByType(DamageType.PHYSICAL_ATTACK);
		
		// Use Sword Shield when we received too many physical damages.
		if (isSkillAvailable(SWORD_SHIELD_ID) && (totalPhysicalSkillDamages > 1500 || totalPhysicalAttackDamages > 1500))
			return SWORD_SHIELD_ID;
		
		// We just use Courage randomly from time to time when available.
		if (isSkillAvailable(COURAGE_ID))
			return COURAGE_ID;
		
		int distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), target.getX(), target.getY(), target.getZ(), false);
		
		// Use close range skills from time to time - or - when close to target.
		if (distance < 100/* || (!_player.isRooted() && Rnd.get(0, 8) == 0)*/)
		{
			// From time to time we blink right away if possible...
			if (Rnd.get(0, 5) == 0 && isSkillAvailable(BLINK_ID))
				return BLINK_ID;
			
			if (isSkillAvailable(CHECKMATE_ID) && target.getFirstEffect(DEATH_MARK_ID) != null)
				return CHECKMATE_ID;
			
			if (isSkillAvailable(SHINING_EDGE_ID))
				return SHINING_EDGE_ID;
			
			if (isSkillAvailable(TRIPLE_THRUST_ID))
				return TRIPLE_THRUST_ID;
			
			if (isSkillAvailable(DARK_FLAME_ID))
				return DARK_FLAME_ID;
			
			if (Rnd.nextBoolean() && isSkillAvailable(DOUBLE_THRUST_ID))
				return DOUBLE_THRUST_ID;
			
			if (isSkillAvailable(BLINK_ID))
				return BLINK_ID;
		}
		
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
			// Use Arcane Shield when the HP gets lower than 25%...
			else if (_arcanaShieldUsageStyle == ARCANE_SHIELD_USAGE_STYLE_THREE)
			{
				if (healthPercent < 25)
					return ARCANE_SHIELD_ID;
			}
		}
		else
			_arcanaShieldUsageStyle = -1;
		
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (targetedPlayer != null)
		{
			if (targetedPlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				final int[] movementTrace = targetedPlayer.getMovementTrace();
				final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
				
				final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
				final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);
				
				// Player is running away...
				if (distanceAfterMove > distanceBeforeMove)
				{
					final boolean isTargetSlownDown = target.getFirstEffect(SHADOW_BIND_ID) != null || target.getFirstEffect(SOUL_WEB_ID) != null;
					
					// Let's slow him down with either Shadow Bind or Soul Web.
					if (!isTargetSlownDown)
					{
						int randomSlowSkillId = Rnd.nextBoolean() ? SHADOW_BIND_ID : SOUL_WEB_ID;
						
						if (!isSkillAvailable(randomSlowSkillId))
							randomSlowSkillId = randomSlowSkillId == SHADOW_BIND_ID ? SOUL_WEB_ID : SHADOW_BIND_ID;
						
						if (!isSkillAvailable(randomSlowSkillId))
							randomSlowSkillId = -1;
						
						if (randomSlowSkillId != -1)
							return randomSlowSkillId;
					}
				}
			}
			
			// Voice bind if the target is a mage, from time to time.
			if (Rnd.get(0, 2) == 0 && isSkillAvailable(VOICE_BIND_ID) && !target.isMuted() && targetedPlayer.isMage())
				return VOICE_BIND_ID;
			
			// Hi-jack target buffs from time to time.
			if (Rnd.get(0, 2) == 0 && isSkillAvailable(STEAL_DIVINITY_ID))
				return STEAL_DIVINITY_ID;
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
