/*
 * Copyright (C) 2004-2014 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.L2ColosseumFence;

/**
 * OP: 0xFE<br>
 * OP2: 0x0003<br>
 * Format: ddddddd<br>
 * - d: object id<br>
 * - d: state(0=hidden, 1=unconnected corners, 2=connected corners)<br>
 * - d: x<br>
 * - d: y<br>
 * - d: z<br>
 * - d: a side length<br>
 * - d: b side length<br>
 *
 * @author FBIagent
 */
public class ExColosseumFenceInfo extends L2GameServerPacket
{
	private final L2ColosseumFence _fence;

	public ExColosseumFenceInfo(L2ColosseumFence fence)
	{
		_fence = fence;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0003);

		writeD(_fence.getObjectId());
		writeD(_fence.getFenceState().ordinal());
		writeD(_fence.getX());
		writeD(_fence.getY());
		writeD(_fence.getZ());
		writeD(_fence.getFenceWidth());
		writeD(_fence.getFenceHeight());
	}
}
