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

package l2server.gameserver.stats;

import java.util.NoSuchElementException;

/**
 * Enum of basic stats.
 *
 * @author mkizub
 */
public enum Stats
{
	// Base stats, for each in Calculator a slot is allocated

	// HP & MP
	MAX_HP("maxHp"),
	MAX_MP("maxMp"),
	MAX_CP("maxCp"),
	REGENERATE_HP_RATE("regHp"),
	REGENERATE_CP_RATE("regCp"),
	REGENERATE_MP_RATE("regMp"),
	RECHARGE_MP_RATE("gainMp"),
	HEAL_EFFECTIVNESS("gainHp"),
	HEAL_PROFICIENCY("giveHp"),
	HEAL_STATIC_BONUS("bonusHp"),
	GAIN_HP_LIMIT("gainHpLimit"),
	GAIN_CP_LIMIT("gainCpLimit"),
	GAIN_MP_LIMIT("gainMpLimit"),
	LIMIT_HP("limitHp"),
	// non-displayed hp limit

	// ATTACK & DEFENCE
	PHYS_DEFENSE("pDef"),
	MAGIC_DEFENSE("mDef"),
	PHYS_ATTACK("pAtk"),
	MAGIC_ATTACK("mAtk"),
	PHYSICAL_SKILL_POWER("physicalSkillPower"),
	MAGIC_SKILL_POWER("mSkillPower"),
	POWER_ATTACK_SPEED("pAtkSpd"),
	MAGIC_ATTACK_SPEED("mAtkSpd"),
	// how fast a magic spell is casted (including animation)
	ATK_REUSE("atkReuse"),
	// make bows hit simple hits way slower and will not affect skills
	P_REUSE("pReuse"),
	MAGIC_REUSE_RATE("mReuse"),
	// how fast spells becomes ready to reuse
	SHIELD_DEFENCE("sDef"),
	CRITICAL_DAMAGE("pCritDmg"),
	CRITICAL_DAMAGE_ADD("pCritDmgAdd"),
	CRITICAL_ATTACK("pCritAtk"),
	// this is another type for special critical damage mods - crit damage SA
	PSKILL_CRIT_DMG("pSkillCritDmg"),
	MAGIC_CRIT_DMG("mCritDmg"),
	MAGIC_CRIT_DMG_ADD("mCritDmgAdd"),
	MAGIC_CRIT_ATTACK("mCritAtk"),
	MOMENTUM_POWER("momentumPower"),
	MOMENTUM_CRIT_RATE("momentumCritRate"),
	NORMAL_ATK_DMG("normalAtkDmg"),

	// PVP BONUS
	PVP_PHYSICAL_DMG("pvpPhysDmg"),
	PVP_MAGICAL_DMG("pvpMagicalDmg"),
	PVP_PHYS_SKILL_DMG("pvpPhysSkillsDmg"),
	PVP_PHYSICAL_DEF("pvpPhysDef"),
	PVP_MAGICAL_DEF("pvpMagicalDef"),
	PVP_PHYS_SKILL_DEF("pvpPhysSkillsDef"),

	// PVE BONUS
	PVE_PHYSICAL_DMG("pvePhysDmg"),
	PVE_PHYS_SKILL_DMG("pvePhysSkillsDmg"),
	PVE_BOW_DMG("pveBowDmg"),
	PVE_BOW_SKILL_DMG("pveBowSkillsDmg"),
	PVE_MAGICAL_DMG("pveMagicalDmg"),
	PVE_PHYSICAL_DEF("pvePhysDef"),
	PVE_MAGICAL_DEF("pveMagicalDef"),
	PVE_PHYS_SKILL_DEF("pvePhysSkillsDef"),

