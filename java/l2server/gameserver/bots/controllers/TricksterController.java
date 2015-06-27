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

import l2server.gameserver.bots.DamageType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class TricksterController extends ArcherController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		487, // Penetrating Shot
		508, // Rising Shot
		507, // Twin Shot
		790, // Wild Shot
		990, // Death Shot
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		482, // Furious Soul
		490, // Fast Shot
	};
	
	private final static int[] GET_ON_TARGET_SKILL_IDS =
	{
		628, // Warp
	};
	
	private final static int[] CRITICAL_STATE_SKILL_IDS =
	{
		622, // Ultimate Escape
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		627, // Soul Shock
		509, // Bleeding Shot
	};
	
	private static final int[] TRAP_SKILL_IDS =
	{
		514, // Fire Trap
		516, // Slow Trap
		517, // Flash Trap
		518, // Binding Trap
	};
	
	private static final int DECOY_ID = 525;
	private static final int IMBUE_DARK_SEED_ID = 523;
	private static final int IMBUE_SEED_OF_DESTRUCTION_ID = 835;
	private static final int BETRAYAL_MARK_ID = 792;
	private static final int REAL_TARGET_ID = 522;
	private static final int SOUL_BARRIER_ID = 1514;
	private static final int PRAHNAH_ID = 1470;
	private static final int SOUL_CLEANSE_ID = 1510;
	private static final int HARD_MARCH_ID = 479;
	private static final int WARP_ID = 628;
	
	public TricksterController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		if (isSkillAvailable(HARD_MARCH_ID) && _player.getFirstEffect(HARD_MARCH_ID) == null)
			return HARD_MARCH_ID;
		
		if (target.getFirstEffect(REAL_TARGET_ID) == null)
			return REAL_TARGET_ID;
		
		// 1 chance / 6 to use Soul Cleanse if a debuff is active.
		if (Rnd.get(0, 5) == 0 && _player.getAllDebuffs().length != 0 && isSkillAvailable(SOUL_CLEANSE_ID))
			return SOUL_CLEANSE_ID;
		
		int totalMagicalAttackDamages = getTotalDamagesByType(DamageType.MAGICAL_ATTACK);
		
		// Use Prahnah when receiving too many magical damages...
		if (isSkillAvailable(PRAHNAH_ID) && totalMagicalAttackDamages > 1000)
			return PRAHNAH_ID;
		
		// Use Soul Barrier when receiving too many magical/bow damages and on low HP.
		// TODO - Trigger when receiving bow damages too.
		if (getPlayerHealthPercent() < 50 && isSkillAvailable(SOUL_BARRIER_ID) && totalMagicalAttackDamages > 1000)
			return SOUL_BARRIER_ID;
		
		// From time to time, summon a Decoy.
		if (Rnd.get(0, 10) == 0 && isSkillAvailable(DECOY_ID))
			return DECOY_ID;
		
		int distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), target.getX(), target.getY(), target.getZ(), false);
		
		boolean shouldSpawnTrap = false;
		// When we're closing to the target...
		if (distance < 150)
		{
			// From time to time...
			if (Rnd.get(0, 5) == 0)
			{
				boolean isTargetHavingDarkSeed = target.getFirstEffect(IMBUE_DARK_SEED_ID) != null;
				boolean isTargetHavingDestructionSeed = target.getFirstEffect(IMBUE_SEED_OF_DESTRUCTION_ID) != null;
				
				// Debuff the target with a seed.
				// Only throw a seed when the target isn't already debuffed by one.
				if (!isTargetHavingDarkSeed && !isTargetHavingDestructionSeed)
				{
					int randomSeedSkillId = Rnd.nextBoolean() ? IMBUE_DARK_SEED_ID : IMBUE_SEED_OF_DESTRUCTION_ID;
					
					if (isSkillAvailable(randomSeedSkillId))
						return randomSeedSkillId;
					
					randomSeedSkillId = randomSeedSkillId == IMBUE_DARK_SEED_ID ? IMBUE_SEED_OF_DESTRUCTION_ID : IMBUE_DARK_SEED_ID;
					
					if (isSkillAvailable(randomSeedSkillId))
						return randomSeedSkillId;
				}
			}
			
			if (isSkillAvailable(WARP_ID))
				return WARP_ID;
			
			if (Rnd.nextBoolean())
				shouldSpawnTrap = true;
		}
		
		if (!shouldSpawnTrap)
			shouldSpawnTrap = Rnd.get(0, 4) == 0;
		
		// From time to time, summon a Trap.
		if (shouldSpawnTrap)
		{
			int[] pickFromSkills = TRAP_SKILL_IDS;
			
			if (pickFromSkills.length != 0)
			{
				int selectedTrapSkillId = pickSkill(pickFromSkills, false);
				
				if (selectedTrapSkillId != -1)
					return selectedTrapSkillId;
			}
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected int getAttackRange()
	{
		return _player.getTemplate().baseAtkRange;
	}
	
	@Override
	protected boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.CROSSBOW;
	}
	
	@Override
	protected int getMinimumRangeToUseCatchupSkill()
	{
		return getAttackRange() * 5;
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
