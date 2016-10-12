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
 * A primitive circular zone
 *
 * @author durgus
 */
public class ZoneCylinder extends L2ZoneForm
{
	private int _x, _y, _z1, _z2, _rad, _radS;

	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
		_rad = rad;
		_radS = rad * rad;
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return !(Math.pow(_x - x, 2) + Math.pow(_y - y, 2) > _radS || z < _z1 || z > _z2);
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		// Circles point inside the rectangle?
		if (_x > ax1 && _x < ax2 && _y > ay1 && _y < ay2)
		{
			return true;
		}

		// Any point of the rectangle intersecting the Circle?
		if (Math.pow(ax1 - _x, 2) + Math.pow(ay1 - _y, 2) < _radS)
		{
			return true;
		}
		if (Math.pow(ax1 - _x, 2) + Math.pow(ay2 - _y, 2) < _radS)
		{
			return true;
		}
		if (Math.pow(ax2 - _x, 2) + Math.pow(ay1 - _y, 2) < _radS)
		{
			return true;
		}
		if (Math.pow(ax2 - _x, 2) + Math.pow(ay2 - _y, 2) < _radS)
		{
			return true;
		}

		// Collision on any side of the rectangle?
		if (_x > ax1 && _x < ax2)
		{
			if (Math.abs(_y - ay2) < _rad)
			{
				return true;
			}
			if (Math.abs(_y - ay1) < _rad)
			{
				return true;
			}
		}
		if (_y > ay1 && _y < ay2)
		{
			if (Math.abs(_x - ax2) < _rad)
			{
				return true;
			}
			if (Math.abs(_x - ax1) < _rad)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		return Math.sqrt(Math.pow(_x - x, 2) + Math.pow(_y - y, 2)) - _rad;
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
		return _x;
	}

	@Override
	public int getCenterY()
	{
		return _y;
	}

	@Override
	public void visualizeZone(ExServerPrimitive packet, String name, int z)
	{
		if (z < _z1 + 100)
		{
			z = _z1 + 100;
		}
		if (z > _z2 - 100)
		{
			z = _z2 - 100;
		}

		Color color = new Color(Rnd.get(2), Rnd.get(2), Rnd.get(2));
		packet.setXYZ(_x, _y, z);
		packet.addPoint(name, color, true, _x, _y, z);

		int count = 32;
		double angle = 2 * Math.PI / count;
		for (int i = 0; i < count; i++)
		{
			int x1 = (int) (Math.cos(angle * i) * _rad);
			int y1 = (int) (Math.sin(angle * i) * _rad);
			int x2 = (int) (Math.cos(angle * (i + 1)) * _rad);
			int y2 = (int) (Math.sin(angle * (i + 1)) * _rad);
			packet.addLine(color, x1, y1, z, x2, y2, z);
			packet.addLine(color, x1, y1, _z1, x2, y2, _z1);
			packet.addLine(color, x1, y1, _z2, x2, y2, _z2);
			packet.addLine(color, x1, y1, _z1, x1, y1, _z2);
		}
	}

	@Override
	public int[] getRandomPoint()
	{
		double x, y, q, r;

		q = Rnd.get() * 2 * Math.PI;
		r = Math.sqrt(Rnd.get());
		x = _rad * r * Math.cos(q) + _x;
		y = _rad * r * Math.sin(q) + _y;

		return new int[]{(int) x, (int) y, GeoEngine.getInstance().getHeight((int) x, (int) y, _z1)};
	}
}
