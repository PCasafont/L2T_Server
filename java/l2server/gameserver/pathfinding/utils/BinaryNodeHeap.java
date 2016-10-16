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

package l2server.gameserver.pathfinding.utils;

import l2server.gameserver.pathfinding.geonodes.GeoNode;

/**
 * @author -Nemesiss-
 */
public class BinaryNodeHeap
{
	private final GeoNode[] list;
	private int size;

	public BinaryNodeHeap(int size)
	{
		this.list = new GeoNode[size + 1];
		this.size = 0;
	}

	public void add(GeoNode n)
	{
		this.size++;
		int pos = this.size;
		this.list[pos] = n;
		while (pos != 1)
		{
			int p2 = pos / 2;
			if (this.list[pos].getCost() <= this.list[p2].getCost())
			{
				GeoNode temp = this.list[p2];
				this.list[p2] = this.list[pos];
				this.list[pos] = temp;
				pos = p2;
			}
			else
			{
				break;
			}
		}
	}

	public GeoNode removeFirst()
	{
		GeoNode first = this.list[1];
		this.list[1] = this.list[this.size];
		this.list[this.size] = null;
		this.size--;
		int pos = 1;
		int cpos;
		int dblcpos;
		GeoNode temp;
		while (true)
		{
			cpos = pos;
			dblcpos = cpos * 2;
			if (dblcpos + 1 <= this.size)
			{
				if (this.list[cpos].getCost() >= this.list[dblcpos].getCost())
				{
					pos = dblcpos;
				}
				if (this.list[pos].getCost() >= this.list[dblcpos + 1].getCost())
				{
					pos = dblcpos + 1;
				}
			}
			else if (dblcpos <= this.size)
			{
				if (this.list[cpos].getCost() >= this.list[dblcpos].getCost())
				{
					pos = dblcpos;
				}
			}

			if (cpos != pos)
			{
				temp = this.list[cpos];
				this.list[cpos] = this.list[pos];
				this.list[pos] = temp;
			}
			else
			{
				break;
			}
		}
		return first;
	}

	public boolean contains(GeoNode n)
	{
		if (this.size == 0)
		{
			return false;
		}
		for (int i = 1; i <= this.size; i++)
		{
			if (this.list[i].equals(n))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty()
	{
		return this.size == 0;
	}
}
