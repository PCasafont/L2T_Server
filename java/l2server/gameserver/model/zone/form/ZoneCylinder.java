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
	private int x, y, z1, z2, rad, radS;

	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		this.x = x;
		this.y = y;
		this.z1 = z1;
		this.z2 = z2;
		this.rad = rad;
		radS = rad * rad;
	}

	@Override
	public boolean isInsideZone(int x, int y, int z)
	{
		return !(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2) > radS || z < z1 || z > z2);
	}

	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		// Circles point inside the rectangle?
		if (x > ax1 && x < ax2 && y > ay1 && y < ay2)
		{
			return true;
		}

		// Any point of the rectangle intersecting the Circle?
		if (Math.pow(ax1 - x, 2) + Math.pow(ay1 - y, 2) < radS)
		{
			return true;
		}
		if (Math.pow(ax1 - x, 2) + Math.pow(ay2 - y, 2) < radS)
		{
			return true;
		}
		if (Math.pow(ax2 - x, 2) + Math.pow(ay1 - y, 2) < radS)
		{
			return true;
		}
		if (Math.pow(ax2 - x, 2) + Math.pow(ay2 - y, 2) < radS)
		{
			return true;
		}

		// Collision on any side of the rectangle?
		if (x > ax1 && x < ax2)
		{
			if (Math.abs(y - ay2) < rad)
			{
				return true;
			}
			if (Math.abs(y - ay1) < rad)
			{
				return true;
			}
		}
		if (y > ay1 && y < ay2)
		{
			if (Math.abs(x - ax2) < rad)
			{
				return true;
			}
			if (Math.abs(x - ax1) < rad)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public double getDistanceToZone(int x, int y)
	{
		return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2)) - rad;
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
		return x;
	}

	@Override
	public int getCenterY()
	{
		return y;
	}

	@Override
	public void visualizeZone(ExServerPrimitive packet, String name, int z)
	{
		if (z < z1 + 100)
		{
			z = z1 + 100;
		}
		if (z > z2 - 100)
		{
			z = z2 - 100;
		}

		Color color = new Color(Rnd.get(2), Rnd.get(2), Rnd.get(2));
		packet.setXYZ(x, y, z);
		packet.addPoint(name, color, true, x, y, z);

		int count = 32;
		double angle = 2 * Math.PI / count;
		for (int i = 0; i < count; i++)
		{
			int x1 = (int) (Math.cos(angle * i) * rad);
			int y1 = (int) (Math.sin(angle * i) * rad);
			int x2 = (int) (Math.cos(angle * (i + 1)) * rad);
			int y2 = (int) (Math.sin(angle * (i + 1)) * rad);
			packet.addLine(color, x1, y1, z, x2, y2, z);
			packet.addLine(color, x1, y1, z1, x2, y2, z1);
			packet.addLine(color, x1, y1, z2, x2, y2, z2);
			packet.addLine(color, x1, y1, z1, x1, y1, z2);
		}
	}

	@Override
	public int[] getRandomPoint()
	{
		double x, y, q, r;

		q = Rnd.get() * 2 * Math.PI;
		r = Math.sqrt(Rnd.get());
		x = rad * r * Math.cos(q) + this.x;
		y = rad * r * Math.sin(q) + this.y;

		return new int[]{(int) x, (int) y, GeoEngine.getInstance().getHeight((int) x, (int) y, z1)};
	}
}
