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
/*
  coded by Balancer
  balancer@balancer.ru
  http://balancer.ru
  <p>
  version 0.1, 2005-03-12
 */

package l2server.gameserver.model;

import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class L2Territory {
	private static Logger log = LoggerFactory.getLogger(L2Territory.class.getName());


	public static class Point {
		public int x, y, zmin, zmax, proc;

		Point(int x, int y, int zmin, int zmax, int proc) {
			this.x = x;
			this.y = y;
			this.zmin = zmin;
			this.zmax = zmax;
			this.proc = proc;
		}
	}

	private List<Point> points;
	private int terr;
	private int xMin;
	private int xMax;
	private int yMin;
	private int yMax;
	private int zMin;
	private int zMax;
	private int procMax;

	public L2Territory(int terr) {
		points = new ArrayList<>();
		this.terr = terr;
		xMin = 999999;
		xMax = -999999;
		yMin = 999999;
		yMax = -999999;
		zMin = 999999;
		zMax = -999999;
		procMax = 0;
	}

	public void add(int x, int y, int zmin, int zmax, int proc) {
		points.add(new Point(x, y, zmin, zmax, proc));
		if (x < xMin) {
			xMin = x;
		}
		if (y < yMin) {
			yMin = y;
		}
		if (x > xMax) {
			xMax = x;
		}
		if (y > yMax) {
			yMax = y;
		}
		if (zmin < zMin) {
			this.zMin = zmin;
		}
		if (zmax > zMax) {
			this.zMax = zmax;
		}
		procMax += proc;
	}

	public void print() {
		for (Point p : points) {
			log.info("(" + p.x + "," + p.y + ")");
		}
	}

	public boolean isIntersect(int x, int y, Point p1, Point p2) {
		double dy1 = p1.y - y;
		double dy2 = p2.y - y;

		if (Math.signum(dy1) == Math.signum(dy2)) {
			return false;
		}

		double dx1 = p1.x - x;
		double dx2 = p2.x - x;

		if (dx1 >= 0 && dx2 >= 0) {
			return true;
		}

		if (dx1 < 0 && dx2 < 0) {
			return false;
		}

		double dx0 = dy1 * (p1.x - p2.x) / (p1.y - p2.y);

		return dx0 <= dx1;
	}

	public boolean isInside(int x, int y) {
		int intersect_count = 0;
		for (int i = 0; i < points.size(); i++) {
			Point p1 = points.get(i > 0 ? i - 1 : points.size() - 1);
			Point p2 = points.get(i);

			if (isIntersect(x, y, p1, p2)) {
				intersect_count++;
			}
		}

		return intersect_count % 2 == 1;
	}

	public int[] getRandomPoint() {
		int[] p = new int[4];
		if (procMax > 0) {
			int pos = 0;
			int rnd = Rnd.nextInt(procMax);
			for (Point p1 : points) {
				pos += p1.proc;
				if (rnd <= pos) {
					p[0] = p1.x;
					p[1] = p1.y;
					p[2] = p1.zmin;
					p[3] = p1.zmax;
					return p;
				}
			}
		}
		for (int i = 0; i < 100; i++) {
			p[0] = Rnd.get(xMin, xMax);
			p[1] = Rnd.get(yMin, yMax);
			if (isInside(p[0], p[1])) {
				double curdistance = 0;
				p[2] = zMin + 100;
				p[3] = zMax;
				for (Point p1 : points) {
					double dx = p1.x - p[0];
					double dy = p1.y - p[1];
					double distance = Math.sqrt(dx * dx + dy * dy);
					if (curdistance == 0 || distance < curdistance) {
						curdistance = distance;
						p[2] = p1.zmin + 100;
					}
				}
				return p;
			}
		}
		log.warn("Can't make point for territory " + terr);
		return p;
	}

	public int getProcMax() {
		return procMax;
	}

	public int getMinZ() {
		return zMin;
	}

	public int getMaxZ() {
		return zMax;
	}

	public List<Point> getPoints() {
		return points;
	}
}
