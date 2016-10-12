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

import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.EnchantItemTable.EnchantSupportItem;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExEnchantItemAllowed;
import l2server.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author KenM
 */
public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{

	private int _supportObjectId;
	private int _enchantObjectId;

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		_supportObjectId = readD();
		_enchantObjectId = readD();
	}

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar != null)
		{
			if (activeChar.isEnchanting())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_enchantObjectId);
				L2ItemInstance support = activeChar.getInventory().getItemByObjectId(_supportObjectId);

				if (item == null || support == null)
				{
					return;
				}

				EnchantSupportItem supportTemplate = EnchantItemTable.getInstance().getSupportItem(support);

				if (supportTemplate == null || !supportTemplate.isValid(item))
				{
					// message may be custom
					activeChar.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
					activeChar.setActiveEnchantSupportItem(null);
					activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
					return;
				}
				activeChar.setActiveEnchantSupportItem(support);

				activeChar.setIsEnchanting(true);
				activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
				activeChar.sendPacket(new ExPutEnchantSupportItemResult(_supportObjectId));
				activeChar.sendPacket(new ExEnchantItemAllowed());
			}
		}
	}
}
