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

import l2server.Config;
import l2server.gameserver.communitybbs.CommunityBoard;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestShowBoard extends L2GameClientPacket
{

	@SuppressWarnings("unused")
	private int _unknown;

	/**
	 * packet type id 0x57
	 * <p>
	 * sample
	 * <p>
	 * 57
	 * 01 00 00 00		// unknown (always 1?)
	 * <p>
	 * format:		cd
	 */
	@Override
	protected final void readImpl()
	{
		_unknown = readD();
	}

	@Override
	protected void runImpl()
	{
		CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
