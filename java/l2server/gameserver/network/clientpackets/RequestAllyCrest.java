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

import l2server.gameserver.network.serverpackets.AllyCrest;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAllyCrest extends L2GameClientPacket
{

	private int _crestId;

	/**
	 * packet type id 0x88 format: cd
	 */
	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new AllyCrest(_crestId));
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
