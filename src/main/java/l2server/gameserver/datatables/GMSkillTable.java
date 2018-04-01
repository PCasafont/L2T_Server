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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.loader.annotations.Load;

/**
 * @author Gnacik
 */
public class GMSkillTable {
	private static final L2Skill[] gmSkills = new L2Skill[34];
	private static final int[] gmSkillsId = {14779, 14780, 14781, 14782, 14783, 14784, 14785, 14786, 14787, 14788, 14789, 14790, 14993, 14994, 14995};

	private GMSkillTable() {
	}
	
	@Load(dependencies = SkillTable.class)
	private void initialize() {
		if (Config.IS_CLASSIC) {
			return;
		}
		
		for (int i = 0; i < gmSkillsId.length; i++) {
			gmSkills[i] = SkillTable.getInstance().getInfo(gmSkillsId[i], 1);
		}
	}
	
	public static GMSkillTable getInstance() {
		return SingletonHolder.instance;
	}

	public L2Skill[] getGMSkills() {
		return gmSkills;
	}

	public static boolean isGMSkill(int skillid) {
		for (int id : gmSkillsId) {
			if (id == skillid) {
				return true;
			}
		}

		return false;
	}

	public void addSkills(L2PcInstance gmchar) {
		for (L2Skill s : getGMSkills()) {
			gmchar.addSkill(s, false); // Don't Save GM skills to database
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final GMSkillTable instance = new GMSkillTable();
	}
}
