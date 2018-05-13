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

package l2server.gameserver.templates.skills

import l2server.gameserver.model.Abnormal
import l2server.gameserver.model.L2Effect
import l2server.gameserver.stats.Env
import l2server.gameserver.stats.VisualEffect
import l2server.gameserver.stats.conditions.Condition
import l2server.gameserver.stats.funcs.FuncTemplate
import java.util.*

/**
 * @author mkizub
 */
class AbnormalTemplate(val applayCond: Condition?,
					   val counter: Int,
					   val duration: Int, // in seconds
					   val visualEffect: Array<VisualEffect>?,
					   val stackType: Array<String>,
					   val stackLvl: Byte,
					   val icon: Boolean,
					   val landRate: Double, // to handle chance
					   val effectType: AbnormalType, // to handle resistences etc...
					   val comboId: Int) {

	var funcTemplates: Array<FuncTemplate> = arrayOf()

	var effects: Array<EffectTemplate> = arrayOf()

	fun attach(effect: EffectTemplate) {
		effects = effects.plus(effect)
	}

	fun getEffect(env: Env): Abnormal {
		val list = ArrayList<L2Effect>()
		for (temp in effects) {
			list.add(temp.getEffect(env))
		}

		val effs = list.toTypedArray()
		val abnormal = Abnormal(env, this, effs)
		for (temp in effs) {
			temp.setAbnormal(abnormal)
		}

		return abnormal
	}

	/**
	 * Creates an L2Effect instance from an existing one and an Env object.
	 */
	fun getStolenEffect(env: Env, stolen: Abnormal): Abnormal {
		val list = ArrayList<L2Effect>()
		for (temp in stolen.effects) {
			list.add(EffectTemplate.getStolenEffect(env, temp))
		}

		val effs = list.toTypedArray()
		val abnormal = Abnormal(env, this, effs)
		for (temp in effs) {
			temp.abnormal = abnormal
		}

		return abnormal
	}

	fun attach(f: FuncTemplate) {
		funcTemplates = funcTemplates.plus(f)
	}
}
