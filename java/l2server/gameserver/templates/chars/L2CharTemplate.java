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

package l2server.gameserver.templates.chars;

import l2server.gameserver.templates.StatsSet;

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/02 15:57:51 $
 */
public class L2CharTemplate
{
	// BaseStats
	public int baseSTR;
	public int baseCON;
	public int baseDEX;
	public int baseINT;
	public int baseWIT;
	public int baseMEN;
	public int baseLUC;
	public int baseCHA;
	public double baseHpMax;
	public double baseMpMax;
	public double baseCpMax;

	public float baseHpReg;
	public float baseMpReg;
	public float baseCpReg;

	public float basePAtk;
	public float baseMAtk;
	public float basePDef;
	public float baseMDef;
	public int basePAtkSpd;
	public int baseMAtkSpd;
	public final float baseMReuseRate;
	public final int baseShldDef;
	public int baseAtkRange;
	public final int baseShldRate;
	public int baseCritRate;
	public int baseMCritRate;
	public float baseWalkSpd;
	public float baseRunSpd;

	// SpecialStats
	public int baseFire;
	public int baseWind;
	public int baseWater;
	public int baseEarth;
	public int baseHoly;
	public int baseDark;
	public double baseFireRes;
	public double baseWindRes;
	public double baseWaterRes;
	public double baseEarthRes;
	public double baseHolyRes;
	public double baseDarkRes;

	//C4 Stats
	public final int baseMpConsumeRate;
	public final int baseHpConsumeRate;

	//Start Locs
	public final int startX;
	public final int startY;
	public final int startZ;
	public final int startRandom;

	/**
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> :
	 * For client info use {@link fCollisionRadius}
	 * </B></FONT><BR><BR>
	 */
	public final int collisionRadius;

	/**
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> :
	 * For client info use {@link fCollisionHeight}
	 * </B></FONT><BR><BR>
	 */
	public final int collisionHeight;

	public double fCollisionRadius;
	public double fCollisionHeight;

	public L2CharTemplate(StatsSet set)
	{
		// Base stats
		baseSTR = set.getInteger("STR", 80);
		baseCON = set.getInteger("CON", 86);
		baseDEX = set.getInteger("DEX", 40);
		baseINT = set.getInteger("INT", 42);
		baseWIT = set.getInteger("WIT", 40);
		baseMEN = set.getInteger("MEN", 40);
		baseLUC = set.getInteger("LUC", 30);
		baseCHA = set.getInteger("CHA", 40);
		baseHpMax = set.getDouble("hpMax", 1.0f);
		baseCpMax = set.getDouble("cpMax", 0.0f);
		baseMpMax = set.getDouble("mpMax", 0.0f);
		baseHpReg = set.getFloat("hpReg", 0.0f);
		baseMpReg = set.getFloat("mpReg", 0.0f);
		baseCpReg = set.getFloat("cpReg", 0.0f);
		basePAtk = set.getFloat("pAtk", 1.0f);
		baseMAtk = set.getFloat("mAtk", 1.0f);
		basePDef = set.getFloat("pDef", 1.0f);
		baseMDef = set.getFloat("mDef", 1.0f);
		basePAtkSpd = set.getInteger("pAtkSpd", 253);
		baseMAtkSpd = set.getInteger("mAtkSpd", 333);
		baseMReuseRate = set.getFloat("mReuseDelay", 1.0f);
		baseShldDef = set.getInteger("shldDef", 0);
		baseAtkRange = set.getInteger("atkRange", 40);
		baseShldRate = set.getInteger("shldRate", 0);
		baseCritRate = set.getInteger("pCritRate", 40);
		baseMCritRate = set.getInteger("mCritRate", 50);
		baseWalkSpd = set.getFloat("walkSpd", 80);
		baseRunSpd = set.getFloat("runSpd", 130);

		// SpecialStats
		baseFire = set.getInteger("fire", 0);
		baseWind = set.getInteger("wind", 0);
		baseWater = set.getInteger("water", 0);
		baseEarth = set.getInteger("earth", 0);
		baseHoly = set.getInteger("holy", 0);
		baseDark = set.getInteger("dark", 0);
		baseFireRes = set.getInteger("fireRes", 0);
		baseWindRes = set.getInteger("windRes", 0);
		baseWaterRes = set.getInteger("waterRes", 0);
		baseEarthRes = set.getInteger("earthRes", 0);
		baseHolyRes = set.getInteger("holyRes", 0);
		baseDarkRes = set.getInteger("darkRes", 0);

		//C4 Stats
		baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
		baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);

		// Geometry
		fCollisionRadius = set.getDouble("collisionRadius", 10.0f);
		fCollisionHeight = set.getDouble("collisionHeight", 24.0f);
		collisionRadius = (int) fCollisionRadius;
		collisionHeight = (int) fCollisionHeight;

		startX = set.getInteger("startX", -114535);
		startY = set.getInteger("startY", 259818);
		startZ = set.getInteger("startZ", -1203);
		startRandom = set.getInteger("startRandom", 300);
	}

	public float getBaseHpReg(int level)
	{
		return baseHpReg;
	}

	public float getBaseMpReg(int level)
	{
		return baseMpReg;
	}

	public float getBaseCpReg(int level)
	{
		return baseCpReg;
	}
}
