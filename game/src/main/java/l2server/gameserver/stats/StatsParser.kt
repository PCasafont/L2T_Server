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

package l2server.gameserver.stats

import l2server.gameserver.datatables.ItemTable
import l2server.gameserver.model.InstanceType
import l2server.gameserver.model.Skill
import l2server.gameserver.model.base.PlayerState
import l2server.gameserver.model.base.Race
import l2server.gameserver.stats.conditions.*
import l2server.gameserver.stats.conditions.ConditionGameTime.CheckGameTime
import l2server.gameserver.stats.funcs.*
import l2server.gameserver.templates.StatsSet
import l2server.gameserver.templates.item.ArmorType
import l2server.gameserver.templates.item.ItemTemplate
import l2server.gameserver.templates.item.WeaponType
import l2server.gameserver.templates.skills.AbnormalTemplate
import l2server.util.xml.XmlNode
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author mkizub
 */
abstract class StatsParser internal constructor(protected var node: XmlNode) {

	var id: Int = 0
		protected set
	var name: String
		protected set

	protected abstract val statsSet: StatsSet

	init {
		id = node.getInt("id")
		name = node.getString("name")
	}

	@Throws(RuntimeException::class)
	abstract fun parse()

	protected fun parseTemplate(node: XmlNode, template: Any) {
		val firstNode = node.firstChild ?: return

		if (firstNode.name.equals("cond", ignoreCase = true)) {
			val condition = parseCondition(firstNode.firstChild, template)
			if (condition != null) {
				if (firstNode.hasAttribute("msg")) {
					condition.message = firstNode.getString("msg")
				} else if (firstNode.hasAttribute("msgId")) {
					condition.messageId = Integer.decode(getValue(firstNode.getString("msgId")))!!
					if (firstNode.hasAttribute("addName") && Integer.decode(getValue(firstNode.getString("msgId"))) > 0) {
						condition.addName()
					}
				}

				if (template is Skill) {
					template.attach(condition, false)
				} else if (template is ItemTemplate) {
					template.attach(condition)
				}
			}
		}
		for (n in node.getChildren()) {
			val nodeType = n.name
			// Custom for the critical damage buffs/passives
			//if (nodeType.equalsIgnoreCase("addPercent")
			//		&& n.getString("stat").equalsIgnoreCase("pCritDmg"))
			//	nodeType = "addPercentBase";

			if (nodeType.equals("add", ignoreCase = true)) {
				attachFunc(n, template, "Add")
			} else if (nodeType.equals("sub", ignoreCase = true)) {
				attachFunc(n, template, "Sub")
			} else if (nodeType.equals("baseAdd", ignoreCase = true)) {
				attachFunc(n, template, "BaseAdd")
			} else if (nodeType.equals("baseSub", ignoreCase = true)) {
				attachFunc(n, template, "BaseSub")
			} else if (nodeType.equals("addPercent", ignoreCase = true)) {
				attachFunc(n, template, "AddPercent")
			} else if (nodeType.equals("subPercent", ignoreCase = true)) {
				attachFunc(n, template, "SubPercent")
			} else if (nodeType.equals("addPercentBase", ignoreCase = true)) {
				attachFunc(n, template, "AddPercentBase")
			} else if (nodeType.equals("subPercentBase", ignoreCase = true)) {
				attachFunc(n, template, "SubPercentBase")
			} else if (nodeType.equals("mul", ignoreCase = true)) {
				attachFunc(n, template, "Mul")
			} else if (nodeType.equals("baseMul", ignoreCase = true)) {
				attachFunc(n, template, "BaseMul")
			} else if (nodeType.equals("set", ignoreCase = true)) {
				attachFunc(n, template, "Set")
			} else if (nodeType.equals("override", ignoreCase = true)) {
				attachFunc(n, template, "Override")
			} else if (nodeType.equals("enchant", ignoreCase = true)) {
				attachFunc(n, template, "Enchant")
			} else if (nodeType.equals("enchanthp", ignoreCase = true)) {
				attachFunc(n, template, "EnchantHp")
			} else if (nodeType.equals("abnormal", ignoreCase = true)) {
				if (this !is SkillParser && template !is Skill) {
					throw RuntimeException("Abnormals in something that's not a skill")
				}
				(this as SkillParser).attachAbnormal(n, template as Skill)
			} else if (nodeType.equals("effect", ignoreCase = true)) {
				if (this !is SkillParser && template !is AbnormalTemplate) {
					throw RuntimeException("Effects in something that's not an abnormal")
				}
				(this as SkillParser).attachEffect(n, template as AbnormalTemplate)
			}
		}
	}

