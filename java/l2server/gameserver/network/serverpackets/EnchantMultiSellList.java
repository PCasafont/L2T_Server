/*
 * $Header: MultiSellList.java, 2/08/2005 14:21:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 2/08/2005 14:21:01 $
 * $Revision: 1 $
 * $Log: MultiSellList.java,v $
 * Revision 1  2/08/2005 14:21:01  luisantonioa
 * Added copyright notice
 *
 *
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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.datatables.*;
import l2server.gameserver.datatables.EnchantMultiSellTable.EnchantMultiSellCategory;
import l2server.gameserver.datatables.EnchantMultiSellTable.EnchantMultiSellEntry;
import l2server.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class EnchantMultiSellList extends L2GameServerPacket
{
	public static int ShopId = 4000000;
	public static int ItemIdMod = 100000000;

	private final Map<Integer, List<L2ItemInstance>> _mainIngredients = new LinkedHashMap<>();
	private final MerchantPriceConfig _mpc;

	public EnchantMultiSellList(L2PcInstance player)
	{
		_mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(player);
		for (EnchantMultiSellCategory category : EnchantMultiSellTable.getInstance().getCategories())
		{
			List<L2ItemInstance> mainIngredients = new ArrayList<>();
			for (L2ItemInstance item : player.getInventory().getItems())
			{
				if (!item.isEquipped() && EnchantItemTable.isEnchantable(item) &&
						category.Entries.containsKey(item.getEnchantLevel() + 1))
				{
					mainIngredients.add(item);
				}
			}

			_mainIngredients.put(category.Id, mainIngredients);
		}
	}

	@Override
	protected final void writeImpl()
	{
		int ingredientsSize = 0;
		for (List<L2ItemInstance> items : _mainIngredients.values())
		{
			ingredientsSize += items.size();
		}

		writeC(0x00);
		writeD(ShopId); // list id
		writeC(0x00);
		writeD(0x01); // page
		writeD(0x01); // finished
		writeD(MultiSell.PAGE_SIZE); // size of pages
		writeD(ingredientsSize); //list length
		writeC(0x01); // Old or modern format
		writeD(0x00);

		for (EnchantMultiSellCategory category : EnchantMultiSellTable.getInstance().getCategories())
		{
			for (L2ItemInstance item : _mainIngredients.get(category.Id))
			{
				EnchantMultiSellEntry entry = category.Entries.get(item.getEnchantLevel() + 1);

				writeD(category.Id * ItemIdMod + item.getObjectId() % ItemIdMod); // entry id
				writeC(0x00); // stackable
				writeH(0x00); // C6
				writeD(0x00); // C6
				writeD(0x00); // T1
				writeH(-2); // T1
				writeH(0x00); // T1
				writeH(0x00); // T1
				writeH(0x00); // T1
				writeH(0x00); // T1
				writeH(0x00); // T1
				writeH(0x00); // T1
				writeH(0x00); // T1

				writeC(0x00);
				writeC(0x00);

				writeH(entry.Products.size() + 1); // products list size
				writeH(entry.Ingredients.size() + 1); // ingredients list size

				// Product
				writeD(item.getItemId());
				writeQ(item.getItem().getBodyPart());
				writeH(item.getItem().getType2());
				writeQ(item.getCount());
				writeH(item.getEnchantLevel() + 1); //enchant lvl
				writeD(100); // Chance
				if (item.isAugmented())
				{
					writeQ(item.getAugmentation().getId()); // C6
				}
				else
				{
					writeQ(0x00);
				}
				writeH(item.getAttackElementType()); // T1 element id
				writeH(item.getAttackElementPower()); // T1 element power
				for (byte j = 0; j < 6; j++)
				{
					writeH(item.getElementDefAttr(j));
				}

				int[] ensoulEffects = item.getEnsoulEffectIds();
				int[] ensoulSpecialEffects = item.getEnsoulSpecialEffectIds();
				writeC(ensoulEffects.length);
				for (int effect : ensoulEffects)
				{
					writeD(effect);
				}
				writeC(ensoulSpecialEffects.length);
				for (int effect : ensoulSpecialEffects)
				{
					writeD(effect);
				}

				for (Entry<Integer, Integer> possibleProduct : entry.Products.entrySet())
				{
					int enchantLevel = possibleProduct.getKey();
					int chance = possibleProduct.getValue();
					// Product
					writeD(item.getItemId());
					writeQ(item.getItem().getBodyPart());
					writeH(item.getItem().getType2());
					writeQ(item.getCount());
					writeH(enchantLevel); //enchant lvl
					writeD(chance); // Chance
					if (item.isAugmented())
					{
						writeQ(item.getAugmentation().getId()); // C6
					}
					else
					{
						writeQ(0x00);
					}
					writeH(item.getAttackElementType()); // T1 element id
					writeH(item.getAttackElementPower()); // T1 element power
					for (byte j = 0; j < 6; j++)
					{
						writeH(item.getElementDefAttr(j));
					}

					ensoulEffects = item.getEnsoulEffectIds();
					ensoulSpecialEffects = item.getEnsoulSpecialEffectIds();
					writeC(ensoulEffects.length);
					for (int effect : ensoulEffects)
					{
						writeD(effect);
					}
					writeC(ensoulSpecialEffects.length);
					for (int effect : ensoulSpecialEffects)
					{
						writeD(effect);
					}
				}

				// Main Ingredient
				writeD(item.getItemId());
				writeH(item.getItem().getType2());
				long ingCount = item.getCount();
				if (item.getItemId() == 57)
				{
					ingCount = (long) (item.getCount() * (1.0 + _mpc.getCastleTaxRate()));
				}
				writeQ(ingCount);
				writeH(item.getEnchantLevel()); // enchant lvl
				if (item.isAugmented())
				{
					writeQ(item.getAugmentation().getId()); // C6
				}
				else
				{
					writeQ(0x00);
				}
				writeH(item.getAttackElementType()); // T1 element id
				writeH(item.getAttackElementPower()); // T1 element power
				for (byte j = 0; j < 6; j++)
				{
					writeH(item.getElementDefAttr(j));
				}

				ensoulEffects = item.getEnsoulEffectIds();
				ensoulSpecialEffects = item.getEnsoulSpecialEffectIds();
				writeC(ensoulEffects.length);
				for (int effect : ensoulEffects)
				{
					writeD(effect);
				}
				writeC(ensoulSpecialEffects.length);
				for (int effect : ensoulSpecialEffects)
				{
					writeD(effect);
				}

				for (Entry<Integer, Long> extraIngredient : entry.Ingredients.entrySet())
				{
					int id = extraIngredient.getKey();
					long count = extraIngredient.getValue();
					writeD(id);
					writeH(ItemTable.getInstance().getTemplate(id).getType2());
					long price = count;
					if (!item.isWeapon())
					{
						price /= 5;
					}
					if (price == 0)
					{
						price = 1;
					}
					writeQ(price);
					writeH(0x00); // enchant lvl
					writeQ(0x00); // augmentation
					writeH(0x00); // T1 element id
					writeH(0x00); // T1 element power
					for (byte j = 0; j < 6; j++)
					{
						writeH(0x00);
					}

					writeC(0x00);
					writeC(0x00);
				}
			}
		}
	}

	@Override
	protected final Class<?> getOpCodeClass()
	{
		return MultiSellList.class;
	}
}
