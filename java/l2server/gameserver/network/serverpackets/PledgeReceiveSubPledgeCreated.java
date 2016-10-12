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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Clan.SubPledge;
import l2server.gameserver.model.L2ClanMember;

/**
 * @author -Wooden-
 */
public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{

	private SubPledge _subPledge;
	private L2Clan _clan;

	/**
	 */
	public PledgeReceiveSubPledgeCreated(SubPledge subPledge, L2Clan clan)
	{
		_subPledge = subPledge;
		_clan = clan;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(0x01);
		writeD(_subPledge.getId());
		writeS(_subPledge.getName());
		writeS(getLeaderName());
	}

	private String getLeaderName()
	{
		int leaderId = _subPledge.getLeaderId();
		if (_subPledge.getId() == L2Clan.SUBUNIT_ACADEMY || leaderId == 0)
		{
			return "";
		}
		else if (_clan.getClanMember(leaderId) == null)
		{
			//Log.warning("SubPledgeLeader: "+ leaderId + " is missing from clan: "+ _clan.getName()+"["+_clan.getClanId()+"]");
			String name = "";
			for (L2ClanMember temp : _clan.getMembers())
			{
				if (temp.getPledgeType() == _subPledge.getId())
				{
					_subPledge.setLeaderId(temp.getObjectId());
					name = temp.getName();
				}
			}

			return name;
		}
		else
		{
			return _clan.getClanMember(leaderId).getName();
		}
	}
}
