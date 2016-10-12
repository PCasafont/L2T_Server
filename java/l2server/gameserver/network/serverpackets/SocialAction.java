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
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class SocialAction extends L2GameServerPacket
{
	public static final int THROW_BALL = 30;
	public static final int HIT_BALL = 31;
	public static final int CHEER = 32;
	public static final int PLAY = 33;
	public static final int CLAP = 34;
	public static final int LEVEL_UP = 2122;

	private int _charObjId;
	private int _actionId;

	/**
	 * @param objectId
	 * @param actionId
	 */
	public SocialAction(int objectId, int actionId)
	{
		_charObjId = objectId;
		_actionId = actionId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId);
		writeD(_actionId);
		writeD(0x00); // GoD ??? (maybe npcstringId or npcTextId)
	}
}
