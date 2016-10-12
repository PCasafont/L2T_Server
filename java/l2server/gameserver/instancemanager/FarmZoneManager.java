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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.L2DropCategory;
import l2server.gameserver.model.L2DropData;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.*;

/**
 * @author Pere
 */
public class FarmZoneManager
{
	public class FarmZone
	{
		private String _name;
		private Set<L2NpcTemplate> _mobs = new HashSet<>();

		public FarmZone(String name)
		{
			_name = name;
		}

		public void addMob(L2NpcTemplate mob)
		{
			_mobs.add(mob);
		}

		public String getName()
		{
			return _name;
		}

		public Set<L2NpcTemplate> getMobs()
		{
			return _mobs;
		}
	}

	private Map<String, FarmZone> _farmZones = new HashMap<>();

	private static FarmZoneManager _instance;

	public static FarmZoneManager getInstance()
	{
		return _instance == null ? (_instance = new FarmZoneManager()) : _instance;
	}

	private FarmZoneManager()
	{
		if (!Config.SERVER_NAME.isEmpty())
		{
			load();
		}
	}

	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "farmZones.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode farmNode : n.getChildren())
				{
					if (farmNode.getName().equalsIgnoreCase("farmZone"))
					{
						String name = farmNode.getString("name");
						FarmZone farmZone = new FarmZone(name);
						//System.out.println();
						//System.out.println("Farm zone " + name + ":");
						for (XmlNode mobNode : farmNode.getChildren())
						{
							if (mobNode.getName().equalsIgnoreCase("mob"))
							{
								int mobId = mobNode.getInt("id");
								L2NpcTemplate temp = NpcTable.getInstance().getTemplate(mobId);
								if (temp == null)
								{
									continue;
								}

								farmZone.addMob(temp);

								/*try
								{
									L2Spawn spawn = new L2Spawn(temp);
									spawn.doSpawn();
									L2Npc mobInstance = spawn.getNpc();
									float mobFarmCost = mobInstance.getMaxHp() * (float)(mobInstance.getPDef(null) + mobInstance.getMDef(null, null));
									mobInstance.deleteMe();
									if (mobInstance.getSpawn() != null)
										mobInstance.getSpawn().stopRespawn();

									float expScore = temp.RewardExp / mobFarmCost * (10000.0f - (99 - temp.Level) * 1000.0f);
									System.out.println("\tId: " + temp.NpcId + "\tLvl: " + temp.Level + "(" + (100.0f - (99 - temp.Level) * 10.0f) + "%)"
											+ "\tExpScore: " + expScore + "\tExp: " + temp.RewardExp
											+ "\tStrength: " + mobFarmCost / 1000000 + "\tHP: " + mobInstance.getMaxHp()
											+ "\tpDef: " + mobInstance.getPDef(null) + "\tpAtk: " + mobInstance.getPAtk(null)
											+ "\tmDef: " + mobInstance.getMDef(null, null) + "\tmAtk: " + mobInstance.getMAtk(null, null));
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}*/
							}
						}

						_farmZones.put(name, farmZone);
					}
				}
			}
		}
		Log.info("Farm Zone Manager: loaded " + _farmZones.size() + " farm zone definitions.");

		file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/customFarm.xml");
		doc = new XmlDocument(file);
		if (doc.getFirstChild() == null)
		{
			return;
		}

		int customized = 0;
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode farmNode : n.getChildren())
				{
					if (farmNode.getName().equalsIgnoreCase("excludeDrop"))
					{
						int itemId = farmNode.getInt("itemId");
						for (L2NpcTemplate npc : NpcTable.getInstance().getAllTemplates())
						{
							List<L2DropData> dropsToRemove = new ArrayList<>();
							for (L2DropData dd : npc.getDropData())
							{
								if (dd.getItemId() == itemId)
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
									if (dd.getItemId() == itemId)
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
					else if (farmNode.getName().equalsIgnoreCase("customFarm"))
					{
						Set<L2NpcTemplate> mobs = new HashSet<>();
						if (farmNode.hasAttribute("farmZone"))
						{
							String name = farmNode.getString("farmZone");
							FarmZone farmZone = _farmZones.get(name);
							for (L2NpcTemplate mob : farmZone.getMobs())
							{
								mobs.add(mob);
							}
						}
						else if (farmNode.hasAttribute("levelRange"))
						{
							String[] levelRange = farmNode.getString("levelRange").split("-");
							int minLvl = Integer.parseInt(levelRange[0]);
							int maxLvl = Integer.parseInt(levelRange[1]);
							for (L2NpcTemplate mob : NpcTable.getInstance().getAllTemplates())
							{
								if (mob.Type.equals("L2Monster") && mob.Level >= minLvl && mob.Level <= maxLvl)
								{
									mobs.add(mob);
								}
							}
						}
						else
						{
							Log.warning("There's a farm customization without any monster group specified!");
							continue;
						}

						float hpMultiplier = farmNode.getFloat("hpMultiplier", 1.0f);
						float atkMultiplier = farmNode.getFloat("atkMultiplier", 1.0f);
						float defMultiplier = farmNode.getFloat("defMultiplier", 1.0f);
						int level = farmNode.getInt("overrideLevels", 0);
						boolean overrideDrops = farmNode.getBool("overrideDrops", false);
						float expMultiplier = farmNode.getFloat("expMultiplier", 1.0f);

						float baseMobFarmCost = 0.0f;
						if (farmNode.hasAttribute("adjustDropsPerMob"))
						{
							int baseMobId = farmNode.getInt("adjustDropsPerMob");
							L2NpcTemplate baseMobTemplate = NpcTable.getInstance().getTemplate(baseMobId);
							if (baseMobTemplate != null)
							{
								try
								{
									L2Spawn spawn = new L2Spawn(baseMobTemplate);
									spawn.doSpawn();
									L2Npc baseMob = spawn.getNpc();
									baseMobFarmCost = baseMob.getMaxHp() *
											(float) (baseMob.getPDef(null) + baseMob.getMDef(null, null));
									baseMob.deleteMe();
									if (baseMob.getSpawn() != null)
									{
										baseMob.getSpawn().stopRespawn();
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}

						List<L2DropData> drops = new ArrayList<>();
						List<L2DropData> spoilDrops = new ArrayList<>();
						List<L2DropCategory> dropCategories = new ArrayList<>();
						for (XmlNode dropNode : farmNode.getChildren())
						{
							if (dropNode.getName().equalsIgnoreCase("itemDrop") ||
									dropNode.getName().equalsIgnoreCase("spoilDrop"))
							{
								int itemId = dropNode.getInt("itemId");
								int min = dropNode.getInt("min");
								int max = dropNode.getInt("max");
								float chance = dropNode.getFloat("chance");

								L2DropData dd = new L2DropData(itemId, min, max, chance);

								L2Item item = ItemTable.getInstance().getTemplate(dd.getItemId());
								if (item == null)
								{
									Log.warning("Drop data for undefined item template in custom drop definitions!");
									continue;
								}

								if (dropNode.getName().equalsIgnoreCase("itemDrop"))
								{
									drops.add(dd);
								}
								else
								{
									spoilDrops.add(dd);
								}
							}
							else if (dropNode.getName().equalsIgnoreCase("dropCategory"))
							{
								float chance = dropNode.getFloat("chance");
								L2DropCategory dc = new L2DropCategory(chance);

								for (XmlNode dropCategoryNode : dropNode.getChildren())
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
											Log.warning(
													"Drop data for undefined item template in custom drop definitions!");
											continue;
										}

										dc.addDropData(dd);
									}
								}

								dropCategories.add(dc);
							}
						}

						for (L2NpcTemplate mob : mobs)
						{
							float dropMultiplier = 1.0f;
							if (baseMobFarmCost > 0.0f)
							{
								try
								{
									L2Spawn spawn = new L2Spawn(mob);
									spawn.doSpawn();
									L2Npc mobInstance = spawn.getNpc();
									float mobFarmCost = mobInstance.getMaxHp() *
											(float) (mobInstance.getPDef(null) + mobInstance.getMDef(null, null));
									dropMultiplier = mobFarmCost / baseMobFarmCost;
									mobInstance.deleteMe();
									if (mobInstance.getSpawn() != null)
									{
										mobInstance.getSpawn().stopRespawn();
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}

							mob.baseHpMax *= hpMultiplier;
							mob.basePAtk *= atkMultiplier;
							mob.baseMAtk *= atkMultiplier;
							mob.basePDef *= defMultiplier;
							mob.baseMDef *= defMultiplier;
							mob.RewardExp = (long) (mob.RewardExp * expMultiplier);

							if (level > 0)
							{
								mob.Level = (byte) level;
							}

							if (overrideDrops)
							{
								mob.getMultiDropData().clear();
								mob.getDropData().clear();
								mob.getSpoilData().clear();
							}

							for (L2DropData drop : drops)
							{
								int min = drop.getMinDrop();
								int max = drop.getMaxDrop();
								float chance = drop.getChance() * dropMultiplier;
								if (min >= 10 && chance > 100.0f ||
										min * dropMultiplier >= 5 && chance / dropMultiplier <= 100.01f)
								{
									min = Math.round(min * dropMultiplier);
									max = Math.round(max * dropMultiplier);
									chance /= dropMultiplier;
								}

								while (chance > 100.01f)
								{
									min *= 2;
									max *= 2;
									chance /= 2.0f;
								}

								while (chance < 50.0f && min > 1)
								{
									min /= 2;
									max /= 2;
									chance *= 2.0f;
								}

								mob.addDropData(new L2DropData(drop.getItemId(), min, max, chance));
							}

							for (L2DropData drop : spoilDrops)
							{
								int min = drop.getMinDrop();
								int max = drop.getMaxDrop();
								float chance = drop.getChance() * dropMultiplier;
								if (min >= 10 && chance > 100.0f ||
										min * dropMultiplier >= 5 && chance / dropMultiplier <= 100.01f)
								{
									min = Math.round(min * dropMultiplier);
									max = Math.round(max * dropMultiplier);
									chance /= dropMultiplier;
								}

								while (chance > 100.01f)
								{
									min *= 2;
									max *= 2;
									chance /= 2.0f;
								}

								while (chance < 50.0f && min > 1)
								{
									min /= 2;
									max /= 2;
									chance *= 2.0f;
								}

								mob.addSpoilData(new L2DropData(drop.getItemId(), min, max, chance));
							}

							for (L2DropCategory dropCategory : dropCategories)
							{
								int multiplier = 1;
								float chance = dropCategory.getChance() * dropMultiplier;
								while (chance > 100.0f)
								{
									multiplier *= 2;
									chance /= 2.0f;
								}

								L2DropCategory dc = new L2DropCategory(chance);
								for (L2DropData drop : dropCategory.getAllDrops())
								{
									dc.addDropData(new L2DropData(drop.getItemId(), drop.getMinDrop() * multiplier,
											drop.getMaxDrop() * multiplier, drop.getChance()));
								}
								mob.addMultiDrop(dc);
							}
						}

						customized++;
					}
				}
			}
		}
		Log.info("Farm Zone Manager: loaded " + customized + " farm zone customizations.");
	}
}
