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

package custom.HeroCirclet;

import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;

public class HeroCirclet extends Quest {
	private static final int[] npcIds = {31690, 31769, 31770, 31771, 31772};

	public HeroCirclet(int questId, String name, String descr) {
		super(questId, name, descr);
		for (int i : npcIds) {
			addStartNpc(i);
			addTalkId(i);
		}
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null) {
			st = newQuestState(player);
		}

		if (player.isHero()) {
			if (player.getInventory().getItemByItemId(6842) == null) {
				st.giveItems(6842, 1);
			} else {
				htmltext = "already_have_circlet.htm";
			}
		} else {
			htmltext = "no_hero.htm";
		}

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args) {
		new HeroCirclet(-1, "HeroCirclet", "custom");
	}
}
