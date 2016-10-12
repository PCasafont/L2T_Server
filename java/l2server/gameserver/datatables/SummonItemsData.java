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

/*
  @author FBIagent
 */

package l2server.gameserver.datatables;

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.model.L2SummonItem;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

public class SummonItemsData
{
	private TIntObjectHashMap<L2SummonItem> _summonitems;

	public static SummonItemsData getInstance()
	{
		return SingletonHolder._instance;
	}

	private SummonItemsData()
	{
		_summonitems = new TIntObjectHashMap<>();

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "summonItems.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("item"))
					{
						int itemId = d.getInt("id");
						int npcId = d.getInt("npcId");
						byte summonType = (byte) d.getInt("summonType");
						int despawnTime = d.getInt("despawn", -1);

						L2SummonItem summonitem = new L2SummonItem(itemId, npcId, summonType, despawnTime);
						_summonitems.put(itemId, summonitem);
					}
				}
			}
		}

		Log.info("Summon items data: Loaded " + _summonitems.size() + " summon items.");
	}

	public L2SummonItem getSummonItem(int itemId)
	{
		return _summonitems.get(itemId);
	}

	public int[] itemIDs()
	{
		int size = _summonitems.size();
		int[] result = new int[size];
		int i = 0;
		for (Object si : _summonitems.getValues())
		{
			result[i++] = ((L2SummonItem) si).getItemId();
		}
		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SummonItemsData _instance = new SummonItemsData();
	}
}
