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

package l2server.gameserver.model.actor.instance

import l2server.gameserver.model.Abnormal
import l2server.gameserver.model.InstanceType
import l2server.gameserver.model.actor.Npc
import l2server.gameserver.model.actor.status.FolkStatus
import l2server.gameserver.stats.effects.EffectBuff
import l2server.gameserver.stats.effects.EffectDebuff
import l2server.gameserver.templates.chars.NpcTemplate

open class NpcInstance(objectId: Int, template: NpcTemplate) : Npc(objectId, template) {

	init {
		instanceType = InstanceType.L2NpcInstance
		setIsInvul(false)
	}

	override fun getStatus(): FolkStatus {
		return super.getStatus() as FolkStatus
	}

	override fun initCharStatus() {
		status = FolkStatus(this)
	}

	override fun addEffect(newEffect: Abnormal?) {
		if (newEffect == null) {
			return
		}

		var found = false
		for (effect in newEffect.effects) {
			if (!(effect is EffectDebuff || effect is EffectBuff)) {
				found = true
			}
		}

		if (!found) {
			super.addEffect(newEffect)
		} else {
			newEffect.stopEffectTask()
		}
	}
}
