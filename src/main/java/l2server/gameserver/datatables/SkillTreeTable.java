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

package l2server.gameserver.datatables;

import java.util.HashMap; import java.util.Map;
import l2server.Config;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.L2TransformSkillLearn;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.*;

public class SkillTreeTable {
	private static Logger log = LoggerFactory.getLogger(SkillTreeTable.class.getName());


	private List<L2SkillLearn> fishingSkillTrees = new ArrayList<>();
	//all common skills (taught by Fisherman)
	private List<L2SkillLearn> expandDwarfCraftSkillTrees = new ArrayList<>();
	//list of special skill for dwarf (expand dwarf craft) learned by class teacher
	private List<L2TransformSkillLearn> transformSkillTrees = new ArrayList<>();
	// Transform Skills (Test)
	private ArrayList<L2SkillLearn> specialSkillTrees = new ArrayList<>();

	// checker, sorted arrays of hash codes
	private Map<Integer, long[]> skillsByRaceHashCodes; // race-specific transformations
	private long[] allSkillsHashCodes; // fishing, special and all races transformations

	private boolean loading = true;

	public static SkillTreeTable getInstance() {
		return SingletonHolder.instance;
	}

	private SkillTreeTable() {
	}

	/**
	 * Return the minimum level needed to have this Expertise.<BR><BR>
	 *
	 * @param grade The grade level searched
	 */
	public int getExpertiseLevel(int grade) {
		if (grade <= 0) {
			return 0;
		}

		if (Config.IS_CLASSIC && grade > 3) {
			return 100;
		}

		// since expertise comes at same level for all classes we use paladin for now
		Map<Long, L2SkillLearn> learnMap = PlayerClassTable.getInstance().getClassById(0).getSkills();

		long skillHashCode = SkillTable.getSkillHashCode(239, grade);
		if (learnMap.containsKey(skillHashCode)) {
			return learnMap.get(skillHashCode).getMinLevel();
		}

		log.error("Expertise not found for grade " + grade);
		return 0;
	}

	@Load(dependencies = SkillTable.class)
	public void load() {
		loading = true;

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skilltrees/fishingSkillTree.xml");
		XmlDocument doc = new XmlDocument(file);

		int prevSkillId = -1;
		for (XmlNode n : doc.getChildren()) {
			if (n.getName().equalsIgnoreCase("skill")) {
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				int minLvl = n.getInt("minLevel");
				int cost = n.getInt("spCost");
				int costId = n.getInt("itemId");
				int costCount = n.getInt("count");
				boolean isDwarven = n.getBool("isForDwarf");
				boolean npc = n.getBool("learnedByNpc");
				boolean fs = n.getBool("learnedByFS");

				if (prevSkillId != id) {
					prevSkillId = id;
				}

				L2SkillLearn skill = new L2SkillLearn(id, lvl, cost, minLvl, 0, npc, fs, false, false);
				skill.addCostItem(costId, costCount);

				if (isDwarven) {
					expandDwarfCraftSkillTrees.add(skill);
				} else {
					fishingSkillTrees.add(skill);
				}
			}
		}

		file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skilltrees/transformSkillTree.xml");
		doc = new XmlDocument(file);

		prevSkillId = -1;
		for (XmlNode n : doc.getChildren()) {
			if (n.getName().equalsIgnoreCase("skill")) {
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				int minLvl = n.getInt("minLevel");
				int cost = n.getInt("spCost");
				int itemId = n.getInt("itemId");
				int raceId = n.getInt("raceId");

				if (prevSkillId != id) {
					prevSkillId = id;
				}

				L2TransformSkillLearn skill = new L2TransformSkillLearn(raceId, id, itemId, lvl, cost, minLvl);

				transformSkillTrees.add(skill);
			}
		}

		file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skilltrees/specialSkillTree.xml");
		doc = new XmlDocument(file);

		prevSkillId = -1;
		for (XmlNode n : doc.getChildren()) {
			if (n.getName().equalsIgnoreCase("skill")) {
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				int costId = n.getInt("itemId");
				int costCount = n.getInt("count");
				boolean npc = n.getBool("learnedByNpc");
				boolean fs = n.getBool("learnedByFS");

				if (prevSkillId != id) {
					prevSkillId = id;
				}

				L2SkillLearn skill = new L2SkillLearn(id, lvl, 0, 0, 0, npc, fs, false, false);
				skill.addCostItem(costId, costCount);

				specialSkillTrees.add(skill);
			}
		}

		generateCheckArrays();

		log.info("FishingSkillTreeTable: Loaded " + fishingSkillTrees.size() + " general skills.");
		log.info("DwarvenCraftSkillTreeTable: Loaded " + expandDwarfCraftSkillTrees.size() + " dwarven skills.");
		log.info("TransformSkillTreeTable: Loaded " + transformSkillTrees.size() + " transform skills");
		log.info("SpecialSkillTreeTable: Loaded " + specialSkillTrees.size() + " special skills");
		loading = false;
	}

