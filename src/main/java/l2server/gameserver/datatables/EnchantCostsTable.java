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

import l2server.Config;
import l2server.gameserver.model.L2EnchantSkillLearn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pere
 */
public class EnchantCostsTable {
	private static Logger log = LoggerFactory.getLogger(EnchantCostsTable.class.getName());


	public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;
	public static final int SAFE_ENCHANT_COST_MULTIPLIER = 5;
	public static final int IMMORTAL_ENCHANT_COST_MULTIPLIER = 25;

	public static class EnchantSkillRange {
		private final int startLevel;
		private final int maxLevel;
		private final int normalBook;
		private final int safeBook;
		private final int changeBook;
		private final int untrainBook;
		private final int immortalBook;

		public EnchantSkillRange(int startLevel, int maxLevel, int normalBook, int safeBook, int changeBook, int untrainBook, int immortalBook) {
			this.startLevel = startLevel;
			this.maxLevel = maxLevel;
			this.normalBook = normalBook;
			this.safeBook = safeBook;
			this.changeBook = changeBook;
			this.untrainBook = untrainBook;
			this.immortalBook = immortalBook;
		}

		public int getStartLevel() {
			return startLevel;
		}

		public int getMaxLevel() {
			return maxLevel;
		}

		public int getNormalBook() {
			return normalBook;
		}

		public int getSafeBook() {
			return safeBook;
		}

		public int getChangeBook() {
			return changeBook;
		}

		public int getUntrainBook() {
			return untrainBook;
		}

		public int getImmortalBook() {
			return immortalBook;
		}
	}

	public static class EnchantSkillDetail {
		private final int level;
		private final int adenaCost;
		private final int spCost;
		private final byte[] rates;
		private final EnchantSkillRange range;

		public EnchantSkillDetail(int lvl, int adena, int sp, byte[] rates, EnchantSkillRange range) {
			level = lvl;
			adenaCost = adena;
			spCost = sp;
			this.rates = rates;
			this.range = range;
		}

		/**
		 * @return Returns the level.
		 */
		public int getLevel() {
			return level;
		}

		/**
		 * @return Returns the spCost.
		 */
		public int getSpCost() {
			return spCost;
		}

		public int getAdenaCost() {
			return adenaCost;
		}

		public byte getRate(Player ply) {
			if (ply.getLevel() < 85) {
				return 0;
			}

			return rates[ply.getLevel() - 85];
		}

		public EnchantSkillRange getRange() {
			return range;
		}
	}

	private Map<Integer, L2EnchantSkillLearn> enchantSkillTrees = new HashMap<>();
	//enchant skill list
	private Map<Integer, EnchantSkillRange> enchantRanges = new HashMap<>();
	private List<EnchantSkillDetail> enchantDetails = new ArrayList<>();

	public static EnchantCostsTable getInstance() {
		return SingletonHolder.instance;
	}

	private EnchantCostsTable() {
		if (!Config.IS_CLASSIC) {
			load();
		}
	}
	
	@Load
	private void load() {
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "enchantSkillCosts.xml");
		XmlDocument doc = new XmlDocument(file);

		enchantSkillTrees.clear();
		enchantDetails.clear();

		for (XmlNode enchantNode : doc.getChildren()) {
			if (enchantNode.getName().equalsIgnoreCase("enchantRange")) {
				int startLevel = enchantNode.getInt("startLevel");
				int maxLevel = enchantNode.getInt("maxLevel");
				int normalBook = enchantNode.getInt("normalBook");
				int safeBook = enchantNode.getInt("safeBook");
				int changeBook = enchantNode.getInt("changeBook");
				int untrainBook = enchantNode.getInt("untrainBook");
				int immortalBook = enchantNode.getInt("immortalBook");
				EnchantSkillRange range = new EnchantSkillRange(startLevel, maxLevel, normalBook, safeBook, changeBook, untrainBook, immortalBook);
				for (int lvl = startLevel; lvl < maxLevel; lvl++) {
					enchantRanges.put(lvl, range);
				}
			} else if (enchantNode.getName().equalsIgnoreCase("enchant")) {
				String[] levels = enchantNode.getString("level").split(",");
				int adena = enchantNode.getInt("adena");
				int sp = 0;//enchantNode.getInt("sp");

				if (Config.isServer(Config.TENKAI_LEGACY)) {
					adena = (int) Math.sqrt(adena);
				}

				for (String ls : levels) {
					int enchLvl = Integer.valueOf(ls);
					EnchantSkillRange range = enchantRanges.get(enchLvl - 1);
					if (range == null) {
						continue;
					}

					byte[] rate = new byte[30];
					for (int i = 0; i < 30; i++) {
						int playerLvl = 85 + i;
						// Hardcoded calculation of the enchant chances
						rate[i] = (byte) (playerLvl - (enchLvl - 1) % 10 * 5);
						if (i - enchLvl < 3) {
							rate[i] -= 30;
						}

						if (rate[i] < 0) {
							rate[i] = 0;
						} else if (rate[i] > 100) {
							rate[i] = 100;
						}
					}

					EnchantSkillDetail esd = new EnchantSkillDetail(enchLvl, adena, sp, rate, range);
					addEnchantDetail(esd);
				}
			}
		}

		log.info("EnchantGroupsTable: Loaded " + enchantDetails.size() + " enchant details.");
	}

	public int addNewRouteForSkill(int skillId, int maxLvL, int route) {
		L2EnchantSkillLearn enchantableSkill = enchantSkillTrees.get(skillId);
		if (enchantableSkill == null) {
			enchantableSkill = new L2EnchantSkillLearn(skillId, maxLvL);
			enchantSkillTrees.put(skillId, enchantableSkill);
		}

		enchantableSkill.addNewEnchantRoute(route);
		return getEnchantGroupDetails().size();
	}

	public L2EnchantSkillLearn getSkillEnchantmentForSkill(Skill skill) {
		L2EnchantSkillLearn esl = getSkillEnchantmentBySkillId(skill.getId());
		// there is enchantment for this skill and we have the required level of it
		if (esl != null && skill.getLevelHash() >= esl.getBaseLevel()) {
			return esl;
		}
		return null;
	}

	public L2EnchantSkillLearn getSkillEnchantmentBySkillId(int skillId) {
		return enchantSkillTrees.get(skillId);
	}

	public int getEnchantSkillSpCost(Skill skill) {
		L2EnchantSkillLearn enchantSkillLearn = enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null) {
			EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getEnchantRouteId(), skill.getEnchantLevel());
			if (esd != null) {
				return esd.getSpCost();
			}
		}

		return 0;
	}

	public int getEnchantSkillAdenaCost(Skill skill) {
		L2EnchantSkillLearn enchantSkillLearn = enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null) {
			EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getEnchantRouteId(), skill.getEnchantLevel());
			if (esd != null) {
				return esd.getAdenaCost();
			}
		}

		return Integer.MAX_VALUE;
	}

	public byte getEnchantSkillRate(Player player, Skill skill) {
		L2EnchantSkillLearn enchantSkillLearn = enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null) {
			EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getEnchantRouteId(), skill.getEnchantLevel());
			if (esd != null) {
				return esd.getRate(player);
			}
		}

		return 0;
	}

	public void addEnchantDetail(EnchantSkillDetail detail) {
		enchantDetails.add(detail);
	}

	public List<EnchantSkillDetail> getEnchantGroupDetails() {
		return enchantDetails;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final EnchantCostsTable instance = new EnchantCostsTable();
	}

	public void reload() {
		load();
	}
}
