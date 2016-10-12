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

package l2server.gameserver.model.actor.appearance;

import l2server.gameserver.events.instanced.EventInstance.EventType;
import l2server.gameserver.model.actor.instance.L2ApInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public class PcAppearance
{
	// =========================================================
	// Data Field
	private L2PcInstance _owner;

	private int _face;
	private int _hairColor;
	private int _hairStyle;

	private boolean _sex; // Female true(1)

	/**
	 * true if the player is invisible
	 */
	private boolean _invisible = false;

	/**
	 * The current visible name of this player, not necessarily the real one
	 */
	private String _visibleName;

	/**
	 * The current visible title of this player, not necessarily the real one
	 */
	private String _visibleTitle;

	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int _nameColor = 0xFFFFFF;

	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int _titleColor = 0xFFFF77;

	// =========================================================
	// Constructor
	public PcAppearance(int face, int hColor, int hStyle, boolean sex)
	{
		_face = face;
		_hairColor = hColor;
		_hairStyle = hStyle;
		_sex = sex;
	}

	// =========================================================
	// Method - Public

	// =========================================================
	// Method - Private

	/**
	 * @param visibleName The visibleName to set.
	 */
	public final void setVisibleName(String visibleName)
	{
		_visibleName = visibleName;
	}

	/**
	 * @return Returns the visibleName.
	 */
	public final String getVisibleName()
	{
		if (_owner.isPlayingEvent() && (_owner.getEvent().getType() == EventType.DeathMatch ||
				_owner.getEvent().getType() == EventType.Survival ||
				_owner.getEvent().getType() == EventType.KingOfTheHill))
		{
			return "Event Participant";
		}

		if (_visibleName == null)
		{
			_visibleName = getOwner().getName();
		}
		return _visibleName;
	}

	/**
	 * @param visibleTitle The visibleTitle to set.
	 */
	public final void setVisibleTitle(String visibleTitle)
	{
		_visibleTitle = visibleTitle;
	}

	/**
	 * @return Returns the visibleTitle.
	 */
	public final String getVisibleTitle()
	{
		if (_owner.isPlayingEvent() && (_owner.getEvent().getType() == EventType.DeathMatch ||
				_owner.getEvent().getType() == EventType.Survival ||
				_owner.getEvent().getType() == EventType.KingOfTheHill))
		{
			return "";
		}

		if (_owner instanceof L2ApInstance)
		{
			String title = "L2 Tenkai";
			if (_owner.getParty() != null)
			{
				title += " #" + _owner.getParty().getLeader().getObjectId() % 100;
			}
			return title;
		}

		if (_visibleTitle == null)
		{
			_visibleTitle = getOwner().getTitle();
		}

		if (_visibleTitle.equalsIgnoreCase("wtb ballance") || _visibleTitle.equalsIgnoreCase("wtb balance"))
		{
			return "WTB BRAIN";
		}

		return _visibleTitle;
	}

	// =========================================================
	// Property - Public
	public final int getFace()
	{
		return _face;
	}

	/**
	 */
	public final void setFace(int value)
	{
		_face = value;
	}

	public final int getHairColor()
	{
		return _hairColor;
	}

	/**
	 */
	public final void setHairColor(int value)
	{
		_hairColor = value;
	}

	public final int getHairStyle()
	{
		return _hairStyle;
	}

	/**
	 */
	public final void setHairStyle(int value)
	{
		_hairStyle = value;
	}

	/**
	 * @return true if char is female
	 */
	public final boolean getSex()
	{
		return _sex;
	}

	/**
	 */
	public final void setSex(boolean isfemale)
	{
		_sex = isfemale;
	}

	public void setInvisible()
	{
		_invisible = true;
	}

	public void setVisible()
	{
		_invisible = false;
	}

	public boolean getInvisible()
	{
		return _invisible;
	}

	public int getNameColor()
	{
		return _nameColor;
	}

	public void setNameColor(int nameColor)
	{
		if (nameColor < 0)
		{
			return;
		}

		_nameColor = nameColor;
	}

	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	public int getTitleColor()
	{
		return _titleColor;
	}

	public void setTitleColor(int titleColor)
	{
		if (titleColor < 0)
		{
			return;
		}

		_titleColor = titleColor;
	}

	public void setTitleColor(int red, int green, int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(L2PcInstance owner)
	{
		_owner = owner;
	}

	/**
	 * @return Returns the owner.
	 */
	public L2PcInstance getOwner()
	{
		return _owner;
	}
}
