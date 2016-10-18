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
	 * @return Returns the classHpAdd.
	 */
	@Deprecated

	public void setClassHpAdd(float hpAdd)
	{
		classHpAdd = hpAdd;
	}

	/**
	 * @return Returns the classHpBase.
	 */
	@Deprecated

	public void setClassHpBase(float hpBase)
	{
		classHpBase = hpBase;
	}

	/**
	 * @return Returns the classHpModifier.
	 */
	@Deprecated

	public void setClassHpModifier(float hpModifier)
	{
		classHpModifier = hpModifier;
	}

	/**
	 * @return Returns the classCpAdd.
	 */
	@Deprecated

	public void setClassCpAdd(float cpAdd)
	{
		classCpAdd = cpAdd;
	}

	/**
	 * @return Returns the classCpBase.
	 */
	@Deprecated

	public void setClassCpBase(float cpBase)
	{
		classCpBase = cpBase;
	}

	/**
	 * @return Returns the classCpModifier.
	 */
	@Deprecated

	public void setClassCpModifier(float cpModifier)
	{
		classCpModifier = cpModifier;
	}

	/**
	 * @return Returns the classid.
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
	 * @return Returns the classLvl.
	 */
	@Deprecated

	public void setClassLvl(int lvl)
	{
		classLvl = lvl;
	}

	/**
	 * @return Returns the classMpAdd.
	 */
	@Deprecated

	public void setClassMpAdd(float mpAdd)
	{
		classMpAdd = mpAdd;
	}

	/**
	 * @return Returns the classMpBase.
	 */
	@Deprecated

	public void setClassMpBase(float mpBase)
	{
		classMpBase = mpBase;
	}

	/**
	 * @return Returns the classMpModifier.
	 */
	@Deprecated

	public void setClassMpModifier(float mpModifier)
	{
		classMpModifier = mpModifier;
	}
}
