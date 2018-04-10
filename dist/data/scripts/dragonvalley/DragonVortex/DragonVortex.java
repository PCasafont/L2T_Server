package dragonvalley.DragonVortex;

import l2server.gameserver.instancemanager.QuestManager;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.model.quest.QuestState;
import l2server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author LasTravel
 */

public class DragonVortex extends Quest {
	private static Logger log = LoggerFactory.getLogger(DragonVortex.class.getName());

	private static final boolean Debug = false;

	private static Npc raid = null;
	private static final int dragonVortex = 32871;
	private static final int largeDragonBone = 17248;
	private static final int[] bosses = {25718, 25719, 25720, 25721, 25722, 25723, 25724};

	public DragonVortex(int questId, String name, String descr) {
		super(questId, name, descr);
		addTalkId(dragonVortex);
		addStartNpc(dragonVortex);
		addFirstTalkId(dragonVortex);

		for (int id : bosses) {
			addKillId(id);
		}
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		if (Debug) {
			log.warn(getName() + ": onKill: " + npc.getName());
		}

		raid = null;

		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onFirstTalk(Npc npc, Player player) {
		QuestState st = player.getQuestState(getName());
		if (st == null) {
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return "32871.htm";
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (Debug) {
			log.warn(getName() + ": onAdvEvent: " + event);
		}

		QuestState st = player.getQuestState(getName());

		if (event.equalsIgnoreCase("spawn_boss")) {
			if (raid != null && !raid.isDead()) {
				return "32871-3.htm";
			}

			if (st.getQuestItemsCount(largeDragonBone) > 0) {
				st.takeItems(largeDragonBone, 1);
				raid = addSpawn(bosses[Rnd.get(bosses.length)],
						player.getX() - Rnd.get(500, 800),
						player.getY() - Rnd.get(500, 800),
						player.getZ(),
						0,
						true,
						0,
						true,
						0);
				return "32871-1.htm";
			} else {
				return "32871-2.htm";
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	public static void main(String[] args) {
		new DragonVortex(-1, "DragonVortex", "dragonvalley");
	}
}
