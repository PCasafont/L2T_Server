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

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;

/**
 * @author KenM
 */
public final class FlyToLocation extends L2GameServerPacket
{
	private final int destX, destY, destZ;
	private final int chaObjId, chaX, chaY, chaZ;
	private final FlyType type;

	public enum FlyType
	{
		THROW_UP, THROW_HORIZONTAL, DUMMY, // no effect
		CHARGE, KNOCK_BACK, MAGIC, UNK2, // Causes critical error
		KNOCK_DOWN, MOVE_HORIZONTAL, DRAG
	}

	public FlyToLocation(L2Character cha, int destX, int destY, int destZ, FlyType type)
	{
		chaObjId = cha.getObjectId();
		chaX = cha.getX();
		chaY = cha.getY();
		chaZ = cha.getZ();
		this.destX = destX;
		this.destY = destY;
		this.destZ = destZ;
		this.type = type;
	}

	public FlyToLocation(L2Character cha, L2Object dest, FlyType type)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
	}

    /*
	  @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
     */

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(chaObjId);
		writeD(destX);
		writeD(destY);
		writeD(destZ);
		writeD(chaX);
		writeD(chaY);
		writeD(chaZ);
		writeD(type.ordinal());
		writeD(0x00); // flySpeed?
		writeD(0x00); // flyDelay?
		writeD(333); // animationSpeed?
	}
}
