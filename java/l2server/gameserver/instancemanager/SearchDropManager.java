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

import l2server.gameserver.communitybbs.Manager.CustomCommunityBoard;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.NpcTable.DropChances;
import l2server.gameserver.model.L2DropCategory;
import l2server.gameserver.model.L2DropData;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author LasTravel
 */

public class SearchDropManager
{
	private static Map<Integer, Drops> _allDrops = new HashMap<>();

	private class Drops
	{
		private int _itemId;
		private String _iemName;
		private String _itemIcon;
		private List<L2NpcTemplate> _droppedBy = new ArrayList<>();
		private List<L2NpcTemplate> _spoilBy = new ArrayList<>();

		private Drops(int itemId, L2NpcTemplate npc, boolean isSpoil)
		{
			_itemId = itemId;
			L2Item temp = ItemTable.getInstance().getTemplate(itemId);
			_iemName = temp.getName();
			_itemIcon = temp.getIcon();
			if (isSpoil)
			{
				_spoilBy.add(npc);
			}
			else
			{
				_droppedBy.add(npc);
			}
		}

		private List<L2NpcTemplate> getDroppedBy(boolean isSpoil)
		{
			if (isSpoil)
			{
				return _spoilBy;
			}
			return _droppedBy;
		}

		private int getItemId()
		{
			return _itemId;
		}

		private String getName()
		{
			return _iemName;
		}

		private String getIcon()
		{
			return _itemIcon;
		}

		private void addMonster(L2NpcTemplate t, boolean isSpoil)
		{
			if (isSpoil)
			{
				_spoilBy.add(t);
			}
			else
			{
				_droppedBy.add(t);
			}
		}
	}

	private List<Drops> getPossibleDropItem(String name)
	{
		List<Drops> toReturn = new ArrayList<>();
		List<String> itemNames = new ArrayList<>();
		for (Entry<Integer, Drops> i : _allDrops.entrySet())
		{
			Drops d = i.getValue();
			if (d.getName().toLowerCase().trim().contains(name.toLowerCase()))
			{
				if (itemNames.contains(d.getName()))
				{
					continue;
				}
				itemNames.add(d.getName());
				toReturn.add(d);
			}
		}
		return toReturn;
	}

	public void addLootInfo(L2NpcTemplate temp, boolean overrideDrops)
	{
		if (temp == null)
		{
			return;
		}

		if (temp.Type.equalsIgnoreCase("L2Npc"))
		{
			return;
		}

		if (temp.Type.equalsIgnoreCase("L2Monster") || temp.Type.equalsIgnoreCase("L2FeedableBeast"))
		{
			if (temp.getAllSpawns().isEmpty())
			{
				return;
			}
		}

		if (overrideDrops)
		{
			overrideDrops(temp);
		}

		Map<L2DropData, Float> drops = new HashMap<>();
		Map<L2DropData, Float> spoilDrop = new HashMap<>();

		if (!temp.getMultiDropData().isEmpty())
		{
			for (L2DropCategory catDrop : temp.getMultiDropData())
			{
				if (catDrop == null)
				{
					continue;
				}

				if (catDrop.getAllDrops() != null)
				{
					for (L2DropData drop : catDrop.getAllDrops())
					{
						drops.put(drop, catDrop.getChance());
					}
				}
			}
		}

		if (!temp.getDropData().isEmpty())
		{
			for (L2DropData drop : temp.getDropData())
			{
				drops.put(drop, 100.0f);
			}
		}

		if (!temp.getSpoilData().isEmpty())
		{
			for (L2DropData drop : temp.getSpoilData())
			{
				spoilDrop.put(drop, 100.0f);
			}
		}

		for (Entry<L2DropData, Float> i : drops.entrySet())
		{
			L2DropData data = i.getKey();
			if (_allDrops.containsKey(data.getItemId()))
			{
				_allDrops.get(data.getItemId()).addMonster(temp, false);
			}
			else
			{
				Drops d = new Drops(data.getItemId(), temp, false);
				_allDrops.put(data.getItemId(), d);
			}
		}

		for (Entry<L2DropData, Float> i : spoilDrop.entrySet())
		{
			L2DropData data = i.getKey();
			if (_allDrops.containsKey(data.getItemId()))
			{
				_allDrops.get(data.getItemId()).addMonster(temp, true);
			}
			else
			{
				Drops d = new Drops(data.getItemId(), temp, true);
				_allDrops.put(data.getItemId(), d);
			}
		}
	}

