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
 * This class ...
 *
 * @version $Revision: 1.1.6.2 $ $Date: 2005/03/27 15:29:39 $
 */
public class PlaySound extends L2GameServerPacket
{
	private int unknown1;
	private String soundFile;
	private int unknown3;
	private int unknown4;
	private int unknown5;
	private int unknown6;
	private int unknown7;
	private int unknown8;

	public PlaySound(String soundFile)
	{
		this.unknown1 = 0;
		this.soundFile = soundFile;
		this.unknown3 = 0;
		this.unknown4 = 0;
		this.unknown5 = 0;
		this.unknown6 = 0;
		this.unknown7 = 0;
		this.unknown8 = 0;
	}

	public PlaySound(int unknown1, String soundFile, int unknown3, int unknown4, int unknown5, int unknown6, int unknown7)
	{
		this.unknown1 = unknown1;
		this.soundFile = soundFile;
		this.unknown3 = unknown3;
		this.unknown4 = unknown4;
		this.unknown5 = unknown5;
		this.unknown6 = unknown6;
		this.unknown7 = unknown7;
		this.unknown8 = 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.unknown1); //unknown 0 for quest and ship;
		writeS(this.soundFile);
		writeD(this.unknown3); //unknown 0 for quest; 1 for ship;
		writeD(this.unknown4); //0 for quest; objectId of ship
		writeD(this.unknown5); //x
		writeD(this.unknown6); //y
		writeD(this.unknown7); //z
		writeD(this.unknown8);
	}
}
