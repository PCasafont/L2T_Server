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

package l2server.gameserver;

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.templates.SpawnData;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * @author Pere
 */
public class SqlToXml
{
	public static void races()
	{
		Connection con = null;

		try
		{
			String content = "<list>\r\n";

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM char_templates GROUP BY raceId");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				content += "\t<race id=\"" + rset.getInt("raceId") + "\">\r\n";

				PreparedStatement st2 = con.prepareStatement(
						"SELECT * FROM char_templates WHERE raceId = ? GROUP BY P_ATK ORDER BY classId");
				st2.setInt(1, rset.getInt("raceId"));
				ResultSet rset2 = st2.executeQuery();

				boolean fighter = true;
				while (rset2.next())
				{
					content += "\t\t<template type=\"" + (fighter ? "fighter" : "mage") + "\"" + " STR=\"" +
							rset2.getInt("STR") + "\"" + " CON=\"" + rset2.getInt("CON") + "\"" + " DEX=\"" +
							rset2.getInt("DEX") + "\"" + " INT=\"" + rset2.getInt("_INT") + "\"" + " WIT=\"" +
							rset2.getInt("WIT") + "\"" + " MEN=\"" + rset2.getInt("MEN") + "\"" + " basePAtk=\"" +
							rset2.getInt("P_ATK") + "\"" + " baseMAtk=\"" + rset2.getInt("M_ATK") + "\"" +
							" basePDef=\"" + rset2.getInt("P_DEF") + "\"" + " baseMDef=\"" + rset2.getInt("M_DEF") +
							"\"" + " basePAtkSpd=\"" + rset2.getInt("P_SPD") + "\"" + " baseMAtkSpd=\"" +
							rset2.getInt("M_SPD") + "\"" + " baseAccuracy=\"" + rset2.getInt("ACC") + "\"" +
							" baseCritical=\"" + rset2.getInt("CRITICAL") + "\"" + " baseEvasion=\"" +
							rset2.getInt("EVASION") + "\"" + " baseMoveSpd=\"" + rset2.getInt("MOVE_SPD") + "\"" +
							" mColRadius=\"" + rset2.getFloat("M_COL_R") + "\"" + " mColHeight=\"" +
							rset2.getFloat("M_COL_H") + "\"" + " fColRadius=\"" + rset2.getFloat("F_COL_R") + "\"" +
							" fColHeight=\"" + rset2.getFloat("F_COL_H") + "\">\r\n";

					PreparedStatement st3 = con.prepareStatement("SELECT * FROM char_creation_items WHERE classId = ?");
					st3.setInt(1, rset2.getInt("classId"));
					ResultSet rset3 = st3.executeQuery();

					while (rset3.next())
					{
						content += "\t\t\t<creationItem id=\"" + rset3.getInt("itemId") + "\"" + " count=\"" +
								rset3.getInt("amount") + "\"" + " equipped=\"" + rset3.getString("equipped") +
								"\" /> <!-- " +
								ItemTable.getInstance().createDummyItem(rset3.getInt("itemId")).getName() + " -->\r\n";
					}

					content += "\t\t</template>\r\n";

					fighter = false;
				}

				rset2.close();
				st2.close();

				content += "\t</race>\r\n";
			}

			rset.close();
			statement.close();

			content += "\t<!-- Common creation items -->\r\n";

			statement = con.prepareStatement("SELECT * FROM char_creation_items WHERE classId = -1");
			rset = statement.executeQuery();

			while (rset.next())
			{
				content +=
						"\t<creationItem id=\"" + rset.getInt("itemId") + "\"" + " count=\"" + rset.getInt("amount") +
								"\"" + " equipped=\"" + rset.getString("equipped") + "\" /> <!-- " +
								ItemTable.getInstance().createDummyItem(rset.getInt("itemId")).getName() + " -->\r\n";
			}

			content += "</list>\r\n";
			//Util.writeFile("C:/Users/Pere/Desktop/races.xml", content);
			System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void classes()
	{
		Connection con = null;

		try
		{
			String content = "<list>\r\n";

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT * FROM char_templates c, lvlupgain l WHERE c.classId = l.classid");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				content +=
						"\t<class id=\"" + rset.getInt("c.classId") + "\"" + " name=\"" + rset.getString("className") +
								"\"" + " level=\"" + rset.getInt("class_lvl") + "\"" + " baseHp=\"" +
								rset.getInt("defaulthpbase") + "\"" + " hpAdd=\"" + rset.getInt("defaulthpadd") + "\"" +
								" hpMod=\"" + rset.getInt("defaulthpmod") + "\"" + " baseMp=\"" +
								rset.getInt("defaultmpbase") + "\"" + " mpAdd=\"" + rset.getInt("defaultmpadd") + "\"" +
								" mpMod=\"" + rset.getInt("defaultmpmod") + "\"" + " baseCp=\"" +
								rset.getInt("defaultcpbase") + "\"" + " cpAdd=\"" + rset.getInt("defaultcpadd") + "\"" +
								" cpMod=\"" + rset.getInt("defaultcpmod") + "\">\r\n";

				PreparedStatement st2 = con.prepareStatement(
						"SELECT min(level), max(level), skill_trees.* FROM skill_trees WHERE class_id = ? GROUP BY min_level, sp, skill_id ORDER BY min_level, sp, skill_id, level");
				st2.setInt(1, rset.getInt("c.classId"));
				ResultSet rset2 = st2.executeQuery();

				while (rset2.next())
				{
					content += "\t\t<skill id=\"" + rset2.getInt("skill_id") + "\"" + " level=\"";
					for (int level = rset2.getInt("min(level)"); level <= rset2.getInt("max(level)"); level++)
					{
						content += level + ",";
					}
					content = content.substring(0, content.length() - 1);
					content += "\" reqSp=\"" + rset2.getInt("sp") + "\"" + " minLevel=\"" + rset2.getInt("min_level") +
							"\"" +
							(rset2.getString("learned_by_npc").equals("false") ? " learnFromPanel=\"false\"" : "") +
							(rset2.getString("learned_by_fs").equals("true") ? " learnFromFS=\"true\"" : "") +
							(rset2.getString("is_transfer").equals("true") ? " isTransfer=\"true\"" : "") +
							(rset2.getString("is_autoget").equals("true") ? " autoGet=\"true\"" : "") + " /> <!-- " +
							SkillTable.getInstance().getInfo(rset2.getInt("skill_id"), rset2.getInt("level"))
									.getName() + " -->\r\n";
				}

				content += "\t</class>\r\n";
			}

			rset.close();
			statement.close();

			content += "</list>\r\n";
			Util.writeFile(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "classes.xml", content);
			//System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void shops()
	{
		Connection con = null;

		try
		{
			String content = "<list>\r\n";

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM merchant_shopids ORDER BY npc_id ASC");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				content += "\t<shop id=\"" + rset.getString("shop_id") + "\"" + " npcId=\"" +
						(rset.getString("npc_id").equalsIgnoreCase("gm") ? -1 : rset.getInt("npc_id")) + "\">" +
						" <!-- " + (rset.getString("npc_id").equalsIgnoreCase("gm") ? "GM Shop" :
						NpcTable.getInstance().getTemplate(rset.getInt("npc_id")).getName()) + " -->\r\n";

				PreparedStatement st2 =
						con.prepareStatement("SELECT * FROM merchant_buylists WHERE shop_id = ? ORDER BY `order` ASC");
				st2.setInt(1, rset.getInt("shop_id"));
				ResultSet rset2 = st2.executeQuery();

				while (rset2.next())
				{
					content += "\t\t<item id=\"" + rset2.getInt("item_id") + "\"" +
							(rset2.getInt("price") > -1 ? " price=\"" + rset2.getInt("price") + "\"" : "") +
							(rset2.getInt("count") > -1 ? " count=\"" + rset2.getInt("count") + "\"" : "") +
							(rset2.getInt("time") > 0 ? " time=\"" + rset2.getInt("time") + "\"" : "") + " /> <!-- " +
							ItemTable.getInstance().getTemplate(rset2.getInt("item_id")).getName() + " -->\r\n";
				}

				content += "\t</shop>\r\n";
			}

			rset.close();
			statement.close();

			content += "</list>\r\n";
			Util.writeFile(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "shops.xml", content);
			//System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void customShops()
	{
		Connection con = null;

		try
		{
			TIntObjectHashMap<List<L2Item>> shops = new TIntObjectHashMap<>();
			for (int i = 0; i < 23; i++)
			{
				shops.put(i, new ArrayList<>());
			}

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT * FROM custom_merchant_shopids ORDER BY npc_id ASC");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				PreparedStatement st2 = con.prepareStatement(
						"SELECT * FROM custom_merchant_buylists WHERE shop_id = ? ORDER BY `order` ASC");
				st2.setInt(1, rset.getInt("shop_id"));
				ResultSet rset2 = st2.executeQuery();

				while (rset2.next())
				{
					L2Item item = ItemTable.getInstance().getTemplate(rset2.getInt("item_id"));
					int shopId = item.getItemGradePlain();
					if (shopId == L2Item.CRYSTAL_R)
					{
						shopId -= 2;
					}
					if (item.getType2() == L2Item.TYPE2_SHIELD_ARMOR)
					{
						shopId += 7;
					}
					else if (item.getType2() == L2Item.TYPE2_ACCESSORY)
					{
						shopId += 14;
					}
					else if (item.getType2() == L2Item.TYPE2_OTHER || item.getBodyPart() == L2Item.SLOT_HAIR ||
							item.getBodyPart() == L2Item.SLOT_HAIR2 || item.getBodyPart() == L2Item.SLOT_HAIRALL)
					{
						shopId = 21;
					}
					else if (item.getType2() != L2Item.TYPE2_WEAPON)
					{
						shopId = 22;
					}
					shops.get(shopId).add(item);
				}
			}

			rset.close();
			statement.close();

			String content = "<list>\r\n";

			for (int shopId = 0; shopId < 23; shopId++)
			{
				if (shops.get(shopId).isEmpty())
				{
					continue;
				}

				content += "\t<shop id=\"" + shopId + "\" npcId=\"0\">\r\n";

				for (L2Item item : shops.get(shopId))
				{
					content += "\t\t<item id=\"" + item.getItemId() + "\" /> <!-- " + item.getName() + " -->\r\n";
				}

				content += "\t</shop>\r\n";
			}

			content += "</list>\r\n";
			Util.writeFile(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "shops.xml", content);
			//System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void enchantSkillGroups()
	{
		Connection con = null;

		try
		{
			String content = "<list>\r\n";

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT * FROM enchant_skill_groups GROUP BY group_id ORDER BY group_id ASC");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				content += "\t<enchantGroup id=\"" + rset.getString("group_id") + "\">\r\n";

				PreparedStatement st2 = con.prepareStatement(
						"SELECT *, min(level), max(level) FROM enchant_skill_groups WHERE group_id = ? GROUP BY adena ORDER BY level ASC");
				st2.setInt(1, rset.getInt("group_id"));
				ResultSet rset2 = st2.executeQuery();

				while (rset2.next())
				{
					content += "\t\t<enchant level=\"";
					for (int level = rset2.getInt("min(level)"); level <= rset2.getInt("max(level)"); level++)
					{
						content += level + ",";
					}
					content = content.substring(0, content.length() - 1) + "\"";

					content += (rset2.getInt("adena") > -1 ? " adena=\"" + rset2.getInt("adena") + "\"" : "") +
							(rset2.getInt("sp") > -1 ? " sp=\"" + rset2.getInt("sp") + "\"" : "") + ">\r\n";

					int minLvl = 76;
					int current = rset2.getInt("success_rate76");
					for (int lvl = 77; lvl <= 86; lvl++)
					{
						if (lvl == 86 || rset2.getInt("success_rate" + lvl) != current)
						{
							content += "\t\t\t<rate level=\"";
							for (int level = minLvl; level <= lvl - 1; level++)
							{
								content += level + ",";
							}
							content =
									content.substring(0, content.length() - 1) + "\" chance=\"" + current + "\" />\r\n";
							minLvl = lvl;
							if (lvl < 86)
							{
								current = rset2.getInt("success_rate" + lvl);
							}
						}
					}

					content += "\t\t</enchant>\r\n";
				}

				content += "\t</enchantGroup>\r\n";
			}

			rset.close();
			statement.close();

			content += "</list>\r\n";
			Util.writeFile(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "enchantSkillGroups.xml", content);
			//System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private static class SpawnLine
	{
		public int NpcId;
		public SpawnData Point;

		public SpawnLine(int npcId, SpawnData point)
		{
			NpcId = npcId;
			Point = point;
		}
	}

	public static void spawns()
	{
		Connection con = null;

		try
		{
			List<SpawnLine> lines = new ArrayList<>();

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM spawnlist");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int npcId = rset.getInt("npc_templateid");
				int x = rset.getInt("locx");
				int y = rset.getInt("locy");
				int z = rset.getInt("locz");
				int heading = rset.getInt("heading");
				int delay = rset.getInt("respawn_delay");
				lines.add(new SpawnLine(npcId, new SpawnData(x, y, z, heading, delay, 0)));
			}

			rset.close();
			statement.close();

			List<SpawnLine> deleted = new ArrayList<>();

			for (SpawnLine l1 : lines)
			{
				if (l1 == null || deleted.contains(l1))
				{
					continue;
				}

				int dist100 = 0;
				int dist200 = 0;
				for (SpawnLine l2 : lines)
				{
					if (l2 == null || l2 == l1 || deleted.contains(l2))
					{
						continue;
					}

					double distance = Math.sqrt((l2.Point.X - l1.Point.X) * (l2.Point.X - l1.Point.X) +
							(l2.Point.Y - l1.Point.Y) * (l2.Point.Y - l1.Point.Y));

					if (distance < 50)
					{
						deleted.add(l2);
						continue;
					}

					if (distance < 100)
					{
						if (dist100 == 1)
						{
							deleted.add(l2);
							continue;
						}
						dist100++;
					}

					if (distance < 200)
					{
						if (dist200 == 3)
						{
							deleted.add(l2);
							continue;
						}
						dist200++;
					}
				}
			}

			lines.removeAll(deleted);

			for (SpawnLine line : lines)
			{
				NpcTable.getInstance().getTemplate(line.NpcId).addSpawn(line.Point);
			}

			NpcTable.getInstance().save();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void armorSets()
	{
		Connection con = null;

		try
		{
			String content = "<list>\r\n";

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM armorsets ORDER BY chest ASC");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				content += "\t<!-- " + ItemTable.getInstance().createDummyItem(rset.getInt("chest")).getName() +
						" -->\r\n";
				int parts = 1;
				if (rset.getInt("legs") > 0)
				{
					parts++;
				}
				if (rset.getInt("head") > 0)
				{
					parts++;
				}
				if (rset.getInt("gloves") > 0)
				{
					parts++;
				}
				if (rset.getInt("feet") > 0)
				{
					parts++;
				}
				content += "\t<armorSet id=\"" + rset.getInt("chest") + "\" parts=\"" + parts + "\">\r\n";
				String[] skills = rset.getString("skill").split("[;-]");
				for (int i = 0; i < skills.length; i += 2)
				{
					content += "\t\t<skill id=\"" + skills[i] + "\" levels=\"" + skills[i + 1] + "\" /> <!-- " +
							SkillTable.getInstance().getInfo(Integer.valueOf(skills[i]), Integer.valueOf(skills[i + 1]))
									.getName() + " -->\r\n";
				}
				if (rset.getInt("enchant6skill") > 0)
				{
					content += "\t\t<enchant6Skill id=\"" + rset.getInt("enchant6skill") + "\" /> <!-- " +
							SkillTable.getInstance().getInfo(rset.getInt("enchant6skill"), 1).getName() + " -->\r\n";
				}
				if (rset.getInt("shield_skill_id") > 0)
				{
					content += "\t\t<shieldSkill id=\"" + rset.getInt("shield_skill_id") + "\" /> <!-- " +
							SkillTable.getInstance().getInfo(rset.getInt("shield_skill_id"), 1).getName() + " -->\r\n";
				}
				content += "\t</armorSet>\r\n";
			}

			rset.close();
			statement.close();

			content += "</list>\r\n";
			Util.writeFile(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "armorSetss.xml", content);
			//System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void henna()
	{
		Connection con = null;

		try
		{
			String content = "<list>\r\n";

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM henna ORDER BY symbol_id ASC");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				content += "\t<henna symbolId=\"" + rset.getString("symbol_id") + "\"" + " dyeId=\"" +
						rset.getString("dye_id") + "\"" + " name=\"" +
						ItemTable.getInstance().getTemplate(rset.getInt("dye_id")).getName().replace('<', '(')
								.replace('>', ')') + "\"" + " price=\"" + rset.getString("price") + "\"" +
						(rset.getInt("stat_STR") != 0 ? " STR=\"" + rset.getString("stat_STR") + "\"" : "") +
						(rset.getInt("stat_CON") != 0 ? " CON=\"" + rset.getString("stat_CON") + "\"" : "") +
						(rset.getInt("stat_DEX") != 0 ? " DEX=\"" + rset.getString("stat_DEX") + "\"" : "") +
						(rset.getInt("stat_INT") != 0 ? " INT=\"" + rset.getString("stat_INT") + "\"" : "") +
						(rset.getInt("stat_WIT") != 0 ? " WIT=\"" + rset.getString("stat_WIT") + "\"" : "") +
						(rset.getInt("stat_MEM") != 0 ? " MEN=\"" + rset.getString("stat_MEM") + "\"" : "") + ">\r\n";

				PreparedStatement st2 = con.prepareStatement(
						"SELECT class_id FROM henna_trees WHERE symbol_id = ? ORDER BY `class_id` ASC");
				st2.setInt(1, rset.getInt("symbol_id"));
				ResultSet rset2 = st2.executeQuery();

				while (rset2.next())
				{
					content += "\t\t<allowedClass id=\"" + rset2.getInt("class_id") + "\" />" + " <!-- " +
							PlayerClassTable.getInstance().getClassNameById(rset2.getInt("class_id")) + " -->\r\n";
				}

				content += "\t</henna>\r\n";
			}

			rset.close();
			statement.close();

			content += "</list>\r\n";
			Util.writeFile(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "henna.xml", content);
			//System.out.println(content);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void raidBosses()
	{
		Connection con = null;
		PreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * FROM raidboss_spawnlist ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(rset.getInt("boss_id"));
				if (template != null)
				{
					int x = rset.getInt("loc_x");
					int y = rset.getInt("loc_y");
					int z = rset.getInt("loc_z");
					int heading = rset.getInt("heading");
					int delay = rset.getInt("respawn_min_delay");
					int randomDelay = rset.getInt("respawn_max_delay") - delay;
					SpawnData spawn = new SpawnData(x, y, z, heading, delay, randomDelay);
					spawn.DbName = template.Name;
					template.addSpawn(spawn);
				}
				else
				{
					Log.warning(
							"RaidBossSpawnManager: Could not load raidboss #" + rset.getInt("boss_id") + " from DB");
				}
			}

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.warning("RaidBossSpawnManager: Couldnt load raidboss_spawnlist table");
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while initializing RaidBossSpawnManager: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		NpcTable.getInstance().save();
	}

	public static void fortSpawns()
	{
		String content = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			InputStream is = new FileInputStream(new File(Config.CONFIG_DIRECTORY + "fortsiege.properties"));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);
			for (Fort fort : FortManager.getInstance().getForts())
			{
				content += "<!-- " + fort.getName() + " -->\r\n";
				PreparedStatement statement =
						con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = 0");
				statement.setInt(1, fort.getFortId());
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					content += "\t<spawn npcId=\"" + rset.getInt("npcId") + "\" x=\"" + rset.getInt("x") + "\" y=\"" +
							rset.getInt("y") + "\" z=\"" + rset.getInt("z") + "\" heading=\"" + rset.getInt("heading") +
							"\" respawn=\"60\" />\r\n";
				}

				rset.close();
				statement.close();

				content += "\t<specificSpawnList name=\"" + fort.getName() + "_suspicious_merchant\">\r\n";
				statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = 2");
				statement.setInt(1, fort.getFortId());
				rset = statement.executeQuery();
				while (rset.next())
				{
					content += "\t\t<spawn npcId=\"" + rset.getInt("npcId") + "\" x=\"" + rset.getInt("x") + "\" y=\"" +
							rset.getInt("y") + "\" z=\"" + rset.getInt("z") + "\" heading=\"" + rset.getInt("heading") +
							"\" respawn=\"60\" />\r\n";
				}

				rset.close();
				statement.close();
				content += "\t</specificSpawnList>\r\n";

				content += "\t<specificSpawnList name=\"" + fort.getName() + "_npc_commanders\">\r\n";
				statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = 1");
				statement.setInt(1, fort.getFortId());
				rset = statement.executeQuery();
				while (rset.next())
				{
					content += "\t\t<spawn npcId=\"" + rset.getInt("npcId") + "\" x=\"" + rset.getInt("x") + "\" y=\"" +
							rset.getInt("y") + "\" z=\"" + rset.getInt("z") + "\" heading=\"" + rset.getInt("heading") +
							"\" respawn=\"60\" />\r\n";
				}

				rset.close();
				statement.close();
				content += "\t</specificSpawnList>\r\n";

				content += "\t<specificSpawnList name=\"" + fort.getName() + "_defending_commanders\">\r\n";
				for (int i = 1; i < 5; i++)
				{
					String _spawnParams =
							siegeSettings.getProperty(fort.getName().replace(" ", "") + "Commander" + i, "");
					if (_spawnParams.length() == 0)
					{
						break;
					}

					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					int x = Integer.parseInt(st.nextToken());
					int y = Integer.parseInt(st.nextToken());
					int z = Integer.parseInt(st.nextToken());
					int heading = Integer.parseInt(st.nextToken());
					int npcId = Integer.parseInt(st.nextToken());

					content += "\t\t<spawn npcId=\"" + npcId + "\" x=\"" + x + "\" y=\"" + y + "\" z=\"" + z +
							"\" heading=\"" + heading + "\" respawn=\"60\" />\r\n";
				}
				content += "\t</specificSpawnList>\r\n";

				content += "\t<specificSpawnList name=\"" + fort.getName() + "_siege_guards\">\r\n";
				statement = con.prepareStatement("SELECT * FROM fort_siege_guards WHERE fortId = ?");
				statement.setInt(1, fort.getFortId());
				rset = statement.executeQuery();
				while (rset.next())
				{
					content += "\t\t<spawn npcId=\"" + rset.getInt("npcId") + "\" x=\"" + rset.getInt("x") + "\" y=\"" +
							rset.getInt("y") + "\" z=\"" + rset.getInt("z") + "\" heading=\"" + rset.getInt("heading") +
							"\" respawn=\"" + rset.getInt("respawnDelay") + "\" />\r\n";
				}

				rset.close();
				statement.close();
				content += "\t</specificSpawnList>\r\n";

				content += "\t<specificSpawnList name=\"" + fort.getName() + "_envoys\">\r\n";
				statement = con.prepareStatement("SELECT * FROM fort_spawnlist WHERE fortId = ? AND spawnType = 3");
				statement.setInt(1, fort.getFortId());
				rset = statement.executeQuery();
				while (rset.next())
				{
					content += "\t\t<spawn npcId=\"" + rset.getInt("npcId") + "\" x=\"" + rset.getInt("x") + "\" y=\"" +
							rset.getInt("y") + "\" z=\"" + rset.getInt("z") + "\" heading=\"" + rset.getInt("heading") +
							"\" respawn=\"60\" />\r\n";
				}

				rset.close();
				statement.close();
				content += "\t</specificSpawnList>\r\n";
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		Util.writeFile("./data/spawns/forts.xml", content);
	}
}
