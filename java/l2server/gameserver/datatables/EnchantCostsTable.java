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

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.model.L2EnchantSkillLearn;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantCostsTable
{
	public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;
	public static final int SAFE_ENCHANT_COST_MULTIPLIER = 5;
	public static final int IMMORTAL_ENCHANT_COST_MULTIPLIER = 25;

	public static class EnchantSkillRange
	{
		private final int _startLevel;
		private final int _maxLevel;
		private final int _normalBook;
		private final int _safeBook;
		private final int _changeBook;
		private final int _untrainBook;
		private final int _immortalBook;

		public EnchantSkillRange(int startLevel, int maxLevel, int normalBook, int safeBook, int changeBook, int untrainBook, int immortalBook)
		{
			_startLevel = startLevel;
			_maxLevel = maxLevel;
			_normalBook = normalBook;
			_safeBook = safeBook;
			_changeBook = changeBook;
			_untrainBook = untrainBook;
			_immortalBook = immortalBook;
		}

		public int getStartLevel()
		{
			return _startLevel;
		}

		public int getMaxLevel()
		{
			return _maxLevel;
		}

		public int getNormalBook()
		{
			return _normalBook;
		}

		public int getSafeBook()
		{
			return _safeBook;
		}

		public int getChangeBook()
		{
			return _changeBook;
		}

		public int getUntrainBook()
		{
			return _untrainBook;
		}

		public int getImmortalBook()
		{
			return _immortalBook;
		}
	}

	public static class EnchantSkillDetail
	{
		private final int _level;
		private final int _adenaCost;
		private final int _spCost;
		private final byte[] _rates;
		private final EnchantSkillRange _range;

		public EnchantSkillDetail(int lvl, int adena, int sp, byte[] rates, EnchantSkillRange range)
		{
			_level = lvl;
			_adenaCost = adena;
			_spCost = sp;
			_rates = rates;
			_range = range;
		}

		/**
		 * @return Returns the level.
		 */
		public int getLevel()
		{
			return _level;
		}

		/**
		 * @return Returns the spCost.
		 */
		public int getSpCost()
		{
			return _spCost;
		}

		public int getAdenaCost()
		{
			return _adenaCost;
		}

		public byte getRate(L2PcInstance ply)
		{
			if (ply.getLevel() < 85)
			{
				return 0;
			}

			return _rates[ply.getLevel() - 85];
		}

		public EnchantSkillRange getRange()
		{
			return _range;
		}
	}

	private TIntObjectHashMap<L2EnchantSkillLearn> _enchantSkillTrees = new TIntObjectHashMap<>();
	//enchant skill list
	private Map<Integer, EnchantSkillRange> _enchantRanges = new HashMap<>();
	private List<EnchantSkillDetail> _enchantDetails = new ArrayList<>();

	public static EnchantCostsTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private EnchantCostsTable()
	{
		if (!Config.IS_CLASSIC)
		{
			load();
		}
	}

	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "enchantSkillCosts.xml");
		XmlDocument doc = new XmlDocument(file);

		int count = 0;
		_enchantSkillTrees.clear();
		_enchantDetails.clear();

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode enchantNode : n.getChildren())
				{
					if (enchantNode.getName().equalsIgnoreCase("enchantRange"))
					{
						int startLevel = enchantNode.getInt("startLevel");
						int maxLevel = enchantNode.getInt("maxLevel");
						int normalBook = enchantNode.getInt("normalBook");
						int safeBook = enchantNode.getInt("safeBook");
						int changeBook = enchantNode.getInt("changeBook");
						int untrainBook = enchantNode.getInt("untrainBook");
						int immortalBook = enchantNode.getInt("immortalBook");
						EnchantSkillRange range =
								new EnchantSkillRange(startLevel, maxLevel, normalBook, safeBook, changeBook,
										untrainBook, immortalBook);
						for (int lvl = startLevel; lvl < maxLevel; lvl++)
						{
							_enchantRanges.put(lvl, range);
						}
					}
					else if (enchantNode.getName().equalsIgnoreCase("enchant"))
					{
						String[] levels = enchantNode.getString("level").split(",");
						int adena = enchantNode.getInt("adena");
						int sp = 0;//enchantNode.getInt("sp");

						if (Config.isServer(Config.TENKAI_ESTHUS))
						{
							adena = (int) Math.sqrt(adena);
						}

						for (String ls : levels)
						{
							int enchLvl = Integer.valueOf(ls);
							EnchantSkillRange range = _enchantRanges.get(enchLvl - 1);
							if (range == null)
							{
								continue;
							}

							byte[] rate = new byte[30];
							for (int i = 0; i < 30; i++)
							{
								int playerLvl = 85 + i;
								// Hardcoded calculation of the enchant chances
								rate[i] = (byte) (playerLvl - (enchLvl - 1) % 10 * 5);
								if (i - enchLvl < 3)
								{
									rate[i] -= 30;
								}

								if (rate[i] < 0)
								{
									rate[i] = 0;
								}
								else if (rate[i] > 100)
								{
									rate[i] = 100;
								}
							}

							EnchantSkillDetail esd = new EnchantSkillDetail(enchLvl, adena, sp, rate, range);
							addEnchantDetail(esd);
						}
					}
				}
			}
		}

		Log.info("EnchantGroupsTable: Loaded " + count + " groups.");
	}

	public int addNewRouteForSkill(int skillId, int maxLvL, int route)
	{
		L2EnchantSkillLearn enchantableSkill = _enchantSkillTrees.get(skillId);
		if (enchantableSkill == null)
		{
			enchantableSkill = new L2EnchantSkillLearn(skillId, maxLvL);
			_enchantSkillTrees.put(skillId, enchantableSkill);
		}

		enchantableSkill.addNewEnchantRoute(route);
		return getEnchantGroupDetails().size();
	}

	public L2EnchantSkillLearn getSkillEnchantmentForSkill(L2Skill skill)
	{
		L2EnchantSkillLearn esl = getSkillEnchantmentBySkillId(skill.getId());
		// there is enchantment for this skill and we have the required level of it
		if (esl != null && skill.getLevelHash() >= esl.getBaseLevel())
		{
			return esl;
		}
		return null;
	}

	public L2EnchantSkillLearn getSkillEnchantmentBySkillId(int skillId)
	{
		return _enchantSkillTrees.get(skillId);
	}

	public int getEnchantSkillSpCost(L2Skill skill)
	{
		L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			EnchantSkillDetail esd =
					enchantSkillLearn.getEnchantSkillDetail(skill.getEnchantRouteId(), skill.getEnchantLevel());
			if (esd != null)
			{
				return esd.getSpCost();
			}
		}

		return 0;
	}

	public int getEnchantSkillAdenaCost(L2Skill skill)
	{
		L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			EnchantSkillDetail esd =
					enchantSkillLearn.getEnchantSkillDetail(skill.getEnchantRouteId(), skill.getEnchantLevel());
			if (esd != null)
			{
				return esd.getAdenaCost();
			}
		}

		return Integer.MAX_VALUE;
	}

	public byte getEnchantSkillRate(L2PcInstance player, L2Skill skill)
	{
		L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
		if (enchantSkillLearn != null)
		{
			EnchantSkillDetail esd =
					enchantSkillLearn.getEnchantSkillDetail(skill.getEnchantRouteId(), skill.getEnchantLevel());
			if (esd != null)
			{
				return esd.getRate(player);
			}
		}

		return 0;
	}

	public void addEnchantDetail(EnchantSkillDetail detail)
	{
		_enchantDetails.add(detail);
	}

	public List<EnchantSkillDetail> getEnchantGroupDetails()
	{
		return _enchantDetails;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EnchantCostsTable _instance = new EnchantCostsTable();
	}

	public void reload()
	{
		load();
	}
}
