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

package quests.Q10361_RolesOfTheSeeker;

import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;

/**
 * @author Pere
 */
public class Q10361_RolesOfTheSeeker extends Quest {
	// Quest
	public static String qn = "Q10361_RolesOfTheSeeker";

	// NPC
	private int lakcis = 32977;
	private int chesha = 33449;

	public Q10361_RolesOfTheSeeker(int questId, String name, String descr) {
		super(questId, name, descr);
		addStartNpc(lakcis);
		addTalkId(lakcis);
		addTalkId(chesha);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null) {
			return htmltext;
		}

		if (npc.getNpcId() == lakcis && event.equalsIgnoreCase("32977-03.htm")) {
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
		} else if (npc.getNpcId() == chesha && event.equalsIgnoreCase("33449-03.htm") && st.getInt("cond") == 1) {
			st.unset("cond");
			st.giveItems(22, 1);
			st.giveItems(29, 1);
			st.giveItems(57, 34000);
			st.addExpAndSp(35000, 6500);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);

			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.YE_SAGIRA, 10);
		}
		return htmltext;
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null) {
			return htmltext;
		}

		if (npc.getNpcId() == lakcis) {
			switch (st.getState()) {
				case State.CREATED:
					if (canStart(player)) {
						htmltext = "32977-01.htm";
					} else {
						htmltext = "32977-00.htm";
					}
					break;
				case State.STARTED:
					htmltext = "32977-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32977-05.htm"; // TODO
					break;
			}
		} else if (npc.getNpcId() == chesha && st.getInt("cond") == 1) {
			htmltext = "33449-01.htm";
		}
		return htmltext;
	}

	@Override
	public boolean canStart(Player player) {
		return player.getLevel() >= 10 && player.getLevel() <= 20;
	}

	public static void main(String[] args) {
		new Q10361_RolesOfTheSeeker(10361, qn, "Entering the Ye Sagira Ruins.");
	}
}
