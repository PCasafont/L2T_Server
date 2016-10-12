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

package l2server.gameserver.handler;

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.templates.skills.L2SkillTargetType;

/**
 * @author Max
 */
public interface ISkillTargetTypeHandler
{
	/**
	 * @param skill
	 * @param activeChar
	 * @param onlyFirst
	 * @param target
	 * @return
	 */
	L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target);

	/**
	 * @return
	 */
	Enum<L2SkillTargetType> getTargetType();
}
