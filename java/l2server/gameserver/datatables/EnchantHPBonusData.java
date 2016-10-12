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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.FuncTemplate;
import l2server.gameserver.stats.funcs.LambdaConst;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * @author MrPoke
 */
public class EnchantHPBonusData
{

	private final TIntObjectHashMap<Integer[]> _armorHPBonus = new TIntObjectHashMap<>();
	private static final float fullArmorModifier = 1.5f;

	public static EnchantHPBonusData getInstance()
	{
		return SingletonHolder._instance;
	}

	private EnchantHPBonusData()
	{
		load();
	}

	public void reload()
	{
		load();
	}

	private void load()
	{
		_armorHPBonus.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "enchantHPBonus.xml");

		if (file.exists())
		{
			XmlDocument doc = new XmlDocument(file);
			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("enchantHP"))
						{
							if (!d.hasAttribute("grade"))
							{
								Log.severe("[EnchantHPBonusData] Missing grade, skipping");
								continue;
							}
							int grade = d.getInt("grade");

							if (!d.hasAttribute("values"))
							{
								Log.severe("[EnchantHPBonusData] Missing bonus id: " + grade + ", skipping");
								continue;
							}
							StringTokenizer st = new StringTokenizer(d.getString("values"), ",");
							int tokenCount = st.countTokens();
							Integer[] bonus = new Integer[tokenCount];
							for (int i = 0; i < tokenCount; i++)
							{
								Integer value = Integer.decode(st.nextToken().trim());
								if (value == null)
								{
									Log.severe("[EnchantHPBonusData] Bad Hp value!! grade: " + grade + " token: " + i);
									value = 0;
								}
								bonus[i] = value;
							}
							_armorHPBonus.put(grade, bonus);
						}
					}
				}
			}
			if (_armorHPBonus.isEmpty())
			{
				return;
			}

			Collection<Integer> itemIds = ItemTable.getInstance().getAllArmorsId();
			int count = 0;

			for (Integer itemId : itemIds)
			{
				L2Item item = ItemTable.getInstance().getTemplate(itemId);
				if (item != null && item.getCrystalType() != L2Item.CRYSTAL_NONE)
				{
					switch (item.getBodyPart())
					{
						case L2Item.SLOT_CHEST:
						case L2Item.SLOT_FEET:
						case L2Item.SLOT_GLOVES:
						case L2Item.SLOT_HEAD:
						case L2Item.SLOT_LEGS:
						case L2Item.SLOT_BACK:
						case L2Item.SLOT_FULL_ARMOR:
						case L2Item.SLOT_UNDERWEAR:
						case L2Item.SLOT_L_HAND:
							count++;
							FuncTemplate ft = new FuncTemplate(null, "EnchantHp", Stats.MAX_HP, new LambdaConst(0));
							item.attach(ft);
							break;
					}
				}
			}

			// shields in the weapons table
			itemIds = ItemTable.getInstance().getAllWeaponsId();
			for (Integer itemId : itemIds)
			{
				L2Item item = ItemTable.getInstance().getTemplate(itemId);
				if (item != null && item.getCrystalType() != L2Item.CRYSTAL_NONE)
				{
					switch (item.getBodyPart())
					{
						case L2Item.SLOT_L_HAND:
							count++;
							FuncTemplate ft = new FuncTemplate(null, "EnchantHp", Stats.MAX_HP, new LambdaConst(0));
							item.attach(ft);
							break;
					}
				}
			}
			Log.info("Enchant HP Bonus registered for " + count + " items.");
		}
	}

	public final int getHPBonus(L2ItemInstance item)
	{
		final Integer[] values = _armorHPBonus.get(item.getItem().getItemGradePlain());

		if (values == null || values.length == 0)
		{
			return 0;
		}

		if (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
		{
			return (int) (values[Math.min(item.getEnchantLevel(), values.length) - 1] * fullArmorModifier);
		}
		else
		{
			return values[Math.min(item.getEnchantLevel(), values.length) - 1];
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EnchantHPBonusData _instance = new EnchantHPBonusData();
	}
}