	private void generateCheckArrays() {
		int i;
		long[] array;

		// race-specific skills including dwarven (obtained by fishing)
		ArrayList<Long> list = new ArrayList<>();
		Map<Integer, long[]> result = new HashMap<>(Race.values().length);
		for (Race r : Race.values()) {
			for (L2TransformSkillLearn s : transformSkillTrees) {
				if (s.getRace() == r.ordinal()) {
					list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
				}
			}

			if (r == Race.Dwarf) {
				for (L2SkillLearn s : expandDwarfCraftSkillTrees) {
					list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
				}
			}

			i = 0;
			array = new long[list.size()];
			for (long s : list) {
				array[i++] = s;
			}
			Arrays.sort(array);
			result.put(r.ordinal(), array);
			list.clear();
		}
		skillsByRaceHashCodes = result;

		// skills available for all classes and races
		for (L2SkillLearn s : fishingSkillTrees) {
			list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
		}

		for (L2TransformSkillLearn s : transformSkillTrees) {
			if (s.getRace() == -1) {
				list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
			}
		}

		for (L2SkillLearn s : specialSkillTrees) {
			list.add(SkillTable.getSkillHashCode(s.getId(), s.getLevel()));
		}

		i = 0;
		array = new long[list.size()];
		for (long s : list) {
			array[i++] = s;
		}
		Arrays.sort(array);
		allSkillsHashCodes = array;
	}

