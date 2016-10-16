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
import lombok.Getter;
import lombok.Setter;

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
	@Getter private long exp = Experience.getAbsoluteExp(40);
	@Getter private long sp = 0;
	@Getter private byte level = 40;
	@Getter @Setter private int classIndex = 1;
	@Setter private boolean isDual = false;
	@Getter @Setter private int certificates = 0;

	public SubClass(int classId, long exp, long sp, byte level, int classIndex, boolean isDual)
	{
		playerClass = PlayerClassTable.getInstance().getClassById(classId);
		this.exp = exp;
		this.sp = sp;
		this.level = level;
		this.classIndex = classIndex;
		this.isDual = isDual;
	}

	public SubClass(int classId, int classIndex)
	{
		// Used for defining a sub class using default values for XP, SP and player level.
		playerClass = PlayerClassTable.getInstance().getClassById(classId);
		this.classIndex = classIndex;
		if (Config.STARTING_LEVEL > 40)
		{
			level = Config.STARTING_LEVEL;
			if (level > getMaxLevel())
			{
				level = getMaxLevel();
			}
			exp = Experience.getAbsoluteExp(level);
		}
	}

	public SubClass()
	{
		// Used for specifying ALL attributes of a sub class directly,
		// using the preset default values.
		if (Config.STARTING_LEVEL > 40)
		{
			level = Config.STARTING_LEVEL;
			if (level > getMaxLevel())
			{
				level = getMaxLevel();
			}
			exp = Experience.getAbsoluteExp(level);
		}
	}

	public PlayerClass getClassDefinition()
	{
		return playerClass;
	}

	public int getClassId()
	{
		return playerClass.getId();
	}

	public boolean isDual()
	{
		return isDual;
	}

	public byte getMaxLevel()
	{
		return isDual ? Config.MAX_LEVEL : maxLevel;
	}

	public void setClassId(int classId)
	{
		playerClass = PlayerClassTable.getInstance().getClassById(classId);
	}

	public void setExp(long expValue)
	{
		if (expValue > Experience.getAbsoluteExp(getMaxLevel() + 1) - 1)
		{
			expValue = Experience.getAbsoluteExp(getMaxLevel() + 1) - 1;
		}

		exp = expValue;
	}

	public void setSp(long spValue)
	{
		sp = spValue;
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

		level = levelValue;
	}

	public void incLevel()
	{
		if (getLevel() == getMaxLevel())
		{
			return;
		}

		level++;
		setExp(Experience.getAbsoluteExp(getLevel()));
	}

	public void decLevel()
	{
		if (getLevel() == 40)
		{
			return;
		}

		level--;
		setExp(Experience.getAbsoluteExp(getLevel()));
	}
}
