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
package l2tserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2tserver.Config;
import l2tserver.gameserver.datatables.EnchantItemTable;
import l2tserver.gameserver.datatables.ItemTable;
import l2tserver.gameserver.datatables.MultiSell;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class EnchantMultiSellList extends L2GameServerPacket
{
	public static int ShopId = 4000000;
	public static Map<Integer, Integer> Prices = new HashMap<Integer, Integer>();
	
	static
	{
		if (Config.SERVER_NAME.contains("tarziph"))
		{
			Prices.put(9, 400);
			Prices.put(10, 1200);
			Prices.put(11, 5000);
			Prices.put(12, 15000);
		}
		else
		{
			Prices.put(17, 1);
			Prices.put(18, 3);
			Prices.put(19, 10);
			Prices.put(20, 35);
		}
	}
	
	private static final String _S__D0_MULTISELLLIST = "[S] d0 MultiSellList";
	
	private final List<L2ItemInstance> _mainIngredients = new ArrayList<L2ItemInstance>();
	
	public EnchantMultiSellList(L2PcInstance player)
	{
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (!item.isEquipped() && EnchantItemTable.isEnchantable(item)
					&& Prices.containsKey(item.getEnchantLevel() + 1))
				_mainIngredients.add(item);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xd0);
		writeD(ShopId);	// list id
		writeC(0x00);
		writeD(0x01);		// page
		writeD(0x01);	// finished
		writeD(MultiSell.PAGE_SIZE);	// size of pages
		writeD(_mainIngredients.size()); //list length
		writeC(0x00); // Old or modern format
		
		if (!_mainIngredients.isEmpty())
		{
			for (L2ItemInstance item : _mainIngredients)
			{
				writeD(item.getObjectId()); // entry id
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
				
				writeH(0x01); // products list size
				writeH(0x02); // ingredients list size
				
				// Product
				writeD(item.getItemId());
				writeQ(item.getItem().getBodyPart());
				writeH(item.getItem().getType2());
				writeQ(item.getCount());
				writeH(item.getEnchantLevel() + 1); //enchant lvl
				writeD(100); // Chance
				if (item.isAugmented())
					writeQ(item.getAugmentation().getId()); // C6
				else
					writeQ(0x00);
				writeH(item.getAttackElementType()); // T1 element id
				writeH(item.getAttackElementPower()); // T1 element power
				for (byte j = 0; j < 6; j++)
					writeH(item.getElementDefAttr(j));
				
				// Main Ingredient
				writeD(item.getItemId());
				writeH(item.getItem().getType2());
				writeQ(item.getCount());
				writeH(item.getEnchantLevel()); // enchant lvl
				if (item.isAugmented())
					writeQ(item.getAugmentation().getId()); // C6
				else
					writeQ(0x00);
				writeH(item.getAttackElementType()); // T1 element id
				writeH(item.getAttackElementPower()); // T1 element power
				for (byte j = 0; j < 6; j++)
					writeH(item.getElementDefAttr(j));
				
				// Currency
				int currencyId = Config.DONATION_COIN_ID;
				writeD(currencyId);
				writeH(ItemTable.getInstance().getTemplate(currencyId).getType2());
				int price = Prices.get(item.getEnchantLevel() + 1);
				if (!item.isWeapon())
					price /= 5;
				if (price == 0)
					price = 1;
				writeQ(price);
				writeH(0x00); // enchant lvl
				writeQ(0x00); // augmentation
				writeH(0x00); // T1 element id
				writeH(0x00); // T1 element power
				for (byte j = 0; j < 6; j++)
					writeH(0x00);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D0_MULTISELLLIST;
	}
	
}
