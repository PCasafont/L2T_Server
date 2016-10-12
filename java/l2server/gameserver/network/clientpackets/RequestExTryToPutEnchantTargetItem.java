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

import l2server.Config;
import l2server.gameserver.datatables.EnchantItemTable;
import l2server.gameserver.datatables.EnchantItemTable.EnchantScroll;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author KenM
 */
public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{

	private int _objectId = 0;

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	/**
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (_objectId == 0)
		{
			return;
		}

		if (activeChar == null)
		{
			return;
		}

		if (activeChar.isEnchanting())
		{
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();

		if (item == null || scroll == null)
		{
			return;
		}

		// template for scroll
		EnchantScroll scrollTemplate = EnchantItemTable.getInstance().getEnchantScroll(scroll);

		if (!scrollTemplate.isValid(item) || !EnchantItemTable.isEnchantable(item))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS));
			activeChar.setActiveEnchantItem(null);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
			return;
		}

		if (Config.ENCHANT_CHANCE_PER_LEVEL.length > 0)
		{
			int chance = Math.round(scrollTemplate.getChance(item, null) * 10);
			if (chance < 1000)
			{
				String chanceText = String.valueOf(chance / 10);
				if (chance % 10 > 0)
				{
					chanceText += "." + chance % 10;
				}
				activeChar.sendPacket(
						new ExShowScreenMessage("This enchantment has a " + chanceText + "% chance to succeed", 3000));
			}
		}

		activeChar.setIsEnchanting(true);
		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		activeChar.sendPacket(new ExPutEnchantTargetItemResult(1));
	}
}
