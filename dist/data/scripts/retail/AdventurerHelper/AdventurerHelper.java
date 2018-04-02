package retail.AdventurerHelper;

import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.util.Rnd;

/**
 * @author LasTravel
 * <p>
 * Source: http://l2wiki.com/Adventurer_Helpers
 */

public class AdventurerHelper extends Quest {
	private static final int adventurerHelper = 33463;
	private static final int adventurerSupportGoods = 32241;

	public AdventurerHelper(int questId, String name, String descr) {
		super(questId, name, descr);
		addStartNpc(adventurerHelper);
		addTalkId(adventurerHelper);
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		QuestState st = player.getQuestState(getName());
		Quest q = QuestManager.getInstance().getQuest(getName());

		if (st == null || q == null) {
			q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}

		long _curr_time = System.currentTimeMillis();
		String value = q.loadGlobalQuestVar(player.getAccountName());
		long _reuse_time = value == "" ? 0 : Long.parseLong(value);

		if (_curr_time > _reuse_time) {
			st.giveItems(adventurerSupportGoods, 1);
			q.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + 86400000)); //24h
		}

		return "33463-" + Rnd.get(1, 236) + ".htm";
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.equalsIgnoreCase("showRandom")) {
			return "33463-" + Rnd.get(1, 236) + ".htm";
		}

		return super.onAdvEvent(event, npc, player);
	}

	public static void main(String[] args) {
		new AdventurerHelper(-1, "AdventurerHelper", "retail");
	}
}
