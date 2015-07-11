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
package l2tserver.loginserver.network.gameserverpackets;

import l2tserver.loginserver.GameServerThread;
import l2tserver.loginserver.LoginController;
import l2tserver.util.network.BaseRecievePacket;

/**
 * @author mrTJO
 * Thanks to mochitto
 */
public class ReplyCharacters extends BaseRecievePacket
{
	/**
	 * @param decrypt
	 */
	public ReplyCharacters(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		String account = readS();
		int chars = readC();
		int charsToDel = readC();
		long[] charsList = new long[charsToDel];
		for (int i = 0; i < charsToDel; i++)
		{
			charsList[i] = readQ();
		}
		LoginController.getInstance().setCharactersOnServer(account,
				chars, charsList, server.getServerId());
	}
}
