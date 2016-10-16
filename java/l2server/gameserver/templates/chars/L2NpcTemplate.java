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
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2XmassTreeInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.Quest.QuestEventType;
import l2server.gameserver.templates.SpawnData;
import l2server.gameserver.templates.StatsSet;
import l2server.log.Log;
import lombok.Getter;

import java.text.DecimalFormat;
import java.util.*;

/**
 * This cl contains all generic data of a L2Spawn object.<BR><BR>
 * <p>
 * <B><U> Data</U> :</B><BR><BR>
 * <li>npcId, type, name, sex</li>
 * <li>rewardExp, rewardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><BR><BR>
 *
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2NpcTemplate extends L2CharTemplate
{
	public static final int AIST_BUFF = 0;
	public static final int AIST_NEGATIVE = 1;
	public static final int AIST_DEBUFF = 2;
	public static final int AIST_ATK = 3;
	public static final int AIST_ROOT = 4;
	public static final int AIST_STUN = 5;
	public static final int AIST_SLEEP = 6;
	public static final int AIST_PARALYZE = 7;
	public static final int AIST_FOSSIL = 8;
	public static final int AIST_FLOAT = 9;
	public static final int AIST_IMMOBILIZE = 10;
	public static final int AIST_HEAL = 11;
	public static final int AIST_RES = 12;
	public static final int AIST_DOT = 13;
	public static final int AIST_COT = 14;
	public static final int AIST_UNIVERSAL = 15;
	public static final int AIST_MANA = 16;
	public static final int AIST_LONG_RANGE = 17;
	public static final int AIST_SHORT_RANGE = 18;
	public static final int AIST_GENERAL = 19;
	public static final int AIST_COUNT = 20;

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
	@SuppressWarnings("unchecked") public List<L2Skill>[] aiSkills = new List[AIST_COUNT];
	public boolean[] aiSkillChecks = new boolean[AIST_COUNT];

	private L2NpcAIData aiData = new L2NpcAIData();

	public enum AIType
	{
		FIGHTER, ARCHER, BALANCED, MAGE, HEALER, CORPSE
	}

	public enum L2NpcRace
	{
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
	 * The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate
	 */
	private List<L2MinionData> minions = null;
	private L2RandomMinionData randomMinions = null;
	@Getter private Map<Integer, L2Skill> skills = null;
	@Getter private List<SpawnData> spawns = new ArrayList<>();
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private Map<QuestEventType, Quest[]> questEvents = null;

	@Getter private StatsSet baseSet;
	@Getter private L2NpcTemplate baseTemplate;

	@Getter private final List<L2Spawn> allSpawns = new ArrayList<>();

	/**
	 * Constructor of L2Character.<BR><BR>
	 *
	 * @param set The StatsSet object to transfer data to the method
	 */
	public L2NpcTemplate(StatsSet set)
	{
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
		if (herbGroup > 0 && ExtraDropTable.getInstance().getExtraDroplist(herbGroup) == null)
		{
			Log.warning("Missing Herb Drop Group for npcId: " + NpcId);
			ExtraDropGroup = 0;
		}
		else
		{
			ExtraDropGroup = herbGroup;
		}
		Targetable = set.getBool("targetable", true);
		IsNonTalking = set.getBool("isNonTalking", false);
		ShowName = set.getBool("showName", true);

		// can be loaded from db
		BaseVitalityDivider =
				Level > 0 && RewardExp > 0 ? (float) baseHpMax * 9 / (100 * RewardExp / (Level * Level)) : 0;

		InteractionDistance = set.getInteger("interactionDistance", L2Npc.DEFAULT_INTERACTION_DISTANCE);

		boolean bonusByDefault = false;
		BonusFromBaseStats = set.getBool("bonusFromBaseStats", bonusByDefault);
		FixedAccuracy = set.getInteger("fixedAccuracy", 0);
		FixedEvasion = set.getInteger("fixedEvasion", 0);
		HatersDamageMultiplier = set.getFloat("hatersDamageMultiplier", 0);

		baseSet = set;
		baseTemplate = this;

		if (Config.isServer(Config.TENKAI_ESTHUS) && Type.equals("L2Defender"))
		{
			Level = 103;
		}
	}

	public L2NpcTemplate(StatsSet set, L2NpcTemplate baseTemplate)
	{
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
		if (herbGroup > 0 && ExtraDropTable.getInstance().getExtraDroplist(herbGroup) == null)
		{
			Log.warning("Missing Herb Drop Group for npcId: " + NpcId);
			ExtraDropGroup = 0;
		}
		else
		{
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
		BaseVitalityDivider =
				Level > 0 && RewardExp > 0 ? (float) baseHpMax * 9 / (100 * RewardExp / (Level * Level)) : 0;

		InteractionDistance = set.getInteger("interactionDistance", baseTemplate.InteractionDistance);

		baseFire = baseTemplate.baseFire;
		baseWater = baseTemplate.baseWater;
		baseEarth = baseTemplate.baseEarth;
		baseWind = baseTemplate.baseWind;
		baseHoly = baseTemplate.baseHoly;
		baseDark = baseTemplate.baseDark;
		baseFireRes = baseTemplate.baseFireRes;
		baseWaterRes = baseTemplate.baseWaterRes;
		baseEarthRes = baseTemplate.baseEarthRes;
		baseWindRes = baseTemplate.baseWindRes;
		baseHolyRes = baseTemplate.baseHolyRes;
		baseDarkRes = baseTemplate.baseDarkRes;

		aiData = baseTemplate.aiData;

		if (!set.getBool("overrideDrops", false))
		{
			spoilDrop = new ArrayList<>(baseTemplate.spoilDrop);
			normalDrop = new ArrayList<>(baseTemplate.normalDrop);
			multiDrop = new ArrayList<>();
			for (L2DropCategory dc : baseTemplate.multiDrop)
			{
				L2DropCategory newDC = new L2DropCategory(dc.getChance());
				for (L2DropData dd : dc.getAllDrops())
				{
					newDC.addDropData(dd);
				}
				multiDrop.add(newDC);
			}
		}

		if (baseTemplate.minions != null)
		{
			minions = new ArrayList<>(baseTemplate.minions);
		}

		if (baseTemplate.randomMinions != null)
		{
			randomMinions = new L2RandomMinionData(baseTemplate.randomMinions);
		}

		if (baseTemplate.skills != null)
		{
			for (L2Skill skill : baseTemplate.skills.values())
			{
				addSkill(skill);
			}
		}

		if (!set.getBool("overrideSpawns", false))
		{
			spawns = new ArrayList<>(baseTemplate.spawns);
		}

		if (baseTemplate.questEvents != null)
		{
			questEvents = new HashMap<>(baseTemplate.questEvents);
		}

		baseSet = set;
		while (baseTemplate != baseTemplate.baseTemplate)
		{
			baseTemplate = baseTemplate.baseTemplate;
		}
		this.baseTemplate = baseTemplate;
	}

	public void addSpoilData(L2DropData drop)
	{
		spoilDrop.add(drop);
	}

	public void addDropData(L2DropData drop)
	{
		normalDrop.add(drop);
	}

	public void addMultiDrop(L2DropCategory category)
	{
		multiDrop.add(category);
	}

	public void addRaidData(L2MinionData minion)
	{
		if (minions == null)
		{
			minions = new ArrayList<>();
		}
		minions.add(minion);
	}

	public void setRandomRaidData(L2RandomMinionData minion)
	{
		randomMinions = minion;
	}

	public void addSkill(L2Skill skill)
	{
		if (skills == null)
		{
			skills = new LinkedHashMap<>();
		}

		if (!skill.isPassive())
		{
			addGeneralSkill(skill);
			switch (skill.getSkillType())
			{
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

	public void addSpawn(SpawnData spawn)
	{
		spawns.add(spawn);
	}

	public ArrayList<L2DropData> getSpoilData()
	{
		return spoilDrop;
	}

	public ArrayList<L2DropData> getDropData()
	{
		return normalDrop;
	}

	/**
	 * Return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.<BR><BR>
	 */
	public ArrayList<L2DropCategory> getMultiDropData()
	{
		return multiDrop;
	}

	/**
	 * Return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.<BR><BR>
	 */
	public List<L2MinionData> getMinionData()
	{
		return minions;
	}

	public L2MinionData getMinionData(final int minionId)
	{
		if (minions == null)
		{
			return null;
		}

		for (L2MinionData minion : minions)
		{
			if (minionId != minion.getMinionId())
			{
				continue;
			}

			return minion;
		}

		return null;
	}

	public L2RandomMinionData getRandomMinionData()
	{
		return randomMinions;
	}

	public void addQuestEvent(Quest.QuestEventType EventType, Quest q)
	{
		if (questEvents == null)
		{
			questEvents = new HashMap<>();
		}

		if (questEvents.get(EventType) == null)
		{
			questEvents.put(EventType, new Quest[]{q});
		}
		else
		{
			Quest[] quests = questEvents.get(EventType);
			int len = quests.length;

			// if only one registration per npc is allowed for this event type
			// then only register this NPC if not already registered for the specified event.
			// if a quest allows multiple registrations, then register regardless of count
			// In all cases, check if this new registration is replacing an older copy of the SAME quest
			// Finally, check quest class hierarchy: a parent class should never replace a child class.
			// a child class should always replace a parent class.
			if (!EventType.isMultipleRegistrationAllowed())
			{
				// if it is the same quest (i.e. reload) or the existing is a superclass of the new one, replace the existing.
				if (quests[0].getName().equals(q.getName()) || L2NpcTemplate.isAssignableTo(q, quests[0].getClass()))
				{
					quests[0] = q;
				}
				else
				{
					Log.warning("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" +
							EventType + "\" for NPC \"" + Name + "\" and quest \"" + q.getName() + "\".");
				}
			}
			else
			{
				// be ready to add a new quest to a new copy of the list, with larger size than previously.
				Quest[] tmp = new Quest[len + 1];

				// loop through the existing quests and copy them to the new list.  While doing so, also
				// check if this new quest happens to be just a replacement for a previously loaded quest.
				// Replace existing if the new quest is the same (reload) or a child of the existing quest.
				// Do nothing if the new quest is a superclass of an existing quest.
				// Add the new quest in the end of the list otherwise.
				for (int i = 0; i < len; i++)
				{
					if (quests[i].getName().equals(q.getName()) ||
							L2NpcTemplate.isAssignableTo(q, quests[i].getClass()))
					{
						quests[i] = q;
						return;
					}
					else if (L2NpcTemplate.isAssignableTo(quests[i], q.getClass()))
					{
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
	 * Checks if obj can be assigned to the Class represented by clazz.<br>
	 * This is true if, and only if, obj is the same class represented by clazz,
	 * or a subclass of it or obj implements the interface represented by clazz.
	 *
	 * @param obj
	 * @param clazz
	 * @return
	 */
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return L2NpcTemplate.isAssignableTo(obj.getClass(), clazz);
	}

	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz)
	{
		// if clazz represents an interface
		if (clazz.isInterface())
		{
			// check if obj implements the clazz interface
			Class<?>[] interfaces = sub.getInterfaces();
			for (Class<?> interface1 : interfaces)
			{
				if (clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			do
			{
				if (sub.getName().equals(clazz.getName()))
				{
					return true;
				}

				sub = sub.getSuperclass();
			}
			while (sub != null);
		}

		return false;
	}

	public Quest[] getEventQuests(Quest.QuestEventType EventType)
	{
		if (questEvents == null)
		{
			return null;
		}

		return questEvents.get(EventType);
	}

	public void setRace(int raceId)
	{
		switch (raceId)
		{
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
	public void setAIData(L2NpcAIData aidata)
	{
		//_AIdataStatic = new L2NpcAIData(); // not needed to init object and in next line override with other reference. maybe other intention?
		aiData = aidata;
	}

	//-----------------------------------------------------------------------

	public L2NpcAIData getAIData()
	{
		return aiData;
	}

	public void addBuffSkill(L2Skill skill)
	{
		if (aiSkills[AIST_BUFF] == null)
		{
			aiSkills[AIST_BUFF] = new ArrayList<>();
		}
		aiSkills[AIST_BUFF].add(skill);
		aiSkillChecks[AIST_BUFF] = true;
	}

	public void addHealSkill(L2Skill skill)
	{
		if (aiSkills[AIST_HEAL] == null)
		{
			aiSkills[AIST_HEAL] = new ArrayList<>();
		}
		aiSkills[AIST_HEAL].add(skill);
		aiSkillChecks[AIST_HEAL] = true;
	}

	public void addResSkill(L2Skill skill)
	{
		if (aiSkills[AIST_RES] == null)
		{
			aiSkills[AIST_RES] = new ArrayList<>();
		}
		aiSkills[AIST_RES].add(skill);
		aiSkillChecks[AIST_RES] = true;
	}

	public void addAtkSkill(L2Skill skill)
	{
		if (aiSkills[AIST_ATK] == null)
		{
			aiSkills[AIST_ATK] = new ArrayList<>();
		}
		aiSkills[AIST_ATK].add(skill);
		aiSkillChecks[AIST_ATK] = true;
	}

	public void addDebuffSkill(L2Skill skill)
	{
		if (aiSkills[AIST_DEBUFF] == null)
		{
			aiSkills[AIST_DEBUFF] = new ArrayList<>();
		}
		aiSkills[AIST_DEBUFF].add(skill);
		aiSkillChecks[AIST_DEBUFF] = true;
	}

	public void addRootSkill(L2Skill skill)
	{
		if (aiSkills[AIST_ROOT] == null)
		{
			aiSkills[AIST_ROOT] = new ArrayList<>();
		}
		aiSkills[AIST_ROOT].add(skill);
		aiSkillChecks[AIST_ROOT] = true;
	}

	public void addSleepSkill(L2Skill skill)
	{
		if (aiSkills[AIST_SLEEP] == null)
		{
			aiSkills[AIST_SLEEP] = new ArrayList<>();
		}
		aiSkills[AIST_SLEEP].add(skill);
		aiSkillChecks[AIST_SLEEP] = true;
	}

	public void addStunSkill(L2Skill skill)
	{
		if (aiSkills[AIST_STUN] == null)
		{
			aiSkills[AIST_STUN] = new ArrayList<>();
		}
		aiSkills[AIST_STUN].add(skill);
		aiSkillChecks[AIST_STUN] = true;
	}

	public void addParalyzeSkill(L2Skill skill)
	{
		if (aiSkills[AIST_PARALYZE] == null)
		{
			aiSkills[AIST_PARALYZE] = new ArrayList<>();
		}
		aiSkills[AIST_PARALYZE].add(skill);
		aiSkillChecks[AIST_PARALYZE] = true;
	}

	public void addFloatSkill(L2Skill skill)
	{
		if (aiSkills[AIST_FLOAT] == null)
		{
			aiSkills[AIST_FLOAT] = new ArrayList<>();
		}
		aiSkills[AIST_FLOAT].add(skill);
		aiSkillChecks[AIST_FLOAT] = true;
	}

	public void addFossilSkill(L2Skill skill)
	{
		if (aiSkills[AIST_FOSSIL] == null)
		{
			aiSkills[AIST_FOSSIL] = new ArrayList<>();
		}
		aiSkills[AIST_FOSSIL].add(skill);
		aiSkillChecks[AIST_FOSSIL] = true;
	}

	public void addNegativeSkill(L2Skill skill)
	{
		if (aiSkills[AIST_NEGATIVE] == null)
		{
			aiSkills[AIST_NEGATIVE] = new ArrayList<>();
		}
		aiSkills[AIST_NEGATIVE].add(skill);
		aiSkillChecks[AIST_NEGATIVE] = true;
	}

	public void addImmobilizeSkill(L2Skill skill)
	{
		if (aiSkills[AIST_IMMOBILIZE] == null)
		{
			aiSkills[AIST_IMMOBILIZE] = new ArrayList<>();
		}
		aiSkills[AIST_IMMOBILIZE].add(skill);
		aiSkillChecks[AIST_IMMOBILIZE] = true;
	}

	public void addDOTSkill(L2Skill skill)
	{
		if (aiSkills[AIST_DOT] == null)
		{
			aiSkills[AIST_DOT] = new ArrayList<>();
		}
		aiSkills[AIST_DOT].add(skill);
		aiSkillChecks[AIST_DOT] = true;
	}

	public void addUniversalSkill(L2Skill skill)
	{
		if (aiSkills[AIST_UNIVERSAL] == null)
		{
			aiSkills[AIST_UNIVERSAL] = new ArrayList<>();
		}
		aiSkills[AIST_UNIVERSAL].add(skill);
		aiSkillChecks[AIST_UNIVERSAL] = true;
	}

	public void addCOTSkill(L2Skill skill)
	{
		if (aiSkills[AIST_COT] == null)
		{
			aiSkills[AIST_COT] = new ArrayList<>();
		}
		aiSkills[AIST_COT].add(skill);
		aiSkillChecks[AIST_COT] = true;
	}

	public void addManaHealSkill(L2Skill skill)
	{
		if (aiSkills[AIST_MANA] == null)
		{
			aiSkills[AIST_MANA] = new ArrayList<>();
		}
		aiSkills[AIST_MANA].add(skill);
		aiSkillChecks[AIST_MANA] = true;
	}

	public void addGeneralSkill(L2Skill skill)
	{
		if (aiSkills[AIST_GENERAL] == null)
		{
			aiSkills[AIST_GENERAL] = new ArrayList<>();
		}
		aiSkills[AIST_GENERAL].add(skill);
		aiSkillChecks[AIST_GENERAL] = true;
	}

	public void addRangeSkill(L2Skill skill)
	{
		if (skill.getCastRange() <= 150 && skill.getCastRange() > 0)
		{
			if (aiSkills[AIST_SHORT_RANGE] == null)
			{
				aiSkills[AIST_SHORT_RANGE] = new ArrayList<>();
			}
			aiSkills[AIST_SHORT_RANGE].add(skill);
			aiSkillChecks[AIST_SHORT_RANGE] = true;
		}
		else if (skill.getCastRange() > 150)
		{
			if (aiSkills[AIST_LONG_RANGE] == null)
			{
				aiSkills[AIST_LONG_RANGE] = new ArrayList<>();
			}
			aiSkills[AIST_LONG_RANGE].add(skill);
			aiSkillChecks[AIST_LONG_RANGE] = true;
		}
	}

	//--------------------------------------------------------------------
	public boolean hasBuffSkill()
	{
		return aiSkillChecks[AIST_BUFF];
	}

	public boolean hasHealSkill()
	{
		return aiSkillChecks[AIST_HEAL];
	}

	public boolean hasResSkill()
	{
		return aiSkillChecks[AIST_RES];
	}

	public boolean hasAtkSkill()
	{
		return aiSkillChecks[AIST_ATK];
	}

	public boolean hasDebuffSkill()
	{
		return aiSkillChecks[AIST_DEBUFF];
	}

	public boolean hasRootSkill()
	{
		return aiSkillChecks[AIST_ROOT];
	}

	public boolean hasSleepSkill()
	{
		return aiSkillChecks[AIST_SLEEP];
	}

	public boolean hasStunSkill()
	{
		return aiSkillChecks[AIST_STUN];
	}

	public boolean hasParalyzeSkill()
	{
		return aiSkillChecks[AIST_PARALYZE];
	}

	public boolean hasFloatSkill()
	{
		return aiSkillChecks[AIST_FLOAT];
	}

	public boolean hasFossilSkill()
	{
		return aiSkillChecks[AIST_FOSSIL];
	}

	public boolean hasNegativeSkill()
	{
		return aiSkillChecks[AIST_NEGATIVE];
	}

	public boolean hasImmobiliseSkill()
	{
		return aiSkillChecks[AIST_IMMOBILIZE];
	}

	public boolean hasDOTSkill()
	{
		return aiSkillChecks[AIST_DOT];
	}

	public boolean hasUniversalSkill()
	{
		return aiSkillChecks[AIST_UNIVERSAL];
	}

	public boolean hasCOTSkill()
	{
		return aiSkillChecks[AIST_COT];
	}

	public boolean hasManaHealSkill()
	{
		return aiSkillChecks[AIST_MANA];
	}

	public boolean hasAutoLrangeSkill()
	{
		return aiSkillChecks[AIST_LONG_RANGE];
	}

	public boolean hasAutoSrangeSkill()
	{
		return aiSkillChecks[AIST_SHORT_RANGE];
	}

	public boolean hasSkill()
	{
		return aiSkillChecks[AIST_GENERAL];
	}

	public L2NpcRace getRace()
	{
		if (Race == null)
		{
			Race = L2NpcRace.NONE;
		}

		return Race;
	}

	public boolean isCustom()
	{
		return NpcId != TemplateId;
	}

	/**
	 * @return name
	 */
	public String getName()
	{
		return Name;
	}

	public boolean isSpecialTree()
	{
		return NpcId == L2XmassTreeInstance.SPECIAL_TREE_ID;
	}

	public boolean isUndead()
	{
		return Race == L2NpcRace.UNDEAD;
	}

	public String getXmlNpcId()
	{
		return " id=\"" + NpcId + "\"";
	}

	public String getXmlTemplateId()
	{
		if (TemplateId == 0 || TemplateId == NpcId)
		{
			return "";
		}
		return " templateId=\"" + TemplateId + "\"";
	}

	public String getXmlName()
	{
		return " name=\"" + Name.replace("\"", "\\\"").replace("&", "&amp;") + "\"";
	}

	public String getXmlServerSideName()
	{
		if (!ServerSideName)
		{
			return "";
		}
		return " serverSideName=\"true\"";
	}

	public String getXmlTitle()
	{
		if (Title.length() == 0)
		{
			return "";
		}
		return " title=\"" + Title.replace("\"", "\\\"").replace("&", "&amp;") + "\"";
	}

	public String getXmlServerSideTitle()
	{
		if (!ServerSideTitle)
		{
			return "";
		}
		return " serverSideTitle=\"true\"";
	}

	public String getXmlInteractionDistance()
	{
		if (InteractionDistance == 150)
		{
			return "";
		}
		return " interactionDistance=\"" + InteractionDistance + "\"";
	}

	public String getXmlLevel()
	{
		return " level=\"" + Level + "\"";
	}

	public String getXmlType()
	{
		return " type=\"" + Type + "\"";
	}

	public String getXmlAttackRange()
	{
		return " atkRange=\"" + baseAtkRange + "\"";
	}

	public String getXmlMaxHp()
	{
		return " hpMax=\"" + Math.round(baseHpMax) + "\"";
	}

	public String getXmlMaxMp()
	{
		return " mpMax=\"" + Math.round(baseMpMax) + "\"";
	}

	public String getXmlHpReg()
	{
		return " hpReg=\"" + baseHpReg + "\"";
	}

	public String getXmlMpReg()
	{
		return " mpReg=\"" + baseMpReg + "\"";
	}

	public String getXmlSTR()
	{
		return " STR=\"" + baseSTR + "\"";
	}

	public String getXmlCON()
	{
		return " CON=\"" + baseCON + "\"";
	}

	public String getXmlDEX()
	{
		return " DEX=\"" + baseDEX + "\"";
	}

	public String getXmlINT()
	{
		return " INT=\"" + baseINT + "\"";
	}

	public String getXmlWIT()
	{
		return " WIT=\"" + baseWIT + "\"";
	}

	public String getXmlMEN()
	{
		return " MEN=\"" + baseMEN + "\"";
	}

	public String getXmlExp()
	{
		if (RewardExp == 0)
		{
			return "";
		}
		return " exp=\"" + RewardExp + "\"";
	}

	public String getXmlSp()
	{
		if (RewardSp == 0)
		{
			return "";
		}
		return " sp=\"" + RewardSp + "\"";
	}

	public String getXmlPAtk()
	{
		return " pAtk=\"" + Math.round(basePAtk) + "\"";
	}

	public String getXmlPDef()
	{
		return " pDef=\"" + Math.round(basePDef) + "\"";
	}

	public String getXmlMAtk()
	{
		return " mAtk=\"" + Math.round(baseMAtk) + "\"";
	}

	public String getXmlMDef()
	{
		return " mDef=\"" + Math.round(baseMDef) + "\"";
	}

	public String getXmlPAtkSpd()
	{
		return " pAtkSpd=\"" + basePAtkSpd + "\"";
	}

	public String getXmlMAtkSpd()
	{
		return " mAtkSpd=\"" + baseMAtkSpd + "\"";
	}

	public String getXmlCritical()
	{
		return " pCritRate=\"" + baseCritRate + "\"";
	}

	public String getXmlMCritical()
	{
		return " mCritRate=\"" + baseMCritRate + "\"";
	}

	public String getXmlCanSeeThroughSilentMove()
	{
		if (!CanSeeThroughSilentMove)
		{
			return "";
		}
		return " canSeeThroughSilentMove=\"true\"";
	}

	public String getXmlCanBeChampion()
	{
		if (canBeChampion)
		{
			return "";
		}
		return " canBeChampion=\"false\"";
	}

	public String getXmlIsLethalImmune()
	{
		if (!isLethalImmune)
		{
			return "";
		}
		return " isLethalImmune=\"true\"";
	}

	public String getXmlIsDebuffImmune()
	{
		if (!isDebuffImmune)
		{
			return "";
		}
		return " isDebuffImmune=\"true\"";
	}

	public String getXmlAggessive()
	{
		if (!Aggressive)
		{
			return "";
		}
		return " aggressive=\"true\"";
	}

	public String getXmlAggroRange()
	{
		if (AggroRange == 0)
		{
			return "";
		}
		return " aggroRange=\"" + AggroRange + "\"";
	}

	public String getXmlRHand()
	{
		if (RHand == 0)
		{
			return "";
		}
		return " rHand=\"" + RHand + "\"";
	}

	public String getXmlLHand()
	{
		if (LHand == 0)
		{
			return "";
		}
		return " lHand=\"" + LHand + "\"";
	}

	public String getXmlWalkSpd()
	{
		return " walkSpd=\"" + Math.round(baseWalkSpd) + "\"";
	}

	public String getXmlRunSpd()
	{
		return " runSpd=\"" + Math.round(baseRunSpd) + "\"";
	}

	public String getXmlRandomWalk()
	{
		if (!RandomWalk)
		{
			return "";
		}
		return " randomWalk=\"true\"";
	}

	public String getXmlTargetable()
	{
		if (Targetable)
		{
			return "";
		}
		return " targetable=\"false\"";
	}

	public String getXmlIsNonTalking()
	{
		if (!IsNonTalking)
		{
			return "";
		}
		return " isNonTalking=\"true\"";
	}

	public String getXmlShowName()
	{
		if (ShowName)
		{
			return "";
		}
		return " showName=\"false\"";
	}

	public String getXmlExtraDropGroup()
	{
		if (ExtraDropGroup == 0)
		{
			return "";
		}
		return " extraDropGroup=\"" + ExtraDropGroup + "\"";
	}

	public String getXmlCollisionRadius()
	{
		return " collisionRadius=\"" + new DecimalFormat("#.##").format(fCollisionRadius) + "\"";
	}

	public String getXmlCollisionHeight()
	{
		return " collisionHeight=\"" + new DecimalFormat("#.##").format(fCollisionHeight) + "\"";
	}

	public String getXmlElemAtk()
	{
		int elem = -1;
		int val = 0;
		if (baseFire > val)
		{
			elem = Elementals.FIRE;
			val = baseFire;
		}
		if (baseWater > val)
		{
			elem = Elementals.WATER;
			val = baseWater;
		}
		if (baseWind > val)
		{
			elem = Elementals.WIND;
			val = baseWind;
		}
		if (baseEarth > val)
		{
			elem = Elementals.EARTH;
			val = baseEarth;
		}
		if (baseHoly > val)
		{
			elem = Elementals.HOLY;
			val = baseHoly;
		}
		if (baseDark > val)
		{
			elem = Elementals.DARK;
			val = baseDark;
		}
		if (elem == -1)
		{
			return "";
		}
		return " elemAtkType=\"" + elem + "\" elemAtkValue=\"" + val + "\"";
	}

	public String getXmlElemRes()
	{
		return " fireRes=\"" + Math.round(baseFireRes) + "\" waterRes=\"" + Math.round(baseWaterRes) + "\" windRes=\"" +
				Math.round(baseWindRes) + "\"" + " earthRes=\"" + Math.round(baseEarthRes) + "\" holyRes=\"" +
				Math.round(baseHolyRes) + "\" darkRes=\"" + Math.round(baseDarkRes) + "\"";
	}

	public String getXmlAIType()
	{
		return " aiType=\"" + getAIData().getAiType().name().toLowerCase() + "\"";
	}

	public String getXmlSkillChance()
	{
		if (getAIData().getSkillChance() == 0)
		{
			return "";
		}
		return " skillChance=\"" + getAIData().getSkillChance() + "\"";
	}

	public String getXmlPrimaryAttack()
	{
		if (getAIData().getPrimaryAttack() == 0)
		{
			return "";
		}
		return " primaryAttack=\"" + getAIData().getPrimaryAttack() + "\"";
	}

	public String getXmlCanMove()
	{
		if (getAIData().canMove())
		{
			return "";
		}
		return " canMove=\"false\"";
	}

	public String getXmlMinRangeSkill()
	{
		if (getAIData().getShortRangeSkill() == 0)
		{
			return "";
		}
		return " minRangeSkill=\"" + getAIData().getShortRangeSkill() + "\"";
	}

	public String getXmlMinRangeChance()
	{
		if (getAIData().getShortRangeChance() == 0)
		{
			return "";
		}
		return " minRangeChance=\"" + getAIData().getShortRangeChance() + "\"";
	}

	public String getXmlMaxRangeSkill()
	{
		if (getAIData().getLongRangeSkill() == 0)
		{
			return "";
		}
		return " maxRangeSkill=\"" + getAIData().getLongRangeSkill() + "\"";
	}

	public String getXmlMaxRangeChance()
	{
		if (getAIData().getLongRangeChance() == 0)
		{
			return "";
		}
		return " maxRangeChance=\"" + getAIData().getLongRangeChance() + "\"";
	}

	public String getXmlSoulshots()
	{
		if (getAIData().getSoulShot() == 0)
		{
			return "";
		}
		return " soulshots=\"" + getAIData().getSoulShot() + "\"";
	}

	public String getXmlSpiritshots()
	{
		if (getAIData().getSpiritShot() == 0)
		{
			return "";
		}
		return " spiritshots=\"" + getAIData().getSpiritShot() + "\"";
	}

	public String getXmlSSChance()
	{
		if (getAIData().getSoulShotChance() == 0)
		{
			return "";
		}
		return " ssChance=\"" + getAIData().getSoulShotChance() + "\"";
	}

	public String getXmlSpSChance()
	{
		if (getAIData().getSpiritShotChance() == 0)
		{
			return "";
		}
		return " spsChance=\"" + getAIData().getSpiritShotChance() + "\"";
	}

	public String getXmlIsChaos()
	{
		if (getAIData().getIsChaos() == 0)
		{
			return "";
		}
		return " isChaos=\"" + getAIData().getIsChaos() + "\"";
	}

	public String getXmlClan()
	{
		if (getAIData().getClan() == null || getAIData().getClan().length() == 0 ||
				getAIData().getClan().equalsIgnoreCase("null"))
		{
			return "";
		}
		return " clan=\"" + getAIData().getClan() + "\"";
	}

	public String getXmlClanRange()
	{
		if (getAIData().getClanRange() == 0)
		{
			return "";
		}
		return " clanRange=\"" + getAIData().getClanRange() + "\"";
	}

	public String getXmlEnemy()
	{
		if (getAIData().getEnemyClan() == null || getAIData().getEnemyClan().length() == 0 ||
				getAIData().getEnemyClan().equalsIgnoreCase("null"))
		{
			return "";
		}
		return " enemyClan=\"" + getAIData().getEnemyClan() + "\"";
	}

	public String getXmlEnemyRange()
	{
		if (getAIData().getEnemyRange() == 0)
		{
			return "";
		}
		return " enemyRange=\"" + getAIData().getEnemyRange() + "\"";
	}

	public String getXmlDodge()
	{
		if (getAIData().getDodge() == 0)
		{
			return "";
		}
		return " dodge=\"" + getAIData().getDodge() + "\"";
	}

	public String getXmlMinSocial1()
	{
		if (getAIData().getMinSocial(false) == -1)
		{
			return "";
		}
		return " minSocial1=\"" + getAIData().getMinSocial(false) + "\"";
	}

	public String getXmlMaxSocial1()
	{
		if (getAIData().getMaxSocial(false) == -1)
		{
			return "";
		}
		return " maxSocial1=\"" + getAIData().getMaxSocial(false) + "\"";
	}

	public String getXmlMinSocial2()
	{
		if (getAIData().getMinSocial(true) == -1)
		{
			return "";
		}
		return " minSocial2=\"" + getAIData().getMinSocial(true) + "\"";
	}

	public String getXmlMaxSocial2()
	{
		if (getAIData().getMaxSocial(true) == -1)
		{
			return "";
		}
		return " maxSocial2=\"" + getAIData().getMaxSocial(true) + "\"";
	}

	private List<L2Spawn> knownSpawns = new ArrayList<>();

	public final void addKnownSpawn(final L2Spawn spawn)
	{
		knownSpawns.add(spawn);
	}

	public final List<L2Spawn> getKnownSpawns()
	{
		return knownSpawns;
	}

	public void onSpawn(L2Spawn spawn)
	{
		synchronized (allSpawns)
		{
			allSpawns.add(spawn);
		}
	}

	public void onUnSpawn(L2Spawn spawn)
	{
		synchronized (allSpawns)
		{
			allSpawns.remove(spawn);
		}
	}
}
