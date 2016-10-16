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
 * A not so primitive npoly zone
 *
 * @author durgus
 */
public class ZoneNPoly extends L2ZoneForm
{
	private int[] x;
	private int[] y;
	private int z1;
	private int z2;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;

	public ZoneNPoly(int[] x, int[] y, int z1, int z2)
	{
		this.x = x;
		this.y = y;
		this.z1 = z1;
		this.z2 = z2;
	}

	public ZoneNPoly(int[] x, int[] y, int z1, int z2, int minX, int maxX, int minY, int maxY)
	{
		this.x = x;
		this.y = y;
		this.z1 = z1;
		this.z2 = z2;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		if (z < this.z1 || z > z2)
		{
			return false;
		}

		boolean inside = false;
		for (int i = 0, j = this.x.length - 1; i < this.x.length; j = i++)
		{
			if ((this.y[i] <= y && y < this.y[j] || this.y[j] <= y && y < this.y[i]) &&
					x < (this.x[j] - this.x[i]) * (y - this.y[i]) / (this.y[j] - this.y[i]) + this.x[i])
			{
				inside = !inside;
			}
		}
		return inside;
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		int tX, tY, uX, uY;

		// First check if a point of the polygon lies inside the rectangle
		if (this.x[0] > ax1 && this.x[0] < ax2 && this.y[0] > ay1 && this.y[0] < ay2)
		{
			return true;
		}

		// Or a point of the rectangle inside the polygon
		if (isInsideZone(ax1, ay1, this.z2 - 1))
		{
			return true;
		}

		// If the first point wasn't inside the rectangle it might still have any line crossing any side
		// of the rectangle

		// Check every possible line of the polygon for a collision with any of the rectangles side
		for (int i = 0; i < this.y.length; i++)
		{
			tX = this.x[i];
			tY = this.y[i];
			uX = this.x[(i + 1) % this.x.length];
			uY = this.y[(i + 1) % this.x.length];

			// Check if this line intersects any of the four sites of the rectangle
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax1, ay2))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax2, ay1))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax1, ay2))
			{
				return true;
			}
			if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax2, ay1))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test, shortestDist;
		double u = ((x - this.x[this.x.length - 1]) * (this.x[0] - this.x[this.x.length - 1]) +
				(y - this.y[this.y.length - 1]) * (this.y[0] - this.y[this.y.length - 1])) /
				(Math.pow(this.x[0] - this.x[this.x.length - 1], 2) + Math.pow(this.y[0] - this.y[this.y.length - 1], 2));
		if (u > 0 && u < 1)
		{
			shortestDist = Math.pow(this.x[0] + u * (this.x[this.x.length - 1] - this.x[0]) - x, 2) +
					Math.pow(this.y[0] + u * (this.y[this.y.length - 1] - this.y[0]) - y, 2);
		}
		else
		{
			shortestDist = Math.pow(this.x[0] - x, 2) + Math.pow(this.y[0] - y, 2);
		}

		for (int i = 1; i < this.y.length; i++)
		{
			u = ((x - this.x[this.x.length - 1]) * (this.x[0] - this.x[this.x.length - 1]) +
					(y - this.y[this.y.length - 1]) * (this.y[0] - this.y[this.y.length - 1])) /
					(Math.pow(this.x[0] - this.x[this.x.length - 1], 2) + Math.pow(this.y[0] - this.y[this.y.length - 1], 2));
			if (u > 0 && u < 1)
			{
				test = Math.pow(this.x[i] + u * (this.x[i - 1] - this.x[i]) - x, 2) +
						Math.pow(this.y[i] + u * (this.y[i - 1] - this.y[i]) - y, 2);
			}
			else
			{
				test = Math.pow(this.x[i] - x, 2) + Math.pow(this.y[i] - y, 2);
			}
			if (test < shortestDist)
			{
				shortestDist = test;
			}
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
		return this.z1;
	}

	@Override
	public int getHighZ()
	{
		return this.z2;
	}

	@Override
	public int getCenterX()
	{
		return this.minX + (this.maxX - this.minX) / 2;
	}

	@Override
	public int getCenterY()
	{
		return this.minY + (this.maxY - this.minY) / 2;
	}

	@Override
	public void visualizeZone(ExServerPrimitive packet, String name, int z)
	{
		if (z < this.z1 + 100)
		{
			z = this.z1 + 100;
		}
		if (z > z2 - 20)
		{
			z = this.z2 - 20;
		}

		Color color = new Color(Rnd.get(2), Rnd.get(2), Rnd.get(2));
		int avgX = 0;
		int avgY = 0;
		for (int i = 0; i < this.x.length; i++)
		{
			int nextIndex = i + 1;
			// ending point to first one
			if (nextIndex == this.x.length)
			{
				nextIndex = 0;
			}

			packet.addLine(color, this.x[i], this.y[i], z, this.x[nextIndex], this.y[nextIndex], z);
			packet.addLine(color, this.x[i], this.y[i], this.z1, this.x[nextIndex], this.y[nextIndex], this.z1);
			packet.addLine(color, this.x[i], this.y[i], this.z2, this.x[nextIndex], this.y[nextIndex], this.z2);
			packet.addLine(color, this.x[i], this.y[i], this.z1, this.x[i], this.y[i], this.z2);
			avgX += this.x[i];
			avgY += this.y[i];
		}

		packet.setXYZ(avgX / this.x.length, avgY / this.y.length, z);
		packet.addPoint(name, color, true, avgX / this.x.length, avgY / this.y.length, z);

		int centerX = this.minX + (this.maxX - this.minX) / 2;
		int centerY = this.minY + (this.maxY - this.minY) / 2;
		int radius = (int) Math.sqrt((this.maxX - this.minX) * (this.maxX - this.minX) + (this.maxY - this.minY) * (this.maxY - this.minY));
		int count = 500;//Math.min(Math.max((this.maxX - this.minX) / 50, 5), 100);
		int angle = Rnd.get(180);
		double dirX = Math.cos(angle * Math.PI / 180.0);
		double dirY = Math.sin(angle * Math.PI / 180.0);
		int baseX = centerX - (int) (dirX * radius);
		int baseY = centerY - (int) (dirY * radius);
		//packet.addPoint("CENTER", Color.red, true, centerX, centerY, z);
		//packet.addPoint("BASE", Color.red, true, baseX, baseY, z);
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
			for (int j = 0; j < this.x.length; j++)
			{
				int nextIndex = j + 1;
				if (nextIndex == this.x.length)
				{
					nextIndex = 0;
				}

				int[] intersec =
						segmentsIntersection(this.x[j], this.y[j], this.x[nextIndex], this.y[nextIndex], minX0, minY0, maxX0, maxY0);
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
		int x, y;

		x = Rnd.get(this.minX, this.maxX);
		y = Rnd.get(this.minY, this.maxY);

		int antiBlocker = 0;
		while (!isInsideZone(x, y, getHighZ()) && antiBlocker < 1000)
		{
			x = Rnd.get(this.minX, this.maxX);
			y = Rnd.get(this.minY, this.maxY);
			antiBlocker++;
		}

		return new int[]{x, y, GeoEngine.getInstance().getHeight(x, y, this.z1)};
	}
}
