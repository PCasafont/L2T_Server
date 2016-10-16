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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@EqualsAndHashCode
public class Point3D implements Serializable
{
	private static final long serialVersionUID = 4638345252031872577L;

	@Getter @Setter private volatile int x, y, z;

	public Point3D(int pX, int pY)
	{
		x = pX;
		y = pY;
		z = 0;
	}

	/**
	 * @param other The other point to copy this from
	 */
	public Point3D(Point3D other)
	{
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public synchronized void setTo(Point3D point)
	{
		x = point.x;
		y = point.y;
		z = point.z;
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public synchronized boolean equalsTo(int pX, int pY, int pZ)
	{
		return x == pX && y == pY && z == pZ;
	}

	public synchronized long distanceSquaredTo(Point3D point)
	{
		long dx = x - point.x;
		long dy = y - point.y;
		return dx * dx + dy * dy;
	}

	public static long distanceSquared(Point3D point1, Point3D point2)
	{
		long dx = point1.x - point2.x;
		long dy = point1.y - point2.y;
		return dx * dx + dy * dy;
	}

	public static boolean distanceLessThan(Point3D point1, Point3D point2, double distance)
	{
		return distanceSquared(point1, point2) < distance * distance;
	}

	public synchronized void setXYZ(int pX, int pY, int pZ)
	{
		x = pX;
		y = pY;
		z = pZ;
	}
}