	protected fun attachFunc(n: XmlNode, template: Any, name: String) {
		val stat = Stats.fromString(n.getString("stat"))
		val lambda = getLambda(n, template)
		val applayCond = parseCondition(n.firstChild, template)
		val ft = FuncTemplate(applayCond, name, stat, lambda)
		if (template is ItemTemplate) {
			template.attach(ft)
		} else if (template is Skill) {
			template.attach(ft)
		} else if (template is AbnormalTemplate) {
			template.attach(ft)
		}
	}

	protected fun attachLambdaFunc(n: XmlNode, template: Any, calc: LambdaCalc) {
		var name = n.name
		val sb = StringBuilder(name)
		sb.setCharAt(0, Character.toUpperCase(name[0]))
		name = sb.toString()
		val lambda = getLambda(n, template)
		val ft = FuncTemplate(null, name, null, lambda)
		calc.addFunc(ft.getFunc(calc))
	}

	protected fun parseCondition(n: XmlNode?, template: Any): Condition? {
		if (n == null) {
			return null
		}
		if (n.name.equals("and", ignoreCase = true)) {
			return parseLogicAnd(n, template)
		}
		if (n.name.equals("or", ignoreCase = true)) {
			return parseLogicOr(n, template)
		}
		if (n.name.equals("not", ignoreCase = true)) {
			return parseLogicNot(n, template)
		}
		if (n.name.equals("player", ignoreCase = true)) {
			return parsePlayerCondition(n, template)
		}
		if (n.name.equals("target", ignoreCase = true)) {
			return parseTargetCondition(n, template)
		}
		if (n.name.equals("using", ignoreCase = true)) {
			return parseUsingCondition(n)
		}
		return if (n.name.equals("game", ignoreCase = true)) {
			parseGameCondition(n)
		} else null
	}

	protected fun parseLogicAnd(node: XmlNode, template: Any): Condition {
		val cond = ConditionLogicAnd()
		for (n in node.getChildren()) {
			cond.add(parseCondition(n, template))
		}

		if (cond.conditions == null || cond.conditions.size == 0) {
			log.error("Empty <and> condition in $name")
		}
		return cond
	}

	protected fun parseLogicOr(node: XmlNode, template: Any): Condition {
		val cond = ConditionLogicOr()
		for (n in node.getChildren()) {
			cond.add(parseCondition(n, template))
		}

		if (cond.conditions == null || cond.conditions.size == 0) {
			log.error("Empty <or> condition in $name")
		}
		return cond
	}

	protected fun parseLogicNot(node: XmlNode, template: Any): Condition? {
		if (node.firstChild != null) {
			return ConditionLogicNot(parseCondition(node.firstChild, template))
		}

		log.error("Empty <not> condition in $name")
		return null
	}

