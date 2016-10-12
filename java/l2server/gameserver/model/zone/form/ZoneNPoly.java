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
	private int[] _x;
	private int[] _y;
	private int _z1;
	private int _z2;
	private int _minX;
	private int _maxX;
	private int _minY;
	private int _maxY;

	public ZoneNPoly(int[] x, int[] y, int z1, int z2)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
	}

	public ZoneNPoly(int[] x, int[] y, int z1, int z2, int minX, int maxX, int minY, int maxY)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
		_minX = minX;
		_maxX = maxX;
		_minY = minY;
		_maxY = maxY;
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		if (z < _z1 || z > _z2)
		{
			return false;
		}

		boolean inside = false;
		for (int i = 0, j = _x.length - 1; i < _x.length; j = i++)
		{
			if ((_y[i] <= y && y < _y[j] || _y[j] <= y && y < _y[i]) &&
					x < (_x[j] - _x[i]) * (y - _y[i]) / (_y[j] - _y[i]) + _x[i])
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
		if (_x[0] > ax1 && _x[0] < ax2 && _y[0] > ay1 && _y[0] < ay2)
		{
			return true;
		}

		// Or a point of the rectangle inside the polygon
		if (isInsideZone(ax1, ay1, _z2 - 1))
		{
			return true;
		}

		// If the first point wasn't inside the rectangle it might still have any line crossing any side
		// of the rectangle

		// Check every possible line of the polygon for a collision with any of the rectangles side
		for (int i = 0; i < _y.length; i++)
		{
			tX = _x[i];
			tY = _y[i];
			uX = _x[(i + 1) % _x.length];
			uY = _y[(i + 1) % _x.length];

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
		double u = ((x - _x[_x.length - 1]) * (_x[0] - _x[_x.length - 1]) +
				(y - _y[_y.length - 1]) * (_y[0] - _y[_y.length - 1])) /
				(Math.pow(_x[0] - _x[_x.length - 1], 2) + Math.pow(_y[0] - _y[_y.length - 1], 2));
		if (u > 0 && u < 1)
		{
			shortestDist = Math.pow(_x[0] + u * (_x[_x.length - 1] - _x[0]) - x, 2) +
					Math.pow(_y[0] + u * (_y[_y.length - 1] - _y[0]) - y, 2);
		}
		else
		{
			shortestDist = Math.pow(_x[0] - x, 2) + Math.pow(_y[0] - y, 2);
		}

		for (int i = 1; i < _y.length; i++)
		{
			u = ((x - _x[_x.length - 1]) * (_x[0] - _x[_x.length - 1]) +
					(y - _y[_y.length - 1]) * (_y[0] - _y[_y.length - 1])) /
					(Math.pow(_x[0] - _x[_x.length - 1], 2) + Math.pow(_y[0] - _y[_y.length - 1], 2));
			if (u > 0 && u < 1)
			{
				test = Math.pow(_x[i] + u * (_x[i - 1] - _x[i]) - x, 2) +
						Math.pow(_y[i] + u * (_y[i - 1] - _y[i]) - y, 2);
			}
			else
			{
				test = Math.pow(_x[i] - x, 2) + Math.pow(_y[i] - y, 2);
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
		return _z1;
	}

	@Override
	public int getHighZ()
	{
		return _z2;
	}

	@Override
	public int getCenterX()
	{
		return _minX + (_maxX - _minX) / 2;
	}

	@Override
	public int getCenterY()
	{
		return _minY + (_maxY - _minY) / 2;
	}

	@Override
	public void visualizeZone(ExServerPrimitive packet, String name, int z)
	{
		if (z < _z1 + 100)
		{
			z = _z1 + 100;
		}
		if (z > _z2 - 20)
		{
			z = _z2 - 20;
		}

		Color color = new Color(Rnd.get(2), Rnd.get(2), Rnd.get(2));
		int avgX = 0;
		int avgY = 0;
		for (int i = 0; i < _x.length; i++)
		{
			int nextIndex = i + 1;
			// ending point to first one
			if (nextIndex == _x.length)
			{
				nextIndex = 0;
			}

			packet.addLine(color, _x[i], _y[i], z, _x[nextIndex], _y[nextIndex], z);
			packet.addLine(color, _x[i], _y[i], _z1, _x[nextIndex], _y[nextIndex], _z1);
			packet.addLine(color, _x[i], _y[i], _z2, _x[nextIndex], _y[nextIndex], _z2);
			packet.addLine(color, _x[i], _y[i], _z1, _x[i], _y[i], _z2);
			avgX += _x[i];
			avgY += _y[i];
		}

		packet.setXYZ(avgX / _x.length, avgY / _y.length, z);
		packet.addPoint(name, color, true, avgX / _x.length, avgY / _y.length, z);

		int centerX = _minX + (_maxX - _minX) / 2;
		int centerY = _minY + (_maxY - _minY) / 2;
		int radius = (int) Math.sqrt((_maxX - _minX) * (_maxX - _minX) + (_maxY - _minY) * (_maxY - _minY));
		int count = 500;//Math.min(Math.max((_maxX - _minX) / 50, 5), 100);
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
			for (int j = 0; j < _x.length; j++)
			{
				int nextIndex = j + 1;
				if (nextIndex == _x.length)
				{
					nextIndex = 0;
				}

				int[] intersec =
						segmentsIntersection(_x[j], _y[j], _x[nextIndex], _y[nextIndex], minX0, minY0, maxX0, maxY0);
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

		x = Rnd.get(_minX, _maxX);
		y = Rnd.get(_minY, _maxY);

		int antiBlocker = 0;
		while (!isInsideZone(x, y, getHighZ()) && antiBlocker < 1000)
		{
			x = Rnd.get(_minX, _maxX);
			y = Rnd.get(_minY, _maxY);
			antiBlocker++;
		}

		return new int[]{x, y, GeoEngine.getInstance().getHeight(x, y, _z1)};
	}
}
