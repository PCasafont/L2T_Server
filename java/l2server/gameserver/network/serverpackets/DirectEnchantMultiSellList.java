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

import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.MultiSell;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Item;

import java.util.ArrayList;
import java.util.List;

public final class DirectEnchantMultiSellList extends L2GameServerPacket
{
	public enum DirectEnchantMultiSellConfig
	{
		ENCHANT_TO_10(400001, 10, 50549, 50550, 50551, 1, 1),
		ENCHANT_TO_15(400002, 15, -1, -1, 50552, 1, 1),
		ENCHANT_TO_16(400003, 16, -1, -1, 50553, 1, 1);

		public final int shopId;
		public final int enchantLevel;
		public final int armorMaterialId;
		public final int jewelMaterialId;
		public final int weaponMaterialId;

		public final int costCount;
		public final double priceDividerForArmor;

		DirectEnchantMultiSellConfig(int id, int enchant, int amid, int jwid, int wmid, int count, double priceDivider)
		{
			shopId = id;
			enchantLevel = enchant;
			armorMaterialId = amid;
			jewelMaterialId = jwid;
			weaponMaterialId = wmid;
			costCount = count;
			priceDividerForArmor = priceDivider;
		}

		public static DirectEnchantMultiSellConfig getConfig(int id)
		{
			for (DirectEnchantMultiSellConfig config : values())
			{
				if (config.shopId == id)
				{
					return config;
				}
			}

			return null;
		}
	}

	private final DirectEnchantMultiSellConfig _config;
	private final List<L2ItemInstance> _mainIngredients = new ArrayList<>();

	public DirectEnchantMultiSellList(L2PcInstance player, DirectEnchantMultiSellConfig config)
	{
		_config = config;

		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)
			{
				continue;
			}

			int currencyId = item.isWeapon() ? _config.weaponMaterialId :
					item.getItem().getBodyPart() >= L2Item.SLOT_R_EAR &&
							item.getItem().getBodyPart() <= L2Item.SLOT_LR_FINGER ? _config.jewelMaterialId :
							_config.armorMaterialId;

			System.out.println("Currency " + currencyId + " for " + item.getName());
			if (currencyId != -1 && !item.isEquipped() && EnchantItemTable.isEnchantable(item) &&
					item.getEnchantLevel() < _config.enchantLevel)
			{
				_mainIngredients.add(item);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		writeD(_config.shopId); // list id
		writeC(0x00);
		writeD(0x01); // page
		writeD(0x01); // finished
		writeD(MultiSell.PAGE_SIZE); // size of pages
		writeD(_mainIngredients.size()); //list length
		writeC(0x00); // Old or modern format
		writeD(0x00);

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

				writeC(0x00);
				writeC(0x00);

				writeH(0x01); // products list size
				writeH(0x02); // ingredients list size

				// Product
				writeD(item.getItemId());
				writeQ(item.getItem().getBodyPart());
				writeH(item.getItem().getType2());
				writeQ(item.getCount());
				writeH(_config.enchantLevel); //enchant lvl
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

				// Main Ingredient
				writeD(item.getItemId());
				writeH(item.getItem().getType2());
				writeQ(item.getCount());
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

				// Currency
				int currencyId = item.isWeapon() ? _config.weaponMaterialId :
						item.getItem().getBodyPart() >= L2Item.SLOT_R_EAR &&
								item.getItem().getBodyPart() <= L2Item.SLOT_LR_FINGER ? _config.jewelMaterialId :
								_config.armorMaterialId;
				writeD(currencyId);
				writeH(ItemTable.getInstance().getTemplate(currencyId).getType2());
				writeQ(item.isWeapon() ? _config.costCount : (int) (_config.costCount / _config.priceDividerForArmor));
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

	@Override
	protected final Class<?> getOpCodeClass()
	{
		return MultiSellList.class;
	}
}
