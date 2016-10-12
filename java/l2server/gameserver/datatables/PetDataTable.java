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
import l2server.gameserver.model.L2PetData;
import l2server.gameserver.model.L2PetLevelData;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

public class PetDataTable
{

	private static TIntObjectHashMap<L2PetData> _petTable;

	public static PetDataTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private PetDataTable()
	{
		_petTable = new TIntObjectHashMap<>();
		load();
	}

	public void load()
	{
		_petTable.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "PetData.xml");
		if (file.exists())
		{
			XmlDocument doc = new XmlDocument(file);

			XmlNode n = doc.getFirstChild();
			for (XmlNode d : n.getChildren())
			{
				if (d.getName().equals("pet"))
				{
					int npcId = d.getInt("id");
					//index ignored for now
					L2PetData data = new L2PetData();
					for (XmlNode p : d.getChildren())
					{
						switch (p.getName())
						{
							case "set":
							{
								String type = p.getString("name");
								if ("food".equals(type))
								{
									String[] values = p.getString("val").split(";");
									int[] food = new int[values.length];
									for (int i = 0; i < values.length; i++)
									{
										food[i] = Integer.parseInt(values[i]);
									}
									data.set_food(food);
								}
								else if ("load".equals(type))
								{
									data.set_load(p.getInt("val"));
								}
								else if ("hungry_limit".equals(type))
								{
									data.set_hungry_limit(p.getInt("val"));
								}
								//sync_level and evolve ignored
								break;
							}
							case "skills":
								for (XmlNode s : p.getChildren())
								{
									if (s.getName().equals("skill"))
									{
										int skillId = s.getInt("skillId");
										int skillLvl = s.getInt("skillLvl");
										int minLvl = s.getInt("minLvl");
										data.addNewSkill(skillId, skillLvl, minLvl);
									}
								}
								break;
							case "stats":
								for (XmlNode s : p.getChildren())
								{
									if (s.getName().equals("stat"))
									{
										int level = s.getInt("level");
										L2PetLevelData stat = new L2PetLevelData();
										for (XmlNode bean : s.getChildren())
										{
											if (bean.getName().equals("set"))
											{
												String type = bean.getString("name");
												String value = bean.getString("val");
												if ("exp".equals(type))
												{
													stat.setPetMaxExp(Long.parseLong(value));
												}
												else if ("get_exp_type".equals(type))
												{
													stat.setOwnerExpTaken(Integer.parseInt(value));
												}
												else if ("consume_meal_in_battle".equals(type))
												{
													stat.setPetFeedBattle(Integer.parseInt(value));
												}
												else if ("consume_meal_in_normal".equals(type))
												{
													stat.setPetFeedNormal(Integer.parseInt(value));
												}
												else if ("max_meal".equals(type))
												{
													stat.setPetMaxFeed(Integer.parseInt(value));
												}
												else if ("soulshot_count".equals(type))
												{
													stat.setPetSoulShot((short) Integer.parseInt(value));
												}
												else if ("spiritshot_count".equals(type))
												{
													stat.setPetSpiritShot((short) Integer.parseInt(value));
												}
												else if ("hp".equals(type))
												{
													stat.setPetMaxHP(Integer.parseInt(value));
												}
												else if ("mp".equals(type))
												{
													stat.setPetMaxMP(Integer.parseInt(value));
												}
												else if ("pdef".equals(type))
												{
													stat.setPetPDef(Integer.parseInt(value));
												}
												else if ("mdef".equals(type))
												{
													stat.setPetMDef(Integer.parseInt(value));
												}
												else if ("patk".equals(type))
												{
													stat.setPetPAtk(Integer.parseInt(value));
												}
												else if ("matk".equals(type))
												{
													stat.setPetMAtk(Integer.parseInt(value));
												}
												else if ("hpreg".equals(type))
												{
													stat.setPetRegenHP(Integer.parseInt(value));
												}
												else if ("mpreg".equals(type))
												{
													stat.setPetRegenMP(Integer.parseInt(value));
												}
											}
										}
										data.addNewStat(stat, level);
									}
								}
								break;
						}
					}
					_petTable.put(npcId, data);
				}
			}
		}
		else
		{
			Log.warning("Not found PetData.xml");
		}

