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

package l2server.gameserver.model.actor.stat;

import l2server.Config;
import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.*;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.L2SkillType;

public class CharStat {
	// =========================================================
	// Data Field
	private L2Character activeChar;
	private long exp = 0;
	private long sp = 0;
	private byte level = 1;
	
	// =========================================================
	// Constructor
	public CharStat(L2Character activeChar) {
		this.activeChar = activeChar;
	}
	
	// =========================================================
	// Method - Public
	
	/**
	 * Calculate the new value of the state with modifiers that will be applied
	 * on the targeted L2Character.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object. A
	 * Func object is a mathematic function that permit to calculate the
	 * modifier of a state (ex : REGENERATE_HP_RATE...) : <BR>
	 * <BR>
	 * <p>
	 * FuncAtkAccuracy -> Math.sqrt(player.getDEX())*6+player.getLevel()<BR>
	 * <BR>
	 * <p>
	 * When the calc method of a calculator is launched, each mathematic
	 * function is called according to its priority <B>order</B>. Indeed, Func
	 * with lowest priority order is executed firsta and Funcs with the same
	 * order are executed in unspecified order. The result of the calculation is
	 * stored in the value property of an Env class instance.<BR>
	 * <BR>
	 *
	 * @param stat   The stat to calculate the new value with modifiers
	 * @param init   The initial value of the stat before applying modifiers
	 * @param target The L2Charcater whose properties will be used in the
	 *               calculation (ex : CON, INT...)
	 * @param skill  The L2Skill whose properties will be used in the calculation
	 *               (ex : Level...)
	 */
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill) {
		if (activeChar == null || stat == null) {
			return init;
		}
		
		int id = stat.ordinal();
		
		Calculator c = activeChar.getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if (c == null || c.size() == 0) {
			return init;
		}
		
		// Create and init an Env object to pass parameters to the Calculator
		Env env = new Env();
		env.player = activeChar;
		env.target = target;
		env.skill = skill;
		env.value = init;
		env.baseValue = init;
		// Launch the calculation
		c.calc(env);
		
		// avoid some troubles with negative stats (some stats should never be negative)
		if (env.value <= 0) {
			switch (stat) {
				case MAX_HP:
				case MAX_MP:
				case MAX_CP:
				case MAGIC_DEFENSE:
				case PHYS_DEFENSE:
				case PHYS_ATTACK:
				case MAGIC_ATTACK:
				case POWER_ATTACK_SPEED:
				case MAGIC_ATTACK_SPEED:
				case SHIELD_DEFENCE:
				case STAT_CON:
				case STAT_DEX:
				case STAT_INT:
				case STAT_MEN:
				case STAT_STR:
				case STAT_WIT:
				case STAT_LUC:
				case STAT_CHA:
					env.value = 1;
			}
		}
		
		return env.value;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	
	/**
	 * Return the Accuracy (base+modifier) of the L2Character in function of the
	 * Weapon Expertise Penalty.
	 */
	public int getAccuracy() {
		if (activeChar == null) {
			return 0;
		}
		
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
				((L2PcInstance) activeChar).getEvent().isType(EventType.StalkedSalkers)) {
			return 1000;
		}
		
		if (activeChar instanceof L2MonsterInstance) {
			final L2MonsterInstance monster = (L2MonsterInstance) activeChar;
			
			if (monster.getTemplate().FixedAccuracy != 0) {
				return monster.getTemplate().FixedAccuracy;
			}
		}
		
		return (int) Math.round(calcStat(Stats.ACCURACY_COMBAT, 0, null, null));
	}
	
	public int getMAccuracy() {
		if (activeChar == null) {
			return 0;
		}
		
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
				((L2PcInstance) activeChar).getEvent().isType(EventType.StalkedSalkers)) {
			return 1000;
		}
		
		return (int) Math.round(calcStat(Stats.ACCURACY_MAGIC, 0, null, null));
	}
	
	public L2Character getActiveChar() {
		return activeChar;
	}
	
	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to
	 * get proper animations.
	 */
	public final float getAttackSpeedMultiplier() {
		if (activeChar == null) {
			return 1;
		}
		
		int pAtkSpd = getPAtkSpd();
		return 1.1f * pAtkSpd / 250.0f;//activeChar.getTemplate().basePAtkSpd);
	}
	
	/**
	 * Return the CON of the L2Character (base+modifier).
	 */
	public final int getCON() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_CON, activeChar.getTemplate().baseCON, null, null);
	}
	
	/**
	 * Return the Critical Hit rate (base+modifier) of the L2Character.
	 */
	public int getCriticalHit(L2Character target, L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		
		double criticalHit = calcStat(Stats.CRITICAL_RATE, activeChar.getTemplate().baseCritRate, target, skill) * 10.0 + 0.5;
		
		if (Formulas.isInFrontOf(target, activeChar)) {
			criticalHit = calcStat(Stats.CRITICAL_RATE_FRONT, criticalHit, target, skill);
		} else if (Formulas.isBehind(target, activeChar)) {
			criticalHit = calcStat(Stats.CRITICAL_RATE_BEHIND, criticalHit, target, skill);
		} else {
			criticalHit = calcStat(Stats.CRITICAL_RATE_SIDE, criticalHit, target, skill);
		}
		
		if (target != null) {
			criticalHit /= target.calcStat(Stats.CRIT_DAMAGE_EVASION, 1, activeChar, skill);
		}
		
		criticalHit /= 10;
		
		// Set a cap of Critical Hit at 500
		int maxCritical = (int) Math.round(calcStat(Stats.MAX_CRITICAL_RATE, Config.MAX_PCRIT_RATE, target, skill));
		if (criticalHit > maxCritical) {
			criticalHit = maxCritical;
		}
		
		return (int) Math.round(criticalHit);
	}
	
	/**
	 * Return the DEX of the L2Character (base+modifier).
	 */
	public final int getDEX() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_DEX, activeChar.getTemplate().baseDEX, null, null);
	}
	
	/**
	 * Return the Attack Evasion rate (base+modifier) of the L2Character.
	 */
	public int getEvasionRate(L2Character target) {
		if (activeChar == null) {
			return 1;
		}
		
		int val = (int) Math.round(calcStat(Stats.P_EVASION_RATE, 0, target, null));
		
		if (activeChar instanceof L2MonsterInstance) {
			final L2MonsterInstance monster = (L2MonsterInstance) activeChar;
			
			if (monster.getTemplate().FixedEvasion != 0) {
				return monster.getTemplate().FixedEvasion;
			}
		}

		/*if (val > Config.MAX_EVASION && !activeChar.isGM())
			val = Config.MAX_EVASION;*/
		return val;
	}
	
	public int getMEvasionRate(L2Character target) {
		if (activeChar == null) {
			return 1;
		}
		
		int val = (int) Math.round(calcStat(Stats.M_EVASION_RATE, 0, target, null));
		
		if (activeChar instanceof L2MonsterInstance) {
			final L2MonsterInstance monster = (L2MonsterInstance) activeChar;
			
			if (monster.getTemplate().FixedEvasion != 0) {
				return monster.getTemplate().FixedEvasion;
			}
		}

		/*if (val > 300 && !activeChar.isGM())
			val = 300;*/
		return val;
	}
	
	public long getExp() {
		return exp;
	}
	
	public void setExp(long value) {
		exp = value;
	}
	
	/**
	 * Return the INT of the L2Character (base+modifier).
	 */
	public int getINT() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_INT, activeChar.getTemplate().baseINT, null, null);
	}
	
	public byte getLevel() {
		/*
		if (activeChar instanceof L2PcInstance)
		{
			final L2PcInstance activeChar = (L2PcInstance) activeChar;

			if (activeChar.getTemporaryLevel() != 0)
				return activeChar.getTemporaryLevel();

		}*/
		return level;
	}
	
	public void setLevel(byte value) {
		level = value;
	}
	
	/**
	 * Return the Magical Attack range (base+modifier) of the L2Character.
	 */
	public final int getMagicalAttackRange(L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		
		if (skill != null) {
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		
		return activeChar.getTemplate().baseAtkRange;
	}
	
	public int getMaxCp() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_CP, activeChar.getTemplate().baseCpMax, null, null);
	}
	
	public int getMaxHp() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.LIMIT_HP, getMaxVisibleHp(), null, null);
	}
	
	public int getMaxVisibleHp() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_HP, activeChar.getTemplate().baseHpMax, null, null);
	}
	
	public int getMaxMp() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_MP, activeChar.getTemplate().baseMpMax, null, null);
	}
	
	/**
	 * Return the MAtk (base+modifier) of the L2Character for a skill used in
	 * function of abnormal effects in progress.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li> Calculate Magic damage </li>
	 * <BR>
	 * <BR>
	 *
	 * @param target The L2Character targeted by the skill
	 * @param skill  The L2Skill used against the target
	 */
	public int getMAtk(L2Character target, L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion()) {
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}
		if (activeChar.isRaid()) {
			bonusAtk *= Config.RAID_MATTACK_MULTIPLIER;
		}
		double attack = activeChar.getTemplate().baseMAtk * bonusAtk;
		
		// Add the power of the skill to the attack effect
		if (skill != null) {
			attack += skill.getPower();
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character in function of
	 * the Armour Expertise Penalty.
	 */
	public int getMAtkSpd() {
		if (activeChar == null) {
			return 1;
		}
		float bonusSpdAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion()) {
			bonusSpdAtk = Config.L2JMOD_CHAMPION_SPD_ATK;
		}
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, activeChar.getTemplate().baseMAtkSpd * bonusSpdAtk, null, null);
		if (val > Config.MAX_MATK_SPEED) {
			val = Config.MAX_MATK_SPEED;
		}
		
		if (val > 1650) {
			val = 1650 + (int) Math.pow(val - 1650, 0.8);
		}
		
		return (int) val;
	}
	
	/**
	 * Return the Magic Critical Hit rate (base+modifier) of the L2Character.
	 */
	public final int getMCriticalHit(L2Character target, L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		
		double mrate = calcStat(Stats.MCRITICAL_RATE, activeChar.getTemplate().baseMCritRate, target, skill);
		if (target != null) {
			//Radiant Heal Panic Heal Brilliant Heal have 100% critical when the target have this stat
			if (calcStat(Stats.HEAL_CRIT_RATE, 1, target, skill) > 1 && skill.getSkillType() == L2SkillType.OVERHEAL && skill.getId() >= 11755 &&
					skill.getId() <= 11757) {
				return 1550;
			}
			
			mrate = target.calcStat(Stats.MCRITICAL_RECV_RATE, mrate, activeChar, skill);
		}
		
		if (mrate < 40) {
			mrate = 40;
		}
		
		int maxCritical = (int) Math.round(calcStat(Stats.MAX_MAGIC_CRITICAL_RATE, Config.MAX_MCRIT_RATE, target, skill));
		if (mrate > maxCritical) {
			mrate = maxCritical;
		}
		
		// For magical skills, critical rate is an additional percentage of chance
		if (skill != null && skill.getBaseCritRate() > 0) {
			mrate *= 1.0f + skill.getBaseCritRate() / 100.0f;
		}
		
		return (int) mrate;
	}
	
	/**
	 * Return the MDef (base+modifier) of the L2Character against a skill in
	 * function of abnormal effects in progress.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li> Calculate Magic damage </li>
	 * <BR>
	 *
	 * @param target The L2Character targeted by the skill
	 * @param skill  The L2Skill used against the target
	 */
	public int getMDef(L2Character target, L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		
		// Get the base MAtk of the L2Character
		double defense = activeChar.getTemplate().baseMDef;
		
		// Calculate modifier for Raid Bosses
		if (activeChar.isRaid()) {
			defense *= Config.RAID_MDEFENCE_MULTIPLIER;
		}
		
		double finalDef = calcStat(Stats.MAGIC_DEFENSE, defense, target, skill);
		if (finalDef < defense * 0.5) {
			finalDef = defense * 0.5;
		}
		
		// Calculate modifiers Magic Attack
		return (int) finalDef;
	}
	
	/**
	 * Return the MEN of the L2Character (base+modifier).
	 */
	public final int getMEN() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_MEN, activeChar.getTemplate().baseMEN, null, null);
	}
	
	public float getMovementSpeedMultiplier() {
		if (activeChar == null) {
			return 1;
		}
		
		float speed = getRunSpeed();
		
		return speed / activeChar.getTemplate().baseRunSpd;
	}
	
	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the
	 * L2Character in function of the movement type.
	 */
	public float getMoveSpeed() {
		if (activeChar == null) {
			return 1;
		}
		
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
				((L2PcInstance) activeChar).getEvent().isType(EventType.StalkedSalkers)) {
			return 150;
		}
		
		float moveSpeed = getWalkSpeed();
		if (activeChar.isRunning()) {
			moveSpeed = getRunSpeed();
		}
		
		return moveSpeed;
	}
	
	/**
	 * Return the MReuse rate (base+modifier) of the L2Character.
	 */
	public final double getMReuseRate(L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		
		return calcStat(Stats.MAGIC_REUSE_RATE, activeChar.getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PReuse rate (base+modifier) of the L2Character.
	 */
	public final double getPReuseRate(L2Skill skill) {
		if (activeChar == null) {
			return 1;
		}
		
		return calcStat(Stats.P_REUSE, activeChar.getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PAtk (base+modifier) of the L2Character.
	 */
	public int getPAtk(L2Character target) {
		if (activeChar == null) {
			return 1;
		}
		
		if (activeChar instanceof L2MonsterInstance) {
			final L2MonsterInstance monster = (L2MonsterInstance) activeChar;
			if (!monster.getTemplate().BonusFromBaseStats) {
				return (int) monster.getTemplate().basePAtk;
			}
		}
		
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion()) {
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}
		if (activeChar.isRaid()) {
			bonusAtk *= Config.RAID_PATTACK_MULTIPLIER;
		}
		return (int) calcStat(Stats.PHYS_ATTACK, activeChar.getTemplate().basePAtk * bonusAtk, target, null);
	}
	
	public double getSkillMastery() {
		if (activeChar == null) {
			return 1;
		}
		
		double val = calcStat(Stats.SKILL_MASTERY, 0, null, null);
		
		if (activeChar instanceof L2PcInstance) {
			if (((L2PcInstance) activeChar).isMageClass()) {
				val *= BaseStats.INT.calcBonus(activeChar);
			} else {
				val *= BaseStats.STR.calcBonus(activeChar);
			}
		}
		return val;
	}
	
	/**
	 * Return the PAtk Modifier against animals.
	 */
	public final double getPAtkAnimals(L2Character target) {
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against dragons.
	 */
	public final double getPAtkDragons(L2Character target) {
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against insects.
	 */
	public final double getPAtkInsects(L2Character target) {
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against monsters.
	 */
	public final double getPAtkMonsters(L2Character target) {
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against plants.
	 */
	public final double getPAtkPlants(L2Character target) {
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against giants.
	 */
	public final double getPAtkGiants(L2Character target) {
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against magic creatures
	 */
	public final double getPAtkMagicCreatures(L2Character target) {
		return calcStat(Stats.PATK_MCREATURES, 1, target, null);
	}
	
	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character in function of
	 * the Armour Expertise Penalty.
	 */
	public int getPAtkSpd() {
		if (activeChar == null) {
			return 1;
		}
		
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
				((L2PcInstance) activeChar).getEvent().isType(EventType.StalkedSalkers)) {
			return 300;
		}
		
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && activeChar.isChampion()) {
			bonusAtk *= Config.L2JMOD_CHAMPION_SPD_ATK;
		}
		
		int val = (int) Math.round(calcStat(Stats.POWER_ATTACK_SPEED, activeChar.getTemplate().basePAtkSpd * bonusAtk, null, null));
		if (val > 1200) {
			val = 1200 + (int) Math.pow(val - 1200, 0.75);
		}
		
		return val;
	}
	
	/**
	 * Return the PDef Modifier against animals.
	 */
	public final double getPDefAnimals(L2Character target) {
		return calcStat(Stats.PDEF_ANIMALS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against dragons.
	 */
	public final double getPDefDragons(L2Character target) {
		return calcStat(Stats.PDEF_DRAGONS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against insects.
	 */
	public final double getPDefInsects(L2Character target) {
		return calcStat(Stats.PDEF_INSECTS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against monsters.
	 */
	public final double getPDefMonsters(L2Character target) {
		return calcStat(Stats.PDEF_MONSTERS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against plants.
	 */
	public final double getPDefPlants(L2Character target) {
		return calcStat(Stats.PDEF_PLANTS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against giants.
	 */
	public final double getPDefGiants(L2Character target) {
		return calcStat(Stats.PDEF_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PDef Modifier against giants.
	 */
	public final double getPDefMagicCreatures(L2Character target) {
		return calcStat(Stats.PDEF_MCREATURES, 1, target, null);
	}
	
	/**
	 * Return the PDef (base+modifier) of the L2Character.
	 */
	public int getPDef(L2Character target) {
		if (activeChar == null) {
			return 1;
		}
		
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
				((L2PcInstance) activeChar).getEvent().isType(EventType.StalkedSalkers)) {
			return 100;
		}
		
		double defense = activeChar.getTemplate().basePDef;
		// Calculate modifier for Raid Bosses
		if (activeChar.isRaid()) {
			defense *= Config.RAID_PDEFENCE_MULTIPLIER;
		}
		
		double finalDef = calcStat(Stats.PHYS_DEFENSE, defense, target, null);
		if (finalDef < defense * 0.5) {
			finalDef = defense * 0.5;
		}
		
		// Calculate modifiers Magic Attack
		return (int) finalDef;
	}
	
	/**
	 * Return the Physical Attack range (base+modifier) of the L2Character.
	 */
	public final int getPhysicalAttackRange() {
		if (activeChar == null) {
			return 1;
		}
		
		if (activeChar.isTransformed()) {
			return activeChar.getTemplate().baseAtkRange;
		}
		// Polearm handled here for now. Basically L2PcInstance could have a function
		// similar to FuncBowAtkRange and NPC are defined in DP.
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.POLE) {
			return (int) calcStat(Stats.POWER_ATTACK_RANGE, 66, null, null);
		}
		
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, activeChar.getTemplate().baseAtkRange, null, null);
	}
	
	/**
	 * Return the weapon reuse modifier
	 */
	public final double getWeaponReuseModifier(L2Character target) {
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * Return the RunSpeed (base+modifier) of the L2Character in function of the
	 * Armour Expertise Penalty.
	 */
	public int getRunSpeed() {
		if (activeChar == null) {
			return 1;
		}
		
		if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() &&
				((L2PcInstance) activeChar).getEvent().isType(EventType.StalkedSalkers)) {
			return 180;
		}
		
		// err we should be adding TO the persons run speed
		// not making it a constant
		double baseRunSpd = activeChar.getTemplate().baseRunSpd;
		if (baseRunSpd == 0) {
			return 0;
		}
		
		baseRunSpd += calcStat(Stats.SPD_PER_DEX, 0, null, null) * (BaseStats.DEX.calcBonus(activeChar) - 1) * baseRunSpd;
		int runSpeed = (int) Math.round(calcStat(Stats.RUN_SPEED, baseRunSpd, null, null));
		
		// Guessed formula
		if (runSpeed < 2) {
			runSpeed = 2;
		}
		
		if (runSpeed > 250) {
			runSpeed = (int) (250 + Math.pow(runSpeed - 250, 0.80));
		}
		
		return runSpeed;
	}
	
	/**
	 * Return the ShieldDef rate (base+modifier) of the L2Character.
	 */
	public final int getShldDef() {
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	public long getSp() {
		return sp;
	}
	
	public void setSp(long value) {
		sp = value;
	}
	
	public final int getLUC() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_LUC, activeChar.getTemplate().baseLUC, null, null);
	}
	
	public final int getCHA() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_CHA, activeChar.getTemplate().baseCHA, null, null);
	}
	
	/**
	 * Return the STR of the L2Character (base+modifier).
	 */
	public final int getSTR() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_STR, activeChar.getTemplate().baseSTR, null, null);
	}
	
	/**
	 * Return the WalkSpeed (base+modifier) of the L2Character.
	 */
	public int getWalkSpeed() {
		if (activeChar == null) {
			return 1;
		}
		
		double baseWalkSpd = activeChar.getTemplate().baseWalkSpd;
		
		if (baseWalkSpd == 0) {
			return 0;
		}
		
		return (int) calcStat(Stats.WALK_SPEED, baseWalkSpd, null, null);
	}
	
	/**
	 * Return the WIT of the L2Character (base+modifier).
	 */
	public final int getWIT() {
		if (activeChar == null) {
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_WIT, activeChar.getTemplate().baseWIT, null, null);
	}
	
	/**
	 * Return the mpConsume.
	 */
	public final int getMpConsume(L2Skill skill) {
		if (skill == null) {
			return 1;
		}
		double mpConsume = skill.getMpConsume();
		if (skill.isMagic()) {
			mpConsume = mpConsume * 4 / 5;
		}
		if (skill.isDance()) {
			if (Config.DANCE_CONSUME_ADDITIONAL_MP && activeChar != null && activeChar.getDanceCount() > 0) {
				mpConsume += activeChar.getDanceCount() * skill.getNextDanceMpCost();
			}
		}
		
		mpConsume = calcStat(Stats.MP_CONSUME, mpConsume, null, skill);
		
		if (skill.isDance()) {
			return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null);
		} else if (skill.isMagic()) {
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
		} else {
			return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
		}
	}
	
	/**
	 * Return the mpInitialConsume.
	 */
	public final int getMpInitialConsume(L2Skill skill) {
		if (skill == null) {
			return 1;
		}
		
		double mpConsume = 0;
		if (skill.isMagic()) {
			mpConsume = calcStat(Stats.MP_CONSUME, skill.getMpConsume() / 5, null, skill);
		}
		
		if (skill.isDance()) {
			return (int) calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null);
		} else if (skill.isMagic()) {
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
		} else {
			return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
		}
	}
	
	public byte getAttackElement() {
		L2ItemInstance weaponInstance = activeChar.getActiveWeaponInstance();
		// 1st order - weapon element
		if (weaponInstance != null && weaponInstance.getAttackElementType() >= 0) {
			return weaponInstance.getAttackElementType();
		}
		
		// temp fix starts
		int tempVal = 0, stats[] = {0, 0, 0, 0, 0, 0};
		
		byte returnVal = -2;
		stats[0] = (int) calcStat(Stats.FIRE_POWER, activeChar.getTemplate().baseFire, null, null);
		stats[1] = (int) calcStat(Stats.WATER_POWER, activeChar.getTemplate().baseWater, null, null);
		stats[2] = (int) calcStat(Stats.WIND_POWER, activeChar.getTemplate().baseWind, null, null);
		stats[3] = (int) calcStat(Stats.EARTH_POWER, activeChar.getTemplate().baseEarth, null, null);
		stats[4] = (int) calcStat(Stats.HOLY_POWER, activeChar.getTemplate().baseHoly, null, null);
		stats[5] = (int) calcStat(Stats.DARK_POWER, activeChar.getTemplate().baseDark, null, null);
		
		for (byte x = 0; x < 6; x++) {
			if (stats[x] > tempVal) {
				returnVal = x;
				tempVal = stats[x];
			}
		}
		
		return returnVal;
		// temp fix ends
		
		/*
		 * uncomment me once deadlocks in getAllEffects() fixed
			return activeChar.getElementIdFromEffects();
		 */
	}
	
	public int getAttackElementValue(byte attackAttribute) {
		switch (attackAttribute) {
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_POWER, activeChar.getTemplate().baseFire, null, null);
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_POWER, activeChar.getTemplate().baseWater, null, null);
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_POWER, activeChar.getTemplate().baseWind, null, null);
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_POWER, activeChar.getTemplate().baseEarth, null, null);
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_POWER, activeChar.getTemplate().baseHoly, null, null);
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_POWER, activeChar.getTemplate().baseDark, null, null);
			default:
				return 0;
		}
	}
	
	public byte getDefenseElement() {
		// temp fix starts
		int tempVal = 0, stats[] = {0, 0, 0, 0, 0, 0};
		
		byte returnVal = -2;
		stats[0] = (int) calcStat(Stats.FIRE_RES, activeChar.getTemplate().baseFire, null, null);
		stats[1] = (int) calcStat(Stats.WATER_RES, activeChar.getTemplate().baseWater, null, null);
		stats[2] = (int) calcStat(Stats.WIND_RES, activeChar.getTemplate().baseWind, null, null);
		stats[3] = (int) calcStat(Stats.EARTH_RES, activeChar.getTemplate().baseEarth, null, null);
		stats[4] = (int) calcStat(Stats.HOLY_RES, activeChar.getTemplate().baseHoly, null, null);
		stats[5] = (int) calcStat(Stats.DARK_RES, activeChar.getTemplate().baseDark, null, null);
		
		for (byte x = 0; x < 6; x++) {
			if (stats[x] > tempVal) {
				returnVal = x;
				tempVal = stats[x];
			}
		}
		
		return returnVal;
		// temp fix ends
		
		/*
		 * uncomment me once deadlocks in getAllEffects() fixed
			return activeChar.getElementIdFromEffects();
		 */
	}
	
	public int getDefenseElementValue(byte defenseAttribute) {
		switch (defenseAttribute) {
			case Elementals.FIRE:
				return (int) calcStat(Stats.FIRE_RES, activeChar.getTemplate().baseFireRes, null, null);
			case Elementals.WATER:
				return (int) calcStat(Stats.WATER_RES, activeChar.getTemplate().baseWaterRes, null, null);
			case Elementals.WIND:
				return (int) calcStat(Stats.WIND_RES, activeChar.getTemplate().baseWindRes, null, null);
			case Elementals.EARTH:
				return (int) calcStat(Stats.EARTH_RES, activeChar.getTemplate().baseEarthRes, null, null);
			case Elementals.HOLY:
				return (int) calcStat(Stats.HOLY_RES, activeChar.getTemplate().baseHolyRes, null, null);
			case Elementals.DARK:
				return (int) calcStat(Stats.DARK_RES, activeChar.getTemplate().baseDarkRes, null, null);
			default:
				return 0;
		}
	}
	
	public double getPvPPhysicalDamage(L2Character target) {
		return calcStat(Stats.PVP_PHYSICAL_DMG, 1, target, null);
	}
	
	public double getPvPPhysicalSkillDamage(L2Character target) {
		return calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, target, null);
	}
	
	public double getPvPPhysicalDefense(L2Character attacker) {
		return calcStat(Stats.PVP_PHYSICAL_DEF, 1, attacker, null);
	}
	
	public double getPvPPhysicalSkillDefense(L2Character attacker) {
		return calcStat(Stats.PVP_PHYS_SKILL_DEF, 1, attacker, null);
	}
	
	public double getPvPMagicDamage(L2Character target) {
		return calcStat(Stats.PVP_MAGICAL_DMG, 1, target, null);
	}
	
	public double getPvPMagicDefense(L2Character attacker) {
		return calcStat(Stats.PVP_MAGICAL_DEF, 1, attacker, null);
	}
	
	public double getPvEPhysicalSkillDamage(L2Character target) {
		return calcStat(Stats.PVE_PHYS_SKILL_DMG, 1, target, null);
	}
	
	public double getPvEPhysicalSkillDefense(L2Character attacker) {
		return calcStat(Stats.PVE_PHYS_SKILL_DEF, 1, attacker, null);
	}
	
	public double getPvEPhysicalDamage(L2Character target) {
		return calcStat(Stats.PVE_PHYSICAL_DMG, 1, target, null);
	}
	
	public double getPvEPhysicalDefense(L2Character attacker) {
		return calcStat(Stats.PVE_PHYSICAL_DEF, 1, attacker, null);
	}
	
	public double getPvEMagicDamage(L2Character target) {
		return calcStat(Stats.PVE_MAGICAL_DMG, 1, target, null);
	}
	
	public double getPvEMagicDefense(L2Character attacker) {
		return calcStat(Stats.PVE_MAGICAL_DEF, 1, attacker, null);
	}
	
	public double getPCriticalDamage(L2Character target, double damage, L2Skill skill) {
		double var = calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
		
		if (var > 150) {
			var = (int) (150 + Math.pow(var - 150, 0.80));
		}
		return var;
	}
}
