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

package l2server.gameserver.network.serverpackets

import l2server.gameserver.model.actor.Creature

/**
 * sample
 *
 *
 * 0000: 3e 2a 89 00 4c 01 00 00 00						 .|...
 *
 *
 * format   dd
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
class ChangeMoveType(character: Creature) : L2GameServerPacket() {

	private val charObjId: Int
	private val running: Boolean

	init {
		charObjId = character.objectId
		running = character.isRunning
	}

	override fun writeImpl() {
		writeD(charObjId)
		writeD(if (running) RUN else WALK)
		writeD(0) //c2
	}

	companion object {
		val WALK = 0
		val RUN = 1
	}
}
