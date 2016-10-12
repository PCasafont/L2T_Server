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
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Pere
 */

public class CompoundTable implements Reloadable
{
	public class Combination
	{
		private final int _item1;
		private final int _item2;
		private final int _result;
		private final int _chance;

		public Combination(int item1, int item2, int result, int chance)
		{
			_item1 = item1;
			_item2 = item2;
			_result = result;
			_chance = chance;
		}

		public int getItem1()
		{
			return _item1;
		}

		public int getItem2()
		{
			return _item2;
		}

		public int getResult()
		{
			return _result;
		}

		public int getChance()
		{
			return _chance;
		}
	}

	private final Map<Integer, Combination> _combinations = new HashMap<>();
	private final Set<Integer> _combinable = new HashSet<>();

	private CompoundTable()
	{
		reload();
		ReloadableManager.getInstance().register("compound", this);
	}

	@Override
	public boolean reload()
	{
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/compound.xml");
		if (!file.exists())
		{
			file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "/compound.xml");
		}

		XmlDocument doc = new XmlDocument(file);
		_combinations.clear();

		for (XmlNode d : doc.getFirstChild().getChildren())
		{
			if (d.getName().equalsIgnoreCase("combination"))
			{
				int item1 = d.getInt("item1");
				int item2 = d.getInt("item2");
				int result = d.getInt("result");
				int chance = d.getInt("chance");
				_combinations.put(getHash(item1, item2), new Combination(item2, item2, result, chance));
				_combinable.add(item1);
				_combinable.add(item2);
			}
		}

		Log.info("CompoundTable: Loaded " + _combinations.size() + " combinations.");
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Compound table reloaded";
	}

	public Combination getCombination(int item1, int item2)
	{
		return _combinations.get(getHash(item1, item2));
	}

	public boolean isCombinable(int itemId)
	{
		return _combinable.contains(itemId);
	}

	private int getHash(int item1, int item2)
	{
		return Math.min(item1, item2) * 100000 + Math.max(item1, item2);
	}

	public static CompoundTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CompoundTable _instance = new CompoundTable();
	}
}
