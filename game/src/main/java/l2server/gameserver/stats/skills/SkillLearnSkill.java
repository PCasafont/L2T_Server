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

package l2server.gameserver.stats.skills;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.StatsSet;

public class SkillLearnSkill extends Skill {
	private final int[] learnSkillId;
	private final int[] learnSkillLvl;

	public SkillLearnSkill(StatsSet set) {
		super(set);

		String[] ar = set.getString("learnSkillId", "0").split(",");
		int[] ar2 = new int[ar.length];

		for (int i = 0; i < ar.length; i++) {
			ar2[i] = Integer.parseInt(ar[i]);
		}

		learnSkillId = ar2;

		ar = set.getString("learnSkillLvl", "1").split(",");
		ar2 = new int[learnSkillId.length];

		for (int i = 0; i < learnSkillId.length; i++) {
			ar2[i] = 1;
		}

		for (int i = 0; i < ar.length; i++) {
			ar2[i] = Integer.parseInt(ar[i]);
		}

		learnSkillLvl = ar2;
	}

	@Override
	public void useSkill(Creature activeChar, WorldObject[] targets) {
		if (!(activeChar instanceof Player)) {
			return;
		}

		final Player player = (Player) activeChar;
		Skill newSkill;

		for (int i = 0; i < learnSkillId.length; i++) {
			if (player.getSkillLevelHash(learnSkillId[i]) < learnSkillLvl[i] && learnSkillId[i] != 0) {
				newSkill = SkillTable.getInstance().getInfo(learnSkillId[i], learnSkillLvl[i]);
				if (newSkill != null) {
					player.addSkill(newSkill, true);
				}
			}
		}
	}
}
