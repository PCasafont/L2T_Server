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

import gnu.trove.TIntObjectHashMap;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class L2FlyMove
{
	public enum L2FlyMoveType
	{
		START, CHOOSE, MOVE
	}

	private int _id;
	private TIntObjectHashMap<Point3D> _steps = new TIntObjectHashMap<>();
	private TIntObjectHashMap<L2FlyMoveChoose> _chooses = new TIntObjectHashMap<>();

	public L2FlyMove(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public void addStep(int id, Point3D s)
	{
		_steps.put(id, s);
	}

	public Point3D getStep(int id)
	{
		return _steps.get(id);
	}

	public void addChoose(int id, L2FlyMoveChoose c)
	{
		_chooses.put(id, c);
	}

	public L2FlyMoveChoose getChoose(int id)
	{
		return _chooses.get(id);
	}

	public class L2FlyMoveChoose
	{
		private int _at;
		private List<L2FlyMoveOption> _options = new ArrayList<>();

		public L2FlyMoveChoose(int at)
		{
			_at = at;
		}

		public int getAt()
		{
			return _at;
		}

		public void addOption(L2FlyMoveOption o)
		{
			_options.add(o);
		}

		public List<L2FlyMoveOption> getOptions()
		{
			return _options;
		}
	}

	public class L2FlyMoveOption
	{
		private int _start;
		private int _end;
		private int _last;

		public L2FlyMoveOption(int start, int end, int last)
		{
			_start = start;
			_end = end;
			_last = last;
		}

		public int getStart()
		{
			return _start;
		}

		public int getEnd()
		{
			return _end;
		}

		public int getLast()
		{
			return _last;
		}
	}
}
