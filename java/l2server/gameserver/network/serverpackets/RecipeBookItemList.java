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

import l2server.gameserver.model.L2RecipeList;

/**
 * format   d d(dd)
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeBookItemList extends L2GameServerPacket
{
	private L2RecipeList[] _recipes;
	private boolean _isDwarvenCraft;
	private int _maxMp;

	public RecipeBookItemList(boolean isDwarvenCraft, int maxMp)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_maxMp = maxMp;
	}

	public void addRecipes(L2RecipeList[] recipeBook)
	{
		_recipes = recipeBook;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_isDwarvenCraft ? 0x00 : 0x01); //0 = Dwarven - 1 = Common
		writeD(_maxMp);

		if (_recipes == null)
		{
			writeD(0);
		}
		else
		{
			writeD(_recipes.length);//number of items in recipe book

			for (int i = 0; i < _recipes.length; i++)
			{
				L2RecipeList temp = _recipes[i];
				writeD(temp.getId());
				writeD(i + 1);
			}
		}
	}
}
