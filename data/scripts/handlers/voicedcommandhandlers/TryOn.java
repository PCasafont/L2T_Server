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

package handlers.voicedcommandhandlers;

import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.templates.item.L2EtcItem;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */

public class TryOn implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = {"tryon"};

	private static final int ITEMS_PER_PAGE = 6;

	/**
	 * @see l2server.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, l2server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance player, String params)
	{
		if (!command.equalsIgnoreCase("tryon"))
		{
			return false;
		}

		if (ThreadPoolManager.getInstance().isShutdown())
		{
			return false;
		}

		if (!player.isGM() && !player.isInsideZone(L2Character.ZONE_TOWN))
		{
			player.sendMessage("You can't try on apps outside of town!");
			return false;
		}

		if (params == null)
		{
			params = "";
		}

		String search = "";
		if (params.contains("search "))
		{
			int searchStartIndex = params.indexOf("search ") + 7;
			int searchEndIndex = params.indexOf(" ", searchStartIndex);
			if (searchEndIndex > 0)
			{
				search = params.substring(searchStartIndex, searchEndIndex).toLowerCase();
			}
			else
			{
				search = params.substring(searchStartIndex).toLowerCase();
			}

			if (search.startsWith("page="))
			{
				search = "";
			}
		}

		int page = 1;
		if (params.contains("page="))
		{
			int pageStartIndex = params.indexOf("page=") + 5;
			int pageEndIndex = params.indexOf(" ", pageStartIndex);
			if (pageEndIndex > 0)
			{
				page = Integer.valueOf(params.substring(pageStartIndex, pageEndIndex));
			}
			else
			{
				page = Integer.valueOf(params.substring(pageStartIndex));
			}
		}

		if (params.contains("item="))
		{
			int itemId;
			int itemStartIndex = params.indexOf("item=") + 5;
			int itemEndIndex = params.indexOf(" ", itemStartIndex);
			if (itemEndIndex > 0)
			{
				itemId = Integer.valueOf(params.substring(itemStartIndex, itemEndIndex));
			}
			else
			{
				itemId = Integer.valueOf(params.substring(itemStartIndex));
			}

			player.tryOn(itemId);
		}

		//Html page section
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><title>Try On</title>");

		if (search.isEmpty())
		{
			sb.append("<center><table width=300 bgcolor=666666><tr><td align=center>Search App:</td></tr");
			sb.append("<tr><td align=center><edit var=\"what\" width=150 type=char length=16></td></tr>");
			sb.append("<tr><td align=center><button action=\"bypass -h voice .tryon search $what page=" + page +
					"\" value=Search! width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
		}
		else
		{
			sb.append("<center><tr><td align=center>Searching for <font color=LEVEL>" + search + "</font></td></tr>");
			sb.append("<tr><td align=center><button action=\"bypass -h voice .tryon page=" + page +
					"\" value=\"Remove Search\" width=120 height=20 back=L2UI_ct1.button_df fore=L2UI_ct1.button_df></td></tr>");
		}
		sb.append("</table></center><br>");

		List<L2Item> items = getItems(search);
		int totalCount = items.size();
		int pageCount = (int) Math.ceil(totalCount / (float) ITEMS_PER_PAGE);
		if (page > pageCount)
		{
			page = 1;
		}

		items = getPage(items, page);

		String bypass = "bypass -h voice .tryon search " + search + " page=" + page;
		String[] colors = new String[]{"444444", "555555"};

		//Current items
		sb.append("<br>");

		sb.append("<table width=300>");

		int index = 0;
		for (L2Item item : items)
		{
			sb.append("<tr>");
			sb.append("<td width=300>");

			String itemName = item.getName();
			sb.append("<table width=300 bgcolor=" + colors[index % 2] + "><tr>");
			sb.append("<td FIXWIDTH=30><button action=\"" + bypass + " item=" + item.getItemId() +
					"\" value=\" \" width=32 height=32 back=\"" + item.getIcon() + "\" fore=\"" + item.getIcon() +
					"\"></td>");
			sb.append("<td FIXWIDTH=180>" + itemName + "</td><td FIXWIDTH=20></td>");
			sb.append("</tr></table>");

			sb.append("</td>");
			sb.append("</tr>");
			index++;
		}

		int pageLinks = 13;
		int minPage = Math.max(1, Math.min(pageCount - (pageLinks - 1), page - (pageLinks - 1) / 2));
		int maxPage = Math.min(pageCount, minPage + pageLinks - 1);

		bypass = "bypass -h voice .tryon search " + search;

		pageLinks = maxPage - minPage + 1;

		sb.append("</table>");

		sb.append("<br><br>");

		if (pageLinks > 1)
		{
			sb.append("<center><table width=" + pageLinks * 20 + "><tr>");
			for (int p = minPage; p <= maxPage; p++)
			{
				if (p == page)
				{
					sb.append("<td FIXWIDTH=20>" + p + "</td>");
				}
				else if (p > 1 && p == minPage || p < pageCount && p == maxPage)
				{
					sb.append("<td FIXWIDTH=20><a action=\"" + bypass + " page=" + p + "\">...</a></td>");
				}
				else
				{
					sb.append("<td FIXWIDTH=20><a action=\"" + bypass + " page=" + p + "\">" + p + "</a></td>");
				}
			}

			sb.append("</tr></table></center>");
		}

		sb.append("</body></html>");

		player.sendPacket(new NpcHtmlMessage(0, sb.toString()));

		return true;
	}

	private List<L2Item> getItems(String search)
	{
		List<L2Item> items = new ArrayList<L2Item>();
		for (L2Item it : ItemTable.getInstance().getAllItems())
		{
			L2Item item = null;
			if (it instanceof L2Weapon)
			{
				switch (it.getItemId())
				{
					case 36417: // Antharas Shaper - Fragment
					case 36441: // Antharas Shaper - Standard
					case 36465: // Antharas Shaper - High-grade
					case 36489: // Antharas Shaper - Top-grade

					case 36418: // Antharas Slasher - Fragment
					case 36442: // Antharas Slasher - Standard
					case 36466: // Antharas Slasher - High-grade
					case 36490: // Antharas Slasher - Top-grade

					case 36419: // Antharas Thrower - Fragment
					case 36443: // Antharas Thrower - Standard
					case 36467: // Antharas Thrower - High-grade
					case 36491: // Antharas Thrower - Top-grade

					case 36420: // Antharas Buster - Fragment
					case 36444: // Antharas Buster - Standard
					case 36468: // Antharas Buster - High-grade
					case 36492: // Antharas Buster - Top-grade

					case 36421: // Antharas Cutter - Fragment
					case 36445: // Antharas Cutter - Standard
					case 36469: // Antharas Cutter - High-grade
					case 36493: // Antharas Cutter - Top-grade

					case 36422: // Antharas Stormer - Fragment
					case 36446: // Antharas Stormer - Standard
					case 36470: // Antharas Stormer - High-grade
					case 36494: // Antharas Stormer - Top-grade

					case 36423: // Antharas Fighter - Fragment
					case 36447: // Antharas Fighter - Standard
					case 36471: // Antharas Fighter - High-grade
					case 36495: // Antharas Fighter - Top-grade

					case 36424: // Antharas Avenger - Fragment
					case 36448: // Antharas Avenger - Standard
					case 36472: // Antharas Avenger - High-grade
					case 36496: // Antharas Avenger - Top-grade

					case 36425: // Antharas Dual Blunt Weapon - Fragment
					case 36449: // Antharas Dual Blunt Weapon - Standard
					case 36473: // Antharas Dual Blunt Weapon - High-grade
					case 36497: // Antharas Dual Blunt Weapon - Top-grade

					case 36426: // Antharas Dualsword - Fragment
					case 36450: // Antharas Dualsword - Standard
					case 36474: // Antharas Dualsword - High-grade
					case 36498: // Antharas Dualsword - Top-grade

					case 36427: // Valakas Shaper - Fragment
					case 36451: // Valakas Shaper - Standard
					case 36475: // Valakas Shaper - High-grade
					case 36499: // Valakas Shaper - Top-grade

					case 36428: // Valakas Cutter - Fragment
					case 36452: // Valakas Cutter - Standard
					case 36476: // Valakas Cutter - High-grade
					case 36500: // Valakas Cutter - Top-grade

					case 36429: // Valakas Slasher - Fragment
					case 36453: // Valakas Slasher - Standard
					case 36477: // Valakas Slasher - High-grade
					case 36501: // Valakas Slasher - Top-grade

					case 36430: // Valakas Thrower - Fragment
					case 36454: // Valakas Thrower - Standard
					case 36478: // Valakas Thrower - High-grade
					case 36502: // Valakas Thrower - Top-grade

					case 36431: // Valakas Buster - Fragment
					case 36455: // Valakas Buster - Standard
					case 36479: // Valakas Buster - High-grade
					case 36503: // Valakas Buster - Top-grade

					case 36432: // Valakas Caster - Fragment
					case 36456: // Valakas Caster - Standard
					case 36480: // Valakas Caster - High-grade
					case 36504: // Valakas Caster - Top-grade

					case 36433: // Valakas Retributer - Fragment
					case 36457: // Valakas Retributer - Standard
					case 36481: // Valakas Retributer - High-grade
					case 36505: // Valakas Retributer - Top-grade

					case 36434: // Lindvior Shaper - Fragment
					case 36458: // Lindvior Shaper - Standard
					case 36482: // Lindvior Shaper - High-grade
					case 36506: // Lindvior Shaper - Top-grade

					case 36435: // Lindvior Thrower - Fragment
					case 36459: // Lindvior Thrower - Standard
					case 36483: // Lindvior Thrower - High-grade
					case 36507: // Lindvior Thrower - Top-grade

					case 36436: // Lindvior Slasher - Fragment
					case 36460: // Lindvior Slasher - Standard
					case 36484: // Lindvior Slasher - High-grade
					case 36508: // Lindvior Slasher - Top-grade

					case 36437: // Lindvior Caster - Fragment
					case 36461: // Lindvior Caster - Standard
					case 36485: // Lindvior Caster - High-grade
					case 36509: // Lindvior Caster - Top-grade

					case 36438: // Lindvior Cutter - Fragment
					case 36462: // Lindvior Cutter - Standard
					case 36486: // Lindvior Cutter - High-grade
					case 36510: // Lindvior Cutter - Top-grade

					case 36439: // Lindvior Shooter - Fragment
					case 36463: // Lindvior Shooter - Standard
					case 36487: // Lindvior Shooter - High-grade
					case 36511: // Lindvior Shooter - Top-grade

					case 36440: // Lindvior Dual Dagger - Fragment
					case 36464: // Lindvior Dual Dagger - Standard
					case 36488: // Lindvior Dual Dagger - High-grade
					case 36512: // Lindvior Dual Dagger - Top-grade

					{
						item = it;
						break;
					}
					default:
					{
						continue;
					}
				}
			}
			else
			{
				if (!(it instanceof L2EtcItem))
				{
					continue;
				}

				L2EtcItem stone = (L2EtcItem) it;
				if (stone.getHandlerName() == null || !stone.getHandlerName().equals("AppearanceStone") ||
						stone.getStandardItem() <= 0)
				{
					continue;
				}

				item = ItemTable.getInstance().getTemplate(stone.getStandardItem());
				if (item == null || items.contains(item))
				{
					continue;
				}
			}

			if (!search.isEmpty() && !item.getName().toLowerCase().contains(search))
			{
				continue;
			}

			boolean contained = false;
			for (L2Item i : items)
			{
				if (i.getName().equalsIgnoreCase(item.getName()))
				{
					contained = true;
				}
			}

			if (contained)
			{
				continue;
			}

			items.add(item);
		}

		return items;
	}

	private List<L2Item> getPage(List<L2Item> allItems, int page)
	{
		List<L2Item> items = new ArrayList<L2Item>();
		int index = 0;
		for (L2Item item : allItems)
		{
			if (index >= (page - 1) * ITEMS_PER_PAGE)
			{
				items.add(item);
			}

			index++;

			if (index >= page * ITEMS_PER_PAGE)
			{
				break;
			}
		}

		return items;
	}

	/**
	 * @see l2server.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
