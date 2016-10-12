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
import l2server.gameserver.model.TradeList.TradeItem;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.templates.item.L2Item;

import static l2server.gameserver.datatables.MultiSell.PAGE_SIZE;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class PlayerMultiSellList extends L2GameServerPacket
{
	private L2PcInstance _player;

	public PlayerMultiSellList(L2PcInstance player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		writeD(_player.getObjectId()); // list id
		writeC(0x00);
		writeD(1); // page started from 1
		writeD(1); // finished
		writeD(PAGE_SIZE); // size of pages
		writeD(_player.getCustomSellList().getItemCount()); //list length
		writeC(0x00); // Old or modern format
		writeD(0x00);

		int i = 1;
		for (TradeItem item : _player.getCustomSellList().getItems())
		{
			if (item.getAppearance() > 0)
			{
				L2Item app = ItemTable.getInstance().getTemplate(item.getAppearance());
				getClient().sendPacket(new CreatureSay(_player.getObjectId(), Say2.TELL, _player.getName(),
						"WARNING: The " + item.getItem().getName() + " has appearance of " + app.getName() + "!"));
			}

			writeD(i);
			writeC(item.getItem().isStackable() ? 1 : 0);
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

			writeH(1);
			writeH(item.getPriceItems().size());

			writeD(item.getItem().getItemId());
			writeQ(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());

			writeQ(1); // Count
			writeH(item.getEnchantLevel()); // enchant level
			writeD(100); // Chance
			writeQ(0x00); // augment id
			writeH(item.getAttackElementType()); // attack element
			writeH(item.getAttackElementPower()); //element power
			writeH(item.getElementDefAttr((byte) 0)); // fire
			writeH(item.getElementDefAttr((byte) 1)); // water
			writeH(item.getElementDefAttr((byte) 2)); // wind
			writeH(item.getElementDefAttr((byte) 3)); // earth
			writeH(item.getElementDefAttr((byte) 4)); // holy
			writeH(item.getElementDefAttr((byte) 5)); // dark

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

			for (L2Item priceItem : item.getPriceItems().keySet())
			{
				writeD(priceItem.getItemId());
				writeH(priceItem.getType2());
				writeQ(item.getPriceItems().get(priceItem));
				writeH(0x00); // enchant level
				writeQ(0x00); // augment id
				writeH(-2); // attack element
				writeH(0x00); //element power
				writeH(0x00); // fire
				writeH(0x00); // water
				writeH(0x00); // wind
				writeH(0x00); // earth
				writeH(0x00); // holy
				writeH(0x00); // dark

				writeC(0x00);
				writeC(0x00);
			}

			i++;
		}

		if (_player.getClient() != null && _player.getClient().isDetached() && _player.getCustomSellList() != null &&
				_player.getCustomSellList().getItemCount() == 0)
		{
			_player.logout();
		}
	}

	@Override
	protected final Class<?> getOpCodeClass()
	{
		return MultiSellList.class;
	}
}
