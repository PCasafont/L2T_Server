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

import l2server.gameserver.model.L2ShortCut;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * ShortCutInit
 * format   d *(1dddd)/(2ddddd)/(3dddd)
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class ShortCutInit extends L2GameServerPacket
{

	private L2ShortCut[] _shortCuts;
	private L2PcInstance _activeChar;

	public ShortCutInit(L2PcInstance activeChar)
	{
		_activeChar = activeChar;

		if (_activeChar == null)
		{
			return;
		}

		_shortCuts = _activeChar.getAllShortCuts();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_shortCuts.length);

		if (getClient().getActiveChar() != null)
		{
			getClient().getActiveChar().sendSysMessage("Shortcuts Length = " + _shortCuts.length);
		}

		for (L2ShortCut sc : _shortCuts)
		{
			if (sc == null)
			{
				continue;
			}

			writeD(sc.getType());
			writeD(sc.getSlot() + sc.getPage() * 12);

			switch (sc.getType())
			{
				case L2ShortCut.TYPE_ITEM: //1
					writeD(sc.getId());
					writeD(0x01);
					writeD(sc.getSharedReuseGroup());
					writeD(0x00);
					writeD(0x00);
					writeH(0x00);
					writeH(0x00);
					writeQ(0x00);
					break;
				case L2ShortCut.TYPE_SKILL: //2
					writeD(sc.getId());
					writeD(sc.getLevel());
					writeD(sc.getSharedReuseGroup());
					writeC(0x00); // C5
					writeD(0x01); // C6
					break;
				case L2ShortCut.TYPE_ACTION: //3
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				case L2ShortCut.TYPE_MACRO: //4
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				case L2ShortCut.TYPE_RECIPE: //5
					writeD(sc.getId());
					writeD(0x01); // C6
					break;
				default:
					writeD(sc.getId());
					writeD(0x01); // C6
			}
		}
	}
}
