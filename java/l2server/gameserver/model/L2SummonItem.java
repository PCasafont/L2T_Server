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
 * @author -Nemesiss-
 */
public class L2SummonItem
{
	private final int _itemId;
	private final int _npcId;
	private final byte _type;
	private final int _despawnDelay;

	public L2SummonItem(int itemId, int npcId, byte type, int despawnDelay)
	{
		_itemId = itemId;
		_npcId = npcId;
		_type = type;
		_despawnDelay = despawnDelay;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public byte getType()
	{
		return _type;
	}

	public boolean isPetSummon()
	{
		return _type == 1 || _type == 2;
	}

	public int getDespawnDelay()
	{
		return _despawnDelay;
	}
}
