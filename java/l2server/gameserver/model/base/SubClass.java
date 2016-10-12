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
	private static final byte _maxLevel =
			Config.MAX_SUBCLASS_LEVEL < Config.MAX_LEVEL ? Config.MAX_SUBCLASS_LEVEL : Config.MAX_LEVEL;

	private PlayerClass _class;
	private long _exp = Experience.getAbsoluteExp(40);
	private long _sp = 0;
	private byte _level = 40;
	private int _classIndex = 1;
	private boolean _isDual = false;
	private int _certificates = 0;

	public SubClass(int classId, long exp, long sp, byte level, int classIndex, boolean isDual)
	{
		_class = PlayerClassTable.getInstance().getClassById(classId);
		_exp = exp;
		_sp = sp;
		_level = level;
		_classIndex = classIndex;
		_isDual = isDual;
	}

	public SubClass(int classId, int classIndex)
	{
		// Used for defining a sub class using default values for XP, SP and player level.
		_class = PlayerClassTable.getInstance().getClassById(classId);
		_classIndex = classIndex;
		if (Config.STARTING_LEVEL > 40)
		{
			_level = Config.STARTING_LEVEL;
			if (_level > getMaxLevel())
			{
				_level = getMaxLevel();
			}
			_exp = Experience.getAbsoluteExp(_level);
		}
	}

	public SubClass()
	{
		// Used for specifying ALL attributes of a sub class directly,
		// using the preset default values.
		if (Config.STARTING_LEVEL > 40)
		{
			_level = Config.STARTING_LEVEL;
			if (_level > getMaxLevel())
			{
				_level = getMaxLevel();
			}
			_exp = Experience.getAbsoluteExp(_level);
		}
	}

	public PlayerClass getClassDefinition()
	{
		return _class;
	}

	public int getClassId()
	{
		return _class.getId();
	}

	public long getExp()
	{
		return _exp;
	}

	public long getSp()
	{
		return _sp;
	}

	public byte getLevel()
	{
		return _level;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	public boolean isDual()
	{
		return _isDual;
	}

	public int getCertificates()
	{
		return _certificates;
	}

	public byte getMaxLevel()
	{
		return _isDual ? Config.MAX_LEVEL : _maxLevel;
	}

	public void setClassId(int classId)
	{
		_class = PlayerClassTable.getInstance().getClassById(classId);
	}

	public void setExp(long expValue)
	{
		if (expValue > Experience.getAbsoluteExp(getMaxLevel() + 1) - 1)
		{
			expValue = Experience.getAbsoluteExp(getMaxLevel() + 1) - 1;
		}

		_exp = expValue;
	}

	public void setSp(long spValue)
	{
		_sp = spValue;
	}

	public void setClassIndex(int classIndex)
	{
		_classIndex = classIndex;
	}

	public void setIsDual(boolean isDual)
	{
		_isDual = isDual;
	}

	public void setCertificates(int certificates)
	{
		_certificates = certificates;
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

		_level = levelValue;
	}

	public void incLevel()
	{
		if (getLevel() == getMaxLevel())
		{
			return;
		}

		_level++;
		setExp(Experience.getAbsoluteExp(getLevel()));
	}

	public void decLevel()
	{
		if (getLevel() == 40)
		{
			return;
		}

		_level--;
		setExp(Experience.getAbsoluteExp(getLevel()));
	}
}
