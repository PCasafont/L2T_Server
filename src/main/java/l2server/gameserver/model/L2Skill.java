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

package l2server.gameserver.model;

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.datatables.*;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager;
import l2server.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.actor.instance.*;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.conditions.Condition;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Point3D;

import java.util.*;

/**
 * This class...
 *
 * @version $Revision: 1.3.2.8.2.22 $ $Date: 2005/04/06 16:13:42 $
 */
public abstract class L2Skill implements IChanceSkillTrigger {
	private static final L2Object[] emptyTargetList = new L2Object[0];
	
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	public static final int SKILL_DIVINE_EXPANSION = 10956;
	public static final int SKILL_CLAN_LUCK = 390;
	
	public static final boolean geoEnabled = Config.GEODATA > 0;
	
	public enum SkillOpType {
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE
	}

	/*public static enum TargetObjectType
	{
		NONE,
		ANY,
		SELF,
		PARTY,
		CLAN,
		ALLY,
		CORPSE,
		CORPSE_MOB,
		PVP,
		SUMMON,
		ENEMY_SUMMON,
		UNLOCKABLE,
		HOLY,
		FLAGPOLE,
		GROUND,
		EVENT,
		MENTEE;
	}*/
	
	//conditional values
	public static final int COND_RUNNING = 0x0001;
	public static final int COND_WALKING = 0x0002;
	public static final int COND_SIT = 0x0004;
	public static final int COND_BEHIND = 0x0008;
	public static final int COND_CRIT = 0x0010;
	public static final int COND_LOWHP = 0x0020;
	public static final int COND_ROBES = 0x0040;
	public static final int COND_CHARGES = 0x0080;
	public static final int COND_SHIELD = 0x0100;
	public static final int COND_FRONT = 0x0200;
	
	private static final Func[] emptyFunctionSet = new Func[0];
	private static final L2Abnormal[] emptyEffectSet = new L2Abnormal[0];
	
	// these two build the primary key
	private final int id;
	private final int level;
	private final int enchantRouteId;
	private final int enchantLevel;
	
	/**
	 * Identifier for a skill that client can't display
	 */
	private int displayId;
	
	// not needed, just for easier debug
	private final String name;
	private final SkillOpType operateType;
	private final boolean magic;
	private final boolean staticReuse;
	private final boolean staticHitTime;
	private final int mpConsume;
	private final int hpConsume;
	private final int cpConsume;
	
	private final int targetConsume;
	private final int targetConsumeId;
	
	private final int itemConsume;
	private final int itemConsumeId;
	
	private final int fameConsume;
	private final int clanRepConsume;
	
	private final int castRange;
	private final int effectRange;
	
	// Abnormal levels for skills and their canceling, e.g. poison vs negate
	private final int abnormalLvl; // e.g. poison or bleed lvl 2
	// Note: see also effectAbnormalLvl
	private final int[] negateId; // cancels the effect of skill ID
	private final L2AbnormalType[] negateStats; // lists the effect types that are canceled
	private final Map<String, Byte> negateAbnormals;
	// lists the effect abnormal types with order below the presented that are canceled
	private final int minNegatedEffects; // minimum number of effects to negate
	private final int maxNegatedEffects; // maximum number of effects to negate
	
	private final boolean stayAfterDeath; // skill should stay after death
	
	// kill by damage over time
	private final boolean killByDOT;
	// absorb the damage over time
	private final boolean absorbDOT;
	
	private final int refId;
	// all times in milliseconds
	private final int hitTime;
	private final int[] hitTimings;
	//private final int skillInterruptTime;
	private final int coolTime;
	private final int reuseHashCode;
	private final int reuseDelay;
	private final int buffDuration;
	// for item skills delay on equip
	private final int equipDelay;
	
	/**
	 * Target type of the skill : SELF, PARTY, CLAN, PET...
	 */
	private final L2SkillTargetType targetType;
	private final L2SkillTargetDirection targetDirection;
	private final L2SkillBehaviorType behaviorType;
	
	private final int feed;
	// base success chance
	private final double power;
	private final double pvpPower;
	private final double pvePower; //FIXME: remove?
	private final double stunPower;
	private final int magicLevel;
	private final int levelDepend;
	private final boolean ignoreResists;
	private final boolean ignoreImmunity;
	private final int minChance;
	private final int maxChance;
	private final int blowChance;
	
	private final boolean isNeutral;
	// Effecting area of the skill, in radius.
	// The radius center varies according to the targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private int skillRadius;
	private final int skillSafeRadius;
	
	private final L2SkillType skillType;
	private final int effectAbnormalLvl; // abnormal level for the additional effect type, e.g. poison lvl 1
	private final int effectId;
	private final int effectLvl; // normal effect level
	
	private final boolean nextActionIsAttack;
	private final boolean nextActionIsAttackMob;
	
	private final boolean removedOnAction;
	private final boolean removedOnDamage;
	private final int removedOnDamageChance;
	private final int strikesToRemove;
	private final int damageToRemove;
	private final boolean removedOnDebuffBlock;
	private final int debuffBlocksToRemove;
	
	private final boolean isPotion;
	private final byte element;
	private final int elementPower;
	
	private final BaseStats saveVs;
	
	private final int condition;
	private final int conditionValue;
	private final boolean overhit;
	private final int weaponsAllowed;
	private final int armorsAllowed;
	
	private final int minPledgeClass;
	private final boolean isOffensive;
	private final int maxCharges;
	private final int numCharges;
	private final int maxChargeConsume;
	private final int triggeredId;
	private final int triggeredLevel;
	private final int triggeredEnchantRoute;
	private final int triggeredEnchantLevel;
	private final String chanceType;
	private final int soulMaxConsume;
	private final int soulConsume;
	private final int numSouls;
	private final int expNeeded;
	private final int critChance;
	private final float dependOnTargetBuff;
	private final int[] dependOnTargetEffectId;
	private final double[] damageDepend;
	
	private final int transformId;
	private final int transformDuration;
	
	private final int afterEffectId;
	private final int afterEffectLvl;
	private final boolean isHeroSkill; // If true the skill is a Hero Skill
	private final boolean isGMSkill; // True if skill is GM skill
	
	private final float baseCritRate;
	// percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int lethalEffect1;
	// percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int lethalEffect2;
	// percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean directHpDmg; // If true then dmg is being make directly
	private final boolean isDance; // If true then casting more dances will cost more MP
	private final int nextDanceCost;
	private final int aggroPoints;
	private final float ignoredDefPercent;
	private final boolean canBeUsedWhenDisabled;
	
	protected List<Condition> preCondition;
	protected List<Condition> itemPreCondition;
	protected FuncTemplate[] funcTemplates;
	protected L2AbnormalTemplate[] effectTemplates;
	protected L2AbnormalTemplate[] effectTemplatesSelf;
	
	protected ChanceCondition chanceCondition = null;
	
	// Flying support
	private final String flyType;
	private final int flyRadius;
	private final float flyCourse;
	
	private final boolean isDebuff;
	
	private final String attribute;
	
	private final boolean ignoreShield;
	private final boolean isSuicideAttack;
	private final boolean canBeReflected;
	private final boolean canBeSharedWithSummon;
	
	private final boolean canBeDispeled;
	
	private final boolean isClanSkill;
	private final boolean excludedFromCheck;
	private final boolean simultaneousCast;
	
	private L2ExtractableSkill extractableItems = null;
	
	private boolean isTriggered = false;
	
	private int partyChangeSkill = -1;
	private int partyChangeSkillLevel = 1;
	private int partyChangeSkillEnchantRoute = 0;
	private int partyChangeSkillEnchantLevel = 0;
	private boolean isCastedToParty = false;
	private final int skillActionId;
	private final int alterSkillId;
	private final int alterSkillLevel;
	private final int alterIconTime;
	private final boolean isElemental;
	private final boolean isStanceSwitch;
	