	protected fun parsePlayerCondition(n: XmlNode, template: Any): Condition? {
		var cond: Condition? = null
		val forces = ByteArray(2)
		for ((key, value1) in n.getAttributes()) {
			if (key.equals("races", ignoreCase = true)) {
				val racesVal = value1.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				val races = arrayOfNulls<Race>(racesVal.size)
				for (r in racesVal.indices) {
					if (racesVal[r] != null) {
						races[r] = Race.valueOf(racesVal[r])
					}
				}
				cond = joinAnd(cond, ConditionPlayerRace(races))
			} else if (key.equals("level", ignoreCase = true)) {
				val lvl = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerLevel(lvl))
			} else if (key.equals("levelRange", ignoreCase = true)) {
				val range = getValue(value1).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				if (range.size == 2) {
					val lvlRange = IntArray(2)
					lvlRange[0] = Integer.decode(getValue(value1).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])!!
					lvlRange[1] = Integer.decode(getValue(value1).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])!!
					cond = joinAnd(cond, ConditionPlayerLevelRange(lvlRange))
				}
			} else if (key.equals("resting", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.RESTING, `val`))
			} else if (key.equals("flying", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.FLYING, `val`))
			} else if (key.equals("moving", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.MOVING, `val`))
			} else if (key.equals("running", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.RUNNING, `val`))
			} else if (key.equals("standing", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.STANDING, `val`))
			} else if (key.equals("combat", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.COMBAT, `val`))
			} else if (key.equals("behind", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.BEHIND, `val`))
			} else if (key.equals("front", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.FRONT, `val`))
			} else if (key.equals("chaotic", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.CHAOTIC, `val`))
			} else if (key.equals("olympiad", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerState(PlayerState.OLYMPIAD, `val`))
			} else if (key.equals("ishero", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerIsHero(`val`))
			} else if (key.equals("hp", ignoreCase = true)) {
				val hp = java.lang.Float.parseFloat(getValue(value1)).toInt()
				cond = joinAnd(cond, ConditionPlayerHp(hp))
			} else if (key.equals("mp", ignoreCase = true)) {
				val hp = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerMp(hp))
			} else if (key.equals("cp", ignoreCase = true)) {
				val cp = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerCp(cp))
			} else if (key.equals("grade", ignoreCase = true)) {
				val expIndex = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerGrade(expIndex))
			} else if (key.equals("pkCount", ignoreCase = true)) {
				val expIndex = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerPkCount(expIndex))
			} else if (key.equals("siegezone", ignoreCase = true)) {
				val value = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionSiegeZone(value, true))
			} else if (key.equals("siegeside", ignoreCase = true)) {
				val value = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerSiegeSide(value))
			} else if ("battle_force".equals(key, ignoreCase = true)) {
				forces[0] = java.lang.Byte.decode(getValue(value1))!!
			} else if ("spell_force".equals(key, ignoreCase = true)) {
				forces[1] = java.lang.Byte.decode(getValue(value1))!!
			} else if (key.equals("charges", ignoreCase = true)) {
				val value = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerCharges(value))
			} else if (key.equals("souls", ignoreCase = true)) {
				val value = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerSouls(value))
			} else if (key.equals("weight", ignoreCase = true)) {
				val weight = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerWeight(weight))
			} else if (key.equals("invSize", ignoreCase = true)) {
				val size = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerInvSize(size))
			} else if (key.equals("isClanLeader", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerIsClanLeader(`val`))
			} else if (key.equals("clanLeaderOn", ignoreCase = true)) {
				cond = joinAnd(cond, ConditionPlayerClanLeaderIsOn())
			} else if (key.equals("insideZoneId", ignoreCase = true)) {
				val id = Integer.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerInsideZoneId(id))
			} else if (key.equals("onEvent", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerEvent(`val`))
			} else if (key.equals("onSurvivalEvent", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerSurvivalEvent(`val`))
			} else if (key.equals("pledgeClass", ignoreCase = true)) {
				val pledgeClass = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerPledgeClass(pledgeClass))
			} else if (key.equals("clanHall", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionPlayerHasClanHall(array))
			} else if (key.equals("fort", ignoreCase = true)) {
				val fort = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerHasFort(fort))
			} else if (key.equals("castle", ignoreCase = true)) {
				val castle = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerHasCastle(castle))
			} else if (key.equals("sex", ignoreCase = true)) {
				val sex = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerSex(sex))
			} else if (key.equals("flyMounted", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerFlyMounted(`val`))
			} else if (key.equals("landingZone", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerLandingZone(`val`))
			} else if ("active_effect_id".equals(key, ignoreCase = true)) {
				val effect_id = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerActiveEffectId(effect_id))
			} else if ("active_effect_id_lvl".equals(key, ignoreCase = true)) {
				val `val` = getValue(value1)
				val effect_id = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]))!!
				val effect_lvl = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]))!!
				cond = joinAnd(cond, ConditionPlayerActiveEffectId(effect_id, effect_lvl))
			} else if ("active_effect".equals(key, ignoreCase = true)) {
				val `val` = getValue(value1)
				val effectName = getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
				cond = joinAnd(cond, ConditionPlayerActiveEffect(effectName))
			} else if ("active_skill_id".equals(key, ignoreCase = true)) {
				val skill_id = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionPlayerActiveSkillId(skill_id))
			} else if ("active_skill_id_lvl".equals(key, ignoreCase = true)) {
				val `val` = getValue(value1)
				val skill_id = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]))!!
				val skill_lvl = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]))!!
				cond = joinAnd(cond, ConditionPlayerActiveSkillId(skill_id, skill_lvl))
			} else if ("class_id_restriction".equals(key, ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionPlayerClassIdRestriction(array))
			} else if ("starts_with_class_name_restriction".equals(key, ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<String>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(getValue(item))
				}
				cond = joinAnd(cond, ConditionPlayerClassNameStartsWith(array))
			} else if (key.equals("subclass", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerSubclass(`val`))
			} else if (key.equals("instanceId", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionPlayerInstanceId(array))
			} else if (key.equals("agathionId", ignoreCase = true)) {
				val agathionId = Integer.decode(value1)!!
				cond = joinAnd(cond, ConditionPlayerAgathionId(agathionId))
			} else if (key.equals("cloakStatus", ignoreCase = true)) {
				val `val` = Integer.valueOf(value1)
				cond = joinAnd(cond, ConditionPlayerCloakStatus(`val`))
			} else if (key.equals("hasPet", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionPlayerHasPet(array))
			} else if (key.equals("hasPet", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionPlayerHasPet(array))
			} else if (key.equals("servitorNpcId", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionPlayerServitorNpcId(array))
			} else if (key.equals("hasSummon", ignoreCase = true)) {
				val `val` = java.lang.Boolean.parseBoolean(getValue(value1))
				cond = joinAnd(cond, ConditionPlayerHasSummon(`val`))
			} else if (key.equals("npcIdRadius", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				var npcId = 0
				var radius = 0
				if (st.countTokens() > 1) {
					npcId = Integer.decode(getValue(st.nextToken().trim { it <= ' ' }))!!
					radius = Integer.decode(getValue(st.nextToken().trim { it <= ' ' }))!!
				}
				cond = joinAnd(cond, ConditionPlayerRangeFromNpc(npcId, radius))
			}
		}

		if (forces[0] + forces[1] > 0) {
			cond = joinAnd(cond, ConditionForceBuff(forces))
		}

		if (cond == null) {
			log.error("Unrecognized <player> condition in $name")
		}
		return cond
	}

	protected fun parseTargetCondition(n: XmlNode, template: Any): Condition? {
		var cond: Condition? = null
		for ((key, value1) in n.getAttributes()) {
			if (key.equals("hp", ignoreCase = true)) {
				val hp = Math.round(java.lang.Float.parseFloat(getValue(value1)))
				cond = joinAnd(cond, ConditionTargetHp(hp))
			} else if (key.equals("aggro", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value1)
				cond = joinAnd(cond, ConditionTargetAggro(`val`))
			} else if (key.equals("siegezone", ignoreCase = true)) {
				val value = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionSiegeZone(value, false))
			} else if (key.equals("level", ignoreCase = true)) {
				val lvl = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionTargetLevel(lvl))
			} else if (key.equals("playable", ignoreCase = true)) {
				cond = joinAnd(cond, ConditionTargetPlayable())
			} else if ("class_id_restriction".equals(key, ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionTargetClassIdRestriction(array))
			} else if ("active_effect_id".equals(key, ignoreCase = true)) {
				val effect_id = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionTargetActiveEffectId(effect_id))
			} else if ("active_effect_id_lvl".equals(key, ignoreCase = true)) {
				val `val` = getValue(value1)
				val effect_id = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]))!!
				val effect_lvl = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]))!!
				cond = joinAnd(cond, ConditionTargetActiveEffectId(effect_id, effect_lvl))
			} else if ("active_effect".equals(key, ignoreCase = true)) {
				val effectName = getValue(value1)
				cond = joinAnd(cond, ConditionTargetActiveEffect(effectName))
			} else if ("active_skill_id".equals(key, ignoreCase = true)) {
				val skill_id = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionTargetActiveSkillId(skill_id))
			} else if ("active_skill_id_lvl".equals(key, ignoreCase = true)) {
				val `val` = getValue(value1)
				val skill_id = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]))!!
				val skill_lvl = Integer.decode(getValue(`val`.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]))!!
				cond = joinAnd(cond, ConditionTargetActiveSkillId(skill_id, skill_lvl))
			} else if (key.equals("abnormal", ignoreCase = true)) {
				val abnormalId = Integer.decode(getValue(value1))!!
				cond = joinAnd(cond, ConditionTargetAbnormal(abnormalId))
			} else if (key.equals("mindistance", ignoreCase = true)) {
				val distance = java.lang.Float.parseFloat(getValue(value1))
				cond = joinAnd(cond, ConditionMinDistance(Math.round(distance * distance)))
			} else if ("race_id".equals(key, ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionTargetRaceId(array))
			} else if (key.equals("races", ignoreCase = true)) {
				val racesVal = value1.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				val races = arrayOfNulls<Race>(racesVal.size)
				for (r in racesVal.indices) {
					if (racesVal[r] != null) {
						races[r] = Race.valueOf(racesVal[r])
					}
				}
				cond = joinAnd(cond, ConditionTargetRace(races))
			} else if (key.equals("using", ignoreCase = true)) {
				var mask = 0
				val st = StringTokenizer(value1, ",")
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					for (wt in WeaponType.values()) {
						if (wt.toString() == item) {
							mask = mask or wt.mask()
							break
						}
					}
					for (at in ArmorType.values()) {
						if (at.toString() == item) {
							mask = mask or at.mask()
							break
						}
					}
				}
				cond = joinAnd(cond, ConditionTargetUsesWeaponKind(mask))
			} else if (key.equals("npcId", ignoreCase = true)) {
				val st = StringTokenizer(value1, ",")
				val array = ArrayList<Int>(st.countTokens())
				while (st.hasMoreTokens()) {
					val item = st.nextToken().trim { it <= ' ' }
					array.add(Integer.decode(getValue(item)))
				}
				cond = joinAnd(cond, ConditionTargetNpcId(array))
			} else if (key.equals("npcType", ignoreCase = true)) {
				val values = getValue(value1).trim { it <= ' ' }
				val valuesSplit = values.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

				val types = arrayOfNulls<InstanceType>(valuesSplit.size)
				var type: InstanceType?

				for (j in valuesSplit.indices) {
					type = InstanceType.valueOf(valuesSplit[j])
					if (type == null) {
						throw IllegalArgumentException("Instance type not recognized: " + valuesSplit[j])
					}
					types[j] = type
				}

				cond = joinAnd(cond, ConditionTargetNpcType(types))
			} else {
				log.error("Unrecognized <target> $key condition in $name")
			}// used for pc race
			// used for npc race
		}
		return cond
	}

	protected fun parseUsingCondition(n: XmlNode): Condition? {
		var cond: Condition? = null
		for ((key, value) in n.getAttributes()) {
			if (key.equals("kind", ignoreCase = true)) {
				var mask = 0
				val st = StringTokenizer(value, ",")
				while (st.hasMoreTokens()) {
					val old = mask
					val item = st.nextToken().trim { it <= ' ' }
					ItemTable.weaponTypes[item]?.let {
						mask = mask or it.mask()
					}

					ItemTable.armorTypes[item]?.let {
						mask = mask or it.mask()
					}

					if (item == "crossbow") {
						mask = mask or WeaponType.CROSSBOWK.mask()
					}

					if (old == mask) {
						log.info("[parseUsingCondition=\"kind\"] Unknown item type name: $item")
					}
				}
				cond = joinAnd(cond, ConditionUsingItemType(mask))
			} else if (key.equals("skill", ignoreCase = true)) {
				val id = Integer.parseInt(value)
				cond = joinAnd(cond, ConditionUsingSkill(id))
			} else if (key.equals("slotitem", ignoreCase = true)) {
				val st = StringTokenizer(value, ";")
				val id = Integer.parseInt(st.nextToken().trim { it <= ' ' })
				val slot = Integer.parseInt(st.nextToken().trim { it <= ' ' })
				var enchant = 0
				if (st.hasMoreTokens()) {
					enchant = Integer.parseInt(st.nextToken().trim { it <= ' ' })
				}
				cond = joinAnd(cond, ConditionSlotItemId(slot, id, enchant))
			} else if (key.equals("weaponChange", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value)
				cond = joinAnd(cond, ConditionChangeWeapon(`val`))
			}
		}
		if (cond == null) {
			log.error("Unrecognized <using> condition in $name")
		}
		return cond
	}

	protected fun parseGameCondition(n: XmlNode): Condition? {
		var cond: Condition? = null
		for ((key, value) in n.getAttributes()) {
			if (key.equals("skill", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value)
				cond = joinAnd(cond, ConditionWithSkill(`val`))
			}
			if (key.equals("night", ignoreCase = true)) {
				val `val` = java.lang.Boolean.valueOf(value)
				cond = joinAnd(cond, ConditionGameTime(CheckGameTime.NIGHT, `val`))
			}
			if (key.equals("chance", ignoreCase = true)) {
				val `val` = Integer.decode(getValue(value))!!
				cond = joinAnd(cond, ConditionGameChance(`val`))
			}
		}
		if (cond == null) {
			log.error("Unrecognized <game> condition in $name")
		}
		return cond
	}

	protected fun getLambda(node: XmlNode?, template: Any): Lambda {
		var node = node
		if (node!!.hasAttribute("val")) {
			val `val` = node.getString("val")
			if (`val`[0] == '$') {
				if (`val`.equals("\$player_level", ignoreCase = true)) {
					return LambdaStats(LambdaStats.StatsType.PLAYER_LEVEL)
				}
				if (`val`.equals("\$target_level", ignoreCase = true)) {
					return LambdaStats(LambdaStats.StatsType.TARGET_LEVEL)
				}
				if (`val`.equals("\$player_max_hp", ignoreCase = true)) {
					return LambdaStats(LambdaStats.StatsType.PLAYER_MAX_HP)
				}
				if (`val`.equals("\$player_max_mp", ignoreCase = true)) {
					return LambdaStats(LambdaStats.StatsType.PLAYER_MAX_MP)
				}
				// try to find value out of item fields
				val set = statsSet
				val field = set.getString(`val`.substring(1))
				if (field != null) {
					return LambdaConst(java.lang.Double.parseDouble(getValue(field)))
				}
				// failed
				throw IllegalArgumentException("Unknown value $`val`")
			} else {
				return LambdaConst(java.lang.Double.parseDouble(getValue(`val`)))
			}
		}
		val calc = LambdaCalc()
		node = node.firstChild
		if (node == null || "val" != node.name) {
			throw IllegalArgumentException("Value not specified")
		}

		for (n in node.getChildren()) {
			attachLambdaFunc(n, template, calc)
		}

		return calc
	}

	protected open fun getValue(value: String): String {
		return value
	}

	protected fun joinAnd(cond: Condition?, c: Condition): Condition {
		if (cond == null) {
			return c
		}
		if (cond is ConditionLogicAnd) {
			cond.add(c)
			return cond
		}

		val and = ConditionLogicAnd()
		and.add(cond)
		and.add(c)
		return and
	}

	companion object {
		private val log = LoggerFactory.getLogger(StatsParser::class.java.name)
	}
}
