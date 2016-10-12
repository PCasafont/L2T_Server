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

import l2server.Config;
import l2server.gameserver.RecipeController;
import l2server.gameserver.model.L2RecipeList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

/**
 * format   dddd
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RecipeItemMakeInfo extends L2GameServerPacket
{

	private int _id;
	private L2PcInstance _activeChar;
	private boolean _success;

	public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
	{
		_id = id;
		_activeChar = player;
		_success = success;
	}

	public RecipeItemMakeInfo(int id, L2PcInstance player)
	{
		_id = id;
		_activeChar = player;
		_success = true;
	}

	@Override
	protected final void writeImpl()
	{
		L2RecipeList recipe = RecipeController.getInstance().getRecipeList(_id);

		if (recipe != null)
		{
			writeD(_id);
			writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common
			writeD((int) _activeChar.getCurrentMp());
			writeD(_activeChar.getMaxMp());
			writeD(_success ? 1 : 0); // item creation success/failed
		}
		else if (Config.DEBUG)
		{
			Log.info("No recipe found with ID = " + _id);
		}
	}
}