	protected L2Skill(StatsSet set) {
		id = set.getInteger("skill_id");
		level = set.getInteger("level");
		enchantRouteId = set.getInteger("enchantRouteId", 0);
		enchantLevel = set.getInteger("enchantLevel", 0);
		refId = set.getInteger("referenceId", 0);
		displayId = set.getInteger("displayId", id);
		name = set.getString("name");
		operateType = set.getEnum("operateType", SkillOpType.class);
		magic = set.getBool("isMagic", false);
		staticReuse = set.getBool("staticReuse", false);
		staticHitTime = set.getBool("staticHitTime", false);
		isPotion = set.getBool("isPotion", false);
		mpConsume = set.getInteger("mpConsume", 0);
		hpConsume = set.getInteger("hpConsume", 0);
		cpConsume = set.getInteger("cpConsume", 0);
		targetConsume = set.getInteger("targetConsumeCount", 0);
		targetConsumeId = set.getInteger("targetConsumeId", 0);
		itemConsume = set.getInteger("itemConsumeCount", 0);
		itemConsumeId = set.getInteger("itemConsumeId", 0);
		fameConsume = set.getInteger("fameConsume", 0);
		clanRepConsume = set.getInteger("clanRepConsume", 0);
		afterEffectId = set.getInteger("afterEffectId", 0);
		afterEffectLvl = set.getInteger("afterEffectLvl", 1);
		castRange = (int) set.getFloat("castRange", -1);
		effectRange = (int) set.getFloat("effectRange", -1);
		abnormalLvl = set.getInteger("abnormalLvl", -1);
		effectAbnormalLvl =
				set.getInteger("effectAbnormalLvl", -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
		attribute = set.getString("attribute", "");
		String str = set.getString("negateStats", "");
		
		if (Objects.equals(str, "")) {
			negateStats = new L2AbnormalType[0];
		} else {
			String[] stats = str.split(" ");
			L2AbnormalType[] array = new L2AbnormalType[stats.length];
			
			for (int i = 0; i < stats.length; i++) {
				L2AbnormalType type = null;
				try {
					type = Enum.valueOf(L2AbnormalType.class, stats[i]);
				} catch (Exception e) {
					throw new IllegalArgumentException("SkillId: " + id + " Enum value of type L2AbnormalType required, but found: " + stats[i]);
				}
				
				array[i] = type;
			}
			negateStats = array;
		}
		
		String negateAbnormals = set.getString("negateAbnormals", null);
		if (negateAbnormals != null && !Objects.equals(negateAbnormals, "")) {
			this.negateAbnormals = new HashMap<>();
			for (String ngtStack : negateAbnormals.split(";")) {
				String[] ngt = ngtStack.split(",");
				if (ngt.length == 1) // Only abnormalType is present, without abnormalLvl
				{
					this.negateAbnormals.put(ngt[0], Byte.MAX_VALUE);
				} else if (ngt.length == 2) // Both abnormalType and abnormalLvl are present
				{
					try {
						this.negateAbnormals.put(ngt[0], Byte.parseByte(ngt[1]));
					} catch (Exception e) {
						throw new IllegalArgumentException("SkillId: " + id + " Byte value required, but found: " + ngt[1]);
					}
				} else
				// If not both from above, then smth is messed up... throw an error.
				{
					throw new IllegalArgumentException("SkillId: " + id + ": Incorrect negate Abnormals for " + ngtStack +
							". Lvl: abnormalType1,abnormalLvl1;abnormalType2,abnormalLvl2;abnormalType3,abnormalLvl3... or abnormalType1;abnormalType2;abnormalType3...");
				}
			}
		} else {
			this.negateAbnormals = null;
		}
		
		String negateId = set.getString("negateId", null);
		if (negateId != null) {
			String[] valuesSplit = negateId.split(",");
			this.negateId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++) {
				this.negateId[i] = Integer.parseInt(valuesSplit[i]);
			}
		} else {
			this.negateId = new int[0];
		}
		minNegatedEffects = set.getInteger("minNegated", 0);
		maxNegatedEffects = set.getInteger("maxNegated", 0);
		
		stayAfterDeath = set.getBool("stayAfterDeath", false);
		
		killByDOT = set.getBool("killByDOT", false);
		absorbDOT = set.getBool("absorbDOT", false);
		isNeutral = set.getBool("neutral", false);
		hitTime = set.getInteger("hitTime", 0);
		String hitTimings = set.getString("hitTimings", null);
		if (hitTimings != null) {
			try {
				String[] valuesSplit = hitTimings.split(",");
				this.hitTimings = new int[valuesSplit.length];
				for (int i = 0; i < valuesSplit.length; i++) {
					this.hitTimings[i] = Integer.parseInt(valuesSplit[i]);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"SkillId: " + id + " invalid hitTimings value: " + hitTimings + ", \"percent,percent,...percent\" required");
			}
		} else {
			this.hitTimings = new int[0];
		}
		
		coolTime = set.getInteger("coolTime", 0);
		feed = set.getInteger("feed", 0);
		
		String reuseHash = set.getString("sharedReuse", null);
		if (reuseHash != null) {
			try {
				String[] valuesSplit = reuseHash.split("-");
				if (valuesSplit.length > 1) {
					reuseHashCode = Integer.parseInt(valuesSplit[0]) * 1000 + Integer.parseInt(valuesSplit[1]);
				} else {
					reuseHashCode = Integer.parseInt(valuesSplit[0]) * 1000 + level;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("SkillId: " + id + " invalid sharedReuse value: " + reuseHash + ", \"skillId-skillLvl\" required");
			}
		} else {
			reuseHashCode = id * 1000 + level;
		}
		
		if (Config.ENABLE_MODIFY_SKILL_REUSE && Config.SKILL_REUSE_LIST.containsKey(id)) {
			if (Config.DEBUG) {
				Log.info("*** Skill " + name + " (" + level + ") changed reuse from " + set.getInteger("reuseDelay", 0) + " to " +
						Config.SKILL_REUSE_LIST.get(id) + " seconds.");
			}
			reuseDelay = Config.SKILL_REUSE_LIST.get(id);
		} else {
			reuseDelay = set.getInteger("reuseDelay", 0);
		}
		
		buffDuration = set.getInteger("buffDuration", 0);
		
		equipDelay = set.getInteger("equipDelay", 0);
		
		skillRadius = set.getInteger("skillRadius", 80);
		
		skillSafeRadius = set.getInteger("skillSafeRadius", 0);
		
		targetType = set.getEnum("target", L2SkillTargetType.class);
		targetDirection = set.getEnum("targetDirection", L2SkillTargetDirection.class, L2SkillTargetDirection.DEFAULT);
		behaviorType = set.getEnum("behaviorType", L2SkillBehaviorType.class, L2SkillBehaviorType.UNKNOWN);
		if (skillRadius == 80 && targetType == L2SkillTargetType.TARGET_FRIENDS) {
			skillRadius = 900;
		}
		
		power = set.getFloat("power", 0.f);
		pvpPower = set.getFloat("pvpPower", (float) getPower());
		pvePower = set.getFloat("pvePower", (float) getPower());
		stunPower = set.getFloat("stunPower", (float) getPower());
		magicLevel = set.getInteger("magicLvl", PlayerClassTable.getInstance().getMinSkillLevel(id, level));
		levelDepend = set.getInteger("lvlDepend", 0);
		ignoreResists = set.getBool("ignoreResists", false);
		ignoreImmunity = set.getBool("ignoreImmunity", false);
		minChance = set.getInteger("minChance", 10);
		maxChance = set.getInteger("maxChance", 90);
		ignoreShield = set.getBool("ignoreShld", false);
		skillType = set.getEnum("skillType", L2SkillType.class);
		effectId = set.getInteger("effectId", 0);
		effectLvl = set.getInteger("effectLevel", 0);
		
		nextActionIsAttack = set.getBool("nextActionAttack", false);
		nextActionIsAttackMob = set.getBool("nextActionAttackMob", false);
		
		removedOnAction = set.getBool("removedOnAction", false);
		removedOnDamage = set.getBool("removedOnDamage", false);
		removedOnDamageChance = set.getInteger("removedOnDamageChance", removedOnDamage ? 100 : 0);
		strikesToRemove = set.getInteger("strikesToRemove", 0);
		damageToRemove = set.getInteger("damageToRemove", 0);
		removedOnDebuffBlock = set.getBool("removedOnDebuffBlock", false);
		debuffBlocksToRemove = set.getInteger("debuffBlocksToRemove", 0);
		
		element = set.getByte("element", (byte) -1);
		elementPower = set.getInteger("elementPower", 0);
		
		saveVs = set.getEnum("saveVs", BaseStats.class, null);
		
		condition = set.getInteger("condition", 0);
		conditionValue = set.getInteger("conditionValue", 0);
		overhit = set.getBool("overHit", false);
		isSuicideAttack = set.getBool("isSuicideAttack", false);
		
		String weaponsAllowedString = set.getString("weaponsAllowed", null);
		if (weaponsAllowedString != null && !weaponsAllowedString.trim().isEmpty()) {
			int mask = 0;
			StringTokenizer st = new StringTokenizer(weaponsAllowedString, ",");
			while (st.hasMoreTokens()) {
				int old = mask;
				String item = st.nextToken().trim();
				if (ItemTable.weaponTypes.containsKey(item)) {
					mask |= ItemTable.weaponTypes.get(item).mask();
				}
				
				if (ItemTable.armorTypes.containsKey(item)) // for shield
				{
					mask |= ItemTable.armorTypes.get(item).mask();
				}
				
				if (item.equals("crossbow")) {
					mask |= L2WeaponType.CROSSBOWK.mask();
				}
				
				if (old == mask) {
					Log.info("[weaponsAllowed] Unknown item type name: " + item);
				}
			}
			weaponsAllowed = mask;
		} else {
			weaponsAllowed = 0;
		}
		
		armorsAllowed = set.getInteger("armorsAllowed", 0);
		
		minPledgeClass = set.getInteger("minPledgeClass", 0);
		isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		isDebuff = set.getBool("isDebuff", isSkillTypeOffensive());
		//isDebuff = set.getBool("isDebuff", isSkillTypeDebuff());
		maxCharges = set.getInteger("maxCharges", 0);
		numCharges = set.getInteger("numCharges", 0);
		maxChargeConsume = set.getInteger("maxChargeConsume", 0);
		triggeredId = set.getInteger("triggeredId", -1);
		triggeredLevel = set.getInteger("triggeredLevel", 0);
		triggeredEnchantRoute = set.getInteger("triggeredEnchantRoute", 0);
		triggeredEnchantLevel = set.getInteger("triggeredEnchantLevel", 0);
		chanceType = set.getString("chanceType", "");
		if (!Objects.equals(chanceType, "") && !chanceType.isEmpty()) {
			chanceCondition = ChanceCondition.parse(set);
		}
		
		numSouls = set.getInteger("num_souls", 0);
		soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
		soulConsume = set.getInteger("soulConsumeCount", 0);
		blowChance = set.getInteger("blowChance", 0);
		expNeeded = set.getInteger("expNeeded", 0);
		critChance = set.getInteger("critChance", 0);
		
		transformId = set.getInteger("transformId", 0);
		transformDuration = set.getInteger("transformDuration", 0);
		
		isHeroSkill = HeroSkillTable.isHeroSkill(id);
		isGMSkill = GMSkillTable.isGMSkill(id);
		
		baseCritRate = set.getFloat("baseCritRate", 0);
		lethalEffect1 = set.getInteger("lethal1", 0);
		lethalEffect2 = set.getInteger("lethal2", 0);
		
		directHpDmg = set.getBool("dmgDirectlyToHp", false);
		isDance = set.getBool("isDance", false);
		nextDanceCost = set.getInteger("nextDanceCost", 0);
		aggroPoints = Math.round(set.getFloat("aggroPoints", 0));
		ignoredDefPercent = set.getFloat("ignoredDefPercent", 0.0f);
		canBeUsedWhenDisabled = set.getBool("canBeUsedWhenDisabled", false);
		
		flyType = set.getString("flyType", null);
		flyRadius = set.getInteger("flyRadius", 0);
		flyCourse = set.getFloat("flyCourse", 0);
		canBeReflected = set.getBool("canBeReflected", true);
		canBeSharedWithSummon = set.getBool("canBeSharedWithSummon", true);
		canBeDispeled = set.getBool("canBeDispeled", true);
		
		isClanSkill = set.getBool("isClanSkill", false);
		excludedFromCheck = set.getBool("excludedFromCheck", false);
		dependOnTargetBuff = set.getFloat("dependOnTargetBuff", 0);
		
		String dependOnTargetEffectId = set.getString("dependOnTargetEffectId", null);
		if (dependOnTargetEffectId != null) {
			String[] valuesSplit = dependOnTargetEffectId.split(",");
			this.dependOnTargetEffectId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++) {
				this.dependOnTargetEffectId[i] = Integer.parseInt(valuesSplit[i]);
			}
		} else {
			this.dependOnTargetEffectId = new int[0];
		}
		
		String damageDepend = set.getString("damageDepend", null);
		if (damageDepend != null) {
			String[] valuesSplit = damageDepend.split(",");
			this.damageDepend = new double[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length; i++) {
				this.damageDepend[i] = Double.parseDouble(valuesSplit[i]);
			}
		} else {
			this.damageDepend = new double[0];
		}
		
		simultaneousCast = set.getBool("simultaneousCast", false);
		
		String capsuled_items = set.getString("capsuled_items_skill", null);
		if (capsuled_items != null) {
			if (capsuled_items.isEmpty()) {
				Log.warning("Empty Extractable Item Skill data in Skill Id: " + id);
			}
			
			extractableItems = parseExtractableSkill(id, level, capsuled_items);
		}
		
		partyChangeSkill = set.getInteger("partyChangeSkill", -1);
		partyChangeSkillLevel = set.getInteger("partyChangeSkillLevel", 1);
		partyChangeSkillEnchantRoute = set.getInteger("partyChangeSkillEnchantRoute", 0);
		partyChangeSkillEnchantLevel = set.getInteger("partyChangeSkillEnchantLevel", 0);
		isCastedToParty = set.getBool("isCastedToParty", true);
		skillActionId = set.getInteger("skillActionId", 0);
		alterSkillId = set.getInteger("alterSkillId", -1);
		alterSkillLevel = set.getInteger("alterSkillLevel", -1);
		alterIconTime = set.getInteger("alterIconTime", -1);
		
		isElemental = set.getBool("isElemental", false);
		isStanceSwitch = set.getBool("isStanceSwitch", false);
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final boolean isPotion() {
		return isPotion;
	}
	
	public final int getArmorsAllowed() {
		return armorsAllowed;
	}
	
	public final int getConditionValue() {
		return conditionValue;
	}
	
	public final L2SkillType getSkillType() {
		return skillType;
	}
	
	public final byte getElement() {
		return element;
	}
	
	public final int getElementPower() {
		return elementPower;
	}
	
	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR><BR>
	 */
	public final L2SkillTargetType getTargetType() {
		return targetType;
	}
	
	public final int getCondition() {
		return condition;
	}
	
	public final boolean isOverhit() {
		return overhit;
	}
	
	public final boolean killByDOT() {
		return killByDOT;
	}
	
	public final boolean absorbDOT() {
		return absorbDOT;
	}
	
	public final boolean isSuicideAttack() {
		return isSuicideAttack;
	}
	
	public final boolean allowOnTransform() {
		return isPassive();
	}
	
	/**
	 * Return the power of the skill.<BR><BR>
	 */
	public final double getPower(L2Character activeChar, L2Character target, boolean isPvP, boolean isPvE) {
		if (activeChar == null) {
			return getPower(isPvP, isPvE);
		}
		
		double power = getPower(isPvP, isPvE);
		if (target != null && target.isStunned()) {
			power = stunPower;
		}
		
		switch (skillType) {
			case DEATHLINK: {
				return power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
                /*
				 * DrHouse:
				 * Rolling back to old formula (look below) for DEATHLINK due to this one based on logarithm is not
				 * accurate enough. Commented here because probably is a matter of just adjusting a constant
				if (activeChar.getCurrentHp() / activeChar.getMaxHp() > 0.005)
					return power*(-0.45*Math.log(activeChar.getCurrentHp()/activeChar.getMaxHp())+1.);
				else
					return power*(-0.45*Math.log(0.005)+1.);
				 */
			}
			case FATAL: {
				return power * 3.5 * (1 - target.getCurrentHp() / target.getMaxHp());
			}
			default:
				return getPower(isPvP, isPvE);
		}
	}
	
	public final double getPower() {
		return power;
	}
	
	public final double getPower(boolean isPvP, boolean isPvE) {
		return isPvP ? pvpPower : isPvE ? pvePower : power;
	}
	
	public final L2AbnormalType[] getNegateStats() {
		return negateStats;
	}
	
	public final Map<String, Byte> getNegateAbnormals() {
		return negateAbnormals;
	}
	
	public final int getAbnormalLvl() {
		return abnormalLvl;
	}
	
	public final int[] getNegateId() {
		return negateId;
	}
	
	public final int getMagicLevel() {
		if (magicLevel == 0) {
			int skillMaxLevel = SkillTable.getInstance().getMaxLevel(getId());
			return PlayerClassTable.getInstance().getMinSkillLevel(id, skillMaxLevel);
		}
		
		return magicLevel;
	}
	
	public final int getMinNegatedEffects() {
		return minNegatedEffects;
	}
	
	public final int getMaxNegatedEffects() {
		return maxNegatedEffects;
	}
	
	public final int getLevelDepend() {
		return levelDepend;
	}
	
	/**
	 * Return true if skill should ignore all resistances
	 */
	public final boolean ignoreResists() {
		return ignoreResists;
	}
	
	/**
	 * Return true if skill should ignore immunity
	 */
	public final boolean ignoreImmunity() {
		return ignoreImmunity;
	}
	
	/**
	 * Return minimum skill/effect land rate (default is 1).
	 */
	public final int getMinChance() {
		return minChance;
	}
	
	/**
	 * Return maximum skill/effect land rate (default is 99).
	 */
	public final int getMaxChance() {
		return maxChance;
	}
	
	/**
	 * Return true if skill effects should be removed on any action except movement
	 */
	public final boolean isRemovedOnAction() {
		return removedOnAction;
	}
	
	/**
	 * Return true if skill effects should be removed on damage
	 */
	public final boolean isRemovedOnDamage() {
		return removedOnDamage;
	}
	
	public final int getRemovedOnDamageChance() {
		return removedOnDamageChance;
	}
	
	public final int getStrikesToRemove() {
		return strikesToRemove;
	}
	
	public final int getDamageToRemove() {
		return damageToRemove;
	}
	
	/**
	 * Return true if skill effects should be removed on debuff block
	 */
	public final boolean isRemovedOnDebuffBlock() {
		return removedOnDebuffBlock;
	}
	
	public final int getDebuffBlocksToRemove() {
		return debuffBlocksToRemove;
	}
	
	/**
	 * Return the additional effect Id.<BR><BR>
	 */
	public final int getEffectId() {
		return effectId;
	}
	
	/**
	 * Return the additional effect level.<BR><BR>
	 */
	public final int getEffectLvl() {
		return effectLvl;
	}
	
	public final int getEffectAbnormalLvl() {
		return effectAbnormalLvl;
	}
	
	/**
	 * Return true if character should attack target after skill
	 */
	public final boolean nextActionIsAttack() {
		return nextActionIsAttack;
	}
	
	public final boolean nextActionIsAttackMob() {
		return nextActionIsAttackMob;
	}
	
	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration() {
		return buffDuration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange() {
		return castRange;
	}
	
	/**
	 * @return Returns the cpConsume;
	 */
	public final int getCpConsume() {
		return cpConsume;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange() {
		return effectRange;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume() {
		return hpConsume;
	}
	
	/**
	 * @return Returns the id.
	 */
	public final int getId() {
		return id;
	}
	
	/**
	 * @return Returns the boolean isDebuff.
	 */
	public final boolean isDebuff() {
		return isDebuff;
	}
	
	public int getDisplayId() {
		return displayId;
	}
	
	public void setDisplayId(int id) {
		displayId = id;
	}
	
	public int getTriggeredId() {
		return triggeredId;
	}
	
	public int getTriggeredLevel() {
		return triggeredLevel;
	}
	
	public int getTriggeredEnchantRoute() {
		return triggeredEnchantRoute;
	}
	
	public int getTriggeredEnchantLevel() {
		return triggeredEnchantLevel;
	}
	
	public boolean triggerAnotherSkill() {
		return triggeredId > 1;
	}
	
	/**
	 * Return skill saveVs base stat (STR, INT ...).<BR><BR>
	 */
	public final BaseStats getSaveVs() {
		return saveVs;
	}
	
	/**
	 * @return Returns the targetConsumeId.
	 */
	public final int getTargetConsumeId() {
		return targetConsumeId;
	}
	
	/**
	 * @return Returns the targetConsume.
	 */
	public final int getTargetConsume() {
		return targetConsume;
	}
	
	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume() {
		return itemConsume;
	}
	
	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId() {
		return itemConsumeId;
	}
	
	/**
	 * @return Returns the fameConsume.
	 */
	public final int getFameConsume() {
		return fameConsume;
	}
	
	/**
	 * @return Returns the clanRepConsume.
	 */
	public final int getClanRepConsume() {
		return clanRepConsume;
	}
	
	/**
	 * @return Returns the level.
	 */
	public final int getLevel() {
		return level;
	}
	
	public final int getEnchantRouteId() {
		return enchantRouteId;
	}
	
	public final int getEnchantLevel() {
		return enchantLevel;
	}
	
	public final int getLevelHash() {
		return level | getEnchantHash() << 16;
	}
	
	public final int getEnchantHash() {
		return enchantRouteId * 1000 + enchantLevel;
	}
	
	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic() {
		return magic;
	}
	
	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse() {
		return staticReuse;
	}
	
	/**
	 * @return Returns true to set static hittime.
	 */
	public final boolean isStaticHitTime() {
		return staticHitTime;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume() {
		return mpConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay() {
		return reuseDelay;
	}
	
	public final int getReuseHashCode() {
		return reuseHashCode;
	}
	
	public final int getEquipDelay() {
		return equipDelay;
	}
	
	public final int getHitTime() {
		return hitTime;
	}
	
	public final int getHitCounts() {
		return hitTimings.length;
	}
	
	public final int[] getHitTimings() {
		return hitTimings;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime() {
		return coolTime;
	}
	
	public final int getSkillRadius() {
		return skillRadius;
	}
	
	public final int getSkillSafeRadius() {
		return skillSafeRadius;
	}
	
	public final boolean isActive() {
		return operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive() {
		return operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle() {
		return operateType == SkillOpType.OP_TOGGLE;
	}
	
	public final boolean isChance() {
		return chanceCondition != null && isPassive();
	}
	
	public final boolean isDance() {
		return isDance;
	}
	
	public final int getNextDanceMpCost() {
		return nextDanceCost;
	}
	
	public final int getAggroPoints() {
		return aggroPoints;
	}
	
	public final float getIgnoredDefPercent() {
		return ignoredDefPercent;
	}
	
	public boolean canBeUsedWhenDisabled() {
		return canBeUsedWhenDisabled;
	}
	
	public final boolean useSoulShot() {
		switch (getSkillType()) {
			case PDAM:
			case CHARGEDAM:
			case BLOW:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean useSpiritShot() {
		switch (getSkillType()) {
			case MDAM:
			case DRAIN:
				return true;
			default:
				return isMagic();
		}
	}
	
	public final boolean useFishShot() {
		return getSkillType() == L2SkillType.PUMPING || getSkillType() == L2SkillType.REELING;
	}
	
	public final int getWeaponsAllowed() {
		return weaponsAllowed;
	}
	
	public int getMinPledgeClass() {
		return minPledgeClass;
	}
	
	public final boolean isPvpSkill() {
		switch (skillType) {
			case DEBUFF:
			case AGGDEBUFF:
			case CONTINUOUS_DEBUFF:
			case CANCEL:
			case BETRAY:
			case AGGDAMAGE:
			case STEAL_BUFF:
			case AGGREDUCE_CHAR:
			case MANADAM:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean isOffensive() {
		return isOffensive;
	}
	
	public final boolean isNeutral() {
		return isNeutral;
	}
	
	public final boolean isHeroSkill() {
		return isHeroSkill;
	}
	
	public final boolean isGMSkill() {
		return isGMSkill;
	}
	
	public final int getNumCharges() {
		return numCharges;
	}
	
	public final int getMaxChargeConsume() {
		return maxChargeConsume;
	}
	
	public final int getNumSouls() {
		return numSouls;
	}
	
	public final int getMaxSoulConsumeCount() {
		return soulMaxConsume;
	}
	
	public final int getSoulConsumeCount() {
		return soulConsume;
	}
	
	public final int getExpNeeded() {
		return expNeeded;
	}
	
	public final int getCritChance() {
		return critChance;
	}
	
	public final int getTransformId() {
		return transformId;
	}
	
	public final int getTransformDuration() {
		return transformDuration;
	}
	
	public final float getBaseCritRate() {
		return baseCritRate;
	}
	
	public final int getLethalChance1() {
		return lethalEffect1;
	}
	
	public final int getLethalChance2() {
		return lethalEffect2;
	}
	
	public final boolean getDmgDirectlyToHP() {
		return directHpDmg;
	}
	
	public final String getFlyType() {
		return flyType;
	}
	
	public final int getFlyRadius() {
		return flyRadius;
	}
	
	public final float getFlyCourse() {
		return flyCourse;
	}
	
	public final boolean isSkillTypeOffensive() {
		switch (skillType) {
			case PDAM:
			case MDAM:
			case CPDAM:
			case CPDAMPERCENT:
			case MAXHPDAMPERCENT:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case ERASE:
			case BLOW:
			case FATAL:
			case DRAIN:
			case CHARGEDAM:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case SWEEP:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case BETRAY:
			case DELUXE_KEY_UNLOCK:
			case SOW:
			case HARVEST:
			case STEAL_BUFF:
			case INSTANT_JUMP:
			case CONTINUOUS_DEBUFF:
			case CONTINUOUS_DRAIN:
			case RESET:
			case MARK:
				return true;
			case DUMMY:
				if (id == 998) // blazing boost
				{
					return true;
				}
			default:
				return isDebuff();
		}
	}
	
	public final boolean isSkillTypeDebuff() {
		switch (skillType) {
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case ERASE:
			case DRAIN:
			case CHARGEDAM:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case SPOIL:
			case SWEEP:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case BETRAY:
			case STEAL_BUFF:
			case INSTANT_JUMP:
			case CONTINUOUS_DEBUFF:
			case CONTINUOUS_DRAIN:
			case RESET:
				return true;
			case DUMMY:
				if (id == 998) // blazing boost
				{
					return true;
				}
			default:
				return isDebuff();
		}
	}
	
	public final boolean is7Signs() {
		return id > 4360 && id < 4367;
	}
	
	public final boolean isStayAfterDeath() {
		return stayAfterDeath;
	}
	
	public final boolean getWeaponDependancy(L2Character activeChar) {
		if (getWeaponDependancy(activeChar, false)) {
			return true;
		} else {
			SystemMessage message = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			message.addSkillName(this);
			activeChar.sendPacket(message);
			
			return false;
		}
	}
	
	public final boolean getWeaponDependancy(L2Character activeChar, boolean chance) {
		int weaponsAllowed = getWeaponsAllowed();
		//check to see if skill has a weapon dependency.
		if (weaponsAllowed == 0) {
			return true;
		}
		
		int mask = 0;
		
		if (activeChar instanceof L2MonsterInstance && ((L2MonsterInstance) activeChar).getClonedPlayer() != null) {
			return true;
		}
		
		if (activeChar.getActiveWeaponItem() != null) {
			mask |= activeChar.getActiveWeaponItem().getItemType().mask();
		}
		if (activeChar.getSecondaryWeaponItem() != null && activeChar.getSecondaryWeaponItem() instanceof L2Armor) {
			mask |= activeChar.getSecondaryWeaponItem().getItemType().mask();
		}
		
		return (mask & weaponsAllowed) != 0;
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon) {
		if (activeChar.isGM() && !Config.GM_SKILL_RESTRICTION) {
			return true;
		}
		if ((getCondition() & L2Skill.COND_SHIELD) != 0) {
			/*
			 L2Armor armorPiece;
			 L2ItemInstance dummy;
			 dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			 armorPiece = (L2Armor) dummy.getItem();
			 */
			//TODO add checks for shield here.
		}
		
		List<Condition> preCondition = this.preCondition;
		if (itemOrWeapon) {
			preCondition = itemPreCondition;
		}
		if (preCondition == null || preCondition.isEmpty()) {
			return true;
		}
		
		for (Condition cond : preCondition) {
			Env env = new Env();
			env.player = activeChar;
			if (target instanceof L2Character) // TODO: object or char?
			{
				env.target = (L2Character) target;
			}
			env.skill = this;
			
			if (!cond.test(env)) {
				String msg = cond.getMessage();
				int msgId = cond.getMessageId();
				if (msgId != 0) {
					SystemMessage sm = SystemMessage.getSystemMessage(msgId);
					if (cond.isAddName()) {
						sm.addSkillName(id);
					}
					activeChar.sendPacket(sm);
				} else if (msg != null) {
					activeChar.sendMessage(msg);
				}
				return false;
			}
		}
		return true;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst) {
		// Init to null the target of the skill
		L2Character target = null;
		
		// Get the L2Objcet targeted by the user of the skill at this moment
		L2Object objTarget = activeChar.getTarget();
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character) {
			target = (L2Character) objTarget;
		}
		
		return getTargetList(activeChar, onlyFirst, target);
	}
	
	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR><BR>
	 * <p>
	 * <B><U> Values of skill type</U> :</B><BR><BR>
	 * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no L2PcInstance targeted</li>
	 * <li>SELF</li>
	 * <li>HOLY, UNDEAD</li>
	 * <li>PET</li>
	 * <li>AURA, AURA_CLOSE</li>
	 * <li>AREA</li>
	 * <li>MULTIFACE</li>
	 * <li>PARTY, CLAN</li>
	 * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
	 * <li>UNLOCKABLE</li>
	 * <li>ITEM</li><BR><BR>
	 *
	 * @param activeChar The L2Character who use the skill
	 */
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target) {
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		L2SkillTargetType targetType = getTargetType();
		
		List<L2Character> targetList = new ArrayList<>();
		
		switch (targetType) {
			case TARGET_HOLY: {
				if (activeChar instanceof L2PcInstance) {
					if (target instanceof L2ArtefactInstance) {
						return new L2Character[]{target};
					}
				}
				
				return emptyTargetList;
			}
			case TARGET_CORPSE_PARTY_CLAN:
			case TARGET_PARTY_CLAN: {
				if (onlyFirst) {
					return new L2Character[]{activeChar};
				}
				
				final L2PcInstance player = activeChar.getActingPlayer();
				
				if (player == null) {
					return emptyTargetList;
				}
				
				final boolean isCorpseType = targetType == L2SkillTargetType.TARGET_CORPSE_PARTY_CLAN;
				
				targetList.add(player);
				
				final int radius = getSkillRadius();
				final boolean hasClan = player.getClan() != null;
				final boolean hasParty = player.isInParty();
				
				if (addCharacter(activeChar, player.getPet(), radius, isCorpseType)) {
					targetList.add(player.getPet());
				}
				for (L2SummonInstance summon : player.getSummons()) {
					if (addCharacter(activeChar, summon, radius, isCorpseType)) {
						targetList.add(summon);
					}
				}
				
				// if player in clan and not in party
				if (!(hasClan || hasParty)) {
					return targetList.toArray(new L2Character[targetList.size()]);
				}
				
				// Get all visible objects in a spherical area near the L2Character
				final Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2PcInstance obj : objs) {
						if (obj == null) {
							continue;
						}
						
						// olympiad mode - adding only own side
						if (player.isInOlympiadMode()) {
							if (!obj.isInOlympiadMode()) {
								continue;
							}
							if (player.getOlympiadGameId() != obj.getOlympiadGameId()) {
								continue;
							}
							if (player.getOlympiadSide() != obj.getOlympiadSide()) {
								continue;
							}
						}
						
						if (player.isInDuel()) {
							if (player.getDuelId() != obj.getDuelId()) {
								continue;
							}
							
							if (hasParty && obj.isInParty() && player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID()) {
								continue;
							}
						}
						
						if (!(hasClan && obj.getClanId() == player.getClanId() ||
								hasParty && obj.isInParty() && player.getParty().getPartyLeaderOID() == obj.getParty().getPartyLeaderOID())) {
							continue;
						}
						
						// Don't add this target if this is a Pc->Pc pvp
						// casting and pvp condition not met
						if (!player.checkPvpSkill(obj, this)) {
							continue;
						}
						
						if (obj.getEvent() != null && obj.getEvent().isState(EventState.STARTED) && player.getEvent() != obj.getEvent()) {
							continue;
						}
						
						if (!onlyFirst) {
							if (addCharacter(activeChar, obj.getPet(), radius, isCorpseType)) {
								targetList.add(obj.getPet());
							}
							for (L2SummonInstance summon : obj.getSummons()) {
								if (addCharacter(activeChar, summon, radius, isCorpseType)) {
									targetList.add(summon);
								}
							}
						}
						
						if (!addCharacter(activeChar, obj, radius, isCorpseType)) {
							continue;
						}
						
						if (onlyFirst) {
							return new L2Character[]{obj};
						}
						
						targetList.add(obj);
					}
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PARTY:
			case TARGET_PARTY: {
				final boolean isCorpseType = targetType == L2SkillTargetType.TARGET_CORPSE_PARTY;
				
				if (!isCorpseType) {
					if (onlyFirst) {
						return new L2Character[]{activeChar};
					}
					
					targetList.add(activeChar);
				}
				
				final int radius = getSkillRadius();
				
				L2PcInstance player = activeChar.getActingPlayer();
				if (activeChar instanceof L2Summon) {
					if (addCharacter(activeChar, player, radius, isCorpseType)) {
						targetList.add(player);
					}
				} else if (activeChar instanceof L2PcInstance) {
					if (addCharacter(activeChar, player.getPet(), radius, isCorpseType)) {
						targetList.add(player.getPet());
					}
					for (L2SummonInstance summon : player.getSummons()) {
						if (addCharacter(activeChar, summon, radius, isCorpseType)) {
							targetList.add(summon);
						}
					}
				}
				
				if (activeChar.isInParty()) {
					// Get a list of Party Members
					for (L2PcInstance partyMember : activeChar.getParty().getPartyMembers()) {
						if (partyMember == null || partyMember == player) {
							continue;
						}
						
						if (partyMember.getEvent() != null && partyMember.getEvent().isState(EventState.STARTED) &&
								(player.getEvent() != partyMember.getEvent() || player.getEvent().getConfig().isAllVsAll())) {
							continue;
						}
						
						if (addCharacter(activeChar, partyMember, radius, isCorpseType)) {
							targetList.add(partyMember);
						}
						
						if (addCharacter(activeChar, partyMember.getPet(), radius, isCorpseType)) {
							targetList.add(partyMember.getPet());
						}
						for (L2SummonInstance summon : partyMember.getSummons()) {
							if (addCharacter(activeChar, summon, radius, isCorpseType)) {
								targetList.add(summon);
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AURA_CORPSE_MOB: {
				// Go through the L2Character knownList
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(getSkillRadius());
				for (L2Character obj : objs) {
					if (obj instanceof L2Attackable && obj.isDead()) {
						if (onlyFirst) {
							return new L2Character[]{obj};
						}
						
						targetList.add(obj);
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_FLAGPOLE: {
				return new L2Character[]{activeChar};
			}
			case TARGET_GROUND_AREA: {
				if (!(activeChar instanceof L2PcInstance)) {
					return emptyTargetList;
				}
				
				L2PcInstance player = (L2PcInstance) activeChar;
				
				Point3D position = player.getSkillCastPosition();
				if (position == null) {
					return emptyTargetList;
				}
				
				final int radius = getSkillRadius();
				
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2Character obj : objs) {
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable)) {
							continue;
						}
						
						if (Util.calculateDistance(obj.getX(), obj.getY(), obj.getZ(), position.getX(), position.getY(), position.getZ(), true) <=
								radius) {
							if (!activeChar.isAbleToCastOnTarget(obj, this, true)) {
								continue;
							}
							
							if (activeChar instanceof L2PcInstance && !((L2PcInstance) activeChar).checkPvpSkill(obj, this)) {
								continue;
							}
							
							targetList.add(obj);
						}
					}
				}
				
				if (targetList.isEmpty()) {
					return emptyTargetList;
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_NOTME:
			case TARGET_ALLY_NOTME: {
				//target all party members except yourself
				if (onlyFirst) {
					return new L2Character[]{activeChar};
				}
				
				L2PcInstance player = null;
				
				if (activeChar instanceof L2Summon) {
					player = ((L2Summon) activeChar).getOwner();
					targetList.add(player);
				} else if (activeChar instanceof L2PcInstance) {
					player = (L2PcInstance) activeChar;
					if (((L2PcInstance) activeChar).getPet() != null) {
						targetList.add(((L2PcInstance) activeChar).getPet());
					}
					for (L2SummonInstance summon : ((L2PcInstance) activeChar).getSummons()) {
						targetList.add(summon);
					}
				}
				
				if (activeChar.getParty() != null) {
					List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();
					for (L2PcInstance partyMember : partyList) {
						if (partyMember == null || partyMember == player) {
							continue;
						}
						
						if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true)) {
							targetList.add(partyMember);
							
							if (partyMember.getPet() != null && !partyMember.getPet().isDead()) {
								targetList.add(partyMember.getPet());
							}
							
							for (L2SummonInstance summon : partyMember.getSummons()) {
								if (!summon.isDead()) {
									targetList.add(summon);
								}
							}
						}
					}
				}
				
				if (targetType == L2SkillTargetType.TARGET_ALLY_NOTME) {
					if (player != null) {
						final int radius = getSkillRadius();
						
						if (addCharacter(activeChar, player.getPet(), radius, false)) {
							targetList.add(player.getPet());
						}
						for (L2SummonInstance summon : player.getSummons()) {
							if (addCharacter(activeChar, summon, radius, false)) {
								targetList.add(summon);
							}
						}
						
						if (player.getClan() != null) {
							// Get all visible objects in a spherical area near the L2Character
							final Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
							//synchronized (activeChar.getKnownList().getKnownObjects())
							{
								for (L2PcInstance obj : objs) {
									if (obj == null || obj == player) {
										continue;
									}
									
									if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) &&
											(obj.getClan() == null || obj.getClanId() != player.getClanId())) {
										continue;
									}
									
									if (player.isInDuel()) {
										if (player.getDuelId() != obj.getDuelId()) {
											continue;
										}
										
										if (player.isInParty() && obj.isInParty() &&
												player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID()) {
											continue;
										}
									}
									
									// Don't add this target if this is a Pc->Pc pvp
									// casting and pvp condition not met
									if (!player.checkPvpSkill(obj, this)) {
										continue;
									}
									
									if (obj.getEvent() != null && obj.getEvent().isState(EventState.STARTED) &&
											EventsManager.getInstance().isPlayerParticipant(obj.getObjectId()) &&
											(player.getEvent() != obj.getEvent() || player.getEvent().getConfig().isAllVsAll())) {
										continue;
									}
									
									if (!onlyFirst) {
										if (addCharacter(activeChar, obj.getPet(), radius, false)) {
											targetList.add(obj.getPet());
										}
										for (L2SummonInstance summon : obj.getSummons()) {
											if (addCharacter(activeChar, summon, radius, false)) {
												targetList.add(summon);
											}
										}
									}
									
									if (!addCharacter(activeChar, obj, radius, false)) {
										continue;
									}
									
									targetList.add(obj);
								}
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CLAN_MEMBER: {
				if (activeChar instanceof L2Npc) {
					// for buff purposes, returns friendly mobs nearby and mob itself
					final L2Npc npc = (L2Npc) activeChar;
					if (npc.getFactionId() == null || npc.getFactionId().isEmpty()) {
						return new L2Character[]{activeChar};
					}
					final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					for (L2Object newTarget : objs) {
						if (newTarget instanceof L2Npc && npc.getFactionId().equals(((L2Npc) newTarget).getFactionId())) {
							if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true)) {
								continue;
							}
							if (((L2Npc) newTarget).getFirstEffect(this) != null) {
								continue;
							}
							targetList.add((L2Npc) newTarget);
							break; // found
						}
					}
					if (targetList.isEmpty()) {
						targetList.add(npc);
					}
				} else if (activeChar instanceof L2PcInstance) {
					if (target instanceof L2PcInstance) {
						final L2PcInstance targetPlayer = (L2PcInstance) target;
						final L2PcInstance casterPlayer = (L2PcInstance) activeChar;
						
						//Dummy checks
						if (targetPlayer == casterPlayer || targetPlayer.isInParty() && casterPlayer.isInParty() &&
								targetPlayer.getParty().getPartyLeaderOID() == casterPlayer.getParty().getPartyLeaderOID() &&
								targetPlayer.getClan() != null && casterPlayer.getClan() != null &&
								targetPlayer.getClanId() == casterPlayer.getClanId() && !targetPlayer.isInOlympiadMode() &&
								!casterPlayer.isInOlympiadMode() && !targetPlayer.isPlayingEvent() && !casterPlayer.isPlayingEvent() &&
								targetPlayer.getInstanceId() == casterPlayer.getInstanceId()) {
							return new L2Character[]{targetPlayer};
						} else {
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
							return emptyTargetList;
						}
					}
				} else {
					return emptyTargetList;
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			// Specially for Block Checker Event
			case TARGET_EVENT: {
				if (activeChar instanceof L2PcInstance) {
					L2PcInstance player = (L2PcInstance) activeChar;
					int playerArena = player.getBlockCheckerArena();
					
					if (playerArena != -1) {
						ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(playerArena);
						int team = holder.getPlayerTeam(player);
						// Aura attack
						for (L2PcInstance actor : player.getKnownList().getKnownPlayersInRadius(250)) {
							if (holder.getAllPlayers().contains(actor) && holder.getPlayerTeam(actor) != team) {
								targetList.add(actor);
							}
						}
						return targetList.toArray(new L2Character[targetList.size()]);
					}
				}
				return emptyTargetList;
			}
			case TARGET_MENTEE: {
				if (activeChar instanceof L2PcInstance) {
					L2PcInstance player = (L2PcInstance) activeChar;
					
					if (target instanceof L2PcInstance && ((L2PcInstance) target).getMentorId() == player.getObjectId()) {
						if (!target.isDead()) {
							return new L2Character[]{target};
						} else {
							return emptyTargetList;
						}
					} else {
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return emptyTargetList;
					}
				}
				return emptyTargetList;
			}
			case TARGET_LINE: {
				if (target == null || target == activeChar || target.isAlikeDead() ||
						!(target instanceof L2Attackable || target instanceof L2Playable)) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return emptyTargetList;
				}
				
				// Tenkai custom - in Duels, area skills attack only Duel enemy. Not checking if same Duel ID, but whatever
				if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isInDuel() ||
						activeChar instanceof L2SummonInstance && ((L2SummonInstance) activeChar).getOwner().isInDuel()) {
					if (activeChar.getTarget() instanceof L2PcInstance && ((L2PcInstance) activeChar.getTarget()).isInDuel()) {
						return new L2Object[]{activeChar.getTarget()};
					} else {
						return emptyTargetList;
					}
				}
				
				targetList.add(target);
				
				final boolean srcInArena = activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE);
				final int radius = getSkillRadius();
				
				// Calculate a normalized direction vector from the player to the target
				float dirX = target.getX() - activeChar.getX();
				float dirY = target.getY() - activeChar.getY();
				float dirZ = target.getZ() - activeChar.getZ();
				float length = (float) Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
				dirX /= length;
				dirY /= length;
				dirZ /= length;
				
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				for (L2Character obj : objs) {
					if (!(obj instanceof L2Attackable || obj instanceof L2Playable)) {
						continue;
					}
					
					if (obj == activeChar) {
						continue;
					}
					
					if (Util.checkIfInRange(radius, activeChar, obj, true)) {
						if (obj == target || !checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena)) {
							continue;
						}
						
						// Calculate a normalized direction vector from the player to the object
						float dx = obj.getX() - activeChar.getX();
						float dy = obj.getY() - activeChar.getY();
						float dz = obj.getZ() - activeChar.getZ();
						length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
						dx /= length;
						dy /= length;
						dz /= length;
						
						// Their dot product is the cosine of the angle between both vectors
						float dot = dirX * dx + dirY * dy + dirZ * dz;
						// If the cosine is near 1, we have a tight angle
						if (dot > 0.99f) {
							targetList.add(obj);
						}
					}
				}
				
				if (targetList.isEmpty()) {
					return emptyTargetList;
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
		}
		
		ISkillTargetTypeHandler stth = SkillTargetTypeHandler.getInstance().getSkillTarget(targetType);
		if (stth != null) {
			/*
			if (activeChar.getName().equals("Chuter"))
			{
				for (L2Object o : result)
					activeChar.sendMessage("TTTT = " + o);
			}*/
			return stth.getTargetList(this, activeChar, onlyFirst, target);
		} else {
			activeChar.sendMessage("Target type not handled.");
			return null;
		}
	}
	
	public final L2Object[] getTargetList(L2Character activeChar) {
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(L2Character activeChar) {
		L2Object[] targets;
		
		targets = getTargetList(activeChar, true);
		
		if (targets == null || targets.length == 0) {
			return null;
		} else {
			return targets[0];
		}
	}
	
	/*
	 * Check if should be target added to the target list
	 * false if target is dead, target same as caster,
	 * target inside peace zone, target in the same party with caster,
	 * caster can see target
	 * Additional checks if not in PvP zones (arena, siege):
	 * target in not the same clan and alliance with caster,
	 * and usual skill PvP check.
	 * If TvT event is active - performing additional checks.
	 *
	 * Caution: distance is not checked.
	 */
	public static boolean checkForAreaOffensiveSkills(L2Character caster, L2Character target, L2Skill skill, boolean sourceInArena) {
		if (target == null || target.isDead() || target == caster || target.isInvul()) {
			return false;
		}
		
		final L2PcInstance player = caster.getActingPlayer();
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (player != null) {
			if (player.inObserverMode()) {
				return false;
			}
			
			if (target instanceof L2MobSummonInstance) {
				return false;
			}
			
			if (targetPlayer != null) {
				if (targetPlayer == caster || targetPlayer == player) {
					return false;
				}
				
				if (targetPlayer.inObserverMode()) {
					return false;
				}
				
				if (player.hasAwakaned()) {
					if (!targetPlayer.hasAwakaned()) {
						return false;
					}
				} else if (targetPlayer.hasAwakaned()) {
					return false;
				}
				
				if (targetPlayer.getLevel() + 9 <= player.getLevel()) {
					return false;
				}
				
				if (skill.isOffensive() && player.getSiegeState() > 0 && player.isInsideZone(L2Character.ZONE_SIEGE) &&
						player.getSiegeState() == targetPlayer.getSiegeState() && player.getSiegeSide() == targetPlayer.getSiegeSide()) {
					return false;
				}
				
				if (target.isInsideZone(L2Character.ZONE_PEACE)) {
					return false;
				}
				
				if (player.isInParty() && targetPlayer.isInParty()) {
					// Same party
					if (player.getParty().getPartyLeaderOID() == targetPlayer.getParty().getPartyLeaderOID()) {
						return false;
					}
					
					// Same commandchannel
					if (player.getParty().getCommandChannel() != null &&
							player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel()) {
						return false;
					}
				}
				
				/*if (EventsManager.getInstance().isState(EventState.STARTED) && EventsManager.getInstance().isPlayerParticipant(targetPlayer.getObjectId()) && player.getEvent() != targetPlayer.getEvent())
					return false;*/
				
				if (player.getEvent() != null && targetPlayer.getEvent() != null && player.getEvent().isState(EventState.STARTED) &&
						!player.getEvent().getConfig().isAllVsAll() && EventsManager.getInstance().isPlayerParticipant(player.getObjectId()) &&
						EventsManager.getInstance().isPlayerParticipant(targetPlayer.getObjectId()) &&
						(EventsManager.getInstance().getParticipantTeamId(player.getObjectId()) ==
								EventsManager.getInstance().getParticipantTeamId(targetPlayer.getObjectId()) ||
								player.getEvent() != targetPlayer.getEvent())) {
					return false;
				}
				
				if (player.getPvpFlag() == 0 && !player.isInsideZone(L2Character.ZONE_PVP) && !player.isInsideZone(L2Character.ZONE_SIEGE)) {
					return false;
				}
				
				if (!sourceInArena && !(targetPlayer.isInsideZone(L2Character.ZONE_PVP) && !targetPlayer.isInsideZone(L2Character.ZONE_SIEGE))) {
					if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId()) {
						return false;
					}
					
					if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId()) {
						return false;
					}
					
					if (!player.checkPvpSkill(targetPlayer, skill, caster instanceof L2Summon)) {
						return false;
					}
				}
			}
		} else {
			// source is not playable
			if (caster instanceof L2Attackable) {
				// target is mob
				if (targetPlayer == null && target instanceof L2Attackable && caster instanceof L2Attackable) {
					String casterEnemyClan = ((L2Attackable) caster).getEnemyClan();
					if (casterEnemyClan == null || casterEnemyClan.isEmpty()) {
						return false;
					}
					
					String targetClan = ((L2Attackable) target).getClan();
					if (targetClan == null || targetClan.isEmpty()) {
						return false;
					}
					
					if (!casterEnemyClan.equals(targetClan)) {
						return false;
					}
					
					if (casterEnemyClan.equals(targetClan) && skill.getSkillType() == L2SkillType.BUFF) {
						return false;
					}
				} else {
					if (caster instanceof L2GuardInstance && caster.isInvul() && target instanceof L2Playable) {
						return false;
					}
				}
			} else if (caster instanceof L2Npc && ((L2Npc) caster).getOwner() != null) //to filter
			{
				if (targetPlayer != null && !checkForAreaOffensiveSkills(((L2Npc) caster).getOwner(), targetPlayer, skill, sourceInArena)) {
					return false;
				}
			}
		}
		
		return !(geoEnabled && !GeoData.getInstance().canSeeTarget(caster, target));
	}
	
	public static boolean addCharacter(L2Character caster, L2Character target, int radius, boolean isDead) {
		if (target == null || isDead != target.isDead()) {
			return false;
		}
		
		return !(radius > 0 && !Util.checkIfInRange(radius, caster, target, true) && !GeoData.getInstance().canSeeTarget(caster, target));
	}
	
	public final Func[] getStatFuncs(L2Character player) {
		if (funcTemplates == null) {
			return emptyFunctionSet;
		}
		
		if (!(player instanceof L2Playable) && !(player instanceof L2Attackable)) {
			return emptyFunctionSet;
		}
		
		ArrayList<Func> funcs = new ArrayList<>(funcTemplates.length);
		
		Func f;
		for (FuncTemplate t : funcTemplates) {
			f = t.getFunc(this); // skill is owner
			if (f != null) {
				funcs.add(f);
			}
		}
		if (funcs.isEmpty()) {
			return emptyFunctionSet;
		}
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects() {
		return effectTemplates != null && effectTemplates.length > 0;
	}
	
	public L2AbnormalTemplate[] getEffectTemplates() {
		return effectTemplates;
	}
	
	public boolean hasSelfEffects() {
		return effectTemplatesSelf != null && effectTemplatesSelf.length > 0;
	}
	
	/**
	 * Env is used to pass parameters for secondary effects (shield and ss/bss/bsss)
	 *
	 * @return an array with the effects that have been added to effector
	 */
	public final L2Abnormal[] getEffects(L2Character effector, L2Character effected, Env env) {
		if (!hasEffects() || isPassive()) {
			return emptyEffectSet;
		}
		
		// doors and siege flags cannot receive any effects
		if (effected instanceof L2DoorInstance || effected instanceof L2SiegeFlagInstance) {
			return emptyEffectSet;
		}
		
		if (effector != effected && !ignoreImmunity()) {
			if (effected instanceof L2PcInstance && effected.getFaceoffTarget() != null && effector != effected.getFaceoffTarget()) {
				return emptyEffectSet;
			}
			
			if (isOffensive() || isDebuff()) {
				if (effected.isInvul(effector) && getId() != 11604) // Shocking Blow
				{
					boolean invul = true;
					for (L2Abnormal effect : effected.getAllEffects()) {
						if (effect.getSkill().getDamageToRemove() > 0) {
							invul = false;
						}
					}
					
					if (invul) {
						return emptyEffectSet;
					}
				}
				
				if (effector instanceof L2PcInstance && effector.isGM()) {
					if (!((L2PcInstance) effector).getAccessLevel().canGiveDamage()) {
						return emptyEffectSet;
					}
				}
			}
		}
		
		ArrayList<L2Abnormal> effects = new ArrayList<>(effectTemplates.length);
		if (env == null) {
			env = new Env();
		}
		
		if (!isOffensive()) {
			for (L2AbnormalTemplate effect : effectTemplates) {
				if (effected.calcStat(Stats.BUFF_IMMUNITY, 0.0, effector, null) > 0.0) {
					return emptyEffectSet;
				}
				
				if (effected.isAffected(L2EffectType.BLOCK_INVUL.getMask())) {
					for (L2EffectTemplate eff : effect.effects) {
						if (eff.funcName.equals("Invincible")) {
							return emptyEffectSet;
						}
					}
				}
				
				if (effected.isAffected(L2EffectType.BLOCK_HIDE.getMask())) {
					for (L2EffectTemplate eff : effect.effects) {
						if (eff.funcName.equals("Hide")) {
							return emptyEffectSet;
						}
					}
				}
				
				if (effected.isAffected(L2EffectType.BLOCK_TALISMANS.getMask()) && getName().contains("Talisman")) {
					return emptyEffectSet;
				}
			}
		}
		
		env.skillMastery = Formulas.calcSkillMastery(effector, this);
		env.player = effector;
		env.target = effected;
		env.skill = this;
		
		for (L2AbnormalTemplate et : effectTemplates) {
			L2Abnormal e = et.getEffect(env);
			if (e == null) {
				continue;
			}
			
			boolean success = true;
			if (et.landRate > -1) {
				success = Formulas.calcEffectSuccess(effector, effected, e, this, env.shld, env.ssMul);
			}
			
			if (success) {
				e.scheduleEffect();
				effects.add(e);
			}
			// display fail message only for effects with icons
			else if (et.icon && effector instanceof L2PcInstance) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(effected);
				sm.addSkillName(this);
				effector.sendPacket(sm);
			}
		}
		
		if (effects.isEmpty()) {
			return emptyEffectSet;
		}
		
		return effects.toArray(new L2Abnormal[effects.size()]);
	}
	
	/**
	 * Warning: this method doesn't consider modifier (shield, ss, sps, bss) for secondary effects
	 */
	public final L2Abnormal[] getEffects(L2Character effector, L2Character effected) {
		return getEffects(effector, effected, null);
	}
	
	/**
	 * This method has suffered some changes in CT2.2 ->CT2.3<br>
	 * Effect engine is now supporting secondary effects with independent
	 * success/fail calculus from effect skill. Env parameter has been added to
	 * pass parameters like soulshot, spiritshots, blessed spiritshots or shield deffence.
	 * Some other optimizations have been done
	 * <br><br>
	 * This new feature works following next Rule:
	 * <li> To enable feature, effectPower must be over -1 (check DocumentSkill#attachEffect for further information)</li>
	 * <li> If main skill fails, secondary effect always fail</li>
	 */
	public final L2Abnormal[] getEffects(L2CubicInstance effector, L2Character effected, Env env) {
		if (!hasEffects() || isPassive()) {
			return emptyEffectSet;
		}
		
		if (effector.getOwner() != effected && !ignoreImmunity()) {
			if (isDebuff() || isOffensive()) {
				if (effected.isInvul(effector.getOwner())) {
					return emptyEffectSet;
				}
				
				if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage()) {
					return emptyEffectSet;
				}
			}
		}
		
		ArrayList<L2Abnormal> effects = new ArrayList<>(effectTemplates.length);
		
		if (env == null) {
			env = new Env();
		}
		
		env.player = effector.getOwner();
		env.cubic = effector;
		env.target = effected;
		env.skill = this;
		
		for (L2AbnormalTemplate et : effectTemplates) {
			L2Abnormal e = et.getEffect(env);
			if (e == null) {
				continue;
			}
			
			boolean success = true;
			if (et.landRate > -1) {
				success = Formulas.calcEffectSuccess(effector.getOwner(), effected, e, this, env.shld, env.ssMul);
			}
			
			if (success) {
				e.scheduleEffect();
				effects.add(e);
			}
		}
		
		if (effects.isEmpty()) {
			return emptyEffectSet;
		}
		
		return effects.toArray(new L2Abnormal[effects.size()]);
	}
	
	public final L2Abnormal[] getEffectsSelf(L2Character effector) {
		if (!hasSelfEffects() || isPassive()) {
			return emptyEffectSet;
		}
		
		List<L2Abnormal> effects = new ArrayList<>(effectTemplatesSelf.length);
		
		for (L2AbnormalTemplate et : effectTemplatesSelf) {
			Env env = new Env();
			env.skillMastery = Formulas.calcSkillMastery(effector, this);
			env.player = effector;
			env.target = effector;
			env.skill = this;
			L2Abnormal e = et.getEffect(env);
			if (e != null) {
				e.setSelfEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		if (effects.isEmpty()) {
			return emptyEffectSet;
		}
		
		return effects.toArray(new L2Abnormal[effects.size()]);
	}
	
	public final void attach(FuncTemplate f) {
		if (funcTemplates == null) {
			funcTemplates = new FuncTemplate[]{f};
		} else {
			int len = funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
	
	public final void attach(L2AbnormalTemplate effect) {
		if (effectTemplates == null) {
			effectTemplates = new L2AbnormalTemplate[]{effect};
		} else {
			int len = effectTemplates.length;
			L2AbnormalTemplate[] tmp = new L2AbnormalTemplate[len + 1];
			System.arraycopy(effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			effectTemplates = tmp;
		}
	}
	
	public final void attachSelf(L2AbnormalTemplate effect) {
		if (effectTemplatesSelf == null) {
			effectTemplatesSelf = new L2AbnormalTemplate[]{effect};
		} else {
			int len = effectTemplatesSelf.length;
			L2AbnormalTemplate[] tmp = new L2AbnormalTemplate[len + 1];
			System.arraycopy(effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			effectTemplatesSelf = tmp;
		}
	}
	
	public final void attach(Condition c, boolean itemOrWeapon) {
		if (itemOrWeapon) {
			if (itemPreCondition == null) {
				itemPreCondition = new ArrayList<>();
			}
			itemPreCondition.add(c);
		} else {
			if (preCondition == null) {
				preCondition = new ArrayList<>();
			}
			preCondition.add(c);
		}
	}
	
	@Override
	public String toString() {
		return "" + name + "[id=" + id + ",lvl=" + level + "]";
	}
	
	/**
	 * @return pet food
	 */
	public int getFeed() {
		return feed;
	}
	
	/**
	 * used for tracking item id in case that item consume cannot be used
	 *
	 * @return reference item id
	 */
	public int getReferenceItemId() {
		return refId;
	}
	
	public final int getMaxCharges() {
		return maxCharges;
	}
	
	public int getAfterEffectId() {
		return afterEffectId;
	}
	
	public int getAfterEffectLvl() {
		return afterEffectLvl;
	}
	
	@Override
	public boolean triggersChanceSkill() {
		return triggeredId > 0 && isChance();
	}
	
	@Override
	public int getTriggeredChanceId() {
		return triggeredId;
	}
	
	@Override
	public int getTriggeredChanceLevel() {
		return triggeredLevel;
	}
	
	@Override
	public int getTriggeredChanceEnchantRoute() {
		return triggeredEnchantRoute;
	}
	
	@Override
	public int getTriggeredChanceEnchantLevel() {
		return triggeredEnchantLevel;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition() {
		return chanceCondition;
	}
	
	public String getAttributeName() {
		return attribute;
	}
	
	/**
	 * @return the blowChance
	 */
	public int getBlowChance() {
		return blowChance;
	}
	
	public boolean ignoreShield() {
		return ignoreShield;
	}
	
	public boolean canBeReflected() {
		return canBeReflected;
	}
	
	public boolean canBeSharedWithSummon() {
		return canBeSharedWithSummon;
	}
	
	public boolean canBeDispeled() {
		return canBeDispeled;
	}
	
	public boolean isClanSkill() {
		return isClanSkill;
	}
	
	public boolean isExcludedFromCheck() {
		return excludedFromCheck;
	}
	
	public float getDependOnTargetBuff() {
		return dependOnTargetBuff;
	}
	
	public boolean isSimultaneousCast() {
		return simultaneousCast;
	}
	
	/**
	 * @param skillId
	 * @param skillLvl
	 * @param values
	 * @return L2ExtractableSkill
	 * @author Zoey76
	 */
	private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values) {
		String[] lineSplit = values.split(";");
		
		final ArrayList<L2ExtractableProductItem> product_temp = new ArrayList<>();
		
		for (int i = 0; i <= lineSplit.length - 1; i++) {
			final String[] lineSplit2 = lineSplit[i].split(",");
			
			if (lineSplit2.length < 3) {
				Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong separator!");
			}
			
			int[] production = null;
			int[] amount = null;
			double chance = 0;
			int prodId = 0;
			int quantity = 0;
			try {
				int k = 0;
				production = new int[(lineSplit2.length - 1) / 2];
				amount = new int[(lineSplit2.length - 1) / 2];
				for (int j = 0; j < lineSplit2.length - 1; j++) {
					prodId = Integer.parseInt(lineSplit2[j]);
					quantity = Integer.parseInt(lineSplit2[j += 1]);
					if (prodId <= 0 || quantity <= 0) {
						Log.warning(
								"Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId +
										" or wrond quantity: " + quantity + "!");
					}
					production[k] = prodId;
					amount[k] = quantity;
					k++;
				}
				chance = Double.parseDouble(lineSplit2[lineSplit2.length - 1]);
			} catch (Exception e) {
				Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl +
						" -> incomplete/invalid production data or wrong separator!");
				e.printStackTrace();
			}
			
			product_temp.add(new L2ExtractableProductItem(production, amount, chance));
		}
		
		if (product_temp.size() == 0) {
			Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
		}
		
		return new L2ExtractableSkill(SkillTable.getSkillHashCode(this), product_temp);
	}
	
	public L2ExtractableSkill getExtractableSkill() {
		return extractableItems;
	}
	
	public boolean isTriggered() {
		return isTriggered;
	}
	
	public void setIsTriggered() {
		isTriggered = true;
	}
	
	public boolean isActivation() {
		return isTriggered && !isDebuff;
	}
	
	public int getPartyChangeSkill() {
		return partyChangeSkill;
	}
	
	public int getPartyChangeSkillLevel() {
		return partyChangeSkillLevel;
	}
	
	public int getPartyChangeSkillEnchantRoute() {
		return partyChangeSkillEnchantRoute;
	}
	
	public int getPartyChangeSkillEnchantLevel() {
		return partyChangeSkillEnchantLevel;
	}
	
	public boolean isCastedToParty() {
		return isCastedToParty;
	}
	
	/**
	 * Return the additional alter skill info.<BR><BR>
	 *
	 * @return
	 */
	public final int getAlterSkillId() {
		return alterSkillId;
	}
	
	public final int getAlterSkillLevel() {
		return alterSkillLevel;
	}
	
	public final int getAlterSkillTime() {
		return alterIconTime;
	}
	
	public int[] getDependOnTargetEffectId() {
		return dependOnTargetEffectId;
	}
	
	public double[] getDamageDepend() {
		return damageDepend;
	}
	
	public boolean isElemental() {
		return isElemental;
	}
	
	public boolean isStanceSwitch() {
		return isStanceSwitch;
	}
	
	public String getFirstEffectStack() {
		if (getEffectTemplates() != null && getEffectTemplates().length > 0) {
			if (getEffectTemplates()[0].stackType.length == 0) {
				return "";
			}
			
			return getEffectTemplates()[0].stackType[0];
		}
		return "";
	}
	
	public L2SkillBehaviorType getSkillBehavior() {
		if (behaviorType != L2SkillBehaviorType.UNKNOWN) {
			return behaviorType;
		}
		
		// Temporary failsafe
		if (isAttack()) {
			return L2SkillBehaviorType.ATTACK;
		}
		if (isDebuff()) {
			return L2SkillBehaviorType.UNFRIENDLY;
		}
		
		return L2SkillBehaviorType.FRIENDLY;
	}
	
	public final boolean isAttack() {
		switch (getSkillType()) {
			case PDAM:
			case BLOW:
			case CHARGEDAM:
			case MDAM:
			case DRAIN:
			case DEATHLINK:
			case CPDAM:
			case CPDAMPERCENT:
			case FATAL:
			case MARK:
				return true;
		}
		
		return false;
	}
	
	public boolean isAuraAttack() {
		switch (getTargetType()) {
			case TARGET_AROUND_CASTER:
			case TARGET_AROUND_TARGET:
			case TARGET_AURA: // Set temporary for compatiblity.
				return true;
		}
		
		return false;
	}
	
	public boolean isUseableWithoutTarget() {
		if (getTargetType() == L2SkillTargetType.TARGET_SELF) {
			return true;
		} else if (getTargetType() == L2SkillTargetType.TARGET_GROUND) {
			return true;
		} else if (getTargetType() == L2SkillTargetType.TARGET_AROUND_CASTER) {
			return true;
		} else if (getTargetType() == L2SkillTargetType.TARGET_SPECIAL) {
			return getTargetDirection() != L2SkillTargetDirection.CHAIN_HEAL;
		} else if (getTargetType() == L2SkillTargetType.TARGET_GROUND_AREA) {
			return true;
		} else if (getTargetType() == L2SkillTargetType.TARGET_SUMMON) {
			if (getTargetDirection() == L2SkillTargetDirection.ALL_SUMMONS) {
				return true;
			}
		} else if (getTargetType() == L2SkillTargetType.TARGET_FRIENDS) {
			if (getTargetDirection() == L2SkillTargetDirection.PARTY_AND_CLAN) {
				return false;
			}
			
			if (getTargetDirection() != L2SkillTargetDirection.PARTY_ONE && getTargetDirection() != L2SkillTargetDirection.PARTY_ONE_NOTME) {
				return true;
			}
		}
		
		if (getTargetDirection() == L2SkillTargetDirection.PARTY_ALL) {
			return true;
		} else if (getTargetDirection() == L2SkillTargetDirection.PARTY_AND_CLAN) {
			return true;
		} else if (getTargetDirection() == L2SkillTargetDirection.PARTY_ALL_NOTME) {
			return true;
		} else if (getTargetDirection() == L2SkillTargetDirection.ALLIANCE) {
			return true;
		} else if (getTargetDirection() == L2SkillTargetDirection.CLAN) {
			return true;
		} else if (getTargetDirection() == L2SkillTargetDirection.SUBLIMES) {
			return true;
		}
		
		return false;
	}
	
	public L2SkillTargetDirection getTargetDirection() {
		return targetDirection;
	}
	
	public final boolean isFishingSkill() {
		switch (getSkillType()) {
			case PUMPING:
			case REELING:
			case FISHING:
				return true;
		}
		
		return false;
	}
	
	public final boolean isUseableOnSelf() {
		switch (getSkillType()) {
			case BUFF:
			case HEAL:
				//case HOT:
			case HEAL_PERCENT:
			case HPMPCPHEAL_PERCENT:
			case HPCPHEAL_PERCENT:
			case HPMPHEAL_PERCENT:
			case MANARECHARGE:
			case MANAHEAL:
			case NEGATE:
			case CANCEL_DEBUFF:
			case OVERHEAL:
				//case REFLECT:
			case COMBATPOINTHEAL:
			case BALANCE_LIFE:
				return true;
		}
		
		return false;
	}
	
	public final boolean isUseableOnDead() {
		if (getSkillType() == L2SkillType.RESURRECT) {
			return true;
		}
		
		switch (getTargetType()) {
			case TARGET_AREA_CORPSE_MOB:
			case TARGET_CORPSE:
			case TARGET_CORPSE_MOB:
				return true;
			
			default: {
				switch (getTargetDirection()) {
					case DEAD_PLAYABLE:
					case DEAD_PARTY_MEMBER:
					case DEAD_CLAN_MEMBER:
					case DEAD_ALLY_MEMBER:
					case DEAD_PET:
					case DEAD_MONSTER:
						return true;
					
					default:
						return false;
				}
			}
		}
	}
	
	public int getActionId() {
		return skillActionId;
	}
}
