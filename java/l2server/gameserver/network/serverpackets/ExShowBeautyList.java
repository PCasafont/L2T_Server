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

import l2server.gameserver.datatables.BeautyTable;
import l2server.gameserver.datatables.BeautyTable.BeautyInfo;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Map;

/**
 * @author LasTravel
 */

public final class ExShowBeautyList extends L2GameServerPacket
{
	private long _adena;
	private long _tickets;
	private boolean _isFace;

	public ExShowBeautyList(long adena, long tickets, boolean isFace)
	{
		_adena = adena;
		_tickets = tickets;
		_isFace = isFace;
	}

	@Override
	protected final void writeImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		writeQ(_adena);
		writeQ(_tickets);

		writeD(_isFace ? 1 : 0);

		Map<Integer, BeautyInfo> styles = null;
		if (!_isFace)
		{
			styles = BeautyTable.getInstance().getTemplate(0).getHairStyles();
		}
		else
		{
			styles = BeautyTable.getInstance().getTemplate(0).getFaceStyles();
		}

		writeD(styles.size());

		for (int id : styles.keySet())
		{
			writeD(id);
			writeD(99999999); // Remaining units
		}

		writeD(0);
		/*writeD(styles.size()); // For now ignore the already bought ones

		for (int id : styles.keySet())
		{
			writeD(id);
			writeD(1);
			writeD(101);
		}*/
	}
}
