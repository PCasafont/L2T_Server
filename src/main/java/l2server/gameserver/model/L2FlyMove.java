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

import java.util.HashMap; import java.util.Map;
import l2server.util.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class L2FlyMove {
	public enum L2FlyMoveType {
		START,
		CHOOSE,
		MOVE
	}

	private int id;
	private Map<Integer, Point3D> steps = new HashMap<>();
	private Map<Integer, L2FlyMoveChoose> chooses = new HashMap<>();

	public L2FlyMove(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void addStep(int id, Point3D s) {
		steps.put(id, s);
	}

	public Point3D getStep(int id) {
		return steps.get(id);
	}

	public void addChoose(int id, L2FlyMoveChoose c) {
		chooses.put(id, c);
	}

	public L2FlyMoveChoose getChoose(int id) {
		return chooses.get(id);
	}

	public class L2FlyMoveChoose {
		private int at;
		private List<L2FlyMoveOption> options = new ArrayList<>();

		public L2FlyMoveChoose(int at) {
			this.at = at;
		}

		public int getAt() {
			return at;
		}

		public void addOption(L2FlyMoveOption o) {
			options.add(o);
		}

		public List<L2FlyMoveOption> getOptions() {
			return options;
		}
	}

	public class L2FlyMoveOption {
		private int start;
		private int end;
		private int last;

		public L2FlyMoveOption(int start, int end, int last) {
			this.start = start;
			this.end = end;
			this.last = last;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public int getLast() {
			return last;
		}
	}
}
