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
package l2tserver.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import l2tserver.Config;
import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.datatables.GMSkillTable;
import l2tserver.gameserver.datatables.HeroSkillTable;
import l2tserver.gameserver.datatables.ItemTable;
import l2tserver.gameserver.datatables.PlayerClassTable;
import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.events.instanced.EventsManager;
import l2tserver.gameserver.instancemanager.HandysBlockCheckerManager;
import l2tserver.gameserver.instancemanager.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2tserver.gameserver.model.actor.L2Attackable;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.model.actor.L2Summon;
import l2tserver.gameserver.model.actor.instance.L2ArtefactInstance;
import l2tserver.gameserver.model.actor.instance.L2ChestInstance;
import l2tserver.gameserver.model.actor.instance.L2CubicInstance;
import l2tserver.gameserver.model.actor.instance.L2DoorInstance;
import l2tserver.gameserver.model.actor.instance.L2GuardInstance;
import l2tserver.gameserver.model.actor.instance.L2MobSummonInstance;
import l2tserver.gameserver.model.actor.instance.L2MonsterInstance;
import l2tserver.gameserver.model.actor.instance.L2NpcInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.actor.instance.L2PetInstance;
import l2tserver.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2tserver.gameserver.model.actor.instance.L2SummonInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.stats.BaseStats;
import l2tserver.gameserver.stats.Env;
import l2tserver.gameserver.stats.Formulas;
import l2tserver.gameserver.stats.conditions.Condition;
import l2tserver.gameserver.stats.funcs.Func;
import l2tserver.gameserver.stats.funcs.FuncTemplate;
import l2tserver.gameserver.taskmanager.DecayTaskManager;
import l2tserver.gameserver.templates.StatsSet;
import l2tserver.gameserver.templates.item.L2Armor;
import l2tserver.gameserver.templates.item.L2ArmorType;
import l2tserver.gameserver.templates.item.L2WeaponType;
import l2tserver.gameserver.templates.skills.L2AbnormalTemplate;
import l2tserver.gameserver.templates.skills.L2AbnormalType;
import l2tserver.gameserver.templates.skills.L2SkillType;
import l2tserver.gameserver.util.Util;
import l2tserver.log.Log;
import l2tserver.util.Point3D;

/**
 * This class...
 *
 * @version $Revision: 1.3.2.8.2.22 $ $Date: 2005/04/06 16:13:42 $
 */
public abstract class L2Skill implements IChanceSkillTrigger
{
	private static final L2Object[] _emptyTargetList = new L2Object[0];
	
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	public static final int SKILL_DIVINE_EXPANSION = 10956;
	public static final int SKILL_CLAN_LUCK = 390;
	
	public static final boolean geoEnabled = Config.GEODATA > 0;
	
	public static enum SkillOpType
	{
		OP_PASSIVE, OP_ACTIVE, OP_TOGGLE
	}
	
	/** Target types of skills : SELF, PARTY, CLAN, PET... */
	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PVP,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_SUMMON,
		TARGET_ALL_SUMMONS,
		TARGET_AREA,
		TARGET_PVP_AREA,
		TARGET_FRONT_AREA,
		TARGET_BEHIND_AREA,
		TARGET_AURA,
		TARGET_FRONT_AURA,
		TARGET_BEHIND_AURA,
		TARGET_PVP_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_AREA_UNDEAD,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PARTY_CLAN,
		TARGET_CORPSE_PARTY,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_AURA_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_FLAGPOLE,
		TARGET_PARTY_MEMBER,
		TARGET_PARTY_OTHER,
		TARGET_PARTY_CLAN,
		TARGET_ENEMY_SUMMON,
		TARGET_OWNER_PET,
		TARGET_GROUND,
		TARGET_GROUND_AREA,
		TARGET_PARTY_NOTME,
		TARGET_ALLY_NOTME,
		TARGET_AREA_SUMMON,
		TARGET_CLAN_MEMBER,
		TARGET_EVENT,
		TARGET_MENTEE,
		TARGET_LINE
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
	public final static int COND_RUNNING = 0x0001;
	public final static int COND_WALKING = 0x0002;
	public final static int COND_SIT = 0x0004;
	public final static int COND_BEHIND = 0x0008;
	public final static int COND_CRIT = 0x0010;
	public final static int COND_LOWHP = 0x0020;
	public final static int COND_ROBES = 0x0040;
	public final static int COND_CHARGES = 0x0080;
	public final static int COND_SHIELD = 0x0100;
	public final static int COND_FRONT = 0x0200;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final L2Abnormal[] _emptyEffectSet = new L2Abnormal[0];
	
	// these two build the primary key
	private final int _id;
	private final int _level;
	private final int _enchantRouteId;
	private final int _enchantLevel;
	
	/** Identifier for a skill that client can't display */
	private int _displayId;
	
	// not needed, just for easier debug
	private final String _name;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private final boolean _staticReuse;
	private final boolean _staticHitTime;
	private final int _mpConsume;
	private final int _hpConsume;
	private final int _cpConsume;
	
	private final int _targetConsume;
	private final int _targetConsumeId;
	
	private final int _itemConsume;
	private final int _itemConsumeId;

	private final int _fameConsume;
	private final int _clanRepConsume;
	
	private final int _castRange;
	private final int _effectRange;
	
	// Abnormal levels for skills and their canceling, e.g. poison vs negate
	private final int _abnormalLvl; // e.g. poison or bleed lvl 2
	// Note: see also _effectAbnormalLvl
	private final int[] _negateId; 			// cancels the effect of skill ID
	private final L2AbnormalType[] _negateStats; 	// lists the effect types that are canceled
	private final Map<String, Byte> _negateAbnormals; // lists the effect abnormal types with order below the presented that are canceled
	private final int _maxNegatedEffects; 	// maximum number of effects to negate
	
	private final boolean _stayAfterDeath; // skill should stay after death

	// kill by damage over time
	private final boolean _killByDOT;
	// absorb the damage over time
	private final boolean _absorbDOT;
	
	private final int _refId;
	// all times in milliseconds
	private final int _hitTime;
	private final int[] _hitTimings;
	//private final int _skillInterruptTime;
	private final int _coolTime;
	private final int _reuseHashCode;
	private final int _reuseDelay;
	private final int _buffDuration;
	// for item skills delay on equip
	private final int _equipDelay;
	
	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetType _targetType;
	private final int _feed;
	// base success chance
	private final double _power;
	private final double _pvpPower;
	private final double _pvePower; //FIXME: remove?
	private final int _magicLevel;
	private final int _levelDepend;
	private final boolean _ignoreResists;
	private final boolean _ignoreImmunity;
	private final int _minChance;
	private final int _maxChance;
	private final int _blowChance;
	
	private final boolean _isNeutral;
	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int _skillRadius;
	private final int _skillSafeRadius;
	
	private final L2SkillType _skillType;
	private final int _effectAbnormalLvl; // abnormal level for the additional effect type, e.g. poison lvl 1
	private final int _effectId;
	private final int _effectLvl; // normal effect level
	
	private final boolean _nextActionIsAttack;
	private final boolean _nextActionIsAttackMob;
	
	private final boolean _removedOnAction;
	private final boolean _removedOnDamage;
	private final int _removedOnDamageChance;
	private final int _strikesToRemove;
	private final boolean _removedOnDebuffBlock;
	private final int _debuffBlocksToRemove;
	
	private final boolean _isPotion;
	private final byte _element;
	private final int _elementPower;
	
	private final BaseStats _saveVs;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	private final int _weaponsAllowed;
	private final int _armorsAllowed;
	
	private final int _minPledgeClass;
	private final boolean _isOffensive;
	private final int _maxCharges;
	private final int _numCharges;
	private final int _maxChargeConsume;
	private final int _triggeredId;
	private final int _triggeredLevel;
	private final String _chanceType;
	private final int _soulMaxConsume;
	private final int _soulConsume;
	private final int _numSouls;
	private final int _expNeeded;
	private final int _critChance;
	private final float _dependOnTargetBuff;
	private final int[] _dependOnTargetEffectId;
	private final double[] _damageDepend;
	
	private final int _transformId;
	private final int _transformDuration;
	
	private final int _afterEffectId;
	private final int _afterEffectLvl;
	private final boolean _isHeroSkill; // If true the skill is a Hero Skill
	private final boolean _isGMSkill;	// True if skill is GM skill
	
	private final int _baseCritRate;  // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int _lethalEffect1;	 // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int _lethalEffect2;	 // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean _directHpDmg;  // If true then dmg is being make directly
	private final boolean _isDance;	  // If true then casting more dances will cost more MP
	private final int _nextDanceCost;
	private final float _sSBoost;	//If true skill will have SoulShot boost (power*2)
	private final int _aggroPoints;
	private final float _ignoredDefPercent;
	private final boolean _canBeUsedWhenDisabled;
	
	protected List<Condition> _preCondition;
	protected List<Condition> _itemPreCondition;
	protected FuncTemplate[] _funcTemplates;
	protected L2AbnormalTemplate[] _effectTemplates;
	protected L2AbnormalTemplate[] _effectTemplatesSelf;
	
	protected ChanceCondition _chanceCondition = null;
	
	// Flying support
	private final String _flyType;
	private final int _flyRadius;
	private final float _flyCourse;
	
	private final boolean _isDebuff;
	
	private final String _attribute;
	
	private final boolean _ignoreShield;
	private final boolean _isSuicideAttack;
	private final boolean _canBeReflected;
	private final boolean _canBeSharedWithSummon;
	
	private final boolean _canBeDispeled;
	
	private final boolean _isClanSkill;
	private final boolean _excludedFromCheck;
	private final boolean _simultaneousCast;
	
	private L2ExtractableSkill _extractableItems = null;
	
	private boolean _isTriggered = false;

	private int _partyChangeSkill = -1;
	private boolean _isCastedToParty = false;
	private final int _skillActionId;
	private final int _alterSkillId;
	private final int _alterSkillLevel;
	private final int _alterIconTime;
	private final boolean _isElemental;
	private final boolean _isStanceSwitch;
	
