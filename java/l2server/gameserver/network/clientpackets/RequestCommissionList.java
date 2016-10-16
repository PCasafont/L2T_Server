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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Erlandys
 */
public final class RequestCommissionList extends L2GameClientPacket
{
	@SuppressWarnings("unused") private long category;
	@SuppressWarnings("unused") private int type;
	@SuppressWarnings("unused") private int grade;
	@SuppressWarnings("unused") private String searchName;

	@Override
	protected void readImpl()
	{
		category = readQ();
		type = readD();
		grade = readD();
		searchName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
		}

		/*AuctionManager am = AuctionManager.getInstance();

		if (this.category == 0)
			this.category = 100;
		else if (this.category == 1)
			this.category = 101;

		if (this.category != 101 && this.category != 100 && this.category % 10000 != 7297 && this.category % 10000 != 4593 && this.category % 10000 != 1889 &&
				this.category % 10000 != 9185 && this.category % 10000 != 6481)
			this.category = am.convertCategory((int)(_category/1000));
		else if (this.category != 101 && this.category != 100)
			this.category = am.convertMassCategory((int)(_category/1000));

		if (this.category > 60 && this.category < 66 || this.category == 101)
		{
			if (am.getAuctionsSizeById(this.category, this.grade, this.searchName) > 999)
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT);
			else if (am.getAuctionsSizeById(this.category, this.grade, this.searchName) <= 0)
				activeChar.sendPacket(SystemMessageId.CURRENTLY_THERE_ARE_NO_REGISTERED_ITEMS);
		}
		else if (this.category == 100)
			if (am.getAuctionsSizeById(this.grade, this.searchName) > 999)
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT);

		am.checkForAuctionsDeletion();
		activeChar.sendPacket(new ExResponseCommissionList(activeChar, this.category, this.type, this.grade, this.searchName));*/
	}
}
