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

package l2server.gameserver.model.zone.form;

import l2server.gameserver.GeoEngine;
import l2server.gameserver.model.zone.L2ZoneForm;
import l2server.gameserver.network.serverpackets.ExServerPrimitive;
import l2server.util.Rnd;

import java.awt.*;

/**
 * A primitive rectangular zone
 *
 * @author durgus
 */
public class ZoneCuboid extends L2ZoneForm
{
	private int x1, x2, y1, y2, z1, z2;

	public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2)
	{
		this.x1 = x1;
		this.x2 = x2;
		if (this.x1 > x2) // switch them if alignment is wrong
		{
			this.x1 = x2;
			this.x2 = x1;
		}

		this.y1 = y1;
		this.y2 = y2;
		if (this.y1 > y2) // switch them if alignment is wrong
		{
			this.y1 = y2;
			this.y2 = y1;
		}

		this.z1 = z1;
		this.z2 = z2;
		if (this.z1 > z2) // switch them if alignment is wrong
		{
			this.z1 = z2;
			this.z2 = z1;
		}
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return !(x < x1 || x > x2 || y < y1 || y > y2 || z < z1 || z > z2);
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		// Check if any point inside this rectangle
		if (isInsideZone(ax1, ay1, z2 - 1))
		{
			return true;
		}
		if (isInsideZone(ax1, ay2, z2 - 1))
		{
			return true;
		}
		if (isInsideZone(ax2, ay1, z2 - 1))
		{
			return true;
		}
		if (isInsideZone(ax2, ay2, z2 - 1))
		{
			return true;
		}

		// Check if any point from this rectangle is inside the other one
		if (x1 > ax1 && x1 < ax2 && y1 > ay1 && y1 < ay2)
		{
			return true;
		}
		if (x1 > ax1 && x1 < ax2 && y2 > ay1 && y2 < ay2)
		{
			return true;
		}
		if (x2 > ax1 && x2 < ax2 && y1 > ay1 && y1 < ay2)
		{
			return true;
		}
		if (x2 > ax1 && x2 < ax2 && y2 > ay1 && y2 < ay2)
		{
			return true;
		}

		// Horizontal lines may intersect vertical lines
		if (lineSegmentsIntersect(x1, y1, x2, y1, ax1, ay1, ax1, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(x1, y1, x2, y1, ax2, ay1, ax2, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(x1, y2, x2, y2, ax1, ay1, ax1, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(x1, y2, x2, y2, ax2, ay1, ax2, ay2))
		{
			return true;
		}

		// Vertical lines may intersect horizontal lines
		if (lineSegmentsIntersect(x1, y1, x1, y2, ax1, ay1, ax2, ay1))
		{
			return true;
		}
		if (lineSegmentsIntersect(x1, y1, x1, y2, ax1, ay2, ax2, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(x2, y1, x2, y2, ax1, ay1, ax2, ay1))
		{
			return true;
		}
		return lineSegmentsIntersect(x2, y1, x2, y2, ax1, ay2, ax2, ay2);
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test, shortestDist = Math.pow(x1 - x, 2) + Math.pow(y1 - y, 2);

		test = Math.pow(x1 - x, 2) + Math.pow(y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}

		test = Math.pow(x2 - x, 2) + Math.pow(y1 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}

		test = Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}

		return Math.sqrt(shortestDist);
	}

	/* getLowZ() / getHighZ() - These two functions were added to cope with the demand of the new
	 * fishing algorithms, wich are now able to correctly place the hook in the water, thanks to getHighZ().
	 * getLowZ() was added, considering potential future modifications.
	 */
	@Override
	public int getLowZ()
	{
		return z1;
	}

	@Override
	public int getHighZ()
	{
		return z2;
	}

	@Override
	public int getCenterX()
	{
		return x1 + (x2 - x1) / 2;
	}

	@Override
	public int getCenterY()
	{
		return y1 + (y2 - y1) / 2;
	}

	@Override
	public void visualizeZone(ExServerPrimitive packet, String name, int z)
	{
		if (z < z1 + 100)
		{
			z = z1 + 100;
		}
		if (z > z2 - 20)
		{
			z = z2 - 20;
		}

		Color color = new Color(Rnd.get(2), Rnd.get(2), Rnd.get(2));
		packet.setXYZ((x1 + x2) / 2, (y1 + y2) / 2, z);
		packet.addPoint(name, color, true, (x1 + x2) / 2, (y1 + y2) / 2, z);

		packet.addLine(color, x1, y1, z, x2, y1, z);
		packet.addLine(color, x2, y1, z, x2, y2, z);
		packet.addLine(color, x2, y2, z, x1, y2, z);
		packet.addLine(color, x1, y2, z, x1, y1, z);

		packet.addLine(color, x1, y1, z1, x2, y1, z1);
		packet.addLine(color, x2, y1, z1, x2, y2, z1);
		packet.addLine(color, x2, y2, z1, x1, y2, z1);
		packet.addLine(color, x1, y2, z1, x1, y1, z1);

		packet.addLine(color, x1, y1, z2, x2, y1, z2);
		packet.addLine(color, x2, y1, z2, x2, y2, z2);
		packet.addLine(color, x2, y2, z2, x1, y2, z2);
		packet.addLine(color, x1, y2, z2, x1, y1, z2);

		packet.addLine(color, x1, y1, z1, x1, y1, z2);
		packet.addLine(color, x2, y1, z1, x2, y1, z2);
		packet.addLine(color, x2, y2, z1, x2, y2, z2);
		packet.addLine(color, x1, y2, z1, x1, y2, z2);

		int centerX = x1 + (x2 - x1) / 2;
		int centerY = y1 + (y2 - y1) / 2;
		int radius = (int) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		int count = 500;//Math.min(Math.max((this.maxX - this.minX) / 50, 5), 100);
		int angle = Rnd.get(180);
		double dirX = Math.cos(angle * Math.PI / 180.0);
		double dirY = Math.sin(angle * Math.PI / 180.0);
		int baseX = centerX - (int) (dirX * radius);
		int baseY = centerY - (int) (dirY * radius);
		//packet.addPoint("CENTER", Color.red, true, centerX, centerY, z);
		//packet.addPoint("BASE", Color.red, true, baseX, baseY, z);
		int[] x = new int[]{x1, x2, x2, x1};
		int[] y = new int[]{y1, y1, y2, y2};
		for (int i = 0; i < count; i++)
		{
			boolean found = false;
			int curX = baseX + (int) (2 * radius * dirX * i / count);
			int curY = baseY + (int) (2 * radius * dirY * i / count);
			int minX0 = curX + (int) (radius * -dirY);
			int minY0 = curY + (int) (radius * dirX);
			int maxX0 = curX + (int) (radius * dirY);
			int maxY0 = curY + (int) (radius * -dirX);
			int minX = minX0;
			int minY = minY0;
			int maxX = maxX0;
			int maxY = maxY0;
			int maxDot = -radius;
			int minDot = radius;
			//packet.addLine(color, baseX, baseY, z, baseX + (int)(2 * radius * dirX), baseY + (int)(2 * radius * dirY), z);
			for (int j = 0; j < x.length; j++)
			{
				int nextIndex = j + 1;
				if (nextIndex == x.length)
				{
					nextIndex = 0;
				}

				int[] intersec =
						segmentsIntersection(x[j], y[j], x[nextIndex], y[nextIndex], minX0, minY0, maxX0, maxY0);
				if (intersec != null)
				{
					int dx = intersec[0] - curX;
					int dy = intersec[1] - curY;
					int dot = (int) (dx * dirY - dy * dirX);
					if (dot < minDot)
					{
						minX = intersec[0];
						minY = intersec[1];
						minDot = dot;
					}
					if (dot > maxDot)
					{
						maxX = intersec[0];
						maxY = intersec[1];
						maxDot = dot;
					}
					found = true;
				}
			}

			if (found)
			{
				packet.addLine(color, minX, minY, z, maxX, maxY, z);
			}
		}
	}

	@Override
	public int[] getRandomPoint()
	{
		int x = Rnd.get(x1, x2);
		int y = Rnd.get(y1, y2);

		return new int[]{x, y, GeoEngine.getInstance().getHeight(x, y, z1)};
	}
}
