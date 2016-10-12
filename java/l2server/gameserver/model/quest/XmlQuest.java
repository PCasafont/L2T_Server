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

package l2server.gameserver.model.quest;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Pere
 */
public class XmlQuest extends Quest
{
	public class XmlQuestState
	{
		public int Id;
	}

	public XmlQuest(int questId, String name, String descr)
	{
		super(questId, name, descr);

		/*addStartNpc(_rafforty);
		addTalkId(_rafforty);
		addTalkId(_kier);
		addFirstTalkId(_jinia);
		addTalkId(_jinia);*/
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());

		if (st == null)
		{
			return event;
		}

		return event;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}

		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return null;
		}

		return null;
	}

	@Override
	public boolean canStart(L2PcInstance player)
	{
		return true;//player.getLevel() >= _minLevel && player.getLevel() <= _maxLevel;
	}
}
