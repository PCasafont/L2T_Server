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

import l2server.gameserver.communitybbs.CommunityBoard;

/**
 * Format SSSSSS
 *
 * @author -Wooden-
 */
public final class RequestBBSWrite extends L2GameClientPacket
{
	private String url;
	private String arg1;
	private String arg2;
	private String arg3;
	private String arg4;
	private String arg5;

	@Override
	protected final void readImpl()
	{
		this.url = readS();
		this.arg1 = readS();
		this.arg2 = readS();
		this.arg3 = readS();
		this.arg4 = readS();
		this.arg5 = readS();
	}

	@Override
	protected final void runImpl()
	{
		CommunityBoard.getInstance().handleWriteCommands(getClient(), this.url, this.arg1, this.arg2, this.arg3, this.arg4, this.arg5);
	}
}