		Log.info(getClass().getSimpleName() + ": Loaded " + _petTable.size() + " Pets.");
	}

	public L2PetLevelData getPetLevelData(int petID, int petLevel)
	{
		return _petTable.get(petID).getPetLevelData(petLevel);
	}

	public L2PetData getPetData(int petID)
	{
		if (!_petTable.contains(petID))
		{
			Log.info("Missing pet data for npcid: " + petID);
		}
		return _petTable.get(petID);
	}

	public int getPetMinLevel(int petID)
	{
		return _petTable.get(petID).getMinLevel();
	}

	/*
	 * Pets stuffs
	 */
	public static boolean isWolf(int npcId)
	{
		return npcId == 12077;
	}

	public static boolean isEvolvedWolf(int npcId)
	{
		return npcId == 16030 || npcId == 16037 || npcId == 16025 || npcId == 16041 || npcId == 16042;
	}

	public static boolean isSinEater(int npcId)
	{
		return npcId == 12564;
	}

	public static boolean isHatchling(int npcId)
	{
		return npcId > 12310 && npcId < 12314;
	}

	public static boolean isStrider(int npcId)
	{
		return npcId > 12525 && npcId < 12529 || npcId > 16037 && npcId < 16041 || npcId == 16068;
	}

	public static boolean isWyvern(int npcId)
	{
		return npcId == 12621;
	}

	public static boolean isBaby(int npcId)
	{
		return npcId > 12779 && npcId < 12783;
	}

	public static boolean isImprovedBaby(int npcId)
	{
		return npcId > 16033 && npcId < 16037;
	}

	public static boolean isPetFood(int itemId)
	{
		switch (itemId)
		{
			case 2515:
			case 4038:
			case 5168:
			case 5169:
			case 6316:
			case 7582:
			case 9668:
			case 10425:
				return true;
			default:
				return false;
		}
	}

	/**
	 * @see L2PetData#getFood()
	 */
	@Deprecated
	public static int[] getFoodItemId(int npcId)
	{
		switch (npcId)
		{
			case 12077:// Wolf
			case 12564://Sin Eater
				return new int[]{2515};

			case 16030:// Great Wolf
			case 16025:// Black Wolf
			case 16037:// White Great Wolf
			case 16041:// Fenrir
			case 16042:// White Fenrir
				return new int[]{9668};

			case 12311:// hatchling of wind
			case 12312:// hatchling of star
			case 12313:// hatchling of twilight
				return new int[]{4038};

			case 12526:// wind strider
			case 12527:// Star strider
			case 12528:// Twilight strider
			case 16038:// red wind strider
			case 16039:// red Star strider
			case 16040:// red Twilight strider
			case 16068:// Guardian Strider
				return new int[]{5168, 5169};

			case 12621: // wyvern
				return new int[]{6316};

			case 12780:// Baby Buffalo
			case 12782:// Baby Cougar
			case 12781:// Baby Kookaburra
				return new int[]{7582};

			case 16034:// Improved Baby Buffalo
			case 16036:// Improved Baby Cougar
			case 16035:// Improved Baby Kookaburra
				return new int[]{10425};

			default:
				return new int[]{0};
		}
	}

	public static boolean isPetItem(int itemId)
	{
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		return item != null && item.getItemType() == L2EtcItemType.PET_COLLAR;

		/*switch (itemId)
		{
			case 2375: // Wolf
			case 3500: // hatchling of wind
			case 3501: // hatchling of star
			case 3502: // hatchling of twilight
			case 4422: // strider of wind
			case 4423: // strider of star
			case 4424: // strider of dusk
			case 4425: // Sin Eater
			case 6648: // baby buffalo
			case 6649: // baby cougar
			case 6650: // baby kookaburra
			case 8663: // Wyvern
			case 9882: // Great Wolf
			case 10163: // Black Wolf
			case 10307: // Great Snow Wolf
			case 10308: // red strider of wind
			case 10309: // red strider of star
			case 10310: // red strider of dusk
			case 10311: // improved buffalo
			case 10312: // improved cougar
			case 10313: // improved kookaburra
			case 10426: // Fenrir
			case 10611: // White Fenrir
			case 14819: // Guardian Strider
				return true;
			default:
				return false;
		}*/
	}

	public static int[] getPetItemsByNpc(int npcId)
	{
		switch (npcId)
		{
			case 12077:// Wolf
				return new int[]{2375};
			case 16025:// Great Wolf
				return new int[]{9882};
			case 16030:// Black Wolf
				return new int[]{10163};
			case 16037:// White Great Wolf
				return new int[]{10307};
			case 16041:// Fenrir
				return new int[]{10426};
			case 16042:// White Fenrir
				return new int[]{10611};
			case 12564://Sin Eater
				return new int[]{4425};

			case 12311:// hatchling of wind
			case 12312:// hatchling of star
			case 12313:// hatchling of twilight
				return new int[]{3500, 3501, 3502};

			case 12526:// wind strider
			case 12527:// Star strider
			case 12528:// Twilight strider
			case 16038: // red strider of wind
			case 16039: // red strider of star
			case 16040: // red strider of dusk
			case 16068: // Guardian Strider
				return new int[]{4422, 4423, 4424, 10308, 10309, 10310, 14819};

			case 12621:// Wyvern
				return new int[]{8663};

			case 12780:// Baby Buffalo
			case 12782:// Baby Cougar
			case 12781:// Baby Kookaburra
				return new int[]{6648, 6649, 6650};

			case 16034:// Improved Baby Buffalo
			case 16036:// Improved Baby Cougar
			case 16035:// Improved Baby Kookaburra
				return new int[]{10311, 10312, 10313};

			// unknown item id.. should never happen
			default:
				return new int[]{0};
		}
	}

	public static boolean isMountable(int npcId)
	{
		return npcId == 12526 || npcId == 12527 || npcId == 12528 || npcId == 12621 || npcId == 16037 ||
				npcId == 16041 || npcId == 16042 || npcId == 16038 || npcId == 16039 || npcId == 16040 ||
				npcId == 16068; // Guardian Strider
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PetDataTable _instance = new PetDataTable();
	}

	public static void main(String... s)
	{
		getInstance();
	}
}
