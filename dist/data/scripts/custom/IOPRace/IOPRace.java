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

package custom.IOPRace;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;

public class IOPRace extends Quest {
	private static final int RIGNOS = 32349;
	private static final int STAMP = 10013;
	private static final int KEY = 9694;

	private int player = -1;

	public IOPRace(int id, String name, String descr) {
		super(id, name, descr);
		addStartNpc(RIGNOS);
		addTalkId(RIGNOS);
		addFirstTalkId(RIGNOS);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		QuestState st = player.getQuestState(getName());
		if (st == null) {
			st = newQuestState(player);
		}

		if (player.getLevel() < 78) {
			return "32349-notavailable.htm";
		} else if (this.player != -1 && this.player == player.getObjectId() && st.getQuestItemsCount(STAMP) == 4) {
			return "32349-return.htm";
		} else if (this.player != -1) {
			return "32349-notavailable.htm";
		}

		npc.showChatWindow(player);
		return null;
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		QuestState st = player.getQuestState(getName());
		if (st == null) {
			st = newQuestState(player);
		}

		if (this.player == -1) {
			// clean old data
			player.stopSkillEffects(5239);
			if (player.getPet() != null) {
				player.getPet().stopSkillEffects(5239);
			}
			for (SummonInstance summon : player.getSummons()) {
				summon.stopSkillEffects(5239);
			}

			st.takeItems(STAMP, -1);
			st.set("1st", "0");
			st.set("2nd", "0");
			st.set("3rd", "0");
			st.set("4th", "0");

			Skill skill = SkillTable.getInstance().getInfo(5239, 5);
			if (skill != null) {
				skill.getEffects(npc, player);
				if (player.getPet() != null) {
					skill.getEffects(npc, player.getPet());
				}
			}

			startQuestTimer("timer", 1800000, null, null); // 30 min
			this.player = player.getObjectId();
		}

		return null;
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		String htmltext = "";

		if (event.equalsIgnoreCase("timer")) {
			this.player = -1;
			return null;
		} else if (event.equalsIgnoreCase("finish")) {
			if (this.player == player.getObjectId()) {
				QuestState st = player.getQuestState(getName());
				st.giveItems(KEY, 3);
				st.takeItems(STAMP, -1);
				st.exitQuest(true);
			}
		}

		return htmltext;
	}

	public static void main(String[] args) {
		new IOPRace(-1, "IOPRace", "custom");
	}
}
