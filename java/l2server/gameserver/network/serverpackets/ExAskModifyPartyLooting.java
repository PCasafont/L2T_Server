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

/**
 * @author JIV
 */
public class ExAskModifyPartyLooting extends L2GameServerPacket
{

	private String _requestor;
	private byte _mode;

	public ExAskModifyPartyLooting(String name, byte mode)
	{
		_requestor = name;
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_requestor);
		writeD(_mode);
	}
}
