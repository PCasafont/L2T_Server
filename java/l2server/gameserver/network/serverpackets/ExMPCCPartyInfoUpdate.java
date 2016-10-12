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

import l2server.gameserver.model.L2Party;

/**
 * @author chris_00
 *         <p>
 *         ch Sddd
 */
public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{

	private L2Party _party;
	private int _mode, _LeaderOID, _memberCount;
	private String _name;

	/**
	 * @param party
	 * @param mode  0 = Remove, 1 = Add
	 */
	public ExMPCCPartyInfoUpdate(L2Party party, int mode)
	{
		_party = party;
		_name = _party.getLeader().getName();
		_LeaderOID = _party.getPartyLeaderOID();
		_memberCount = _party.getMemberCount();
		_mode = mode;
	}

	/**
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeS(_name);
		writeD(_LeaderOID);
		writeD(_memberCount);
		writeD(_mode); //mode 0 = Remove Party, 1 = AddParty, maybe more...
	}
}
