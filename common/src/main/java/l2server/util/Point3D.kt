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

package l2server.util

import java.io.Serializable

class Point3D : Serializable {

	@Volatile
	@set:Synchronized
	var x: Int = 0
	@Volatile
	@set:Synchronized
	var y: Int = 0
	@Volatile
	@set:Synchronized
	var z: Int = 0

	constructor(pX: Int, pY: Int, pZ: Int) {
		x = pX
		y = pY
		z = pZ
	}

	constructor(pX: Int, pY: Int) {
		x = pX
		y = pY
		z = 0
	}

	constructor(worldPosition: Point3D) {
		synchronized(worldPosition) {
			x = worldPosition.x
			y = worldPosition.y
			z = worldPosition.z
		}
	}

	@Synchronized
	fun setTo(point: Point3D) {
		synchronized(point) {
			x = point.x
			y = point.y
			z = point.z
		}
	}

	override fun toString(): String {
		return "($x, $y, $z)"
	}

	override fun hashCode(): Int {
		return x xor y xor z
	}

	@Synchronized
	override fun equals(other: Any?): Boolean {
		if (other is Point3D) {
			val point3D = other
			var ret = false
			synchronized(point3D) {
				ret = point3D.x == x && point3D.y == y && point3D.z == z
			}
			return ret
		}
		return false
	}

	@Synchronized
	fun equals(pX: Int, pY: Int, pZ: Int): Boolean {
		return x == pX && y == pY && z == pZ
	}

	@Synchronized
	fun distanceSquaredTo(point: Point3D): Long {
		var dx = 0L
		var dy = 0L
		synchronized(point) {
			dx = (x - point.x).toLong()
			dy = (y - point.y).toLong()
		}
		return dx * dx + dy * dy
	}

	@Synchronized
	fun setXYZ(pX: Int, pY: Int, pZ: Int) {
		x = pX
		y = pY
		z = pZ
	}

	companion object {
		private const val serialVersionUID = 4638345252031872576L
	}
}
