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
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2PcTemplate;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author Unknown, Forsaiken
 */
public final class CharTemplateTable implements Reloadable
{

	public static CharTemplateTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private final L2PcTemplate[] _templates;

	private CharTemplateTable()
	{
		_templates = new L2PcTemplate[Race.values().length * 2];
		load();

		ReloadableManager.getInstance().register("chartemplates", this);
	}

	public void load()
	{
		File file = null;

		File customFile = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/races.xml");
		if (customFile.exists())
		{
			file = customFile;
		}
		else
		{
			file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "races.xml");
		}

		XmlDocument doc = new XmlDocument(file);
		int count = 0;
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{

				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("race"))
					{
						int raceId = d.getInt("id");
						String raceName = d.getString("name");
						for (XmlNode raceNode : d.getChildren())
						{
							if (raceNode.getName().equalsIgnoreCase("template"))
							{
								StatsSet set = new StatsSet();
								set.set("raceId", raceId);
								set.set("raceName", raceName);
								set.set("isMage", raceNode.getString("type").equalsIgnoreCase("mage"));
								set.set("startingClassId", raceNode.getInt("startingClassId"));
								set.set("STR", raceNode.getInt("STR"));
								set.set("CON", raceNode.getInt("CON"));
								set.set("DEX", raceNode.getInt("DEX"));
								set.set("INT", raceNode.getInt("INT"));
								set.set("WIT", raceNode.getInt("WIT"));
								set.set("MEN", raceNode.getInt("MEN"));
								set.set("LUC", raceNode.getInt("LUC"));
								set.set("CHA", raceNode.getInt("CHA"));
								set.set("hpReg", 1.5);
								set.set("mpReg", 0.9);
								set.set("pAtk", raceNode.getInt("basePAtk"));
								set.set("mAtk", raceNode.getInt("baseMAtk"));
								set.set("pDef", raceNode.getInt("basePDef"));
								set.set("mDef", raceNode.getInt("baseMDef"));
								set.set("pAtkSpd", raceNode.getInt("basePAtkSpd"));
								set.set("mAtkSpd", raceNode.getInt("baseMAtkSpd"));
								set.set("pCritRate", raceNode.getInt("baseCritical") / 10);
								set.set("runSpd", raceNode.getInt("baseMoveSpd"));
								set.set("walkSpd", raceNode.getInt("baseMoveSpd") * 70 / 100);
								set.set("shldDef", 0);
								set.set("shldRate", 0);
								set.set("atkRange", 40);

								set.set("collisionRadius", raceNode.getFloat("mColRadius"));
								set.set("collisionHeight", raceNode.getFloat("mColHeight"));
								set.set("collisionRadiusFemale", raceNode.getFloat("fColRadius"));
								set.set("collisionHeightFemale", raceNode.getFloat("fColHeight"));

								set.set("startX", raceNode.getInt("startX"));
								set.set("startY", raceNode.getInt("startY"));
								set.set("startZ", raceNode.getInt("startZ"));
								set.set("startRandom", raceNode.getInt("startRandom"));

								L2PcTemplate ct = new L2PcTemplate(set);

								for (XmlNode itemNode : raceNode.getChildren())
								{
									if (itemNode.getName().equalsIgnoreCase("creationItem"))
									{
										int itemId = itemNode.getInt("id");
										int amount = itemNode.getInt("count");
										boolean equipped = itemNode.getBool("equipped");

										if (ItemTable.getInstance().getTemplate(itemId) != null)
										{
											ct.addItem(itemId, amount, equipped);
										}
										else
										{
											Log.warning(
													"races: No data for itemId: " + itemId + " defined for race id " +
															raceId);
										}
									}
								}

								_templates[ct.race.ordinal() * 2 + (ct.isMage ? 1 : 0)] = ct;
								count++;
							}
							else if (raceNode.getName().equalsIgnoreCase("skill"))
							{
								_templates[raceId * 2].addSkill(raceNode.getInt("id"));
								_templates[raceId * 2 + 1].addSkill(raceNode.getInt("id"));
							}
						}
					}
					else if (d.getName().equalsIgnoreCase("creationItem"))
					{
						int itemId = d.getInt("id");
						int amount = d.getInt("count");
						boolean equipped = d.getBool("equipped");

						if (ItemTable.getInstance().getTemplate(itemId) != null)
						{
							for (L2PcTemplate pct : _templates)
							{
								if (pct != null)
								{
									pct.addItem(itemId, amount, equipped);
								}
							}
						}
						else
						{
							Log.warning("races: No data for itemId: " + itemId + " defined for all the pc templates");
						}
					}
				}
			}
		}
		Log.info("CharTemplateTable: Loaded " + count + " Character Templates.");
	}

	@Override
	public boolean reload()
	{
		load();
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			player.setTemplate(_templates[player.getRace().ordinal() * 2 + (player.getTemplate().isMage ? 1 : 0)]);
			player.broadcastUserInfo();
		}
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Character templates reloaded";
	}

	public L2PcTemplate getTemplate(int tId)
	{
		return _templates[tId];
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CharTemplateTable _instance = new CharTemplateTable();
	}
}
