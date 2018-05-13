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
import l2server.gameserver.model.L2PledgeSkillLearn;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JIV
 */
public class PledgeSkillTree {
	private static Logger log = LoggerFactory.getLogger(PledgeSkillTree.class.getName());

	private Map<Long, L2PledgeSkillLearn> skillTree = new HashMap<>();

	public PledgeSkillTree() {
	}

	public static PledgeSkillTree getInstance() {
		return SingletonHolder.instance;
	}
	
	@Load(dependencies = SkillTable.class)
	public void load() {
		skillTree.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skilltrees/pledgeSkillTree.xml");
		if (file.exists()) {
			XmlDocument doc = new XmlDocument(file);
			for (XmlNode d : doc.getChildren()) {
				if (d.getName().equalsIgnoreCase("skill")) {
					int skillId;
					int skillLvl;
					int clanLvl;
					int reputation;

					if (!d.hasAttribute("id")) {
						log.error("[PledgeSkillTree] Missing id, skipping");
						continue;
					}
					skillId = d.getInt("id");

					if (!d.hasAttribute("level")) {
						log.error("[PledgeSkillTree] Missing level, skipping");
						continue;
					}
					skillLvl = d.getInt("level");

					if (!d.hasAttribute("reputation")) {
						log.error("[PledgeSkillTree] Missing reputation, skipping");
						continue;
					}
					reputation = d.getInt("reputation");

					if (!d.hasAttribute("clanLevel")) {
						log.error("[PledgeSkillTree] Missing clan_level, skipping");
						continue;
					}
					clanLvl = d.getInt("clanLevel");

					Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
					if (skill == null) {
						log.error("[PledgeSkillTree] Skill " + skillId + " not exist, skipping");
						continue;
					}

					skillTree.put(SkillTable.getSkillHashCode(skill), new L2PledgeSkillLearn(skillId, skillLvl, clanLvl, reputation));
				}
			}
		}
		log.info("Loaded " + skillTree.size() + " Pledge Skills");
	}

	public L2PledgeSkillLearn[] getAvailableSkills(Player cha) {
		List<L2PledgeSkillLearn> result = new ArrayList<>();
		Map<Long, L2PledgeSkillLearn> skills = skillTree;

		if (skills == null) {
			// the skillTree for this class is undefined, so we give an empty list

			log.warn("No clan skills defined!");
			return new L2PledgeSkillLearn[]{};
		}

		Skill[] oldSkills = cha.getClan().getAllSkills();

		for (L2PledgeSkillLearn temp : skills.values()) {
			if (temp.getBaseLevel() <= cha.getClan().getLevel()) {
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++) {
					if (oldSkills[j].getId() == temp.getId()) {
						knownSkill = true;

						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1) {
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1) {
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2PledgeSkillLearn[result.size()]);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final PledgeSkillTree instance = new PledgeSkillTree();
	}
}