	// Atk & Def rates
	P_EVASION_RATE("rEvas"),
	M_EVASION_RATE("mEvasRate"),
	P_SKILL_EVASION("pSkillEvas"),
	M_SKILL_EVASION("mSkillEvas"),
	SHIELD_RATE("rShld"),
	CRITICAL_RATE("rCrit"),
	CRIT_DAMAGE_EVASION("critDamEvas"),
	MAX_CRITICAL_RATE("maxCrit"),
	MAX_MAGIC_CRITICAL_RATE("maxMCrit"),
	BLOW_RATE("blowRate"),
	LETHAL_RATE("lethalRate"),
	PCRITICAL_RATE("pSkillCritRate"),
	MCRITICAL_RATE("mCritRate"),
	MCRITICAL_RECV_RATE("mCritRecvRate"),
	HEAL_CRIT_RATE("healCritRate"),

	EXP_RATE("rExp"),
	SP_RATE("rSp"),
	DROP_RATE("rDrop"),

	ATTACK_CANCEL("cancel"),

	// ACCURACY & RANGE
	ACCURACY_COMBAT("accCombat"),
	ACCURACY_MAGIC("accMagic"),
	POWER_ATTACK_RANGE("pAtkRange"),
	MAGIC_ATTACK_RANGE("mAtkRange"),
	POWER_ATTACK_ANGLE("pAtkAngle"),
	ATTACK_COUNT_MAX("atkCountMax"),

	// Run speed, walk & escape speed are calculated proportionally, magic speed is a buff
	RUN_SPEED("runSpd"),
	WALK_SPEED("walkSpd"),

	// Position-based stats
	CRITICAL_RATE_FRONT("frontCritRate"),
	CRITICAL_RATE_SIDE("sideCritRate"),
	CRITICAL_RATE_BEHIND("behindCritRate"),
	CRITICAL_DMG_FRONT("frontCritDmg"),
	CRITICAL_DMG_SIDE("sideCritDmg"),
	CRITICAL_DMG_BEHIND("behindCritDmg"),

	// BASIC STATS
	STAT_STR("STR"),
	STAT_CON("CON"),
	STAT_DEX("DEX"),
	STAT_INT("INT"),
	STAT_WIT("WIT"),
	STAT_MEN("MEN"),
	STAT_LUC("LUC"),
	STAT_CHA("CHA"),

	// Special stats, share one slot in Calculator

	// VARIOUS
	BREATH("breath"),
	FALL("fall"),
	AGGRESSION("aggression"),
	// locks a mob on tank caster
	AGGRESSION_PROF("aggressionProf"),
	AGGRESSION_DMG("aggressionDmg"),
	VALAKAS("valakas"),

	CRIT_VULN("critVuln"),
	// Resistance to Crit dmg in percent
	CRIT_ADD_VULN("critAddVuln"),
	// Resistance to Crit dmg in value (ex: +100 will be 100 more crit dmg, NOT 100% more)
	REFLECT_VULN("reflectVuln"),
	MAGIC_DAMAGE_VULN("magicDamVul"),
	MAGIC_CRIT_VULN("magicCritVuln"),
	// Resistace to Crit dmg in percent
	MAGIC_SUCCESS_RES("magicSuccRes"),
	MAGIC_FAILURE_RATE("magicFailureRate"),
	DEBUFF_IMMUNITY("debuffImmunity"),
	// "Bool" value, considered true if greater than 0
	BUFF_IMMUNITY("buffImmunity"),
	// "Bool" value, considered true if greater than 0
	BLOCK_RESURRECTION("blockResurrection"),
	//"Bool" value, considered true if greater than 0
	BLOCK_ESCAPE("blockEscape"),
	//"Bool" value, considered true if greater than 0
	IS_BEHIND("isBehind"),
	//"Bool" value, considered true if greater than 0
	INVUL_RADIUS("invulRadius"),
	// "Limited" invulnerability, attacks above this range will not affect you
	FIXED_DMG_VULN("fixedDmgVuln"),
	// From the rare fixed dmg skills such as Topaz's active

