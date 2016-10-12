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

import l2server.gameserver.datatables.LifeStoneTable;
import l2server.gameserver.datatables.LifeStoneTable.LifeStone;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * Fromat(ch) dd
 *
 * @author -Wooden-
 */
public class RequestConfirmRefinerItem extends L2GameClientPacket
{

	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		if (targetItem == null)
		{
			return;
		}

		final L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if (refinerItem == null)
		{
			return;
		}

		if (!LifeStoneTable.getInstance().isValid(activeChar, targetItem, refinerItem))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM));
			return;
		}

		final int refinerItemId = refinerItem.getItem().getItemId();
		final int grade = targetItem.getItem().getItemGrade();
		final LifeStone ls = LifeStoneTable.getInstance().getLifeStone(refinerItemId);
		final int gemStoneId = LifeStoneTable.getGemStoneId(grade, ls.getGrade());
		final int gemStoneCount = LifeStoneTable.getGemStoneCount(grade, ls.getGrade());

		activeChar.sendPacket(
				new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemStoneId, gemStoneCount));
	}
}
