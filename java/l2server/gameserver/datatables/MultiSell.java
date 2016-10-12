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
import l2server.gameserver.instancemanager.RaidBossPointsManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.multisell.Ingredient;
import l2server.gameserver.model.multisell.ListContainer;
import l2server.gameserver.model.multisell.MultiSellEntry;
import l2server.gameserver.model.multisell.PreparedListContainer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MultiSellList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MultiSell implements Reloadable
{
	public static final int PAGE_SIZE = 40;

	public static final int PC_BANG_POINTS = -100;
	public static final int CLAN_REPUTATION = -200;
	public static final int FAME = -300;
	public static final int RAID_POINTS = -500;

	private final Map<String, ListContainer> _entries = new HashMap<>();
	private int _nextId = 1;

	public static MultiSell getInstance()
	{
		return SingletonHolder._instance;
	}

	private MultiSell()
	{
		load();

		ReloadableManager.getInstance().register("multisell", this);
	}

	@Override
	public final boolean reload()
	{
		_entries.clear();
		load();

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "All Multisells have been reloaded";
	}

	/**
	 * This will generate the multisell list for the items.  There exist various
	 * parameters in multisells that affect the way they will appear:
	 * 1) inventory only:
	 * * if true, only show items of the multisell for which the
	 * "primary" ingredients are already in the player's inventory.  By "primary"
	 * ingredients we mean weapon and armor.
	 * * if false, show the entire list.
	 * 2) maintain enchantment: presumably, only lists with "inventory only" set to true
	 * should sometimes have this as true.  This makes no sense otherwise...
	 * * If true, then the product will match the enchantment level of the ingredient.
	 * if the player has multiple items that match the ingredient list but the enchantment
	 * levels differ, then the entries need to be duplicated to show the products and
	 * ingredients for each enchantment level.
	 * For example: If the player has a crystal staff +1 and a crystal staff +3 and goes
	 * to exchange it at the mammon, the list should have all exchange possibilities for
	 * the +1 staff, followed by all possibilities for the +3 staff.
	 * * If false, then any level ingredient will be considered equal and product will always
	 * be at +0
	 * 3) apply taxes: Uses the "taxIngredient" entry in order to add a certain amount of adena to the ingredients
	 */
	public final void separateAndSend(String listName, L2PcInstance player, L2Npc npc, boolean inventoryOnly)
	{
		ListContainer template = _entries.get(listName);
		if (template == null)
		{
			Log.warning("[MultiSell] can't find list: " + listName + " requested by player: " + player.getName() +
					", npcId:" + (npc != null ? npc.getNpcId() : 0));
			return;
		}

		final PreparedListContainer list = new PreparedListContainer(template, inventoryOnly, player, npc);
		int index = 0;
		do
		{
			// send list at least once even if size = 0
			player.sendPacket(new MultiSellList(list, index));
			index += PAGE_SIZE;
		}
		while (index < list.getEntries().size());

		player.setMultiSell(list);
	}

	public static boolean checkSpecialIngredient(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
					break;
				}
				if (!player.isClanLeader())
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED));
					break;
				}
				if (player.getClan().getReputationScore() < amount)
				{
					player.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
					break;
				}
				return true;
			case FAME:
				if (player.getFame() < amount)
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_FAME_POINTS));
					break;
				}
				return true;
			case RAID_POINTS:
				if (RaidBossPointsManager.getInstance().getPointsByOwnerId(player.getObjectId()) < amount)
				{
					player.sendMessage("Not enough Raid Points");
					break;
				}
				return true;
		}
		return false;
	}

	public static boolean getSpecialIngredient(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
				// Tenkai custom - Only the clan leader can shop for clan reputation (to avoid abuse)
				if (player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
				{
					player.sendMessage("This item can only be acquired by a clan's leader.");
					return false;
				}

				player.getClan().takeReputationScore((int) amount, true);
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
				smsg.addItemNumber(amount);
				player.sendPacket(smsg);
				return true;
			case FAME:
				player.setFame(player.getFame() - (int) amount);
				player.sendPacket(new UserInfo(player));
				return true;
			case RAID_POINTS:
				Map<Integer, Integer> points = RaidBossPointsManager.getInstance().getList(player);
				points.put(0, points.get(0) - (int) amount);
				RaidBossPointsManager.getInstance().updatePointsInDB(player, 0, points.get(0));
				player.sendPacket(new UserInfo(player));
				return true;
		}
		return false;
	}

	public static void addSpecialProduct(int id, long amount, L2PcInstance player)
	{
		switch (id)
		{
			case CLAN_REPUTATION:
				player.getClan().addReputationScore((int) amount, true);
				break;
			case FAME:
				player.setFame((int) (player.getFame() + amount));
				player.sendPacket(new UserInfo(player));
				break;
			case RAID_POINTS:
				Map<Integer, Integer> points = RaidBossPointsManager.getInstance().getList(player);
				points.put(0, points.get(0) + (int) amount);
				RaidBossPointsManager.getInstance().updatePointsInDB(player, 0, points.get(0));
				player.sendPacket(new UserInfo(player));
				break;
		}
	}

	private void load()
	{
		List<File> files = new ArrayList<>();

		if (!Config.SERVER_NAME.isEmpty())
		{
			hashFiles(Config.DATAPACK_ROOT + "/data_" + Config.SERVER_NAME + "/multisell", files);
		}
		hashFiles(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "multisell", files);

		for (File f : files)
		{
			String name = f.getName().replaceAll(".xml", "");
			XmlDocument doc = new XmlDocument(f);

			// Already got custom data, skipping default...
			if (_entries.containsKey(name))
			{
				//Log.log(Level.WARNING, "Already got custom data for Multisell[" + id + "], skipping default...");
				continue;
			}

			try
			{
				ListContainer list = parseDocument(doc);
				list.setListId(_nextId++);
				_entries.put(name, list);

				if (name.equals("app_stones"))
				{
					long total = 0;
					for (MultiSellEntry entry : list.getEntries())
					{
						if (entry.getIngredients().get(0).getItemId() == 4037)
						{
							total += entry.getIngredients().get(0).getItemCount();
						}
					}

                    /*for (MultiSellEntry entry : list.getEntries())
					{
                        if (entry.getIngredients().get(0).getItemId() != 4037)
                        {
                            continue;
                        }

                        int hatId = entry.getProducts().get(0).getItemId();
                        long price = entry.getIngredients().get(0).getItemCount();
                        System.out.println("<item id=\"" + hatId + " min=\"1\" max=\"1\" chance=\"" +
                                10000 * price / total / 100.0 + "\" /> <!-- " +
                                ItemTable.getInstance().getTemplate(hatId).getName() + " -->");
                    }*/
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "Error in file " + f, e);
			}
		}
		verify();
		Log.info("MultiSell: Loaded " + _entries.size() + " lists.");
	}

	private ListContainer parseDocument(XmlDocument doc)
	{
		int entryId = 1;
		ListContainer list = new ListContainer();

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				list.setApplyTaxes(n.getBool("applyTaxes", false));
				list.setMaintainEnchantment(n.getBool("maintainEnchantment", false));
				list.setIsChance(n.getBool("isChance", false));
				list.setTimeLimit(n.getInt("timeLimit", 0));

				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("item"))
					{
						MultiSellEntry e = parseEntry(d, entryId++);
						list.getEntries().add(e);
					}
				}
			}
			else if (n.getName().equalsIgnoreCase("item"))
			{
				MultiSellEntry e = parseEntry(n, entryId++);
				list.getEntries().add(e);
			}
		}

		return list;
	}

	private MultiSellEntry parseEntry(XmlNode node, int entryId)
	{
		final MultiSellEntry entry = new MultiSellEntry(entryId);

		for (XmlNode n : node.getChildren())
		{
			if (n.getName().equalsIgnoreCase("ingredient"))
			{
				int id = n.getInt("id");
				long count = n.getLong("count");
				boolean isTaxIngredient = n.getBool("isTaxIngredient", false);
				boolean maintainIngredient = n.getBool("maintainIngredient", false);

				entry.addIngredient(new Ingredient(id, count, isTaxIngredient, maintainIngredient));
			}
			else if (n.getName().equalsIgnoreCase("production"))
			{
				int id = n.getInt("id");
				long count = n.getLong("count");

				Ingredient product = new Ingredient(id, count, false, false);
				entry.addProduct(product);

				if (n.hasAttribute("chance"))
				{
					product.setChance(n.getFloat("chance"));
				}
			}
		}

		return entry;
	}

	private void hashFiles(String directoryPath, List<File> hash)
	{
		File dir = new File(directoryPath);
		if (!dir.exists())
		{
			Log.warning("Dir " + dir.getAbsolutePath() + " does not exist");
			return;
		}

		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml"))
			{
				hash.add(f);
			}
		}
	}

	private void verify()
	{
		for (ListContainer list : _entries.values())
		{
			for (MultiSellEntry ent : list.getEntries())
			{
				for (Ingredient ing : ent.getIngredients())
				{
					if (!verifyIngredient(ing))
					{
						Log.warning("[MultiSell] can't find ingredient with itemId: " + ing.getItemId() + " in list: " +
								list.getListId());
					}
				}
				for (Ingredient ing : ent.getProducts())
				{
					if (!verifyIngredient(ing))
					{
						Log.warning("[MultiSell] can't find product with itemId: " + ing.getItemId() + " in list: " +
								list.getListId());
					}
				}
			}
		}
	}

	private boolean verifyIngredient(Ingredient ing)
	{
		switch (ing.getItemId())
		{
			case CLAN_REPUTATION:
			case FAME:
			case RAID_POINTS:
				return true;
			default:
				if (ing.getTemplate() != null)
				{
					return true;
				}
		}

		return false;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MultiSell _instance = new MultiSell();
	}
}
