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
public class L2ApInstance extends L2PcInstance
{
	public L2ApInstance(int objectId, L2PcTemplate template, String account, PcAppearance app)
	{
		super(objectId, template, account, app);
		getAI();
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					int classId = getCurrentClass().getParent().getAwakeningClassId();
					if (getClassId() == 188)
					{
						classId = 140;
					}
					else if (getClassId() == 189)
					{
						classId = 143;
					}

					switch (classId)
					{
						case 139:
							_ai = new L2AKnightAI(new L2PcInstance.AIAccessor());
							break;
						case 140:
							_ai = new L2AWarriorAI(new L2PcInstance.AIAccessor());
							break;
						case 141:
							_ai = new L2ARogueAI(new L2PcInstance.AIAccessor());
							break;
						case 142:
							_ai = new L2AArcherAI(new L2PcInstance.AIAccessor());
							break;
						case 143:
							_ai = new L2AWizardAI(new L2PcInstance.AIAccessor());
							break;
						case 144:
							_ai = new L2AEnchanterAI(new L2PcInstance.AIAccessor());
							break;
						case 145:
							_ai = new L2ASummonerAI(new L2PcInstance.AIAccessor());
							break;
						case 146:
							_ai = new L2AHealerAI(new L2PcInstance.AIAccessor());
							break;
					}
				}
				return _ai;
			}
		}
		return ai;
	}
}
