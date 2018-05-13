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

package l2server.gameserver.templates.chars;

import l2server.Config;
import l2server.gameserver.datatables.ExtraDropTable;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.XmassTreeInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.QuestEventType;
import l2server.gameserver.templates.SpawnData;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.skills.AISkillType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

/**
 * This cl contains all generic data of a L2Spawn object.<BR><BR>
 *
 * <B><U> Data</U> :</B><BR><BR>
 * <li>npcId, type, name, sex</li>
 * <li>rewardExp, rewardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand</li>
 * <li>isUndead</li>
 * <li>drops</li>
 * <li>minions</li>
 * <li>teachInfo</li>
 * <li>skills</li>
 * <li>questsStart</li><BR><BR>
 *
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class NpcTemplate extends CreatureTemplate {
	private static Logger log = LoggerFactory.getLogger(NpcTemplate.class.getName());
	
	public int NpcId;
	public int TemplateId;
	public String Type;
	public String Name;
	public boolean ServerSideName;
	public String Title;
	public boolean ServerSideTitle;
	public byte Level;
	public long RewardExp;
	public long RewardSp;
	public boolean CanSeeThroughSilentMove;
	public boolean canBeChampion;
	public boolean isLethalImmune;
	public boolean isDebuffImmune;
	public boolean Aggressive;
	public int AggroRange;
	public int RHand;
	public int LHand;
	public int EnchantEffect;
	public boolean RandomWalk;
	public L2NpcRace Race;
	public int ExtraDropGroup;
	public boolean Targetable;
	public boolean IsNonTalking;
	public boolean ShowName;
	public boolean isQuestMonster; // doesn't include all mobs that are involved in
	// quests, just plain quest monsters for preventing champion spawn
	public float BaseVitalityDivider;
	public int InteractionDistance;
	public boolean BonusFromBaseStats;
	
	public int FixedAccuracy;
	public int FixedEvasion;
	public float HatersDamageMultiplier;
	
	//Skill AI
	@SuppressWarnings("unchecked")
	public List<Skill>[] aiSkills = new List[AISkillType.AIST_COUNT];
	public boolean[] aiSkillChecks = new boolean[AISkillType.AIST_COUNT];
	
	private L2NpcAIData aiData = new L2NpcAIData();
	
	public enum AIType {
		FIGHTER,
		ARCHER,
		BALANCED,
		MAGE,
		HEALER,
		CORPSE
	}
	
	public enum L2NpcRace {
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		UNKNOWN,
		KAMAEL,
		NONE
	}
	
	private ArrayList<L2DropData> spoilDrop = new ArrayList<>();
	private ArrayList<L2DropData> normalDrop = new ArrayList<>();
	private ArrayList<L2DropCategory> multiDrop = new ArrayList<>();
	
	/**
	 * The table containing all Minions that must be spawn with the NpcInstance using this NpcTemplate
	 */
	private List<L2MinionData> minions = null;
	private L2RandomMinionData randomMinions = null;
	private Map<Integer, Skill> skills = null;
	private List<SpawnData> spawns = new ArrayList<>();
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private Map<QuestEventType, Quest[]> questEvents = null;
	
	private StatsSet baseSet;
	private NpcTemplate baseTemplate;
	
	private final List<L2Spawn> allSpawns = new ArrayList<>();
	
	/**
	 * Constructor of Creature.<BR><BR>
	 *
	 * @param set The StatsSet object to transfer data to the method
	 */
	public NpcTemplate(StatsSet set) {
		super(set);
		NpcId = set.getInteger("id");
		TemplateId = set.getInteger("templateId", NpcId);
		Type = set.getString("type");
		Name = set.getString("name");
		ServerSideName = set.getBool("serverSideName", false);
		Title = set.getString("title", "");
		isQuestMonster = Title.equalsIgnoreCase("Quest Monster");
		ServerSideTitle = set.getBool("serverSideTitle", false);
		Level = set.getByte("level", (byte) 150);
		RewardExp = set.getLong("exp", 0);
		RewardSp = set.getLong("sp", 0);
		CanSeeThroughSilentMove = set.getBool("canSeeThroughSilentMove", false);
		canBeChampion = set.getBool("canBeChampion", true);
		isLethalImmune = set.getBool("isLethalImmune", false);
		isDebuffImmune = set.getBool("isDebuffImmune", false);
		Aggressive = set.getBool("aggressive", false);
		AggroRange = set.getInteger("aggroRange", 0);
		RHand = set.getInteger("rHand", 0);
		LHand = set.getInteger("lHand", 0);
		EnchantEffect = set.getInteger("enchant", 0);
		RandomWalk = set.getBool("randomWalk", false);
		Race = null;
		int herbGroup = set.getInteger("extraDropGroup", 0);
		if (herbGroup > 0 && ExtraDropTable.getInstance().getExtraDroplist(herbGroup) == null) {
			log.warn("Missing Herb Drop Group for npcId: " + NpcId);
			ExtraDropGroup = 0;
		} else {
			ExtraDropGroup = herbGroup;
		}
		Targetable = set.getBool("targetable", true);
		IsNonTalking = set.getBool("isNonTalking", false);
		ShowName = set.getBool("showName", true);
		
		// can be loaded from db
		BaseVitalityDivider = Level > 0 && RewardExp > 0 ? (float) getBaseHpMax() * 9 / (100 * RewardExp / (Level * Level)) : 0;
		
		InteractionDistance = set.getInteger("interactionDistance", Npc.DEFAULT_INTERACTION_DISTANCE);
		
		boolean bonusByDefault = false;
		BonusFromBaseStats = set.getBool("bonusFromBaseStats", bonusByDefault);
		FixedAccuracy = set.getInteger("fixedAccuracy", 0);
		FixedEvasion = set.getInteger("fixedEvasion", 0);
		HatersDamageMultiplier = set.getFloat("hatersDamageMultiplier", 0);
		
		baseSet = set;
		baseTemplate = this;
		
		if (Config.isServer(Config.TENKAI_LEGACY) && Type.equals("L2Defender")) {
			Level = 103;
		}
	}
	
	public NpcTemplate(StatsSet set, NpcTemplate baseTemplate) {
		super(set);
		
		NpcId = set.getInteger("id");
		TemplateId = set.getInteger("templateId", baseTemplate.TemplateId);
		Type = set.getString("type", baseTemplate.Type);
		Name = set.getString("name", baseTemplate.Name);
		ServerSideName = set.getBool("serverSideName", baseTemplate.ServerSideName);
		Title = set.getString("title", baseTemplate.Title);
		isQuestMonster = baseTemplate.isQuestMonster;
		ServerSideTitle = set.getBool("serverSideTitle", baseTemplate.ServerSideName);
		Level = set.getByte("level", baseTemplate.Level);
		RewardExp = set.getLong("exp", baseTemplate.RewardExp);
		RewardSp = set.getLong("sp", baseTemplate.RewardSp);
		CanSeeThroughSilentMove = set.getBool("canSeeThroughSilentMove", baseTemplate.CanSeeThroughSilentMove);
		canBeChampion = set.getBool("canBeChampion", baseTemplate.canBeChampion);
		isLethalImmune = set.getBool("isLethalImmune", baseTemplate.isLethalImmune);
		isDebuffImmune = set.getBool("isDebuffImmune", baseTemplate.isDebuffImmune);
		Aggressive = set.getBool("aggressive", baseTemplate.Aggressive);
		AggroRange = set.getInteger("aggroRange", baseTemplate.AggroRange);
		RHand = set.getInteger("rHand", baseTemplate.RHand);
		LHand = set.getInteger("lHand", baseTemplate.LHand);
		EnchantEffect = set.getInteger("enchant", baseTemplate.EnchantEffect);
		RandomWalk = set.getBool("randomWalk", baseTemplate.RandomWalk);
		Race = baseTemplate.Race;
		int herbGroup = set.getInteger("extraDropGroup", baseTemplate.ExtraDropGroup);
		if (herbGroup > 0 && ExtraDropTable.getInstance().getExtraDroplist(herbGroup) == null) {
			log.warn("Missing Herb Drop Group for npcId: " + NpcId);
			ExtraDropGroup = 0;
		} else {
			ExtraDropGroup = herbGroup;
		}
		Targetable = set.getBool("targetable", baseTemplate.Targetable);
		IsNonTalking = set.getBool("isNonTalking", baseTemplate.IsNonTalking);
		ShowName = set.getBool("showName", baseTemplate.ShowName);
		
		BonusFromBaseStats = set.getBool("bonusFromBaseStats", baseTemplate.BonusFromBaseStats);
		FixedAccuracy = set.getInteger("fixedAccuracy", baseTemplate.FixedAccuracy);
		FixedEvasion = set.getInteger("fixedEvasion", baseTemplate.FixedEvasion);
		
		HatersDamageMultiplier = set.getFloat("hatersDamageMultiplier", baseTemplate.HatersDamageMultiplier);
		
		// can be loaded from db
		BaseVitalityDivider = Level > 0 && RewardExp > 0 ? (float) getBaseHpMax() * 9 / (100 * RewardExp / (Level * Level)) : 0;
		
		InteractionDistance = set.getInteger("interactionDistance", baseTemplate.InteractionDistance);
		
		setBaseFire(baseTemplate.getBaseFire());
		setBaseWater(baseTemplate.getBaseWater());
		setBaseEarth(baseTemplate.getBaseEarth());
		setBaseWind(baseTemplate.getBaseWind());
		setBaseHoly(baseTemplate.getBaseHoly());
		setBaseDark(baseTemplate.getBaseDark());
		setBaseFireRes(baseTemplate.getBaseFireRes());
		setBaseWaterRes(baseTemplate.getBaseWaterRes());
		setBaseEarthRes(baseTemplate.getBaseEarthRes());
		setBaseWindRes(baseTemplate.getBaseWindRes());
		setBaseHolyRes(baseTemplate.getBaseHolyRes());
		setBaseDarkRes(baseTemplate.getBaseDarkRes());
		
		aiData = baseTemplate.aiData;
		
		if (!set.getBool("overrideDrops", false)) {
			spoilDrop = new ArrayList<>(baseTemplate.spoilDrop);
			normalDrop = new ArrayList<>(baseTemplate.normalDrop);
			multiDrop = new ArrayList<>();
			for (L2DropCategory dc : baseTemplate.multiDrop) {
				L2DropCategory newDC = new L2DropCategory(dc.getChance());
				for (L2DropData dd : dc.getAllDrops()) {
					newDC.addDropData(dd);
				}
				multiDrop.add(newDC);
			}
		}
		
		if (baseTemplate.minions != null) {
			minions = new ArrayList<>(baseTemplate.minions);
		}
		
		if (baseTemplate.randomMinions != null) {
			randomMinions = new L2RandomMinionData(baseTemplate.randomMinions);
		}
		
		if (baseTemplate.skills != null) {
			for (Skill skill : baseTemplate.skills.values()) {
				addSkill(skill);
			}
		}
		
		if (!set.getBool("overrideSpawns", false)) {
			spawns = new ArrayList<>(baseTemplate.spawns);
		}
		
		if (baseTemplate.questEvents != null) {
			questEvents = new HashMap<>(baseTemplate.questEvents);
		}
		
		baseSet = set;
		while (baseTemplate != baseTemplate.baseTemplate) {
			baseTemplate = baseTemplate.baseTemplate;
		}
		this.baseTemplate = baseTemplate;
	}
	
	public void addSpoilData(L2DropData drop) {
		spoilDrop.add(drop);
	}
	
	public void addDropData(L2DropData drop) {
		normalDrop.add(drop);
	}
	
	public void addMultiDrop(L2DropCategory category) {
		multiDrop.add(category);
	}
	
	public void addRaidData(L2MinionData minion) {
		if (minions == null) {
			minions = new ArrayList<>();
		}
		minions.add(minion);
	}
	
	public void setRandomRaidData(L2RandomMinionData minion) {
		randomMinions = minion;
	}
	
	public void addSkill(Skill skill) {
		if (skills == null) {
			skills = new LinkedHashMap<>();
		}
		
		if (!skill.isPassive()) {
			addGeneralSkill(skill);
			switch (skill.getSkillType()) {
				case BUFF:
					addBuffSkill(skill);
					break;
				case HEAL:
					//case HOT:
				case HEAL_PERCENT:
				case HEAL_STATIC:
				case BALANCE_LIFE:
					addHealSkill(skill);
					break;
				case RESURRECT:
					addResSkill(skill);
					break;
				case DEBUFF:
					addDebuffSkill(skill);
					addCOTSkill(skill);
					addRangeSkill(skill);
					break;
				/*case ROOT:
                addRootSkill(skill);
				addImmobiliseSkill(skill);
				addRangeSkill(skill);
				break;
				case SLEEP:
				addSleepSkill(skill);
				addImmobiliseSkill(skill);
				break;
				case STUN:
				addRootSkill(skill);
				addImmobiliseSkill(skill);
				addRangeSkill(skill);
				break;
				case PARALYZE:
				addParalyzeSkill(skill);
				addImmobiliseSkill(skill);
				addRangeSkill(skill);
				break;*/
				case PDAM:
				case MDAM:
				case BLOW:
				case DRAIN:
				case CHARGEDAM:
				case FATAL:
				case DEATHLINK:
				case CPDAM:
				case MANADAM:
				case CPDAMPERCENT:
				case MAXHPDAMPERCENT:
					addAtkSkill(skill);
					addUniversalSkill(skill);
					addRangeSkill(skill);
					break;
                /*case POISON:
				case DOT:
				case MDOT:
				case BLEED:
				addDOTSkill(skill);
				addRangeSkill(skill);
				break;
				case MUTE:
				case FEAR:
				addCOTSkill(skill);
				addRangeSkill(skill);
				break;*/
				case CANCEL:
				case NEGATE:
					addNegativeSkill(skill);
					addRangeSkill(skill);
					break;
				default:
					addUniversalSkill(skill);
					break;
			}
		}
		
		skills.put(skill.getId(), skill);
	}
	
	public void addSpawn(SpawnData spawn) {
		spawns.add(spawn);
	}
	
	public ArrayList<L2DropData> getSpoilData() {
		return spoilDrop;
	}
	
	public ArrayList<L2DropData> getDropData() {
		return normalDrop;
	}
	
	/**
	 * Return the list of all possible UNCATEGORIZED drops of this NpcTemplate.<BR><BR>
	 */
	public ArrayList<L2DropCategory> getMultiDropData() {
		return multiDrop;
	}
	
	/**
	 * Return the list of all Minions that must be spawn with the NpcInstance using this NpcTemplate.<BR><BR>
	 */
	public List<L2MinionData> getMinionData() {
		return minions;
	}
	
	public L2MinionData getMinionData(final int minionId) {
		if (minions == null) {
			return null;
		}
		
		for (L2MinionData minion : minions) {
			if (minionId != minion.getMinionId()) {
				continue;
			}
			
			return minion;
		}
		
		return null;
	}
	
	public L2RandomMinionData getRandomMinionData() {
		return randomMinions;
	}
	
	public Map<Integer, Skill> getSkills() {
		return skills;
	}
	
	public List<SpawnData> getSpawns() {
		return spawns;
	}
	
	public void addQuestEvent(QuestEventType EventType, Quest q) {
		if (questEvents == null) {
			questEvents = new HashMap<>();
		}
		
		if (questEvents.get(EventType) == null) {
			questEvents.put(EventType, new Quest[]{q});
		} else {
			Quest[] quests = questEvents.get(EventType);
			int len = quests.length;
			
			// if only one registration per npc is allowed for this event type
			// then only register this NPC if not already registered for the specified event.
			// if a quest allows multiple registrations, then register regardless of count
			// In all cases, check if this new registration is replacing an older copy of the SAME quest
			// Finally, check quest class hierarchy: a parent class should never replace a child class.
			// a child class should always replace a parent class.
			if (!EventType.isMultipleRegistrationAllowed()) {
				// if it is the same quest (i.e. reload) or the existing is a superclass of the new one, replace the existing.
				if (quests[0].getName().equals(q.getName()) || NpcTemplate.isAssignableTo(q, quests[0].getClass())) {
					quests[0] = q;
				} else {
					log.warn("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + EventType + "\" for NPC \"" + Name +
							"\" and quest \"" + q.getName() + "\".");
				}
			} else {
				// be ready to add a new quest to a new copy of the list, with larger size than previously.
				Quest[] tmp = new Quest[len + 1];
				
				// loop through the existing quests and copy them to the new list.  While doing so, also
				// check if this new quest happens to be just a replacement for a previously loaded quest.
				// Replace existing if the new quest is the same (reload) or a child of the existing quest.
				// Do nothing if the new quest is a superclass of an existing quest.
				// Add the new quest in the end of the list otherwise.
				for (int i = 0; i < len; i++) {
					if (quests[i].getName().equals(q.getName()) || NpcTemplate.isAssignableTo(q, quests[i].getClass())) {
						quests[i] = q;
						return;
					} else if (NpcTemplate.isAssignableTo(quests[i], q.getClass())) {
						return;
					}
					tmp[i] = quests[i];
				}
				tmp[len] = q;
				questEvents.put(EventType, tmp);
			}
		}
	}
	
	/**
	 * Checks if obj can be assigned to the Class represented by value.<br>
	 * This is true if, and only if, obj is the same class represented by value,
	 * or a subclass of it or obj implements the interface represented by value.
	 */
	public static boolean isAssignableTo(Object obj, Class<?> clazz) {
		return NpcTemplate.isAssignableTo(obj.getClass(), clazz);
	}
	
	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz) {
		// if value represents an interface
		if (clazz.isInterface()) {
			// check if obj implements the value interface
			Class<?>[] interfaces = sub.getInterfaces();
			for (Class<?> interface1 : interfaces) {
				if (clazz.getName().equals(interface1.getName())) {
					return true;
				}
			}
		} else {
			do {
				if (sub.getName().equals(clazz.getName())) {
					return true;
				}
				
				sub = sub.getSuperclass();
			} while (sub != null);
		}
		
		return false;
	}
	
	public Quest[] getEventQuests(QuestEventType EventType) {
		if (questEvents == null) {
			return null;
		}
		
		return questEvents.get(EventType);
	}
	
	public void setRace(int raceId) {
		switch (raceId) {
			case 1:
				Race = L2NpcRace.UNDEAD;
				break;
			case 2:
				Race = L2NpcRace.MAGICCREATURE;
				break;
			case 3:
				Race = L2NpcRace.BEAST;
				break;
			case 4:
				Race = L2NpcRace.ANIMAL;
				break;
			case 5:
				Race = L2NpcRace.PLANT;
				break;
			case 6:
				Race = L2NpcRace.HUMANOID;
				break;
			case 7:
				Race = L2NpcRace.SPIRIT;
				break;
			case 8:
				Race = L2NpcRace.ANGEL;
				break;
			case 9:
				Race = L2NpcRace.DEMON;
				break;
			case 10:
				Race = L2NpcRace.DRAGON;
				break;
			case 11:
				Race = L2NpcRace.GIANT;
				break;
			case 12:
				Race = L2NpcRace.BUG;
				break;
			case 13:
				Race = L2NpcRace.FAIRIE;
				break;
			case 14:
				Race = L2NpcRace.HUMAN;
				break;
			case 15:
				Race = L2NpcRace.ELVE;
				break;
			case 16:
				Race = L2NpcRace.DARKELVE;
				break;
			case 17:
				Race = L2NpcRace.ORC;
				break;
			case 18:
				Race = L2NpcRace.DWARVE;
				break;
			case 19:
				Race = L2NpcRace.OTHER;
				break;
			case 20:
				Race = L2NpcRace.NONLIVING;
				break;
			case 21:
				Race = L2NpcRace.SIEGEWEAPON;
				break;
			case 22:
				Race = L2NpcRace.DEFENDINGARMY;
				break;
			case 23:
				Race = L2NpcRace.MERCENARIE;
				break;
			case 24:
				Race = L2NpcRace.UNKNOWN;
				break;
			case 25:
				Race = L2NpcRace.KAMAEL;
				break;
			default:
				Race = L2NpcRace.NONE;
				break;
		}
	}
	
	//-----------------------------------------------------------------------
	// Npc AI Data
	// By ShanSoft
	public void setAIData(L2NpcAIData aidata) {
		//AIdataStatic = new L2NpcAIData(); // not needed to init object and in next line override with other reference. maybe other intention?
		aiData = aidata;
	}
	
	//-----------------------------------------------------------------------
	
	public L2NpcAIData getAIData() {
		return aiData;
	}
	
	public void addBuffSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_BUFF] == null) {
			aiSkills[AISkillType.AIST_BUFF] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_BUFF].add(skill);
		aiSkillChecks[AISkillType.AIST_BUFF] = true;
	}
	
	public void addHealSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_HEAL] == null) {
			aiSkills[AISkillType.AIST_HEAL] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_HEAL].add(skill);
		aiSkillChecks[AISkillType.AIST_HEAL] = true;
	}
	
	public void addResSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_RES] == null) {
			aiSkills[AISkillType.AIST_RES] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_RES].add(skill);
		aiSkillChecks[AISkillType.AIST_RES] = true;
	}
	
	public void addAtkSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_ATK] == null) {
			aiSkills[AISkillType.AIST_ATK] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_ATK].add(skill);
		aiSkillChecks[AISkillType.AIST_ATK] = true;
	}
	
	public void addDebuffSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_DEBUFF] == null) {
			aiSkills[AISkillType.AIST_DEBUFF] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_DEBUFF].add(skill);
		aiSkillChecks[AISkillType.AIST_DEBUFF] = true;
	}
	
	public void addRootSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_ROOT] == null) {
			aiSkills[AISkillType.AIST_ROOT] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_ROOT].add(skill);
		aiSkillChecks[AISkillType.AIST_ROOT] = true;
	}
	
	public void addSleepSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_SLEEP] == null) {
			aiSkills[AISkillType.AIST_SLEEP] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_SLEEP].add(skill);
		aiSkillChecks[AISkillType.AIST_SLEEP] = true;
	}
	
	public void addStunSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_STUN] == null) {
			aiSkills[AISkillType.AIST_STUN] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_STUN].add(skill);
		aiSkillChecks[AISkillType.AIST_STUN] = true;
	}
	
	public void addParalyzeSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_PARALYZE] == null) {
			aiSkills[AISkillType.AIST_PARALYZE] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_PARALYZE].add(skill);
		aiSkillChecks[AISkillType.AIST_PARALYZE] = true;
	}
	
	public void addFloatSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_FLOAT] == null) {
			aiSkills[AISkillType.AIST_FLOAT] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_FLOAT].add(skill);
		aiSkillChecks[AISkillType.AIST_FLOAT] = true;
	}
	
	public void addFossilSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_FOSSIL] == null) {
			aiSkills[AISkillType.AIST_FOSSIL] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_FOSSIL].add(skill);
		aiSkillChecks[AISkillType.AIST_FOSSIL] = true;
	}
	
	public void addNegativeSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_NEGATIVE] == null) {
			aiSkills[AISkillType.AIST_NEGATIVE] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_NEGATIVE].add(skill);
		aiSkillChecks[AISkillType.AIST_NEGATIVE] = true;
	}
	
	public void addImmobilizeSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_IMMOBILIZE] == null) {
			aiSkills[AISkillType.AIST_IMMOBILIZE] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_IMMOBILIZE].add(skill);
		aiSkillChecks[AISkillType.AIST_IMMOBILIZE] = true;
	}
	
	public void addDOTSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_DOT] == null) {
			aiSkills[AISkillType.AIST_DOT] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_DOT].add(skill);
		aiSkillChecks[AISkillType.AIST_DOT] = true;
	}
	
	public void addUniversalSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_UNIVERSAL] == null) {
			aiSkills[AISkillType.AIST_UNIVERSAL] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_UNIVERSAL].add(skill);
		aiSkillChecks[AISkillType.AIST_UNIVERSAL] = true;
	}
	
	public void addCOTSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_COT] == null) {
			aiSkills[AISkillType.AIST_COT] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_COT].add(skill);
		aiSkillChecks[AISkillType.AIST_COT] = true;
	}
	
	public void addManaHealSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_MANA] == null) {
			aiSkills[AISkillType.AIST_MANA] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_MANA].add(skill);
		aiSkillChecks[AISkillType.AIST_MANA] = true;
	}
	
	public void addGeneralSkill(Skill skill) {
		if (aiSkills[AISkillType.AIST_GENERAL] == null) {
			aiSkills[AISkillType.AIST_GENERAL] = new ArrayList<>();
		}
		aiSkills[AISkillType.AIST_GENERAL].add(skill);
		aiSkillChecks[AISkillType.AIST_GENERAL] = true;
	}
	
	public void addRangeSkill(Skill skill) {
		if (skill.getCastRange() <= 150 && skill.getCastRange() > 0) {
			if (aiSkills[AISkillType.AIST_SHORT_RANGE] == null) {
				aiSkills[AISkillType.AIST_SHORT_RANGE] = new ArrayList<>();
			}
			aiSkills[AISkillType.AIST_SHORT_RANGE].add(skill);
			aiSkillChecks[AISkillType.AIST_SHORT_RANGE] = true;
		} else if (skill.getCastRange() > 150) {
			if (aiSkills[AISkillType.AIST_LONG_RANGE] == null) {
				aiSkills[AISkillType.AIST_LONG_RANGE] = new ArrayList<>();
			}
			aiSkills[AISkillType.AIST_LONG_RANGE].add(skill);
			aiSkillChecks[AISkillType.AIST_LONG_RANGE] = true;
		}
	}
	
	//--------------------------------------------------------------------
	public boolean hasBuffSkill() {
		return aiSkillChecks[AISkillType.AIST_BUFF];
	}
	
	public boolean hasHealSkill() {
		return aiSkillChecks[AISkillType.AIST_HEAL];
	}
	
	public boolean hasResSkill() {
		return aiSkillChecks[AISkillType.AIST_RES];
	}
	
	public boolean hasAtkSkill() {
		return aiSkillChecks[AISkillType.AIST_ATK];
	}
	
	public boolean hasDebuffSkill() {
		return aiSkillChecks[AISkillType.AIST_DEBUFF];
	}
	
	public boolean hasRootSkill() {
		return aiSkillChecks[AISkillType.AIST_ROOT];
	}
	
	public boolean hasSleepSkill() {
		return aiSkillChecks[AISkillType.AIST_SLEEP];
	}
	
	public boolean hasStunSkill() {
		return aiSkillChecks[AISkillType.AIST_STUN];
	}
	
	public boolean hasParalyzeSkill() {
		return aiSkillChecks[AISkillType.AIST_PARALYZE];
	}
	
	public boolean hasFloatSkill() {
		return aiSkillChecks[AISkillType.AIST_FLOAT];
	}
	
	public boolean hasFossilSkill() {
		return aiSkillChecks[AISkillType.AIST_FOSSIL];
	}
	
	public boolean hasNegativeSkill() {
		return aiSkillChecks[AISkillType.AIST_NEGATIVE];
	}
	
	public boolean hasImmobiliseSkill() {
		return aiSkillChecks[AISkillType.AIST_IMMOBILIZE];
	}
	
	public boolean hasDOTSkill() {
		return aiSkillChecks[AISkillType.AIST_DOT];
	}
	
	public boolean hasUniversalSkill() {
		return aiSkillChecks[AISkillType.AIST_UNIVERSAL];
	}
	
	public boolean hasCOTSkill() {
		return aiSkillChecks[AISkillType.AIST_COT];
	}
	
	public boolean hasManaHealSkill() {
		return aiSkillChecks[AISkillType.AIST_MANA];
	}
	
	public boolean hasAutoLrangeSkill() {
		return aiSkillChecks[AISkillType.AIST_LONG_RANGE];
	}
	
	public boolean hasAutoSrangeSkill() {
		return aiSkillChecks[AISkillType.AIST_SHORT_RANGE];
	}
	
	public boolean hasSkill() {
		return aiSkillChecks[AISkillType.AIST_GENERAL];
	}
	
	public L2NpcRace getRace() {
		if (Race == null) {
			Race = L2NpcRace.NONE;
		}
		
		return Race;
	}
	
	public boolean isCustom() {
		return NpcId != TemplateId;
	}
	
	/**
	 * @return name
	 */
	public String getName() {
		return Name;
	}
	
	public boolean isSpecialTree() {
		return NpcId == XmassTreeInstance.SPECIAL_TREE_ID;
	}
	
	public boolean isUndead() {
		return Race == L2NpcRace.UNDEAD;
	}
	
	public StatsSet getBaseSet() {
		return baseSet;
	}
	
	public NpcTemplate getBaseTemplate() {
		return baseTemplate;
	}
	
	public String getXmlNpcId() {
		return " id=\"" + NpcId + "\"";
	}
	
	public String getXmlTemplateId() {
		if (TemplateId == 0 || TemplateId == NpcId) {
			return "";
		}
		return " templateId=\"" + TemplateId + "\"";
	}
	
	public String getXmlName() {
		return " name=\"" + Name.replace("\"", "\\\"").replace("&", "&amp;") + "\"";
	}
	
	public String getXmlServerSideName() {
		if (!ServerSideName) {
			return "";
		}
		return " serverSideName=\"true\"";
	}
	
	public String getXmlTitle() {
		if (Title.length() == 0) {
			return "";
		}
		return " title=\"" + Title.replace("\"", "\\\"").replace("&", "&amp;") + "\"";
	}
	
	public String getXmlServerSideTitle() {
		if (!ServerSideTitle) {
			return "";
		}
		return " serverSideTitle=\"true\"";
	}
	
	public String getXmlInteractionDistance() {
		if (InteractionDistance == 150) {
			return "";
		}
		return " interactionDistance=\"" + InteractionDistance + "\"";
	}
	
	public String getXmlLevel() {
		return " level=\"" + Level + "\"";
	}
	
	public String getXmlType() {
		return " type=\"" + Type + "\"";
	}
	
	public String getXmlAttackRange() {
		return " atkRange=\"" + getBaseAtkRange() + "\"";
	}
	
	public String getXmlMaxHp() {
		return " hpMax=\"" + Math.round(getBaseHpMax()) + "\"";
	}
	
	public String getXmlMaxMp() {
		return " mpMax=\"" + Math.round(getBaseMpMax()) + "\"";
	}
	
	public String getXmlHpReg() {
		return " hpReg=\"" + getBaseHpReg() + "\"";
	}
	
	public String getXmlMpReg() {
		return " mpReg=\"" + getBaseMpReg() + "\"";
	}
	
	public String getXmlSTR() {
		return " STR=\"" + getBaseSTR() + "\"";
	}
	
	public String getXmlCON() {
		return " CON=\"" + getBaseCON() + "\"";
	}
	
	public String getXmlDEX() {
		return " DEX=\"" + getBaseDEX() + "\"";
	}
	
	public String getXmlINT() {
		return " INT=\"" + getBaseINT() + "\"";
	}
	
	public String getXmlWIT() {
		return " WIT=\"" + getBaseWIT() + "\"";
	}
	
	public String getXmlMEN() {
		return " MEN=\"" + getBaseMEN() + "\"";
	}
	
	public String getXmlExp() {
		if (RewardExp == 0) {
			return "";
		}
		return " exp=\"" + RewardExp + "\"";
	}
	
	public String getXmlSp() {
		if (RewardSp == 0) {
			return "";
		}
		return " sp=\"" + RewardSp + "\"";
	}
	
	public String getXmlPAtk() {
		return " pAtk=\"" + Math.round(getBasePAtk()) + "\"";
	}
	
	public String getXmlPDef() {
		return " pDef=\"" + Math.round(getBasePDef()) + "\"";
	}
	
	public String getXmlMAtk() {
		return " mAtk=\"" + Math.round(getBaseMAtk()) + "\"";
	}
	
	public String getXmlMDef() {
		return " mDef=\"" + Math.round(getBaseMDef()) + "\"";
	}
	
	public String getXmlPAtkSpd() {
		return " pAtkSpd=\"" + getBasePAtkSpd() + "\"";
	}
	
	public String getXmlMAtkSpd() {
		return " mAtkSpd=\"" + getBaseMAtkSpd() + "\"";
	}
	
	public String getXmlCritical() {
		return " pCritRate=\"" + getBaseCritRate() + "\"";
	}
	
	public String getXmlMCritical() {
		return " mCritRate=\"" + getBaseMCritRate() + "\"";
	}
	
	public String getXmlCanSeeThroughSilentMove() {
		if (!CanSeeThroughSilentMove) {
			return "";
		}
		return " canSeeThroughSilentMove=\"true\"";
	}
	
	public String getXmlCanBeChampion() {
		if (canBeChampion) {
			return "";
		}
		return " canBeChampion=\"false\"";
	}
	
	public String getXmlIsLethalImmune() {
		if (!isLethalImmune) {
			return "";
		}
		return " isLethalImmune=\"true\"";
	}
	
	public String getXmlIsDebuffImmune() {
		if (!isDebuffImmune) {
			return "";
		}
		return " isDebuffImmune=\"true\"";
	}
	
	public String getXmlAggessive() {
		if (!Aggressive) {
			return "";
		}
		return " aggressive=\"true\"";
	}
	
	public String getXmlAggroRange() {
		if (AggroRange == 0) {
			return "";
		}
		return " aggroRange=\"" + AggroRange + "\"";
	}
	
	public String getXmlRHand() {
		if (RHand == 0) {
			return "";
		}
		return " rHand=\"" + RHand + "\"";
	}
	
	public String getXmlLHand() {
		if (LHand == 0) {
			return "";
		}
		return " lHand=\"" + LHand + "\"";
	}
	
	public String getXmlWalkSpd() {
		return " walkSpd=\"" + Math.round(getBaseWalkSpd()) + "\"";
	}
	
	public String getXmlRunSpd() {
		return " runSpd=\"" + Math.round(getBaseRunSpd()) + "\"";
	}
	
	public String getXmlRandomWalk() {
		if (!RandomWalk) {
			return "";
		}
		return " randomWalk=\"true\"";
	}
	
	public String getXmlTargetable() {
		if (Targetable) {
			return "";
		}
		return " targetable=\"false\"";
	}
	
	public String getXmlIsNonTalking() {
		if (!IsNonTalking) {
			return "";
		}
		return " isNonTalking=\"true\"";
	}
	
	public String getXmlShowName() {
		if (ShowName) {
			return "";
		}
		return " showName=\"false\"";
	}
	
	public String getXmlExtraDropGroup() {
		if (ExtraDropGroup == 0) {
			return "";
		}
		return " extraDropGroup=\"" + ExtraDropGroup + "\"";
	}
	
	public String getXmlCollisionRadius() {
		return " collisionRadius=\"" + new DecimalFormat("#.##").format(getFCollisionRadius()) + "\"";
	}
	
	public String getXmlCollisionHeight() {
		return " collisionHeight=\"" + new DecimalFormat("#.##").format(getFCollisionHeight()) + "\"";
	}
	
	public String getXmlElemAtk() {
		int elem = -1;
		int val = 0;
		if (getBaseFire() > val) {
			elem = Elementals.FIRE;
			val = getBaseFire();
		}
		if (getBaseWater() > val) {
			elem = Elementals.WATER;
			val = getBaseWater();
		}
		if (getBaseWind() > val) {
			elem = Elementals.WIND;
			val = getBaseWind();
		}
		if (getBaseEarth() > val) {
			elem = Elementals.EARTH;
			val = getBaseEarth();
		}
		if (getBaseHoly() > val) {
			elem = Elementals.HOLY;
			val = getBaseHoly();
		}
		if (getBaseDark() > val) {
			elem = Elementals.DARK;
			val = getBaseDark();
		}
		if (elem == -1) {
			return "";
		}
		return " elemAtkType=\"" + elem + "\" elemAtkValue=\"" + val + "\"";
	}
	
	public String getXmlElemRes() {
		return " fireRes=\"" + Math.round(getBaseFireRes()) + "\" waterRes=\"" + Math.round(getBaseWaterRes()) + "\" windRes=\"" + Math.round(
				getBaseWindRes()) +
				"\"" + " earthRes=\"" + Math.round(getBaseEarthRes()) + "\" holyRes=\"" + Math.round(getBaseHolyRes()) + "\" darkRes=\"" +
				Math.round(getBaseDarkRes()) + "\"";
	}
	
	public String getXmlAIType() {
		return " aiType=\"" + getAIData().getAiType().name().toLowerCase() + "\"";
	}
	
	public String getXmlSkillChance() {
		if (getAIData().getSkillChance() == 0) {
			return "";
		}
		return " skillChance=\"" + getAIData().getSkillChance() + "\"";
	}
	
	public String getXmlPrimaryAttack() {
		if (getAIData().getPrimaryAttack() == 0) {
			return "";
		}
		return " primaryAttack=\"" + getAIData().getPrimaryAttack() + "\"";
	}
	
	public String getXmlCanMove() {
		if (getAIData().canMove()) {
			return "";
		}
		return " canMove=\"false\"";
	}
	
	public String getXmlMinRangeSkill() {
		if (getAIData().getShortRangeSkill() == 0) {
			return "";
		}
		return " minRangeSkill=\"" + getAIData().getShortRangeSkill() + "\"";
	}
	
	public String getXmlMinRangeChance() {
		if (getAIData().getShortRangeChance() == 0) {
			return "";
		}
		return " minRangeChance=\"" + getAIData().getShortRangeChance() + "\"";
	}
	
	public String getXmlMaxRangeSkill() {
		if (getAIData().getLongRangeSkill() == 0) {
			return "";
		}
		return " maxRangeSkill=\"" + getAIData().getLongRangeSkill() + "\"";
	}
	
	public String getXmlMaxRangeChance() {
		if (getAIData().getLongRangeChance() == 0) {
			return "";
		}
		return " maxRangeChance=\"" + getAIData().getLongRangeChance() + "\"";
	}
	
	public String getXmlSoulshots() {
		if (getAIData().getSoulShot() == 0) {
			return "";
		}
		return " soulshots=\"" + getAIData().getSoulShot() + "\"";
	}
	
	public String getXmlSpiritshots() {
		if (getAIData().getSpiritShot() == 0) {
			return "";
		}
		return " spiritshots=\"" + getAIData().getSpiritShot() + "\"";
	}
	
	public String getXmlSSChance() {
		if (getAIData().getSoulShotChance() == 0) {
			return "";
		}
		return " ssChance=\"" + getAIData().getSoulShotChance() + "\"";
	}
	
	public String getXmlSpSChance() {
		if (getAIData().getSpiritShotChance() == 0) {
			return "";
		}
		return " spsChance=\"" + getAIData().getSpiritShotChance() + "\"";
	}
	
	public String getXmlIsChaos() {
		if (getAIData().getIsChaos() == 0) {
			return "";
		}
		return " isChaos=\"" + getAIData().getIsChaos() + "\"";
	}
	
	public String getXmlClan() {
		if (getAIData().getClan() == null || getAIData().getClan().length() == 0 || getAIData().getClan().equalsIgnoreCase("null")) {
			return "";
		}
		return " clan=\"" + getAIData().getClan() + "\"";
	}
	
	public String getXmlClanRange() {
		if (getAIData().getClanRange() == 0) {
			return "";
		}
		return " clanRange=\"" + getAIData().getClanRange() + "\"";
	}
	
	public String getXmlEnemy() {
		if (getAIData().getEnemyClan() == null || getAIData().getEnemyClan().length() == 0 || getAIData().getEnemyClan().equalsIgnoreCase("null")) {
			return "";
		}
		return " enemyClan=\"" + getAIData().getEnemyClan() + "\"";
	}
	
	public String getXmlEnemyRange() {
		if (getAIData().getEnemyRange() == 0) {
			return "";
		}
		return " enemyRange=\"" + getAIData().getEnemyRange() + "\"";
	}
	
	public String getXmlDodge() {
		if (getAIData().getDodge() == 0) {
			return "";
		}
		return " dodge=\"" + getAIData().getDodge() + "\"";
	}
	
	public String getXmlMinSocial1() {
		if (getAIData().getMinSocial(false) == -1) {
			return "";
		}
		return " minSocial1=\"" + getAIData().getMinSocial(false) + "\"";
	}
	
	public String getXmlMaxSocial1() {
		if (getAIData().getMaxSocial(false) == -1) {
			return "";
		}
		return " maxSocial1=\"" + getAIData().getMaxSocial(false) + "\"";
	}
	
	public String getXmlMinSocial2() {
		if (getAIData().getMinSocial(true) == -1) {
			return "";
		}
		return " minSocial2=\"" + getAIData().getMinSocial(true) + "\"";
	}
	
	public String getXmlMaxSocial2() {
		if (getAIData().getMaxSocial(true) == -1) {
			return "";
		}
		return " maxSocial2=\"" + getAIData().getMaxSocial(true) + "\"";
	}
	
	private List<L2Spawn> knownSpawns = new ArrayList<>();
	
	public final void addKnownSpawn(final L2Spawn spawn) {
		knownSpawns.add(spawn);
	}
	
	public final List<L2Spawn> getKnownSpawns() {
		return knownSpawns;
	}
	
	public void onSpawn(L2Spawn spawn) {
		synchronized (allSpawns) {
			allSpawns.add(spawn);
		}
	}
	
	public void onUnSpawn(L2Spawn spawn) {
		synchronized (allSpawns) {
			allSpawns.remove(spawn);
		}
	}
	
	public List<L2Spawn> getAllSpawns() {
		return allSpawns;
	}
}
