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
import l2server.gameserver.instancemanager.SearchDropManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.SpawnData;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class ...
 *
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
	private TIntObjectHashMap<L2NpcTemplate> _npcs;

	public static NpcTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private NpcTable()
	{
		reloadAllNpc();
	}

	// just wrapper
	public void reloadAllNpc()
	{
		_npcs = new TIntObjectHashMap<>();

		restoreNpcData(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "npcs", false);

		// Remove shit drops from highrate servers
		if (Config.isServer(Config.TENKAI))
		{
			for (int npcId : _npcs.keys())
			{
				L2NpcTemplate npc = _npcs.get(npcId);
				List<L2DropData> dropsToRemove = new ArrayList<>();
				for (L2DropData dd : npc.getDropData())
				{
					L2Item item = ItemTable.getInstance().getTemplate(dd.getItemId());
					if (item.isCommon() || item.getItemType() == L2EtcItemType.ARROW ||
							item.getItemType() == L2EtcItemType.RECIPE || item.getItemType() == L2EtcItemType.MATERIAL)
					{
						dropsToRemove.add(dd);
					}
				}

				for (L2DropData dd : dropsToRemove)
				{
					npc.getDropData().remove(dd);
				}

				for (L2DropCategory dc : npc.getMultiDropData())
				{
					dropsToRemove.clear();
					for (L2DropData dd : dc.getAllDrops())
					{
						L2Item item = ItemTable.getInstance().getTemplate(dd.getItemId());
						if (item.isCommon() || item.getItemType() == L2EtcItemType.ARROW ||
								item.getItemType() == L2EtcItemType.RECIPE ||
								item.getItemType() == L2EtcItemType.MATERIAL)
						{
							dropsToRemove.add(dd);
						}
					}

					for (L2DropData dd : dropsToRemove)
					{
						dc.getAllDrops().remove(dd);
					}
				}
			}
		}

		// Load custom data
		if (!Config.SERVER_NAME.isEmpty())
		{
			restoreNpcData(Config.DATAPACK_ROOT + "/data_" + Config.SERVER_NAME + "/npcs", true);
		}

		//save();
	}

	private void restoreNpcData(String dataPath, boolean overrideDrops)
	{
		try
		{
			File dir = new File(dataPath);
			if (!dir.exists())
			{
				Log.warning("Dir " + dir.getAbsolutePath() + " doesn't exist");
				return;
			}

			int loadedNpcs = 0;
			File[] files = dir.listFiles();
			for (File f : files)
			{
				if (!f.getName().endsWith(".xml"))
				{
					continue;
				}

				XmlDocument doc = new XmlDocument(f);
				for (XmlNode npcNode : doc.getFirstChild().getChildren())
				{
					if (npcNode.getName().equalsIgnoreCase("npc"))
					{
						final int npcId = npcNode.getInt("id");

						StatsSet set = new StatsSet();
						for (Entry<String, String> attrib : npcNode.getAttributes().entrySet())
						{
							String name = attrib.getKey();
							String value = attrib.getValue();
							set.set(name, value);
						}
						switch (set.getByte("elemAtkType", Elementals.NONE))
						{
							case Elementals.NONE:
								break;
							case Elementals.FIRE:
								set.set("fire", set.getString("elemAtkValue"));
								break;
							case Elementals.WATER:
								set.set("water", set.getString("elemAtkValue"));
								break;
							case Elementals.EARTH:
								set.set("earth", set.getString("elemAtkValue"));
								break;
							case Elementals.WIND:
								set.set("wind", set.getString("elemAtkValue"));
								break;
							case Elementals.HOLY:
								set.set("holy", set.getString("elemAtkValue"));
								break;
							case Elementals.DARK:
								set.set("dark", set.getString("elemAtkValue"));
								break;
							default:
								Log.severe("NPCElementals: Elementals Error with id : " + npcId +
										"; unknown elementType: " + set.getByte("elemAtkType"));
								continue;
						}

						L2NpcTemplate npc;
						// Already loaded
						if (_npcs.containsKey(npcId))
						{
							L2NpcTemplate original = _npcs.get(npcId);
							StatsSet completeSet = new StatsSet();
							completeSet.add(original.getBaseSet());
							completeSet.add(set);
							npc = new L2NpcTemplate(completeSet, original);
							if (completeSet.getString("aiType", null) != null)
							{
								npc.setAIData(new L2NpcAIData(completeSet));
							}
						}
						else
						{
							npc = new L2NpcTemplate(set);
							if (set.getString("aiType", null) != null)
							{
								npc.setAIData(new L2NpcAIData(set));
							}
						}

						for (XmlNode propertyNode : npcNode.getChildren())
						{
							if (propertyNode.getName().equalsIgnoreCase("minion"))
							{
								L2MinionData minionDat = new L2MinionData();
								minionDat.setMinionId(propertyNode.getInt("npcId"));
								minionDat.setAmountMin(propertyNode.getInt("min"));
								minionDat.setAmountMax(propertyNode.getInt("max"));

								if (propertyNode.hasAttribute("respawnTime"))
								{
									minionDat.setRespawnTime(propertyNode.getInt("respawnTime"));
								}

								npc.addRaidData(minionDat);
							}

							if (propertyNode.getName().equalsIgnoreCase("randomMinion"))
							{
								//Manage here the multiple minions
								String minions = propertyNode.getString("npcId");
								int count = minions.split(";").length;

								L2RandomMinionData minionDat = new L2RandomMinionData();

								for (int a = 0; a < count; a++)
								{
									int minionId = Integer.valueOf(minions.split(";")[a]);
									minionDat.addMinionId(minionId);
									minionDat.setAmount(propertyNode.getInt("count"));
								}

								npc.setRandomRaidData(minionDat);
							}

							if (propertyNode.getName().equalsIgnoreCase("skill"))
							{
								int skillId = propertyNode.getInt("id");
								int level = propertyNode.getInt("level");

								if (skillId == 4416)
								{
									npc.setRace(level);
								}

								L2Skill npcSkill = SkillTable.getInstance().getInfo(skillId, level);

								if (npcSkill == null)
								{
									Log.warning("NPC " + npcId + " has a skill that doesn't exist (" + skillId + "-" +
											level + ")");
									continue;
								}

								//if (npcSkill.getSkillType() == L2SkillType.NOTDONE)
								//	Log.warning("NPC " + npcId + " has a not implemented skill (" + skillId + "-" + level + ")");

								npc.addSkill(npcSkill);
							}

							if (propertyNode.getName().equalsIgnoreCase("spoilDrop") ||
									propertyNode.getName().equalsIgnoreCase("itemDrop"))
							{
								int itemId = propertyNode.getInt("itemId");
								int min = propertyNode.getInt("min");
								int max = propertyNode.getInt("max");
								float chance = propertyNode.getFloat("chance");

								L2DropData dd = new L2DropData(itemId, min, max, chance);

								L2Item item = ItemTable.getInstance().getTemplate(dd.getItemId());
								if (item == null)
								{
									Log.warning(
											"Drop data for undefined item template! NpcId: " + npc.NpcId + " itemId: " +
													dd.getItemId());
									continue;
								}

								if (propertyNode.getName().equalsIgnoreCase("spoilDrop"))
								{
									npc.addSpoilData(dd);
								}
								else
								{
									npc.addDropData(dd);
								}
							}

							if (propertyNode.getName().equalsIgnoreCase("dropCategory"))
							{
								float chance = propertyNode.getFloat("chance");
								L2DropCategory dc = new L2DropCategory(chance);

								for (XmlNode dropCategoryNode : propertyNode.getChildren())
								{
									if (dropCategoryNode.getName().equalsIgnoreCase("itemDrop"))
									{
										int itemId = dropCategoryNode.getInt("itemId");
										int min = dropCategoryNode.getInt("min");
										int max = dropCategoryNode.getInt("max");
										float chance2 = dropCategoryNode.getFloat("chance");
										L2DropData dd = new L2DropData(itemId, min, max, chance2);

										L2Item item = ItemTable.getInstance().getTemplate(dd.getItemId());
										if (item == null)
										{
											Log.warning("Drop data for undefined item template! NpcId: " + npc.NpcId +
													" itemId: " + dd.getItemId());
											continue;
										}

										dc.addDropData(dd);
									}
								}
								/*if (dc.getAllDrops().size() == 1)
                                {
									L2DropData dd = dc.getAllDrops().getFirst();
									npc.addDropData(new L2DropData(dd.getItemId(), dd.getMinDrop(), dd.getMaxDrop(), dc.getChance()));
								}
								else if (!dc.getAllDrops().isEmpty())*/
								npc.addMultiDrop(dc);
							}

							if (propertyNode.getName().equalsIgnoreCase("spawn"))
							{
								int x = propertyNode.getInt("x");
								int y = propertyNode.getInt("y");
								int z = propertyNode.getInt("z");
								int heading = propertyNode.getInt("heading");
								int respawn = propertyNode.getInt("respawn");
								int randomRespawn = 0;
								if (propertyNode.hasAttribute("randomRespawn"))
								{
									randomRespawn = propertyNode.getInt("randomRespawn");
								}

								SpawnData sp = new SpawnData(x, y, z, heading, respawn, randomRespawn);
								if (propertyNode.hasAttribute("dbName"))
								{
									sp.DbName = propertyNode.getString("dbName");
								}

								npc.addSpawn(sp);
							}
						}

						if (!_npcs.containsKey(npc.NpcId))
						{
							L2NpcTemplate original = npc;
							npc = new L2NpcTemplate(original.getBaseSet(), original);
						}
						_npcs.put(npc.NpcId, npc);
						SearchDropManager.getInstance().addLootInfo(npc, overrideDrops);

						loadedNpcs++;
					}
				}
			}

			if (!overrideDrops)
			{
				Log.info("Npc Table: Loaded " + loadedNpcs + " npcs.");
			}
			else
			{
				Log.info("Npc Table: Overridden " + loadedNpcs + " npcs with custom data.");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void save()
	{
		try
		{
			for (int i = 0; i < 1000; i++)
			{
				boolean createFile = false;

				for (int j = i * 1000; j < (i + 1) * 1000; j++)
				{
					if (_npcs.containsKey(j))
					{
						createFile = true;
						break;
					}
				}

				if (!createFile)
				{
					continue;
				}

				String fileName = ("00000" + i * 1000).substring(String.valueOf(i * 1000).length()) + "-" +
						("00000" + ((i + 1) * 1000 - 1)).substring(String.valueOf((i + 1) * 1000 - 1).length()) +
						".xml";
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "npcs/" + fileName),
						"UTF-8"));

				out.write("<list>\r\n");

				for (int j = i * 1000; j < (i + 1) * 1000; j++)
				{
					L2NpcTemplate t = _npcs.get(j);

					if (t == null)
					{
						continue;
					}

					while (t.getBaseTemplate() != null && t.getBaseTemplate() != t)
					{
						t = t.getBaseTemplate();
					}

					String baseNode = "\t<npc" + t.getXmlNpcId() + t.getXmlTemplateId() + t.getXmlName() +
							t.getXmlServerSideName() + t.getXmlTitle() + t.getXmlServerSideTitle() + t.getXmlLevel() +
							t.getXmlType() + t.getXmlInteractionDistance() + t.getXmlAttackRange() + t.getXmlMaxHp() +
							t.getXmlHpReg() + t.getXmlMaxMp() + t.getXmlMpReg() + t.getXmlSTR() + t.getXmlCON() +
							t.getXmlDEX() + t.getXmlINT() + t.getXmlWIT() + t.getXmlMEN() + t.getXmlExp() +
							t.getXmlSp() + t.getXmlPAtk() + t.getXmlMAtk() + t.getXmlPDef() + t.getXmlMDef() +
							t.getXmlPAtkSpd() + t.getXmlMAtkSpd() + t.getXmlCritical() + t.getXmlMCritical() +
							t.getXmlWalkSpd() + t.getXmlRunSpd() + t.getXmlRandomWalk() + t.getXmlAggessive() +
							t.getXmlAggroRange() + t.getXmlCanSeeThroughSilentMove() + t.getXmlRHand() +
							t.getXmlLHand() + t.getXmlExtraDropGroup() + t.getXmlCollisionRadius() +
							t.getXmlCollisionHeight() + t.getXmlTargetable() + t.getXmlShowName() +
							t.getXmlIsLethalImmune() + t.getXmlIsDebuffImmune() + t.getXmlCanBeChampion() +
							t.getXmlIsNonTalking() + t.getXmlElemAtk() + t.getXmlElemRes() + t.getXmlAIType() +
							t.getXmlSkillChance() + t.getXmlPrimaryAttack() + t.getXmlCanMove() +
							t.getXmlMinRangeSkill() + t.getXmlMinRangeChance() + t.getXmlMaxRangeSkill() +
							t.getXmlMaxRangeChance() + t.getXmlSoulshots() + t.getXmlSpiritshots() +
							t.getXmlSSChance() + t.getXmlSpSChance() + t.getXmlIsChaos() + t.getXmlClan() +
							t.getXmlClanRange() + t.getXmlEnemy() + t.getXmlEnemyRange() + t.getXmlDodge() +
							t.getXmlMinSocial1() + t.getXmlMaxSocial1() + t.getXmlMinSocial2() + t.getXmlMaxSocial2() +
							">\r\n";

					int index = baseNode.indexOf(" ", 120);
					while (index >= 120)
					{
						out.write(baseNode.substring(0, index));
						baseNode = "\r\n\t\t\t" + baseNode.substring(index + 1);
						index = baseNode.indexOf(" ", 120);
					}
					out.write(baseNode);

					if (t.getRandomMinionData() != null)
					{
						String mins = "";

						int count = t.getRandomMinionData().getMinionIds().size();

						for (int minionId : t.getRandomMinionData().getMinionIds())
						{
							mins += String.valueOf(minionId) + (count > 1 ? ";" : "");

							count--;
						}
						out.write("\t\t<randomMinion npcId=\"" + mins + "\" count=\"" +
								t.getRandomMinionData().getAmount() + "\" />\r\n");
					}

					if (t.getMinionData() != null)
					{
						for (L2MinionData md : t.getMinionData())
						{
							String comment = "";
							L2NpcTemplate mt = getTemplate(md.getMinionId());
							if (mt != null && mt.getName() != null)
							{
								comment = " <!-- " + mt.getName() + " -->";
							}
							out.write("\t\t<minion npcId=\"" + md.getMinionId() + "\" min=\"" + md.getAmountMin() +
									"\" max=\"" + md.getAmountMax() + "\" />" + comment + "\r\n");
						}
					}

					if (t.getSkills() != null)
					{
						for (L2Skill skill : t.getSkills().values())
						{
							String comment = "";
							if (skill.getName() != null)
							{
								comment = " <!-- " + skill.getName() + " -->";
							}
							out.write("\t\t<skill id=\"" + skill.getId() + "\" level=\"" + skill.getLevel() + "\" />" +
									comment + "\r\n");
						}
					}

					for (L2DropData dd : t.getSpoilData())
					{
						if (dd.isCustom())
						{
							continue;
						}

						String comment = "";
						L2Item it = ItemTable.getInstance().getTemplate(dd.getItemId());
						if (it != null && it.getName() != null)
						{
							comment = " <!-- " + it.getName() + " -->";
						}
						out.write("\t\t<spoilDrop itemId=\"" + dd.getItemId() + "\" min=\"" + dd.getMinDrop() +
								"\" max=\"" + dd.getMaxDrop() + "\" chance=\"" + Util.getDecimalString(dd.getChance()) +
								"\" />" + comment + "\r\n");
					}

					for (L2DropData dd : t.getDropData())
					{
						if (dd.isCustom())
						{
							continue;
						}

						String comment = "";
						L2Item it = ItemTable.getInstance().getTemplate(dd.getItemId());
						if (it != null && it.getName() != null)
						{
							comment = " <!-- " + it.getName() + " -->";
						}
						out.write("\t\t<itemDrop itemId=\"" + dd.getItemId() + "\" min=\"" + dd.getMinDrop() +
								"\" max=\"" + dd.getMaxDrop() + "\" chance=\"" + Util.getDecimalString(dd.getChance()) +
								"\" />" + comment + "\r\n");
					}

					for (L2DropCategory dc : t.getMultiDropData())
					{
						if (dc.isCustom())
						{
							continue;
						}

						out.write("\t\t<dropCategory chance=\"" + Util.getDecimalString(dc.getChance()) + "\">\r\n");
						for (L2DropData dd : dc.getAllDrops())
						{
							String comment = "";
							L2Item it = ItemTable.getInstance().getTemplate(dd.getItemId());
							if (it != null && it.getName() != null)
							{
								comment = " <!-- " + it.getName() + " -->";
							}
							out.write("\t\t\t<itemDrop itemId=\"" + dd.getItemId() + "\" min=\"" + dd.getMinDrop() +
									"\" max=\"" + dd.getMaxDrop() + "\" chance=\"" +
									Util.getDecimalString(dd.getChance()) + "\" />" + comment + "\r\n");
						}
						out.write("\t\t</dropCategory>\r\n");
					}

					for (SpawnData s : t.getSpawns())
					{
						out.write("\t\t<spawn x=\"" + s.X + "\" y=\"" + s.Y + "\" z=\"" + s.Z + "\" " + "heading=\"" +
								s.Heading + "\" respawn=\"" + s.Respawn + "\"");
						if (s.RandomRespawn > 0)
						{
							out.write(" randomRespawn=\"" + s.RandomRespawn + "\"");
						}
						if (s.DbName != null)
						{
							out.write(" dbName=\"" + s.DbName + "\"");
						}
						out.write(" />\r\n");
					}

					out.write("\t</npc>\r\n");
				}

				out.write("</list>\r\n");
				out.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}

	public L2NpcTemplate getTemplateByName(String name)
	{
		for (Object npcTemplate : _npcs.getValues())
		{
			if (((L2NpcTemplate) npcTemplate).Name.equalsIgnoreCase(name))
			{
				return (L2NpcTemplate) npcTemplate;
			}
		}

		return null;
	}

	public L2NpcTemplate[] getAllTemplates()
	{
		List<L2NpcTemplate> list = new ArrayList<>();

		for (Object t : _npcs.getValues())
		{
			list.add((L2NpcTemplate) t);
		}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();

		for (Object t : _npcs.getValues())
		{
			if (((L2NpcTemplate) t).Level == lvl)
			{
				list.add((L2NpcTemplate) t);
			}
		}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();

		for (Object t : _npcs.getValues())
		{
			if (((L2NpcTemplate) t).Level == lvl && "L2Monster".equals(((L2NpcTemplate) t).Type))
			{
				list.add((L2NpcTemplate) t);
			}
		}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public L2NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		List<L2NpcTemplate> list = new ArrayList<>();

		for (Object t : _npcs.getValues())
		{
			if (((L2NpcTemplate) t).Name.startsWith(letter) && "L2Npc".equals(((L2NpcTemplate) t).Type))
			{
				list.add((L2NpcTemplate) t);
			}
		}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	/**
	 * @param classType
	 * @return
	 */
	public Set<Integer> getAllNpcOfClassType(String classType)
	{
		return null;
	}

	/**
	 * @return
	 */
	public Set<Integer> getAllNpcOfL2jClass(Class<?> clazz)
	{
		return null;
	}

	/**
	 * @param aiType
	 * @return
	 */
	public Set<Integer> getAllNpcOfAiType(String aiType)
	{
		return null;
	}

	public final L2NpcTemplate[] getAllRaidBoss()
	{
		final ArrayList<L2NpcTemplate> list = new ArrayList<>();

		for (final Object t : _npcs.getValues())
		{
			if ("L2Raidboss".equalsIgnoreCase(((L2NpcTemplate) t).Type))
			{
				list.add((L2NpcTemplate) t);
			}
		}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public final L2NpcTemplate[] getAllMonsters()
	{
		final ArrayList<L2NpcTemplate> list = new ArrayList<>();

		for (final Object t : _npcs.getValues())
		{
			if ("L2Monster".equalsIgnoreCase(((L2NpcTemplate) t).Type))
			{
				list.add((L2NpcTemplate) t);
			}
		}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public final L2NpcTemplate[] getAllNpcByType(final String type)
	{
		final ArrayList<L2NpcTemplate> list = new ArrayList<>();

		for (final Object t : _npcs.getValues())
		{
			if (type.equalsIgnoreCase(((L2NpcTemplate) t).Type))
			{
				list.add((L2NpcTemplate) t);
			}
		}
		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	public final L2NpcTemplate[] getAllMonstersBetweenLevels(final int minLevel, final int maxLevel)
	{
		final ArrayList<L2NpcTemplate> list = new ArrayList<>();

		for (final Object t : _npcs.getValues())
		{
			if (!((L2NpcTemplate) t).Type.equals("L2Monster"))
			{
				continue;
			}
			else if (((L2NpcTemplate) t).Level < minLevel || ((L2NpcTemplate) t).Level > maxLevel)
			{
				continue;
			}
			else if (((L2NpcTemplate) t).getKnownSpawns().size() == 0)
			{
				continue;
			}

			list.add((L2NpcTemplate) t);
		}

		return list.toArray(new L2NpcTemplate[list.size()]);
	}

	//From MagicVisor
	public DropChances calculateRewardChances(L2NpcTemplate template, L2PcInstance player, L2DropData drop, float catChance, int levelModifier, boolean isSweep, L2Npc mob)
	{
		float dropChance = drop.getChance() * catChance / 100.0f;
		int deepBlueDrop = 1;

		boolean isRaid = false;
		boolean isChampion = false;
		if (mob != null)
		{
			isRaid = template.Type.contains("Boss");
			isChampion = Config.L2JMOD_CHAMPION_ENABLE && mob.isChampion();
		}

		if (Config.DEEPBLUE_DROP_RULES && levelModifier > 0)
		{
			deepBlueDrop = 3;
			if (drop.getItemId() == 57)
			{
				deepBlueDrop *= (int) Config.RATE_DROP_ITEMS_BY_RAID;
			}
		}

		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}

		if (Config.DEEPBLUE_DROP_RULES)
		{
			dropChance = (dropChance - dropChance * levelModifier / 100) / deepBlueDrop;
		}

		if (!drop.isCustom())
		{
			if (Config.RATE_DROP_ITEMS_ID.containsKey(drop.getItemId()))
			{
				dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
			}
			else if (isSweep)
			{
				dropChance *= Config.RATE_DROP_SPOIL;
			}
			else
			{
				dropChance *= isRaid ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}
		}

		dropChance = (float) player.calcStat(Stats.DROP_RATE, dropChance, null, null);
		if (isChampion)
		{
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}

		if (dropChance < 0.00001F)
		{
			dropChance = 0.00001F;
		}

		DropChances dropChances = new DropChances();
		dropChances.itemName = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();
		dropChances.icon = ItemTable.getInstance().getTemplate(drop.getItemId()).getIcon();
		dropChances.min = 0;
		dropChances.max = 0;
		dropChances.chance = dropChance;

		if (dropChance >= L2DropData.MAX_CHANCE)
		{
			dropChances.chance = L2DropData.MAX_CHANCE;
		}

		if (dropChance <= L2DropData.MAX_CHANCE)
		{
			dropChances.min += drop.getMinDrop();
			dropChances.max += drop.getMaxDrop();
		}
		else
		{
			while (dropChance > L2DropData.MAX_CHANCE)
			{
				dropChances.min += drop.getMinDrop();
				dropChances.max += drop.getMaxDrop();
				dropChance -= L2DropData.MAX_CHANCE;
			}

			if (dropChance > 0)
			{
				dropChances.max += drop.getMaxDrop();
			}
		}

		if (dropChances.min == 0)
		{
			dropChances.min = 1;
		}

		if (dropChances.max == 0)
		{
			dropChances.max = 1;
		}

		return dropChances;
	}

	public class DropChances
	{
		public String itemName;
		public String icon;
		public long min;
		public long max;
		public float chance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final NpcTable _instance = new NpcTable();
	}
}
