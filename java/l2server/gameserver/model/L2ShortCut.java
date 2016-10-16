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

package l2server.gameserver.model;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2ShortCut
{
	public static final int TYPE_ITEM = 1;
	public static final int TYPE_SKILL = 2;
	public static final int TYPE_ACTION = 3;
	public static final int TYPE_MACRO = 4;
	public static final int TYPE_RECIPE = 5;
	public static final int TYPE_TPBOOKMARK = 6;

	private final int slot;
	private final int page;
	private final int type;
	private final int id;
	private final int level;
	private final int characterType;
	private int sharedReuseGroup = -1;

	public L2ShortCut(int slotId, int pageId, int shortcutType, int shortcutId, int shortcutLevel, int characterType)
	{
		this.slot = slotId;
		this.page = pageId;
		this.type = shortcutType;
		this.id = shortcutId;
		this.level = shortcutLevel;
		this.characterType = characterType;
	}

	public int getId()
	{
		return this.id;
	}

	public int getLevel()
	{
		return this.level;
	}

	public int getPage()
	{
		return this.page;
	}

	public int getSlot()
	{
		return this.slot;
	}

	public int getType()
	{
		return this.type;
	}

	public int getCharacterType()
	{
		return this.characterType;
	}

	public int getSharedReuseGroup()
	{
		return this.sharedReuseGroup;
	}

	public void setSharedReuseGroup(int g)
	{
		this.sharedReuseGroup = g;
	}
}
