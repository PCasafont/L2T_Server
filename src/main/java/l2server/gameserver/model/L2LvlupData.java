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

package l2server.gameserver.model;

/**
 * This class ...
 *
 * @author NightMarez
 * @version $Revision: 1.2.2.1.2.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2LvlupData {
	private int classid;
	private int classLvl;
	private float classHpAdd;
	private float classHpBase;
	private float classHpModifier;
	private float classCpAdd;
	private float classCpBase;
	private float classCpModifier;
	private float classMpAdd;
	private float classMpBase;
	private float classMpModifier;

	/**
	 * @return Returns the classHpAdd.
	 */
	@Deprecated
	public float getClassHpAdd() {
		return classHpAdd;
	}

	/**
	 * @param hpAdd The classHpAdd to set.
	 */
	public void setClassHpAdd(float hpAdd) {
		classHpAdd = hpAdd;
	}

	/**
	 * @return Returns the classHpBase.
	 */
	@Deprecated
	public float getClassHpBase() {
		return classHpBase;
	}

	/**
	 * @param hpBase The classHpBase to set.
	 */
	public void setClassHpBase(float hpBase) {
		classHpBase = hpBase;
	}

	/**
	 * @return Returns the classHpModifier.
	 */
	@Deprecated
	public float getClassHpModifier() {
		return classHpModifier;
	}

	/**
	 * @param hpModifier The classHpModifier to set.
	 */
	public void setClassHpModifier(float hpModifier) {
		classHpModifier = hpModifier;
	}

	/**
	 * @return Returns the classCpAdd.
	 */
	@Deprecated
	public float getClassCpAdd() {
		return classCpAdd;
	}

	/**
	 */
	public void setClassCpAdd(float cpAdd) {
		classCpAdd = cpAdd;
	}

	/**
	 * @return Returns the classCpBase.
	 */
	@Deprecated
	public float getClassCpBase() {
		return classCpBase;
	}

	/**
	 */
	public void setClassCpBase(float cpBase) {
		classCpBase = cpBase;
	}

	/**
	 * @return Returns the classCpModifier.
	 */
	@Deprecated
	public float getClassCpModifier() {
		return classCpModifier;
	}

	/**
	 * @param cpModifier The classCpModifier to set.
	 */
	public void setClassCpModifier(float cpModifier) {
		classCpModifier = cpModifier;
	}

	/**
	 * @return Returns the classid.
	 */
	public int getCurrentClass() {
		return classid;
	}

	/**
	 */
	public void setClassid(int pClassid) {
		classid = pClassid;
	}

	/**
	 * @return Returns the classLvl.
	 */
	@Deprecated
	public int getClassLvl() {
		return classLvl;
	}

	/**
	 * @param lvl The classLvl to set.
	 */
	public void setClassLvl(int lvl) {
		classLvl = lvl;
	}

	/**
	 * @return Returns the classMpAdd.
	 */
	@Deprecated
	public float getClassMpAdd() {
		return classMpAdd;
	}

	/**
	 * @param mpAdd The classMpAdd to set.
	 */
	public void setClassMpAdd(float mpAdd) {
		classMpAdd = mpAdd;
	}

	/**
	 * @return Returns the classMpBase.
	 */
	@Deprecated
	public float getClassMpBase() {
		return classMpBase;
	}

	/**
	 * @param mpBase The classMpBase to set.
	 */
	public void setClassMpBase(float mpBase) {
		classMpBase = mpBase;
	}

	/**
	 * @return Returns the classMpModifier.
	 */
	@Deprecated
	public float getClassMpModifier() {
		return classMpModifier;
	}

	/**
	 * @param mpModifier The classMpModifier to set.
	 */
	public void setClassMpModifier(float mpModifier) {
		classMpModifier = mpModifier;
	}
}
