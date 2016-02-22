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

package handlers.itemhandlers;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExShowItemAppearanceWindow;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;

public class AppearanceStone implements IItemHandler
{
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		if (activeChar.isCastingNow())
			return;
		
		activeChar.setActiveAppearanceStone(item);
		
		if (item.getName().contains("Restor"))
		{
			activeChar.sendPacket(new ExShowScreenMessage("Double click on the item from which you want to remove the custom appearance", 3000));
		}
		else if (item.getItem().getStandardItem() > 0)
		{
			activeChar.sendPacket(new ExShowScreenMessage("Double click on the item on which you want to add the custom appearance", 3000));
		}
		else
		{
			activeChar.sendPacket(new ExShowItemAppearanceWindow(item.getStoneType(), item.getItemId()));
		}
	}
}
