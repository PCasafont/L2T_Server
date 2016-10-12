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

import l2server.gameserver.instancemanager.GraciaSeedsManager;

/**
 * format: 0xfe cd(dddd)
 * FE - packet id
 * A1 00 - packet subid
 * d - seed count
 * d - x pos
 * d - y pos
 * d - z pos
 * d - sys msg no
 */
public class ExShowSeedMapInfo extends L2GameServerPacket
{

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{ // Id
		writeH(0xa1); // SubId

		writeD(2); // seed count
		// Seed of Destruction
		writeD(-246857); // x coord
		writeD(251960); // y coord
		writeD(4331); // z coord
		writeD(2770 + GraciaSeedsManager.getInstance().getSoDState()); // sys msg id
		// Seed of Infinity
		writeD(-213770); // x coord
		writeD(210760); // y coord
		writeD(4400); // z coord
		// Manager not implemented yet
		writeD(2766); // sys msg id
	}
}
