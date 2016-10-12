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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * ddddd
 *
 * @version $Revision: 1.1.2.3.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeShopItemInfo extends L2GameServerPacket
{

	private L2PcInstance _player;
	private int _recipeId;

	public RecipeShopItemInfo(L2PcInstance player, int recipeId)
	{
		_player = player;
		_recipeId = recipeId;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.getObjectId());
		writeD(_recipeId);
		writeD((int) _player.getCurrentMp());
		writeD(_player.getMaxMp());
		writeD(0xffffffff);
	}
}
