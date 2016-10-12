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

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.MultiSell;
import l2server.gameserver.model.multisell.Ingredient;
import l2server.gameserver.model.multisell.ListContainer;
import l2server.gameserver.model.multisell.MultiSellEntry;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.templates.item.L2Item;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class MultiSellList extends L2GameServerPacket
{

	private int _size, _index;
	private final ListContainer _list;
	private final boolean _finished;

	public MultiSellList(ListContainer list, int index)
	{
		_list = list;
		_index = index;
		_size = list.getEntries().size() - index;
		if (_size > MultiSell.PAGE_SIZE)
		{
			_finished = false;
			_size = MultiSell.PAGE_SIZE;
		}
		else
		{
			_finished = true;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		writeD(_list.getListId()); // list id
		writeC(0x00);
		writeD(1 + _index / MultiSell.PAGE_SIZE); // page started from 1
		writeD(_finished ? 1 : 0); // finished
		writeD(MultiSell.PAGE_SIZE); // size of pages
		writeD(_size); //list length
		writeC(_list.isChance() ? 0x01 : 0x00); // Old or modern format
		writeD(0);

		//String toLog = "";
		MultiSellEntry ent;
		while (_size-- > 0)
		{
			ent = _list.getEntries().get(_index++);

			final Ingredient product = ent.getProducts().get(0);

			if (product.getItemId() > 0)
			{
				L2Item productTemplate = ItemTable.getInstance().getTemplate(product.getItemId());

				if (productTemplate != null)
				{
					productTemplate.setSalePrice(0);
				}
			}

			/*
			toLog += "\t<!-- " + productTemplate.getName() + " -->\n";
			toLog += "\t<item>\n";

			toLog += "\t\t<ingredient id=\"57\" count=\"" + productTemplate.getReferencePrice() * 1000 + "\" /><!-- Adena -->\n";
			 */
            /*
			for (Ingredient i : ent.getIngredients())
			{
				L2Item itemTemplate = ItemTable.getInstance().getTemplate(i.getItemId());

				if (itemTemplate == null)
					continue;
				else if (itemTemplate.getItemId() == 57)
					continue;

				//long itemCount = i.getItemId() >= 5570 && i.getItemId() <= 5574 ? i.getItemCount() / 5 : i.getItemCount();
				//toLog += "\t\t<ingredient id=\"" + i.getItemId() + "\" count=\"" + itemCount + "\" /> <!-- " + itemTemplate.getName() + " -->\n";
			}*/
			/*
			toLog += "\t\t<production id=\"" + product.getItemId() + "\" count=\"" + product.getItemCount() + "\" />\n";
			toLog += "\t</item>\n";
			 */
			writeD(ent.getEntryId());
			writeC(ent.isStackable() ? 1 : 0);
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

			writeH(ent.getProducts().size());
			writeH(ent.getIngredients().size());

			for (Ingredient ing : ent.getProducts())
			{
				writeD(ing.getItemId());
				if (ing.getTemplate() != null)
				{
					writeQ(ing.getTemplate().getBodyPart());
					writeH(ing.getTemplate().getType2());
				}
				else
				{
					writeQ(0);
					writeH(-1);
				}

				writeQ(ing.getItemCount());
				if (ing.getItemInfo() != null)
				{
					writeH(ing.getItemInfo().getEnchantLevel()); // enchant level
					writeD(ing.getChance() > 0 ? Math.max(Math.round(ing.getChance()), 1) : 0); // Chance
					writeQ(ing.getItemInfo().getAugmentId()); // augment id
					writeH(ing.getItemInfo().getElementId()); // attack element
					writeH(ing.getItemInfo().getElementPower()); //element power
					for (byte j = 0; j < 6; j++)
					{
						writeH(ing.getItemInfo().getElementals()[j]);
					}

					int[] ensoulEffects = ing.getItemInfo().getEnsoulEffectIds();
					int[] ensoulSpecialEffects = ing.getItemInfo().getEnsoulSpecialEffectIds();
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
				else
				{
					writeH(0x00); // enchant level
					writeD(ing.getChance() > 0 ? Math.max(Math.round(ing.getChance()), 1) : 0); // Chance
					writeQ(0x00); // augment id
					writeH(-2); // attack element
					writeH(0x00); //element power
					for (byte j = 0; j < 6; j++)
					{
						writeH(0x00);
					}

					writeC(0x00);
					writeC(0x00);
				}
			}

			for (Ingredient ing : ent.getIngredients())
			{
				writeD(ing.getItemId());
				writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : -1);
				writeQ(ing.getItemCount());

				if (ing.getItemCount() > Integer.MAX_VALUE)
				{
					L2Item productItem = ItemTable.getInstance().getTemplate(ent.getProducts().get(0).getItemId());
					L2Item ingItem = ItemTable.getInstance().getTemplate(ing.getItemId());
					getWriteClient().sendPacket(new CreatureSay(0, Say2.TELL, "Store",
							"WARNING: The " + productItem.getName() + "'s necessary " + ingItem.getName() +
									" quantity is " + ing.getItemCount()));
				}

				if (ing.getItemInfo() != null)
				{
					writeH(ing.getItemInfo().getEnchantLevel()); // enchant level
					writeQ(ing.getItemInfo().getAugmentId()); // augment id
					writeH(ing.getItemInfo().getElementId()); // attack element
					writeH(ing.getItemInfo().getElementPower()); //element power
					for (byte j = 0; j < 6; j++)
					{
						writeH(ing.getItemInfo().getElementals()[j]);
					}

					int[] ensoulEffects = ing.getItemInfo().getEnsoulEffectIds();
					int[] ensoulSpecialEffects = ing.getItemInfo().getEnsoulSpecialEffectIds();
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
				else
				{
					writeH(0x00); // enchant level
					writeQ(0x00); // augment id
					writeH(-2); // attack element
					writeH(0x00); //element power
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
}
