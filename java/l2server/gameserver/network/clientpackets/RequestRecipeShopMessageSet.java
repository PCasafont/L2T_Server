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
import l2server.gameserver.network.serverpackets.RecipeShopMsg;
import l2server.gameserver.util.Util;

/**
 * This class ...
 * cS
 *
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	//

	private static final int MAX_MSG_LENGTH = 29;

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (_name != null && _name.length() > MAX_MSG_LENGTH)
		{
			Util.handleIllegalPlayerAction(player,
					"Player " + player.getName() + " tried to overflow recipe shop message", Config.DEFAULT_PUNISH);
			return;
		}

		if (player.getCreateList() != null)
		{
			player.getCreateList().setStoreName(_name);
		}

		player.sendPacket(new RecipeShopMsg(player));
	}
}
