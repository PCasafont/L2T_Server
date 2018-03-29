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
		list = new GeoNode[size + 1];
		size = 0;
	}

	public void add(GeoNode n)
	{
		size++;
		int pos = size;
		list[pos] = n;
		while (pos != 1)
		{
			int p2 = pos / 2;
			if (list[pos].getCost() <= list[p2].getCost())
			{
				GeoNode temp = list[p2];
				list[p2] = list[pos];
				list[pos] = temp;
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
		GeoNode first = list[1];
		list[1] = list[size];
		list[size] = null;
		size--;
		int pos = 1;
		int cpos;
		int dblcpos;
		GeoNode temp;
		while (true)
		{
			cpos = pos;
			dblcpos = cpos * 2;
			if (dblcpos + 1 <= size)
			{
				if (list[cpos].getCost() >= list[dblcpos].getCost())
				{
					pos = dblcpos;
				}
				if (list[pos].getCost() >= list[dblcpos + 1].getCost())
				{
					pos = dblcpos + 1;
				}
			}
			else if (dblcpos <= size)
			{
				if (list[cpos].getCost() >= list[dblcpos].getCost())
				{
					pos = dblcpos;
				}
			}

			if (cpos != pos)
			{
				temp = list[cpos];
				list[cpos] = list[pos];
				list[pos] = temp;
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
		if (size == 0)
		{
			return false;
		}
		for (int i = 1; i <= size; i++)
		{
			if (list[i].equals(n))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty()
	{
		return size == 0;
	}
}