	public String getDrops(L2PcInstance player, int itemId, boolean isSpoil, int pageToShow)
	{
		Drops i = _allDrops.get(itemId);
		if (i == null)
		{
			return "ERROR";
		}

		String spoil = isSpoil ? "spoil" : "normal";

		StringBuilder sb = new StringBuilder();

		int maxMobsPerPage = 20;
		int mobsSize = i.getDroppedBy(isSpoil).size();
		int maxPages = mobsSize / maxMobsPerPage;
		if (maxPages > 20)
		{
			maxPages = 20;
		}
		if (mobsSize > maxMobsPerPage * maxPages)
		{
			maxPages++;
		}
		if (pageToShow > maxPages)
		{
			pageToShow = maxPages;
		}
		int pageStart = maxMobsPerPage * pageToShow;
		int pageEnd = mobsSize;
		if (pageEnd - pageStart > maxMobsPerPage)
		{
			pageEnd = pageStart + maxMobsPerPage;
		}

		sb.append(maxPages > 1 ? CustomCommunityBoard.getInstance()
				.createPages(pageToShow, maxPages, "_bbscustom;action;searchDrop;" + itemId + ";" + spoil + ";", ";") :
				"");
		sb.append("<br>");
		sb.append("<table width=600>");
		sb.append("<tr><td>Monster Name</td><td>Level</td><td>Chance</td><td>Count</td></tr>");

		int playerLevel = player.getLevel();
		for (int b = pageStart; b < pageEnd; b++)
		{
			L2NpcTemplate temp = i.getDroppedBy(isSpoil).get(b);
			if (temp == null)
			{
				continue;
			}

			DropChances drop = getDropChance(temp, player, i.getItemId(), isSpoil);
			if (drop == null)
			{
				continue;
			}

			String count = " (" + drop.min + ")";
			if (drop.max > drop.min)
			{
				count = " (" + drop.min + "-" + drop.max + ")";
			}

			String lineColor = "";
			if (playerLevel < temp.Level + 8 && playerLevel > temp.Level - 8)
			{
				lineColor = "<font color=LEVEL>";
			}

			String radar = "";
			if (!temp.getAllSpawns().isEmpty())
			{
				L2Spawn spawn = temp.getAllSpawns().get(0);
				if (spawn != null)
				{
					radar = "<a action=\"bypass " +
							(player.isGM() ? " -h admin_move_to " : "_bbscustom;action;showRadar; ") + spawn.getX() +
							" " + spawn.getY() + " " + spawn.getZ() + "\">";
				}
			}

			sb.append("<tr><td> " + radar + " " + lineColor + "" + temp.getName() + (radar.length() > 0 ? "</a>" : "") +
					"</td><td>" + temp.Level + "</td><td>" + Util.roundTo(drop.chance, 3) + "%</td><td>" + count + "" +
					(lineColor.length() > 0 ? "</font>" : "") + "</td></tr>");
		}

		sb.append("</table>");
		return sb.toString();
	}

	public String searchPossiblesResults(L2PcInstance player, String itemName, boolean isSpoil)
	{
		List<Drops> drops = getPossibleDropItem(itemName);
		if (drops.isEmpty())
		{
			return "<font color=LEVEL>Wops.. We can't find that item... Try with other name!</font>";
		}

		String spoil = isSpoil ? "spoil" : "normal";

		StringBuilder sb = new StringBuilder();
		sb.append("<img src=l2ui.squaregray width=600 height=1>");
		sb.append("<center>Click on the item are you looking for:</center>");
		sb.append("<img src=l2ui.squaregray width=600 height=1><br>");
		sb.append("<table width=600>");
		for (Drops d : drops)
		{
			sb.append("<tr><td><img src=" + d.getIcon() +
					" width=32 height=32></td><td width=568><a action=\"bypass _bbscustom;action;searchDrop;" +
					d.getItemId() + ";" + spoil + ";0\">" + d.getName() + "</a></td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private DropChances getDropChance(L2NpcTemplate temp, L2PcInstance pl, int itemId, boolean isSpoil)
	{
		if (isSpoil)
		{
			if (!temp.getSpoilData().isEmpty())
			{
				for (L2DropData drop : temp.getSpoilData())
				{
					if (drop.getItemId() == itemId)
					{
						return NpcTable.getInstance().calculateRewardChances(temp, pl, drop, 100.0f, 0, false, null);
					}
				}
			}
		}

		if (!temp.getDropData().isEmpty())
		{
			for (L2DropData drop : temp.getDropData())
			{
				if (drop.getItemId() == itemId)
				{
					return NpcTable.getInstance().calculateRewardChances(temp, pl, drop, 100.0f, 0, false, null);
				}
			}
		}

		if (!temp.getMultiDropData().isEmpty())
		{
			for (L2DropCategory catDrop : temp.getMultiDropData())
			{
				for (L2DropData drop : catDrop.getAllDrops())
				{
					if (drop.getItemId() == itemId)
					{
						return NpcTable.getInstance()
								.calculateRewardChances(temp, pl, drop, catDrop.getChance(), 0, false, null);
					}
				}
			}
		}
		return null;
	}

	public void overrideDrops(L2NpcTemplate temp)
	{
		for (Entry<Integer, Drops> i : _allDrops.entrySet())
		{
			Drops d = i.getValue();
			List<L2NpcTemplate> dropped = new ArrayList<>(d.getDroppedBy(false));
			for (L2NpcTemplate b : dropped)
			{
				if (b.NpcId == temp.NpcId)
				{
					d.getDroppedBy(false).remove(b);
				}
			}

			dropped = new ArrayList<>(d.getDroppedBy(true));
			for (L2NpcTemplate b : dropped)
			{
				if (b.NpcId == temp.NpcId)
				{
					d.getDroppedBy(true).remove(b);
				}
			}
		}
	}

	public static SearchDropManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SearchDropManager _instance = new SearchDropManager();
	}
}