	public L2SkillLearn[] getAvailableClassSkills(Player cha) {
		List<L2SkillLearn> result = new ArrayList<>();
		Set<Long> skillIds = cha.getCurrentClass().getSkills().keySet();

		if (skillIds == null) {
			// the skilltree for this class is undefined, so we give an empty list
			log.warn("Skilltree for class " + cha.getCurrentClass().getName() + " is not defined!");
			return new L2SkillLearn[0];
		}

		Skill[] oldSkills = cha.getAllSkills();

		for (long skillId : skillIds) {
			L2SkillLearn temp = cha.getCurrentClass().getSkills().get(skillId);
			//Let's get all auto-get skills and all skill learn from npc, but transfer skills.
			if ((temp.isAutoGetSkill() && temp.getMinLevel() <= cha.getLevel() && temp.getMinDualLevel() <= cha.getDualLevel() ||
					temp.isLearnedFromPanel() && !temp.isTransferSkill()) && (!temp.isRemember() || cha.isRememberSkills())) {
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
					if (oldSkills[j].getId() == temp.getId()) {
						knownSkill = true;

						if (oldSkills[j].getLevel() == temp.getLevel() - 1) {
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && (temp.getLevel() == 1 || temp.getId() == 248)) {
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2SkillLearn[result.size()]);
	}

	// Very inefficient function but works
	public boolean hasNewSkillsToLearn(Player cha, PlayerClass cl) {
		List<L2SkillLearn> result1 = new ArrayList<>();
		List<L2SkillLearn> result2 = new ArrayList<>();
		Collection<L2SkillLearn> skills = PlayerClassTable.getInstance().getClassById(cl.getId()).getSkills().values();

		if (skills == null) {
			// the skilltree for this class is undefined, so we give an empty list
			log.warn("Skilltree for class " + cl.getName() + " is not defined!");
			return false;
		}

		Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : skills) {
			//Let's get all auto-get skills and all skill learn from npc, but transfer skills.
			if ((temp.isAutoGetSkill() || temp.isLearnedFromPanel() && !temp.isTransferSkill()) && temp.getMinLevel() <= cha.getLevel() &&
					temp.getMinDualLevel() <= cha.getDualLevel()) {
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
					if (oldSkills[j].getId() == temp.getId()) {
						knownSkill = true;

						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1) {
							// this is the next level of a skill that we know
							result1.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1) {
					// this is a new skill
					result1.add(temp);
				}
			}
		}

		for (L2SkillLearn temp : skills) {
			//Let's get all auto-get skills and all skill learn from npc, but transfer skills.
			if ((temp.isAutoGetSkill() || temp.isLearnedFromPanel() && !temp.isTransferSkill()) && temp.getMinLevel() <= cha.getLevel() - 1 &&
					temp.getMinDualLevel() <= cha.getDualLevel() - 1) {
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
					if (oldSkills[j].getId() == temp.getId()) {
						knownSkill = true;

						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1) {
							// this is the next level of a skill that we know
							result2.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1) {
					// this is a new skill
					result2.add(temp);
				}
			}
		}

		return result1.size() > result2.size();
	}

	public L2SkillLearn[] getAvailableSkills(Player cha) {
		List<L2SkillLearn> result = new ArrayList<>();
		List<L2SkillLearn> skills = new ArrayList<>();

		skills.addAll(fishingSkillTrees);

		if (skills.size() < 1) {
			// the skilltree for this class is undefined, so we give an empty list
			log.warn("Skilltree for fishing is not defined!");
			return new L2SkillLearn[0];
		}

		if (cha.hasDwarvenCraft() && expandDwarfCraftSkillTrees != null) {
			skills.addAll(expandDwarfCraftSkillTrees);
		}

		Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : skills) {
			if (temp.isLearnedFromPanel() && temp.getMinLevel() <= cha.getLevel() && temp.getMinDualLevel() <= cha.getDualLevel()) {
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
					if (oldSkills[j].getId() == temp.getId()) {
						knownSkill = true;

						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1) {
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1) {
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public L2SkillLearn[] getAvailableSpecialSkills(Player cha) {
		List<L2SkillLearn> result = new ArrayList<>();
		List<L2SkillLearn> skills = new ArrayList<>();

		skills.addAll(specialSkillTrees);

		if (skills.size() < 1) {
			// the skilltree for this class is undefined, so we give an empty list
			log.warn("Skilltree for special is not defined!");
			return new L2SkillLearn[0];
		}

		Skill[] oldSkills = cha.getAllSkills();

		for (L2SkillLearn temp : skills) {
			boolean knownSkill = false;

			for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
				if (oldSkills[j].getId() == temp.getId()) {
					knownSkill = true;

					if (oldSkills[j].getLevelHash() == temp.getLevel() - 1) {
						// this is the next level of a skill that we know
						result.add(temp);
					}
				}
			}

			if (!knownSkill && temp.getLevel() == 1) {
				// this is a new skill
				result.add(temp);
			}
		}

		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public L2TransformSkillLearn[] getAvailableTransformSkills(Player cha) {
		List<L2TransformSkillLearn> result = new ArrayList<>();
		List<L2TransformSkillLearn> skills = transformSkillTrees;

		if (skills == null) {
			// the skilltree for this class is undefined, so we give an empty list

			log.warn("No Transform skills defined!");
			return new L2TransformSkillLearn[0];
		}

		Skill[] oldSkills = cha.getAllSkills();

		for (L2TransformSkillLearn temp : skills) {
			if (temp.getMinLevel() <= cha.getLevel() && (temp.getRace() == cha.getRace().ordinal() || temp.getRace() == -1)) {
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
					if (oldSkills[j].getId() == temp.getId()) {
						knownSkill = true;

						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1) {
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1) {
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2TransformSkillLearn[result.size()]);
	}

	public int getMinLevelForNewSkill(Player cha) {
		int minLevel = 0;
		List<L2SkillLearn> skills = new ArrayList<>();

		skills.addAll(fishingSkillTrees);

		if (skills.size() < 1) {
			// the skilltree for this class is undefined, so we give an empty list
			log.warn("SkillTree for fishing is not defined!");
			return minLevel;
		}

		if (cha.hasDwarvenCraft() && expandDwarfCraftSkillTrees != null) {
			skills.addAll(expandDwarfCraftSkillTrees);
		}

		for (L2SkillLearn s : skills) {
			if (s.getMinLevel() > cha.getLevel()) {
				if (minLevel == 0 || s.getMinLevel() < minLevel) {
					minLevel = s.getMinLevel();
				}
			}
		}

		return minLevel;
	}

	public int getMinLevelForNewTransformSkill(Player cha) {
		int minLevel = 0;
		List<L2TransformSkillLearn> skills = new ArrayList<>();

		skills.addAll(transformSkillTrees);

		if (skills.size() < 1) {
			// the skilltree for this class is undefined, so we give an empty list
			log.warn("SkillTree for fishing is not defined!");
			return minLevel;
		}

		for (L2TransformSkillLearn s : skills) {
			if (s.getMinLevel() > cha.getLevel() && s.getRace() == cha.getRace().ordinal()) {
				if (minLevel == 0 || s.getMinLevel() < minLevel) {
					minLevel = s.getMinLevel();
				}
			}
		}

		return minLevel;
	}

	public int getSkillCost(Player player, Skill skill) {
		int skillCost = 100000000;
		long skillHashCode = SkillTable.getSkillHashCode(skill);

		if (player.getCurrentClass().getSkills().containsKey(skillHashCode)) {
			L2SkillLearn skillLearn = player.getCurrentClass().getSkills().get(skillHashCode);
			if (skillLearn.getMinLevel() <= player.getLevel()) {
				skillCost = skillLearn.getSpCost();
			}
		}

		return skillCost;
	}

	public L2SkillLearn getSkillLearnBySkillIdLevel(PlayerClass cl, int skillId, int skillLvl) {
		for (L2SkillLearn sl : PlayerClassTable.getInstance().getClassById(cl.getId()).getSkills().values()) {
			if (sl.getId() == skillId && sl.getLevel() == skillLvl) {
				return sl; // found skill learn
			}
		}
		return null;
	}

	public List<Integer> getAllAllowedSkillId(Player player) {
		ArrayList<Integer> skills = new ArrayList<>();

		for (L2SkillLearn tmp : player.getCurrentClass().getSkills().values()) {
			if (skills.contains(tmp.getId())) {
				skills.add(tmp.getId());
			}
		}

		return skills;
	}

	public boolean isSkillAllowed(Player player, Skill skill) {
		if (skill.isExcludedFromCheck()) {
			return true;
		}

		if (player.isGM() && skill.isGMSkill()) {
			return true;
		}

		if (loading) // prevent accidental skill remove during reload
		{
			return true;
		}

		final int maxLvl = SkillTable.getInstance().getMaxLevel(skill.getId());
		final long hashCode = SkillTable.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLvl));

		if (player.getCurrentClass().getSkills().containsKey(hashCode)) {
			return true;
		}

		if (player.getTemplate().getSkillIds().contains(skill.getId())) {
			return true;
		}

		if (Arrays.binarySearch(skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0) {
			return true;
		}

		return Arrays.binarySearch(allSkillsHashCodes, hashCode) >= 0;
	}

	public L2SkillLearn[] getAvailableSkillsForPlayer(final Player player, boolean missingOnesOnly, boolean topLevelOnly) {
		List<L2SkillLearn> result = new ArrayList<>();

		@SuppressWarnings("unused") final int classId = player.getCurrentClass().getId();

		Set<Long> skillIds = player.getCurrentClass().getSkills().keySet();

		if (skillIds == null) {
			// the skilltree for this class is undefined, so we give an empty list
			//log.warn("Skilltree for class " + classId + " is not defined !");
			return new L2SkillLearn[0];
		}

		//int lastPickedUpSkillId = 0;
		L2SkillLearn learnableSkill = null;
		for (Long skillId : skillIds) {
			final Skill playerSkill = player.getSkills().get(skillId);

			final int playerSkillLevel = playerSkill == null ? 0 : playerSkill.getLevel();

			L2SkillLearn skill = player.getCurrentClass().getSkills().get(skillId);

			// If we're looking for the missing ones only, we skip anything we already have higher or same level.
			if (missingOnesOnly && playerSkillLevel >= skill.getLevel()) {
				continue;
			}

			// We should use that instead of the below code to check if a skill has already been picked up...
			// But not sure if the fucking skills are ordered in the map for all classes...
			// Cant get bothered to check (:
			// FIXME
			//if (lastPickedUpSkillId != 0 && lastPickedUpSkillId == skill.getId())
			//	continue;

			if (topLevelOnly) {
				boolean alreadyPickedItUp = false;

				for (L2SkillLearn skillLearn : result) {
					if (skillLearn.getId() != skill.getId()) {
						continue;
					}

					alreadyPickedItUp = true;
					break;
				}

				if (alreadyPickedItUp) {
					continue;
				}

				learnableSkill = getSkillTopLevelFor(skill.getId(), player);
			} else if (skill.getMinLevel() < player.getLevel()) {
				learnableSkill = skill;
			}

			if (learnableSkill != null) {
				result.add(learnableSkill);

				//System.out.println("We're going to learn " + learnableSkill.getName() + " at the level " + learnableSkill.getLevel() + ".");
			}
		}

		return result.toArray(new L2SkillLearn[result.size()]);
	}

	public final L2SkillLearn getSkillTopLevelFor(final int skillId, final Player player) {
		Set<Long> skillIds = player.getCurrentClass().getSkills().keySet();

		L2SkillLearn result = null;

		int playerLevel = player.getLevel();
		for (long s : skillIds) {
			L2SkillLearn learnableSkill = player.getCurrentClass().getSkills().get(s);

			// Not the skill we're looking for...
			if (learnableSkill.getId() != skillId) {
				continue;
			}
			// Minimum level for this skill is higher level than the player...
			else if (learnableSkill.getMinLevel() > playerLevel) {
				continue;
			}
			// If we already found the skill, and the found skill level is higher than the one here, fuck it
			else if (result != null && result.getLevel() > learnableSkill.getLevel()) {
				continue;
			}

			result = learnableSkill;

			// TODO
			// Not sure if all skills for all classes are in order in that map...
			// So... no break; for now...
		}

		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final SkillTreeTable instance = new SkillTreeTable();
	}
}
