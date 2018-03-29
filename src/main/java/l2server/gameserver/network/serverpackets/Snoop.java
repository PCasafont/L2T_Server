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

public class Snoop extends L2GameServerPacket
{
	private int convoId;
	private String name;
	private int type;
	private String speaker;
	private String msg;

	public Snoop(int id, String name, int type, String speaker, String msg)
	{
		convoId = id;
		this.name = name;
		this.type = type;
		this.speaker = speaker;
		this.msg = msg;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(convoId);
		writeS(name);
		writeD(0x00); //??
		writeD(type);
		writeS(speaker);
		writeS(msg);
	}
}
