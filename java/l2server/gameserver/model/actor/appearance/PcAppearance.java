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
	private L2PcInstance owner;

	private int face;
	private int hairColor;
	private int hairStyle;

	private boolean sex; // Female true(1)

	/**
	 * true if the player is invisible
	 */
	private boolean invisible = false;

	/**
	 * The current visible name of this player, not necessarily the real one
	 */
	private String visibleName;

	/**
	 * The current visible title of this player, not necessarily the real one
	 */
	private String visibleTitle;

	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int nameColor = 0xFFFFFF;

	/**
	 * The hexadecimal Color of players name (white is 0xFFFFFF)
	 */
	private int titleColor = 0xFFFF77;

	// =========================================================
	// Constructor
	public PcAppearance(int face, int hColor, int hStyle, boolean sex)
	{
		this.face = face;
		hairColor = hColor;
		hairStyle = hStyle;
		this.sex = sex;
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
		this.visibleName = visibleName;
	}

	/**
	 * @return Returns the visibleName.
	 */
	public final String getVisibleName()
	{
		if (owner.isPlayingEvent() && (owner.getEvent().getType() == EventType.DeathMatch ||
				owner.getEvent().getType() == EventType.Survival ||
				owner.getEvent().getType() == EventType.KingOfTheHill))
		{
			return "Event Participant";
		}

		if (visibleName == null)
		{
			visibleName = getOwner().getName();
		}
		return visibleName;
	}

	/**
	 * @param visibleTitle The visibleTitle to set.
	 */
	public final void setVisibleTitle(String visibleTitle)
	{
		this.visibleTitle = visibleTitle;
	}

	/**
	 * @return Returns the visibleTitle.
	 */
	public final String getVisibleTitle()
	{
		if (owner.isPlayingEvent() && (owner.getEvent().getType() == EventType.DeathMatch ||
				owner.getEvent().getType() == EventType.Survival ||
				owner.getEvent().getType() == EventType.KingOfTheHill))
		{
			return "";
		}

		if (owner instanceof L2ApInstance)
		{
			String title = "L2 Tenkai";
			if (owner.getParty() != null)
			{
				title += " #" + owner.getParty().getLeader().getObjectId() % 100;
			}
			return title;
		}

		if (visibleTitle == null)
		{
			visibleTitle = getOwner().getTitle();
		}

		if (visibleTitle.equalsIgnoreCase("wtb ballance") || visibleTitle.equalsIgnoreCase("wtb balance"))
		{
			return "WTB BRAIN";
		}

		return visibleTitle;
	}

	// =========================================================
	// Property - Public
	public final int getFace()
	{
		return face;
	}

	/**
	 */
	public final void setFace(int value)
	{
		face = value;
	}

	public final int getHairColor()
	{
		return hairColor;
	}

	/**
	 */
	public final void setHairColor(int value)
	{
		hairColor = value;
	}

	public final int getHairStyle()
	{
		return hairStyle;
	}

	/**
	 */
	public final void setHairStyle(int value)
	{
		hairStyle = value;
	}

	/**
	 * @return true if char is female
	 */
	public final boolean getSex()
	{
		return sex;
	}

	/**
	 */
	public final void setSex(boolean isfemale)
	{
		sex = isfemale;
	}

	public void setInvisible()
	{
		invisible = true;
	}

	public void setVisible()
	{
		invisible = false;
	}

	public boolean getInvisible()
	{
		return invisible;
	}

	public int getNameColor()
	{
		return nameColor;
	}

	public void setNameColor(int nameColor)
	{
		if (nameColor < 0)
		{
			return;
		}

		this.nameColor = nameColor;
	}

	public void setNameColor(int red, int green, int blue)
	{
		nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	public int getTitleColor()
	{
		return titleColor;
	}

	public void setTitleColor(int titleColor)
	{
		if (titleColor < 0)
		{
			return;
		}

		this.titleColor = titleColor;
	}

	public void setTitleColor(int red, int green, int blue)
	{
		titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}

	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(L2PcInstance owner)
	{
		this.owner = owner;
	}

	/**
	 * @return Returns the owner.
	 */
	public L2PcInstance getOwner()
	{
		return owner;
	}
}
