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
	private int _x1, _x2, _y1, _y2, _z1, _z2;

	public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2)
	{
		_x1 = x1;
		_x2 = x2;
		if (_x1 > _x2) // switch them if alignment is wrong
		{
			_x1 = x2;
			_x2 = x1;
		}

		_y1 = y1;
		_y2 = y2;
		if (_y1 > _y2) // switch them if alignment is wrong
		{
			_y1 = y2;
			_y2 = y1;
		}

		_z1 = z1;
		_z2 = z2;
		if (_z1 > _z2) // switch them if alignment is wrong
		{
			_z1 = z2;
			_z2 = z1;
		}
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return !(x < _x1 || x > _x2 || y < _y1 || y > _y2 || z < _z1 || z > _z2);
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		// Check if any point inside this rectangle
		if (isInsideZone(ax1, ay1, _z2 - 1))
		{
			return true;
		}
		if (isInsideZone(ax1, ay2, _z2 - 1))
		{
			return true;
		}
		if (isInsideZone(ax2, ay1, _z2 - 1))
		{
			return true;
		}
		if (isInsideZone(ax2, ay2, _z2 - 1))
		{
			return true;
		}

		// Check if any point from this rectangle is inside the other one
		if (_x1 > ax1 && _x1 < ax2 && _y1 > ay1 && _y1 < ay2)
		{
			return true;
		}
		if (_x1 > ax1 && _x1 < ax2 && _y2 > ay1 && _y2 < ay2)
		{
			return true;
		}
		if (_x2 > ax1 && _x2 < ax2 && _y1 > ay1 && _y1 < ay2)
		{
			return true;
		}
		if (_x2 > ax1 && _x2 < ax2 && _y2 > ay1 && _y2 < ay2)
		{
			return true;
		}

		// Horizontal lines may intersect vertical lines
		if (lineSegmentsIntersect(_x1, _y1, _x2, _y1, ax1, ay1, ax1, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(_x1, _y1, _x2, _y1, ax2, ay1, ax2, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(_x1, _y2, _x2, _y2, ax1, ay1, ax1, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(_x1, _y2, _x2, _y2, ax2, ay1, ax2, ay2))
		{
			return true;
		}

		// Vertical lines may intersect horizontal lines
		if (lineSegmentsIntersect(_x1, _y1, _x1, _y2, ax1, ay1, ax2, ay1))
		{
			return true;
		}
		if (lineSegmentsIntersect(_x1, _y1, _x1, _y2, ax1, ay2, ax2, ay2))
		{
			return true;
		}
		if (lineSegmentsIntersect(_x2, _y1, _x2, _y2, ax1, ay1, ax2, ay1))
		{
			return true;
		}
		return lineSegmentsIntersect(_x2, _y1, _x2, _y2, ax1, ay2, ax2, ay2);

	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test, shortestDist = Math.pow(_x1 - x, 2) + Math.pow(_y1 - y, 2);

		test = Math.pow(_x1 - x, 2) + Math.pow(_y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}

		test = Math.pow(_x2 - x, 2) + Math.pow(_y1 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}

		test = Math.pow(_x2 - x, 2) + Math.pow(_y2 - y, 2);
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
		return _x1 + (_x2 - _x1) / 2;
	}

	@Override
	public int getCenterY()
	{
		return _y1 + (_y2 - _y1) / 2;
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
		packet.setXYZ((_x1 + _x2) / 2, (_y1 + _y2) / 2, z);
		packet.addPoint(name, color, true, (_x1 + _x2) / 2, (_y1 + _y2) / 2, z);

		packet.addLine(color, _x1, _y1, z, _x2, _y1, z);
		packet.addLine(color, _x2, _y1, z, _x2, _y2, z);
		packet.addLine(color, _x2, _y2, z, _x1, _y2, z);
		packet.addLine(color, _x1, _y2, z, _x1, _y1, z);

		packet.addLine(color, _x1, _y1, _z1, _x2, _y1, _z1);
		packet.addLine(color, _x2, _y1, _z1, _x2, _y2, _z1);
		packet.addLine(color, _x2, _y2, _z1, _x1, _y2, _z1);
		packet.addLine(color, _x1, _y2, _z1, _x1, _y1, _z1);

		packet.addLine(color, _x1, _y1, _z2, _x2, _y1, _z2);
		packet.addLine(color, _x2, _y1, _z2, _x2, _y2, _z2);
		packet.addLine(color, _x2, _y2, _z2, _x1, _y2, _z2);
		packet.addLine(color, _x1, _y2, _z2, _x1, _y1, _z2);

		packet.addLine(color, _x1, _y1, _z1, _x1, _y1, _z2);
		packet.addLine(color, _x2, _y1, _z1, _x2, _y1, _z2);
		packet.addLine(color, _x2, _y2, _z1, _x2, _y2, _z2);
		packet.addLine(color, _x1, _y2, _z1, _x1, _y2, _z2);

		int centerX = _x1 + (_x2 - _x1) / 2;
		int centerY = _y1 + (_y2 - _y1) / 2;
		int radius = (int) Math.sqrt((_x2 - _x1) * (_x2 - _x1) + (_y2 - _y1) * (_y2 - _y1));
		int count = 500;//Math.min(Math.max((_maxX - _minX) / 50, 5), 100);
		int angle = Rnd.get(180);
		double dirX = Math.cos(angle * Math.PI / 180.0);
		double dirY = Math.sin(angle * Math.PI / 180.0);
		int baseX = centerX - (int) (dirX * radius);
		int baseY = centerY - (int) (dirY * radius);
		//packet.addPoint("CENTER", Color.red, true, centerX, centerY, z);
		//packet.addPoint("BASE", Color.red, true, baseX, baseY, z);
		int[] _x = new int[]{_x1, _x2, _x2, _x1};
		int[] _y = new int[]{_y1, _y1, _y2, _y2};
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
		int x = Rnd.get(_x1, _x2);
		int y = Rnd.get(_y1, _y2);

		return new int[]{x, y, GeoEngine.getInstance().getHeight(x, y, _z1)};
	}
}
