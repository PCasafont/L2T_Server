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

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Erlandys
 */
public class ExRegistWaitingSubstituteOk extends L2GameServerPacket
{

	int classId;
	L2PcInstance player;

	public ExRegistWaitingSubstituteOk(int classId, L2PcInstance player)
	{
		this.classId = classId;
		this.player = player.getParty().getLeader();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00); // TODO: Unknown
		writeC(0x00); // TODO: Unknown
		writeD(0x00); // TODO: Region Id
		writeC(0x00); // TODO: Unknown
		writeC(0x00); // TODO: Unknown
		writeD(0x00); // TODO: Unknown
		writeD(player.getInstanceId() > 0 ? 0x01 : 0x00);
		writeD(classId);
	}
}
