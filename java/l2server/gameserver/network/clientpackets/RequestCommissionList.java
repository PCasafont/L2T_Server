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
	private long _category;
	@SuppressWarnings("unused")
	private int _type;
	@SuppressWarnings("unused")
	private int _grade;
	@SuppressWarnings("unused")
	private String _searchName;

	@Override
	protected void readImpl()
	{
		_category = readQ();
		_type = readD();
		_grade = readD();
		_searchName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
		}

		/*AuctionManager am = AuctionManager.getInstance();

		if (_category == 0)
			_category = 100;
		else if (_category == 1)
			_category = 101;

		if (_category != 101 && _category != 100 && _category % 10000 != 7297 && _category % 10000 != 4593 && _category % 10000 != 1889 &&
				_category % 10000 != 9185 && _category % 10000 != 6481)
			_category = am.convertCategory((int)(_category/1000));
		else if (_category != 101 && _category != 100)
			_category = am.convertMassCategory((int)(_category/1000));

		if (_category > 60 && _category < 66 || _category == 101)
		{
			if (am.getAuctionsSizeById(_category, _grade, _searchName) > 999)
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT);
			else if (am.getAuctionsSizeById(_category, _grade, _searchName) <= 0)
				activeChar.sendPacket(SystemMessageId.CURRENTLY_THERE_ARE_NO_REGISTERED_ITEMS);
		}
		else if (_category == 100)
			if (am.getAuctionsSizeById(_grade, _searchName) > 999)
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT);

		am.checkForAuctionsDeletion();
		activeChar.sendPacket(new ExResponseCommissionList(activeChar, _category, _type, _grade, _searchName));*/
	}
}
