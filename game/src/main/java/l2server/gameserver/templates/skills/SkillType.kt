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

package l2server.gameserver.templates.skills

import l2server.gameserver.model.Skill
import l2server.gameserver.stats.skills.*
import l2server.gameserver.templates.StatsSet

/**
 * @author nBd
 */
enum class SkillType(private val _class: Class<out Skill> = SkillDefault::class.java) {
    // Damage
    PDAM,
    MDAM,
    CPDAM,
    MANADAM,
    CPDAMPERCENT,
    MAXHPDAMPERCENT,
    DRAIN_SOUL,
    DRAIN(SkillDrain::class.java),
    DEATHLINK,
    FATAL,
    BLOW,
    SIGNET(SkillSignet::class.java),
    SIGNET_CASTTIME(SkillSignetCasttime::class.java),
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
    SIEGEFLAG(SkillSiegeFlag::class.java),
    TAKECASTLE,
    TAKEFORT,
    WEAPON_SA,
    DELUXE_KEY_UNLOCK,
    SOW,
    HARVEST,
    GET_PLAYER,
    AGATHION(SkillAgathion::class.java),
    MOUNT(SkillMount::class.java),
    INSTANT_JUMP,
    DETECTION,
    DUMMY,

    // Creation
    COMMON_CRAFT,
    DWARVEN_CRAFT,
    CREATE_ITEM(SkillCreateItem::class.java),
    EXTRACTABLE,
    EXTRACTABLE_FISH,
    LEARN_SKILL(SkillLearnSkill::class.java),

    // Summons
    SUMMON(SkillSummon::class.java),
    FEED_PET,
    DEATHLINK_PET,
    STRSIEGEASSAULT,
    ERASE,
    BETRAY,
    DECOY(SkillDecoy::class.java),
    SPAWN(SkillSpawn::class.java),

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
    CONTINUOUS_DRAIN(SkillContinuousDrain::class.java),
    CONTINUOUS_CASTS(SkillContinuousCasts::class.java),

    RESURRECT,
    CHARGEDAM(SkillChargeDmg::class.java),
    DETECT_WEAKNESS,
    LUCK,
    RECALL(SkillTeleport::class.java),
    TELEPORT(SkillTeleport::class.java),
    SUMMON_FRIEND,
    GO_TO_FRIEND,
    SWITCH_POSITION,
    SPOIL,
    SWEEP(SkillSweeper::class.java),
    FAKE_DEATH,
    UNDEAD_DEFENSE,
    BEAST_FEED,
    BEAST_RELEASE,
    BEAST_RELEASE_ALL,
    BEAST_SKILL,
    BEAST_ACCOMPANY,
    CHARGESOUL,
    TRANSFORMDISPEL,
    SUMMON_TRAP(SkillTrap::class.java),
    DETECT_TRAP,
    REMOVE_TRAP,
    SHIFT_TARGET,
    // Kamael WeaponChange
    CHANGEWEAPON(SkillChangeWeapon::class.java),

    STEAL_BUFF,
    RESET,

    // Skill is done within the core.
    COREDONE,

    CLASS_CHANGE,

    CHANGE_APPEARANCE(SkillAppearance::class.java),
    CHANGE_HAIR_ACCESSORY,

    // Refuel airship
    REFUEL,
    // Nornil's Power (Nornil's Garden instance)
    NORNILS_POWER,

    // unimplemented
    NOTDONE,
    BALLISTA;

    @Throws(RuntimeException::class)
    fun makeSkill(set: StatsSet): Skill {
        try {
            val c = _class.getConstructor(StatsSet::class.java)

            return c.newInstance(set)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
