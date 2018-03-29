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

	@SuppressWarnings("unused")
	private long category;
	@SuppressWarnings("unused")
	private int type;
	@SuppressWarnings("unused")
	private int grade;
	@SuppressWarnings("unused")
	private String searchName;

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

		if (category == 0)
			category = 100;
		else if (category == 1)
			category = 101;

		if (category != 101 && category != 100 && category % 10000 != 7297 && category % 10000 != 4593 && category % 10000 != 1889 &&
				category % 10000 != 9185 && category % 10000 != 6481)
			category = am.convertCategory((int)(category/1000));
		else if (category != 101 && category != 100)
			category = am.convertMassCategory((int)(category/1000));

		if (category > 60 && category < 66 || category == 101)
		{
			if (am.getAuctionsSizeById(category, grade, searchName) > 999)
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT);
			else if (am.getAuctionsSizeById(category, grade, searchName) <= 0)
				activeChar.sendPacket(SystemMessageId.CURRENTLY_THERE_ARE_NO_REGISTERED_ITEMS);
		}
		else if (category == 100)
			if (am.getAuctionsSizeById(grade, searchName) > 999)
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT);

		am.checkForAuctionsDeletion();
		activeChar.sendPacket(new ExResponseCommissionList(activeChar, category, type, grade, searchName));*/
	}
}
