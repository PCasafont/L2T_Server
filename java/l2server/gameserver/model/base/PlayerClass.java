/*
 * $Header: PlayerClass.java, 24/11/2005 12:56:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 24/11/2005 12:56:01 $
 * $Revision: 1 $
 * $Log: PlayerClass.java,v $
 * Revision 1  24/11/2005 12:56:01  luisantonioa
 * Added copyright notice
 *
 *
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

import l2server.gameserver.model.L2SkillLearn;
import l2server.gameserver.templates.item.L2Henna;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class PlayerClass
{
	private int id;
	private String name;
	private PlayerClass parent;
	private int awakensTo;
	private boolean isMage;
	private Race race;
	private int level;

	private Map<Long, L2SkillLearn> skills = new LinkedHashMap<>();

	private List<L2Henna> allowedDyes = new ArrayList<>();

	public PlayerClass(int id, String name, PlayerClass parent, int awakensTo, boolean isMage, int raceId, int level)
	{
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.awakensTo = awakensTo;
		this.isMage = isMage;
		this.race = raceId < 0 ? null : Race.values()[raceId];
		this.level = level;
	}

	public final int getId()
	{
		return this.id;
	}

	public final String getName()
	{
		return this.name;
	}

	public final PlayerClass getParent()
	{
		return this.parent;
	}

	public final int getAwakeningClassId()
	{
		return this.awakensTo;
	}

	public final boolean isMage()
	{
		return this.isMage;
	}

	public final int getLevel()
	{
		return this.level;
	}

	public final Race getRace()
	{
		return this.race;
	}

	public final void addSkill(long hash, L2SkillLearn skill)
	{
		this.skills.put(hash, skill);
	}

	public final Map<Long, L2SkillLearn> getSkills()
	{
		return this.skills;
	}

	public void addAllowedDye(L2Henna henna)
	{
		this.allowedDyes.add(henna);
	}

	public final List<L2Henna> getAllowedDyes()
	{
		return this.allowedDyes;
	}

	public final boolean isSummoner()
	{
		return this.id == 14 || this.id == 28 || this.id == 41 || this.id == 96 || this.id == 104 || this.id == 111 || this.id == 146 ||
				this.id == 176 || this.id == 177 || this.id == 178;
	}

	public final boolean childOf(PlayerClass cl)
	{
		if (this.parent == null)
		{
			return false;
		}

		if (this.parent == cl)
		{
			return true;
		}

		return this.parent.childOf(cl);
	}

	public final boolean equalsOrChildOf(PlayerClass cl)
	{
		return this == cl || childOf(cl);
	}

	public final int level()
	{
		if (this.parent == null)
		{
			return 0;
		}

		if (this.id == 184 || this.id == 185)
		{
			return 2;
		}

		return 1 + this.parent.level();
	}
}
