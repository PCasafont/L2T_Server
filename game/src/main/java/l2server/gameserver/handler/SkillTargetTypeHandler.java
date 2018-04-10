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

import l2server.gameserver.templates.skills.SkillTargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Max
 */
public class SkillTargetTypeHandler {
	private static Logger log = LoggerFactory.getLogger(SkillTargetTypeHandler.class.getName());

	private Map<Enum<SkillTargetType>, ISkillTargetTypeHandler> datatable;

	public static SkillTargetTypeHandler getInstance() {
		return SingletonHolder.instance;
	}

	private SkillTargetTypeHandler() {
		datatable = new HashMap<>();
	}

	public void registerSkillTargetType(ISkillTargetTypeHandler handler) {
		Enum<SkillTargetType> ids = handler.getTargetType();
		datatable.put(ids, handler);
	}

	public ISkillTargetTypeHandler getSkillTarget(Enum<SkillTargetType> skillTargetType) {

		log.debug("getting handler for command: " + skillTargetType.toString() + " -> " + (datatable.get(skillTargetType) != null));
		return datatable.get(skillTargetType);
	}

	public int size() {
		return datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final SkillTargetTypeHandler instance = new SkillTargetTypeHandler();
	}
}
