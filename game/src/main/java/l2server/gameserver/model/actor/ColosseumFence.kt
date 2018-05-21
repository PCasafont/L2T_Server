/*
 * Copyright (C) 2004-2014 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor

import l2server.gameserver.idfactory.IdFactory
import l2server.gameserver.model.WorldObject
import l2server.gameserver.model.actor.instance.Player
import l2server.gameserver.network.serverpackets.ExColosseumFenceInfo
import java.awt.Rectangle

/**
 * @author FBIagent
 */
class ColosseumFence(instanceId: Int,
					 x: Int, y: Int,
					 z: Int,
					 val fenceMinZ: Int,
					 val fenceMaxZ: Int,
					 width: Int,
					 height: Int,
					 val fenceState: FenceState) : WorldObject(IdFactory.getInstance().nextId) {

	private val bounds: Rectangle

	val fenceX: Int
		get() = bounds.x

	val fenceY: Int
		get() = bounds.y

	val fenceWidth: Int
		get() = bounds.width

	val fenceHeight: Int
		get() = bounds.height

	enum class FenceState {
		HIDDEN,
		// the fence isn't shown at all
		OPEN,
		// the 4 edges of the fence is shown only
		CLOSED // full fence
	}

	init {
		this.instanceId = instanceId
		setXYZ(x, y, z)
		bounds = Rectangle(x - width / 2, y - height / 2, width, height)
	}

	override fun sendInfo(activeChar: Player) {
		activeChar.sendPacket(ExColosseumFenceInfo(this))
	}

	override fun isAutoAttackable(attacker: Creature): Boolean {
		return false
	}

	fun isInsideFence(x: Int, y: Int, z: Int): Boolean {
		return x >= bounds.x && y >= bounds.y && z >= fenceMinZ && z <= fenceMaxZ && x <= bounds.x + bounds.width && y <= bounds.y + bounds.width
	}
}
