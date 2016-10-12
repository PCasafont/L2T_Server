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
import l2server.gameserver.network.serverpackets.CharDeleteFail;
import l2server.gameserver.network.serverpackets.CharDeleteSuccess;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;
import l2server.log.Log;

import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.8.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class CharacterDelete extends L2GameClientPacket
{

	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterDelete"))
		{
			sendPacket(new CharDeleteFail(CharDeleteFail.REASON_DELETION_FAILED));
			return;
		}

		if (Config.DEBUG)
		{
			Log.fine("deleting slot:" + _charSlot);
		}

		try
		{
			byte answer = getClient().markToDeleteChar(_charSlot);

			switch (answer)
			{
				default:
				case -1: // Error
					break;
				case 0: // Success!
					sendPacket(new CharDeleteSuccess());
					break;
				case 1:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				case 2:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error:", e);
		}

		CharSelectionInfo cl =
				new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
}
