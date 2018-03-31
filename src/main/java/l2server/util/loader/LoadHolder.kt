/*
 * Copyright (C) 2004-2016 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2server.util.loader

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * @author NosKun
 */
class LoadHolder(val instanceGetterMethod: Method, val loadMethod: Method) {

	@Throws(InvocationTargetException::class, IllegalAccessException::class)
	fun call() {
		val loadMethodAccessible = loadMethod.isAccessible
		loadMethod.isAccessible = true
		loadMethod.invoke(instanceGetterMethod.invoke(null))
		loadMethod.isAccessible = loadMethodAccessible
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) {
			return true
		}
		if (other == null || javaClass != other.javaClass) {
			return false
		}

		val that = other as LoadHolder?
		return if (instanceGetterMethod != that!!.instanceGetterMethod) {
			false
		} else loadMethod == that.loadMethod

	}

	override fun hashCode(): Int {
		var result = instanceGetterMethod.hashCode()
		result = 31 * result + loadMethod.hashCode()
		return result
	}

	override fun toString(): String {
		return instanceGetterMethod.declaringClass.name + "." + instanceGetterMethod.name + "()." + loadMethod.name + "()"
	}
}
