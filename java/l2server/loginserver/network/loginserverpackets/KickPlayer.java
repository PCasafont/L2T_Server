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

package l2server.loginserver.network.loginserverpackets;

import l2server.util.network.BaseSendablePacket;

import java.io.IOException;

/**
 * @author -Wooden-
 */
public class KickPlayer extends BaseSendablePacket
{
	public KickPlayer(String account)
	{
		writeC(0x04);
		writeS(account);
	}

	/* (non-Javadoc)
	 * @see l2server.loginserver.serverpackets.ServerBasePacket#getContent()
	 */
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
