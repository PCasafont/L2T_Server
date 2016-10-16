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

package l2server.gameserver.model.base;

import l2server.Config;
import l2server.gameserver.datatables.PlayerClassTable;

/**
 * Character Sub-Class Definition
 * <BR>
 * Used to store key information about a character's sub-class.
 *
 * @author Tempy
 */
public final class SubClass
{
	private static final byte maxLevel =
			Config.MAX_SUBCLASS_LEVEL < Config.MAX_LEVEL ? Config.MAX_SUBCLASS_LEVEL : Config.MAX_LEVEL;

	private PlayerClass playerClass;
	private long exp = Experience.getAbsoluteExp(40);
	private long sp = 0;
	private byte level = 40;
	private int classIndex = 1;
	private boolean isDual = false;
	private int certificates = 0;

	public SubClass(int classId, long exp, long sp, byte level, int classIndex, boolean isDual)
	{
		this.playerClass = PlayerClassTable.getInstance().getClassById(classId);
		this.exp = exp;
		this.sp = sp;
		this.level = level;
		this.classIndex = classIndex;
		this.isDual = isDual;
	}

	public SubClass(int classId, int classIndex)
	{
		// Used for defining a sub class using default values for XP, SP and player level.
		this.playerClass = PlayerClassTable.getInstance().getClassById(classId);
		this.classIndex = classIndex;
		if (Config.STARTING_LEVEL > 40)
		{
			this.level = Config.STARTING_LEVEL;
			if (this.level > getMaxLevel())
			{
				this.level = getMaxLevel();
			}
			this.exp = Experience.getAbsoluteExp(this.level);
		}
	}

	public SubClass()
	{
		// Used for specifying ALL attributes of a sub class directly,
		// using the preset default values.
		if (Config.STARTING_LEVEL > 40)
		{
			this.level = Config.STARTING_LEVEL;
			if (this.level > getMaxLevel())
			{
				this.level = getMaxLevel();
			}
			this.exp = Experience.getAbsoluteExp(this.level);
		}
	}

	public PlayerClass getClassDefinition()
	{
		return this.playerClass;
	}

	public int getClassId()
	{
		return this.playerClass.getId();
	}

	public long getExp()
	{
		return this.exp;
	}

	public long getSp()
	{
		return this.sp;
	}

	public byte getLevel()
	{
		return this.level;
	}

	public int getClassIndex()
	{
		return this.classIndex;
	}

	public boolean isDual()
	{
		return this.isDual;
	}

	public int getCertificates()
	{
		return this.certificates;
	}

	public byte getMaxLevel()
	{
		return this.isDual ? Config.MAX_LEVEL : this.maxLevel;
	}

	public void setClassId(int classId)
	{
		this.playerClass = PlayerClassTable.getInstance().getClassById(classId);
	}

	public void setExp(long expValue)
	{
		if (expValue > Experience.getAbsoluteExp(getMaxLevel() + 1) - 1)
		{
			expValue = Experience.getAbsoluteExp(getMaxLevel() + 1) - 1;
		}

		this.exp = expValue;
	}

	public void setSp(long spValue)
	{
		this.sp = spValue;
	}

	public void setClassIndex(int classIndex)
	{
		this.classIndex = classIndex;
	}

	public void setIsDual(boolean isDual)
	{
		this.isDual = isDual;
	}

	public void setCertificates(int certificates)
	{
		this.certificates = certificates;
	}

	public void setLevel(byte levelValue)
	{
		if (levelValue > getMaxLevel())
		{
			levelValue = getMaxLevel();
		}
		else if (levelValue < 40)
		{
			levelValue = 40;
		}

		this.level = levelValue;
	}

	public void incLevel()
	{
		if (getLevel() == getMaxLevel())
		{
			return;
		}

		this.level++;
		setExp(Experience.getAbsoluteExp(getLevel()));
	}

	public void decLevel()
	{
		if (getLevel() == 40)
		{
			return;
		}

		this.level--;
		setExp(Experience.getAbsoluteExp(getLevel()));
	}
}
