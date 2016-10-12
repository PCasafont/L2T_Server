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

import l2server.gameserver.model.L2CommandChannel;
import l2server.gameserver.model.L2Party;

/**
 * @author chris_00
 *         ch sdd d[sdd]
 */
public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private L2CommandChannel _channel;

	public ExMultiPartyCommandChannelInfo(L2CommandChannel channel)
	{
		_channel = channel;
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
		if (_channel == null)
		{
			return;
		}

		// L2PcInstance player = this.getClient().getActiveChar();

		writeS(_channel.getChannelLeader().getName()); // Channelowner
		writeD(0); // Channelloot 0 or 1
		writeD(_channel.getMemberCount());

		writeD(_channel.getPartys().size());
		for (L2Party p : _channel.getPartys())
		{
			writeS(p.getLeader().getName()); // Leadername
			writeD(p.getPartyLeaderOID()); // Leaders ObjId
			writeD(p.getMemberCount()); // Membercount
		}
	}
}
