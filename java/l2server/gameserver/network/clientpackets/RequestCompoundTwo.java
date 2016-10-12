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

import l2server.gameserver.datatables.CompoundTable;
import l2server.gameserver.datatables.CompoundTable.Combination;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExCompoundTwoFail;
import l2server.gameserver.network.serverpackets.ExCompoundTwoOK;

/**
 * @author Pere
 */
public final class RequestCompoundTwo extends L2GameClientPacket
{
	private int _objId;

	@Override
	protected void readImpl()
	{
		_objId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2ItemInstance compoundItem = activeChar.getInventory().getItemByObjectId(_objId);
		if (compoundItem == null)
		{
			sendPacket(new ExCompoundTwoFail());
			return;
		}

		if (activeChar.getCompoundItem1() == null || activeChar.getCompoundItem1() == compoundItem)
		{
			sendPacket(new ExCompoundTwoFail());
			return;
		}

		if (!CompoundTable.getInstance().isCombinable(compoundItem.getItemId()))
		{
			sendPacket(new ExCompoundTwoFail());
			return;
		}

		Combination combination = CompoundTable.getInstance()
				.getCombination(activeChar.getCompoundItem1().getItemId(), compoundItem.getItemId());
		if (combination == null)
		{
			sendPacket(new ExCompoundTwoFail());
			return;
		}

		activeChar.setCompoundItem2(compoundItem);
		sendPacket(new ExCompoundTwoOK());
	}
}