	// BUFF/DEBUFF RESISTANCES
	BLEED_RES("bleedRes"),
	POISON_RES("poisonRes"),
	STUN_RES("stunRes"),
	PARALYSIS_RES("paralysisRes"),
	PETRIFY_RES("petrifyRes"),
	HOLD_RES("holdRes"),
	SLEEP_RES("sleepRes"),
	KNOCK_DOWN_RES("knockDownRes"),
	KNOCK_BACK_RES("knockBackRes"),
	PULL_RES("pullRes"),
	AERIAL_YOKE_RES("aerialYokeRes"),
	CANCEL_RES("cancelRes"),
	// Resistance for cancel type skills
	DERANGEMENT_RES("derangementRes"),
	PHYS_DEBUFF_RES("physDebuffRes"),
	MENTAL_DEBUFF_RES("mentalDebuffRes"),
	DEBUFF_RES("debuffRes"),
	LETHAL_RES("lethalRes"),

	BLEED_PROF("bleedProf"),
	POISON_PROF("poisonProf"),
	STUN_PROF("stunProf"),
	PARALYSIS_PROF("paralysisProf"),
	PETRIFY_PROF("petrifyProf"),
	HOLD_PROF("holdProf"),
	SLEEP_PROF("sleepProf"),
	KNOCK_DOWN_PROF("knockDownProf"),
	KNOCK_BACK_PROF("knockBackProf"),
	PULL_PROF("pullProf"),
	AERIAL_YOKE_PROF("aerialYokeProf"),
	CANCEL_PROF("cancelProf"),
	DERANGEMENT_PROF("derangementProf"),
	PHYS_DEBUFF_PROF("physDebuffProf"),
	MENTAL_DEBUFF_PROF("mentalDebuffProf"),
	DEBUFF_PROF("debuffProf"),

	// ELEMENTS
	FIRE_RES("fireRes"),
	WATER_RES("waterRes"),
	WIND_RES("windRes"),
	EARTH_RES("earthRes"),
	HOLY_RES("holyRes"),
	DARK_RES("darkRes"),
	VALAKAS_RES("valakasRes"),

	FIRE_POWER("firePower"),
	WATER_POWER("waterPower"),
	WIND_POWER("windPower"),
	EARTH_POWER("earthPower"),
	HOLY_POWER("holyPower"),
	DARK_POWER("darkPower"),

	// WEAPONS VULNERABILITIES
	SWORD_WPN_VULN("swordWpnVuln"),
	BLUNT_WPN_VULN("bluntWpnVuln"),
	DAGGER_WPN_VULN("daggerWpnVuln"),
	BOW_WPN_VULN("bowWpnVuln"),
	CROSSBOW_WPN_VULN("crossbowWpnVuln"),
	POLE_WPN_VULN("poleWpnVuln"),
	ETC_WPN_VULN("etcWpnVuln"),
	FIST_WPN_VULN("fistWpnVuln"),
	DUAL_WPN_VULN("dualWpnVuln"),
	DUALFIST_WPN_VULN("dualFistWpnVuln"),
	BIGSWORD_WPN_VULN("bigSwordWpnVuln"),
	BIGBLUNT_WPN_VULN("bigBluntWpnVuln"),
	DUALDAGGER_WPN_VULN("dualDaggerWpnVuln"),
	RAPIER_WPN_VULN("rapierWpnVuln"),
	ANCIENT_WPN_VULN("ancientWpnVuln"),
	PET_WPN_VULN("petWpnVuln"),

	REFLECT_DAMAGE_PERCENT("reflectDam"),
	REFLECT_DEBUFFS("reflectDebuffs"),
	VENGEANCE_SKILL_MAGIC_DAMAGE("vengeanceMdam"),
	VENGEANCE_SKILL_PHYSICAL_DAMAGE("vengeancePdam"),
	ABSORB_DAMAGE_PERCENT("absorbDam"),
	TRANSFER_DAMAGE_PERCENT("transDam"),
	MANA_SHIELD_PERCENT("manaShield"),
	TRANSFER_DAMAGE_TO_PLAYER("transDamToPlayer"),
	ABSORB_MANA_DAMAGE_PERCENT("absorbDamMana"),
	DAMAGE_CAP("dmgCap"),
	PSKILL_CRIT_PER_DEX("pSkillCritPerDex"),
	SPD_PER_DEX("spdPerDex"),

