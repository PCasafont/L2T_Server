/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.L2World;

/**
 * Support for "Chat with Friends" dialog.
 * <BR>
 * Inform player about friend online status change
 * <BR>
 * Format: cdSd<BR>
 * d: Online/Offline<BR>
 * S: Friend Name  <BR>
 * d: Player Object ID <BR>
 *
 * @author JIV
 */
public class FriendStatusPacket extends L2GameServerPacket
{
	private boolean _online;
	private int _objid;
	private String _name;

	public FriendStatusPacket(int objId)
	{
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = L2World.getInstance().getPlayer(objId) != null;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_online ? 1 : 0);
		writeS(_name);
		writeD(_objid);
	}
}
