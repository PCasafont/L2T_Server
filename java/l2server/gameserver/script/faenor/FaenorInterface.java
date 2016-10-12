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

package l2server.gameserver.script.faenor;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.datatables.EventDroplist;
import l2server.gameserver.model.L2DropCategory;
import l2server.gameserver.model.L2DropData;
import l2server.gameserver.script.DateRange;
import l2server.gameserver.script.EngineInterface;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Arias
 */
public class FaenorInterface implements EngineInterface
{

	public static FaenorInterface getInstance()
	{
		return SingletonHolder._instance;
	}

	private FaenorInterface()
	{
	}

	public List<?> getAllPlayers()
	{
		return null;
	}

	/**
	 * Adds a new Quest Drop to an NPC
	 */
	@Override
	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states)
	{
		L2NpcTemplate npc = npcTable.getTemplate(npcID);
		if (npc == null)
		{
			throw new NullPointerException();
		}
		L2DropData drop = new L2DropData(itemID, min, max, chance);
		drop.setQuestID(questID);
		drop.addStates(states);
		addDrop(npc, drop, false);
	}

	/**
	 * Adds a new Drop to an NPC
	 */
	public void addDrop(int npcID, int itemID, int min, int max, boolean sweep, int chance) throws NullPointerException
	{
		L2NpcTemplate npc = npcTable.getTemplate(npcID);
		if (npc == null)
		{
			if (Config.DEBUG)
			{
				Log.warning("Npc doesnt Exist");
			}
			throw new NullPointerException();
		}
		L2DropData drop = new L2DropData(itemID, min, max, chance);

		addDrop(npc, drop, sweep);
	}

	/**
	 * Adds a new drop to an NPC.  If the drop is sweep, it adds it to the NPC's Sweep category
	 * If the drop is non-sweep, it creates a new category for this drop.
	 *
	 * @param npc
	 * @param drop
	 * @param sweep
	 */
	public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep)
	{
		if (sweep)
		{
			addDrop(npc, drop, -1);
		}
		else
		{
			//npc.addMultiDrop(drop, maxCategory);
		}
	}

	/**
	 * Adds a new drop to an NPC, in the specified category.  If the category does not exist,
	 * it is created.
	 *
	 * @param npc
	 * @param drop
	 */
	public void addDrop(L2NpcTemplate npc, L2DropData drop, int category)
	{
		//npc.addMultiDrop(drop, category);
	}

	public List<L2DropData> getQuestDrops(int npcID)
	{
		L2NpcTemplate npc = npcTable.getTemplate(npcID);
		if (npc == null)
		{
			return null;
		}
		List<L2DropData> questDrops = new ArrayList<>();
		for (L2DropCategory cat : npc.getMultiDropData())
		{
			for (L2DropData drop : cat.getAllDrops())
			{
				if (drop.getQuestID() != null)
				{
					questDrops.add(drop);
				}
			}
		}
		return questDrops;
	}

	@Override
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
	{
		EventDroplist.getInstance().addGlobalDrop(items, count, (int) (chance * L2DropData.MAX_CHANCE), range);
	}

	@Override
	public void onPlayerLogin(String[] message, DateRange validDateRange)
	{
		Announcements.getInstance().addEventAnnouncement(validDateRange, message);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FaenorInterface _instance = new FaenorInterface();
	}
}
