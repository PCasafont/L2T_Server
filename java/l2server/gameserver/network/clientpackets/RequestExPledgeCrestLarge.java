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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.cache.CrestCache;
import l2server.gameserver.network.serverpackets.ExPledgeCrestLarge;

/**
 * Fomat : chd
 * c: (id) 0xD0
 * h: (subid) 0x10
 * d: the crest id
 * <p>
 * This is a trigger
 *
 * @author -Wooden-
 */
public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private int _crestId;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
		@SuppressWarnings("unused") int unk = readD();
		//Log.info(unk + "");
	}

	@Override
	protected void runImpl()
	{
		final byte[][] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
		if (data != null)
		{
			for (int i = 0; i < data.length; i++)
			{
				if (data[i] == null || data[i].length == 0)
				{
					break;
				}

				sendPacket(new ExPledgeCrestLarge(_crestId, i, data[i]));
			}
		}
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
