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

package l2server.gameserver.network.serverpackets;

import l2server.Config;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;

/**
 * 5e
 * 01 00 00 00 		01 - added ?  02 - modified
 * 7b 86 73 42	  object id
 * 08 00 00 00	  body slot
 * <p>
 * <p>
 * <p>
 * body slot
 * 0000  ?? underwear
 * 0001  ear
 * 0002  ear
 * 0003  neck
 * 0004  finger   (magic ring)
 * 0005  finger   (magic ring)
 * 0006  head	 (l.cap)
 * 0007  r.hand   (dagger)
 * 0008  l.hand   (arrows)
 * 0009  hands	(short gloves)
 * 000a  chest	(squire shirt)
 * 000b  legs	 (squire pants)
 * 000c  feet
 * 000d  ?? back
 * 000e  lr.hand   (bow)
 * <p>
 * <p>
 * <p>
 * format  ddd
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public final class EquipUpdate extends L2GameServerPacket
{
	private L2ItemInstance _item;
	private int _change;

	public EquipUpdate(L2ItemInstance item, int change)
	{
		_item = item;
		_change = change;
	}

	@Override
	protected final void writeImpl()
	{
		int bodypart = 0;
		writeD(_item.getObjectId());
		writeD(0x00); // ???
		writeH(_change);
		switch (_item.getItem().getBodyPart())
		{
			case L2Item.SLOT_L_EAR:
				bodypart = 0x01;
				break;
			case L2Item.SLOT_R_EAR:
				bodypart = 0x02;
				break;
			case L2Item.SLOT_NECK:
				bodypart = 0x03;
				break;
			case L2Item.SLOT_R_FINGER:
				bodypart = 0x04;
				break;
			case L2Item.SLOT_L_FINGER:
				bodypart = 0x05;
				break;
			case L2Item.SLOT_HEAD:
				bodypart = 0x06;
				break;
			case L2Item.SLOT_R_HAND:
				bodypart = 0x07;
				break;
			case L2Item.SLOT_L_HAND:
				bodypart = 0x08;
				break;
			case L2Item.SLOT_GLOVES:
				bodypart = 0x09;
				break;
			case L2Item.SLOT_CHEST:
				bodypart = 0x0a;
				break;
			case L2Item.SLOT_LEGS:
				bodypart = 0x0b;
				break;
			case L2Item.SLOT_FEET:
				bodypart = 0x0c;
				break;
			case L2Item.SLOT_BACK:
				bodypart = 0x0d;
				break;
			case L2Item.SLOT_LR_HAND:
				bodypart = 0x0e;
				break;
			case L2Item.SLOT_HAIR:
				bodypart = 0x0f;
				break;
			case L2Item.SLOT_BELT:
				bodypart = 0x10;
				break;
		}

		if (Config.DEBUG)
		{
			Log.fine("body:" + bodypart);
		}

		writeH(bodypart);
	}
}
