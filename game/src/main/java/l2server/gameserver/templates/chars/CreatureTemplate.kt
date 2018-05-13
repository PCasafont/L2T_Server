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

package l2server.gameserver.templates.chars

import l2server.gameserver.templates.StatsSet

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/02 15:57:51 $
 */
open class CreatureTemplate(set: StatsSet) {
	// BaseStats
	var baseSTR: Int = 0
	var baseCON: Int = 0
	var baseDEX: Int = 0
	var baseINT: Int = 0
	var baseWIT: Int = 0
	var baseMEN: Int = 0
	var baseLUC: Int = 0
	var baseCHA: Int = 0
	var baseHpMax: Double = 0.toDouble()
	var baseMpMax: Double = 0.toDouble()
	var baseCpMax: Double = 0.toDouble()

	var baseHpReg: Float = 0.toFloat()
	var baseMpReg: Float = 0.toFloat()
	var baseCpReg: Float = 0.toFloat()

	var basePAtk: Float = 0.toFloat()
	var baseMAtk: Float = 0.toFloat()
	var basePDef: Float = 0.toFloat()
	var baseMDef: Float = 0.toFloat()
	var basePAtkSpd: Int = 0
	var baseMAtkSpd: Int = 0
	val baseMReuseRate: Float
	val baseShldDef: Int
	var baseAtkRange: Int = 0
	val baseShldRate: Int
	var baseCritRate: Int = 0
	var baseMCritRate: Int = 0
	var baseWalkSpd: Float = 0.toFloat()
	var baseRunSpd: Float = 0.toFloat()

	// SpecialStats
	var baseFire: Int = 0
	var baseWind: Int = 0
	var baseWater: Int = 0
	var baseEarth: Int = 0
	var baseHoly: Int = 0
	var baseDark: Int = 0
	var baseFireRes: Double = 0.toDouble()
	var baseWindRes: Double = 0.toDouble()
	var baseWaterRes: Double = 0.toDouble()
	var baseEarthRes: Double = 0.toDouble()
	var baseHolyRes: Double = 0.toDouble()
	var baseDarkRes: Double = 0.toDouble()

	//C4 Stats
	val baseMpConsumeRate: Int
	val baseHpConsumeRate: Int

	//Start Locs
	val startX: Int
	val startY: Int
	val startZ: Int
	val startRandom: Int

	/**
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> :
	 * For client info use [fCollisionRadius]
	</B></FONT> * <BR></BR><BR></BR>
	 */
	val collisionRadius: Int

	/**
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> :
	 * For client info use [fCollisionHeight]
	</B></FONT> * <BR></BR><BR></BR>
	 */
	val collisionHeight: Int

	var fCollisionRadius: Double = 0.toDouble()
	var fCollisionHeight: Double = 0.toDouble()

	init {
		// Base stats
		baseSTR = set.getInteger("STR", 80)
		baseCON = set.getInteger("CON", 86)
		baseDEX = set.getInteger("DEX", 40)
		baseINT = set.getInteger("INT", 42)
		baseWIT = set.getInteger("WIT", 40)
		baseMEN = set.getInteger("MEN", 40)
		baseLUC = set.getInteger("LUC", 30)
		baseCHA = set.getInteger("CHA", 40)
		baseHpMax = set.getDouble("hpMax", 1.0f)
		baseCpMax = set.getDouble("cpMax", 0.0f)
		baseMpMax = set.getDouble("mpMax", 0.0f)
		baseHpReg = set.getFloat("hpReg", 0.0f)
		baseMpReg = set.getFloat("mpReg", 0.0f)
		baseCpReg = set.getFloat("cpReg", 0.0f)
		basePAtk = set.getFloat("pAtk", 1.0f)
		baseMAtk = set.getFloat("mAtk", 1.0f)
		basePDef = set.getFloat("pDef", 1.0f)
		baseMDef = set.getFloat("mDef", 1.0f)
		basePAtkSpd = set.getInteger("pAtkSpd", 253)
		baseMAtkSpd = set.getInteger("mAtkSpd", 333)
		baseMReuseRate = set.getFloat("mReuseDelay", 1.0f)
		baseShldDef = set.getInteger("shldDef", 0)
		baseAtkRange = set.getInteger("atkRange", 40)
		baseShldRate = set.getInteger("shldRate", 0)
		baseCritRate = set.getInteger("pCritRate", 40)
		baseMCritRate = set.getInteger("mCritRate", 50)
		baseWalkSpd = set.getFloat("walkSpd", 80f)
		baseRunSpd = set.getFloat("runSpd", 130f)

		// SpecialStats
		baseFire = set.getInteger("fire", 0)
		baseWind = set.getInteger("wind", 0)
		baseWater = set.getInteger("water", 0)
		baseEarth = set.getInteger("earth", 0)
		baseHoly = set.getInteger("holy", 0)
		baseDark = set.getInteger("dark", 0)
		baseFireRes = set.getInteger("fireRes", 0).toDouble()
		baseWindRes = set.getInteger("windRes", 0).toDouble()
		baseWaterRes = set.getInteger("waterRes", 0).toDouble()
		baseEarthRes = set.getInteger("earthRes", 0).toDouble()
		baseHolyRes = set.getInteger("holyRes", 0).toDouble()
		baseDarkRes = set.getInteger("darkRes", 0).toDouble()

		//C4 Stats
		baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0)
		baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0)

		// Geometry
		fCollisionRadius = set.getDouble("collisionRadius", 10.0f)
		fCollisionHeight = set.getDouble("collisionHeight", 24.0f)
		collisionRadius = fCollisionRadius.toInt()
		collisionHeight = fCollisionHeight.toInt()

		startX = set.getInteger("startX", -114535)
		startY = set.getInteger("startY", 259818)
		startZ = set.getInteger("startZ", -1203)
		startRandom = set.getInteger("startRandom", 300)
	}

	open fun getBaseHpReg(level: Int): Float {
		return baseHpReg
	}

	open fun getBaseMpReg(level: Int): Float {
		return baseMpReg
	}

	open fun getBaseCpReg(level: Int): Float {
		return baseCpReg
	}
}