	protected L2Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_enchantRouteId = set.getInteger("enchantRouteId", 0);
		_enchantLevel = set.getInteger("enchantLevel", 0);
		_refId = set.getInteger("referenceId", 0);
		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_magic = set.getBool("isMagic", false);
		_staticReuse = set.getBool("staticReuse", false);
		_staticHitTime = set.getBool("staticHitTime", false);
		_isPotion = set.getBool("isPotion", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_cpConsume = set.getInteger("cpConsume", 0);
		_targetConsume = set.getInteger("targetConsumeCount", 0);
		_targetConsumeId = set.getInteger("targetConsumeId", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_fameConsume = set.getInteger("fameConsume", 0);
		_clanRepConsume = set.getInteger("clanRepConsume", 0);
		_afterEffectId = set.getInteger("afterEffectId", 0);
		_afterEffectLvl = set.getInteger("afterEffectLvl", 1);
		_castRange = set.getInteger("castRange", -1);
		_effectRange = set.getInteger("effectRange", -1);
		_abnormalLvl = set.getInteger("abnormalLvl", -1);
		_effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
		_attribute = set.getString("attribute","");
		String str = set.getString("negateStats", "");
		
		if (str == "")
			_negateStats = new L2AbnormalType[0];
		else
		{
			String[] stats = str.split(" ");
			L2AbnormalType[] array = new L2AbnormalType[stats.length];
			
			for (int i = 0;  i < stats.length; i++)
			{
				L2AbnormalType type = null;
				try
				{
					type = Enum.valueOf(L2AbnormalType.class, stats[i]);
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException("SkillId: " + _id + " Enum value of type L2AbnormalType required, but found: " + stats[i]);
				}
				
				array[i] = type;
			}
			_negateStats = array;
		}
		
		String negateAbnormals = set.getString("negateAbnormals", null);
		if (negateAbnormals != null && negateAbnormals != "")
		{
			_negateAbnormals = new HashMap<String, Byte>();
			for (String ngtStack : negateAbnormals.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				if (ngt.length == 1) // Only abnormalType is present, without abnormalLvl
				{
					_negateAbnormals.put(ngt[0], Byte.MAX_VALUE);
				}
				else if (ngt.length == 2) // Both abnormalType and abnormalLvl are present
				{
					try
					{
						_negateAbnormals.put(ngt[0], Byte.parseByte(ngt[1]));
					}
					catch (Exception e)
					{
						throw new IllegalArgumentException("SkillId: " + _id + " Byte value required, but found: " + ngt[1]);
					}
				}
				else // If not both from above, then smth is messed up... throw an error.
					throw new IllegalArgumentException("SkillId: "+_id+": Incorrect negate Abnormals for "+ngtStack+". Lvl: abnormalType1,abnormalLvl1;abnormalType2,abnormalLvl2;abnormalType3,abnormalLvl3... or abnormalType1;abnormalType2;abnormalType3...");
			}
		}
		else
			_negateAbnormals = null;
		
		String negateId = set.getString("negateId", null);
		if (negateId != null)
		{
			String[] valuesSplit = negateId.split(",");
			_negateId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length;i++)
			{
				_negateId[i] = Integer.parseInt(valuesSplit[i]);
			}
		}
		else
			_negateId = new int[0];
		_maxNegatedEffects = set.getInteger("maxNegated", 0);
		
		_stayAfterDeath = set.getBool("stayAfterDeath", false);

		_killByDOT = set.getBool("killByDOT", false);
		_absorbDOT = set.getBool("absorbDOT", false);
		_isNeutral = set.getBool("neutral", false);
		_hitTime = set.getInteger("hitTime", 0);
		String hitTimings = set.getString("hitTimings", null);
		if (hitTimings != null)
		{
			try
			{
				String [] valuesSplit = hitTimings.split(",");
				_hitTimings = new int[valuesSplit.length];
				for (int i = 0; i < valuesSplit.length; i++)
					_hitTimings[i] = Integer.parseInt(valuesSplit[i]);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: "+_id+" invalid hitTimings value: "+hitTimings+", \"percent,percent,...percent\" required");
			}
		}
		else
			_hitTimings = new int[0];
		
		_coolTime = set.getInteger("coolTime", 0);
		_feed = set.getInteger("feed", 0);
		
		String reuseHash = set.getString("sharedReuse", null);
		if (reuseHash != null)
		{
			try
			{
				String[] valuesSplit = reuseHash.split("-");
				if (valuesSplit.length > 1)
					_reuseHashCode = Integer.parseInt(valuesSplit[0]) * 1000 + Integer.parseInt(valuesSplit[1]);
				else
					_reuseHashCode = Integer.parseInt(valuesSplit[0]) * 1000 + _level;
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("SkillId: "+_id+" invalid sharedReuse value: "+reuseHash+", \"skillId-skillLvl\" required");
			}
		}
		else
			_reuseHashCode = _id * 1000 + _level;
		
		if (Config.ENABLE_MODIFY_SKILL_REUSE && Config.SKILL_REUSE_LIST.containsKey(_id))
		{
			if ( Config.DEBUG )
				Log.info("*** Skill " + _name + " (" + _level + ") changed reuse from " + set.getInteger("reuseDelay", 0) + " to " + Config.SKILL_REUSE_LIST.get(_id) + " seconds.");
			_reuseDelay = Config.SKILL_REUSE_LIST.get(_id);
		}
		else
		{
			_reuseDelay = set.getInteger("reuseDelay", 0);
		}
		
		_buffDuration = set.getInteger("buffDuration", 0);
		
		_equipDelay = set.getInteger("equipDelay", 0);
		
		_skillRadius = set.getInteger("skillRadius", 80);
		
		_skillSafeRadius = set.getInteger("skillSafeRadius", 0);
		
		_targetType = set.getEnum("target", SkillTargetType.class);
		_power = set.getFloat("power", 0.f);
		_pvpPower = set.getFloat("pvpPower", (float)getPower());
		_pvePower = set.getFloat("pvePower", (float)getPower());
		_magicLevel = set.getInteger("magicLvl", PlayerClassTable.getInstance().getMinSkillLevel(_id, _level));
		_levelDepend = set.getInteger("lvlDepend", 0);
		_ignoreResists = set.getBool("ignoreResists", false);
		_ignoreImmunity = set.getBool("ignoreImmunity", false);
		_minChance = set.getInteger("minChance", 10);
		_maxChance = set.getInteger("maxChance", 90);
		_ignoreShield = set.getBool("ignoreShld", false);
		_skillType = set.getEnum("skillType", L2SkillType.class);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getInteger("effectLevel", 0);
		
		_nextActionIsAttack = set.getBool("nextActionAttack", false);
		_nextActionIsAttackMob = set.getBool("nextActionAttackMob", false);
		
		_removedOnAction = set.getBool("removedOnAction", false);
		_removedOnDamage = set.getBool("removedOnDamage", false);
		_removedOnDamageChance = set.getInteger("removedOnDamageChance", _removedOnDamage ? 100: 0);
		_strikesToRemove = set.getInteger("strikesToRemove", 0);
		_removedOnDebuffBlock = set.getBool("removedOnDebuffBlock", false);
		_debuffBlocksToRemove = set.getInteger("debuffBlocksToRemove", 0);
		
		_element = set.getByte("element", (byte)-1);
		_elementPower = set.getInteger("elementPower", 0);
		
		_saveVs = set.getEnum("saveVs", BaseStats.class, null);
		
		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		
		String weaponsAllowedString = set.getString("weaponsAllowed", null);
		if (weaponsAllowedString != null && !weaponsAllowedString.trim().isEmpty())
		{
			int mask = 0;
			StringTokenizer st = new StringTokenizer(weaponsAllowedString, ",");
			while (st.hasMoreTokens())
			{
				int old = mask;
				String item = st.nextToken().trim();
				if (ItemTable._weaponTypes.containsKey(item))
					mask |= ItemTable._weaponTypes.get(item).mask();
				
				if (ItemTable._armorTypes.containsKey(item)) // for shield
					mask |= ItemTable._armorTypes.get(item).mask();
				
				if (item.equals("crossbow"))
					mask |= L2WeaponType.CROSSBOWK.mask();
				
				if (old == mask)
					Log.info("[weaponsAllowed] Unknown item type name: "+item);
			}
			_weaponsAllowed = mask;
		}
		else
			_weaponsAllowed = 0;
		
		_armorsAllowed = set.getInteger("armorsAllowed", 0);
		
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		_isDebuff = set.getBool("isDebuff", isSkillTypeOffensive());
		
		_maxCharges = set.getInteger("maxCharges", 0);
		_numCharges = set.getInteger("numCharges", 0);
		_maxChargeConsume = set.getInteger("maxChargeConsume", 0);
		_triggeredId = set.getInteger("triggeredId", -1);
		_triggeredLevel = set.getInteger("triggeredLevel", 0);
		_chanceType = set.getString("chanceType", "");
		if (_chanceType != "" && !_chanceType.isEmpty())
			_chanceCondition = ChanceCondition.parse(set);
		
		_numSouls = set.getInteger("num_souls", 0);
		_soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
		_soulConsume = set.getInteger("soulConsumeCount", 0);
		_blowChance = set.getInteger("blowChance", 0);
		_expNeeded = set.getInteger("expNeeded", 0);
		_critChance = set.getInteger("critChance", 0);
		
		_transformId = set.getInteger("transformId", 0);
		_transformDuration = set.getInteger("transformDuration", 0);
		
		_isHeroSkill = HeroSkillTable.isHeroSkill(_id);
		_isGMSkill = GMSkillTable.isGMSkill(_id);
		
		_baseCritRate = set.getInteger("baseCritRate", (_skillType == L2SkillType.PDAM  || _skillType == L2SkillType.BLOW) ? 0 : -1);
		_lethalEffect1 = set.getInteger("lethal1",0);
		_lethalEffect2 = set.getInteger("lethal2",0);
		
		_directHpDmg  = set.getBool("dmgDirectlyToHp",false);
		_isDance = set.getBool("isDance",false);
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = Math.round(set.getFloat("aggroPoints", 0));
		_ignoredDefPercent = set.getFloat("ignoredDefPercent", 0.0f);
		_canBeUsedWhenDisabled = set.getBool("canBeUsedWhenDisabled", false);
		
		_flyType = set.getString("flyType", null);
		_flyRadius = set.getInteger("flyRadius", 0);
		_flyCourse = set.getFloat("flyCourse", 0);
		_canBeReflected = set.getBool("canBeReflected", true);
		_canBeSharedWithSummon = set.getBool("canBeSharedWithSummon", true);
		_canBeDispeled = set.getBool("canBeDispeled", true);
		
		_isClanSkill = set.getBool("isClanSkill", false);
		_excludedFromCheck = set.getBool("excludedFromCheck", false);
		_dependOnTargetBuff = set.getFloat("dependOnTargetBuff", 0);
		
		String dependOnTargetEffectId = set.getString("dependOnTargetEffectId", null);
		if (dependOnTargetEffectId != null)
		{
			String[] valuesSplit = dependOnTargetEffectId.split(",");
			_dependOnTargetEffectId = new int[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length;i++)
			{
				_dependOnTargetEffectId[i] = Integer.parseInt(valuesSplit[i]);
			}
		}
		else
			_dependOnTargetEffectId = new int[0];
		
