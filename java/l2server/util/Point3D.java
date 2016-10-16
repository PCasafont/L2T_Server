/*
 * $Header: Point3D.java, 19/07/2005 21:33:07 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 19/07/2005 21:33:07 $
 * $Revision: 1 $
 * $Log: Point3D.java,v $
 * Revision 1  19/07/2005 21:33:07  luisantonioa
 * Added copyright notice
 *
 *
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

package l2server.util;

import java.io.Serializable;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class Point3D implements Serializable
{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4638345252031872576L;

	private volatile int x, y, z;

	public Point3D(int pX, int pY, int pZ)
	{
		this.x = pX;
		this.y = pY;
		this.z = pZ;
	}

	public Point3D(int pX, int pY)
	{
		this.x = pX;
		this.y = pY;
		this.z = 0;
	}

	/**
	 * @param worldPosition
	 */
	public Point3D(Point3D worldPosition)
	{
		synchronized (worldPosition)
		{
			this.x = worldPosition.x;
			this.y = worldPosition.y;
			this.z = worldPosition.z;
		}
	}

	public synchronized void setTo(Point3D point)
	{
		synchronized (point)
		{
			this.x = point.x;
			this.y = point.y;
			this.z = point.z;
		}
	}

	@Override
	public String toString()
	{
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}

	@Override
	public int hashCode()
	{
		return this.x ^ this.y ^ this.z;
	}

	@Override
	public synchronized boolean equals(Object o)
	{
		if (o instanceof Point3D)
		{
			Point3D point3D = (Point3D) o;
			boolean ret;
			synchronized (point3D)
			{
				ret = point3D.x == this.x && point3D.y == this.y && point3D.z == this.z;
			}
			return ret;
		}
		return false;
	}

	public synchronized boolean equals(int pX, int pY, int pZ)
	{
		return this.x == pX && this.y == pY && this.z == pZ;
	}

	public synchronized long distanceSquaredTo(Point3D point)
	{
		long dx, dy;
		synchronized (point)
		{
			dx = this.x - point.x;
			dy = this.y - point.y;
		}
		return dx * dx + dy * dy;
	}

	public static long distanceSquared(Point3D point1, Point3D point2)
	{
		long dx, dy;
		synchronized (point1)
		{
			synchronized (point2)
			{
				dx = point1.x - point2.x;
				dy = point1.y - point2.y;
			}
		}
		return dx * dx + dy * dy;
	}

	public static boolean distanceLessThan(Point3D point1, Point3D point2, double distance)
	{
		return distanceSquared(point1, point2) < distance * distance;
	}

	public int getX()
	{
		return this.x;
	}

	public synchronized void setX(int pX)
	{
		this.x = pX;
	}

	public int getY()
	{
		return this.y;
	}

	public synchronized void setY(int pY)
	{
		this.y = pY;
	}

	public int getZ()
	{
		return this.z;
	}

	public synchronized void setZ(int pZ)
	{
		this.z = pZ;
	}

	public synchronized void setXYZ(int pX, int pY, int pZ)
	{
		this.x = pX;
		this.y = pY;
		this.z = pZ;
	}
}
