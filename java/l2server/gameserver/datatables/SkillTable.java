/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.datatables;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TLongObjectHashMap;
import l2server.Config;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.stats.SkillParser;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 */
public class SkillTable implements Reloadable
{
	private final TLongObjectHashMap<L2Skill> _skills;
	private final TIntIntHashMap _skillMaxLevel;
	private final Set<Integer> _enchantable;

	public static SkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private SkillTable()
	{
		_skills = new TLongObjectHashMap<>();
		_skillMaxLevel = new TIntIntHashMap();
		_enchantable = new HashSet<>();
		load();

		ReloadableManager.getInstance().register("skills", this);
	}

	@Override
	public boolean reload()
	{
		load();

		//reload some related too
		SkillTreeTable.getInstance().reload();
		SubPledgeSkillTree.getInstance().reload();

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "All Skills have been reloaded";
	}

	private void load()
	{
		_skills.clear();
		_skillMaxLevel.clear();
		_enchantable.clear();

		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skills");
		if (!dir.exists())
		{
			Log.warning("Dir " + dir.getAbsolutePath() + " does not exist");
			return;
		}

		List<File> validFiles = new ArrayList<>();
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml") && !f.getName().startsWith("custom"))
			{
				validFiles.add(f);
			}
		}
		File customfile = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/skills.xml");
		if (customfile.exists())
		{
			validFiles.add(customfile);
		}

		for (File f : validFiles)
		{
			XmlDocument doc = new XmlDocument(f);
			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("skill"))
						{
							SkillParser skill = new SkillParser(d);
							try
							{
								skill.parse();
								for (L2Skill s : skill.getSkills().values())
								{
									_skills.put(getSkillHashCode(s.getId(), s.getLevel(), s.getEnchantRouteId(),
											s.getEnchantLevel()), s);
									if (s.getEnchantRouteId() > 0)
									{
										_enchantable.add(s.getId());
										continue;
									}

									// only non-enchanted skills
									final int maxLvl = _skillMaxLevel.get(s.getId());
									if (s.getLevelHash() > maxLvl)
									{
										_skillMaxLevel.put(s.getId(), s.getLevelHash());
									}
								}
							}
							catch (Exception e)
							{
								Log.log(Level.WARNING, "Cannot create skill id " + skill.getId(), e);
							}
						}
					}
				}
			}
		}

		// Reloading as well FrequentSkill enumeration values
		for (FrequentSkill sk : FrequentSkill.values())
		{
			sk._skill = getInfo(sk._id, sk._level);
		}

		Log.info("SkillTable: Loaded " + _skills.size() + " skills.");
	}

	/**
	 * Provides the skill hash
	 *
	 * @param skill The L2Skill to be hashed
	 * @return getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static long getSkillHashCode(L2Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevelHash(), skill.getEnchantRouteId(),
				skill.getEnchantLevel());
	}

	/**
	 * Centralized method for easier change of the hashing sys
	 *
	 * @param skillId    The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static long getSkillHashCode(int skillId, int skillLevel)
	{
		return getSkillHashCode(skillId, skillLevel, 0, 0);
	}

	public static long getSkillHashCode(int skillId, int skillLevel, final int enchant)
	{
		return getSkillHashCode(skillId, skillLevel, enchant / 1000, enchant % 1000);
	}

	public static long getSkillHashCode(int skillId, int skillLevel, int skillEnchantRouteId, int skillEnchantLevel)
	{
		//return skillId * 1000000L + skillLevel * 10000L + skillEnchantRouteId * 100L + skillEnchantRouteLevel;
		return ((long) skillId << 32) + (skillEnchantRouteId * 1000 + skillEnchantLevel << 16) + skillLevel;
	}

	public final L2Skill getInfo(final int skillId, final int level)
	{
		return getInfo(skillId, level, 0, 0);
	}

	public final L2Skill getInfo(final int skillId, final int level, final int enchant)
	{
		return getInfo(skillId, level, enchant / 1000, enchant % 1000);
	}

	public final L2Skill getInfo(final int skillId, final int level, int enchantRouteId, int enchantRouteLevel)
	{
		long hashCode = getSkillHashCode(skillId, level, enchantRouteId, enchantRouteLevel);
		final L2Skill result = _skills.get(hashCode);
		if (result != null)
		{
			return result;
		}

		// skill/level not found, fix for transformation scripts
		final int maxLvl = _skillMaxLevel.get(skillId);
		// requested level too high
		if (maxLvl > 0 && level > maxLvl)
		{
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		}

		String error = "No skill info found for skill id " + skillId;
		if (enchantRouteId <= 0)
		{
			error += " and level " + level + ".";
		}
		else
		{
			error += ", level " + level + ", enchant route " + enchantRouteId + " and enchant level " +
					enchantRouteLevel + ".";
		}

		Log.warning(error);
		return null;
	}

	public final int getMaxLevel(final int skillId)
	{
		return _skillMaxLevel.get(skillId);
	}

	public final boolean isEnchantable(final int skillId)
	{
		return _enchantable.contains(skillId);
	}

	/**
	 * Returns an array with siege skills. If addNoble == true, will add also Advanced headquarters.
	 */
	public L2Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		L2Skill[] temp = new L2Skill[3 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
		int i = 0;
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(19034, 1));
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(19035, 1));
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(1903, 1));

		if (addNoble)
		{
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(326, 1));
		}
		if (hasCastle)
		{
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(844, 1));
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(845, 1));
		}
		return temp;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SkillTable _instance = new SkillTable();
	}

	/**
	 * Enum to hold some important references to frequently used (hardcoded) skills in core
	 *
	 * @author DrHouse
	 */
	public enum FrequentSkill
	{
		RAID_CURSE(4215, 1),
		RAID_CURSE2(4515, 1),
		SEAL_OF_RULER(246, 1),
		BUILD_HEADQUARTERS(247, 1),
		LUCKY(194, 1),
		DWARVEN_CRAFT(1321, 1),
		MAESTRO_CREATE_ITEM(172, 10),
		COMMON_CRAFT(1322, 1),
		WYVERN_BREATH(4289, 1),
		STRIDER_SIEGE_ASSAULT(325, 1),
		FAKE_PETRIFICATION(4616, 1),
		FIREWORK(5965, 1),
		LARGE_FIREWORK(2025, 1),
		BLESSING_OF_PROTECTION(5182, 1),
		ARENA_CP_RECOVERY(4380, 1),
		VOID_BURST(3630, 1),
		VOID_FLOW(3631, 1),
		THE_VICTOR_OF_WAR(5074, 1),
		THE_VANQUISHED_OF_WAR(5075, 1),
		IMPRINT_OF_LIGHT(19034, 1),
		IMPRINT_OF_DARKNESS(19035, 1),
		LUCKY_CLOVER(18103, 1);

		private final int _id;
		private final int _level;
		private L2Skill _skill = null;

		FrequentSkill(int id, int level)
		{
			_id = id;
			_level = level;
		}

		public L2Skill getSkill()
		{
			return _skill;
		}

	}
}
