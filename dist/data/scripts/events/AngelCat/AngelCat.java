package events.AngelCat;

import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author LasTravel
 * <p>
 * Strange retail like event.
 */

public class AngelCat extends Quest {
	private static final int angelCat = 4308;
	private static final int angelCatBlessing = 35669;

	private static final int[][] spawns =
			{{86891, -142848, -1336, 26000}, {43908, -47714, -792, 49999}, {-14081, 123829, -3120, 40959}, {147265, 25624, -2008, 16384},
					{82936, 53093, -1488, 16905}, {111315, 219409, -3544, 49756}, {83673, 147992, -3400, 32767}, {16117, 142909, -2696, 16000},
					{-114199, 254128, -1528, 64706}, {-81006, 149991, -3040, 0}, {117295, 76726, -2688, 49151}, {207625, 86977, -1024, 18048}};

	public AngelCat(int questId, String name, String descr) {
		super(questId, name, descr);

		addStartNpc(angelCat);
		addTalkId(angelCat);
		addFirstTalkId(angelCat);

		for (int[] spawn : spawns) {
			addSpawn(angelCat, spawn[0], spawn[1], spawn[2], spawn[3], false, 0);
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player) {
		QuestState st = player.getQuestState(getName());
		if (st == null) {
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return "4308.htm";
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
		QuestState st = player.getQuestState(getName());
		Quest q = QuestManager.getInstance().getQuest(getName());

		if (event.equalsIgnoreCase("getGift")) {
			long _curr_time = System.currentTimeMillis();
			String value = q.loadGlobalQuestVar(player.getAccountName());
			long _reuse_time = value == "" ? 0 : Long.parseLong(value);

			if (_curr_time > _reuse_time) {
				st.giveItems(angelCatBlessing, 1);
				q.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + 86400000)); //24h
			} else {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACCOUNT_ALREADY_RECEIVED_A_GIFT_ONLY_ONCE_PER_ACCOUNT));
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	public static void main(String[] args) {
		new AngelCat(-1, "AngelCat", "events");
	}
}
