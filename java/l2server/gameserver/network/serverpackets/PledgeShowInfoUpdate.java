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

import l2server.Config;
import l2server.gameserver.model.L2Clan;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private L2Clan clan;

	public PledgeShowInfoUpdate(L2Clan clan)
	{
		this.clan = clan;
	}

	@Override
	protected final void writeImpl()
	{
		//dddddddddddSdd
		//sending empty data so client will ask all the info in response ;)
		writeD(this.clan.getClanId());
		writeD(Config.SERVER_ID);
		writeD(this.clan.getCrestId());
		writeD(this.clan.getLevel()); //clan level
		writeD(0); // GoD ???
		writeD(this.clan.getHasCastle());
		writeD(this.clan.getHasHideout());
		writeD(this.clan.getHasFort());
		writeD(this.clan.getRank());
		writeD(this.clan.getReputationScore()); // clan reputation score
		writeD(0);
		writeD(0);
		writeD(this.clan.getAllyId());
		writeS(this.clan.getAllyName()); //c5
		writeD(this.clan.getAllyCrestId()); //c5
		writeD(this.clan.isAtWar() ? 1 : 0); //c5
		writeD(0); // GoD ???
		writeD(0); // GoD ???
	}
}
