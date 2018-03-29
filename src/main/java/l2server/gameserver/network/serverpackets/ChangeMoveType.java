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

import l2server.gameserver.model.actor.L2Character;

/**
 * sample
 * <p>
 * 0000: 3e 2a 89 00 4c 01 00 00 00						 .|...
 * <p>
 * format   dd
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class ChangeMoveType extends L2GameServerPacket
{
	public static final int WALK = 0;
	public static final int RUN = 1;

	private int charObjId;
	private boolean running;

	public ChangeMoveType(L2Character character)
	{
		charObjId = character.getObjectId();
		running = character.isRunning();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(charObjId);
		writeD(running ? RUN : WALK);
		writeD(0); //c2
	}
}
