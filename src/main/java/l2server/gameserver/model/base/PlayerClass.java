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
import l2server.gameserver.templates.item.HennaTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class PlayerClass {
	private int id;
	private String name;
	private PlayerClass parent;
	private int awakensTo;
	private boolean isMage;
	private Race race;
	private int level;

	private Map<Long, L2SkillLearn> skills = new LinkedHashMap<>();

	private List<HennaTemplate> allowedDyes = new ArrayList<>();

	public PlayerClass(int id, String name, PlayerClass parent, int awakensTo, boolean isMage, int raceId, int level) {
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.awakensTo = awakensTo;
		this.isMage = isMage;
		race = raceId < 0 ? null : Race.values()[raceId];
		this.level = level;
	}

	public final int getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final PlayerClass getParent() {
		return parent;
	}

	public final int getAwakeningClassId() {
		return awakensTo;
	}

	public final boolean isMage() {
		return isMage;
	}

	public final int getLevel() {
		return level;
	}

	public final Race getRace() {
		return race;
	}

	public final void addSkill(long hash, L2SkillLearn skill) {
		skills.put(hash, skill);
	}

	public final Map<Long, L2SkillLearn> getSkills() {
		return skills;
	}

	public void addAllowedDye(HennaTemplate henna) {
		allowedDyes.add(henna);
	}

	public final List<HennaTemplate> getAllowedDyes() {
		return allowedDyes;
	}

	public final boolean isSummoner() {
		return id == 14 || id == 28 || id == 41 || id == 96 || id == 104 || id == 111 || id == 146 || id == 176 || id == 177 || id == 178;
	}

	public final boolean childOf(PlayerClass cl) {
		if (parent == null) {
			return false;
		}

		if (parent == cl) {
			return true;
		}

		return parent.childOf(cl);
	}

	public final boolean equalsOrChildOf(PlayerClass cl) {
		return this == cl || childOf(cl);
	}

	public final int level() {
		if (parent == null) {
			return 0;
		}

		if (id == 184 || id == 185) {
			return 2;
		}

		return 1 + parent.level();
	}
}
