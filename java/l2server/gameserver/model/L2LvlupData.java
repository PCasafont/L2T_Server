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

import lombok.Getter;
/**
 * This class ...
 *
 * @author NightMarez
 * @version $Revision: 1.2.2.1.2.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2LvlupData
{
	private int classid;
	@Getter private int classLvl;
	@Getter private float classHpAdd;
	@Getter private float classHpBase;
	@Getter private float classHpModifier;
	@Getter private float classCpAdd;
	@Getter private float classCpBase;
	@Getter private float classCpModifier;
	@Getter private float classMpAdd;
	@Getter private float classMpBase;
	@Getter private float classMpModifier;

	/**
	 * @return Returns the this.classHpAdd.
	 */
	@Deprecated

	/**
	 * @param hpAdd The this.classHpAdd to set.
	 */
	public void setClassHpAdd(float hpAdd)
	{
		classHpAdd = hpAdd;
	}

	/**
	 * @return Returns the this.classHpBase.
	 */
	@Deprecated

	/**
	 * @param hpBase The this.classHpBase to set.
	 */
	public void setClassHpBase(float hpBase)
	{
		classHpBase = hpBase;
	}

	/**
	 * @return Returns the this.classHpModifier.
	 */
	@Deprecated

	/**
	 * @param hpModifier The this.classHpModifier to set.
	 */
	public void setClassHpModifier(float hpModifier)
	{
		classHpModifier = hpModifier;
	}

	/**
	 * @return Returns the this.classCpAdd.
	 */
	@Deprecated

	/**
	 */
	public void setClassCpAdd(float cpAdd)
	{
		classCpAdd = cpAdd;
	}

	/**
	 * @return Returns the this.classCpBase.
	 */
	@Deprecated

	/**
	 */
	public void setClassCpBase(float cpBase)
	{
		classCpBase = cpBase;
	}

	/**
	 * @return Returns the this.classCpModifier.
	 */
	@Deprecated

	/**
	 * @param cpModifier The this.classCpModifier to set.
	 */
	public void setClassCpModifier(float cpModifier)
	{
		classCpModifier = cpModifier;
	}

	/**
	 * @return Returns the this.classid.
	 */
	public int getCurrentClass()
	{
		return classid;
	}

	/**
	 */
	public void setClassid(int pClassid)
	{
		classid = pClassid;
	}

	/**
	 * @return Returns the this.classLvl.
	 */
	@Deprecated

	/**
	 * @param lvl The this.classLvl to set.
	 */
	public void setClassLvl(int lvl)
	{
		classLvl = lvl;
	}

	/**
	 * @return Returns the this.classMpAdd.
	 */
	@Deprecated

	/**
	 * @param mpAdd The this.classMpAdd to set.
	 */
	public void setClassMpAdd(float mpAdd)
	{
		classMpAdd = mpAdd;
	}

	/**
	 * @return Returns the this.classMpBase.
	 */
	@Deprecated

	/**
	 * @param mpBase The this.classMpBase to set.
	 */
	public void setClassMpBase(float mpBase)
	{
		classMpBase = mpBase;
	}

	/**
	 * @return Returns the this.classMpModifier.
	 */
	@Deprecated

	/**
	 * @param mpModifier The this.classMpModifier to set.
	 */
	public void setClassMpModifier(float mpModifier)
	{
		classMpModifier = mpModifier;
	}
}
