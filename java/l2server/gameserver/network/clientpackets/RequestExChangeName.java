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

import l2server.gameserver.network.serverpackets.ExNeedToChangeName;
import l2server.log.Log;

/**
 * Reply for {@link ExNeedToChangeName}
 *
 * @author JIV
 */
public class RequestExChangeName extends L2GameClientPacket
{
	private String _newName;
	private int _type;
	private int _charSlot;

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		_type = readD();
		_newName = readS();
		_charSlot = readD();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		Log.info("Recieved " + getType() + " name: " + _newName + " type: " + _type + " CharSlot: " + _charSlot);
	}
}
