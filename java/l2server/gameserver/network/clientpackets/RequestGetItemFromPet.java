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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.util.Util;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestGetItemFromPet extends L2GameClientPacket
{

	private int _objectId;
	private long _amount;
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
		_unknown = readD();// = 0 for most trades
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("getfrompet"))
		{
			player.sendMessage("You get items from pet too fast.");
			return;
		}

		L2PetInstance pet = player.getPet();
		if (player.getActiveEnchantItem() != null)
		{
			return;
		}
		if (_amount < 0)
		{
			Util.handleIllegalPlayerAction(player,
					"[RequestGetItemFromPet] Character " + player.getName() + " of account " + player.getAccountName() +
							" tried to get item with oid " + _objectId + " from pet but has count < 0!",
					Config.DEFAULT_PUNISH);
			return;
		}
		else if (_amount == 0)
		{
			return;
		}

		if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			Log.warning("Invalid item transfer request: " + pet.getName() + " (pet) --> " + player.getName());
		}
	}
}
