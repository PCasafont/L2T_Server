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

import gnu.trove.TIntObjectHashMap;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.4 $ $Date: 2005/04/03 15:55:06 $
 */
public class SkillHandler {
	private TIntObjectHashMap<ISkillHandler> datatable;

	public static SkillHandler getInstance() {
		return SingletonHolder.instance;
	}

	private SkillHandler() {
		datatable = new TIntObjectHashMap<>();
	}

	public void registerSkillHandler(ISkillHandler handler) {
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types) {
			datatable.put(t.ordinal(), handler);
		}
	}

	public ISkillHandler getSkillHandler(L2SkillType skillType) {
		return datatable.get(skillType.ordinal());
	}

	/**
	 * @return
	 */
	public int size() {
		return datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final SkillHandler instance = new SkillHandler();
	}
}
