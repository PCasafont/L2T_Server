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

import l2server.gameserver.model.L2ManufactureList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.RecipeShopManageList;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRecipeShopManageList extends L2GameClientPacket
{
	//

	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getPrivateStoreType() != 0)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			if (player.isSitting())
			{
				player.standUp();
			}
		}
		if (player.getCreateList() == null)
		{
			player.setCreateList(new L2ManufactureList());
		}

		player.sendPacket(new RecipeShopManageList(player, true));

		/*
		int privatetype=player.getPrivateStoreType();
		if (privatetype == 0)
		{
			if (player.getWaitType() !=1)
			{
				player.setWaitType(1);
				player.sendPacket(new ChangeWaitType (player,1));
				player.broadcastPacket(new ChangeWaitType (player,1));
			}

			if (player.getTradeList() == null)
			{
				player.setTradeList(new L2TradeList(0));
			}
			if (player.getSellList() == null)
			{
				player.setSellList(new ArrayList());
			}
			player.getTradeList().updateSellList(player,player.getSellList());
			player.setPrivateStoreType(2);
			player.sendPacket(new PrivateSellListSell(client.getActiveChar()));
			player.sendPacket(new ExUserInfo(player));
			player.broadcastPacket(new ExUserInfo(player));

		}

		if (privatetype == 1)
		{
			player.setPrivateStoreType(2);
			player.sendPacket(new PrivateSellListSell(client.getActiveChar()));
			player.sendPacket(new ChangeWaitType (player,1));
			player.broadcastPacket(new ChangeWaitType (player,1));

		}*/

	}
}
