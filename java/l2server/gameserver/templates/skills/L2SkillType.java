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

package l2server.gameserver.templates.skills;

import l2server.gameserver.model.L2Skill;
import l2server.gameserver.stats.skills.*;
import l2server.gameserver.templates.StatsSet;

import java.lang.reflect.Constructor;

/**
 * @author nBd
 */
public enum L2SkillType
{
	// Damage
	PDAM,
	MDAM,
	CPDAM,
	MANADAM,
	CPDAMPERCENT,
	MAXHPDAMPERCENT,
	DRAIN_SOUL,
	DRAIN(L2SkillDrain.class),
	DEATHLINK,
	FATAL,
	BLOW,
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	MARK,

	// Disablers

	// hp, mp, cp
	HEAL,
	BALANCE_LIFE,
	HEAL_PERCENT,
	HEAL_STATIC,
	COMBATPOINTHEAL,
	CPHEAL_PERCENT,
	MANAHEAL,
	MANA_BY_LEVEL,
	MANAHEAL_PERCENT,
	MANARECHARGE,
	HPMPCPHEAL_PERCENT,
	HPMPHEAL_PERCENT,
	HPCPHEAL_PERCENT,
	CHAIN_HEAL,
	OVERHEAL,
	OVERHEAL_STATIC,

	// sp
	GIVE_SP,
	// reco
	GIVE_RECO,
	// vitality
	GIVE_VITALITY,

	// Aggro
	AGGDAMAGE,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	AGGDEBUFF,

	// Fishing
	FISHING,
	PUMPING,
	REELING,

	// MISC
	UNLOCK,
	UNLOCK_SPECIAL,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	ENCHANT_HAIR_ACCESSORY,
	ENCHANT_ATTRIBUTE,
	SOULSHOT,
	SPIRITSHOT,
	SIEGEFLAG(L2SkillSiegeFlag.class),
	TAKECASTLE,
	TAKEFORT,
	WEAPON_SA,
	DELUXE_KEY_UNLOCK,
	SOW,
	HARVEST,
	GET_PLAYER,
	AGATHION(L2SkillAgathion.class),
	MOUNT(L2SkillMount.class),
	INSTANT_JUMP,
	DETECTION,
	DUMMY,

	// Creation
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	CREATE_ITEM(L2SkillCreateItem.class),
	EXTRACTABLE,
	EXTRACTABLE_FISH,
	LEARN_SKILL(L2SkillLearnSkill.class),

	// Summons
	SUMMON(L2SkillSummon.class),
	FEED_PET,
	DEATHLINK_PET,
	STRSIEGEASSAULT,
	ERASE,
	BETRAY,
	DECOY(L2SkillDecoy.class),
	SPAWN(L2SkillSpawn.class),

	// Cancel
	CANCEL,
	CANCEL_STATS,
	CANCEL_DEBUFF,
	NEGATE,

	BUFF,
	DEBUFF,
	PASSIVE,
	CONT,
	FUSION,
	CONTINUOUS_DEBUFF,
	CONTINUOUS_DRAIN(L2SkillContinuousDrain.class),
	CONTINUOUS_CASTS(L2SkillContinuousCasts.class),

	RESURRECT,
	CHARGEDAM(L2SkillChargeDmg.class),
	DETECT_WEAKNESS,
	LUCK,
	RECALL(L2SkillTeleport.class),
	TELEPORT(L2SkillTeleport.class),
	SUMMON_FRIEND,
	GO_TO_FRIEND,
	SWITCH_POSITION,
	SPOIL,
	SWEEP(L2SkillSweeper.class),
	FAKE_DEATH,
	UNDEAD_DEFENSE,
	BEAST_FEED,
	BEAST_RELEASE,
	BEAST_RELEASE_ALL,
	BEAST_SKILL,
	BEAST_ACCOMPANY,
	CHARGESOUL,
	TRANSFORMDISPEL,
	SUMMON_TRAP(L2SkillTrap.class),
	DETECT_TRAP,
	REMOVE_TRAP,
	SHIFT_TARGET,
	// Kamael WeaponChange
	CHANGEWEAPON(L2SkillChangeWeapon.class),

	STEAL_BUFF,
	RESET,

	// Skill is done within the core.
	COREDONE,

	CLASS_CHANGE,

	CHANGE_APPEARANCE(L2SkillAppearance.class),
	CHANGE_HAIR_ACCESSORY,

	// Refuel airship
	REFUEL,
	// Nornil's Power (Nornil's Garden instance)
	NORNILS_POWER,

	// unimplemented
	NOTDONE,
	BALLISTA;

	private final Class<? extends L2Skill> _class;

	public L2Skill makeSkill(StatsSet set) throws RuntimeException
	{
		try
		{
			Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);

			return c.newInstance(set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	L2SkillType()
	{
		_class = L2SkillDefault.class;
	}

	L2SkillType(Class<? extends L2Skill> classType)
	{
		_class = classType;
	}
}
