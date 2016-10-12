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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class EnchantMultiSellTable implements Reloadable
{
	public static class EnchantMultiSellCategory
	{
		public int Id;
		public Map<Integer, EnchantMultiSellEntry> Entries = new HashMap<>();

		public EnchantMultiSellCategory(int id)
		{
			Id = id;
		}
	}

	public static class EnchantMultiSellEntry
	{
		public int EnchantLevel;
		public Map<Integer, Long> Ingredients = new LinkedHashMap<>();
		public Map<Integer, Integer> Products = new LinkedHashMap<>();

		public EnchantMultiSellEntry(int enchantLevel)
		{
			EnchantLevel = enchantLevel;
		}
	}

	public Map<Integer, EnchantMultiSellCategory> _categories = new HashMap<>();

	private EnchantMultiSellTable()
	{
		ReloadableManager.getInstance().register("enchantmultisell", this);
		reload();
	}

	@Override
	public final boolean reload()
	{
		File file = new File(Config.DATAPACK_ROOT, "data_" + Config.SERVER_NAME + "/enchantMultiSell.xml");
		XmlDocument doc = new XmlDocument(file);
		int currentCategoryId = 1;
		for (XmlNode categoryNode : doc.getFirstChild().getChildren())
		{
			if (!categoryNode.getName().equalsIgnoreCase("category"))
			{
				continue;
			}

			EnchantMultiSellCategory category = new EnchantMultiSellCategory(currentCategoryId++);
			for (XmlNode enchantNode : categoryNode.getChildren())
			{
				if (!enchantNode.getName().equalsIgnoreCase("enchant"))
				{
					continue;
				}

				int enchantLevel = enchantNode.getInt("level");
				EnchantMultiSellEntry product = new EnchantMultiSellEntry(enchantLevel);
				for (XmlNode chanceNode : enchantNode.getChildren())
				{
					if (chanceNode.getName().equalsIgnoreCase("ingredient"))
					{
						int id = chanceNode.getInt("id");
						long count = chanceNode.getLong("count");
						product.Ingredients.put(id, count);
					}
					else if (chanceNode.getName().equalsIgnoreCase("production"))
					{
						int enchant = chanceNode.getInt("enchantLevel");
						int chance = chanceNode.getInt("chance");
						product.Products.put(enchant, chance);
					}
				}

				category.Entries.put(enchantLevel, product);
			}

			_categories.put(category.Id, category);
		}

		Log.info("EnchantMultisell: Loaded " + _categories.size() + " categories.");
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Enchant Multisell Table reloaded";
	}

	public final Collection<EnchantMultiSellCategory> getCategories()
	{
		return _categories.values();
	}

	public final EnchantMultiSellCategory getCategory(int id)
	{
		return _categories.get(id);
	}

	public static EnchantMultiSellTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EnchantMultiSellTable _instance = new EnchantMultiSellTable();
	}
}
