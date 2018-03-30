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

package quests.Q10320_LetsGoToTheCentralSquare;

import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.GlobalQuest;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.serverpackets.TutorialShowHtml;

/**
 * @author Pere
 */
public class Q10320_LetsGoToTheCentralSquare extends Quest {
	// Quest
	public static String qn = "Q10320_LetsGoToTheCentralSquare";

	// NPC
	private int pantheon = 32972;
	private int theodore = 32975;

	public Q10320_LetsGoToTheCentralSquare(int questId, String name, String descr) {
		super(questId, name, descr);
		addStartNpc(pantheon);
		addTalkId(pantheon);
		addTalkId(theodore);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
		String htmltext = event;
		QuestState st = player.getQuestState(qn);

		if (st == null) {
			return htmltext;
		}

		if (npc.getNpcId() == pantheon && event.equalsIgnoreCase("32972-03.htm")) {
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound("ItemSound.quest_accept");
			player.sendPacket(new TutorialShowHtml(2, "..\\L2text\\QT_001_Radar_01.htm"));
			// Reset video if it was shown before
			st.deleteGlobalQuestVar("ZoneVid523400");
		} else if (npc.getNpcId() == theodore && event.equalsIgnoreCase("32975-02.htm") && st.getInt("cond") == 1) {
			st.unset("cond");
			st.giveItems(57, 3000);
			st.addExpAndSp(30, 100);
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(false);

			// Main quests state
			player.setGlobalQuestFlag(GlobalQuest.STARTING, 1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player) {
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null) {
			return htmltext;
		}

		if (npc.getNpcId() == pantheon) {
			switch (st.getState()) {
				case State.CREATED:
					if (canStart(player)) {
						htmltext = "32972-01.htm";
					} else {
						htmltext = "32972-00.htm";
					}
					break;
				case State.STARTED:
					htmltext = "32972-04.htm"; // TODO
					break;
				case State.COMPLETED:
					htmltext = "32972-05.htm"; // TODO
					break;
			}
		} else if (npc.getNpcId() == theodore && st.getInt("cond") == 1) {
			htmltext = "32975-01.htm";
		}
		return htmltext;
	}

	@Override
	public boolean canStart(L2PcInstance player) {
		return player.getLevel() <= 20;
	}

	public static void main(String[] args) {
		new Q10320_LetsGoToTheCentralSquare(10320, qn, "Going to the Square of Talking Island Village.");
	}
}