		String damageDepend = set.getString("damageDepend", null);
		if (damageDepend != null)
		{
			String[] valuesSplit = damageDepend.split(",");
			_damageDepend = new double[valuesSplit.length];
			for (int i = 0; i < valuesSplit.length;i++)
			{
				_damageDepend[i] = Double.parseDouble(valuesSplit[i]);
			}
		}
		else
			_damageDepend = new double[0];
		
		_simultaneousCast = set.getBool("simultaneousCast", false);
		
		String capsuled_items = set.getString("capsuled_items_skill", null);
		if (capsuled_items != null)
		{
			if (capsuled_items.isEmpty())
				Log.warning("Empty Extractable Item Skill data in Skill Id: " + _id);
			
			_extractableItems = parseExtractableSkill(_id, _level, capsuled_items);
		}

		_partyChangeSkill = set.getInteger("partyChangeSkill", -1);
		_isCastedToParty = set.getBool("isCastedToParty", true);
		_skillActionId = set.getInteger("skillActionId", 0);
		_alterSkillId = set.getInteger("alterSkillId", -1);
		_alterSkillLevel = set.getInteger("alterSkillLevel", -1);
		_alterIconTime = set.getInteger("alterIconTime", -1);

		_isElemental = set.getBool("isElemental", false);
		_isStanceSwitch = set.getBool("isStanceSwitch", false);
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final boolean isPotion()
	{
		return _isPotion;
	}
	
	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final L2SkillType getSkillType()
	{
		return _skillType;
	}
	
	public final byte getElement()
	{
		return _element;
	}
	
	public final int getElementPower()
	{
		return _elementPower;
	}
	
	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR><BR>
	 *
	 */
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public final int getCondition()
	{
		return _condition;
	}
	
	public final boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean killByDOT()
	{
		return _killByDOT;
	}
	
	public final boolean absorbDOT()
	{
		return _absorbDOT;
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	public final boolean allowOnTransform()
	{
		return isPassive();
	}
	/**
	 * Return the power of the skill.<BR><BR>
	 */
	public final double getPower(L2Character activeChar, L2Character target, boolean isPvP, boolean isPvE)
	{
		if (activeChar == null)
			return getPower(isPvP, isPvE);
		
		switch (_skillType)
		{
			case DEATHLINK:
			{
				return getPower(isPvP, isPvE) * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
				/*
				 * DrHouse:
				 * Rolling back to old formula (look below) for DEATHLINK due to this one based on logarithm is not
				 * accurate enough. Commented here because probably is a matter of just adjusting a constant
				if (activeChar.getCurrentHp() / activeChar.getMaxHp() > 0.005)
					return _power*(-0.45*Math.log(activeChar.getCurrentHp()/activeChar.getMaxHp())+1.);
				else
					return _power*(-0.45*Math.log(0.005)+1.);
				 */
			}
			case FATAL:
			{
				return getPower(isPvP, isPvE)*3.5*(1-target.getCurrentHp()/target.getMaxHp());
			}
			default:
				return getPower(isPvP, isPvE);
		}
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final double getPower(boolean isPvP, boolean isPvE)
	{
		return isPvP ? _pvpPower : isPvE ? _pvePower : _power;
	}
	
	public final L2AbnormalType[] getNegateStats()
	{
		return _negateStats;
	}
	
	public final Map<String, Byte> getNegateAbnormals()
	{
		return _negateAbnormals;
	}
	
	public final int getAbnormalLvl()
	{
		return _abnormalLvl;
	}
	
	public final int[] getNegateId()
	{
		return _negateId;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}
	
	public final int getLevelDepend()
	{
		return _levelDepend;
	}
	
	/**
	 * Return true if skill should ignore all resistances
	 */
	public final boolean ignoreResists()
	{
		return _ignoreResists;
	}
	
	/**
	 * Return true if skill should ignore immunity
	 */
	public final boolean ignoreImmunity()
	{
		return _ignoreImmunity;
	}
	
	/**
	 * Return minimum skill/effect land rate (default is 1).
	 */
	public final int getMinChance()
	{
		return _minChance;
	}
	
	/**
	 * Return maximum skill/effect land rate (default is 99).
	 */
	public final int getMaxChance()
	{
		return _maxChance;
	}
	
	/**
	 * Return true if skill effects should be removed on any action except movement
	 */
	public final boolean isRemovedOnAction()
	{
		return _removedOnAction;
	}
	
	/**
	 * Return true if skill effects should be removed on damage
	 */
	public final boolean isRemovedOnDamage()
	{
		return _removedOnDamage;
	}
	
	public final int getRemovedOnDamageChance()
	{
		return _removedOnDamageChance;
	}
	
	public final int getStrikesToRemove()
	{
		return _strikesToRemove;
	}
	
	/**
	 * Return true if skill effects should be removed on debuff block
	 */
	public final boolean isRemovedOnDebuffBlock()
	{
		return _removedOnDebuffBlock;
	}
	
	public final int getDebuffBlocksToRemove()
	{
		return _debuffBlocksToRemove;
	}
	
	/**
	 * Return the additional effect Id.<BR><BR>
	 */
	public final int getEffectId()
	{
		return _effectId;
	}
	/**
	 * Return the additional effect level.<BR><BR>
	 */
	public final int getEffectLvl()
	{
		return _effectLvl;
	}
	
	public final int getEffectAbnormalLvl()
	{
		return _effectAbnormalLvl;
	}
	
	/**
	 * Return true if character should attack target after skill
	 */
	public final boolean nextActionIsAttack()
	{
		return _nextActionIsAttack;
	}
	
	public final boolean nextActionIsAttackMob()
	{
		return _nextActionIsAttackMob;
	}

	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration()
	{
		return _buffDuration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}
	
	/**
	 * @return Returns the cpConsume;
	 */
	public final int getCpConsume()
	{
		return _cpConsume;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		return _effectRange;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}
	
	/**
	 * @return Returns the id.
	 */
	public final int getId()
	{
		return _id;
	}
	
	/**
	 * @return Returns the boolean _isDebuff.
	 */
	public final boolean isDebuff()
	{
		return _isDebuff;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public void setDisplayId(int id)
	{
		_displayId = id;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}
	
	/**
	 * Return skill saveVs base stat (STR, INT ...).<BR><BR>
	 */
	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}
	
	/**
	 * @return Returns the _targetConsumeId.
	 */
	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}
	
	/**
	 * @return Returns the targetConsume.
	 */
	public final int getTargetConsume()
	{
		return _targetConsume;
	}
	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}
	
	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	/**
	 * @return Returns the fameConsume.
	 */
	public final int getFameConsume()
	{
		return _fameConsume;
	}
	