	MAX_LOAD("maxLoad"),

	PATK_PLANTS("pAtk-plants"),
	PATK_INSECTS("pAtk-insects"),
	PATK_ANIMALS("pAtk-animals"),
	PATK_MONSTERS("pAtk-monsters"),
	PATK_DRAGONS("pAtk-dragons"),
	PATK_GIANTS("pAtk-giants"),
	PATK_MCREATURES("pAtk-magicCreatures"),

	PDEF_PLANTS("pDef-plants"),
	PDEF_INSECTS("pDef-insects"),
	PDEF_ANIMALS("pDef-animals"),
	PDEF_MONSTERS("pDef-monsters"),
	PDEF_DRAGONS("pDef-dragons"),
	PDEF_GIANTS("pDef-giants"),
	PDEF_MCREATURES("pDef-magicCreatures"),

	// ExSkill
	LEVEL("level"),
	INVENTORY_LIMIT("inventoryLimit"),
	WAREHOUSE_LIMIT("whLimit"),
	FREIGHT_LIMIT("FreightLimit"),
	P_SELL_LIMIT("PrivateSellLimit"),
	P_BUY_LIMIT("PrivateBuyLimit"),
	REC_D_LIMIT("DwarfRecipeLimit"),
	REC_C_LIMIT("CommonRecipeLimit"),

	PHYSICAL_MP_CONSUME_RATE("PhysicalMpConsumeRate"),
	MAGICAL_MP_CONSUME_RATE("MagicalMpConsumeRate"),
	DANCE_MP_CONSUME_RATE("DanceMpConsumeRate"),
	BOW_MP_CONSUME_RATE("BowMpConsumeRate"),
	HP_CONSUME_RATE("HpConsumeRate"),
	MP_CONSUME("MpConsume"),
	SOULSHOT_COUNT("soulShotCount"),

	transformId("transformId"),
	TALISMAN_SLOTS("talisman"),
	JEWELRY_SLOTS("jewelry"),
	CLOAK_SLOT("cloak"),

	// Servitor Share
	OWNER_PATK("ownerPAtk"),
	OWNER_PDEF("ownerPDef"),
	OWNER_MATK("ownerMAtk"),
	OWNER_MDEF("ownerMDef"),
	OWNER_MAXHP("ownerMaxHp"),
	OWNER_MAXMP("ownerMaxMp"),
	OWNER_CRIT("ownerCrit"),
	OWNER_CRIT_DMG("ownerCritDmg"),
	OWNER_PATKSPD("ownerPAtkSpd"),
	OWNER_MATKSPD("ownerMAtkSpd"),
	OWNER_PVP_PVE("ownerPvPPvE"),
	// "Bool" value, considered true if greater than 0

	SERVITOR_ACCURACY("servitorAccuracy"),

	// GD1 stats
	SUMMON_POINTS("summonPoints"),

	// Shield Stats
	SHIELD_DEFENCE_ANGLE("shieldDefAngle"),

	// Skill mastery
	SKILL_MASTERY("skillMastery"),

	//Extra global chats
	GLOBAL_CHAT("globalChat"),

	// vitality
	VITALITY_CONSUME_RATE("vitalityConsumeRate");

	public static final int NUM_STATS = values().length;

	private String _value;

	public String getValue()
	{
		return _value;
	}

	Stats(String s)
	{
		_value = s;
	}

	public static Stats fromString(String name)
	{
		name = name.intern();
		for (Stats s : values())
		{
			if (s.getValue().equals(name))
			{
				return s;
			}
		}

		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}
}
