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

package l2server.gameserver.events.instanced;

import l2server.util.Rnd;
import l2server.util.xml.XmlNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public abstract class EventPrize
{
	protected final float _chance;
	protected final boolean _dependsOnPerformance;

	public EventPrize(XmlNode node)
	{
		_chance = node.getFloat("chance");
		_dependsOnPerformance = node.getBool("dependsOnPerformance", false);
	}

	public abstract EventPrizeItem getItem();

	public static class EventPrizeItem extends EventPrize
	{
		private final int _id;
		private final int _min;
		private final int _max;

		public EventPrizeItem(XmlNode node)
		{
			super(node);
			_id = node.getInt("id");
			_min = node.getInt("min");
			_max = node.getInt("max");
		}

		public int getId()
		{
			return _id;
		}

		public int getMin()
		{
			return _min;
		}

		public int getMax()
		{
			return _max;
		}

		@Override
		public EventPrizeItem getItem()
		{
			return this;
		}
	}

	public static class EventPrizeCategory extends EventPrize
	{
		private final List<EventPrizeItem> _items = new ArrayList<>();

		public EventPrizeCategory(XmlNode node)
		{
			super(node);
			for (XmlNode subNode : node.getChildren())
			{
				if (subNode.getName().equalsIgnoreCase("item"))
				{
					_items.add(new EventPrizeItem(subNode));
				}
			}
		}

		@Override
		public EventPrizeItem getItem()
		{
			float rnd = Rnd.get(100000) / 1000.0f;
			float percent = 0.0f;
			for (EventPrizeItem item : _items)
			{
				percent += item.getChance();
				if (percent > rnd)
				{
					return item;
				}
			}

			return _items.get(0);
		}
	}

	public float getChance()
	{
		return _chance;
	}

	public boolean dependsOnPerformance()
	{
		return _dependsOnPerformance;
	}
}
