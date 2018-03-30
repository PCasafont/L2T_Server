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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ai.L2CharacterAI;
import l2server.gameserver.ai.aplayer.*;
import l2server.gameserver.model.actor.appearance.PcAppearance;
import l2server.gameserver.templates.chars.L2PcTemplate;

/**
 * @author Pere
 */
public class L2ApInstance extends L2PcInstance {
	public L2ApInstance(int objectId, L2PcTemplate template, String account, PcAppearance app) {
		super(objectId, template, account, app);
		getAI();
	}

	@Override
	public L2CharacterAI initAI() {
		int classId = getCurrentClass().getParent().getAwakeningClassId();
		if (getClassId() == 188) {
			classId = 140;
		} else if (getClassId() == 189) {
			classId = 143;
		}
		
		switch (classId) {
			case 139:
				return new L2AKnightAI(this);
			case 140:
				return new L2AWarriorAI(this);
			case 141:
				return new L2ARogueAI(this);
			case 142:
				return new L2AArcherAI(this);
			case 143:
				return new L2AWizardAI(this);
			case 144:
				return new L2AEnchanterAI(this);
			case 145:
				return new L2ASummonerAI(this);
			case 146:
				return new L2AHealerAI(this);
			default:
				return new L2AWarriorAI(this);
		}
	}
}
