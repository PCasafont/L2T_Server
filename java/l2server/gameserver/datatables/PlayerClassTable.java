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
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.PlayerClass;
import l2server.gameserver.model.base.Race;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Pere
 */
public class PlayerClassTable implements Reloadable
{
	private Map<Integer, PlayerClass> _classes = new HashMap<>();

	private Map<Long, Integer> _minSkillLevels = new HashMap<>();

	private final List<Integer> _mainSubclassSet = new ArrayList<>();
	private final List<Integer> _neverSubclassed = new ArrayList<>();

	private final Map<Integer, List<Integer>> _awakeningBannedSubclasses = new HashMap<>();

	public static PlayerClassTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private PlayerClassTable()
	{
		try
		{
			load();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		ReloadableManager.getInstance().register("classes", this);
	}

	public void load()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "classes.xml");
		XmlDocument doc = new XmlDocument(file);

		int count = 0;
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode classNode : n.getChildren())
				{
					if (classNode.getName().equalsIgnoreCase("class"))
					{
						int id = classNode.getInt("id");
						String name = classNode.getString("name");
						int parentId = classNode.getInt("parentId", -1);
						int awakensTo = classNode.getInt("awakensTo", -1);
						boolean isMage = classNode.getBool("isMage", false);
						int raceId = classNode.getInt("raceId", -1);
						int level = classNode.getInt("level");

						PlayerClass cl =
								new PlayerClass(id, name, _classes.get(parentId), awakensTo, isMage, raceId, level);

						if (cl.getParent() != null)
						{
							cl.getSkills().putAll(cl.getParent().getSkills());
						}

						if (classNode.hasAttribute("pickSkillsFrom"))
						{
							PlayerClass pickSkillsFrom = _classes.get(classNode.getInt("pickSkillsFrom"));
							cl.getSkills().putAll(pickSkillsFrom.getSkills());
						}

						for (XmlNode subNode : classNode.getChildren())
						{
							if (subNode.getName().equalsIgnoreCase("skill"))
							{
								int skillId = subNode.getInt("id");
								String[] levels = subNode.getString("level").split(",");
								int reqSp = subNode.getInt("reqSp");
								int minLevel = subNode.getInt("minLevel");
								int minDualLevel = subNode.getInt("minDualLevel", 0);
								boolean learnFromPanel = subNode.getBool("learnFromPanel", true);
								boolean learnFromFS = subNode.getBool("learnFromFS", false);
								boolean isTransfer = subNode.getBool("isTransfer", false);
								boolean autoGet = subNode.getBool("autoGet", false);

								for (String mls : levels)
								{
									int skillLevel = Integer.valueOf(mls);
									long hash = SkillTable.getSkillHashCode(skillId, skillLevel);
									L2SkillLearn sl =
											new L2SkillLearn(skillId, skillLevel, reqSp, minLevel, minDualLevel,
													learnFromPanel, learnFromFS, isTransfer, autoGet);

									for (XmlNode reqNode : subNode.getChildren())
									{
										if (reqNode.getName().equalsIgnoreCase("reqSkill"))
										{
											int reqSkillId = reqNode.getInt("id");
											sl.addCostSkill(reqSkillId);
										}
									}

									cl.addSkill(hash, sl);
									_minSkillLevels.put(hash, minLevel);
								}
							}
							else if (subNode.getName().equalsIgnoreCase("skillReplacement"))
							{
								int skillId = subNode.getInt("id");
								int replacedBy = subNode.getInt("replacedBy");
								Map<Long, L2SkillLearn> skills = new HashMap<>();
								skills.putAll(cl.getSkills());
								for (long hash : skills.keySet())
								{
									L2SkillLearn sl = cl.getSkills().get(hash);
									if (sl.getId() == skillId)
									{
										cl.getSkills().remove(hash);
										cl.addSkill(SkillTable.getSkillHashCode(replacedBy, sl.getLevel()),
												new L2SkillLearn(replacedBy, sl.getLevel(), sl.getSpCost(),
														sl.getMinLevel(), sl.getMinDualLevel(), sl.isLearnedFromPanel(),
														sl.isLearnedByFS(), sl.isTransferSkill(), sl.isAutoGetSkill()));
									}
								}
							}
							else if (subNode.getName().equalsIgnoreCase("skillRemoval"))
							{
								int skillId = subNode.getInt("id");
								Map<Long, L2SkillLearn> skills = new HashMap<>();
								skills.putAll(cl.getSkills());
								for (long hash : skills.keySet())
								{
									L2SkillLearn sl = cl.getSkills().get(hash);
									if (sl.getId() == skillId)
									{
										cl.getSkills().remove(hash);
									}
								}
							}
						}

						// Remove low level skills
						if (cl.getLevel() == 85 && cl.getRace() != Race.Ertheia)
						{
							Set<Integer> toRemove = new HashSet<>();
							for (long hash : cl.getSkills().keySet())
							{
								L2SkillLearn sl = cl.getSkills().get(hash);
								if (sl.getMinLevel() < 85)
								{
									toRemove.add(sl.getId());
								}
								else
								{
									toRemove.remove(sl.getId());
								}
							}

							Map<Long, L2SkillLearn> skills = new HashMap<>();
							skills.putAll(cl.getSkills());
							for (long hash : skills.keySet())
							{
								L2SkillLearn sl = cl.getSkills().get(hash);
								if (toRemove.contains(sl.getId()))
								{
									cl.getSkills().remove(hash);
								}
							}
						}

						if (Config.isServer(Config.TENKAI))
						{
							// Copy the map keys so that we avoid a concurrent modification exception
							Set<Long> skillHashes = new HashSet<>(cl.getSkills().keySet());
							for (long hash : skillHashes)
							{
								L2SkillLearn sl = cl.getSkills().get(hash);
								for (int reqSkillId : sl.getCostSkills())
								{
									long reqHash = SkillTable.getSkillHashCode(reqSkillId, 1);
									if (cl.getParent() != null && cl.getParent().getSkills().containsKey(reqHash))
									{
										L2SkillLearn rsl;
										if (!cl.getSkills().containsKey(reqHash))
										{
											rsl = new L2SkillLearn(reqSkillId, 1, 1, 85, 0, true, false, false, false);
											rsl.setIsRemember(true);
											cl.addSkill(reqHash, rsl);
											for (int lv = 2;
												 cl.getParent().getSkills()
														 .containsKey(SkillTable.getSkillHashCode(reqSkillId, lv));
												 lv++)
											{
												cl.addSkill(SkillTable.getSkillHashCode(reqSkillId, lv),
														new L2SkillLearn(reqSkillId, lv, 1, 85, 0, true, false, false,
																false));
											}
										}
										else
										{
											rsl = cl.getSkills().get(reqHash);
										}

										rsl.addCostSkill(sl.getId());
									}
								}
							}
						}

						_classes.put(id, cl);
						count++;
					}
					else if (classNode.getName().equalsIgnoreCase("skill"))
					{
						int skillId = classNode.getInt("id");
						String[] levels = classNode.getString("level").split(",");
						int reqSp = classNode.getInt("reqSp");
						int minLevel = classNode.getInt("minLevel");
						int minDualLevel = classNode.getInt("minDualLevel", 0);
						boolean learnFromPanel = classNode.getBool("learnFromPanel", true);
						boolean learnFromFS = classNode.getBool("learnFromFS", false);
						boolean isTransfer = classNode.getBool("isTransfer", false);
						boolean autoGet = classNode.getBool("autoGet", false);

						for (String mls : levels)
						{
							int skillLevel = Integer.valueOf(mls);
							long hash = SkillTable.getSkillHashCode(skillId, skillLevel);
							for (int PlayerClass : _classes.keySet())
							{
								_classes.get(PlayerClass).addSkill(hash,
										new L2SkillLearn(skillId, skillLevel, reqSp, minLevel, minDualLevel,
												learnFromPanel, learnFromFS, isTransfer, autoGet));
							}
							_minSkillLevels.put(hash, minLevel);
						}
					}
				}
			}
		}
		Log.info("PlayerClassTable: loaded " + count + " classes.");

		if (Config.IS_CLASSIC)
		{
			return;
		}

		_neverSubclassed.clear();
		_neverSubclassed.add(51); // Overlord
		_neverSubclassed.add(57); // Warsmith
		_neverSubclassed.add(184); // Marauder
		_neverSubclassed.add(185); // Cloud Breaker

		_mainSubclassSet.clear();
		_mainSubclassSet.addAll(getList(null, 40));
		_mainSubclassSet.removeAll(_neverSubclassed);

		_awakeningBannedSubclasses.clear();
		for (PlayerClass pc : _classes.values())
		{
			if (pc.getLevel() == 40)
			{
				if (!_awakeningBannedSubclasses.containsKey(getAwakening(pc.getId())))
				{
					List<Integer> list = new ArrayList<>();
					_awakeningBannedSubclasses.put(getAwakening(pc.getId()), list);
				}
				_awakeningBannedSubclasses.get(getAwakening(pc.getId())).add(pc.getId());
			}
		}
	}

	@Override
	public boolean reload()
	{
		load();
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			player.setClassTemplate(player.getCurrentClass().getId());
			player.broadcastUserInfo();
		}

		HennaTable.getInstance().reload();

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Player Classes reloaded";
	}

	public final Collection<PlayerClass> getAllClasses()
	{
		return _classes.values();
	}

	public final PlayerClass getClassById(int PlayerClass)
	{
		PlayerClass cl = _classes.get(PlayerClass);
		if (cl == null)
		{
			throw new IllegalArgumentException("No template for PlayerClass: " + PlayerClass);
		}

		return cl;
	}

	public final String getClassNameById(int PlayerClass)
	{
		PlayerClass cl = _classes.get(PlayerClass);
		if (cl == null)
		{
			throw new IllegalArgumentException("No template for PlayerClass: " + PlayerClass);
		}
		return cl.getName();
	}

	public final int getMinSkillLevel(int id, int level)
	{
		//if (level >= 100)
		//	level = SkillTable.getInstance().getMaxLevel(id);
		long hash = SkillTable.getSkillHashCode(id, level);
		if (_minSkillLevels.containsKey(hash))
		{
			return _minSkillLevels.get(hash);
		}
		return 0;
	}

	public final List<Integer> getAvailableSubclasses(L2PcInstance player, int baseClassId)
	{
		if (player.getLevel() < 76)
		{
			return null;
		}

		List<Integer> subclasses = new CopyOnWriteArrayList<>();
		if (player.getRace() != Race.Kamael)
		{
			subclasses.addAll(_mainSubclassSet);

			// Remove all the same awakening subclasses from selection
			List<Integer> bannedSubs = _awakeningBannedSubclasses.get(getAwakening(baseClassId));
			if (bannedSubs != null)
			{
				subclasses.removeAll(bannedSubs);
			}

			/*switch (player.getRace())
			{
				case Elf:
					subclasses.removeAll(getList(Race.DarkElf, 40));
					break;
				case DarkElf:
					subclasses.removeAll(getList(Race.Elf, 40));
					break;
			}*/

			if (player.getRace() != Race.Ertheia)
			{
				subclasses.removeAll(getList(Race.Kamael, 40));
			}
		}
		else
		{
			subclasses.addAll(_mainSubclassSet);

			// Remove all the same awakening subclasses from selection
			List<Integer> bannedSubs = _awakeningBannedSubclasses.get(getAwakening(baseClassId));
			if (bannedSubs != null)
			{
				subclasses.removeAll(bannedSubs);
			}

			//subclasses = getList(Kamael, 40);

			// Check sex, male subclasses female and vice versa
			// If server owner set MaxSubclass > 3 some kamael's cannot take 4 sub
			// So, in that situation we must skip sex check
			if (Config.MAX_SUBCLASS <= 3)
			{
				if (player.getAppearance().getSex())
				{
					subclasses.remove((Integer) 129); // Female Soul Breaker
				}
				else
				{
					subclasses.remove((Integer) 128); // Male Soul Breaker
				}
			}
			if (player.getTotalSubClasses() < 2)
			{
				subclasses.remove((Integer) 135); // Inspector
			}
		}

		if (player.getRace() == Race.Ertheia)
		{
			List<Integer> awakened = new ArrayList<>();
			for (int subId : subclasses)
			{
				for (PlayerClass cl : _classes.values())
				{
					if (cl.getLevel() == 85 && cl.getParent() != null && cl.getParent().getParent() != null &&
							cl.getParent().getParent().getId() == subId)
					{
						awakened.add(cl.getId());
						break;
					}
				}
			}

			subclasses = awakened;
		}

		return subclasses;
	}

	private List<Integer> getList(Race race, int level)
	{
		List<Integer> list = new ArrayList<>();
		for (PlayerClass cl : _classes.values())
		{
			if ((race == null || cl.getRace() == race) && (level == 0 || cl.getLevel() == level))
			{
				list.add(cl.getId());
			}
		}

		return list;
	}

	public final int getAwakening(int classId)
	{
		PlayerClass pc = _classes.get(classId);
		if (pc.getLevel() < 40 || pc.getRace() == Race.Ertheia)
		{
			return -1;
		}

		int awakeningId = -1;
		if (pc.getLevel() == 85)
		{
			awakeningId = classId;
		}

		if (pc.getAwakeningClassId() == -1)
		{
			PlayerClass supportPc;
			int i = 1;
			int sec = 0;
			while (awakeningId == -1 && sec < 150)
			{
				supportPc = _classes.get(classId + i);
				if (supportPc != null && supportPc.getParent() != null && pc.getId() == supportPc.getParent().getId())
				{
					if (supportPc.getAwakeningClassId() != -1)
					{
						awakeningId = supportPc.getAwakeningClassId();
						pc = supportPc;
						break;
					}
					else
					{
						pc = _classes.get(classId + 1);
					}
				}
				i++;
				sec++;
			}
			if (sec >= 150)
			{
				Log.warning("There was a problem getting the awakening class for the class id " + classId + " (" +
						pc.getName() + ")");
			}
		}

		if (pc.getId() == 133) // Female soulhound
		{
			return 170;
		}

		for (PlayerClass cl : _classes.values())
		{
			if (cl.getParent() == pc)
			{
				return cl.getId();
			}
		}

		return awakeningId;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerClassTable _instance = new PlayerClassTable();
	}
}
