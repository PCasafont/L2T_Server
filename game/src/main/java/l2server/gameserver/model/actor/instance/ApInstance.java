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

import l2server.gameserver.ai.CreatureAI;
import l2server.gameserver.ai.aplayer.*;
import l2server.gameserver.model.actor.appearance.PcAppearance;
import l2server.gameserver.templates.chars.PcTemplate;

/**
 * @author Pere
 */
public class ApInstance extends Player {
	public ApInstance(int objectId, PcTemplate template, String account, PcAppearance app) {
		super(objectId, template, account, app);
		getAI();
	}

	@Override
	public CreatureAI initAI() {
		int classId = getCurrentClass().getParent().getAwakeningClassId();
		if (getClassId() == 188) {
			classId = 140;
		} else if (getClassId() == 189) {
			classId = 143;
		}
		
		switch (classId) {
			case 139:
				return new AKnightAI(this);
			case 140:
				return new AWarriorAI(this);
			case 141:
				return new ARogueAI(this);
			case 142:
				return new AArcherAI(this);
			case 143:
				return new AWizardAI(this);
			case 144:
				return new AEnchanterAI(this);
			case 145:
				return new ASummonerAI(this);
			case 146:
				return new AHealerAI(this);
			default:
				return new AWarriorAI(this);
		}
	}
}
