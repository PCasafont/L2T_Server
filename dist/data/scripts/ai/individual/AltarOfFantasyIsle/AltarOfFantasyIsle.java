package ai.individual.AltarOfFantasyIsle;

import l2server.gameserver.Announcements;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yomi
 */

public class AltarOfFantasyIsle extends Quest {
	private static final int jisooId = 91003;
	private static final String qn = "AltarOfFantasyIsle";
	private static Map<Integer, Boolean> spawnInfo = new HashMap<Integer, Boolean>(3);
	private static final int[] raidIds = {91004, 91005, 80246};
	private static final int[] stoneIds = {9743, 1261, 20772};
	private static final int altarofFantasyIsleId = 91003;

	public AltarOfFantasyIsle(int questId, String name, String descr) {
		super(questId, name, descr);

		addSpawn(jisooId, -44247, 75489, -3654, 843, false, 0);

		addFirstTalkId(altarofFantasyIsleId);
		addStartNpc(altarofFantasyIsleId);
		addTalkId(altarofFantasyIsleId);

		for (int i : raidIds) {
			addKillId(i);
			spawnInfo.put(i, false);
		}
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player) {
		return "AltarOfFantasyIsle.html";
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		if (event.startsWith("trySpawnBoss")) {
			int bossId = Integer.valueOf(event.split(" ")[1]);
			int stoneId = 0;
			if (bossId == raidIds[0]) {
				stoneId = stoneIds[0];
			} else if (bossId == raidIds[1]) {
				stoneId = stoneIds[1];
			} else {
				stoneId = stoneIds[2];
			}

			if (stoneId == 0) //Cheating?
			{
				return null;
			}

			synchronized (spawnInfo) {
				if (!spawnInfo.get(bossId)) {
					if (!player.destroyItemByItemId(qn, stoneId, 1, player, true)) {
						return stoneId + "-no.html";
					}
				}
				Announcements.getInstance().announceToAll("Jisoo: One of the Descendants has been summoned... Death is coming...");
				spawnInfo.put(bossId, true); //Boss is spawned

				Attackable boss = (Attackable) addSpawn(bossId, npc.getX(), npc.getY() + 200, npc.getZ(), 0, false, 0, true);
				boss.setTarget(player);
				boss.addDamageHate(player, 9999, 9999);
				boss.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(Npc npc, Player player, boolean isPet) {
		synchronized (spawnInfo) {
			spawnInfo.put(npc.getNpcId(), false);
		}
		Announcements.getInstance().announceToAll("Descendant: Ughnnn... This... can't be... happening! Nooooo!");
		return super.onKill(npc, player, isPet);
	}

	public String onTalkNpc(Npc npc, Player player) {
		return null;
	}
	
	public static void main(String[] args) {
		new AltarOfFantasyIsle(-1, qn, "ai/individual");
	}
}