	/**
	 * @return Returns the clanRepConsume.
	 */
	public final int getClanRepConsume()
	{
		return _clanRepConsume;
	}
	
	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}

	public final int getEnchantRouteId()
	{
		return _enchantRouteId;
	}

	public final int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public final int getLevelHash()
	{
		return _level + (getEnchantHash() << 16);
	}
	
	public final int getEnchantHash()
	{
		return _enchantRouteId * 1000 + _enchantLevel;
	}
	
	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}
	
	/**
	 * @return Returns true to set static reuse.
	 */
	public final boolean isStaticReuse()
	{
		return _staticReuse;
	}
	
	/**
	 * @return Returns true to set static hittime.
	 */
	public final boolean isStaticHitTime()
	{
		return _staticHitTime;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public final int getReuseHashCode()
	{
		return _reuseHashCode;
	}
	
	public final int getEquipDelay()
	{
		return _equipDelay;
	}
	
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	public final int getHitCounts()
	{
		return _hitTimings.length;
	}
	
	public final int[] getHitTimings()
	{
		return _hitTimings;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	public final int getSkillRadius()
	{
		return _skillRadius;
	}
	
	public final int getSkillSafeRadius()
	{
		return _skillSafeRadius;
	}
	
	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	
	public final boolean isChance()
	{
		return _chanceCondition != null && isPassive();
	}
	
	public final boolean isDance()
	{
		return _isDance;
	}
	
	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}
	
	public final float getSSBoost()
	{
		return _sSBoost;
	}
	
	public final int getAggroPoints()
	{
		return _aggroPoints;
	}
	
	public final float getIgnoredDefPercent()
	{
		return _ignoredDefPercent;
	}
	
	public boolean canBeUsedWhenDisabled()
	{
		return _canBeUsedWhenDisabled;
	}
	
	public final boolean useSoulShot()
	{
		switch (getSkillType())
		{
			case PDAM:
			case BLOW:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean useSpiritShot()
	{
		return isMagic();
	}
	public final boolean useFishShot()
	{
		return ((getSkillType() == L2SkillType.PUMPING) || (getSkillType() == L2SkillType.REELING) );
	}
	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public int getMinPledgeClass() { return _minPledgeClass;  }
	
	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
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
	
	public final boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public final boolean isNeutral()
	{
		return _isNeutral;
	}
	
	public final boolean isHeroSkill()
	{
		return _isHeroSkill;
	}
	
	public final boolean isGMSkill()
	{
		return _isGMSkill;
	}
	
	public final int getNumCharges()
	{
		return _numCharges;
	}
	
	public final int getMaxChargeConsume()
	{
		return _maxChargeConsume;
	}
	
	public final int getNumSouls()
	{
		return _numSouls;
	}
	
	public final int getMaxSoulConsumeCount()
	{
		return _soulMaxConsume;
	}
	
	public final int getSoulConsumeCount()
	{
		return _soulConsume;
	}
	
	public final int getExpNeeded()
	{
		return _expNeeded;
	}
	
	public final int getCritChance()
	{
		return _critChance;
	}
	
	public final int getTransformId()
	{
		return _transformId;
	}
	
	public final int getTransformDuration()
	{
		return _transformDuration;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}
	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}
	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}
	
	public final String getFlyType()
	{
		return _flyType;
	}
	
	public final int getFlyRadius()
	{
		return _flyRadius;
	}
	
	public final float getFlyCourse()
	{
		return _flyCourse;
	}
	
	public final boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
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
			case MARK:
				return true;
			case DUMMY:
				if (_id == 998) // blazing boost
					return true;
			default:
				return this.isDebuff();
		}
	}
	
	public final boolean isStayAfterDeath()
	{
		return _stayAfterDeath;
	}
	
	public final boolean getWeaponDependancy(L2Character activeChar)
	{
		if (getWeaponDependancy(activeChar,false))
		{
			return true;
		}
		else
		{
			SystemMessage message = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			message.addSkillName(this);
			activeChar.sendPacket(message);
			
			return false;
		}
	}
	
	public final boolean getWeaponDependancy(L2Character activeChar, boolean chance)
	{
		int weaponsAllowed = getWeaponsAllowed();
		//check to see if skill has a weapon dependency.
		if (weaponsAllowed == 0)
			return true;
		
		int mask = 0;
		
		if (activeChar instanceof L2MonsterInstance
				&& ((L2MonsterInstance)activeChar).getClonedPlayer() != null)
			return true;
		
		if (activeChar.getActiveWeaponItem() != null)
			mask |= activeChar.getActiveWeaponItem().getItemType().mask();
		if (activeChar.getSecondaryWeaponItem() != null && activeChar.getSecondaryWeaponItem() instanceof L2Armor)
			mask |= ((L2ArmorType) activeChar.getSecondaryWeaponItem().getItemType()).mask();
		
		if ((mask & weaponsAllowed) != 0)
			return true;
		
		return false;
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if (activeChar.isGM() && !Config.GM_SKILL_RESTRICTION)
			return true;
		if ((getCondition() & L2Skill.COND_SHIELD) != 0)
		{
			/*
			 L2Armor armorPiece;
			 L2ItemInstance dummy;
			 dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			 armorPiece = (L2Armor) dummy.getItem();
			 */
			//TODO add checks for shield here.
		}
		
		List<Condition> preCondition = _preCondition;
		if (itemOrWeapon) preCondition = _itemPreCondition;
		if (preCondition == null || preCondition.isEmpty()) return true;
		
		for (Condition cond : preCondition)
		{
			Env env = new Env();
			env.player = activeChar;
			if (target instanceof L2Character) // TODO: object or char?
				env.target = (L2Character)target;
			env.skill = this;
			
			if (!cond.test(env))
			{
				String msg = cond.getMessage();
				int msgId = cond.getMessageId();
				if (msgId != 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(msgId);
					if (cond.isAddName())
						sm.addSkillName(_id);
					activeChar.sendPacket(sm);
				}
				else if (msg != null)
				{
					activeChar.sendMessage(msg);
				}
				return false;
			}
		}
		return true;
	}
	
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		// Init to null the target of the skill
		L2Character target = null;
		
		// Get the L2Objcet targeted by the user of the skill at this moment
		L2Object objTarget = activeChar.getTarget();
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		return getTargetList(activeChar, onlyFirst, target);
	}
	
	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR><BR>
	 *
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
	 *
	 */
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();
		
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		SkillTargetType targetType = getTargetType();
		
		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		L2SkillType skillType = getSkillType();
		
		switch (targetType)
		{
			// The skill can only be used on the L2Character targeted, or on the caster itself
			case TARGET_ONE:
			case TARGET_PVP:
			{
				boolean canTargetSelf = false;
				switch (skillType)
				{
					case BUFF:
					case HEAL:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANA_BY_LEVEL:
					case MANAHEAL:
					case NEGATE:
					case CANCEL_DEBUFF:
					case COMBATPOINTHEAL:
					case BALANCE_LIFE:
					case HPMPCPHEAL_PERCENT:
					case HPMPHEAL_PERCENT:
					case HPCPHEAL_PERCENT:
					case CHAIN_HEAL:
					case OVERHEAL:
					case OVERHEAL_STATIC:
					case SUMMON:
						canTargetSelf = true;
						break;
				}
				
				// Check for null target or any other invalid target
				if (target == null || target.isDead() || (target == activeChar && !canTargetSelf))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				if (getId() == 1323 && target instanceof L2PcInstance && ((L2PcInstance)target).isInEvent())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				if (targetType == SkillTargetType.TARGET_PVP && target.getActingPlayer() == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new L2Character[] {target};
			}
			case TARGET_SELF:
			case TARGET_GROUND:
			{
				return new L2Character[] {activeChar};
			}
			case TARGET_HOLY:
			{
				if (activeChar instanceof L2PcInstance)
				{
					if (target instanceof L2ArtefactInstance)
						return new L2Character[] {target};
				}
				
				return _emptyTargetList;
			}
			case TARGET_FLAGPOLE:
			{
				return new L2Character[] {activeChar};
			}
			case TARGET_SUMMON:
			{
				if (!(target instanceof L2Summon))
					target = ((L2PcInstance)activeChar).getSummon(0);
				if (target != null && !target.isDead() && target instanceof L2Summon && ((L2PcInstance)activeChar).getSummons().contains(target))
					return new L2Character[] {target};
				
				return _emptyTargetList;
			}
			case TARGET_ALL_SUMMONS:
			{
				if (!(activeChar instanceof L2PcInstance))
					return _emptyTargetList;
				
				//LasTravel: Servitor Balance Life should balance owner too
				if (getId() == 11299)
				{
					targetList.add(activeChar);
				}
				
				for (L2Summon summon : ((L2PcInstance)activeChar).getSummons())
				{
					if (!summon.isDead())
						targetList.add(summon);
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_OWNER_PET:
			{
				if (activeChar instanceof L2Summon)
				{
					target = ((L2Summon)activeChar).getOwner();
					if (target != null && !target.isDead())
						return new L2Character[]{target};
				}
				
				return _emptyTargetList;
			}
			case TARGET_CORPSE_PET:
			{
				if (activeChar instanceof L2PcInstance)
				{
					target = ((L2PcInstance)activeChar).getPet();
					if (target != null && target.isDead())
						return new L2Character[]{target};
				}
				
				return _emptyTargetList;
			}
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_PVP_AURA:
			{
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				
				// Tenkai custom - in Duels, area skills attack only Duel enemy. Not checking if same Duel ID, but whatever
				if ((activeChar instanceof L2PcInstance && ((L2PcInstance)activeChar).isInDuel())
					|| (activeChar instanceof L2SummonInstance && ((L2SummonInstance)activeChar).getOwner().isInDuel()))
				{
					if (target != activeChar && target instanceof L2PcInstance && ((L2PcInstance)target).isInDuel())
						return new L2Object[]{target};
					else
						return _emptyTargetList;
				}
				
				final L2PcInstance sourcePlayer = activeChar.getActingPlayer();
					
				// Go through the L2Character _knownList
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(getSkillRadius());
				
				//LASTRAVEL
				//Some NEW area skills only have effect on players what are not closet to the caster, but yes on the rest of long-range players, this is a TEMP way for try to do this...
				if (getSkillSafeRadius() > 0)
				{
					//Delete the characters from the final list
					List<L2Character> toIterate = new ArrayList<L2Character>(objs);
					for (L2Character xObjs : toIterate)
					{
						if (activeChar.isInsideRadius(xObjs, getSkillSafeRadius(), false, false))
							objs.remove(xObjs);
					}
				}
				
				//synchronized (activeChar.getKnownList().getKnownObjects())
				if (getSkillType() == L2SkillType.DUMMY)
				{
					if (onlyFirst)
						return new L2Character[] { activeChar };
					
					targetList.add(activeChar);
					for (L2Character obj : objs)
					{
						if (!(obj == activeChar
								|| obj == sourcePlayer
								|| obj instanceof L2Npc
								|| obj instanceof L2Attackable))
							continue;
						targetList.add(obj);
					}
				}
				else
				{
					for (L2Character obj : objs)
					{
						if (obj instanceof L2Attackable || obj instanceof L2Playable)
						{
							switch (targetType)
							{
								case TARGET_FRONT_AURA:
									if (!obj.isInFrontOf(activeChar))
										continue;
									break;
								case TARGET_BEHIND_AURA:
									if (!obj.isBehind(activeChar))
										continue;
								case TARGET_PVP_AURA:
									if (!(obj instanceof L2PcInstance))
										continue;
									break;
							}
							
							if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
								continue;
							
							if (onlyFirst)
								return new L2Character[] { obj };
							
							targetList.add(obj);
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA_SUMMON:
			{
				if (!(activeChar instanceof L2PcInstance))
					return _emptyTargetList;
				
				target = ((L2PcInstance)activeChar).getSummon(0);
				if (target == null || !(target instanceof L2SummonInstance) || target.isDead())
					return _emptyTargetList;
				
				if (onlyFirst)
					return new L2Character[]{target};
				
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				final Collection<L2Character> objs = target.getKnownList().getKnownCharacters();
				final int radius = getSkillRadius();
				
				for (L2Character obj : objs)
				{
					if (obj == null || obj == target || obj == activeChar)
						continue;
					
					if (!Util.checkIfInRange(radius, target, obj, true))
						continue;
					
					if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
						continue;
					
					if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
						continue;
					
					targetList.add(obj);
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA:
			case TARGET_FRONT_AREA:
			case TARGET_BEHIND_AREA:
			case TARGET_PVP_AREA:
			{
				if (((target == null || target == activeChar || target.isAlikeDead()) && getCastRange() >= 0) ||
						(!(target instanceof L2Attackable || target instanceof L2Playable)))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				// Tenkai custom - in Duels, area skills attack only Duel enemy. Not checking if same Duel ID, but whatever
				if ((activeChar instanceof L2PcInstance && ((L2PcInstance)activeChar).isInDuel())
					|| (activeChar instanceof L2SummonInstance && ((L2SummonInstance)activeChar).getOwner().isInDuel()))
				{
					if (target instanceof L2PcInstance && ((L2PcInstance)target).isInDuel())
						return new L2Object[]{target};
					else
						return _emptyTargetList;
				}
				
				final L2Character origin;
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				final int radius = getSkillRadius();
				
				if (getCastRange() >= 0)
				{
					// Why should it check for the original target?
					//if (!checkForAreaOffensiveSkills(activeChar, target, this, srcInArena))
					//	return _emptyTargetList;
					
					if (onlyFirst)
						return new L2Character[]{target};
					
					origin = target;
					targetList.add(origin); // Add target to target list
				}
				else
					origin = activeChar;
				
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2Character obj : objs)
					{
						if (!(obj instanceof L2Attackable
								|| obj instanceof L2Playable))
							continue;
						
						if (obj == origin)
							continue;
						
						if (Util.checkIfInRange(radius, origin, obj, true))
						{
							switch (targetType)
							{
								case TARGET_FRONT_AREA:
									if (!obj.isInFrontOf(activeChar))
										continue;
									break;
								case TARGET_BEHIND_AREA:
									if (!obj.isBehind(activeChar))
										continue;
									break;
								case TARGET_PVP_AREA:
									if (!(obj instanceof L2PcInstance))
										continue;
									break;
							}
							
							if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
								continue;
							
							targetList.add(obj);
						}
					}
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PARTY:
			case TARGET_PARTY:
			{
				final boolean isCorpseType = targetType == SkillTargetType.TARGET_CORPSE_PARTY;
				
				if (!isCorpseType)
				{
					if (onlyFirst)
						return new L2Character[] {activeChar};
					
					targetList.add(activeChar);
				}
				
				final int radius = getSkillRadius();
				
				L2PcInstance player = activeChar.getActingPlayer();
				if (activeChar instanceof L2Summon)
				{
					if (addCharacter(activeChar, player, radius, isCorpseType))
						targetList.add(player);
				}
				else if (activeChar instanceof L2PcInstance)
				{
					if (addCharacter(activeChar, player.getPet(), radius, isCorpseType))
						targetList.add(player.getPet());
					for (L2SummonInstance summon : player.getSummons())
					{
						if (addCharacter(activeChar, summon, radius, isCorpseType))
							targetList.add(summon);
					}
				}
				
				if (activeChar.isInParty())
				{
					// Get a list of Party Members
					for (L2PcInstance partyMember : activeChar.getParty().getPartyMembers())
					{
						if (partyMember == null || partyMember == player)
							continue;
						
						if (partyMember.isInEvent()
								&& (player.getEvent() != partyMember.getEvent() || player.getEvent().getConfig().isAllVsAll()))
							continue;
						
						if (addCharacter(activeChar, partyMember, radius, isCorpseType))
							targetList.add(partyMember);
						
						if (addCharacter(activeChar, partyMember.getPet(), radius, isCorpseType))
							targetList.add(partyMember.getPet());
						for (L2SummonInstance summon : partyMember.getSummons())
						{
							if (addCharacter(activeChar, summon, radius, isCorpseType))
								targetList.add(summon);
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if (activeChar instanceof L2NpcInstance)
					 return new L2Character[]{activeChar.getTarget().getActingPlayer()};
				if ((target != null
						&& target == activeChar)
						|| (target != null
								&& activeChar.isInParty()
								&& target.isInParty()
								&& activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
								|| (target != null
										&& activeChar instanceof L2PcInstance
										&& target instanceof L2Summon
										&& ((L2PcInstance)activeChar).getPet() == target
										&& ((L2PcInstance)activeChar).getSummons().contains(target))
										|| (target != null
												&& activeChar instanceof L2Summon
												&& target instanceof L2PcInstance
												&& target == ((L2Summon)activeChar).getOwner()))
				{
					if (!target.isDead())
					{
						// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
						return new L2Character[]{target};
					}
					else
						return _emptyTargetList;
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
			}
			case TARGET_PARTY_OTHER:
			{
				if (target != null && target != activeChar
						&& activeChar.isInParty() && target.isInParty()
						&& activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
				{
					if (!target.isDead())
					{
						if (target instanceof L2PcInstance)
						{
							switch (getId())
							{
								// FORCE BUFFS may cancel here but there should be a proper condition
								case 426:
									if (!((L2PcInstance)target).isMageClass())
										return new L2Character[]{target};
									else
										return _emptyTargetList;
								case 427:
									if (((L2PcInstance)target).isMageClass())
										return new L2Character[]{target};
									else
										return _emptyTargetList;
							}
						}
						return new L2Character[]{target};
					}
					else
						return _emptyTargetList;
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (activeChar instanceof L2Playable)
				{
					final L2PcInstance player = activeChar.getActingPlayer();
					
					if (player == null)
						return _emptyTargetList;
					
					if (player.isInOlympiadMode())
						return new L2Character[] {player};
					
					final boolean isCorpseType = targetType == SkillTargetType.TARGET_CORPSE_ALLY;
					
					if (!isCorpseType)
					{
						if (onlyFirst)
							return new L2Character[] {player};
						
						targetList.add(player);
					}
					
					final int radius = getSkillRadius();

					if (addCharacter(activeChar, player.getPet(), radius, isCorpseType))
						targetList.add(player.getPet());
					for (L2SummonInstance summon : player.getSummons())
					{
						if (addCharacter(activeChar, summon, radius, isCorpseType))
							targetList.add(summon);
					}
					
					if (player.getClan() != null)
					{
						// Get all visible objects in a spherical area near the L2Character
						final Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
						//synchronized (activeChar.getKnownList().getKnownObjects())
						{
							for (L2PcInstance obj : objs)
							{
								if (obj == null)
									continue;
								if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId())
										&& (obj.getClan() == null || obj.getClanId() != player.getClanId()))
									continue;
								
								if (player.isInDuel())
								{
									if (player.getDuelId() != obj.getDuelId())
										continue;
									if (player.isInParty() && obj.isInParty() && player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID())
										continue;
								}
								
								// Don't add this target if this is a Pc->Pc pvp
								// casting and pvp condition not met
								if (!player.checkPvpSkill(obj, this))
									continue;
								
								if (obj.isInEvent() && (player.getEvent() != obj.getEvent() || player.getEvent().getConfig().isAllVsAll()))
									continue;
								
								if (!onlyFirst)
								{
									if (addCharacter(activeChar, obj.getPet(), radius, isCorpseType))
										targetList.add(obj.getPet());
									for (L2SummonInstance summon : obj.getSummons())
									{
										if (addCharacter(activeChar, summon, radius, isCorpseType))
											targetList.add(summon);
									}
								}
								
								if (!addCharacter(activeChar, obj, radius, isCorpseType))
									continue;
								
								if (isCorpseType)
								{
									// Siege battlefield resurrect has been made possible for participants
									if (getSkillType() == L2SkillType.RESURRECT)
									{
										if (obj.isInsideZone(L2Character.ZONE_SIEGE) && !obj.isInSiege())
											continue;
									}
								}
								
								if (onlyFirst)
									return new L2Character[] { obj };
								
								targetList.add(obj);
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (activeChar instanceof L2Playable)
				{
					if (activeChar instanceof L2MobSummonInstance)
						return new L2Character[] {activeChar};
					
					final L2PcInstance player = activeChar.getActingPlayer();
					
					if (player == null)
						return _emptyTargetList;
					
					if (player.isInOlympiadMode())
						return new L2Character[] {player};
					
					final boolean isCorpseType = targetType == SkillTargetType.TARGET_CORPSE_CLAN;
					
					if (!isCorpseType)
					{
						if (onlyFirst)
							return new L2Character[] {player};
						
						targetList.add(player);
					}
					
					final int radius = getSkillRadius();
					final L2Clan clan = player.getClan();
					
					if (addCharacter(activeChar, player.getPet(), radius, isCorpseType))
						targetList.add(player.getPet());
					for (L2SummonInstance summon : player.getSummons())
					{
						if (addCharacter(activeChar, summon, radius, isCorpseType))
							targetList.add(summon);
					}
					
					if (clan != null)
					{
						L2PcInstance obj;
						// Get Clan Members
						for (L2ClanMember member : clan.getMembers())
						{
							obj = member.getPlayerInstance();
							
							if (obj == null || obj == player)
								continue;
							
							if (player.isInDuel())
							{
								if (player.getDuelId() != obj.getDuelId())
									continue;
								if (player.isInParty() && obj.isInParty() && player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID())
									continue;
							}
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(obj, this))
								continue;
							
							if (obj.isInEvent() && (player.getEvent() != obj.getEvent() || player.getEvent().getConfig().isAllVsAll()))
								continue;
							
							if (!onlyFirst)
							{
								if (addCharacter(activeChar, obj.getPet(), radius, isCorpseType))
									targetList.add(obj.getPet());
								for (L2SummonInstance summon : obj.getSummons())
								{
									if (addCharacter(activeChar, summon, radius, isCorpseType))
										targetList.add(summon);
								}
							}
							
							if (!addCharacter(activeChar, obj, radius, isCorpseType))
								continue;
							
							if (isCorpseType)
							{
								if (getSkillType() == L2SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									if (obj.isInsideZone(L2Character.ZONE_SIEGE) && !obj.isInSiege())
										continue;
								}
							}
							
							if (onlyFirst)
								return new L2Character[] {obj};
							
							targetList.add(obj);
						}
					}
				}
				else if (activeChar instanceof L2Npc)
				{
					// for buff purposes, returns friendly mobs nearby and mob itself
					final L2Npc npc = (L2Npc) activeChar;
					if (npc.getFactionId() == null || npc.getFactionId().isEmpty())
					{
						return new L2Character[]{activeChar};
					}
					targetList.add(activeChar);
					final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					//synchronized (activeChar.getKnownList().getKnownObjects())
					{
						for (L2Object newTarget : objs)
						{
							if (newTarget instanceof L2Npc
									&& npc.getFactionId().equals(((L2Npc) newTarget).getFactionId()))
							{
								if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
									continue;
								targetList.add((L2Npc) newTarget);
							}
						}
					}
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PARTY_CLAN:
			case TARGET_PARTY_CLAN:
			{
				if (onlyFirst)
					return new L2Character[]{activeChar};
				
				final L2PcInstance player = activeChar.getActingPlayer();
				
				if (player == null)
					return _emptyTargetList;
				
				final boolean isCorpseType = targetType == SkillTargetType.TARGET_CORPSE_PARTY_CLAN;
				
				targetList.add(player);
				
				final int radius = getSkillRadius();
				final boolean hasClan = player.getClan() != null;
				final boolean hasParty = player.isInParty();

				if (addCharacter(activeChar, player.getPet(), radius, isCorpseType))
					targetList.add(player.getPet());
				for (L2SummonInstance summon : player.getSummons())
				{
					if (addCharacter(activeChar, summon, radius, isCorpseType))
						targetList.add(summon);
				}
				
				// if player in clan and not in party
				if (!(hasClan || hasParty))
					return targetList.toArray(new L2Character[targetList.size()]);
				
				// Get all visible objects in a spherical area near the L2Character
				final Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2PcInstance obj : objs)
					{
						if (obj == null)
							continue;

						// olympiad mode - adding only own side
						if (player.isInOlympiadMode())
						{
							if (!obj.isInOlympiadMode())
								continue;
							if (player.getOlympiadGameId() != obj.getOlympiadGameId())
								continue;
							if (player.getOlympiadSide() != obj.getOlympiadSide())
								continue;
						}

						if (player.isInDuel())
						{
							if (player.getDuelId() != obj.getDuelId())
								continue;
							
							if (hasParty && obj.isInParty() && player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID())
								continue;
						}
						
						if (!((hasClan && obj.getClanId() == player.getClanId())
								|| (hasParty && obj.isInParty() && player.getParty().getPartyLeaderOID() == obj.getParty().getPartyLeaderOID())))
							continue;
						
						// Don't add this target if this is a Pc->Pc pvp
						// casting and pvp condition not met
						if (!player.checkPvpSkill(obj, this))
							continue;
						
						if (obj.isInEvent() && player.getEvent() != obj.getEvent())
							continue;
						
						if (!onlyFirst)
						{
							if (addCharacter(activeChar, obj.getPet(), radius, isCorpseType))
								targetList.add(obj.getPet());
							for (L2SummonInstance summon : obj.getSummons())
							{
								if (addCharacter(activeChar, summon, radius, isCorpseType))
									targetList.add(summon);
							}
						}
						
						if (!addCharacter(activeChar, obj, radius, isCorpseType))
							continue;
						
						if (onlyFirst)
							return new L2Character[] { obj };
						
						targetList.add(obj);
					}
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if (target != null && target.isDead())
				{
					final L2PcInstance player;
					if (activeChar instanceof L2PcInstance)
					{
						player = (L2PcInstance) activeChar;
						if (player.isInEvent())
							return _emptyTargetList;
					}
					else
						player = null;
					
					final L2PcInstance targetPlayer;
					if (target instanceof L2PcInstance)
						targetPlayer = (L2PcInstance) target;
					else
						targetPlayer = null;
					
					final L2PetInstance targetPet;
					if (target instanceof L2PetInstance)
						targetPet = (L2PetInstance) target;
					else
						targetPet = null;
					
					if ( (targetPlayer != null || targetPet != null))//TODO
					{
						boolean condGood = true;
						
						if (getSkillType() == L2SkillType.RESURRECT)
						{
							if (targetPlayer != null)
							{
								// check target is not in a active siege zone
								if (targetPlayer.isInsideZone(L2Character.ZONE_SIEGE) && !targetPlayer.isInSiege())
								{
									condGood = false;
									activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
								}
								
								if (targetPlayer.isReviveRequested())
								{
									if (targetPlayer.isRevivingPet())
										player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									else
										player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
									condGood = false;
								}
							}
							else if (targetPet != null)
							{
								if (targetPet.getOwner() != player)
								{
									if (targetPet.getOwner().isReviveRequested())
									{
										if (targetPet.getOwner().isRevivingPet())
											player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
										else
											player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_RES_PET2)); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
										condGood = false;
									}
								}
							}
						}
						
						if (condGood)
						{
							if (!onlyFirst)
							{
								targetList.add(target);
								return targetList.toArray(new L2Object[targetList.size()]);
							}
							else
								return new L2Character[] {target};
						}
					}
				}
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return _emptyTargetList;
			}
			case TARGET_CORPSE_MOB:
			{
				final boolean isSummon = target instanceof L2SummonInstance;
				if (target == null || !target.isDead() || !(target instanceof L2Attackable || isSummon))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				// Corpse mob only available for half time
				switch (getSkillType())
				{
					case SUMMON:
					{
						if (isSummon && ((L2SummonInstance)target).getOwner() != null
								&& ((L2SummonInstance)target).getOwner().getObjectId() == activeChar.getObjectId())
							return _emptyTargetList;
					}
					case DRAIN:
					{
						if (DecayTaskManager.getInstance().getTasks().containsKey(target)
								&& (System.currentTimeMillis() - DecayTaskManager.getInstance().getTasks().get(target)) > DecayTaskManager.ATTACKABLE_DECAY_TIME / 2)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED));
							return _emptyTargetList;
						}
					}
				}
				
				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				else
					return new L2Character[] {target};
				
			}
			case TARGET_AURA_CORPSE_MOB:
			{
				// Go through the L2Character _knownList
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharactersInRadius(getSkillRadius());
				for (L2Character obj : objs)
				{
					if (obj instanceof L2Attackable && obj.isDead())
					{
						if (onlyFirst)
							return new L2Character[] { obj };
						
						targetList.add(obj);
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if (!target.isDead() || !(target instanceof L2Attackable || target instanceof L2Summon))	// Tenkai custom - allow dead servitors
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				if (onlyFirst)
					return new L2Character[] {target};
				
				targetList.add(target);
				
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				
				final int radius = getSkillRadius();
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2Character obj : objs)
					{
						if (!(obj instanceof L2Attackable || obj instanceof L2Playable)
								|| !Util.checkIfInRange(radius, target, obj, true))
							continue;
						
						if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
							continue;
						
						targetList.add(obj);
					}
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
				{
					//activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				if (!onlyFirst)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				else return new L2Character[] {target};
				
			}
			case TARGET_UNDEAD:
			{
				if (target instanceof L2Npc || target instanceof L2SummonInstance)
				{
					if (!target.isUndead() || target.isDead())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return _emptyTargetList;
					}
					
					if (!onlyFirst)
						targetList.add(target);
					else
						return new L2Character[] {target};
					
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
			}
			case TARGET_AREA_UNDEAD:
			{
				final L2Character cha;
				final int radius = getSkillRadius();
				if (getCastRange() >= 0 && (target instanceof L2Npc || target instanceof L2SummonInstance)
						&& target.isUndead() && !target.isAlikeDead())
				{
					cha = target;
					
					if (!onlyFirst)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[] {cha};
					
				}
				else cha = activeChar;
				
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				//synchronized (cha.getKnownList().getKnownObjects())
				{
					for (L2Character obj : objs)
					{
						if (!Util.checkIfInRange(radius, cha, obj, true))
							continue;
						if (obj instanceof L2Npc)
							target = obj;
						else if (obj instanceof L2SummonInstance)
							target = obj;
						else
							continue;
						
						if (!target.isAlikeDead()) // If target is not
							// dead/fake death and not
							// self
						{
							if (!target.isUndead())
								continue;
							
							if (geoEnabled && !GeoData.getInstance().canSeeTarget(activeChar, target))
								continue;
							
							if (!onlyFirst)
								targetList.add(obj);
							else
								return new L2Character[] { obj };
						}
					}
				}
				
				if (targetList.isEmpty()) return _emptyTargetList;
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target instanceof L2Summon)
				{
					L2Summon targetSummon = (L2Summon)target;
					if (activeChar instanceof L2PcInstance && ((L2PcInstance)activeChar).getPet() != targetSummon
							&& !targetSummon.isDead() && !((L2PcInstance)activeChar).getSummons().contains(targetSummon)
							&& (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getReputation() < 0)
							|| (targetSummon.getOwner().isInsideZone(L2Character.ZONE_PVP) && ((L2PcInstance)activeChar).isInsideZone(L2Character.ZONE_PVP))
							|| (targetSummon.getOwner().isInDuel() && ((L2PcInstance)activeChar).isInDuel() && targetSummon.getOwner().getDuelId() == ((L2PcInstance)activeChar).getDuelId()))
						return new L2Character[]{targetSummon};
				}
				return _emptyTargetList;
			}
			case TARGET_PARTY_NOTME:
			case TARGET_ALLY_NOTME:
			{
				//target all party members except yourself
				if (onlyFirst)
					return new L2Character[] { activeChar };
				
				L2PcInstance player = null;
				
				if (activeChar instanceof L2Summon)
				{
					player = ((L2Summon) activeChar).getOwner();
					targetList.add(player);
				}
				else if (activeChar instanceof L2PcInstance)
				{
					player = (L2PcInstance) activeChar;
					if (((L2PcInstance)activeChar).getPet() != null)
						targetList.add(((L2PcInstance)activeChar).getPet());
					for (L2SummonInstance summon : ((L2PcInstance)activeChar).getSummons())
						targetList.add(summon);
				}
				
				if (activeChar.getParty() != null)
				{
					List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();
					for (L2PcInstance partyMember : partyList)
					{
						if (partyMember == null || partyMember == player)
							continue;
						
						if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
						{
							targetList.add(partyMember);
							
							if (partyMember.getPet() != null && !partyMember.getPet().isDead())
								targetList.add(partyMember.getPet());
							
							for (L2SummonInstance summon : partyMember.getSummons())
							{
								if (!summon.isDead())
									targetList.add(summon);
							}
						}
					}
				}
				
				if (targetType == SkillTargetType.TARGET_ALLY_NOTME)
				{
					if (player != null)
					{
						final int radius = getSkillRadius();

						if (addCharacter(activeChar, player.getPet(), radius, false))
							targetList.add(player.getPet());
						for (L2SummonInstance summon : player.getSummons())
						{
							if (addCharacter(activeChar, summon, radius, false))
								targetList.add(summon);
						}
						
						if (player.getClan() != null)
						{
							// Get all visible objects in a spherical area near the L2Character
							final Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
							//synchronized (activeChar.getKnownList().getKnownObjects())
							{
								for (L2PcInstance obj : objs)
								{
									if (obj == null || obj == player)
										continue;
									
									if ((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId())
											&& (obj.getClan() == null || obj.getClanId() != player.getClanId()))
										continue;
									
									if (player.isInDuel())
									{
										if (player.getDuelId() != obj.getDuelId())
											continue;
										
										if (player.isInParty() && obj.isInParty() && player.getParty().getPartyLeaderOID() != obj.getParty().getPartyLeaderOID())
											continue;
									}
									
									// Don't add this target if this is a Pc->Pc pvp
									// casting and pvp condition not met
									if (!player.checkPvpSkill(obj, this))
										continue;
									
									if (obj.isInEvent() && (player.getEvent() != obj.getEvent() || player.getEvent().getConfig().isAllVsAll()))
										continue;
									
									if (!onlyFirst)
									{
										if (addCharacter(activeChar, obj.getPet(), radius, false))
											targetList.add(obj.getPet());
										for (L2SummonInstance summon : obj.getSummons())
										{
											if (addCharacter(activeChar, summon, radius, false))
												targetList.add(summon);
										}
									}
									
									if (!addCharacter(activeChar, obj, radius, false))
										continue;
									
									targetList.add(obj);
								}
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			// npc only for now - untested
			case TARGET_CLAN_MEMBER:
			{
				if (activeChar instanceof L2Npc)
				{
					// for buff purposes, returns friendly mobs nearby and mob itself
					final L2Npc npc = (L2Npc) activeChar;
					if (npc.getFactionId() == null || npc.getFactionId().isEmpty())
					{
						return new L2Character[]{activeChar};
					}
					final Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
					for (L2Object newTarget : objs)
					{
						if (newTarget instanceof L2Npc
								&& npc.getFactionId().equals(((L2Npc) newTarget).getFactionId()))
						{
							if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
								continue;
							if (((L2Npc) newTarget).getFirstEffect(this) != null)
								continue;
							targetList.add((L2Npc) newTarget);
							break; // found
						}
					}
					if (targetList.isEmpty())
						targetList.add(npc);
				}
				else if (activeChar instanceof L2PcInstance)
				{
					if (target instanceof L2PcInstance)
					{
						final L2PcInstance targetPlayer = (L2PcInstance)target;
						final L2PcInstance casterPlayer = (L2PcInstance)activeChar;
						
						//Dummy checks
						if (targetPlayer == casterPlayer  || (targetPlayer.getClan() != null && targetPlayer.getClanId() == casterPlayer.getClanId())
								|| (targetPlayer.isInParty() && casterPlayer.isInParty() &&
								targetPlayer.getParty().getPartyLeaderOID() == casterPlayer.getParty().getPartyLeaderOID() && 
								targetPlayer.getClan() != null && casterPlayer.getClan() != null && targetPlayer.getClanId() == casterPlayer.getClanId() &&
								!targetPlayer.isInOlympiadMode() && !casterPlayer.isInOlympiadMode() &&
								!targetPlayer.isInEvent() && !casterPlayer.isInEvent() && 
								targetPlayer.getInstanceId() == casterPlayer.getInstanceId()))
						{	
							return new L2Character[]{targetPlayer};
						}
						else
						{	
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
							return _emptyTargetList;
						}	
					}
					
				}
				else
					return _emptyTargetList;
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			// Specially for Block Checker Event
			case TARGET_EVENT:
			{
				if (activeChar instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance)activeChar;
					int playerArena = player.getBlockCheckerArena();
					
					if (playerArena != -1)
					{
						ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(playerArena);
						int team = holder.getPlayerTeam(player);
						// Aura attack
						for (L2PcInstance actor : player.getKnownList().getKnownPlayersInRadius(250))
						{
							if (holder.getAllPlayers().contains(actor) && holder.getPlayerTeam(actor) != team)
								targetList.add(actor);
						}
						return targetList.toArray(new L2Character[targetList.size()]);
					}
				}
				return _emptyTargetList;
			}
			case TARGET_MENTEE:
			{
				if (activeChar instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance)activeChar;

					if (target != null && player.isMentor())
					{
						if (!target.isDead())
						{
							return new L2Character[]{target};
						}
						else
							return _emptyTargetList;
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return _emptyTargetList;
					}
				}
				return _emptyTargetList;
			}
			case TARGET_LINE:
			{
				if (target == null || target == activeChar || target.isAlikeDead() ||
						(!(target instanceof L2Attackable || target instanceof L2Playable)))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
				
				// Tenkai custom - in Duels, area skills attack only Duel enemy. Not checking if same Duel ID, but whatever
				if ((activeChar instanceof L2PcInstance && ((L2PcInstance)activeChar).isInDuel())
					|| (activeChar instanceof L2SummonInstance && ((L2SummonInstance)activeChar).getOwner().isInDuel()))
				{
					if (activeChar.getTarget() instanceof L2PcInstance && ((L2PcInstance)activeChar.getTarget()).isInDuel())
						return new L2Object[]{activeChar.getTarget()};
					else
						return _emptyTargetList;
				}
				
				targetList.add(target);
				
				final L2Character origin = activeChar;
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				final int radius = getSkillRadius();
				
				// Calculate a normalized direction vector from the player to the target
				float dirX = target.getX() - origin.getX();
				float dirY = target.getY() - origin.getY();
				float dirZ = target.getZ() - origin.getZ();
				float length = (float)Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
				dirX /= length;
				dirY /= length;
				dirZ /= length;
				
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				for (L2Character obj : objs)
				{
					if (!(obj instanceof L2Attackable
							|| obj instanceof L2Playable))
						continue;
					
					if (obj == origin)
						continue;
					
					if (Util.checkIfInRange(radius, origin, obj, true))
					{
						if (obj == target || !checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
							continue;
						
						// Calculate a normalized direction vector from the player to the object
						float dx = obj.getX() - origin.getX();
						float dy = obj.getY() - origin.getY();
						float dz = obj.getZ() - origin.getZ();
						length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
						dx /= length;
						dy /= length;
						dz /= length;
						
						// Their dot product is the cosine of the angle between both vectors
						float dot = dirX * dx + dirY * dy + dirZ * dz;
						// If the cosine is near 1, we have a tight angle
						if (dot > 0.99f)
							targetList.add(obj);
					}
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_GROUND_AREA:
			{
				if (!(activeChar instanceof L2PcInstance))
					return _emptyTargetList;
				
				L2PcInstance player = (L2PcInstance)activeChar;
				
				Point3D position = player.getSkillCastPosition();
				if (position == null)
					return _emptyTargetList;
				
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				final int radius = getSkillRadius();
				
				final Collection<L2Character> objs = activeChar.getKnownList().getKnownCharacters();
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2Character obj : objs)
					{
						if (!(obj instanceof L2Attackable
								|| obj instanceof L2Playable))
							continue;
						
						if (Util.calculateDistance(obj.getX(), obj.getY(), obj.getZ(), position.getX(), position.getY(), position.getZ(), true) <= radius)
						{
							if (!checkForAreaOffensiveSkills(activeChar, obj, this, srcInArena))
								continue;
							
							targetList.add(obj);
						}
					}
				}
				
				if (targetList.isEmpty())
					return _emptyTargetList;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			default:
			{
				activeChar.sendMessage("Target type of skill is not currently handled");
				return _emptyTargetList;
			}
		}//end switch
	}
	
	public final L2Object[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(L2Character activeChar)
	{
		L2Object[] targets;
		
		targets = getTargetList(activeChar, true);
		
		if (targets.length == 0)
			return null;
		else
			return targets[0];
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
	public static final boolean checkForAreaOffensiveSkills(L2Character caster, L2Character target, L2Skill skill, boolean sourceInArena)
	{
		if (target == null || target.isDead() || target == caster || target.isInvul(caster))
			return false;
		
		final L2PcInstance player = caster.getActingPlayer();
		final L2PcInstance targetPlayer = target.getActingPlayer();
		if (player != null)
		{
			if (player.inObserverMode())
				return false;
			
			if (target instanceof L2MobSummonInstance)
				return false;
			
			if (targetPlayer != null)
			{
				if (targetPlayer == caster || targetPlayer == player)
					return false;
				
				if (targetPlayer.inObserverMode())
					return false;
				
				if (skill.isOffensive() && player.getSiegeState() > 0 && player.isInsideZone(L2Character.ZONE_SIEGE)
						&& player.getSiegeState() == targetPlayer.getSiegeState()
						&& player.getSiegeSide() == targetPlayer.getSiegeSide())
					return false;
				
				if (target.isInsideZone(L2Character.ZONE_PEACE))
					return false;
				
				if (player.isInParty() && targetPlayer.isInParty())
				{
					// Same party
					if (player.getParty().getPartyLeaderOID() == targetPlayer.getParty().getPartyLeaderOID())
						return false;
					
					// Same commandchannel
					if (player.getParty().getCommandChannel() != null
							&& player.getParty().getCommandChannel() == targetPlayer.getParty().getCommandChannel())
						return false;
				}
				
				if (player.isInEvent() &&
						targetPlayer.isInEvent() &&
						!player.getEvent().getConfig().isAllVsAll() && 
						EventsManager.getInstance().isPlayerParticipant(player.getObjectId()) && 
						EventsManager.getInstance().isPlayerParticipant(targetPlayer.getObjectId()) &&
						((EventsManager.getInstance().getParticipantTeamId(player.getObjectId()) == EventsManager.getInstance().getParticipantTeamId(targetPlayer.getObjectId())) ||
						player.getEvent() != targetPlayer.getEvent()))
					return false;
				
				if (player.getPvpFlag() == 0 && !player.isInsideZone(L2Character.ZONE_PVP) && !player.isInsideZone(L2Character.ZONE_SIEGE))
					return false;
				
				if (!sourceInArena && !(targetPlayer.isInsideZone(L2Character.ZONE_PVP) && !targetPlayer.isInsideZone(L2Character.ZONE_SIEGE)))
				{
					if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId())
						return false;
					
					if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId())
						return false;
					
					if (!player.checkPvpSkill(targetPlayer, skill, (caster instanceof L2Summon)))
						return false;
				}
			}
		}
		else
		{
			// source is not playable
			if (caster instanceof L2Attackable)
			{
				// target is mob
				if (targetPlayer == null && target instanceof L2Attackable && caster instanceof L2Attackable)
				{
					String casterEnemyClan = ((L2Attackable)caster).getEnemyClan();
					if (casterEnemyClan == null || casterEnemyClan.isEmpty())
						return false;
					
					String targetClan = ((L2Attackable)target).getClan();
					if (targetClan == null || targetClan.isEmpty())
						return false;
					
					if (!casterEnemyClan.equals(targetClan))
						return false;
					
					if (casterEnemyClan.equals(targetClan) && skill.getSkillType() == L2SkillType.BUFF)
					{
						return false;
					}
				}
				else
				{
					if (caster instanceof L2GuardInstance && caster.isInvul(target) && target instanceof L2Playable)
						return false;
				}
			}
			else if (caster instanceof L2Npc && ((L2Npc)caster).getOwner() != null)	//to filter
			{
				if (targetPlayer != null && !checkForAreaOffensiveSkills(((L2Npc)caster).getOwner(), targetPlayer, skill, sourceInArena))
					return false;
			}
		}
		
		if (geoEnabled && !GeoData.getInstance().canSeeTarget(caster, target))
			return false;
		
		return true;
	}
	
	public final boolean addCharacter(L2Character caster, L2Character target, int radius, boolean isDead)
	{
		if (target == null || isDead != target.isDead())
			return false;
		
		if (radius > 0 && !Util.checkIfInRange(radius, caster, target, true)
				&& !GeoData.getInstance().canSeeTarget(caster, target))
			return false;
		
		//LasTravel each target should pass the skill conditions
		if (!checkCondition(caster, target, false))
			return false;
		
		return true;
		
	}
	
	public final Func[] getStatFuncs(L2Abnormal effect, L2Character player)
	{
		if (_funcTemplates == null)
			return _emptyFunctionSet;
		
		if (!(player instanceof L2Playable) && !(player instanceof L2Attackable))
			return _emptyFunctionSet;
		
		ArrayList<Func> funcs = new ArrayList<Func>(_funcTemplates.length);
		
		Env env = new Env();
		env.player = player;
		env.skill = this;
		
		Func f;
		
		for (FuncTemplate t : _funcTemplates)
		{
			
			f = t.getFunc(env, this); // skill is owner
			if (f != null)
				funcs.add(f);
		}
		if (funcs.isEmpty())
			return _emptyFunctionSet;
		
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return (_effectTemplates != null && _effectTemplates.length > 0);
	}
	
	public L2AbnormalTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	public boolean hasSelfEffects()
	{
		return (_effectTemplatesSelf != null && _effectTemplatesSelf.length > 0);
	}
	
	/**
	 * Env is used to pass parameters for secondary effects (shield and ss/bss/bsss)
	 * 
	 * @return an array with the effects that have been added to effector
	 */
	public final L2Abnormal[] getEffects(L2Character effector, L2Character effected, Env env)
	{
		if (!hasEffects() || isPassive())
			return _emptyEffectSet;
		
		// doors and siege flags cannot receive any effects
		if (effected instanceof L2DoorInstance || effected instanceof L2SiegeFlagInstance)
			return _emptyEffectSet;
		
		if (effector != effected && !ignoreImmunity())
		{
			if (effected instanceof L2PcInstance && ((L2PcInstance)effected).getFaceoffTarget() != null
					&& effector != ((L2PcInstance)effected).getFaceoffTarget())
				return _emptyEffectSet;
			
			if (isOffensive() || isDebuff())
			{
				if (effected.isInvul(effector))
					return _emptyEffectSet;
				
				if (effector instanceof L2PcInstance && ((L2PcInstance)effector).isGM())
				{
					if (!((L2PcInstance)effector).getAccessLevel().canGiveDamage())
						return _emptyEffectSet;
				}
			}
		}
		
		ArrayList<L2Abnormal> effects = new ArrayList<L2Abnormal>(_effectTemplates.length);
		
		if (env == null)
			env = new Env();
		
		env.skillMastery = Formulas.calcSkillMastery(effector, this);
		env.player = effector;
		env.target = effected;
		env.skill = this;
		
		for (L2AbnormalTemplate et : _effectTemplates)
		{
			L2Abnormal e = et.getEffect(env);
			if (e == null)
				continue;
			
			boolean success = true;
			if (et.landRate > -1)
				success = Formulas.calcEffectSuccess(effector, effected, e, this, env.shld, env.ssMul);
			
			if (success)
			{
				e.scheduleEffect();
				effects.add(e);
			}
			// display fail message only for effects with icons
			else if (et.icon && effector instanceof L2PcInstance)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
				sm.addCharName(effected);
				sm.addSkillName(this);
				((L2PcInstance)effector).sendPacket(sm);
			}
		}
		
		if (effects.isEmpty())
			return _emptyEffectSet;
		
		return effects.toArray(new L2Abnormal[effects.size()]);
	}
	
	/**
	 * Warning: this method doesn't consider modifier (shield, ss, sps, bss) for secondary effects
	 * 
	 */
	public final L2Abnormal[] getEffects(L2Character effector, L2Character effected)
	{
		return getEffects(effector, effected, null);
	}
	
	/**
	 * This method has suffered some changes in CT2.2 ->CT2.3<br>
	 * Effect engine is now supporting secondary effects with independent
	 * success/fail calculus from effect skill. Env parameter has been added to
	 * pass parameters like soulshot, spiritshots, blessed spiritshots or shield deffence.
	 * Some other optimizations have been done
	 * <br><br>
	 * This new feature works following next rules:
	 * <li> To enable feature, effectPower must be over -1 (check DocumentSkill#attachEffect for further information)</li>
	 * <li> If main skill fails, secondary effect always fail</li>
	 */
	public final L2Abnormal[] getEffects(L2CubicInstance effector, L2Character effected, Env env)
	{
		if (!hasEffects() || isPassive())
			return _emptyEffectSet;
		
		if (effector.getOwner() != effected && !ignoreImmunity())
		{
			if (isDebuff() || isOffensive())
			{
				if (effected.isInvul(effector.getOwner()))
					return _emptyEffectSet;
				
				if (effector.getOwner().isGM() &&
						!effector.getOwner().getAccessLevel().canGiveDamage())
				{
					return _emptyEffectSet;
				}
			}
		}
		
		ArrayList<L2Abnormal> effects = new ArrayList<L2Abnormal>(_effectTemplates.length);
		
		if (env == null)
			env = new Env();
		
		env.player = effector.getOwner();
		env.cubic = effector;
		env.target = effected;
		env.skill = this;
		
		for (L2AbnormalTemplate et : _effectTemplates)
		{
			L2Abnormal e = et.getEffect(env);
			if (e == null)
				continue;
			
			boolean success = true;
			if (et.landRate > -1)
				success = Formulas.calcEffectSuccess(effector.getOwner(), effected, e, this, env.shld, env.ssMul);
			
			if (success)
			{
				e.scheduleEffect();
				effects.add(e);
			}
		}
		
		if (effects.isEmpty()) return _emptyEffectSet;
		
		return effects.toArray(new L2Abnormal[effects.size()]);
	}
	
	public final L2Abnormal[] getEffectsSelf(L2Character effector)
	{
		if (!hasSelfEffects() || isPassive())
			return _emptyEffectSet;
		
		List<L2Abnormal> effects = new ArrayList<L2Abnormal>(_effectTemplatesSelf.length);
		
		for (L2AbnormalTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.skillMastery = Formulas.calcSkillMastery(effector, this);
			env.player = effector;
			env.target = effector;
			env.skill = this;
			L2Abnormal e = et.getEffect(env);
			if (e != null)
			{
				e.setSelfEffect();
				e.scheduleEffect();
				effects.add(e);
			}
		}
		if (effects.isEmpty()) return _emptyEffectSet;
		
		return effects.toArray(new L2Abnormal[effects.size()]);
	}
	
	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[] {f};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public final void attach(L2AbnormalTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new L2AbnormalTemplate[] {effect};
		}
		else
		{
			int len = _effectTemplates.length;
			L2AbnormalTemplate[] tmp = new L2AbnormalTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
		
	}
	public final void attachSelf(L2AbnormalTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new L2AbnormalTemplate[] {effect};
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			L2AbnormalTemplate[] tmp = new L2AbnormalTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}
	
	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			if (_itemPreCondition == null)
				_itemPreCondition = new ArrayList<Condition>();
			_itemPreCondition.add(c);
		}
		else
		{
			if (_preCondition == null)
				_preCondition = new ArrayList<Condition>();
			_preCondition.add(c);
		}
	}
	
	@Override
	public String toString()
	{
		return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
	
	/**
	 * @return pet food
	 */
	public int getFeed()
	{
		return _feed;
	}
	
	/**
	 * used for tracking item id in case that item consume cannot be used
	 * @return reference item id
	 */
	public int getReferenceItemId()
	{
		return _refId;
	}
	
	public final int getMaxCharges()
	{
		return _maxCharges;
	}
	
	public int getAfterEffectId()
	{
		return _afterEffectId;
	}
	
	public int getAfterEffectLvl()
	{
		return _afterEffectLvl;
	}
	
	@Override
	public boolean triggersChanceSkill()
	{
		return _triggeredId > 0 && isChance();
	}
	
	@Override
	public int getTriggeredChanceId()
	{
		return _triggeredId;
	}
	
	@Override
	public int getTriggeredChanceLevel()
	{
		return _triggeredLevel;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return _chanceCondition;
	}
	
	public String getAttributeName()
	{
		return _attribute;
	}
	
	/**
	* @return the _blowChance
	*/
	public int getBlowChance()
	{
		return _blowChance;
	}
	
	public boolean ignoreShield()
	{
		return _ignoreShield;
	}
	
	public boolean canBeReflected()
	{
		return _canBeReflected;
	}
	
	public boolean canBeSharedWithSummon()
	{
		return _canBeSharedWithSummon;
	}
	
	public boolean canBeDispeled()
	{
		return _canBeDispeled;
	}
	
	public boolean isClanSkill()
	{
		return _isClanSkill;
	}
	
	public boolean isExcludedFromCheck()
	{
		return _excludedFromCheck;
	}
	
	public float getDependOnTargetBuff()
	{
		return _dependOnTargetBuff;
	}
	
	public boolean isSimultaneousCast()
	{
		return _simultaneousCast;
	}
	
	/**
	 * @param skillId
	 * @param skillLvl
	 * @param values
	 * @return L2ExtractableSkill
	 * @author Zoey76
	 */
	private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values)
	{
		String[] lineSplit = values.split(";");
		
		final ArrayList<L2ExtractableProductItem> product_temp = new ArrayList<L2ExtractableProductItem>();
		
		for (int i = 0; i <= (lineSplit.length-1); i++)
		{
			final String[] lineSplit2 = lineSplit[i].split(",");
			
			if (lineSplit2.length < 3)
				Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong separator!");
			
			int[] production = null;
			int[] amount = null;
			double chance = 0;
			int prodId = 0;
			int quantity = 0;
			try
			{
				int k =0;
				production = new int[(lineSplit2.length-1)/2];
				amount = new int[(lineSplit2.length-1)/2];
				for (int j = 0; j < (lineSplit2.length-1); j++)
				{
					prodId = Integer.parseInt(lineSplit2[j]);
					quantity = Integer.parseInt(lineSplit2[j+=1]);
					if ((prodId <= 0) || (quantity <= 0))
						Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + "!");
					production[k] = prodId;
					amount[k] = quantity;
					k++;
				}
				chance = Double.parseDouble(lineSplit2[lineSplit2.length-1]);
			}
			catch (Exception e)
			{
				Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong separator!");
				e.printStackTrace();
			}
			
			product_temp.add(new L2ExtractableProductItem(production, amount, chance));
		}
		
		if (product_temp.size()== 0)
			Log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
		
		return new L2ExtractableSkill(SkillTable.getSkillHashCode(this), product_temp);
	}
	
	public L2ExtractableSkill getExtractableSkill()
	{
		return _extractableItems;
	}
	
	public boolean isTriggered()
	{
		return _isTriggered;
	}
	
	public void setIsTriggered()
	{
		_isTriggered = true;
	}
	
	public boolean isActivation()
	{
		return _isTriggered && !_isDebuff;
	}

	public int getPartyChangeSkill()
	{
		return _partyChangeSkill;
	}

	public boolean isCastedToParty()
	{
		return _isCastedToParty;
	}

	/**
	 * Return the additional alter skill info.<BR><BR>
	 * @return 
	 */
	public final int getAlterSkillId()
	{
		return _alterSkillId;
	}
	
	public final int getAlterSkillLevel()
	{
		return _alterSkillLevel;
	}

	public final int getAlterSkillTime()
	{
		return _alterIconTime;
	}

	public int[] getDependOnTargetEffectId()
	{
		return _dependOnTargetEffectId;
	}
	
	public double[] getDamageDepend()
	{
		return _damageDepend;
	}
	
	public boolean isElemental()
	{
		return _isElemental;
	}
	
	public boolean isStanceSwitch()
	{
		return _isStanceSwitch;
	}
	
	public String getFirstEffectStack()
	{
		if (getEffectTemplates() != null && getEffectTemplates().length > 0)
		{
			if (getEffectTemplates()[0].stackType.length == 0)
				return "";
			
			return getEffectTemplates()[0].stackType[0];
		}
		return "";
	}

	public int getActionId()
	{
		return _skillActionId;
	}
}
