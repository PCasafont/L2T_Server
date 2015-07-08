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

import l2tserver.gameserver.handler.IItemHandler;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ShowXMasSeal;

/**
 *
 * @author  devScarlet & mrTJO
 */
public class SpecialXMas implements IItemHandler
{
	/**
	 * 
	 * @see l2tserver.gameserver.handler.IItemHandler#useItem(l2tserver.gameserver.model.actor.L2Playable, l2tserver.gameserver.model.L2ItemInstance, boolean)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		playable.broadcastPacket(new ShowXMasSeal(item.getItemId()));
	}
}
