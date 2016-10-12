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

import l2server.gameserver.datatables.BeautyTable;
import l2server.gameserver.datatables.BeautyTable.BeautyInfo;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExResponseBeautyRegistPacket;
import l2server.gameserver.network.serverpackets.SocialAction;

/**
 * @author Pere
 */

public final class RequestRegistBeauty extends L2GameClientPacket
{
	private int _hair;
	private int _face;
	private int _hairColor;

	@Override
	protected final void readImpl()
	{
		_hair = readD();
		_face = readD();
		_hairColor = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		//Get the price
		BeautyInfo styleInfo = null;

		if (_hair > 0)
		{
			styleInfo = BeautyTable.getInstance().getTemplate(0).getHairStyles().get(_hair);
		}
		else if (_face > 0)
		{
			styleInfo = BeautyTable.getInstance().getTemplate(0).getFaceStyles().get(_face);
		}

		if (styleInfo != null)
		{
			if (_face == -1)
			{
				_face = activeChar.getAppearance().getFace();
			}

			if (_hair == -1)
			{
				_hair = activeChar.getAppearance().getHairStyle();
			}

			if (_hairColor == -1)
			{
				_hairColor = activeChar.getAppearance().getHairColor();
			}

			/*L2ItemInstance playerTickets = activeChar.getInventory().getItemByItemId(36308);

			int adenaPrice = styleInfo.getAdenaPrice();

			int ticketPrice = styleInfo.getTicketPrice();

			if ((adenaPrice > 0 && activeChar.getAdena() < adenaPrice) || (ticketPrice > 0 && (playerTickets == null || playerTickets.getCount() < ticketPrice)))
			{
				activeChar.sendPacket(new ExResponseBeautyRegistPacket(activeChar.getAdena(), activeChar.getInventory().getInventoryItemCount(36308, 0), 0, activeChar.getAppearance().getHairStyle(), activeChar.getAppearance().getFace(), activeChar.getAppearance().getHairColor()));

				activeChar.sendPacket(new ExShowBeautyList(activeChar.getAdena(), activeChar.getInventory().getInventoryItemCount(36308, 0), false));
				return;
			}

			if (adenaPrice > 0)
			{
				activeChar.reduceAdena("Beauty shop", adenaPrice, activeChar, true);
			}

			if (ticketPrice > 0)
			{
				activeChar.destroyItemByItemId("Beauty shop", 36308, ticketPrice, activeChar, true);
			}*/

			activeChar.getAppearance().setHairStyle(_hair);

			activeChar.getAppearance().setFace(_face);

			activeChar.getAppearance().setHairColor(_hairColor);

			activeChar.sendPacket(new ExResponseBeautyRegistPacket(activeChar.getAdena(),
					activeChar.getInventory().getInventoryItemCount(36308, 0), 1, _hair, _face, _hairColor));

			//activeChar.sendPacket(new ExResponseBeautyListPacket());

			//activeChar.sendPacket(new ExShowBeautyList(activeChar.getAdena(), activeChar.getInventory().getInventoryItemCount(36308, 0), false));

			activeChar.broadcastUserInfo();

			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 30));

			//Log.info("INFO: Hair: " + _hair + ", Face: " + _face + ", COLOR: " + _hairColor);
			//Log.info("END OK");
		}
	}
}
